package org.renci.databridge.database;

public abstract class DBObject{

  public static final int NODE = 1;
  public static final int EDGE = 2;
  public int type = 0;

  /** The label for the object */
  public String label;

  /** A database-external databridge-specific identifier */
  public String dbID;

  /** Any additional properties to associate with the object */
  public String[][] properties;

}
