package org.renci.databridge.database;

/**
 * Basic class for passing node edge information to DBWriters
 *
 * @author Ren Bauer
 */
 
public class DBEdge{
  
  /** Index of 1st node */
  public int index1;

  /** Index of 1st node */
  public int index2;

  /** The type of edge */
  public String label;
  
  /** A database-external databridge-specific identifier */
  public String dbID;
  
  /** Any additional properties to associate with the edge */
  public String[][] properties;

  /** Default constructor so msgpack (I think) is happy */
  public DBEdge(){}

  public DBEdge(int index1, int index2, String label, String dbID, String[][] properties){
    this.index1 = index1;
    this.index2 = index2;
    this.label = label;
    this.dbID = dbID;
    this.properties = properties;
  }
}
