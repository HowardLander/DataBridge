package org.renci.databridge.database;

import java.util.*;
import org.renci.databridge.util.AMQPLogger;

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
  ArrayList nodes;
  
  /**
   * Write a node to the database: If a node with the same label and dbID
   * already exists, any properties in the parameter node will be appended or 
   * overwritten to the values provided in the parameter
   *
   * @param DBNode The Node to be written (inserted and/or updated)
   * @param logger An instance of the AMQPLogger class
   *
   * @return The completion status of the operation (-1 failure, 0 success)
   */
  public abstract int writeNode(DBNode n, AMQPLogger logger);

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

}
