package org.renci.databridge.database;

/**
 * Abstract class to provide interfacing for Nodes and Edges
 * so dbID, label, and properties can be accessed dynamically
 *
 * @author Ren Bauer
 */
public abstract class DBObject{

  /** Static type value for nodes */
  public static final int NODE = 1;

  /** Static type value for edges */
  public static final int EDGE = 2;

  /** Type value to distinguish DBObject subclasses */
  public int type = 0;

  /** The label for the object */
  public String label;

  /** A database-external databridge-specific identifier */
  public String dbID;

  /** Any additional properties to associate with the object */
  public String[][] properties;

}
