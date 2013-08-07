package org.renci.databridge.database;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.*;
import org.neo4j.cypher.javacompat.*;
import java.util.*;

/**
 * DBQuery implementation for the Neo4j database
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */
public class DBQueryNeo4j implements DBQuery{

  /** 3 partner arrays to store the nodes retrived through this DBQuerier, their
      IDs, and the DBNodes created from them all at the same index. */
  ArrayList<DBNode> DBNodes;
  ArrayList<Long> IDs;
  ArrayList<Node> nodes;

  /** The Neo4j database service to provide reponses to queries */
  GraphDatabaseService graphDB;

  /**
   * Default constructor that sets path to 'data/neo4j'.
   */
  public DBQueryNeo4j(){
    this("data/neo4j");
  }

  /**
   * Constructor taking the path of the database we want to query.
   *
   * @param path The path of the database of interest.
   */
  public DBQueryNeo4j(String path){
    graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(path);
    DBNodes = new ArrayList<DBNode>();
    IDs = new ArrayList<Long>();
    nodes = new ArrayList<Node>();
  }

  /**
   * Retrieve a node from the database. Note this node's edges attribute will be empty
   * until popNetowrk is called. Note this will return a node with label 'ERROR' if 
   * a problem is encountered.
   *
   * @param label The type of node to look for e.g. 'dataset' or 'user'.
   * @param dbID The databridge unique ID of the node for which to look.
   *
   * @return The DBNode abstraction of the node found in the dataset.
   */
  public DBNode findNode(String labelstr, String dbID){
    int status = -1;
    Label label = DynamicLabel.label(labelstr);
    Transaction tx = graphDB.beginTx();
    Node n = null;
    try{
      Iterator<Node> candidates = graphDB.findNodesByLabelAndProperty(label, "dbID", dbID).iterator();
      if(candidates.hasNext()){
	n = candidates.next();
	status = 0;
      }
      if(candidates.hasNext()){
        n = null;
	status = 1;
      }
      tx.success();
    } finally {
      tx.finish();
    }

    switch(status){
      case -1:
        return new DBNode(-1, "ERROR", "Node not found", null);
      case 0:
	return makeDBNode(n);
      case 1:
        return new DBNode(-1, "ERROR", "Multiple matches found", null);
    }
    return new DBNode(-1, "ERROR", "Unrecognized status code", null);
  }

  /**
   * Make a DBNode from a Neo4j Node
   *
   * @param n The Neo4j Node to convert
   *
   * @return The DBNode abstraction of the node
   */
  private DBNode makeDBNode(Node n){
    int index;
    if(!IDs.contains(n.getId())){
      IDs.add(n.getId());
      DBNodes.add(null);
      nodes.add(null);
    }
    index = IDs.indexOf(n.getId());
    String label = n.getLabels().iterator().next().name();
    DBNode out = new DBNode(index, label, n.getProperty("dbID").toString(), null);
    out.properties = makeProps(n);
    DBNodes.add(index, out);
    nodes.add(index, n);
    return out;
  }

  /**
   * Make a DBEdge from a Neo4j Relationship
   *
   * @param r The Neo4j Relationship to convert
   *
   * @return The DBEdge abstraction of the Relationship
   */
  private DBEdge makeDBEdge(Relationship r){
    Node[] nodes = r.getNodes();
    DBNode[] dbnodes = new DBNode[]{makeDBNode(nodes[0]), makeDBNode(nodes[1])};
    DBEdge out = new DBEdge(dbnodes, r.getType().name(), r.getProperty("dbID").toString(), makeProps(r));
    return out;
  }

  /**
   * Make a properties array of type String[][] from a Neo4j Node or Relationship. This
   * function will copy all properties from the parameter except 'dbID', which is handled
   * separately, into a String[][] and return them.
   *
   * @param o The PropertyContainer from which properties are to be copied
   *
   * @return The String[][] containing all properties except 'dbID'
   */
  private String[][] makeProps(PropertyContainer o){
    int numOfProps = -1 + iterableSize(o.getPropertyKeys()); //start at -1 to ignore 'dbID'
    if(numOfProps > 0){
      String[][] props = new String[numOfProps][];
      Iterator<String> propIterator = o.getPropertyKeys().iterator();
      int i = 0;
      while(propIterator.hasNext()){
        String key = propIterator.next();
        if(!key.equals("dbID")){
          props[i++] = new String[]{key, o.getProperty(key).toString()};
        }
      }
      return props;
    }
    return null;
  }

  /**
   * Populate the network (fill in the edges and nodes attributes) around a seed node 
   * to a certain depth. This function does not return anything, but fills in the 'edges'
   * attribute of the seed node, and the 'nodes' attribute of each edge, and so on to the
   * given depth, thus the entire network can be access via the seed node.
   *
   * @param dbn The seed node to populate from.
   * @param depth The node depth to which the network should be populate
   *               (1 yields seed-edges-nodes, 2 yields seed-edges-nodes-edges-nodes).
   *
   * @return Whether the population was successful or failed.
   */
  public boolean popNetwork(DBNode dbn, int depth){
    if(depth <= 0)return true;
    popEdges(dbn);
    for(DBEdge e: dbn.edges){
      popNetwork(e.nodes[0], depth - 1);
      popNetwork(e.nodes[1], depth - 1);
    }
    return true;
  }

  /**
   * Populate the edges field of a DBNode.
   *
   * @param dbn The DBNode for which edges are to be populated
   *
   * @return Whether the population was successful or failed.
   */
  private boolean popEdges(DBNode dbn){
    if(dbn.edges != null) return true;
    Node n = nodes.get(dbn.index);
    int edges = iterableSize(n.getRelationships());
    dbn.edges = new DBEdge[edges];
    Iterable<Relationship> rItable = n.getRelationships();
    Iterator<Relationship> rIter = rItable.iterator();
    int e = 0;
    while(rIter.hasNext()){
      dbn.edges[e++] = makeDBEdge(rIter.next());
    }
    return true;
  }
 
  /**
   * Find the number of elements in an iterable.
   *
   * @param i The iterable that is to have it's elements counted.
   *
   * @return The number of elements in the iterable.
   */
  private int iterableSize(Iterable i){
    Iterator<Object> ir = i.iterator();
    int o = 0;
    while(ir.hasNext()){
      ir.next();
      o++;
    }
    return o;
  }

  /**
   * Shutdown the database transaction. Neo4j database transactions require a service
   * which must be shut down before another can begin, this function does so.
   */
  public void shutDown(){
    graphDB.shutdown();
  }
}
