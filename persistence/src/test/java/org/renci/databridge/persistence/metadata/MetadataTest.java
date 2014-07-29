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

  /**
   * @todo This is a transport test. Needs to be factored out properly.
   * @todo Put in a proper junit test assertion...
   */
  @Test
  public void testInsert () throws Exception {

     System.out.println("beginning testInsert");
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
         CollectionTransferObject getObj = theCollectionDAO.getCollection(searchMap);
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

         // now we try the delete
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
