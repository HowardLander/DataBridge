package org.renci.databridge.engines.network;

import java.io.*;
import java.util.*;

import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.persistence.network.*;
import org.renci.databridge.mhandling.*;
import org.renci.databridge.util.*;
import org.renci.databridge.message.*;
import org.la4j.*;
import org.la4j.matrix.functor.*;
import  org.neo4j.graphdb.*;
import  org.neo4j.tooling.*;
import  org.neo4j.graphdb.factory.*;
import  org.neo4j.graphdb.traversal.*;
import  org.neo4j.cypher.javacompat.*;

// This is just a diagnostic code so we can look at the network version of the
// similarity matrix and do a sanity check.
public class PrintMatrix {
  public static void main(String [] args) {

  class MyClass implements MatrixProcedure {
     String extraString = "Print me!";

     public void apply(int i, int j, double value) {
        System.out.println("Hello from apply " + i + " " + j + " " + value);
        // System.out.println("Hello from apply ");
   //     System.out.println("extra string: " + PrintSimilarityFile.extraString);
     }
  }


     System.out.println("\nProducing matrix for nameSpace " + args[0]);
     System.out.println("\nProducing matrix for similarity " + args[1]);
     System.out.println("\nusing database " + args[2]);

     GraphDatabaseService theDB = null;
     Transaction tx = null;

     try {
        org.la4j.matrix.sparse.CRSMatrix theMatrix = new org.la4j.matrix.sparse.CRSMatrix();
    
        // Populate the matrix from the neo4j database
        NetworkDAOFactory theNeo4jFactory = NetworkDAOFactory.getNetworkDAOFactory(NetworkDAOFactory.NEO4JDB, args[2]);
        if (null == theNeo4jFactory) {
           System.out.println("Couldn't produce the NetworkDAOFactory");
           return;
        } 
        theDB = Neo4jDAOFactory.getTheNetworkDB();
        GlobalGraphOperations graphOperations = GlobalGraphOperations.at(theDB);
        tx = theDB.beginTx();

        NetworkNodeDAO theNetworkNodeDAO = theNeo4jFactory.getNetworkNodeDAO();
        NetworkRelationshipDAO theNetworkRelationshipDAO = theNeo4jFactory.getNetworkRelationshipDAO();
        NetworkDyadDAO theNetworkDyadDAO = theNeo4jFactory.getNetworkDyadDAO();

        Iterator<NetworkNodeTransferObject> nodeList = 
            theNetworkNodeDAO.getNetworkNodesForNameSpace(args[0]);
        NetworkNodeTransferObject theNode = null;
        Node theRootNode = null;
        if (nodeList.hasNext()) {
           theNode = nodeList.next();
           long nodeId = Long.valueOf(theNode.getDataStoreId()).longValue();
           theRootNode = theDB.getNodeById(nodeId);
        }
        
        RelationshipType newType = DynamicRelationshipType.withName(args[1]);
        TraversalDescription theTraversal = theDB.traversalDescription();
        theTraversal = theTraversal.breadthFirst();
        theTraversal = theTraversal.relationships(newType, Direction.BOTH);
        theTraversal = theTraversal.evaluator(Evaluators.toDepth(100));

        //for (Relationship position : theTraversal.traverse(theRootNode).relationships()) {
        for (Path position : theTraversal.traverse(theRootNode)) {
            System.out.println(position);
        }

        // Now let's try another strategy
        Label newLabel = DynamicLabel.label(args[0]);
        Iterator<Node> neo4jNodeList = graphOperations.getAllNodesWithLabel(newLabel).iterator();
        while (neo4jNodeList.hasNext()) {
           Node thisNode = neo4jNodeList.next();
           Iterator<Relationship>theRels = thisNode.getRelationships(newType, Direction.OUTGOING).iterator();
           while (theRels.hasNext()) {
              Relationship thisRel = theRels.next();
              System.out.println(thisRel);
           }
        }

        // Here's the third strategy...
        System.out.println("Printing dyads: ");
        Iterator<NetworkDyadTransferObject> theDyads = theNetworkDyadDAO.getNetworkDyads(args[0], args[1]);
        while (theDyads.hasNext()) {
            System.out.println(theDyads.next());
        }
      

     }  catch (Exception e) {
         e.printStackTrace();
     } finally {
         tx.close();
     }
  }
}
