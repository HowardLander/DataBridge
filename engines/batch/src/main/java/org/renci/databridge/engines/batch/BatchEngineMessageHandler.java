package org.renci.databridge.engines.batch;
import org.renci.databridge.util.*;
import org.renci.databridge.persistence.metadata.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.rabbitmq.client.*;
import java.lang.Thread;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.io.IOException;
import org.renci.databridge.message.*;
import java.util.*;
import java.text.*;
import java.io.*;
import java.lang.reflect.*;


/**
 * This class is executed in a thread of the Batch Engine. The Batch Engine
 * calls the constructor for this class with the AMQP message as a parameter.  It's
 * up to this class to decode the message according to the headers and implement the
 * required behaviors.
 *
 * @author Howard Lander -RENCI (www.renci.org)
 */

public class BatchEngineMessageHandler implements AMQPMessageHandler {

   private Logger logger = Logger.getLogger ("org.renci.databridge.engine.batch");

  // These are the individual portions of the message.
  // The routing key.
  private String routingKey;

  // The properties class.
  private com.rabbitmq.client.AMQP.BasicProperties properties;

  // The headers, expressed as a map of strings.
  private Map<String, String> stringHeaders;

  // The byte array for the contents of the message.
  private byte[] bytes;

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
      bytes = amqpMessage.getBytes();
      logger.log(Level.INFO, "headers: " + stringHeaders);

      // get the message name
      String messageName = stringHeaders.get(BatchEngineMessage.NAME);

      // Call the function appropriate for the message
      if (null == messageName) {
         logger.log(Level.WARNING, "messageName is missing");
      } else if 
          (messageName.compareTo(BatchEngineMessage.CREATE_SIMILARITYMATRIX_JAVA_BATCH_METADATADB_URI) == 0) {
         processCreateSimilarityBatchMessage(stringHeaders, extra);
      } else {
         logger.log(Level.WARNING, "unimplemented messageName: " + messageName);
      }
  }


  /**
   * Handle the CREATE_SIMILARITYMATRIX_JAVA_METADATADB_URI message.  
   * @param stringHeaders A map of the headers provided in the message
   * @param extra An object containing the needed DAO objects plus a properties
   */
  public void processCreateSimilarityBatchMessage( Map<String, String> stringHeaders, Object extra) {
      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the class name
      String className = stringHeaders.get(BatchEngineMessage.CLASS);    
      if (null == className) {
         this.logger.log (Level.SEVERE, "No class name in message");
         return;
      }

      // Let's try to load the class. Note that we aren't going to execute the clas
      // in this code, so we are only making sure we trap any errors.
      Class<?> theClass = null;
      ClassLoader classLoader = BatchEngineMessageHandler.class.getClassLoader();
      try {
         theClass = classLoader.loadClass(className);
      } catch (ClassNotFoundException e) {
         this.logger.log (Level.SEVERE, "Can't instantiate class " + className);
         e.printStackTrace();
         return;
      }
      this.logger.log (Level.INFO, "Loaded class: " + className);

      // We'll need an object of this type as well.
      Constructor<?> cons = null;
      Object theObject = null;
      SimilarityProcessor thisProcessor = null;
      try {
         cons = (Constructor<?>) theClass.getConstructor(null);
         theObject = cons.newInstance(null);
         thisProcessor = (SimilarityProcessor) theObject;
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't create instance");
         e.printStackTrace();
         return;
      }

      // 2) the name space
      String nameSpace = stringHeaders.get(BatchEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No name space in message");
         return;
      }

      // 3) the outputFile
      String outputFile = stringHeaders.get(BatchEngineMessage.OUTPUT_FILE);    
      if (null == outputFile) {
         this.logger.log (Level.SEVERE, "No output URI in message");
         return;
      }

      // Process the array of extra objects
      Object extraArray[] = (Object[]) extra;

      // The extra parameter in this case is the MetadataDAOFactory followed by the Properties object.
      MetadataDAOFactory theFactory = (MetadataDAOFactory) extraArray[0];
      if (null == theFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      }

      Properties theProps = (Properties) extraArray[1];
      if (null == theProps) {
         this.logger.log (Level.SEVERE, "Properties object is null");
         return;
      }

      CollectionDAO theCollectionDAO = theFactory.getCollectionDAO();
      if (null == theCollectionDAO) {
         this.logger.log (Level.SEVERE, "CollectionDAO is null");
         return;
      } 

      SimilarityInstanceDAO theSimilarityInstanceDAO = theFactory.getSimilarityInstanceDAO();
      if (null == theSimilarityInstanceDAO) {
         this.logger.log (Level.SEVERE, "SimilarityInstanceDAO is null");
         return;
      }

      // Let's add the SimilarityInstance.
      SimilarityInstanceTransferObject theSimilarityInstance = new SimilarityInstanceTransferObject();
      theSimilarityInstance.setNameSpace(nameSpace);
      theSimilarityInstance.setClassName(className);
      theSimilarityInstance.setMethod("compareCollections");
      theSimilarityInstance.setOutput("file://" + outputFile);

      // let's find the highest version for this combination of nameSpace, className and method (if any)
      HashMap<String, String> versionMap = new HashMap<String, String>();
      versionMap.put("nameSpace", nameSpace);
      versionMap.put("className", className);
      versionMap.put("method", "compareCollections");
      
      HashMap<String, String> sortMap = new HashMap<String, String>();
      sortMap.put("version", SimilarityInstanceDAO.SORT_DESCENDING);
      Integer limit = new Integer(1);

      // This is for the case of no previous instance
      theSimilarityInstance.setVersion(1);
      Iterator<SimilarityInstanceTransferObject> versionIterator =
          theSimilarityInstanceDAO.getSimilarityInstances(versionMap, sortMap, limit);
      if (versionIterator.hasNext()) {
         // Found a previous instance
         SimilarityInstanceTransferObject prevInstance = versionIterator.next();
         theSimilarityInstance.setVersion(prevInstance.getVersion() + 1);
      }

      try {
         boolean result = theSimilarityInstanceDAO.insertSimilarityInstance(theSimilarityInstance);
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't insert similarity instance");
         return;
      }
      
      // Search for all of the collections in the nameSpace
      HashMap<String, String> searchMap = new HashMap<String, String>();
      searchMap.put("nameSpace", nameSpace);
 
      // We need an array list of collectionIds
      ArrayList<String> collectionIds = new ArrayList<String>();

      long nCollections = theCollectionDAO.countCollections(searchMap);
      this.logger.log (Level.INFO, "number of collections: " + nCollections);
      if (nCollections <= 0) {
         // Nothing to do, we can stop now
         this.logger.log (Level.INFO, "nCollections <= 0, so nothing to do");
      }

      // We also need the iterator of collections
      Iterator<CollectionTransferObject> collectionIterator = theCollectionDAO.getCollections(searchMap);

      // Here we have a small problem.  Our DB infrastructure supports "long"
      // cardinality for records, but the current similarity file uses a 
      // matrix implementation that "only" supports an int. However, when we
      // get past 2 billion collections, we'll figure out how to deal with this.
      // NOTE that handling this should probably be moved to the DAO level.
      int nCollectionsInt;
      if (nCollections > (long) Integer.MAX_VALUE) {
         this.logger.log (Level.SEVERE, "nCollections > Integer.MAX_VALUE");
         return;
      } else {
         nCollectionsInt = (int) nCollections;
      }

      // We are going to write out each collection as a json file. Then the individual batch processes can
      // read these json files before running the similarity algorithm.  This is needed because we can't
      // assume that the compute nodes where the batch processes are running will be able to access the
      // metadata
      String  collectionFileDir = 
         theProps.getProperty("org.renci.databridge.batch.collectionFileDir", "collectionDir");

      try {
         // Create a sub dir for this invocation
         Date now = new Date();
         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
         String dateString = format.format(now);
         String labeledTmpDir = collectionFileDir + "/" + nameSpace + "-" + dateString;

         File newTmpDir = new File(labeledTmpDir);

         // Create the directory if it does not already exist
         if (newTmpDir.exists() == false) {
             boolean result = newTmpDir.mkdirs();
             if (false == result) {
                 this.logger.log (Level.WARNING, "can't create path: " + labeledTmpDir);
                 return;
             }
         }

         // Now write a file for each of the collections
         int fileNumber = 0;
         while (collectionIterator.hasNext()) {
             String thisFile = labeledTmpDir + "/" + nameSpace + "." + Integer.toString(fileNumber);
             CollectionTransferObject getObj = collectionIterator.next();
             CollectionFileReadWrite.writeToFile(thisFile, getObj);
             fileNumber ++;
         }
         
      } catch (Exception e) {
         e.printStackTrace();
      }

      // How many ops do we want to do in each process?
      int opsPerProcess = 
         Integer.parseInt(theProps.getProperty("org.renci.databridge.batch.opsPerProcess", "1000"));

      // How many concurrent processes shall we fork off?
      int maxConcurrentProcesses = 
         Integer.parseInt(theProps.getProperty("org.renci.databridge.batch.maxConcurrentProcesses", "100"));

      // The size of the upper triangular similarity matrix 
      double nSimilarityOps = ((nCollectionsInt * nCollectionsInt) - nCollectionsInt) / 2;

      // How many total processes will we need?
      int nProcesses = (int) Math.ceil(nSimilarityOps / (double)opsPerProcess);

      // Assuming we get this far, we want to send out the next message
      AMQPComms ac = null;
      try {
         ac = new AMQPComms (theProps);
         String headers = ProcessedMetadataToNetworkFile.getSendHeaders (
                             nameSpace, theSimilarityInstance.getDataStoreId());
         this.logger.log (Level.INFO, "Send headers: " + headers);
         ac.publishMessage (new AMQPMessage (), headers, true);
         this.logger.log (Level.INFO, "Sent ProcessedMetadataToNetworkFile message.");
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception sending action message: " + e.getMessage());
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
