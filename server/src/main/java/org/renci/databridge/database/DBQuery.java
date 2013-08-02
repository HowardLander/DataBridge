package org.renci.databridge.database;

public interface DBQuery{

  public DBNode findNode(String label, String dbID);

  public boolean popNetwork(DBNode dbn, int depth);

  //public DBEdge findEdge(String label, String dbID);

  //public DBObject[] query(String query);

  public void shutDown();

}
