package org.renci.databridge.persistence.network;

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

public class NetworkTest {


  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }
  
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testNetworkNodeDAO () throws Exception {

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testNetworkNodeDAO");
     int result;

     NetworkDAOFactory theNeo4jFactory = 
        NetworkDAOFactory.getNetworkDAOFactory(NetworkDAOFactory.NEO4JDB, "testData");
     try {
         NetworkNodeDAO theNetworkNodeDAO = theNeo4jFactory.getNetworkNodeDAO();
         NetworkNodeTransferObject theNode = new NetworkNodeTransferObject();
         theNode.setNameSpace("junit_test");
         theNode.setNodeId("1");
         HashMap<String, Object> attributes = new HashMap<String, Object>();
         attributes.put("sna1", "1,4");
         theNode.setAttributes(attributes);

         // Insert the node
         result = theNetworkNodeDAO.insertNetworkNode(theNode);
         System.out.println("done with insert: number inserted is " + result);
         System.out.println("inserted Id is: " + theNode.getDataStoreId());
         System.out.println("testing get");
         TestCase.assertTrue("result not 1", result == 1);

         // try to insert this again, it should fail with result 0
         result = theNetworkNodeDAO.insertNetworkNode(theNode);
         System.out.println("done with insert: number inserted is " + result);
         TestCase.assertTrue("result not 0", result == 0);

         // Test the properties
         boolean propResult = theNetworkNodeDAO.addPropertyToNetworkNode(theNode, "testProp", "testValue");
         System.out.println("appProperty result: " + propResult);
         TestCase.assertTrue("propResult not true", propResult == true);

         // Let's retrieve the node using the metadata id.
         Iterator<NetworkNodeTransferObject> theNodes = 
            theNetworkNodeDAO.getNetworkNodes("junit_test", NetworkNodeDAO.METADATA_NODE_KEY, "1");
         if (theNodes.hasNext()) {
             NetworkNodeTransferObject returnedNode = theNodes.next();
             System.out.println("returned nameSpace: " + returnedNode.getNameSpace());
             System.out.println("returned nodeId: " + returnedNode.getNodeId());
             System.out.println("returned dataStoreId: " + returnedNode.getDataStoreId());
             System.out.println("returned attributes: " + returnedNode.getAttributes());
             TestCase.assertTrue("dataStoreIds don't match", theNode.getDataStoreId().compareTo(returnedNode.getDataStoreId()) == 0);
             // Now let's delete this node
             propResult = theNetworkNodeDAO.deleteNetworkNode(theNode); 
             TestCase.assertTrue("propResult not true", propResult == true);
         }  

         // Now let's test multiple insertions, gets and deletes
         System.out.println("starting multiple insertions");
         for (int i = 0; i < 5; i ++) {
             theNode.setNodeId(Integer.toString(i));
             theNode.setNameSpace("junit_test");
             result = theNetworkNodeDAO.insertNetworkNode(theNode);
             System.out.println("done with insert: number inserted is " + result);
             System.out.println("inserted Id is: " + theNode.getDataStoreId());
         }

         theNodes = theNetworkNodeDAO.getNetworkNodesForNameSpace("junit_test");
         int nDeleted = 0;
         System.out.println("Time to start deleting...");
         while (theNodes.hasNext()) {
            NetworkNodeTransferObject returnedNode = theNodes.next();
            System.out.println("returned nameSpace: " + returnedNode.getNameSpace());
            System.out.println("returned nodeId: " + returnedNode.getNodeId());
            System.out.println("returned dataStoreId: " + returnedNode.getDataStoreId());
            System.out.println("returned attributes: " + returnedNode.getAttributes());
            // Now let's delete this node
            System.out.println("deleting node with dataStoreId: " + returnedNode.getDataStoreId());
            propResult = theNetworkNodeDAO.deleteNetworkNode(returnedNode); 
            TestCase.assertTrue("propResult not true", propResult == true);
            nDeleted++; 
         }
         TestCase.assertTrue("nDeleted not == 5", nDeleted == 5);

     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  @Test
  public void testNetworkRelationshipDAO () throws Exception {

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testNetworkRelationshipDAO");
     int result;
     boolean returnCode;

     NetworkDAOFactory theNeo4jFactory = 
        NetworkDAOFactory.getNetworkDAOFactory(NetworkDAOFactory.NEO4JDB, "testData");
     try {
         NetworkNodeDAO theNetworkNodeDAO = theNeo4jFactory.getNetworkNodeDAO();
         NetworkRelationshipDAO theNetworkRelationshipDAO = theNeo4jFactory.getNetworkRelationshipDAO();

         NetworkNodeTransferObject node1 = new NetworkNodeTransferObject();
         node1.setNameSpace("junit_test");
         node1.setNodeId("1");
         HashMap<String, Object> attributes1 = new HashMap<String, Object>();
         attributes1.put("sna1", "1,4");
         node1.setAttributes(attributes1);

         NetworkNodeTransferObject node2 = new NetworkNodeTransferObject();
         node2.setNameSpace("junit_test");
         node2.setNodeId("2");
         HashMap<String, Object> attributes2 = new HashMap<String, Object>();
         attributes2.put("sna1", "2,3");
         node2.setAttributes(attributes2);

         // Insert the nodes
         result = theNetworkNodeDAO.insertNetworkNode(node1);
         System.out.println("node1 returned dataStoreId: " + node1.getDataStoreId());
         result = theNetworkNodeDAO.insertNetworkNode(node2);
         System.out.println("node2 returned dataStoreId: " + node2.getDataStoreId());

         // Now create the Relationship Transfer Object
         NetworkRelationshipTransferObject theNetworkTransfer = new NetworkRelationshipTransferObject();
         theNetworkTransfer.setType("Similarity");
         HashMap<String, Object> netAttributes = new HashMap<String, Object>();
         netAttributes.put("sna1", ".7");
         netAttributes.put("sna2", ".4");
         theNetworkTransfer.setAttributes(netAttributes);

         returnCode = theNetworkRelationshipDAO.insertNetworkRelationship(theNetworkTransfer, node1, node2);
         System.out.println("relationship returned dataStoreId: " + theNetworkTransfer.getDataStoreId());

         String theValue = (String)
             theNetworkRelationshipDAO.getPropertyFromNetworkRelationship(theNetworkTransfer, "sna1");
         System.out.println("value returned for prop sna1 is: " + theValue);
         TestCase.assertTrue("prop sna1 has the wrong value", theValue.compareTo(".7") == 0);
         boolean attrReturn = 
             theNetworkRelationshipDAO.deletePropertyFromNetworkRelationship(theNetworkTransfer, "sna1");
         TestCase.assertTrue("failed to delete attribute", attrReturn == true);

         // What happens after the delete?
         theValue = (String)
             theNetworkRelationshipDAO.getPropertyFromNetworkRelationship(theNetworkTransfer, "sna1");
         System.out.println("value returned for prop sna1 should be null, is: " + theValue);
         TestCase.assertTrue("theValue should be null", theValue == null);
         
         // try to retrieve the inserted relationship
         Iterator<NetworkRelationshipTransferObject> theRels = 
             theNetworkRelationshipDAO.getNetworkRelationships(node1);
         if (theRels.hasNext()) {
             NetworkRelationshipTransferObject returnedTransfer = theRels.next();
             System.out.println("returned type: " +  returnedTransfer.getType());
             System.out.println("returned attrs: " + returnedTransfer.getAttributes());
             TestCase.assertTrue("type wrong: ", returnedTransfer.getType().compareTo("Similarity") ==0);
         }
         
         // this one should not return anything
         Iterator<NetworkRelationshipTransferObject> moreRels = 
             theNetworkRelationshipDAO.getNetworkRelationships(node1, "testRel");
         TestCase.assertTrue("Found attributes when should not have", moreRels.hasNext() == false);

         // Let's try to add an attribute to the relationship
         boolean addReturn = 
             theNetworkRelationshipDAO.addPropertyToNetworkRelationship(theNetworkTransfer,  "value", ".5");
         TestCase.assertTrue("couldn't add property to relationship", addReturn == true);

         // What happens when we try to add this for the second time? Value is overwritten...
         addReturn = 
             theNetworkRelationshipDAO.addPropertyToNetworkRelationship(theNetworkTransfer,  "value", ".4");
         TestCase.assertTrue("couldn't add property to relationship", addReturn == true);
       
         // Cleanup phase. Delete the nodes and the relationship
         boolean deleteReturn;
         deleteReturn = theNetworkNodeDAO.deleteNetworkNode(node1);
         TestCase.assertTrue("failed to delete node1", deleteReturn == true);
         
         deleteReturn = theNetworkNodeDAO.deleteNetworkNode(node2);
         TestCase.assertTrue("failed to delete node2", deleteReturn == true);
         
         deleteReturn = theNetworkRelationshipDAO.deleteNetworkRelationship(theNetworkTransfer);
         TestCase.assertTrue("failed to delete relationship", deleteReturn == true);
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
