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

  /**
   * Add a property to the DBObject
   *
   * @param key The key of the property to add
   * @param val The value of the property to add
   */
  public void addProperty(String key, String val){
    String[][] newProps = new String[properties.length + 1][2];
    for(int i = 0; i < properties.length; i++)
      newProps[i] = properties[i];
    newProps[properties.length] = new String[]{key, val};
    properties = newProps;
  }

  /**
   * Remove a property from the DBObject
   *
   * @param key The key of the property to remove
   *
   * @return Whether or not a property of the key was found
   */
  public boolean removeProperty(String key){
    boolean found = false;
    for(String[] prop : properties){
      if(prop[0].equals(key)){
        found = true;
        break;
      }
    }
    if(found){
      String[][] newProps = new String[properties.length + 1][2];
      int i = 0;
      for(String[] prop : properties){
        if(!prop[0].equals(key))
          newProps[i++] = prop;
      }
      properties = newProps;
    }
    return found;
  }
}
