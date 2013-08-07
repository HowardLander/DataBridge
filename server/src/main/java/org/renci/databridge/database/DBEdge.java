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

  /** Array of nodes at either end of the edge */
  public DBNode[] nodes;

  /** Default constructor */
  public DBEdge(){
    this.type = DBObject.EDGE;
  }

  /**
   * Constructor taking arguments for nodes, label, databridge unique ID, and properties.
   * Note this constructor automatically fills in the index attributes.
   *
   * @param nodes The nodes this edge connects in the form DBNode[]
   * @param label The label to denote the type of edge e.g. 'distance' or 'friends'
   * @param dbID The databridge ID for this edge (unique amongst edges with same nodes)
   * @param properties Additional properties to associate with this edge
   *
   */
  public DBEdge(DBNode[] nodes, String label, String dbID, String[][] properties){
    this(nodes[0].index, nodes[1].index, label, dbID, properties);
    this.nodes = nodes;
  }

  /**
   * Constructor taking arguments for indices, label, databridge unique ID, and properties.
   * Note this constructor does NOT automatically fill in the nodes attribute.
   *
   * @param index1 The index of the first node (per the DBNode's index attribute)
   * @param index2 The index of the second node (per the DBNode's index attribute)
   * @param label The label to denote the type of edge e.g. 'distance' or 'friends'
   * @param dbID The databridge ID for this edge (unique amongst edges with same nodes)
   * @param properties Additional properties to associate with this edge
   *
   */
  public DBEdge(int index1, int index2, String label, String dbID, String[][] properties){
    this();
    this.index1 = index1;
    this.index2 = index2;
    this.label = label;
    this.dbID = dbID;
    this.properties = properties;
  }

 /**
  * Convenience method to find the other node of a given edge. Note this method
  * will return a node with label "ERROR" if the nodes array is of an improper state
  *
  * @param n The known node to which this edge connects
  *
  * @return The other node to which this edge connects (or error)
  */
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
