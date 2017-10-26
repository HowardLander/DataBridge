package org.renci.databridge.engines.relevance;
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
 * This class is executed in a thread of the Relevance Engine. The Relevance Engine
 * calls the constructor for this class with the AMQP message as a parameter.  It's
 * up to this class to decode the message according to the headers and implement the
 * required behaviors.
 *
 * @author Howard Lander -RENCI (www.renci.org)
 */

public class RelevanceEngineMessageHandler implements AMQPMessageHandler {

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
      String messageName = stringHeaders.get(RelevanceEngineMessage.NAME);

      // Call the function appropriate for the message
      if (null == messageName) {
         logger.log(Level.WARNING, "messageName is missing");
      } else if 
          (messageName.compareTo(RelevanceEngineMessage.CREATE_SIMILARITYMATRIX_JAVA_METADATADB_URI) == 0) {
         processCreateSimilarityMessage(stringHeaders, extra);
      } else if (messageName.compareTo(IngestListenerMessage.PROCESSED_METADATA_TO_METADATADB) == 0) {
         processMetadataToMetadataDBMessage(stringHeaders, extra);
      } else {
         logger.log(Level.WARNING, "unimplemented messageName: " + messageName);
      }
  }

  /**
   * Add the associated files and variables to the given CollectionTransferObject.
   * 
   * @param theFileDAO Data Access Object for files
   * @param theVariableDAO Data Access Object for variables
   * @param cto The CollectionTransferObject to which the files and variables will be attached
   */
  public void addFilesAndVariables(FileDAO theFileDAO, 
                                   VariableDAO theVariableDAO, 
                                   CollectionTransferObject cto) {
     // Add in all of the Files and Variables for the collection transfer objects.
     Iterator<FileTransferObject> theFileIterator = theFileDAO.getFiles(cto);
     ArrayList<FileTransferObject> theFileList = new ArrayList<FileTransferObject>();
     while (theFileIterator.hasNext()) {
        ArrayList<VariableTransferObject> theVarList = new ArrayList<VariableTransferObject>();
        FileTransferObject thisFile = theFileIterator.next();
        theFileList.add(thisFile);
        Iterator<VariableTransferObject> theVarIterator = theVariableDAO.getVariables(thisFile);
        // Add each of the vars for this file
        while (theVarIterator.hasNext()) {
           VariableTransferObject thisVar = theVarIterator.next();
           theVarList.add(thisVar);
        }
        // Add the vars to the file
        thisFile.setVariableList(theVarList);
     }
     // add the files to the collection object
     cto.setFileList(theFileList);
  }

  /**
   * Handle the PROCESSED_METADATA_TO_METADATADB message.  Primarily, we are going to search
   * the action table and call the processCreateSimilarityMessage code for each matching
   * action. There is some remapping of the headers involved as well.
   * @param stringHeaders A map of the headers provided in the message
   * @param extra An object containing the needed DAO objects
   */
  public void processMetadataToMetadataDBMessage( Map<String, String> stringHeaders, Object extra) {

      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the name space
      String nameSpace = stringHeaders.get(RelevanceEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No name space in message");
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
      String dbType = theProps.getProperty("org.renci.databridge.relevancedb.dbType", "mongo");

     // Get the list of needed actions
     ActionTransferObject theAction = new ActionTransferObject();
     ActionDAO theActionDAO = theFactory.getActionDAO();
 
     Iterator<ActionTransferObject> actionIt =
            theActionDAO.getActions(IngestListenerMessage.PROCESSED_METADATA_TO_METADATADB, nameSpace);
     this.logger.log (Level.INFO, "Searching action table for: " + 
                      IngestListenerMessage.PROCESSED_METADATA_TO_METADATADB + " nameSpace: " + nameSpace);
     String outputFile = null;
     while (actionIt.hasNext()) {
        ActionTransferObject returnedObject = actionIt.next();
        this.logger.log (Level.INFO, "Found action: " + returnedObject.getDataStoreId());
        HashMap<String, String> passedHeaders = new HashMap<String, String>();

        // Get the class and outputFile from the action object
        HashMap<String, String> actionHeaders = returnedObject.getHeaders();
        passedHeaders.put(RelevanceEngineMessage.CLASS, 
                          (String) actionHeaders.get(RelevanceEngineMessage.CLASS));
        outputFile = (String) actionHeaders.get(RelevanceEngineMessage.OUTPUT_FILE);
        passedHeaders.put(RelevanceEngineMessage.NAME_SPACE, nameSpace);

        // If the outputFile specified is a file, we pass it on. If it's a directory, we generate
        // a file name in that directory and pass that on.
        String lastChar = outputFile.substring(outputFile.length() - 1);
        String fileName = null;
        if (lastChar.compareTo("/") == 0) {
           // The user has given us a directory, so we need to append a fileName
           // We'll let java create the tmp file name, then delete the file.
           try {
              File outFileObject = new File(outputFile);
    
              // Create the directory if it does not already exist
              if (outFileObject.exists() == false) {
                  boolean result = outFileObject.mkdirs();
                  if (false == result) {
                      this.logger.log (Level.WARNING, "can't create path: " + outputFile);
                  }
              }
              // Let's add the last element of the class name to the file name
              String fullClassName = (String) actionHeaders.get(RelevanceEngineMessage.CLASS);
              String lastClass = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

              // Let's add a the date and time as well.
              Date now = new Date();
              SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
              String dateString = format.format(now);
              String labeledFileName = nameSpace + "-" + lastClass + "-" + dateString + ".net";
              fileName = outputFile + labeledFileName;
           } catch (Exception e) {
              e.printStackTrace();
           }
        } else {
          // the user has given us a path
          fileName = outputFile;
        } 
        passedHeaders.put(RelevanceEngineMessage.OUTPUT_FILE, fileName);
        this.logger.log (Level.INFO, "passing headers to processCreateSimilarityMessage: " + passedHeaders);
        processCreateSimilarityMessage(passedHeaders, extra);
     }
  }


  /**
   * Handle the CREATE_SIMILARITYMATRIX_JAVA_METADATADB_URI message.  
   * @param stringHeaders A map of the headers provided in the message
   * @param extra An object containing the needed DAO objects plus a properties
   */
  public void processCreateSimilarityMessage( Map<String, String> stringHeaders, Object extra) {
      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the class name
      String className = stringHeaders.get(RelevanceEngineMessage.CLASS);    
      if (null == className) {
         this.logger.log (Level.SEVERE, "No class name in message");
         return;
      }

      // Let's try to load the class.
      Class<?> theClass = null;
      ClassLoader classLoader = RelevanceEngineMessageHandler.class.getClassLoader();
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
      String nameSpace = stringHeaders.get(RelevanceEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No name space in message");
         return;
      }

      // 3) the outputFile
      String outputFile = stringHeaders.get(RelevanceEngineMessage.OUTPUT_FILE);    
      if (null == outputFile) {
         this.logger.log (Level.SEVERE, "No output URI in message");
         return;
      }

      // 4) the params aren't always needed
      String params = stringHeaders.get(RelevanceEngineMessage.PARAMS);    
      if (null == params) {
          params = "";
      }

      // include all used to be part of the invocation specific engine parameters. But I can't think
      // of any good reason to not always return all the data for the collection.  I'll leave this here
      // just in case we eventually want to re-instate it (which I suppose could happen for performance
      // reasons).
      boolean includeAll = true;
      long count;
      // 5) the count. This is optional, if no count is specified do the whole nameSpace
      String countString = stringHeaders.get(RelevanceEngineMessage.COUNT);    
      if (null == countString) {
         // This is OK
         count  = -1;
      } else {
         count = Long.parseLong(countString);
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

      FileDAO theFileDAO = null; 
      VariableDAO theVariableDAO = null; 
      if (includeAll) {
         // We'll need DAO's for the files and variables
         theFileDAO = theFactory.getFileDAO();
         if (null == theFileDAO) {
             this.logger.log (Level.SEVERE, "FileDAO is null");
             return;
          }
         theVariableDAO = theFactory.getVariableDAO();
         if (null == theVariableDAO) {
             this.logger.log (Level.SEVERE, "VariableDAO is null");
             return;
          }
      }

      // the engine params aren't always needed, but instead of pulling them out of the headers,
      // we are now going to pull them out of the persistence store for this class.  This is because,
      // as far as we can tell, these parameters are invariant for any given class implementation.
      boolean normalize = false;
      boolean distance = false;
      double  maxValue = 1.;

      SimilarityAlgorithmDAO theSimilarityAlgorithmDAO = theFactory.getSimilarityAlgorithmDAO();
      if (null == theSimilarityAlgorithmDAO) {
         this.logger.log (Level.SEVERE, "SimilarityAlgorithmDAO is null");
         return;
      }

      HashMap<String, String> searchMap = new HashMap<String, String>();
      searchMap.put("className", className);
      HashMap<String, String> sortMap = new HashMap<String, String>();
         sortMap.put("_id", SimilarityAlgorithmDAO.SORT_DESCENDING);
      Integer limit = new Integer(1);

      // Find the most recent version of the algorithm, so we can grab any engine params.
      Iterator<SimilarityAlgorithmTransferObject> similarityAlgorithmIterator =
         theSimilarityAlgorithmDAO.getSimilarityAlgorithms(searchMap, sortMap, limit);
      SimilarityAlgorithmTransferObject theSimilarityAlgorithm = similarityAlgorithmIterator.next();

      String engineParams = theSimilarityAlgorithm.getEngineParams();
      if (null == engineParams) {
          engineParams = "";
      } else {
          if (engineParams.indexOf(RelevanceEngineMessage.NORMALIZE) != -1) {
             normalize = true;
          } 
          if (engineParams.indexOf(RelevanceEngineMessage.DISTANCE) != -1) {
             distance = true;
          } 
      }

      this.logger.log (Level.INFO, "normalize: " + normalize);
      this.logger.log (Level.INFO, "distance: " + distance);
      // Let's add the SimilarityInstance.
      SimilarityInstanceTransferObject theSimilarityInstance = new SimilarityInstanceTransferObject();
      theSimilarityInstance.setNameSpace(nameSpace);
      theSimilarityInstance.setClassName(className);
      theSimilarityInstance.setMethod("compareCollections");
      theSimilarityInstance.setOutput("file://" + outputFile);
      theSimilarityInstance.setCount(count);
      theSimilarityInstance.setParams(params);

      // let's find the highest version for this combination of nameSpace, className and method (if any)
      HashMap<String, String> simVersionMap = new HashMap<String, String>();
      simVersionMap.put("nameSpace", nameSpace);
      simVersionMap.put("className", className);
      simVersionMap.put("method", "compareCollections");
      
      HashMap<String, String> simSortMap = new HashMap<String, String>();
      simSortMap.put("version", SimilarityInstanceDAO.SORT_DESCENDING);

      // This is for the case of no previous instance
      theSimilarityInstance.setVersion(1);
      Iterator<SimilarityInstanceTransferObject> versionIterator =
          theSimilarityInstanceDAO.getSimilarityInstances(simVersionMap, simSortMap, limit);
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
      HashMap<String, String> collectionSearchMap = new HashMap<String, String>();
      collectionSearchMap.put("nameSpace", nameSpace);
 
      // We need an array list of collectionIds
      ArrayList<String> collectionIds = new ArrayList<String>();

      long nCollections = theCollectionDAO.countCollections(collectionSearchMap);
      this.logger.log (Level.INFO, "number of collections: " + nCollections);

      if (nCollections <= 0) {
         // Nothing to do, we can stop now
         this.logger.log (Level.INFO, "nCollections <= 0, so nothing to do");
      }

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

      // For each pair of collection objects, we call the user provided function.
      Iterator<CollectionTransferObject> iterator1 = 
          theCollectionDAO.getCollections(searchMap);
      Iterator<CollectionTransferObject> iterator2 = null;
      
      // The following code is known to be ugly, but it's not easy to do better since
      // you can't really copy java iterators.
      int counter = 1;
      int rowCounter = 0;
      double numCompared = 0;
      while (iterator1.hasNext()) { 
         if (count > 0 && numCompared >= count) {
            // we've reached the termination condition
            break;
         }
         CollectionTransferObject cto1 = iterator1.next();
         CollectionTransferObject cto2 = null;
         collectionIds.add(cto1.getDataStoreId());
         this.logger.log (Level.INFO, "adding collection id: " + cto1.getDataStoreId());

         int colCounter = 0;
         // This is the weird part. Since you can't copy iterators in java
         // we re-declare the inner iterator for each iteration of the outer loop, than
         // spin it forward so it is at the position of the outer iterator.
         iterator2 = theCollectionDAO.getCollections(searchMap);
         for (int k = 0; k < counter; k++) {
             iterator2.next();
             colCounter ++;
         }

         double similarity = 0.;

         // Now spin through the rest of the iterator 2 list.
         while (iterator2.hasNext()) {
            cto2 = iterator2.next();

            // Now we have our 2 CollectionTransferObjects, so we want to call the method.
            try {
               if (includeAll) {
                   // Add in all of the Files and Variables for the collection transfer objects.
                   addFilesAndVariables(theFileDAO, theVariableDAO, cto1);
                   addFilesAndVariables(theFileDAO, theVariableDAO, cto2);
               }
               similarity =  (double) thisProcessor.compareCollections(cto1, cto2, params);
               if (normalize) {
                   // We'll need the max value.
                   if (similarity > maxValue) {
                      maxValue = similarity;
                   }
               }
               if (similarity > 0.0) {
                  this.logger.log (Level.INFO, "adding ids: " + cto1.getDataStoreId() + " " + cto2.getDataStoreId());
                  this.logger.log (Level.INFO, "rowCounter: " + rowCounter + " colCounter: " + colCounter + " sim: " + similarity);
                  theSimFile.setSimilarityValue(rowCounter, colCounter, similarity);
               } else if (colCounter == ((int)nCollections - 1)) {
                  // this is the last column in the row and it's zero.  Set it to -1 so we avoid a bug in the
                  // CRSMatrix class.  Of course, we have to remove this when we read the file.
                  this.logger.log (Level.INFO, "setting (" + rowCounter + "," + colCounter + ") to -1");
                  theSimFile.setSimilarityValue(rowCounter, colCounter, -1);
               }
               numCompared ++;
               colCounter++;
               if (count > 0 && numCompared >= count) {
                  // we've reached the termination condition
                  break;
               }
            } catch (Exception e) {
               this.logger.log (Level.SEVERE, "Can't invoke method compareCollections" + e.getMessage(), e);
               return;
            }
         }
         counter ++;
         rowCounter ++;
      }

      // Now that we have set the values in theSimFile, we can do any of the needed adjustments
      if (normalize || distance) {
         // Look at every value.
         org.la4j.matrix.sparse.CRSMatrix localMatrix = theSimFile.getSimilarityMatrix();
         this.logger.log (Level.INFO, "processing matrix, max Value is: " + maxValue);
         this.logger.log (Level.INFO, "rows: " + localMatrix.rows());
         this.logger.log (Level.INFO, "cols: " + localMatrix.columns());
         for (int row = 0; row < localMatrix.rows(); row ++) {
            for (int col = 0; col < localMatrix.columns(); col ++) {
               double localValue = localMatrix.get(row, col);
               if (normalize) {
                  localValue = localValue / maxValue;
               }
               if (distance) {
                  if (localValue > 0) {
                     // leave the zero values unchanged...
                     localValue = 1. - localValue;
                  }
               }
               localMatrix.set(row, col, localValue);
            }
         }
         theSimFile.setSimilarityMatrix(localMatrix);
      }
      theSimFile.setCollectionIds(collectionIds);
      try {
         theSimFile.writeToDisk(outputFile);
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception writing to disk: " + e.getMessage());
         return;
      }

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
