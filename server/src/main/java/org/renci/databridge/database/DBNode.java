package org.renci.databridge.database;

/**
 * Basic class for passing node information to DBWriters
 *
 * @author Ren Bauer
 */
public class DBNode extends DBObject{

  /** Instance-specific index for later referencing */
  public int index;

  /** Array to store edges connected to this node */
  public DBEdge[] edges;

  /** Default constructor */
  public DBNode(){
    this.type = DBObject.NODE;
  }

  /**
   * Constructor taking arguments for index, label, dbID, and properties.
   *
   * @param index Instance-specific index for later referencing. This index is not stored
   *               in the database and is meant only for short-term use.
   * @param label The type of node e.g. 'dataset', 'user', or 'cluster'
   * @param dbID The databridge-specific Identifier, unique amongst all nodes
   * @param properties Additional properties to associate with this node
   */
  public DBNode(int index, String label, String dbID, String[][] properties){
    this();
    this.index = index;
    this.label = label;
    this.dbID = dbID;
    this.properties = properties;
  }

}
