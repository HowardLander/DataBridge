package org.renci.databridge.persistence.metadata;

import java.io.*;
import java.util.*;

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

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testCollectionDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27017);
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
         CollectionDAO theCollectionDAO = theMongoFactory.getCollectionDAO();
         result = theCollectionDAO.insertCollection(theCollection);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theCollection.getDataStoreId());
         System.out.println("testing get");

         // Test the retrieval by id code
         CollectionTransferObject idObject = theCollectionDAO.getCollectionById(theCollection.getDataStoreId());

         // Did we return anything?
         TestCase.assertTrue("idObject is null", idObject != null);

         // Do the fields match
         TestCase.assertTrue("id fields don't march", idObject.getDataStoreId().compareTo(theCollection.getDataStoreId())== 0);
         TestCase.assertTrue("title fields don't march", idObject.getTitle().compareTo(theCollection.getTitle()) == 0);
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
  public void testSimilarityInstanceDAO () throws Exception {

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testSimilarityInstanceDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27017);
     SimilarityInstanceTransferObject theSimilarityInstance = new SimilarityInstanceTransferObject();
     theSimilarityInstance.setNameSpace("junit_test");
     theSimilarityInstance.setClassName("MockSimilarity");
     theSimilarityInstance.setMethod("compareCollections");
     theSimilarityInstance.setVersion(1);

     try {
         SimilarityInstanceDAO theSimilarityInstanceDAO = theMongoFactory.getSimilarityInstanceDAO();
         result = theSimilarityInstanceDAO.insertSimilarityInstance(theSimilarityInstance);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theSimilarityInstance.getDataStoreId());
         System.out.println("testing get");

         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("nameSpace", "junit_test");
         Iterator<SimilarityInstanceTransferObject> similarityInstanceIterator = 
            theSimilarityInstanceDAO.getSimilarityInstances(searchMap);
         System.out.println ("Do we have next? " +  similarityInstanceIterator.hasNext());

         if (similarityInstanceIterator.hasNext()) {
             SimilarityInstanceTransferObject getObj = similarityInstanceIterator.next(); 
             System.out.println("className: " + getObj.getClassName());
             TestCase.assertTrue("classname doesn't match", 
                 getObj.getClassName().compareTo("MockSimilarity") == 0);
             TestCase.assertTrue("subjects don't match", 
                 getObj.getNameSpace().compareTo("junit_test") == 0);
             TestCase.assertTrue("ids don't match", 
                 getObj.getMethod().compareTo("compareCollections") == 0);

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

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testSNAInstanceDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27017);
     SNAInstanceTransferObject theSNAInstance = new SNAInstanceTransferObject();
     theSNAInstance.setNameSpace("junit_test");
     theSNAInstance.setClassName("MockSNAClass");
     theSNAInstance.setMethod("MockSNAAlgorithm");
     theSNAInstance.setSimilarityInstanceId("instance1");
     theSNAInstance.setNResultingClusters(5);
     theSNAInstance.setVersion(1);

     try {
         SNAInstanceDAO theSNAInstanceDAO = theMongoFactory.getSNAInstanceDAO();
         result = theSNAInstanceDAO.insertSNAInstance(theSNAInstance);
         System.out.println("done with insert");
         System.out.println("inserted Id is: " + theSNAInstance.getDataStoreId());
         System.out.println("testing get");

         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("nameSpace", "junit_test");
         Iterator<SNAInstanceTransferObject> SNAInstanceIterator = 
            theSNAInstanceDAO.getSNAInstances(searchMap);
         System.out.println ("Do we have next? " +  SNAInstanceIterator.hasNext());

         if (SNAInstanceIterator.hasNext()) {
             SNAInstanceTransferObject getObj = SNAInstanceIterator.next(); 
             System.out.println("className: " + getObj.getClassName());
             TestCase.assertTrue("classname doesn't match", 
                 getObj.getClassName().compareTo("MockSNAClass") == 0);
             TestCase.assertTrue("nameSpaces don't match", 
                 getObj.getNameSpace().compareTo("junit_test") == 0);
             TestCase.assertTrue("methods don't match", 
                 getObj.getMethod().compareTo("MockSNAAlgorithm") == 0);

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
     System.out.println("");
     System.out.println("");
     System.out.println("beginning testRemove");
     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27017);
     CollectionTransferObject theCollection = new CollectionTransferObject();
     CollectionDAO theCollectionDAO = theMongoFactory.getCollectionDAO();
     HashMap<String, String> searchMap = new HashMap<String, String>();
     searchMap.put("nameSpace", "junit_test");
     Iterator<CollectionTransferObject> nameSpaceIterator = theCollectionDAO.getCollections(searchMap);
     nameSpaceIterator.remove();
  }


  @Test
  public void testMissingValue () throws Exception {

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testMissingValue");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27017);
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

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testFileDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27017);
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

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testVariableDAO");
     boolean result;

     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27017);
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


  @Rule
  public ExpectedException thrown = ExpectedException.none();

/*
  public static void main (String [] args) throws Exception {

  }
*/
}
