package org.renci.databridge.persistence.network;
import  org.neo4j.graphdb.*;
//import  org.neo4j.tooling.*;
//import  org.neo4j.graphdb.factory.*;
//import  org.neo4j.cypher.javacompat.*;
import  java.util.*;
import  java.util.logging.Logger;
import  java.util.logging.Level;

public class Neo4jNetworkRelationshipDAO implements NetworkRelationshipDAO {
    private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.network");
    
   /**
     *  An iterator class for the NetworkNodeTransferObject.  An instance is returned to the
     *  the user by the getNetworkNodes call.  This implements the Iterator interface.
     */
    private class Neo4jNetworkRelationshipDAOIterator implements Iterator<NetworkRelationshipTransferObject> {
       private Iterator<Relationship> relationshipIterator;
       private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.network");

       /**
        * Returns whether or not there is a next item in this iterator
        */
       @Override
       public boolean hasNext() {
           // Wrap the neo4j iterator
           return relationshipIterator.hasNext();
       }

       /**
        * Returns the next Object from the iterator or null.
        */
       @Override
       public NetworkRelationshipTransferObject next() {
           NetworkRelationshipTransferObject theTransfer = null;
           GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
           Transaction tx = theDB.beginTx();
           try {
               if (relationshipIterator.hasNext()) {
                   // Translate the date from the Neo4j representation to the
                   // representation presented to the users in the Transfer object.
                   theTransfer = new NetworkRelationshipTransferObject();
                   Relationship theRelationship = relationshipIterator.next();
                   Node[] theNodes = theRelationship.getNodes();
                   theTransfer.setNodeId1(Long.toString(theNodes[0].getId()));
                   theTransfer.setNodeId2(Long.toString(theNodes[1].getId()));
                   theTransfer.setType(theRelationship.getType().name());

                   HashMap<String, Object> attributes = new HashMap<String, Object>();
                   Iterator<String> theAttributeKeys = theRelationship.getPropertyKeys().iterator();
                   while (theAttributeKeys.hasNext()) {
                       String theKey = theAttributeKeys.next();
                       // an ordinary attribute
                       attributes.put(theKey,theRelationship.getProperty(theKey));
                   }
                   theTransfer.setAttributes(attributes);
               }
           } catch (Exception e) {
               // should send this back using the message logs eventually
               this.logger.log (Level.SEVERE, "exception in next function: " + e.getMessage(), e);
           } finally {
               tx.close();
           }
           return theTransfer;
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
     *  Insert a relationship into the network database.
     *
     *  @param theTransfer object containing the relationship type, and the
     *         attributes (if any) of the relationship
     *  @param transferNode1 The first node of the relationship
     *  @param transferNode2 The second node of the relationship
     *  @return true on success, false on failure
     */
    public boolean insertNetworkRelationship(NetworkRelationshipTransferObject theTransfer, 
                    NetworkNodeTransferObject transferNode1, NetworkNodeTransferObject transferNode2) {
       boolean returnCode = false;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {
          // First we need the 2 nodes.
          long node1Id = Long.valueOf(transferNode1.getDataStoreId()).longValue();
          Node node1 = tx.getNodeById(node1Id); 
          long node2Id = Long.valueOf(transferNode2.getDataStoreId()).longValue();
          Node node2 = tx.getNodeById(node2Id); 

          // Create the Relationship. Note that we don't care about directionality and neither
          // does Neo4j (much)
          RelationshipType newType = RelationshipType.withName(theTransfer.getType());
          Relationship theRelationship = node1.createRelationshipTo(node2, newType);

          // Add all of the attributes for the relationship, if any
          if (null != theTransfer.getAttributes()) {
             for (Map.Entry<String, Object> entry : theTransfer.getAttributes().entrySet()) {
                // System.out.println("setting attributes with key: " + entry.getKey() + " value: " +
                 //                    entry.getValue());
                 theRelationship.setProperty(entry.getKey(), entry.getValue());
             }
          }

          theTransfer.setDataStoreId(Long.toString(theRelationship.getId()));
          theTransfer.setNode1DataStoreId(transferNode1.getDataStoreId());
          theTransfer.setNode2DataStoreId(transferNode2.getDataStoreId());
          returnCode = true;
          tx.close();
       } catch (Exception e) {
          tx.terminate();
          this.logger.log (Level.SEVERE, "exception in insertNetworkRelationship: " + e.getMessage(), e);
       } finally {
          tx.close();
       }

       return returnCode;
    }

    /**
     *  Add a property to an existing relationship. Note that if the property already exists, the old 
     *  value is overwritten with the new value.  
     *
     *  @param theTransfer object containing the existing relationship to which the property will be added.
     *  @param key The key for the property to add
     *  @param value The value for the property to add
     *  @return the requested iterator
     */
    public boolean addPropertyToNetworkRelationship(NetworkRelationshipTransferObject theTransfer, 
                                                    String key, Object value) {
       boolean returnCode = false;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {
          long relId = Long.valueOf(theTransfer.getDataStoreId()).longValue();
          Relationship theRelationship = tx.getRelationshipById(relId);
          theRelationship.setProperty(key, value);
          returnCode = true;
          tx.close();
       } catch (Exception e) {
          tx.close();
          this.logger.log (Level.SEVERE, "exception in Neo4jNetworkRelationshipDAO: " + e.toString());
       } finally {
          tx.close();
       }

       return returnCode;
    }

    /**
     *  Delete a property from an existing relationship. 
     *
     *  @param theTransfer object containing the existing relationship from which the property will be deleted.
     *  @param key The key for the property to delete
     *  @return true on success, false on failure
     */
    public boolean deletePropertyFromNetworkRelationship(NetworkRelationshipTransferObject theTransfer, 
                                                    String key) {
       boolean returnCode = false;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {
          long relId = Long.valueOf(theTransfer.getDataStoreId()).longValue();
          Relationship theRelationship = tx.getRelationshipById(relId);
          theRelationship.removeProperty(key);
          returnCode = true;
          tx.close();
       } catch (Exception e) {
          tx.terminate();
          this.logger.log (Level.SEVERE, "exception in deletePropertyFromNetworkRelationship: "+ e.toString());
       } finally {
          tx.close();
       }

       return returnCode;
    }

    /**
     *  Retrieve a property from an existing relationship. 
     *
     *  @param theTransfer object containing the relationship from which the property will be retrieved.
     *  @param key The key for the property to retrieve
     *  @return An object containing the value of the specified property of the given relationship.  At the
     *          moment that will always be a string, but that may change in the future.
     */
    public Object getPropertyFromNetworkRelationship(NetworkRelationshipTransferObject theTransfer, 
                                                    String key) {
       Object objectProperty = null;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {
          long relId = Long.valueOf(theTransfer.getDataStoreId()).longValue();
          Relationship theRelationship = tx.getRelationshipById(relId);
          objectProperty = theRelationship.getProperty(key);
          tx.close();
       } catch (org.neo4j.graphdb.NotFoundException e) {
          this.logger.log (Level.INFO, "property to delete not found: " + e.toString());
          tx.close();
       } catch (Exception e) {
          tx.terminate();
          this.logger.log (Level.SEVERE, "exception in getPropertyFromNetworkRelationship: "+ e.toString());
       } finally {
          tx.close();
       }

       return objectProperty;
    }

    /**
     *  Retrieve an iterator for all the relationships of the the specified node.
     *  @param theTransferNode object containing the nodes, the relationship type, and the
     *         attributes (if any) of the relationship
     *  @return The requested iterator
     */
    public Iterator<NetworkRelationshipTransferObject>
        getNetworkRelationships(NetworkNodeTransferObject theTransferNode){

        Neo4jNetworkRelationshipDAOIterator theIterator = null;
        GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
        Transaction tx = theDB.beginTx();
        try {
            long nodeId = Long.valueOf(theTransferNode.getDataStoreId()).longValue();
            Node theNode = tx.getNodeById(nodeId);
            Iterator<Relationship> neo4jRelationshipList =
                   theNode.getRelationships().iterator();
            theIterator = new Neo4jNetworkRelationshipDAOIterator();
            theIterator.relationshipIterator = neo4jRelationshipList;
        } catch (Exception e) {
            // should send this back using the message logs eventually
            e.printStackTrace();
        } finally {
            tx.close();
        }

        return theIterator;
    }

    /**
     * Retrieve an iterator for all the relationships of the the specified node that
     * are of a type specified in the key param. In our data model, at this writing, 
     * we will normally only expect one answer, but just in case that changes we are
     * returning an iterator.
     *  @param theTransferNode object containing the nodes, the relationship type, and the
     *         attributes (if any) of the relationship
     *  @param key String containing the relationship type.
     *  @return the requested iterator
     */
    public Iterator<NetworkRelationshipTransferObject>
        getNetworkRelationships(NetworkNodeTransferObject theTransferNode, String key){

        Neo4jNetworkRelationshipDAOIterator theIterator = null;
        GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
        Transaction tx = theDB.beginTx();
        try {
            long nodeId = Long.valueOf(theTransferNode.getDataStoreId()).longValue();
            Node theNode = tx.getNodeById(nodeId);
            RelationshipType theType = RelationshipType.withName(key);
            Iterator<Relationship> neo4jRelationshipList =
                   theNode.getRelationships(theType).iterator();
            theIterator = new Neo4jNetworkRelationshipDAOIterator();
            theIterator.relationshipIterator = neo4jRelationshipList;
        } catch (Exception e) {
            // should send this back using the message logs eventually
            this.logger.log (Level.SEVERE, "exception in getNetworkRelationships: "+ e.toString());
        } finally {
            tx.close();
        }

        return theIterator;
    }

    /**
     *  Delete a relationship from the network database. Uses the dataStoreId in the
     *  relationship transfer object.
     *
     *  @param theTransfer object containing the nodes, the relationship type, and the
     *         attributes (if any) of the relationship
     */
    public boolean deleteNetworkRelationship(NetworkRelationshipTransferObject theTransfer) {
       boolean returnCode = false;

       GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
       Transaction tx = theDB.beginTx();
       try {
          long relId = Long.valueOf(theTransfer.getDataStoreId()).longValue();
          Relationship theRelationship = tx.getRelationshipById(relId);
          theRelationship.delete();
          returnCode = true;
          tx.close();
       } catch (org.neo4j.graphdb.NotFoundException e) {
          returnCode = true;
          this.logger.log (Level.INFO, "relationship to delete not found: " + e.toString());
          tx.close();
       } catch (Exception e) {
          tx.terminate();
          this.logger.log (Level.SEVERE, "exception in Neo4jNetworkRelationshipDAO: " + e.toString());
       } finally {
          tx.close();
       }

       return returnCode;
    }
}
