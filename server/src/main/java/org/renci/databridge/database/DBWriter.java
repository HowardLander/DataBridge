package org.renci.databridge.database;

import java.util.*;

public abstract class DBWriter{

  List nodes;
  
  public abstract int writeNode(DBNode n);

  public abstract int writeEdge(DBEdge e);

  public abstract void shutDown();

  public void ensureRoom(int index){
    while(nodes.size() < index)
      nodes.add(null);
  }

}
