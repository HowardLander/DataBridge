package org.renci.databridge.database;

/**
 * Basic class for passing node edge information to DBWriters
 *
 * @author Ren Bauer
 */
 
public class DBEdge extends DBObject{
  
  /** Index of 1st node */
  public int index1;

  /** Index of 1st node */
  public int index2;

  public DBNode[] nodes;

  /** Default constructor */
  public DBEdge(){
    this.type = DBObject.EDGE;
  }

  public DBEdge(DBNode[] nodes, String label, String dbID, String[][] properties){
    this(nodes[0].index, nodes[1].index, label, dbID, properties);
    this.nodes = nodes;
  }

  public DBEdge(int index1, int index2, String label, String dbID, String[][] properties){
    this();
    this.index1 = index1;
    this.index2 = index2;
    this.label = label;
    this.dbID = dbID;
    this.properties = properties;
  }

  public DBNode otherNode(DBNode n){
    if(nodes == null)
      return new DBNode(-1, "ERROR", "Edge's nodes not established", null);
    else if(nodes.length != 2)
      return new DBNode(-1, "ERROR", "Edge has invalid number of nodes: " + nodes.length, null);
    else if(n.index == index1)
      return nodes[1];
    else if(n.index == index2)
      return nodes[0];
    return new DBNode(-1, "ERROR", "Node matching paramater not found", null);
  }
}
