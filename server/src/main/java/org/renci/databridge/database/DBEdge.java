package org.renci.databridge.database;

public class DBEdge{
  
  public int index1;
  public int index2;
  public String label;
  public String dbID;
  public String[][] properties;

  public DBEdge(){}

  public DBEdge(int index1, int index2, String label, String dbID, String[][] properties){
    this.index1 = index1;
    this.index2 = index2;
    this.label = label;
    this.dbID = dbID;
    this.properties = properties;
  }
}
