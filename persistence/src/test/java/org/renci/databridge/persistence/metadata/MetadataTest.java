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

import org.renci.databridge.mhandling.*;
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
         searchMap.put("nameSpace", "test");
         searchMap.put("title", "title");
         Iterator<CollectionTransferObject> collectionIterator = theCollectionDAO.getCollection(searchMap);
         System.out.println ("Do we have next? " +  collectionIterator.hasNext());

         if (collectionIterator.hasNext()) {
             CollectionTransferObject getObj = collectionIterator.next(); 

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
             nDeleted = theCollectionDAO.deleteCollectionById(theCollection.getDataStoreId());
             System.out.println("nDeleted: " + nDeleted);
             TestCase.assertTrue("nDeleted by Id not 1", nDeleted == 1);
         }

         // Now let's test multiple insertions, gets and deletes
         for (int i = 0; i < 5; i ++) {
             theCollection.setVersion(i);
             result = theCollectionDAO.insertCollection(theCollection);
         }

         int nFound = 0;
         int nDeleted = 0;
         int totalDeleted = 0;
         HashMap<String, String> nameSpaceMap = new HashMap<String, String>();
         searchMap.put("nameSpace", "test");
         Iterator<CollectionTransferObject> nameSpaceIterator = theCollectionDAO.getCollection(nameSpaceMap);
         while (nameSpaceIterator.hasNext()) {
             CollectionTransferObject getObj = nameSpaceIterator.next(); 
             System.out.println("retrieved version: " + getObj.getVersion());
             nDeleted = theCollectionDAO.deleteCollectionById(getObj.getDataStoreId());
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
     searchMap.put("nameSpace", "test");
     Iterator<CollectionTransferObject> nameSpaceIterator = theCollectionDAO.getCollection(searchMap);
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
         searchMap.put("nameSpace", "test");
         searchMap.put("producer", "producer");
         Iterator<CollectionTransferObject> collectionIterator = theCollectionDAO.getCollection(searchMap);
         System.out.println ("Do we have next? " +  collectionIterator.hasNext());

         if (collectionIterator.hasNext()) {
             CollectionTransferObject getObj = collectionIterator.next(); 

             TestCase.assertTrue("title is not null", getObj.getTitle() == null);
             int nDeleted = theCollectionDAO.deleteCollectionById(getObj.getDataStoreId());
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
         searchMap.put("nameSpace", "test");
         searchMap.put("title", "title");
         Iterator<CollectionTransferObject> collectionIterator = theCollectionDAO.getCollection(searchMap);
         System.out.println ("Do we have next? " +  collectionIterator.hasNext());

         if (collectionIterator.hasNext()) {
             CollectionTransferObject getObj = collectionIterator.next(); 
             FileDAO theFileDAO = theMongoFactory.getFileDAO();
             FileTransferObject theFile = new FileTransferObject();
             theFile.setURL("http://www.renci.org");
             theFile.setName("file1");
             theFile.setDescription("here's an example file description");
             theFile.setNameSpace("test");
             theFile.setVersion(1);
             HashMap<String, String> fileExtra = new HashMap<String, String>();
             fileExtra.put("author", "Howard Lander");
             fileExtra.put("reason", "Testing the code");
             theFile.setExtra(fileExtra);
             result = theFileDAO.insertFileForCollection(getObj, theFile);

             // Did it get inserted?
             Iterator<FileTransferObject> fileIterator = theFileDAO.getFileForCollection(getObj);
             if (fileIterator.hasNext()) {
                 FileTransferObject theReturnedFile = fileIterator.next();
                 System.out.println("Returned file name: " + theReturnedFile.getName()); 

                 // Let's try the delete
                 int nDeleted = theFileDAO.deleteFileById(theReturnedFile.getDataStoreId());
                 System.out.println("nDeleted: " + nDeleted); 

                 // Let's delete the collection as well.
                 nDeleted = theCollectionDAO.deleteCollectionById(getObj.getDataStoreId());
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
