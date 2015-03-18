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
  
  public void handle (AMQPMessage amqpMessage, Object extra) throws Exception {
      // Get the individual components of the the message and store
      // them in the fields
      routingKey = amqpMessage.getRoutingKey();
      properties = amqpMessage.getProperties();
      stringHeaders = amqpMessage.getStringHeaders();
      bytes = amqpMessage.getBytes();
      System.out.println("headers: " + stringHeaders);

      // get the message name
      String messageName = stringHeaders.get(RelevanceEngineMessage.NAME);

      // Call the function appropriate for the message
      if (null == messageName) {
         System.out.println("messageName is missing");
      } else if (messageName.compareTo(RelevanceEngineMessage.CREATE_SIMILARITYMATRIX_JAVA_METADATADB_URI) == 0) {
         processCreateSimilarityMessage(stringHeaders, extra);
      } else {
         System.out.println("unimplemented messageName: " + messageName);
      }
  }


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

      System.out.println("Loaded class: " + className);

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

      // The "extra" parameter in this case must be of type MetadataDAOFactory
      MetadataDAOFactory theFactory = (MetadataDAOFactory) extra;
      if (null == theFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
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
      theSimilarityInstance.setOutput(outputFile);

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
      while (iterator1.hasNext()) { 
         CollectionTransferObject cto1 = iterator1.next();
         CollectionTransferObject cto2 = null;
         collectionIds.add(cto1.getDataStoreId());
         

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
               similarity =  (double) thisProcessor.compareCollections(cto1, cto2);
               theSimFile.setSimilarityValue(rowCounter, colCounter, similarity);
               colCounter++;
            } catch (Exception e) {
               this.logger.log (Level.SEVERE, "Can't invoke method compareCollections" );
               return;
            }
         }
         counter ++;
         rowCounter ++;
      }
      theSimFile.setCollectionIds(collectionIds);
      try {
         theSimFile.writeToDisk(outputFile);
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception writing to disk: " + e.getMessage());
         return;
      }
  }
 
  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

// todo

  }
} 
