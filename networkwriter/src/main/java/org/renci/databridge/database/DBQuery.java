package org.renci.databridge.database;

/**
 * Interface class for querying the database.
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */
public interface DBQuery{

  /**
   * Retrieve a node from the database. Note this node's edges attribute will be empty
   * until popNetowrk is called.
   *
   * @param label The type of node to look for e.g. 'dataset' or 'user'.
   * @param dbID The databridge unique ID of the node for which to look.
   *
   * @return The DBNode abstraction of the node found in the dataset.
   */
  public DBNode findNode(String label, String dbID);

  /**
   * Populate the network (fill in the edges and nodes attributes) around a seed node 
   * to a certain depth. This function does not return anything, but fills in the 'edges'
   * attribute of the seed node, and the 'nodes' attribute of each edge, and so on to the
   * given depth, thus the entire network can be access via the seed node.
   *
   * @param dbn The seed node to populate from.
   * @param depth The node depth to which the network should be populate
   *               (1 yields seed-edges-nodes, 2 yields seed-edges-nodes-edges-nodes).
   *
   * @return Whether the population was successful or failed.
   */
  public boolean popNetwork(DBNode dbn, int depth);

  /**
   * Shutdown the database transaction. Most database transactions require a service
   * which must be shut down before another can begin, this function provides means to
   * do so.
   */
  public void shutDown();

}
