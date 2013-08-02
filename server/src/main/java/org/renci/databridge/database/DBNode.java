package org.renci.databridge.database;

/**
 * Basic class for passing node information to DBWriters
 *
 * @author Ren Bauer
 */
public class DBNode extends DBObject{

  /** instance-specific index for later referencing */
  public int index;

  public DBEdge[] edges;

  /** Default constructor */
  public DBNode(){
    this.type = DBObject.NODE;
  }

  public DBNode(int index, String label, String dbID, String[][] properties){
    this();
    this.index = index;
    this.label = label;
    this.dbID = dbID;
    this.properties = properties;
  }

}
