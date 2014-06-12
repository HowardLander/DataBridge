package org.renci.databridge.database;

import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.configuration.*;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.util.*;
import org.apache.commons.configuration.*;
import org.renci.databridge.util.AMQPLogger;
import java.util.*;

/**
 * DBWriter speicific to an HBase-based Titan database:
 * Creates key indexes for 'lbl' and 'dbID' for fast querying
 * label is stored in 'lbl' property and in nodes, specifies edge
 * type in edges
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */

public class DBWriterTitanHB extends DBWriter {

  TitanGraph graph;

  public DBWriterTitanHB(TitanGraph graph){
    this.graph = graph;
    nodes = new ArrayList<Vertex>();
  }

  public int writeNode(DBNode n, AMQPLogger logger){
    int status = -1;
    Vertex v;

    Iterator<Vertex> candidates = graph.getVertices("dbID", n.dbID).iterator();
    if(candidates.hasNext()){
      v = candidates.next();
    }
    else {
      v = graph.addVertex(null);
      v.setProperty("lbl", n.label);
      v.setProperty("dbID", n.dbID);
    }

    if(n.properties != null){
      for(String[] prop : n.properties)
        v.setProperty(prop[0], prop[1]);
    }

    nodes.ensureCapacity(n.index);
    nodes.add(n.index, v);
    graph.commit();
    status = 0;

    return status;
  }

  public int writeEdge(DBEdge e){

    int status = -1;
    Vertex v1 = (Vertex) nodes.get(e.index1);
    Vertex v2 = (Vertex) nodes.get(e.index2);
    Edge edge = null;

    Iterable<Edge> candidates = v1.getEdges(Direction.BOTH, e.label);
    for(Edge e2 : candidates){
      if(e2.getProperty("dbID") == e.dbID && (e2.getVertex(Direction.IN).getId() == v2.getId() || e2.getVertex(Direction.OUT).getId() == v2.getId()))
        edge = e2;
    }
    if(edge == null){
      edge = graph.addEdge(null, v1, v2, e.label);
    }

    for(String[] prop : e.properties)
      edge.setProperty(prop[0], prop[1]);
 
    graph.commit();
    status = 0;

    return status;
  }

  public void shutDown(){
    graph.shutdown();
  }

}
