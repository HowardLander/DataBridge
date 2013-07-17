package org.renci.databridge.database;

/**
 * Basic class for passing node information to DBWriters
 *
 * @author Ren Bauer
 */
public class DBNode{

  /** instance-specific index for later referencing */
  public int index;

  /** Type of node */
  public String label;
 
  /** Database-external databridge-specific identifier */
  public String dbID;
  
  /** Additional properties associated with node */
  public String[][] properties;

  /** Default constructor so msgpack (I think) is happy */
  public DBNode(){}

  public DBNode(int index, String label, String dbID, String[][] properties){
    this.index = index;
    this.label = label;
    this.dbID = dbID;
    this.properties = properties;
  }

}
