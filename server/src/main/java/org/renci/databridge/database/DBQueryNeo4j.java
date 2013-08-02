package org.renci.databridge.database;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.*;
import org.neo4j.cypher.javacompat.*;
import java.util.*;

public class DBQueryNeo4j implements DBQuery{

  ArrayList<DBNode> DBNodes;
  ArrayList<Long> IDs;
  ArrayList<Node> nodes;

  GraphDatabaseService graphDB;

  public DBQueryNeo4j(){
    this("data/neo4j");
  }

  public DBQueryNeo4j(String path){
    graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(path);
    DBNodes = new ArrayList<DBNode>();
    IDs = new ArrayList<Long>();
    nodes = new ArrayList<Node>();
  }

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

  private DBEdge makeDBEdge(Relationship r){
    Node[] nodes = r.getNodes();
    DBNode[] dbnodes = new DBNode[]{makeDBNode(nodes[0]), makeDBNode(nodes[1])};
    DBEdge out = new DBEdge(dbnodes, r.getType().name(), r.getProperty("dbID").toString(), makeProps(r));
    return out;
  }

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

  public boolean popNetwork(DBNode dbn, int depth){
    if(depth <= 0)return true;
    System.out.println("Populating network of node " + dbn.dbID + " to depth " + depth);
    popEdges(dbn);
    for(DBEdge e: dbn.edges){
      popNetwork(e.nodes[0], depth - 1);
      popNetwork(e.nodes[1], depth - 1);
    }
    return true;
  }

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
 
  private int iterableSize(Iterable i){
    Iterator<Object> ir = i.iterator();
    int o = 0;
    while(ir.hasNext()){
      ir.next();
      o++;
    }
    return o;
  }

  public void shutDown(){
    graphDB.shutdown();
  }
}
