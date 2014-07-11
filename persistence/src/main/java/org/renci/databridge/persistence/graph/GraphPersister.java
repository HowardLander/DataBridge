package org.renci.databridge.persistence.graph;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.io.FileInputStream;

import org.renci.databridge.util.AMQPMessageListener;
import org.renci.databridge.message.GraphComputedMessage;

/**
 * Entry point for graph persistence subystem. 
 * This subsystem reads and writes DataBridge network graphs.
 * 
 * @author mrshoffn
 */
public class GraphPersister {

  protected static Logger logger = Logger.getLogger ("org.renci.databridge.persistence.graph");

  /**
   * @param args must contain a path to a properties file that defines:
   * org.renci.databridge.primaryQueue
   * org.renci.databridge.exchange
   * org.renci.databridge.queueHost
   */
  public static void main (String [] args) throws Exception {

    if (args.length != 1) { 
      throw new RuntimeException ("Usage: GraphPersister <abs path to AMQPComms properties file");
    }

    AMQPMessageListener aml = new AMQPMessageListener (args [0], new GraphComputedMessage (), new GraphComputedAMQPMessageHandler (), logger);

    GraphPersister gp = new GraphPersister ();
    aml.start ();
    aml.join (); // keeps main thread from exiting

  }

}
