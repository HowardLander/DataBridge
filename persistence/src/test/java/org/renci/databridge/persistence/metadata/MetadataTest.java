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

     CollectionDAO theCollectionDAO = theMongoFactory.getCollectionDAO();
     try {
         result = theCollectionDAO.insertCollection(theCollection);
     }  catch (Exception e) {
         e.printStackTrace();
     }
     System.out.println("done with insert");
     System.out.println("testing get");

     HashMap<String, String> searchMap = new HashMap<String, String>();
     searchMap.put("nameSpace", "test");
     searchMap.put("title", "title");
     CollectionTransferObject getObj = theCollectionDAO.getCollection(searchMap);
     TestCase.assertTrue("subjects don't match", 
         theCollection.getSubject().compareTo(getObj.getSubject()) == 0);
     System.out.println("retrieved subject: " + getObj.getSubject());

  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

/*
  public static void main (String [] args) throws Exception {

  }
*/
}
