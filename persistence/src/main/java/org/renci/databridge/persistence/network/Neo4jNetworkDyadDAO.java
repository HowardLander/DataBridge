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
       private String similarityId;
       private boolean returnSingletons;
       private Node currentNode = null;
       private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.network");
       private RelationshipType newType;

       /**
        * Returns whether or not there is a next item in this iterator
        */
       @Override
       public boolean hasNext() {
           GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
           Transaction tx = theDB.beginTx();
           try {
               return (relationshipIterator.hasNext() || nodeIterator.hasNext());
           } catch (Exception e) {
               // should send this back using the message logs eventually
               this.logger.log (Level.SEVERE, "exception in hasNext function: " + e.getMessage(), e);
           } finally {
               tx.close();
           }
           return false;
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
                      theTransfer.setNode1MetadataId((String)currentNode.getProperty(NetworkNodeDAO.METADATA_NODE_KEY));

                      // The value of the similarityId property is the index of the node in the 
                      // original matrix.
                      theTransfer.setI((int)currentNode.getProperty(similarityId));
                      Relationship theRel = relationshipIterator.next();
                      double simValue = (double)
                             theRel.getProperty(NetworkRelationshipDAO.METADATA_SIMILARITY_PROPERTY_NAME);
                      theTransfer.setSimilarity(simValue);
                      Node theOtherNode = theRel.getOtherNode(currentNode);
                      theTransfer.setNode2DataStoreId(Long.toString(theOtherNode.getId()));
                      theTransfer.setNode2MetadataId((String)theOtherNode.getProperty(NetworkNodeDAO.METADATA_NODE_KEY));
                      theTransfer.setJ((int)theOtherNode.getProperty(similarityId));
                      stillLooking = false;
                  }  else if (nodeIterator.hasNext()) {
                      // All the relationships of the currentNode have been exhausted. If there is
                      // another node, we start with it's relationships.
                      theTransfer = new NetworkDyadTransferObject();
                      Node theNode = nodeIterator.next();
                      currentNode = theNode;
                      theTransfer.setNode1DataStoreId(Long.toString(theNode.getId()));
                      theTransfer.setNode1MetadataId((String)theNode.getProperty(NetworkNodeDAO.METADATA_NODE_KEY));
                      theTransfer.setI((int)theNode.getProperty(similarityId));
                      this.logger.log (Level.INFO, "working on node: " + theTransfer.getI());

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
                          theTransfer.setNode2MetadataId((String)theOtherNode.getProperty(NetworkNodeDAO.METADATA_NODE_KEY));
                          theTransfer.setJ((int)theOtherNode.getProperty(similarityId));
                          stillLooking = false;
                      } else {
                          // The following code handles singletons.  If the user doesn't want them
                          // we skip it all and stillLooking will remain true.
                          if (returnSingletons == true) {
                              // Why are we check for INCOMING relationships? The reason is to see if this
                              // node is truly unrelated to others, or if all it's relationships are INCOMING
                              // and so it wasn't caught in the previous test. We handle these two
                              // cases differently.  If the node is truly unrelated to others, we want to
                              // return it in the next call, in case the caller has any use for unrelated
                              // nodes.  But if it has incoming relationships, than we want to go to the 
                              // next node.
                              this.logger.log (Level.INFO, "no outgoing relationships for node: " + theTransfer.getI());
                              relationshipIterator = 
                                  theNode.getRelationships(newType, Direction.INCOMING).iterator();
                              if (relationshipIterator.hasNext()) {
                                  // this node is connected, we want to skip it.
                                  stillLooking = true;
                                  if (nodeIterator.hasNext() == false) {
                                     // This is the last node, and it's not a singleton, though it might look like
                                     // it is, so we'll set the Node2DataStoreId = Node1DataStoreId
                                     this.logger.log (Level.INFO, "last node has incoming relationship: " + 
                                                      theTransfer.getI());
                                     theTransfer.setNode2DataStoreId(Long.toString(theNode.getId()));
                                     theTransfer.setNode2MetadataId((String)theNode.getProperty(NetworkNodeDAO.METADATA_NODE_KEY));
                                  }
                              } else {
                                  // an unconnected node we want to return
                                  stillLooking = false;
                              } 
                              relationshipIterator = Collections.emptyIterator();
                          }
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
     *  nameSpace and were produced using the given similarityID. This call will return all
     *  the dyads, including singletons.
     *  @param nameSpace The nameSpace in which to search for dyads
     *  @param similarityId The similarity metric to use in the search for dyads
     *  @return the requested iterator
     */
    public Iterator<NetworkDyadTransferObject>
        getNetworkDyads(String nameSpace, String similarityId) {
        return getNetworkDyads(nameSpace, similarityId, true);
    }

    /**
     *  Retrieve an iterator for all the dyads in the network that exist in the given
     *  nameSpace and were produced using the given similarityID. Depending on the value of
     *  the returnSingletons parameter, this call will return all the dyads, potentially including singletons.
     *  @param nameSpace The nameSpace in which to search for dyads
     *  @param similarityId The similarity metric to use in the search for dyads
     *  @param returnSingletons boolean for whether or not the user wants the singleton nodes
     *  @return the requested iterator
     */
    public Iterator<NetworkDyadTransferObject>
        getNetworkDyads(String nameSpace, String similarityId, boolean returnSingletons) {

        Neo4jNetworkDyadDAOIterator theIterator = null;
        GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
        Transaction tx = theDB.beginTx();
        GlobalGraphOperations graphOperations = GlobalGraphOperations.at(theDB);
        try {
            theIterator = new Neo4jNetworkDyadDAOIterator();
            Label newLabel = DynamicLabel.label(nameSpace);
            theIterator.newType = DynamicRelationshipType.withName(similarityId);
            theIterator.similarityId = similarityId;
            theIterator.returnSingletons = returnSingletons;

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

    /**
     *  Retrieve an arrayList for all the singleton dyads in the network that exist in the given
     *  nameSpace and were produced using the given similarityID. 
     *  @param nameSpace The nameSpace in which to search for dyads
     *  @param similarityId The similarity metric to use in the search for dyads
     *  @return the arrayList
     */
    public ArrayList<NetworkDyadTransferObject> getNetworkSingletonArray(String nameSpace, String similarityId) {
        NetworkDyadTransferObject theTransfer = null;
        GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
        Transaction tx = theDB.beginTx();
        GlobalGraphOperations graphOperations = GlobalGraphOperations.at(theDB);

        Iterator<Relationship> relationshipIterator = Collections.emptyIterator();
        Label newLabel = DynamicLabel.label(nameSpace);
        RelationshipType newType = DynamicRelationshipType.withName(similarityId);
        ArrayList<NetworkDyadTransferObject> theList = new ArrayList<NetworkDyadTransferObject>();
        try {
            // Translate the date from the Neo4j representation to the
            // representation presented to the users in the Transfer object.
            Iterator<Node> neo4jNodeList = graphOperations.getAllNodesWithLabel(newLabel).iterator();
            while (neo4jNodeList.hasNext()) {
               Node theNode = neo4jNodeList.next();

               // Check for outgoing relationships
               relationshipIterator = theNode.getRelationships(newType,Direction.OUTGOING).iterator();
               if (relationshipIterator.hasNext()) {
                   continue;
               }
               // Check for incoming relationships
               relationshipIterator = theNode.getRelationships(newType,Direction.INCOMING).iterator();
               if (relationshipIterator.hasNext()) {
                   continue;
               }
               theTransfer = new NetworkDyadTransferObject();
               theTransfer.setNode1DataStoreId(Long.toString(theNode.getId()));
               theTransfer.setNode1MetadataId((String)theNode.getProperty(NetworkNodeDAO.METADATA_NODE_KEY));
               theTransfer.setI((int)theNode.getProperty(similarityId));
               theList.add(theTransfer);
               this.logger.log (Level.INFO, "added singleton node to list: " + theTransfer.getI());
            }
        } catch (Exception e) {
            // should send this back using the message logs eventually
            this.logger.log (Level.SEVERE, "exception in getNetworkSingletons: " + e.getMessage(), e);
        } finally {
            tx.close();
        }
        return theList;
    }

    /**
     *  Retrieve the count of nodes that are not singletons in the specified
     *  nameSpace with the given similarityID
     *  @param nameSpace The nameSpace in which to search for dyads
     *  @param similarityId The similarity metric to use in the search for dyads
     *  @return the number of non singleton nodes
     */
    public long countConnectedNodes(String nameSpace, String similarityId) {

        GraphDatabaseService theDB = Neo4jDAOFactory.getTheNetworkDB();
        Transaction tx = theDB.beginTx();
        GlobalGraphOperations graphOperations = GlobalGraphOperations.at(theDB);
        RelationshipType newType;
        Iterator<Node> nodeIterator = Collections.emptyIterator();
        Iterator<Relationship> relationshipIterator = Collections.emptyIterator();
        long count = 0;
        try {
            Label newLabel = DynamicLabel.label(nameSpace);
            newType = DynamicRelationshipType.withName(similarityId);

            Iterator<Node> neo4jNodeList = graphOperations.getAllNodesWithLabel(newLabel).iterator();
            nodeIterator = neo4jNodeList;
            while (neo4jNodeList.hasNext()) {
                Node theNode = neo4jNodeList.next();
                relationshipIterator = theNode.getRelationships(newType,Direction.OUTGOING).iterator();
                if (relationshipIterator.hasNext()) {
                    count ++;
                } else {
                    relationshipIterator = theNode.getRelationships(newType,Direction.INCOMING).iterator();
                    if (relationshipIterator.hasNext()) {
                        count ++;
                    }
                }
            }
        } catch (Exception e) {
            // should send this back using the message logs eventually
            this.logger.log (Level.SEVERE, "exception in getNetworkSingletons: " + e.getMessage(), e);
        } finally {
            tx.close();
        }

        return count;
    }
}
