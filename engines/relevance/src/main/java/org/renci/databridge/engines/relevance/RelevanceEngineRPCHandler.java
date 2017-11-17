package org.renci.databridge.engines.relevance;
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
import org.la4j.*;
import org.la4j.matrix.functor.*;
import java.util.*;
import java.net.*;
import java.io.*;
import com.google.gson.*;
import java.nio.file.*;
import java.text.*;

/**
 * This class is executed in a thread of the Relevance Engine. The Relevance Engine
 * calls the constructor for this class with the AMQP message as a parameter.  It's
 * up to this class to decode the message according to the headers and implement the
 * required behaviors.
 *
 * @author Howard Lander -RENCI (www.renci.org)
 */

public class RelevanceEngineRPCHandler implements AMQPMessageHandler {

   private Logger logger = Logger.getLogger ("org.renci.databridge.engine.relevance");

  // These are the individual portions of the message.
  // The routing key.
  private String routingKey;

  // The properties class.
  private com.rabbitmq.client.AMQP.BasicProperties properties;

  // The headers, expressed as a map of strings.
  private Map<String, String> stringHeaders;

  // The byte array for the contents of the message.
  private byte[] bytes;

  // Default version
  public static final int DEFAULT_VERSION = -1;
  
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
      routingKey = amqpMessage.getRoutingKey();
      properties = amqpMessage.getProperties();
      stringHeaders = amqpMessage.getStringHeaders();
      this.logger.log (Level.INFO, "headers: " + stringHeaders);
      bytes = amqpMessage.getBytes();

      // get the message name
      String messageName = stringHeaders.get(NetworkEngineMessage.NAME);
      if (null == messageName) {
         this.logger.log (Level.WARNING, "messageName is missing");
         return;
      }
      this.logger.log (Level.INFO, "messageName: " + messageName);

      // Call the function appropriate for the message
      if (messageName.compareTo(RelevanceEngineMessage.CREATE_SIMILARITYMATRIX_JAVA_METADATADB_URI_RPC) == 0) {
         processCreateSimilarityMatrixJavaMetadataDBURIRPC(stringHeaders, extra, amqpMessage);
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
  public DatabridgeResultsMessage processCreateSimilarityMatrixJavaMetadataDBURIRPC( 
                                                  Map<String, String> stringHeaders, Object extra, 
                                                  AMQPMessage inMessage) {

      String headers;  
      String fileName;  
      String theJsonResults;  
      AMQPMessage thisMessage;
      Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

      Object extraArray[] = (Object[]) extra;
      Properties theProps = (Properties) extraArray[1];
      if (null == theProps) {
         this.logger.log (Level.SEVERE, "Properties object is null");
         return new DatabridgeResultsMessage(false, "Properties object is null");
      }
 
      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.
      HashMap<String,String> validationMap = new HashMap<String,String>();
      validationMap.put("className", "");
      validationMap.put("nameSpace", "");
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
         this.logger.log (Level.INFO, "Sent Validation Failure as RPC reply");
      } else {
         try {
            // We need to add a header for the output dir. Since we have the properties object
            // let's use the defined temporary directory
            String tmpDir = theProps.getProperty("org.renci.databridge.misc.tmpDir", "/tmp/");
            String lastChar = tmpDir.substring(tmpDir.length() - 1);
            if (lastChar.compareTo("/") != 0) {
              // no '/' at the end of the tmpDir, lets add it
              tmpDir += '/';
            }
          // we have a directory, so we need to append a fileName
          // We'll let java create the tmp file name, then delete the file.
           try {
              File outFileObject = new File(tmpDir);

              // Create the directory if it does not already exist
              if (outFileObject.exists() == false) {
                  boolean result = outFileObject.mkdirs();
                  if (false == result) {
                      this.logger.log (Level.WARNING, "can't create path: " + tmpDir);
                      return new DatabridgeResultsMessage(false, "can't create path: " + tmpDir);
                  }
              }
              // Let's add the last element of the class name to the file name
              String fullClassName = (String) stringHeaders.get(RelevanceEngineMessage.CLASS);
              String nameSpace = (String) stringHeaders.get(RelevanceEngineMessage.NAME_SPACE);
              String lastClass = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

              // Let's add the date and time as well.
              Date now = new Date();
              SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
              String dateString = format.format(now);
              String labeledFileName = nameSpace + "-" + lastClass + "-" + dateString + ".net";
              fileName = tmpDir + labeledFileName;
            } catch (Exception e) {
              e.printStackTrace();
              return new DatabridgeResultsMessage(false, "Caught an exception trying to create a file" + e);
            }

            // Add the new header
            stringHeaders.put(RelevanceEngineMessage.OUTPUT_FILE, fileName);

            // We have all of the parameters we need. So we'll declare a basic handler and run the
            // corresponding routine from that handler.
            RelevanceEngineMessageHandler basicHandler = new RelevanceEngineMessageHandler();
            this.logger.log (Level.INFO, 
                             "Sending headers to processCreateSimilarityMessage: " + stringHeaders);
            DatabridgeResultsMessage results = 
               basicHandler.processCreateSimilarityMessage(stringHeaders, extra);

            theJsonResults = gson.toJson(results);
            thisMessage = AMQPMessage.initRPCReplyMessage(inMessage, theJsonResults);

            // Arguably we should create a new class here, but the headers don't matter much in RPC mode.
            headers = 
              ReturnInsertMetadataJavaFileWithParams.getSendHeaders (DatabridgeResultsMessage.STATUS_OK);
         } catch (Exception e) {
            this.logger.log (Level.SEVERE, "Caught Exception calling basic handler: " +
                             e.getMessage(), e);
            // Set up a failure message
             String basicResult = DatabridgeResultsMessage.STATUS_ERROR +
               " Caught Exception calling basic handler";

            // Arguably we should create a new class here, but the headers don't matter much in RPC mode.
            headers = ReturnInsertMetadataJavaFileWithParams.getSendHeaders (basicResult);
            theJsonResults = gson.toJson(new DatabridgeResultsMessage(false,
                               "Caught Exception calling basic handler" + e.getMessage()));

            thisMessage = AMQPMessage.initRPCReplyMessage(inMessage, theJsonResults);
         }

      }
      // Assuming we get this far, we want to send out the reply message
      AMQPRpcComms ac = null;
      try {
         ac = new AMQPRpcComms (theProps);
         ac.publishMessage ( thisMessage, headers, true);
         this.logger.log (Level.INFO, "Sent RPC Reply message.");
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception sending reply message: " + e.getMessage(), e);
      } finally {
         if (null != ac) {
             ac.shutdownConnection ();
         }
      }
      return new DatabridgeResultsMessage(true, DatabridgeResultsMessage.STATUS_OK);
  }
 
  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

// todo

  }
} 
