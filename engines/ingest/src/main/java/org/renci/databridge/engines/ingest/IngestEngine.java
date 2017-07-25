package org.renci.databridge.engines.ingest;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.io.FileInputStream;

import org.renci.databridge.util.AMQPMessageListener;
import org.renci.databridge.message.IngestMetadataMessage;
import org.renci.databridge.persistence.metadata.MetadataDAOFactory;

/**
 * Entry point for metadata ingester subystem. 
 * This subsystem ingests metadata (from external sources) and persists it.
 * 
 * @author mrshoffn
 */
public class IngestEngine {

  protected static Logger logger = Logger.getLogger ("org.renci.databridge.engines.ingest");

  /**
   * @param args must contain a path to a properties file that defines:
   * org.renci.databridge.primaryQueue
   * org.renci.databridge.exchange
   * org.renci.databridge.queueHost
   * org.renci.databridge.relevancedb.dbType
   * org.renci.databridge.relevancedb.dbName
   * org.renci.databridge.relevancedb.dbPort
   */
  public static void main (String [] args) throws Exception {

    if (args.length != 1) { 
      throw new RuntimeException ("Usage: IngestEngine <abs_path_to_AMQPComms_props_file");
    }

    String pathToAmqpPropsFile = args [0];
System.out.println ("pathToAmqpPropsFile: " + pathToAmqpPropsFile);
    Properties p = new Properties ();
    p.load (new FileInputStream (pathToAmqpPropsFile));
    String dbTypeProp = p.getProperty ("org.renci.databridge.relevancedb.dbType", "mongo");
    String dbName = p.getProperty ("org.renci.databridge.relevancedb.dbName", "test2");
    String dbHost = p.getProperty ("org.renci.databridge.relevancedb.dbHost", "localhost");
    int dbPort = Integer.parseInt (p.getProperty ("org.renci.databridge.relevancedb.dbPort", "27017"));
    String dbUser = p.getProperty("org.renci.databridge.relevancedb.dbUser", "localhost");
    String dbPwd = p.getProperty("org.renci.databridge.relevancedb.dbPassword", "localhost");

    p.setProperty("org.renci.databridge.primaryQueue",
                  p.getProperty("org.renci.databridge.ingestEngine.primaryQueue"));

    int dbType = -1; 
    if (dbTypeProp.compareToIgnoreCase ("mongo") != 0) {
      throw new RuntimeException ("Unsupported database type: " + dbTypeProp);
    } else {
      dbType = MetadataDAOFactory.MONGODB;
    }

    AMQPMessageListener aml = new AMQPMessageListener (p, new IngestMetadataMessage (), new IngestMetadataAMQPMessageHandler (dbType, dbName, dbHost, dbPort, dbUser, dbPwd, pathToAmqpPropsFile), logger);

    aml.start ();

    p.setProperty("org.renci.databridge.rpcQueue",
                  p.getProperty("org.renci.databridge.ingestEngine.rpcQueue"));

    AMQPMessageListener amlRPC = new AMQPMessageListener (AMQPMessageListener.MODE_RPC, p, new IngestMetadataMessage (), new IngestEngineRPCHandler (dbType, dbName, dbHost, dbPort, dbUser, dbPwd, pathToAmqpPropsFile), logger);

    amlRPC.start ();
    aml.join (); // keeps main thread from exiting
    amlRPC.join (); // keeps thread from exiting

  }

}
