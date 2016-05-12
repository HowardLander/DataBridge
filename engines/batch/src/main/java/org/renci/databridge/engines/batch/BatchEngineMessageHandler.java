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
import java.nio.*;
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

  protected static final long LISTENER_TIMEOUT_MS = 1000;

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
   * Handle the CREATE_SIMILARITYMATRIX_JAVA_BATCH_METADATADB_URI message.  
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

      SimilarityFile theSimFile = new SimilarityFile(nCollectionsInt, nameSpace);
      theSimFile.setNameSpace(nameSpace);

      theSimFile.setSimilarityInstanceId(theSimilarityInstance.getDataStoreId());

      // We are going to write out each collection as a json file. Then the individual batch processes can
      // read these json files before running the similarity algorithm.  This is needed because we can't
      // assume that the compute nodes where the batch processes are running will be able to access the
      // metadata.  This does assume a commonly accessible file structure.  We may eventually have to do
      // something more sophisticated
      String  collectionFileDir = 
         theProps.getProperty("org.renci.databridge.batch.collectionFileDir", "collectionDir");

      // Create a sub dir for this invocation
      String labeledTmpDir;
      Date now = new Date();
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
      String dateString = format.format(now);
      labeledTmpDir = collectionFileDir + "/" + nameSpace + "-" + dateString;

      try {
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
             collectionIds.add(getObj.getDataStoreId());
             fileNumber ++;
         }
         
      } catch (Exception e) {
         e.printStackTrace();
      }

      // At this point we can add the array of data store ids to the simFile
      theSimFile.setCollectionIds(collectionIds);

      // How many ops do we want to do in each batch?
      int opsPerBatch = 
         Integer.parseInt(theProps.getProperty("org.renci.databridge.batch.opsPerBatch", "1000"));

      // How many concurrent processes shall we fork off?
      int maxWorkers = 
         Integer.parseInt(theProps.getProperty("org.renci.databridge.batch.maxWorkers", "100"));

      // The size of the upper triangular similarity matrix 
      double nSimilarityOps = ((nCollectionsInt * nCollectionsInt) - nCollectionsInt) / 2;
      this.logger.log (Level.INFO, "nSimilarityOps: " + nSimilarityOps);

      // How many total processes will we need?
      int nBatches = (int) Math.ceil(nSimilarityOps / (double)opsPerBatch);
      this.logger.log (Level.INFO, "nBatches: " + nBatches);

      int nWorkersToStart;
      if (nBatches <= maxWorkers) {
         // we don't need more than the max
         nWorkersToStart = nBatches;
      } else {
         nWorkersToStart = maxWorkers;
      }

      String stringWorkers = Integer.toString(nWorkersToStart);

      // Now we want to queue up all the messages
      AMQPDirectComms ac = null;
      try {
         // Here is where we need to start up whatever batch workers we need.
         String workerStartCommand = 
          theProps.getProperty("org.renci.databridge.batchWorker.startCommand", "dataBridgeBatchWorkerCtl.sh");
         String workerStartArgs = 
          theProps.getProperty("org.renci.databridge.batchWorker.startCommandArgs", "");
         String workerStopCommand = 
          theProps.getProperty("org.renci.databridge.batchWorker.stopCommand", "dataBridgeBatchWorkerCtl.sh");
         String workerStopArgs = 
          theProps.getProperty("org.renci.databridge.batchWorker.stopCommandArgs", "");
         String binDir = 
          theProps.getProperty("org.renci.databridge.misc.binDir", "/projects/dataBridge/bin");
         int maxRetries = 
          Integer.parseInt(theProps.getProperty("org.renci.databridge.batchWorker.maxRetries", "10"));
         ArrayList<String> allArgs = new ArrayList<String>();
      
         // Add the command first
         String executable = new String(binDir + "/" + workerStartCommand); 
         allArgs.add(executable);

         // Important note: we are assuming that the startCommand is called with all of the args passed in the
         // startCommandArgs property plus the number of workers we want. So the command has to conform to this
         // interface.
         for (String thisArg: workerStartArgs.split(" ")) {
            allArgs.add(thisArg);
         }
         allArgs.add(stringWorkers);

         ProcessBuilder thePB = new ProcessBuilder(allArgs);
         Process process = thePB.start();
        
         // Read out dir output
         InputStream is = process.getInputStream();
         InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr);
         this.logger.log (Level.INFO, "Running command: " + executable + " " + allArgs);
         String line;
         while ((line = br.readLine()) != null) {
            this.logger.log (Level.INFO, line);
         }

         // This command waits for the exit value of the command.
         int exitValue = process.waitFor(); 
         this.logger.log (Level.INFO, "Command exited with status " + exitValue);

         // Next we send all of the messages. Rabbit takes care of the round robin.
         ac = new AMQPDirectComms (theProps, true);
         int startIndex = 0;
         int nRemainingOps = (int) nSimilarityOps;

         // We are going to save the content for each message in case we need it
         // again to resend in case of failre
         HashMap<Integer, String> contentMap = new HashMap<Integer, String>();
         for (int i = 0; i < nBatches; i++) {
            // Build up the needed message
            int opsThisBatch = (nRemainingOps < opsPerBatch) ? nRemainingOps : opsPerBatch;
            // Each batch produces it's own output file that we will eventually composite into
            // a single file.
            String thisOutFile = labeledTmpDir + "/" + nameSpace + ".out." + Integer.toString(i);
            String theContent = CreateSimilarityMatrixSubsetJavaBatchFile.getSendHeaders(className, 
                                                                                         nameSpace, 
                                                                                         thisOutFile,
                                                                                         startIndex,
                                                                                         opsThisBatch,
                                                                                         labeledTmpDir,
                                                                                         nCollections);
            this.logger.log (Level.INFO, "Content for batch " + i + " is "  + theContent);

            contentMap.put(i, theContent);

            startIndex += opsThisBatch; 
            nRemainingOps -= opsThisBatch;
            // Send the message with a generated correlationId and reply to.
            AMQPMessage thisMessage = new AMQPMessage(theContent.toString().getBytes());
            ac.publishMessage(thisMessage, true, Integer.toString(i));
         }

         // Having sent all of the messages to the exchange, we need to wait for all of the 
         // workers to finish.  Once that's done we can combine the individual output files
         // into a complete network file. We also have to consider the possibility of one or 
         // more of the workers failing...
         int nCompleted = 0;
         long nRetries = 0;
         while (nCompleted < nBatches) {
             try {
                 AMQPMessage am = ac.receiveMessage (LISTENER_TIMEOUT_MS);
                 if (am != null) {
                     // The message handler needs the property file so it can send action messages, so we
                     // store it in an array of Objects along with the needed factory.
                     logger.log(Level.INFO, "received a message: " + am.getContent());
                     String message = am.getContent();
                     if (0 == message.compareTo(AMQPComms.MESSAGE_FAILURE)) {
                        // The call falled for some reason. As long as we have not exceeded
                        // the max number of retries, we will resend.
                        String thisTag = am.getTag();
                        logger.log(Level.INFO, "Message failure on batch: " + thisTag);
                        if (nRetries < maxRetries) {
                           AMQPMessage thisMessage = 
                              new AMQPMessage(contentMap.get(thisTag).getBytes());
                           ac.publishMessage(thisMessage, true, thisTag);
                           nRetries ++;
                        } else {
                           logger.log(Level.INFO, "Max number of retries exceeded: " + nRetries);
                           break;
                        }
                  
                        // The tag tells us which message to resend.
                     } else {
                        nCompleted ++;
                     }
                     ac.ackMessage(am);
                 }
             } catch (Exception e) {
                 // dispatch exception to handler st it doesn't stop dispatch thread
                 this.handleException (e);
   
                 // @todo deal with exceptions here.
            }
         }
         // At this point we are done with processing. So either we have completed all the work or not.
         // If everything worked, we want to compose the individual results into a result file. In either
         // case we need to remove all of the temp files.
         logger.log(Level.INFO, "nCompleted: " + nCompleted + " nBatches " + nBatches);
         if (nCompleted == nBatches) {
            // success
            for (int i = 0; i < nBatches; i++) {
               String thisContent = contentMap.get(i);
               logger.log(Level.INFO, "thisContent for tag " + i + " is " + thisContent);
               String thisTmpFile = CreateSimilarityMatrixSubsetJavaBatchFile.getOutputFile(thisContent);

               BufferedReader readBuff = new BufferedReader (new FileReader(thisTmpFile));
               // Read each line from the input file
               line = readBuff.readLine();
        
               while (line != null) {
                   // Typical line is row,column,value (ex. 0,1,0.0)
                   String[] lineParts = line.split(",");
                   int row = Integer.parseInt(lineParts[0]);
                   int col = Integer.parseInt(lineParts[1]);
                   double similarity = Double.parseDouble(lineParts[2]);
                   if (similarity > 0.0) {
                       this.logger.log (Level.INFO, "row: " + row + " col: " + col + " sim: " + similarity);
                       theSimFile.setSimilarityValue(row, col, similarity);
                   } else if (col == ((int)nCollections - 1)) {
                       // the the last column in the row and it's zero.  Set it to -1 so we avoid a bug in the
                       // CRSMatrix class.  Of course, we have to remove this when we read the file.
                       this.logger.log (Level.INFO, "setting (" + row + "," + col + ") to -1");
                       theSimFile.setSimilarityValue(row, col, -1);
                   }
                   line = readBuff.readLine();
               }
            }
         }
 
         // Write the network file
         theSimFile.writeToDisk(outputFile);

         // Now is the time to delete any generated tmp files.
         File labeledTmpFile = new File(labeledTmpDir);
         String[] allTmpFiles;
         if (labeledTmpFile.isDirectory()) {
            allTmpFiles = labeledTmpFile.list();
            for (int i = 0; i < allTmpFiles.length; i++) {
                File thisToDelete = new File(labeledTmpFile, allTmpFiles[i]);
                thisToDelete.delete();
            }
         }

         // And the directory itself
         labeledTmpFile.delete();
 
         // Shutdown the workers.
         ArrayList<String> stopArgs = new ArrayList<String>();
      
         // Add the command first
         String stopExecutable = new String(binDir + "/" + workerStopCommand); 
         stopArgs.add(stopExecutable);

         // Important note: we are assuming that the startCommand is called with all of the args passed in the
         // stopCommandArgs property. So the command has to conform to this interface.
         for (String thisArg: workerStopArgs.split(" ")) {
            stopArgs.add(thisArg);
         }

         thePB = new ProcessBuilder(stopArgs);
         process = thePB.start();
        
         // Read out dir output
         is = process.getInputStream();
         isr = new InputStreamReader(is);
         br = new BufferedReader(isr);
         this.logger.log (Level.INFO, "Running command: " + executable + " " + stopArgs);
         while ((line = br.readLine()) != null) {
            this.logger.log (Level.INFO, line);
         }

         // This command waits for the exit value of the command.
         exitValue = process.waitFor(); 
         this.logger.log (Level.INFO, "Command exited with status " + exitValue);

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
