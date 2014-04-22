package org.renci.databridge.database;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.*;
import java.util.*;
import org.renci.databridge.util.AMQPLogger;

/**
 * DBWriter specific for a neo4j database:
 * Uses 'label' value in nodes and edges as labels and types
 * for nodes and edges, respectively
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */
public class DBWriterNeo4j extends DBWriter{

  GraphDatabaseService graphDb;

  public DBWriterNeo4j(GraphDatabaseService graphDb){
    nodes = new ArrayList<Node>();
    this.graphDb = graphDb;
  }

  public int writeNode(DBNode n, AMQPLogger logger){
    int status = -1;
    Node node;
    Label label = DynamicLabel.label(n.label);
    Transaction tx = graphDb.beginTx();
    try{
      Iterator<Node> candidates = graphDb.findNodesByLabelAndProperty(label, "dbID", n.dbID).iterator();
      if(candidates.hasNext()){
        logger.publish("Found node for dbID " + n.dbID);
        node = candidates.next();
      }
      else{
        logger.publish("no node found, will create using label " + n.label);
        node = graphDb.createNode();
        node.addLabel(label);
        node.setProperty("dbID", n.dbID);
      }
        
      if(n.properties != null){
        for(String[] prop : n.properties)
          node.setProperty(prop[0], prop[1]);
      }

      ensureRoom(n.index);
      nodes.add(n.index, node);
      tx.success();
      status = 0;
    } finally {
      tx.close();
    }

    return status;
  }

  public int writeEdge(DBEdge e){
    int status = -1;
    Node node1 = (Node) nodes.get(e.index1);
    Node node2 = (Node) nodes.get(e.index2);
    RelationshipType relType = DynamicRelationshipType.withName(e.label);
    Relationship relationship = null;
    Transaction tx = graphDb.beginTx();
    try{
      Iterable<Relationship> candidates = node1.getRelationships(relType);
      for(Relationship rel : candidates){
        if(rel.hasProperty("dbID") && rel.getProperty("dbID").equals(e.dbID) && rel.getOtherNode(node1).getId() == node2.getId())
          relationship = rel;
      }
      if(relationship == null){
        relationship = node1.createRelationshipTo(node2, relType);
	relationship.setProperty("dbID", e.dbID);
      }

      for(String[] prop : e.properties)
        relationship.setProperty(prop[0], prop[1]);
      
      tx.success();
      status = 0;
    } finally {
      tx.close();
    }

    return status;
  }

  public void shutDown(){
    graphDb.shutdown();
  }

}
