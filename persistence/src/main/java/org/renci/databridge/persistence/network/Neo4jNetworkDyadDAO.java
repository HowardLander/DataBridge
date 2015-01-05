package org.renci.databridge.persistence.network;
import  org.neo4j.graphdb.*;
import  org.neo4j.tooling.*;
import  org.neo4j.graphdb.factory.*;
import  org.neo4j.cypher.javacompat.*;
import  java.util.*;
import  java.util.logging.Logger;
import  java.util.logging.Level;

public class Neo4jNetworkDyadDAO implements NetworkDyadDAO {
    private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.network");
    
   /**
     *  An iterator class for the NetworkDyadTransferObject.  An instance is returned to the
     *  the user by the getNetworkDyads call.  This implements the Iterator interface.
     */
    private class Neo4jNetworkDyadDAOIterator implements Iterator<NetworkDyadTransferObject> {
       private Iterator<Node> nodeIterator = Collections.emptyIterator();
       private Iterator<Relationship> relationshipIterator = Collections.emptyIterator();
       private String currentNodeId = null;
       private Node currentNode = null;
       private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.network");
       private RelationshipType newType;

       /**
        * Returns whether or not there is a next item in this iterator
        */
       @Override
       public boolean hasNext() {
           return (relationshipIterator.hasNext() || nodeIterator.hasNext());
       }

       /**
        * Returns the next Object from the iterator or null.
        */
       @Override
       public NetworkDyadTransferObject next() {
           NetworkDyadTransferObject theTransfer = null;
           GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
           Transaction tx = theDB.beginTx();
           boolean stillLooking = true;
           try {
               // Translate the date from the Neo4j representation to the
               // representation presented to the users in the Transfer object.
               while (stillLooking) {
                  if (relationshipIterator.hasNext()) {
                      // The relationship iterator is always interating over the relationships of
                      // the "currentNode"
                      theTransfer = new NetworkDyadTransferObject();
                      theTransfer.setNode1DataStoreId(Long.toString(currentNode.getId()));
                      Relationship theRel = relationshipIterator.next();
                      double simValue = (double)
                             theRel.getProperty(NetworkRelationshipDAO.METADATA_SIMILARITY_PROPERTY_NAME);
                      theTransfer.setSimilarity(simValue);
                      Node theOtherNode = theRel.getOtherNode(currentNode);
                      theTransfer.setNode2DataStoreId(Long.toString(theOtherNode.getId()));
                      stillLooking = false;
                  }  else if (nodeIterator.hasNext()) {
                      // All the relationships of the currentNode have been exhausted. If there is
                      // another node, we start with it's relationships.
                      theTransfer = new NetworkDyadTransferObject();
                      Node theNode = nodeIterator.next();
                      currentNode = theNode;
                      theTransfer.setNode1DataStoreId(Long.toString(theNode.getId()));

                      // We also have to reset the relationship iterator to access the relationships
                      // of this node
                      // Note that we are initially only checking for OUTGOING relationships.  Each rel
                      // must be either incoming or outgoing so this will allow us to traverse every 
                      // in the subgraph.
                      relationshipIterator = theNode.getRelationships(newType,Direction.OUTGOING).iterator();
                      if (relationshipIterator.hasNext()) {
                          Relationship theRel = relationshipIterator.next();
                          double simValue = (double)
                             theRel.getProperty(NetworkRelationshipDAO.METADATA_SIMILARITY_PROPERTY_NAME);
                          theTransfer.setSimilarity(simValue);
                          Node theOtherNode = theRel.getOtherNode(theNode);
                          theTransfer.setNode2DataStoreId(Long.toString(theOtherNode.getId()));
                          stillLooking = false;
                      } else {
                          // Why are we check for INCOMING relationships? The reason is to see if this
                          // node is truly unrelated to others, or if all it's relationships are INCOMING
                          // and so it wasn't caught in the previous test. We handle these two
                          // cases differently.  If the node is truly unrelated to others, we want to
                          // return it in the next call, in case the caller has any use for unrelated
                          // nodes.  But if it has incoming relationships, than we want to go to the 
                          // next node.
                          relationshipIterator = 
                              theNode.getRelationships(newType, Direction.INCOMING).iterator();
                          if (relationshipIterator.hasNext()) {
                              // this node is connected, we want to skip it.
                              stillLooking = true;
                          } else {
                              // an unconnected node we want to return
                              stillLooking = false;
                          } 
                          relationshipIterator = Collections.emptyIterator();
                      }
                  } else {
                      // no more nodes, so we are done.
                      stillLooking = false;
                  }
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
     *  Retrieve an iterator for all the dyads in the network that exist in the given
     *  nameSpace and were produced using the given similarityID
     *  @param nameSpace The nameSpace in which to search for dyads
     *  @param similarityId The similarity metric to use in the search for dyads
     *  @return the requested iterator
     */
    public Iterator<NetworkDyadTransferObject>
        getNetworkDyads(String nameSpace, String similarityId) {

        Neo4jNetworkDyadDAOIterator theIterator = null;
        GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
        Transaction tx = theDB.beginTx();
        GlobalGraphOperations graphOperations = GlobalGraphOperations.at(theDB);
        try {
            theIterator = new Neo4jNetworkDyadDAOIterator();
            Label newLabel = DynamicLabel.label(nameSpace);
            theIterator.newType = DynamicRelationshipType.withName(similarityId);

            Iterator<Node> neo4jNodeList = graphOperations.getAllNodesWithLabel(newLabel).iterator();
            theIterator.nodeIterator = neo4jNodeList;
            if (neo4jNodeList.hasNext()) {
                Node thisNode = neo4jNodeList.next();

                // Set the current node so that the next call starts in the proper place.
                theIterator.currentNode = thisNode;
                Iterator<Relationship>theRels = 
                    thisNode.getRelationships(theIterator.newType, Direction.OUTGOING).iterator();
                theIterator.relationshipIterator = theRels;
            }
        } catch (Exception e) {
            // should send this back using the message logs eventually
            this.logger.log (Level.SEVERE, "exception in getNetworkDyads: " + e.getMessage(), e);
        } finally {
            tx.close();
        }

        return theIterator;
    }
}
