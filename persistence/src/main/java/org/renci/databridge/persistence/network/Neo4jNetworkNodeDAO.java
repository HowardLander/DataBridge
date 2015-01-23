package org.renci.databridge.persistence.network;
import  org.neo4j.graphdb.*;
import  org.neo4j.tooling.*;
import  org.neo4j.graphdb.factory.*;
import  org.neo4j.cypher.javacompat.*;
import  java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Neo4jNetworkNodeDAO implements NetworkNodeDAO {
   private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.network");

   /** 
     *  An iterator class for the NetworkNodeTransferObject.  An instance is returned to the
     *  the user by the getNetworkNodes call.  This implements the Iterator interface.
     */
    private class Neo4jNetworkNodeDAOIterator implements Iterator<NetworkNodeTransferObject> {
       private Iterator<Node> nodeIterator;
       private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.network");

       /** 
        * Returns whether or not there is a next item in this cursor.
        */
       @Override
       public boolean hasNext() {
           // Wrap the neo4j iterator
           return nodeIterator.hasNext();
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
          Label newLabel = DynamicLabel.label(transferNode.getNameSpace());
          // Check to see if the node already exists
          Iterator<Node> neo4jNodeList = 
               theDB.findNodesByLabelAndProperty(newLabel,
                                                 NetworkNodeDAO.METADATA_NODE_KEY, 
                                                 transferNode.getNodeId()).iterator();
          if (neo4jNodeList.hasNext()) {
              // this node is already in the database.  So be it.
              this.logger.log (Level.INFO, "node " + transferNode.getNodeId() + " is already in the database");
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
              Label newLabel = DynamicLabel.label(transferNode.getNameSpace());
              Iterator<Node> neo4jNodeList = 
                   theDB.findNodesByLabelAndProperty(newLabel,
                                                     NetworkNodeDAO.METADATA_NODE_KEY, 
                                                     transferNode.getNodeId()).iterator();
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
              Label newLabel = DynamicLabel.label(transferNode.getNameSpace());
              Iterator<Node> neo4jNodeList = 
                   theDB.findNodesByLabelAndProperty(newLabel,
                                                     NetworkNodeDAO.METADATA_NODE_KEY, 
                                                     transferNode.getNodeId()).iterator();
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
              Label newLabel = DynamicLabel.label(transferNode.getNameSpace());
              Iterator<Node> neo4jNodeList = 
                   theDB.findNodesByLabelAndProperty(newLabel,
                                                     NetworkNodeDAO.METADATA_NODE_KEY, 
                                                     transferNode.getNodeId()).iterator();
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

        Neo4jNetworkNodeDAOIterator theIterator = null;
        GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
        Transaction tx = theDB.beginTx();
        try {

            Label newLabel = DynamicLabel.label(theNode.getNameSpace());
            Iterator<Node> neo4jNodeList =
                   theDB.findNodesByLabelAndProperty(newLabel, key, value).iterator();
            theIterator = new Neo4jNetworkNodeDAOIterator();
            theIterator.nodeIterator = neo4jNodeList;
        } catch (Exception e) {
            // should send this back using the message logs eventually
            this.logger.log (Level.SEVERE, "exception in getNetworkNodes: " + e.getMessage(), e);
        } finally {
            tx.close();
        }

        return theIterator;
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

            Label newLabel = DynamicLabel.label(nameSpace);
            Iterator<Node> neo4jNodeList =
                   theDB.findNodesByLabelAndProperty(newLabel, key, value).iterator();
            theIterator = new Neo4jNetworkNodeDAOIterator();
            theIterator.nodeIterator = neo4jNodeList;
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
        GlobalGraphOperations graphOperations = GlobalGraphOperations.at(theDB);
        try {

            Label newLabel = DynamicLabel.label(nameSpace);
            Iterator<Node> neo4jNodeList =
                   graphOperations.getAllNodesWithLabel(newLabel).iterator();
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
