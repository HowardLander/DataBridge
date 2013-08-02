package org.renci.databridge.database;

import java.util.*;

/**
 * Abstract class to provide interface for database to provide easy
 * add operations, note: both nodes must be written before an edge
 * between them can be written
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */
public abstract class DBWriter{

  /**
   * Maintain a list of nodes added by this writer for easy reference
   * (Very useful when adding edges between new nodes)
   */
  List nodes;

  public int write(DBObject o){
    if(o.type == DBObject.NODE){
      return writeNode((DBNode) o);
    }
    else if(o.type == DBObject.EDGE){
      return writeEdge((DBEdge) o);
    }
    return -1;
  }
  
  /**
   * Write a node to the database: If a node with the same label and dbID
   * already exists, any properties in the parameter node will be appended or 
   * overwritten to the values provided in the parameter
   *
   * @param DBNode The Node to be written (inserted and/or updated)
   *
   * @return The completion status of the operation (-1 failure, 0 success)
   */
  public abstract int writeNode(DBNode n);

  /**
   * Write an edge to the database: If a edge with the same label and dbID
   * already exists, any properties in the parameter edge will be appended or 
   * overwritten to the values provided in the parameter
   *
   * @param DBEdge The Edge to be written (inserted and/or updated)
   *
   * @return The completion status of the operation (-1 failure, 0 success)
   */
  public abstract int writeEdge(DBEdge e);

  /**
   * Shut down the database transaction instance
   */
  public abstract void shutDown();

  /**
   * Ensure the nodes list is long enough to include the current node: 
   * nodes are provided with an index, so appending to the end of the list
   * may cause issues if nodes are provided out of order
   */
  public void ensureRoom(int index){
    while(nodes.size() < index)
      nodes.add(null);
  }

}
