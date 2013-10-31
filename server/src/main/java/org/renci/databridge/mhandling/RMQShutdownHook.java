package org.renci.databridge.mhandling;

import java.lang.Thread;
import org.neo4j.graphdb.GraphDatabaseService;
import com.thinkaurelius.titan.core.TitanGraph;

public class RMQShutdownHook<T> extends Thread{
 
  private T graphDB;

  public RMQShutdownHook(T graphDB){
    this.graphDB = graphDB;
  }

  public void run(){
    if(graphDB instanceof GraphDatabaseService)
      ((GraphDatabaseService) graphDB).shutdown();
    else if(graphDB instanceof TitanGraph)
      ((TitanGraph) graphDB).shutdown();
  }
}
