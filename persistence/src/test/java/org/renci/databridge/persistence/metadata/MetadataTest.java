package org.renci.databridge.persistence.metadata;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.matchers.JUnitMatchers;
import org.junit.Rule;

import com.rabbitmq.client.*;

import org.renci.databridge.util.*;
import org.renci.databridge.message.*;

public class MetadataTest {


  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }
  
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testCollectionDAO () throws Exception {

     Logger.getRootLogger().setLevel(Level.OFF);
     System.out.println("");
     System.out.println("");
     System.out.println("beginning testCollectionDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     CollectionTransferObject theCollection = new CollectionTransferObject();
     theCollection.setURL("http://www.renci.org");
     theCollection.setTitle("title");
     theCollection.setDescription("here's an example description");
     theCollection.setProducer("producer");
     theCollection.setSubject("physics");
     theCollection.setNameSpace("junit_test");
     theCollection.setVersion(1);
     ArrayList<String> keywords = new ArrayList<String>();

     keywords.add("Keyword1");
     keywords.add("Keyword2");
     keywords.add("Keyword3");
     theCollection.setKeywords(keywords);

     HashMap<String, String> extra = new HashMap<String, String>();
     extra.put("author", "Howard Lander");
     extra.put("reason", "Testing the code");
     theCollection.setExtra(extra);

     try {
         // Let's try the getDataByFieldName call
         String field = theCollection.getDataByFieldName("author");
         System.out.println("field: " + field);
         TestCase.assertTrue("Failed to get author", field.compareTo("Howard Lander")== 0);

         field = theCollection.getDataByFieldName("description");
         System.out.println("field: " + field);
         TestCase.assertTrue("Failed to get description", field.compareTo("here's an example description")== 0);

         field = theCollection.getDataByFieldName("producer");
         System.out.println("field: " + field);
         TestCase.assertTrue("Failed to get producer", field.compareTo("producer")== 0);

         field = theCollection.getDataByFieldName("subject");
         System.out.println("field: " + field);
         TestCase.assertTrue("Failed to get subject", field.compareTo("physics")== 0);

         CollectionDAO theCollectionDAO = theMongoFactory.getCollectionDAO();
         System.out.println("after theMongoFactory.getCollectionDAO");
         result = theCollectionDAO.insertCollection(theCollection);
         System.out.println("done with insert, result is " + result);
         System.out.println("inserted Id is: " + theCollection.getDataStoreId());
         System.out.println("testing get");

         // Test the getNamespaceList() code
         boolean found = false;
         Iterator<String> theNames = theCollectionDAO.getNamespaceList();
         while (theNames.hasNext() ) {
            String nameSpace = theNames.next();
            System.out.println("Found nameSpace: " + nameSpace);
            if (nameSpace.compareTo("junit_test") == 0) {
               found = true;
            }
         } 
         TestCase.assertTrue("Didn't find nameSpace junit_test", found == true);

         // Test the retrieval by id code
         CollectionTransferObject idObject = theCollectionDAO.getCollectionById(theCollection.getDataStoreId());

         // Did we return anything?
         TestCase.assertTrue("idObject is null", idObject != null);

         // Do the fields match
         TestCase.assertTrue("id fields don't march", idObject.getDataStoreId().compareTo(theCollection.getDataStoreId())== 0);
         TestCase.assertTrue("title fields don't march", idObject.getTitle().compareTo(theCollection.getTitle()) == 0);
         System.out.println("insertTime is " + idObject.getInsertTime());
         long now = System.currentTimeMillis()/1000;
         TestCase.assertTrue("insertTime is unreasonable", (now - idObject.getInsertTime()) < 5 );
         System.out.println("theCollectionDAO.getCollectionById tests passed");

         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("nameSpace", "junit_test");
         searchMap.put("title", "title");
         Iterator<CollectionTransferObject> collectionIterator = theCollectionDAO.getCollections(searchMap);
         System.out.println ("Do we have next? " +  collectionIterator.hasNext());

         if (collectionIterator.hasNext()) {
             CollectionTransferObject getObj = collectionIterator.next(); 
             System.out.println("keywords");
             System.out.println(getObj.getKeywords());
             String firstKeyword = getObj.getKeywords().get(0);
             TestCase.assertTrue("First keyword not found", 
                 firstKeyword.compareTo("Keyword1") == 0);
             TestCase.assertTrue("subjects don't match", 
                 theCollection.getSubject().compareTo(getObj.getSubject()) == 0);
             System.out.println("retrieved subject: " + getObj.getSubject());
             TestCase.assertTrue("ids don't match", 
                 theCollection.getDataStoreId().compareTo(getObj.getDataStoreId()) == 0);
             System.out.println("retrieved id: " + getObj.getDataStoreId());

             // Now we'll try to delete the object
             HashMap<String, String> deleteMap = new HashMap<String, String>();
             deleteMap.put("_id", getObj.getDataStoreId());
             int nDeleted = theCollectionDAO.deleteCollection(deleteMap);
             System.out.println("nDeleted: " + nDeleted);
             TestCase.assertTrue("nDeleted not 1", nDeleted == 1);

             // One more insert so we can try deleteCollectionById
             result = theCollectionDAO.insertCollection(theCollection);
             nDeleted = theCollectionDAO.deleteCollection(theCollection);
             System.out.println("nDeleted: " + nDeleted);
             TestCase.assertTrue("nDeleted by Id not 1", nDeleted == 1);
         }

         // Now let's test multiple insertions, gets and deletes
         for (int i = 0; i < 5; i ++) {
             theCollection.setVersion(i);
             result = theCollectionDAO.insertCollection(theCollection);
         }

         // let's test the count collections code.
         HashMap<String, String> nameSpaceMap = new HashMap<String, String>();
         nameSpaceMap.put("nameSpace", "junit_test");
         long theCount = theCollectionDAO.countCollections(nameSpaceMap);
         System.out.println("countCollections found " + theCount + " matches");
         TestCase.assertTrue("count of collections found not 5", theCount == 5);
         

         int nFound = 0;
         int nDeleted = 0;
         int totalDeleted = 0;
         Iterator<CollectionTransferObject> nameSpaceIterator = theCollectionDAO.getCollections(nameSpaceMap);
         while (nameSpaceIterator.hasNext()) {
             CollectionTransferObject getObj = nameSpaceIterator.next(); 
             System.out.println("retrieved version: " + getObj.getVersion());
             nDeleted = theCollectionDAO.deleteCollection(getObj);
             System.out.println("nDeleted: " + nDeleted);
             nFound ++;
             totalDeleted += nDeleted;
         }
         System.out.println("number found:" + nFound);
         TestCase.assertTrue("total found not 5", nFound == 5);
         TestCase.assertTrue("totalDeleted by Id not 5", totalDeleted == 5);
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  @Test
  public void testLaneDAO () throws Exception {

     Logger.getRootLogger().setLevel(Level.OFF);
     System.out.println("");
     System.out.println("");
     System.out.println("beginning testLaneDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     LaneTransferObject theLane = new LaneTransferObject();
     theLane.setCreatorId("55d5f0753525be4d0d8f8a5c");
     theLane.setIngestImpl("testIngest.class");
     theLane.setIngestParams("IngestParams");
     theLane.setSignatureImpl("testSignature.class");
     theLane.setSignatureParams("SignatureParams");
     theLane.setSimilarityImpl("testSimilarity.class");
     theLane.setSimilarityParams("SimilarityParams");
     theLane.setSNAImpl("testSNA.class");
     theLane.setSNAParams("SNAParams");
     theLane.setName("testName");
     theLane.setDescription("test description");
     ArrayList<String> nameSpaces = new ArrayList<String>();

     nameSpaces.add("NameSpace1");
     nameSpaces.add("NameSpace2");
     nameSpaces.add("NameSpace3");
     theLane.setNameSpaces(nameSpaces);

     try {
         LaneDAO theLaneDAO = theMongoFactory.getLaneDAO();
         System.out.println("after theMongoFactory.getLaneDAO");
         result = theLaneDAO.insertLane(theLane);
         System.out.println("done with insert, result is " + result);
         System.out.println("inserted Id is: " + theLane.getDataStoreId());
         System.out.println("testing get");

         // Test the retrieval by id code
         LaneTransferObject idObject = theLaneDAO.getLaneById(theLane.getDataStoreId());

         // Did we return anything?
         TestCase.assertTrue("idObject is null", idObject != null);

         // Do the fields match
         TestCase.assertTrue("id fields don't march", 
            idObject.getDataStoreId().compareTo(theLane.getDataStoreId())== 0);
         TestCase.assertTrue("description fields don't march", 
            idObject.getDescription().compareTo(theLane.getDescription())== 0);
         TestCase.assertTrue("name returned is null", idObject.getName() != null);
         TestCase.assertTrue("name fields don't march", 
            idObject.getName().compareTo(theLane.getName())== 0);
         TestCase.assertTrue("IngestImpl fields don't march", 
            idObject.getIngestImpl().compareTo(theLane.getIngestImpl()) == 0);
         System.out.println("insertTime is " + idObject.getInsertTime());
         long now = System.currentTimeMillis()/1000;
         TestCase.assertTrue("insertTime is unreasonable", (now - idObject.getInsertTime()) < 5 );
         System.out.println("theLaneDAO.getCollectionById tests passed");

         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("ingestImpl", "testIngest.class");
         searchMap.put("signatureImpl", "testSignature.class");
         Iterator<LaneTransferObject> laneIterator = theLaneDAO.getLanes(searchMap);
         System.out.println ("Do we have next? " +  laneIterator.hasNext());

         if (laneIterator.hasNext()) {
             LaneTransferObject getObj = laneIterator.next(); 
             System.out.println("namepaces");
             System.out.println(getObj.getNameSpaces());
             String firstNameSpace = getObj.getNameSpaces().get(0);
             TestCase.assertTrue("First nameSpace not found", 
                 firstNameSpace.compareTo("NameSpace1") == 0);
             TestCase.assertTrue("SNAImpls don't match", 
                 theLane.getSNAImpl().compareTo(getObj.getSNAImpl()) == 0);
             System.out.println("retrieved SNAImpl: " + getObj.getSNAImpl());
             TestCase.assertTrue("SNAParams don't match", 
                 theLane.getSNAParams().compareTo(getObj.getSNAParams()) == 0);
             System.out.println("retrieved SNAParams: " + getObj.getSNAParams());

             // Now we'll try to delete the object
             HashMap<String, String> deleteMap = new HashMap<String, String>();
             deleteMap.put("_id", getObj.getDataStoreId());
             int nDeleted = theLaneDAO.deleteLane(deleteMap);
             System.out.println("nDeleted: " + nDeleted);
             TestCase.assertTrue("nDeleted not 1", nDeleted == 1);

             // One more insert so we can try deleteCollectionById
             result = theLaneDAO.insertLane(theLane);
             nDeleted = theLaneDAO.deleteLane(theLane);
             System.out.println("nDeleted: " + nDeleted);
             TestCase.assertTrue("nDeleted by Id not 1", nDeleted == 1);
         }

         // Now let's test multiple insertions, gets and deletes
         for (int i = 0; i < 5; i ++) {
             theLane.setCreatorId(Integer.toString(i));
             result = theLaneDAO.insertLane(theLane);
         }

         // let's test the count collections code.
         HashMap<String, String> nameSpaceMap = new HashMap<String, String>();
         nameSpaceMap.put("SNAParams", "SNAParams");
         long theCount = theLaneDAO.countLanes(nameSpaceMap);
         System.out.println("countLanes found " + theCount + " matches");
         TestCase.assertTrue("count of Lanes found not 5", theCount == 5);
         

         int nFound = 0;
         int nDeleted = 0;
         int totalDeleted = 0;
         Iterator<LaneTransferObject> nameSpaceIterator = theLaneDAO.getLanes(nameSpaceMap);
         while (nameSpaceIterator.hasNext()) {
             LaneTransferObject getObj = nameSpaceIterator.next(); 
             nDeleted = theLaneDAO.deleteLane(getObj);
             System.out.println("nDeleted: " + nDeleted);
             nFound ++;
             totalDeleted += nDeleted;
         }
         System.out.println("number found:" + nFound);
         TestCase.assertTrue("total found not 5", nFound == 5);
         TestCase.assertTrue("totalDeleted by Id not 5", totalDeleted == 5);
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  @Test
  public void testActionDAO () throws Exception {

     Logger.getRootLogger().setLevel(Level.OFF);
     System.out.println("");
     System.out.println("");
     System.out.println("beginning testActionDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     ActionTransferObject theAction = new ActionTransferObject();
     theAction.setCurrentMessage("Insert.Metadata.Java.URI.MetadataDB");
     theAction.setNextMessage("Run.SNA.Algorithm.FileIO.NetworkDB");
     theAction.setNameSpace("junit_test");
     HashMap<String, String> actionHeaders = new HashMap<String, String>();
     actionHeaders.put("className", "org.renci.databridgecontrib.ingest.mockingest");
     actionHeaders.put("inputURI", "/projects/databridge/metadata.xml");
     theAction.setHeaders(actionHeaders);

     try {
         ActionDAO theActionDAO = theMongoFactory.getActionDAO();
         result = theActionDAO.insertAction(theAction);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theAction.getDataStoreId());
         System.out.println("testing get");

         Iterator<ActionTransferObject> actionIt = 
            theActionDAO.getActions("Insert.Metadata.Java.URI.MetadataDB", "junit_test");
         boolean found = false;
         if (actionIt.hasNext() ) {
            ActionTransferObject returnedObject = actionIt.next();
            System.out.println("Found nameSpace: " + returnedObject.getNameSpace());
            System.out.println("Found nextMessage: " + returnedObject.getNextMessage());
            if (returnedObject.getNameSpace().compareTo("junit_test") == 0) {
               found = true;
            }
         } 
         TestCase.assertTrue("Didn't find nameSpace junit_test", found == true);

         // Now we'll try to delete the object
         int nDeleted = theActionDAO.deleteAction("Insert.Metadata.Java.URI.MetadataDB", "junit_test");
         System.out.println("nDeleted: " + nDeleted);
         TestCase.assertTrue("nDeleted not 1", nDeleted == 1);

     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  @Test
  public void testSimilarityInstanceDAO () throws Exception {

     Logger.getRootLogger().setLevel(Level.OFF);
     System.out.println("");
     System.out.println("");
     System.out.println("beginning testSimilarityInstanceDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     SimilarityInstanceTransferObject theSimilarityInstance = new SimilarityInstanceTransferObject();
     theSimilarityInstance.setNameSpace("junit_test");
     theSimilarityInstance.setClassName("MockSimilarity");
     theSimilarityInstance.setMethod("compareCollections");
     theSimilarityInstance.setOutput("/home/howard/testOutput");
     theSimilarityInstance.setVersion(1);
     theSimilarityInstance.setCount(211);
     theSimilarityInstance.setParams("test1");

     try {
         SimilarityInstanceDAO theSimilarityInstanceDAO = theMongoFactory.getSimilarityInstanceDAO();
         result = theSimilarityInstanceDAO.insertSimilarityInstance(theSimilarityInstance);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theSimilarityInstance.getDataStoreId());
         System.out.println("testing get");

         // Let's try the getById
         SimilarityInstanceTransferObject byId = 
             theSimilarityInstanceDAO.getSimilarityInstanceById(theSimilarityInstance.getDataStoreId());
         System.out.println("Testing getSimilarityInstanceById");
         TestCase.assertTrue("nameSpaces don't match", byId.getNameSpace().compareTo("junit_test") == 0);

         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("nameSpace", "junit_test");
         Iterator<SimilarityInstanceTransferObject> similarityInstanceIterator = 
            theSimilarityInstanceDAO.getSimilarityInstances(searchMap);
         System.out.println ("Do we have next? " +  similarityInstanceIterator.hasNext());

         if (similarityInstanceIterator.hasNext()) {
             SimilarityInstanceTransferObject getObj = similarityInstanceIterator.next(); 
             System.out.println("className: " + getObj.getClassName());
             System.out.println("insertTime is " + getObj.getInsertTime());
             long now = System.currentTimeMillis()/1000;
             TestCase.assertTrue("insertTime is unreasonable (" + (now - getObj.getInsertTime()) + ")" , (now - getObj.getInsertTime()) < 5 );
             TestCase.assertTrue("classname doesn't match", 
                 getObj.getClassName().compareTo("MockSimilarity") == 0);
             TestCase.assertTrue("nameSpaces don't match", 
                 getObj.getNameSpace().compareTo("junit_test") == 0);
             TestCase.assertTrue("outputs don't match", 
                 getObj.getOutput().compareTo("/home/howard/testOutput") == 0);
             TestCase.assertTrue("ids don't match", 
                 getObj.getMethod().compareTo("compareCollections") == 0);
             TestCase.assertTrue("params don't match", 
                 getObj.getParams().compareTo("test1") == 0);
             TestCase.assertTrue("count doesn't match", getObj.getCount() == 211);

             // Now we'll try to delete the object
             HashMap<String, String> deleteMap = new HashMap<String, String>();
             deleteMap.put("_id", getObj.getDataStoreId());
             int nDeleted = theSimilarityInstanceDAO.deleteSimilarityInstance(deleteMap);
             System.out.println("nDeleted: " + nDeleted);
             TestCase.assertTrue("nDeleted not 1", nDeleted == 1);

             // One more insert so we can try deleteCollectionById
             result = theSimilarityInstanceDAO.insertSimilarityInstance(theSimilarityInstance);
             nDeleted = theSimilarityInstanceDAO.deleteSimilarityInstance(theSimilarityInstance);
             System.out.println("nDeleted: " + nDeleted);
             TestCase.assertTrue("nDeleted by Id not 1", nDeleted == 1);
         }

         // Now let's test multiple insertions, gets and deletes
         for (int i = 0; i < 5; i ++) {
             theSimilarityInstance.setVersion(i);
             result = theSimilarityInstanceDAO.insertSimilarityInstance(theSimilarityInstance);
         }

         // let's test the count collections code.
         HashMap<String, String> nameSpaceMap = new HashMap<String, String>();
         nameSpaceMap.put("nameSpace", "junit_test");
         long theCount = theSimilarityInstanceDAO.countSimilarityInstances(nameSpaceMap);
         System.out.println("countCollections found " + theCount + " matches");
         TestCase.assertTrue("count of collections found not 5", theCount == 5);

         // Set up the search map to test the sortingi/limit code
         HashMap<String, String> versionMap = new HashMap<String, String>();
         versionMap.put("nameSpace", "junit_test");
         versionMap.put("className", "MockSimilarity");
         versionMap.put("method", "compareCollections");

         HashMap<String, String> sortMap = new HashMap<String, String>();
         sortMap.put("version", SimilarityInstanceDAO.SORT_DESCENDING);
         Integer limit = new Integer(1);

         int nFound = 0;
         int nDeleted = 0;
         int totalDeleted = 0;
         int highestVersionFound = 0;

         // Test the sort first before the records are deleted.
         Iterator<SimilarityInstanceTransferObject> iterator1 =
          theSimilarityInstanceDAO.getSimilarityInstances(searchMap, sortMap, limit);
         theSimilarityInstance = iterator1.next();
         System.out.println("highest version found: " + theSimilarityInstance.getVersion());

         // This better be the highest version number (4)
         TestCase.assertTrue("version number (" + theSimilarityInstance.getVersion() + ") is wrong (should be 4", theSimilarityInstance.getVersion() == 4);
 
         Iterator<SimilarityInstanceTransferObject> nameSpaceIterator = 
            theSimilarityInstanceDAO.getSimilarityInstances(nameSpaceMap);
         SimilarityInstanceTransferObject getObj = null;
         while (nameSpaceIterator.hasNext()) {
             getObj = nameSpaceIterator.next(); 
             System.out.println("retrieved version: " + getObj.getVersion());
             nDeleted = theSimilarityInstanceDAO.deleteSimilarityInstance(getObj);
             System.out.println("nDeleted: " + nDeleted);
             nFound ++;
             totalDeleted += nDeleted;
         }
         System.out.println("number found:" + nFound);
         TestCase.assertTrue("total found not 5", nFound == 5);
         TestCase.assertTrue("totalDeleted by Id not 5", totalDeleted == 5);
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  @Test
  public void testSNAInstanceDAO () throws Exception {

     Logger.getRootLogger().setLevel(Level.OFF);
     System.out.println("");
     System.out.println("");
     System.out.println("beginning testSNAInstanceDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     SNAInstanceTransferObject theSNAInstance = new SNAInstanceTransferObject();
     theSNAInstance.setNameSpace("junit_test");
     theSNAInstance.setClassName("MockSNAClass");
     theSNAInstance.setMethod("MockSNAAlgorithm");
     theSNAInstance.setSimilarityInstanceId("instance1");
     theSNAInstance.setNResultingClusters("0");
     theSNAInstance.setVersion(1);

     try {
         SNAInstanceDAO theSNAInstanceDAO = theMongoFactory.getSNAInstanceDAO();
         result = theSNAInstanceDAO.insertSNAInstance(theSNAInstance);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theSNAInstance.getDataStoreId());
         System.out.println("testing get");

         // Lets test the getByID
         System.out.println("testing getById");
         SNAInstanceTransferObject byIdObject = 
            theSNAInstanceDAO.getSNAInstanceById(theSNAInstance.getDataStoreId());
         TestCase.assertTrue("DataStoreId returned from getById does not match",
            theSNAInstance.getDataStoreId().compareTo(byIdObject.getDataStoreId()) == 0);

         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("nameSpace", "junit_test");
         searchMap.put("similarityInstanceId", "instance1");

         // Let's do an update
         HashMap<String, String> updateMap = new HashMap<String, String>();
         updateMap.put("nResultingClusters", "3");
         updateMap.put("className", "test");
         theSNAInstanceDAO.updateSNAInstance(searchMap, updateMap);
         
         Iterator<SNAInstanceTransferObject> SNAInstanceIterator = 
            theSNAInstanceDAO.getSNAInstances(searchMap);
         System.out.println ("Do we have next? " +  SNAInstanceIterator.hasNext());

         if (SNAInstanceIterator.hasNext()) {
             SNAInstanceTransferObject getObj = SNAInstanceIterator.next(); 
             System.out.println("className: " + getObj.getClassName());
             TestCase.assertTrue("classname doesn't match", 
                 getObj.getClassName().compareTo("test") == 0);
             TestCase.assertTrue("nameSpaces don't match", 
                 getObj.getNameSpace().compareTo("junit_test") == 0);
             TestCase.assertTrue("methods don't match", 
                 getObj.getMethod().compareTo("MockSNAAlgorithm") == 0);
             TestCase.assertTrue("failed to add value for nResultingClusters", 
                 getObj.getNResultingClusters().compareTo("3") == 0);
             System.out.println("insertTime is " + getObj.getInsertTime());
             long now = System.currentTimeMillis()/1000;
             TestCase.assertTrue("insertTime is unreasonable", (now - getObj.getInsertTime()) < 5 );
 
             // These are the two modified values.
             System.out.println("nResultingClusters value: " + getObj.getNResultingClusters());
             System.out.println("className value: " + getObj.getClassName());

             // Let's try another update
             HashMap<String, String> updateMap2 = new HashMap<String, String>();
             updateMap2.put("nResultingClusters", "4");
             updateMap2.put("className", "test2");
             theSNAInstanceDAO.updateSNAInstance(getObj, updateMap2);
         
             Iterator<SNAInstanceTransferObject> SNAInstanceIterator2 = 
                theSNAInstanceDAO.getSNAInstances(searchMap);
             System.out.println ("Do we have next again? " +  SNAInstanceIterator2.hasNext());
    
             if (SNAInstanceIterator2.hasNext()) {
                 SNAInstanceTransferObject getObj2 = SNAInstanceIterator2.next(); 
                 TestCase.assertTrue("failed to add value for nResultingClusters", 
                     getObj2.getNResultingClusters().compareTo("4") == 0);
                 System.out.println("nResultingClusters newest value: " + getObj2.getNResultingClusters());
                 System.out.println("className newest value: " + getObj2.getClassName());
             }

             // Now we'll try to delete the object
             HashMap<String, String> deleteMap = new HashMap<String, String>();
             deleteMap.put("_id", getObj.getDataStoreId());
             int nDeleted = theSNAInstanceDAO.deleteSNAInstance(deleteMap);
             System.out.println("nDeleted: " + nDeleted);
             TestCase.assertTrue("nDeleted not 1", nDeleted == 1);

             // One more insert so we can try deleting by with the instance
             result = theSNAInstanceDAO.insertSNAInstance(theSNAInstance);
             nDeleted = theSNAInstanceDAO.deleteSNAInstance(theSNAInstance);
             System.out.println("nDeleted: " + nDeleted);
             TestCase.assertTrue("nDeleted by Id not 1", nDeleted == 1);
         }

         // Now let's test multiple insertions, gets and deletes
         for (int i = 0; i < 5; i ++) {
             theSNAInstance.setVersion(i);
             result = theSNAInstanceDAO.insertSNAInstance(theSNAInstance);
         }

         HashMap<String, String> nameSpaceMap = new HashMap<String, String>();
         nameSpaceMap.put("nameSpace", "junit_test");

         HashMap<String, String> sortMap = new HashMap<String, String>();
         sortMap.put("version", SimilarityInstanceDAO.SORT_DESCENDING);
         Integer limit = new Integer(1);

         // Test the sort first before the records are deleted.
         Iterator<SNAInstanceTransferObject> iterator1 =
            theSNAInstanceDAO.getSNAInstances(nameSpaceMap, sortMap, limit);
         theSNAInstance = iterator1.next();

         // This better be the highest version number (4)
         System.out.println("highest version found: " + theSNAInstance.getVersion());
         TestCase.assertTrue("version number (" + theSNAInstance.getVersion() + ") is wrong (should be 4", theSNAInstance.getVersion() == 4);

         Iterator<SNAInstanceTransferObject> nameSpaceIterator = 
            theSNAInstanceDAO.getSNAInstances(nameSpaceMap);
         SNAInstanceTransferObject getObj = null;
         int nDeleted = 0;
         int nFound = 0;
         int totalFound = 0;
         int totalDeleted = 0;
         while (nameSpaceIterator.hasNext()) {
             getObj = nameSpaceIterator.next(); 
             System.out.println("retrieved version: " + getObj.getVersion());
             nDeleted = theSNAInstanceDAO.deleteSNAInstance(getObj);
             System.out.println("nDeleted: " + nDeleted);
             nFound ++;
             totalDeleted += nDeleted;
         }
         System.out.println("number found:" + nFound);
         TestCase.assertTrue("total found not 5", nFound == 5);
         TestCase.assertTrue("totalDeleted not 5", totalDeleted == 5);
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }


  @Test(expected=UnsupportedOperationException.class)
  public void testRemove () throws Exception {
     Logger.getRootLogger().setLevel(Level.OFF);
     System.out.println("");
     System.out.println("");
     System.out.println("beginning testRemove");
     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     CollectionTransferObject theCollection = new CollectionTransferObject();
     CollectionDAO theCollectionDAO = theMongoFactory.getCollectionDAO();
     HashMap<String, String> searchMap = new HashMap<String, String>();
     searchMap.put("nameSpace", "junit_test");
     Iterator<CollectionTransferObject> nameSpaceIterator = theCollectionDAO.getCollections(searchMap);
     nameSpaceIterator.remove();
  }


  @Test
  public void testMissingValue () throws Exception {

     Logger.getRootLogger().setLevel(Level.OFF);
     System.out.println("");
     System.out.println("");
     System.out.println("beginning testMissingValue");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     CollectionTransferObject theCollection = new CollectionTransferObject();
     theCollection.setURL("http://www.renci.org");
     //theCollection.setTitle("title");
     theCollection.setDescription("here's an example description");
     theCollection.setProducer("producer");
     theCollection.setSubject("physics");
     theCollection.setNameSpace("test");
     theCollection.setVersion(1);
     HashMap<String, String> extra = new HashMap<String, String>();
     extra.put("author", "Howard Lander");
     extra.put("reason", "Testing the code");
     theCollection.setExtra(extra);

     try {
         CollectionDAO theCollectionDAO = theMongoFactory.getCollectionDAO();
         result = theCollectionDAO.insertCollection(theCollection);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theCollection.getDataStoreId());
         System.out.println("testing get");

         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("nameSpace", "junit_test");
         searchMap.put("producer", "producer");
         Iterator<CollectionTransferObject> collectionIterator = theCollectionDAO.getCollections(searchMap);
         System.out.println ("Do we have next? " +  collectionIterator.hasNext());

         if (collectionIterator.hasNext()) {
             CollectionTransferObject getObj = collectionIterator.next(); 

             TestCase.assertTrue("title is not null", getObj.getTitle() == null);
             int nDeleted = theCollectionDAO.deleteCollection(getObj);
             System.out.println("nDeleted: " + nDeleted);
             
         }
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }


  @Test
  public void testFileDAO () throws Exception {

     Logger.getRootLogger().setLevel(Level.OFF);
     System.out.println("");
     System.out.println("");
     System.out.println("beginning testFileDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     CollectionTransferObject theCollection = new CollectionTransferObject();
     theCollection.setURL("http://www.renci.org");
     theCollection.setTitle("title");
     theCollection.setDescription("here's an example description");
     theCollection.setProducer("producer");
     theCollection.setSubject("physics");
     theCollection.setNameSpace("junit_test");
     theCollection.setVersion(1);
     HashMap<String, String> extra = new HashMap<String, String>();
     extra.put("author", "Howard Lander");
     extra.put("reason", "Testing the code");
     theCollection.setExtra(extra);

     try {
         CollectionDAO theCollectionDAO = theMongoFactory.getCollectionDAO();
         result = theCollectionDAO.insertCollection(theCollection);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theCollection.getDataStoreId());
         System.out.println("testing get");

         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("nameSpace", "junit_test");
         searchMap.put("title", "title");
         Iterator<CollectionTransferObject> collectionIterator = theCollectionDAO.getCollections(searchMap);
         System.out.println ("Do we have next? " +  collectionIterator.hasNext());

         if (collectionIterator.hasNext()) {
             CollectionTransferObject getObj = collectionIterator.next(); 
             FileDAO theFileDAO = theMongoFactory.getFileDAO();
             FileTransferObject theFile = new FileTransferObject();
             theFile.setURL("http://www.renci.org");
             theFile.setName("file1");
             theFile.setDescription("here's an example file description");
             theFile.setNameSpace("junit_test");
             theFile.setVersion(1);
             HashMap<String, String> fileExtra = new HashMap<String, String>();
             fileExtra.put("author", "Howard Lander");
             fileExtra.put("reason", "Testing the code");
             theFile.setExtra(fileExtra);
             result = theFileDAO.insertFile(getObj, theFile);

             // Did it get inserted?
             Iterator<FileTransferObject> fileIterator = theFileDAO.getFiles(getObj);
             if (fileIterator.hasNext()) {
                 FileTransferObject theReturnedFile = fileIterator.next();
                 System.out.println("Returned file name: " + theReturnedFile.getName()); 
                 System.out.println("insertTime is " + theReturnedFile.getInsertTime());
                 long now = System.currentTimeMillis()/1000;
                 TestCase.assertTrue("insertTime is unreasonable", (now - theReturnedFile.getInsertTime()) < 5 );

                 // Let's try the delete
                 int nDeleted = theFileDAO.deleteFile(theReturnedFile);
                 System.out.println("nDeleted: " + nDeleted); 

                 // Let's delete the collection as well.
                 nDeleted = theCollectionDAO.deleteCollection(getObj);
                 System.out.println("nDeleted: " + nDeleted); 
             }
         }
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  @Test
  public void testVariableDAO () throws Exception {
     Logger.getRootLogger().setLevel(Level.OFF);

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testVariableDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     CollectionTransferObject theCollection = new CollectionTransferObject();
     theCollection.setURL("http://www.renci.org");
     theCollection.setTitle("title");
     theCollection.setDescription("here's an example description");
     theCollection.setProducer("producer");
     theCollection.setSubject("physics");
     theCollection.setNameSpace("junit_test");
     theCollection.setVersion(1);
     HashMap<String, String> extra = new HashMap<String, String>();
     extra.put("author", "Howard Lander");
     extra.put("reason", "Testing the code");
     theCollection.setExtra(extra);

     try {
         CollectionDAO theCollectionDAO = theMongoFactory.getCollectionDAO();
         result = theCollectionDAO.insertCollection(theCollection);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theCollection.getDataStoreId());
         System.out.println("testing get");

         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("nameSpace", "junit_test");
         searchMap.put("title", "title");
         Iterator<CollectionTransferObject> collectionIterator = theCollectionDAO.getCollections(searchMap);
         System.out.println ("Do we have next? " +  collectionIterator.hasNext());

         if (collectionIterator.hasNext()) {
             CollectionTransferObject getObj = collectionIterator.next(); 
             FileDAO theFileDAO = theMongoFactory.getFileDAO();
             FileTransferObject theFile = new FileTransferObject();
             theFile.setURL("http://www.renci.org");
             theFile.setName("file1");
             theFile.setDescription("here's an example file description");
             theFile.setNameSpace("junit_test");
             theFile.setVersion(1);
             HashMap<String, String> fileExtra = new HashMap<String, String>();
             fileExtra.put("author", "Howard Lander");
             fileExtra.put("reason", "Testing the code");
             theFile.setExtra(fileExtra);
             result = theFileDAO.insertFile(getObj, theFile);

             // Did it get inserted?
             Iterator<FileTransferObject> fileIterator = theFileDAO.getFiles(getObj);
             if (fileIterator.hasNext()) {
                 FileTransferObject theReturnedFile = fileIterator.next();
                 System.out.println("Returned file name: " + theReturnedFile.getName()); 

                 // Now let's add some variables
                 VariableDAO theVarDAO = theMongoFactory.getVariableDAO();
                 VariableTransferObject var1 = new VariableTransferObject();
                 var1.setName("var1");
                 var1.setDescription("here's an example file description");
                 var1.setVersion(1);
                 HashMap<String, String> varExtra = new HashMap<String, String>();
                 varExtra.put("author", "Howard Lander");
                 varExtra.put("reason", "Testing the code");
                 var1.setExtra(varExtra);
                 result = theVarDAO.insertVariable(theReturnedFile, var1);

                 VariableTransferObject var2 = new VariableTransferObject();
                 var2.setName("var2");
                 var2.setDescription("here's an example file description");
                 var2.setVersion(1);
                 HashMap<String, String> varExtra2 = new HashMap<String, String>();
                 varExtra2.put("name", "wavePeriod");
                 varExtra2.put("convention", "CF");
                 var2.setExtra(varExtra2);
                 result = theVarDAO.insertVariable(theReturnedFile, var2);

                 Iterator<VariableTransferObject> varIterator = theVarDAO.getVariables(theReturnedFile);
                 while (varIterator.hasNext()) {
                     VariableTransferObject theReturnedVar = varIterator.next();
                     System.out.println("Returned var name: " + theReturnedVar.getName()); 
                     System.out.println("insertTime is " + theReturnedVar.getInsertTime());
                     long now = System.currentTimeMillis()/1000;
                     TestCase.assertTrue("insertTime is unreasonable", (now - theReturnedVar.getInsertTime()) < 5 );
                     int nDeleted = theVarDAO.deleteVariable(theReturnedVar);
                     System.out.println("n variables Deleted: " + nDeleted); 
                 }

                 // Let's try the delete
                 int nDeleted = theFileDAO.deleteFile(theReturnedFile);
                 System.out.println("nDeleted: " + nDeleted); 

                 // Let's delete the collection as well.
                 nDeleted = theCollectionDAO.deleteCollection(getObj);
                 System.out.println("nDeleted: " + nDeleted); 
             }
         }
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  @Test
  public void testNameSpaceDAO () throws Exception {
     Logger.getRootLogger().setLevel(Level.OFF);

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testNameSpaceDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27389, "DataBridgeTest", "ColumbusStockadeBlues");
     NameSpaceTransferObject theNameSpace = new NameSpaceTransferObject();
     theNameSpace.setNameSpace("testNamespace");
     theNameSpace.setDescription("here's an example description");

     try {
         NameSpaceDAO theNameSpaceDAO = theMongoFactory.getNameSpaceDAO();
         result = theNameSpaceDAO.insertNameSpace(theNameSpace);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theNameSpace.getDataStoreId());
         System.out.println("testing get");

         Iterator<NameSpaceTransferObject> nameSpaceIterator = theNameSpaceDAO.getNameSpaces();
         System.out.println ("Do we have next? " +  nameSpaceIterator.hasNext());

         if (nameSpaceIterator.hasNext()) {
             NameSpaceTransferObject getObj = nameSpaceIterator.next(); 
             System.out.println("found nameSpace: " + getObj.getNameSpace()); 
             TestCase.assertTrue("nameSpace is wrong",  getObj.getNameSpace().compareTo("testNamespace") == 0);


             // Let's delete the nameSpace
             int nDeleted = theNameSpaceDAO.deleteNameSpace(getObj);
             System.out.println("nDeleted: " + nDeleted); 
             TestCase.assertTrue("nDeleted not 1", nDeleted == 1);
         }
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

/*
  public static void main (String [] args) throws Exception {

  }
*/
}
