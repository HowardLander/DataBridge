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

public class BatchWorkerMessageHandler implements AMQPMessageHandler {

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

  // The string version of the contents of the message.
  private Map<String,String> stringContent;

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
      stringContent = amqpMessage.getStringContent();
      bytes = amqpMessage.getBytes();
      logger.log(Level.INFO, "headers ( null is OK): " + stringHeaders);
      logger.log(Level.INFO, "bytes: " + bytes.toString());
      logger.log(Level.INFO, "stringContent: " + stringContent);

      // get the message name
      String messageName = stringContent.get(BatchEngineMessage.NAME);

      // Call the function appropriate for the message
      if (null == messageName) {
         logger.log(Level.WARNING, "messageName is missing");
      } else if 
          (messageName.compareTo(BatchEngineMessage.CREATE_SIMILARITYMATRIXSUBSET_JAVA_BATCH_FILE) == 0) {
         processCreateSimilaritySubsetBatchMessage(stringHeaders, extra);
      } else {
         logger.log(Level.WARNING, "unimplemented messageName: " + messageName);
      }
  }


  /**
   * Handle the CREATE_SIMILARITYMATRIX_JAVA_MATCH_METADATADB_URI message.  
   * @param stringHeaders A map of the headers provided in the message
   * @param extra An object containing the needed DAO objects plus a properties
   */
  public void processCreateSimilaritySubsetBatchMessage( Map<String, String> stringHeaders, Object extra) {
      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the class name
      String className = stringContent.get(BatchEngineMessage.CLASS);    
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
      String nameSpace = stringContent.get(BatchEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No name space in message");
         return;
      }

      // 3) the outputFile
      String outputFile = stringContent.get(BatchEngineMessage.OUTPUT_FILE);    
      if (null == outputFile) {
         this.logger.log (Level.SEVERE, "No output file in message");
         return;
      }

      File file = new File(outputFile);
      FileWriter fw = null;
      BufferedWriter bw = null;

      try {
         // if file doesnt exists, then create it
         if (!file.exists()) {
             file.createNewFile();
         }

          fw = new FileWriter(file.getAbsoluteFile());
          bw = new BufferedWriter(fw);
      } catch (IOException e) {
         e.printStackTrace();
      }
      


     // 4) the startIndex
      String startIndexString = stringContent.get(BatchEngineMessage.START_INDEX);
      if (null == startIndexString) {
         this.logger.log (Level.SEVERE, "No start index in message");
         return;
      }
      int startIndex = Integer.parseInt(startIndexString);

      // 5) the count
      String countString = stringContent.get(BatchEngineMessage.COUNT);
      if (null == countString) {
         this.logger.log (Level.SEVERE, "No count in message");
         return;
      }
      int count = Integer.parseInt(countString);

      // 6) the inputDir
      String inputDir = stringContent.get(BatchEngineMessage.INPUT_DIR);
      if (null == inputDir) {
         this.logger.log (Level.SEVERE, "No input dir in message");
         return;
      }

      // 7) the dimension of the array
      String dimensionString = stringContent.get(BatchEngineMessage.DIMENSION);
      if (null == dimensionString) {
         this.logger.log (Level.SEVERE, "No dimension in message");
         return;
      }
      int dimension = Integer.parseInt(dimensionString);

      // Process the array of extra objects
      Object extraArray[] = (Object[]) extra;

      // The extra parameter in this case is the Properties object.
      Properties theProps = (Properties) extraArray[0];
      if (null == theProps) {
         this.logger.log (Level.SEVERE, "Properties object is null");
         return;
      }

      if (count <= 0) {
         // Nothing to do, we can stop now
         this.logger.log (Level.INFO, "count <= 0, so nothing to do");
      }

      // Declare a hashmap to store the collection transfer objects
      HashMap<Integer, CollectionTransferObject> ctoMap = new HashMap<Integer, CollectionTransferObject>();
      try {
         long startTime = System.currentTimeMillis();
         // Get the list of pairs for this invocation of the process.
         ArrayList<IndexPair> thePairs = BatchUtils.getPairList(dimension, startIndex, count);

         // Process each IndexPair in the list
         for (IndexPair thisPair: thePairs) {
             int index1 = thisPair.getIndex1();
             int index2 = thisPair.getIndex2();

             String file1 = inputDir + "/" + nameSpace + "." + Integer.toString(index1);
             String file2 = inputDir + "/" + nameSpace + "." + Integer.toString(index2);

             // Here we have a class space vs time tradeoff.  We need to read each of the collection 
             // objects into memory from the files written using CollectionFileReadWrite.writeToFile.
             // We can either read them in every time they are needed (dumping them after each use)
             // or keep them all in memory simultaneously and only read them once. We'll start with
             // keeping them in memory.
             // Check to see if the ctos we need are in the map already.  If not add them in
             if (false == ctoMap.containsKey(index1)) {
                ctoMap.put(index1, CollectionFileReadWrite.readFromFile(file1));
             }
             if (false == ctoMap.containsKey(index2)) {
                ctoMap.put(index2, CollectionFileReadWrite.readFromFile(file2));
             }

             // At this point we have both the collection objects we need, so we can in theory
             // execute the class.
             double similarity = 0.;
             similarity =  
                (double) thisProcessor.compareCollections(ctoMap.get(index1), ctoMap.get(index2));
             String resultString = 
                 Integer.toString(index1) + "," + Integer.toString(index2) + "," + Double.toString(similarity);
             bw.write(resultString);
             bw.newLine();
         }
         long endTime = System.currentTimeMillis();
         long elapsedTime = endTime - startTime;

         logger.log(Level.INFO, "Used " + elapsedTime + " milliseconds for " + count + " operations");
         bw.flush();
         bw.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

/*
      try {
         ac = new AMQPComms (theProps);
         String headers = ProcessedMetadataToNetworkFile.getSendHeaders (
                             nameSpace, theSimilarityInstance.getDataStoreId());
         this.logger.log (Level.INFO, "Send headers: " + headers);
         this.logger.log (Level.INFO, "Sent ProcessedMetadataToNetworkFile message.");
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception sending action message: " + e.getMessage());
      } finally {
         if (null != ac) {
             ac.shutdownConnection ();
         }
      }
 */
  }
 
  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

// todo

  }
} 
