package org.renci.databridge.database;

public class DBNode{

  public int index;
  public String label;
  public String dbID;
  public String[][] properties;

  public DBNode(){}

  public DBNode(int index, String label, String dbID, String[][] properties){
    this.index = index;
    this.label = label;
    this.dbID = dbID;
    this.properties = properties;
  }

}
