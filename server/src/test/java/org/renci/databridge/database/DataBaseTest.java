
package org.renci.databridge.database.tests;

import org.renci.databridge.database.*;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.matchers.JUnitMatchers;
import org.junit.Rule;

public class DataBaseTest{

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testDataBaseWrites(){
   
    System.out.println("Testing DBWriters");

    DBNode node = new DBNode(0, "test", "test-1", new String[][]{{"prop1", "test"}, {"prop2", "test"}});
    DBNode node2 = new DBNode(1, "test", "test-2", null);
    DBEdge edge = new DBEdge(0, 1, "test", "test-e", new String[][]{{"prop1", "test"}});
    
    System.out.println("Testing Writes");

    write(node, node2, edge);

    node = new DBNode(0, "test", "test-1", new String[][]{{"prop1", "updated"}});
    node2 = new DBNode(1, "test", "test-2", new String[][]{{"prop1", "updated"}});
    edge = new DBEdge(0, 1, "test", "test-e", new String[][]{{"prop1", "updated"}});

    System.out.println("Testing Updates");

    write(node, node2, edge);

    testDataBaseQueries();
  }

  public void write(DBNode node, DBNode node2, DBEdge edge){

    GraphDatabaseService neoDB = new GraphDatabaseFactory().newEmbeddedDatabase("data/test/neo4j");
    TitanGraph titanDB = TitanFactory.open("data/test/titanHB");

    DBWriter neo = new DBWriterNeo4j(neoDB);
    DBWriter titan = new DBWriterTitanHB(titanDB);

    int ret = -1;
    ret = neo.writeNode(node);
    TestCase.assertTrue("Neo4j write node 1 failed", ret == 0);
    ret = neo.writeNode(node2);
    TestCase.assertTrue("Neo4j write node 2 failed", ret == 0);
    ret = neo.writeEdge(edge);
    TestCase.assertTrue("Neo4j write edge failed", ret == 0);
    ret = titan.writeNode(node);
    TestCase.assertTrue("TitanHB write node 1 failed", ret == 0);
    ret = titan.writeNode(node2);
    TestCase.assertTrue("TitanHB write node 2 failed", ret == 0);
    ret = titan.writeEdge(edge);
    TestCase.assertTrue("TitanHB write edge failed", ret == 0);

    neo.shutDown();
    titan.shutDown();

  }

  public void testDataBaseQueries(){

    System.out.println("Testing Queries");

    DBQuery neoq = new DBQueryNeo4j("data/test/neo4j");

    //System.out.println("DBQ initialized");
  try{

    //System.out.println("Searching for node");
    DBNode node = neoq.findNode("test", "test-1");
    //System.out.println("Found node with DBid " + node.dbID);
    neoq.popNetwork(node, 3);
    //System.out.println("Traversing network:");
    //printNetwork(node, 3, 0);
    //System.out.println("Done traversing");
    neoq.shutDown();
  } catch(Exception e){
    neoq.shutDown();
    e.printStackTrace();
  }

  }

  private void printNetwork(DBNode node, int d, int indent){
    if(d == 0) return;
    println(indent++, "Node " + node.dbID + " (Index " + node.index + ")");
    if(node.edges != null)
    for(DBEdge e : node.edges){
      println(indent, "Edge " + e.dbID);
      if(e.nodes != null)
      for(DBNode n : e.nodes){
	printNetwork(n, d - 1, indent + 1);
      }
    }
  }

  private void println(int indent, String msg){
    for(int i = 0; i < indent; i++){
      System.out.print("  ");
    }
    System.out.println(msg);
  }
  
}
