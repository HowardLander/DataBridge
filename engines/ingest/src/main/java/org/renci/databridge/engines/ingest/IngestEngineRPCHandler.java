package org.renci.databridge.engines.ingest;
import org.renci.databridge.util.*;
import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.persistence.network.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.LinkedList;
import com.rabbitmq.client.*;
import java.lang.Thread;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.io.IOException;
import org.renci.databridge.message.*;
import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import com.google.gson.*;
import java.text.*;
import java.lang.IllegalArgumentException;

/**
 * This class is executed in a thread of the Ingest Engine. The Ingest Engine
 * calls the constructor for this class with the AMQP message as a parameter.  It's
 * up to this class to decode the message according to the headers and implement the
 * required behaviors.
 *
 * @author Howard Lander -RENCI (www.renci.org)
 */

public class IngestEngineRPCHandler implements AMQPMessageHandler {

   private Logger logger = Logger.getLogger ("org.renci.databridge.engine.ingest");

  protected MetadataDAOFactory metadataDAOFactory;
  protected String pathToAmqpPropsFile;

  protected int dbType;
  protected String dbName;
  protected String dbHost;
  protected int dbPort;
  protected String dbUser;
  protected String dbPwd;

  /**
   * Constructor for the RPC handler.  Takes all of the arguments the normal ingest handler take
   * and saves them in the instance so it can pass them to the instance of IngestMetadataAMQPMessageHandler
   * it's going to create.
   *
   * @param amqpMessage The message to handle.
   * @param extra An object containing the needed DAO objects plus the Properties object
   */
  public IngestEngineRPCHandler (int dbType, String dbName, String dbHost, int dbPort,
                                 String dbUser, String dbPwd, String pathToAmqpPropsFile) {

    MetadataDAOFactory mdf =
       MetadataDAOFactory.getMetadataDAOFactory (dbType, dbName, dbHost, dbPort, dbUser, dbPwd);
    this.metadataDAOFactory = mdf;
    this.pathToAmqpPropsFile = pathToAmqpPropsFile;
    this.dbType = dbType;
    this.dbName = dbName;
    this.dbHost = dbHost;
    this.dbPort = dbPort;
    this.dbUser = dbUser;
    this.dbPwd = dbPwd;
  }
  
  /**
   * This function essentially de-multiplexes the message by calling the
   * appropriate lower level handler based on the headers.
   *
   * @param amqpMessage The message to handle.
   * @param extra An object containing the needed DAO objects plus the Properties object
   */
  public void handle (AMQPMessage amqpMessage, Object extra) throws Exception {
      // Get the individual components of the the message and store
      // them in the fields
      Map<String, String> stringHeaders = amqpMessage.getStringHeaders();
      this.logger.log (Level.INFO, "headers: " + stringHeaders);
      this.logger.log (Level.INFO, "replyQueue in handle  is: " + amqpMessage.getReplyQueue());

      // get the message name
      String messageName = stringHeaders.get(IngestMetadataMessage.NAME);
      if (null == messageName) {
         this.logger.log (Level.WARNING, "messageName is missing");
         return;
      }
      this.logger.log (Level.INFO, "messageName: " + messageName);

      // Call the function appropriate for the message
      if (messageName.compareTo(IngestMetadataMessage.INSERT_METADATA_JAVA_FILEWITHPARAMS_METADATADB_RPC) 
             == 0) {
         processInsertMetadataJavaFileWithParamsMetadataDBRPC(stringHeaders, extra, amqpMessage);
      } else {
         this.logger.log (Level.WARNING, "unimplemented messageName: " + messageName);
      }
  }

    /**
     * Handle the FIND_CLOSEST_MATCHES_IN_NETWORK message finding the relevant
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     * @param amqpMessage The tag incoming message, needed to populate the outgoing message
     */
  public void processInsertMetadataJavaFileWithParamsMetadataDBRPC( Map<String, String> stringHeaders, 
                                                                     Object extra, 
                                                                     AMQPMessage inMessage) {

      this.logger.log (Level.INFO, "replyQueue is: " + inMessage.getReplyQueue());
      // Let's create a Gson object to use to convert our results struct to json
      Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

      // We are also going to want a results structure.
      DatabridgeResultsMessage theResults = null;

      // we will want to send out the return message
      AMQPMessage thisMessage = null;
      AMQPRpcComms ac = null;
      String headers = null;
      String theJsonResults = null;
      Properties theProps = new Properties();
      boolean loadError = false;
      try {
         theProps.load(new FileInputStream (this.pathToAmqpPropsFile));
         if (null == theProps) {
            this.logger.log (Level.SEVERE, "Properties object is null");
         }
         // Grap the properties we need so we can create the basic handler.
         ac = new AMQPRpcComms (theProps);
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception trying to load prop file: " + e.getMessage(), e);
         // Set up a failure message
         headers = 
            ReturnInsertMetadataJavaFileWithParams.getSendHeaders (DatabridgeResultsMessage.STATUS_ERROR);
         theJsonResults = 
            gson.toJson(new DatabridgeResultsMessage(false, "Caught Exception trying to load prop file"));
         thisMessage = AMQPMessage.initRPCReplyMessage(inMessage, theJsonResults);
         loadError = true;
      }

      if (loadError == false) {
         // We need several pieces of information before we can continue.  This info has to 
         // all be in the headers or we are toast.
         HashMap<String,String> validationMap = new HashMap<String,String>();
         validationMap.put("className", "");
         validationMap.put("nameSpace", "");
         validationMap.put("inputFile", "");
         validationMap.put("params", "");

         String returnString = inMessage.validateStringHeaders(stringHeaders, validationMap);
         if (returnString.compareTo(DatabridgeResultsMessage.STATUS_OK) != 0) {
            // The validation failed
            this.logger.log (Level.SEVERE, "Validation Failure: " + returnString);

            // Get the headers for the return message
            headers = 
              ReturnInsertMetadataJavaFileWithParams.getSendHeaders(DatabridgeResultsMessage.STATUS_ERROR);
            this.logger.log (Level.INFO, "Send headers: " + headers);

            theJsonResults = 
               gson.toJson(new DatabridgeResultsMessage(false, "Validation Failure: " + returnString));
            thisMessage = AMQPMessage.initRPCReplyMessage(inMessage, theJsonResults);
            this.logger.log (Level.INFO, "Sent ReturnInsertMetadataJavaFileWithParams message.");
         } else {
            // Validation succeeded
            // We declare an instance of the non-RPC handler so we can call it to
            // service the incoming message.
            IngestMetadataAMQPMessageHandler basicHandler = 
               new IngestMetadataAMQPMessageHandler(this.dbType, this.dbName, this.dbHost, this.dbPort,
                                    this.dbUser, this.dbPwd, this.pathToAmqpPropsFile);
 
            try { 
               // Call the routine from the basic handler
               theResults = basicHandler.processInsertMetadataFileWithParamsMessage(stringHeaders, extra);

               // Get the headers for the return message
               headers = 
                 ReturnInsertMetadataJavaFileWithParams.getSendHeaders (DatabridgeResultsMessage.STATUS_ERROR);
   
               theJsonResults = gson.toJson(theResults);
               thisMessage = AMQPMessage.initRPCReplyMessage(inMessage, theJsonResults);
            } catch (Exception e) {
               this.logger.log (Level.SEVERE, "Caught Exception calling basic handler: " + 
                                e.getMessage(), e);
               // Set up a failure message
               String basicResult = DatabridgeResultsMessage.STATUS_ERROR + 
                  " Caught Exception calling basic handler";
               headers = ReturnInsertMetadataJavaFileWithParams.getSendHeaders (basicResult);
               theJsonResults = 
                 gson.toJson(new DatabridgeResultsMessage(false, 
                               "Caught Exception calling basic handler" + e.getMessage()));
   
               thisMessage = AMQPMessage.initRPCReplyMessage(inMessage, theJsonResults);
            }
         }
      }

      // Now at this point, we want to publish the message whether we succeeded or failed.
      try { 
         ac.publishMessage ( thisMessage, headers, true);
         this.logger.log (Level.INFO, "Sent ReturnInsertMetadataJavaFileWithParams message.");
      } catch (Exception e) {
          this.logger.log (Level.SEVERE, "Caught Exception sending action message: " + e.getMessage(), e);
      } finally {
          if (null != ac) {
              ac.shutdownConnection ();
          }
      }
  }
 
  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

// todo

  }
} 
