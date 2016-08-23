package org.renci.databridge.persistence.network;
import  org.neo4j.graphdb.*;
//import  org.neo4j.tooling.*;
import  org.neo4j.graphdb.factory.*;
//import  org.neo4j.cypher.javacompat.*;
import  java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.io.*;

public class Neo4jNetworkNodeDAO implements NetworkNodeDAO {
   private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.network");

   /** 
     *  An iterator class for the NetworkNodeTransferObject.  An instance is returned to the
     *  the user by the getNetworkNodes call.  This implements the Iterator interface.
     */
    private class Neo4jNetworkNodeDAOIterator implements Iterator<NetworkNodeTransferObject> {
       private Iterator<Node> nodeIterator;
       private ArrayList<Node> nodeList;
       private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.network");

       /** 
        * Returns whether or not there is a next item in this cursor.
        */
       @Override
       public boolean hasNext() {
           // Wrap the neo4j iterator
           boolean hasN = false;
           try {
              hasN = nodeIterator.hasNext();
           } catch (Exception e) {
               // should send this back using the message logs eventually
               this.logger.log (Level.SEVERE, "exception in hasNext: " + e.getMessage(), e);
           }
           return hasN;
       }

       /** 
        * Returns the next Object from the cursor or null.
        */
       @Override
       public NetworkNodeTransferObject next() {
           NetworkNodeTransferObject theNetworkTransfer = null; 
           GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
           Transaction tx = theDB.beginTx();
           try {
               if (nodeIterator.hasNext()) {
                   // Translate the date from the Neo4j representation to the 
                   // representation presented to the users in the Transfer object.
                   theNetworkTransfer = new NetworkNodeTransferObject();
                   Node theNode = nodeIterator.next();
                   // Note that at the moment we are assuming that each node is only in one
                   // namespace. I'm not sure that is a good assumption.
                   Iterator<Label> theLabels = theNode.getLabels().iterator();
                   if (theLabels.hasNext()) {
                       Label theFirstLabel = theLabels.next();
                       theNetworkTransfer.setNameSpace(theFirstLabel.name());
                   }

                   HashMap<String, Object> attributes = new HashMap<String, Object>();
                   Iterator<String> theAttributeKeys = theNode.getPropertyKeys().iterator();
                   while (theAttributeKeys.hasNext()) {
                       String theKey = theAttributeKeys.next();
                       if (theKey.compareTo(NetworkNodeDAO.METADATA_NODE_KEY) == 0) {
                           // Found the metadata key
                           theNetworkTransfer.setNodeId((String)theNode.getProperty(NetworkNodeDAO.METADATA_NODE_KEY));
                       } else {
                           // an ordinary attribute
                           attributes.put(theKey,theNode.getProperty(theKey));
                       }
                   }
                   theNetworkTransfer.setAttributes(attributes);
                   theNetworkTransfer.setDataStoreId(Long.toString(theNode.getId()));
               }
           } catch (Exception e) {
               // should send this back using the message logs eventually
               this.logger.log (Level.SEVERE, "exception in next: " + e.getMessage(), e);
           } finally {
               tx.close();
           }
           return theNetworkTransfer;
       }

       /** 
        * Remove the object. Currently and probably always unsupported.
        */
       @Override
       public void remove() throws UnsupportedOperationException{
           throw new UnsupportedOperationException();
       }
    }

    /** 
     * Insert a node into the network.
     * @param transferNode The node to insert
     * @return 1 on success, -1 on failure, 0 if the node already exists.
     */
    public int insertNetworkNode(NetworkNodeTransferObject transferNode) {

       int returnCode = 0;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {
          Label newLabel = Label.label(transferNode.getNameSpace());
          // Check to see if the node already exists
          Iterator<Node> neo4jNodeList = 
               theDB.findNodes(newLabel, NetworkNodeDAO.METADATA_NODE_KEY, transferNode.getNodeId());
          if (neo4jNodeList.hasNext()) {
              // this node is already in the database.  So be it.
              this.logger.log (Level.INFO, "node " + transferNode.getNodeId() + " is already in the database");
              // Make sure the transfer node has the data store id
              this.logger.log (Level.INFO, "adding nodeId to the transferNode");
              Node existingNode = neo4jNodeList.next();
              transferNode.setDataStoreId(Long.toString(existingNode.getId()));
          } else {
              // We need to insert the node, its label and it's associated properties
              Node newNode = theDB.createNode();

              // Save the ID
              transferNode.setDataStoreId(Long.toString(newNode.getId()));

              // The label is the nameSpace
              newNode.addLabel(newLabel);

              // Add all of the attributes for the new node.
              if (null != transferNode.getAttributes()) {
                  for (Map.Entry<String, Object> entry : transferNode.getAttributes().entrySet()){ 
                      newNode.setProperty(entry.getKey(), entry.getValue());
                  } 
              }

              // Don't forget to add the NodeId as an attribute.
              newNode.setProperty(NetworkNodeDAO.METADATA_NODE_KEY, transferNode.getNodeId());
              
              returnCode = 1;
          }
          tx.success();
       } catch (Exception e) {
          returnCode = -1;
          this.logger.log (Level.SEVERE, "exception in insertNetworkNode: " + e.getMessage(), e);
       } finally {
          tx.close();
       }

       return returnCode;
    }
    
    /** 
     * Add a property to the given node.
     * @param transferNode The node for which to insert the property
     * @param key The key for the new property
     * @param value The value for the new property
     * @return true on success, false on failure
     */
    public boolean addPropertyToNetworkNode(NetworkNodeTransferObject transferNode, String key, Object value) {

       boolean returnCode = false;
       Node updateNode = null;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {

          // Get the neo4j node id if it's in the transfer object
          String dataStoreId = transferNode.getDataStoreId();
          // Is the dataStoreId in the transfer object?
          if (null == dataStoreId) {
              // Nope, try to find the node the hard away.
              Label newLabel = Label.label(transferNode.getNameSpace());
              Iterator<Node> neo4jNodeList = 
                   theDB.findNodes(newLabel, NetworkNodeDAO.METADATA_NODE_KEY, transferNode.getNodeId());
              if (neo4jNodeList.hasNext()) {
                  updateNode = neo4jNodeList.next();    
              } else {
                  // this node is not found, hmm
                  this.logger.log (Level.SEVERE, "node " + transferNode.getNodeId() + " is not found");
              }
          } else {
              long nodeId = Long.valueOf(dataStoreId).longValue();
              updateNode = theDB.getNodeById(nodeId);
              if (null == updateNode) {
                  this.logger.log (Level.SEVERE, "node " + nodeId + " is not found");
              }
          }
          if (null != updateNode) {
              // Cool, we've got a node to update.
              updateNode.setProperty(key, value);
              returnCode = true;
          }
          tx.success();
       } catch (Exception e) {
          this.logger.log (Level.SEVERE, "exception in addPropertyToNetworkNode: " + e.getMessage(), e);
       } finally {
          tx.close();
       }
       return returnCode;
    }
    
    /**
     * Retrieve the value of a property of a node
     * @param transferNode The node from which to retrieve the property
     * @param key Which property to retrieve
     * @return Object with the value of the property or null if the property does not exist on the node.
     */
    public Object getPropertyFromNetworkNode(NetworkNodeTransferObject transferNode, String key) {
       Object returnObject = null;
       Node updateNode = null;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {

          // Get the neo4j node id if it's in the transfer object
          String dataStoreId = transferNode.getDataStoreId();
          // Is the dataStoreId in the transfer object?
          if (null == dataStoreId) {
              // Nope, try to find the node the hard away.
              Label newLabel = Label.label(transferNode.getNameSpace());
              Iterator<Node> neo4jNodeList = 
                   theDB.findNodes(newLabel, NetworkNodeDAO.METADATA_NODE_KEY, transferNode.getNodeId());
              if (neo4jNodeList.hasNext()) {
                  updateNode = neo4jNodeList.next();    
              } else {
                  // this node is not found, hmm
                  this.logger.log (Level.SEVERE, "node " + transferNode.getNodeId() + " is not found");
              }
          } else {
              long nodeId = Long.valueOf(dataStoreId).longValue();
              updateNode = theDB.getNodeById(nodeId);
              if (null == updateNode) {
                  this.logger.log (Level.SEVERE, "node " + nodeId + " is not found");
              }
          }
          if (null != updateNode) {
              // Cool, we've got a node from which to get the property
              returnObject = updateNode.getProperty(key, null);
          }
          tx.success();
       } catch (Exception e) {
          this.logger.log (Level.SEVERE, "exception in getPropertyFromNetworkNode: " + e.getMessage(), e);
       } finally {
          tx.close();
       }
       return returnObject;
    }
    
    /**
     * Delete a property from the node
     * @param transferNode The node from which to delete the property
     * @param key Which property to delete
     * @return Object with the value of the deleted property or null if the property did not exist on the node.
     */
    public Object deletePropertyFromNetworkNode(NetworkNodeTransferObject transferNode, String key) {
       Object returnObject = null;
       Node updateNode = null;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {

          // Get the neo4j node id if it's in the transfer object
          String dataStoreId = transferNode.getDataStoreId();
          // Is the dataStoreId in the transfer object?
          if (null == dataStoreId) {
              // Nope, try to find the node the hard away.
              Label newLabel = Label.label(transferNode.getNameSpace());
              Iterator<Node> neo4jNodeList = 
                   theDB.findNodes(newLabel, NetworkNodeDAO.METADATA_NODE_KEY, transferNode.getNodeId());
              if (neo4jNodeList.hasNext()) {
                  updateNode = neo4jNodeList.next();    
              } else {
                  // this node is not found, hmm
                  this.logger.log (Level.SEVERE, "node " + transferNode.getNodeId() + " is not found");
              }
          } else {
              long nodeId = Long.valueOf(dataStoreId).longValue();
              updateNode = theDB.getNodeById(nodeId);
              if (null == updateNode) {
                  this.logger.log (Level.SEVERE, "node " + nodeId + " is not found");
              }
          }
          if (null != updateNode) {
              // Cool, we've got a node from which to get the property
              returnObject = updateNode.removeProperty(key);
          }
          tx.success();
       } catch (Exception e) {
          this.logger.log (Level.SEVERE, "exception in removePropertyFromNetworkNode: " + e.getMessage(), e);
       } finally {
          tx.close();
       }
       return returnObject;
    }
    
    
    /**
     * Retrieve an iterator for all nodes that match the given search key.
     * @param theNode The node containing the nameSpace in which to search
     * @param key The key for the new property
     * @param value The value for the new property
     * @return The iterator
     */
    public Iterator<NetworkNodeTransferObject> 
        getNetworkNodes(NetworkNodeTransferObject theNode, String key, Object value){

        Transaction tx = null;
        Neo4jNetworkNodeDAOIterator theIterator = new Neo4jNetworkNodeDAOIterator();

        try {
            GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
            tx = theDB.beginTx();

            Label newLabel = Label.label(theNode.getNameSpace());
            Iterator<Node> neo4jNodeList = theDB.findNodes(newLabel, key, value);
            theIterator.nodeList = new ArrayList<Node>();

            // We don't really like this, but changes in the Neo4j API for version 3 
            // don't leave us a lot of choice. We could try moving the transaction into the
            // iterator, but for now we are going to live with this. See 
            // http://stackoverflow.com/questions/39046000/issue-upgrading-from-neo4j-2-to-3-using-embedded-java-api-exception-in-hasnexti
            while(neo4jNodeList.hasNext()) {
               Node thisNode = neo4jNodeList.next();
               theIterator.nodeList.add(thisNode);
            }
            theIterator.nodeIterator = theIterator.nodeList.iterator();
        } catch (Exception e) {
            // should send this back using the message logs eventually
            this.logger.log (Level.SEVERE, "exception in getNetworkNodes: " + e.getMessage(), e);
        } finally {
            tx.close();
        }

        return theIterator;
    }

       /** 
        * Retrieve the node specified by the id parameter.
        * @param theId The string for which to return the node
        * @return The requested node
        */
       public NetworkNodeTransferObject getNetworkNode(String id) {
           NetworkNodeTransferObject theNetworkTransfer = null; 
           GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
           Transaction tx = theDB.beginTx();
           try {
               Node theNode = theDB.getNodeById(Long.valueOf(id).longValue());
               if (null != theNode) {
                   // Translate the date from the Neo4j representation to the 
                   // representation presented to the users in the Transfer object.
                   theNetworkTransfer = new NetworkNodeTransferObject();
                   // Note that at the moment we are assuming that each node is only in one
                   // namespace. I'm not sure that is a good assumption.
                   Iterator<Label> theLabels = theNode.getLabels().iterator();
                   if (theLabels.hasNext()) {
                       Label theFirstLabel = theLabels.next();
                       theNetworkTransfer.setNameSpace(theFirstLabel.name());
                   }

                   HashMap<String, Object> attributes = new HashMap<String, Object>();
                   Iterator<String> theAttributeKeys = theNode.getPropertyKeys().iterator();
                   while (theAttributeKeys.hasNext()) {
                       String theKey = theAttributeKeys.next();
                       if (theKey.compareTo(NetworkNodeDAO.METADATA_NODE_KEY) == 0) {
                           // Found the metadata key
                           theNetworkTransfer.setNodeId((String)theNode.getProperty(NetworkNodeDAO.METADATA_NODE_KEY));
                       } else {
                           // an ordinary attribute
                           attributes.put(theKey,theNode.getProperty(theKey));
                       }
                   }
                   theNetworkTransfer.setAttributes(attributes);
                   theNetworkTransfer.setDataStoreId(Long.toString(theNode.getId()));
               }
           } catch (Exception e) {
               // should send this back using the message logs eventually
               this.logger.log (Level.SEVERE, "exception in getNetworkNodeById: " + e.getMessage(), e);
           } finally {
               tx.close();
           }
           return theNetworkTransfer;
       }
    
    /**
     * Retrieve an iterator for all nodes that match the given search key.
     * @param nameSpace The nameSpace in which to search
     * @param key The key for the new property
     * @param value The value for the new property
     * @return The iterator
     */
    public Iterator<NetworkNodeTransferObject> 
        getNetworkNodes(String nameSpace, String key, Object value){

        Neo4jNetworkNodeDAOIterator theIterator = null;
        GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
        Transaction tx = theDB.beginTx();
        try {

            Label newLabel = Label.label(nameSpace);
            Iterator<Node> neo4jNodeList = theDB.findNodes(newLabel, key, value);
            theIterator = new Neo4jNetworkNodeDAOIterator();
            theIterator.nodeList = new ArrayList<Node>();
            // We don't really like this, but changes in the Neo4j API for version 3 
            // don't leave us a lot of choice. We could try moving the transaction into the
            // iterator, but for now we are going to live with this. See 
            // http://stackoverflow.com/questions/39046000/issue-upgrading-from-neo4j-2-to-3-using-embedded-java-api-exception-in-hasnexti
            while(neo4jNodeList.hasNext()) {
               Node thisNode = neo4jNodeList.next();
               theIterator.nodeList.add(thisNode);
            }
            //theIterator.nodeIterator = neo4jNodeList;
            theIterator.nodeIterator = theIterator.nodeList.iterator();
        } catch (Exception e) {
            // should send this back using the message logs eventually
            this.logger.log (Level.SEVERE, "exception in getNetworkNodes: " + e.getMessage(), e);
        } finally {
            tx.close();
        }

        return theIterator;
    }
    
    /**
     * Retrieve an iterator for all nodes in a given nameSpace
     * @param nameSpace The nameSpace in which to search
     * @return The iterator
     */
    public Iterator<NetworkNodeTransferObject> 
        getNetworkNodesForNameSpace(String nameSpace) {

        Neo4jNetworkNodeDAOIterator theIterator = null;
        GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
        Transaction tx = theDB.beginTx();
        //GlobalGraphOperations graphOperations = GlobalGraphOperations.at(theDB);
        try {

            Label newLabel = Label.label(nameSpace);
            Iterator<Node> neo4jNodeList = theDB.findNodes(newLabel);
            theIterator = new Neo4jNetworkNodeDAOIterator();
            theIterator.nodeIterator = neo4jNodeList;
        } catch (Exception e) {
            // should send this back using the message logs eventually
            this.logger.log (Level.SEVERE, "exception in getNetworkNodesForNameSpace: " + e.getMessage(), e);
       } finally {
            tx.close();
        }

        return theIterator;
    }


    /** 
     * Delete the given node
     * @param theTransferNode The node to delete
     * @return true on success, false on failure
     */
    public boolean deleteNetworkNode(NetworkNodeTransferObject theTransferNode) {
       boolean returnCode = false;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {
         // Note that in order to delete a node in neo4j you have to delete all it's
         // relationships first...
         // get the node
         long nodeId = Long.valueOf(theTransferNode.getDataStoreId()).longValue();
         Node theNode = theDB.getNodeById(nodeId);

         // get an iterator to it's relationships and delete them all.
         Iterator<Relationship> relationships = theNode.getRelationships().iterator();
         while (relationships.hasNext()) {
             Relationship theRelationship = relationships.next();
             theRelationship.delete();
         }
         
         // delete the node. 
         theNode.delete();
         tx.success();
         returnCode = true;
       } catch (Exception e) {
          this.logger.log (Level.SEVERE, "exception in deleteNetworkNode: " + e.getMessage(), e);
          tx.failure();
       } finally {
          tx.close();
       }
       return returnCode;
    }
}
