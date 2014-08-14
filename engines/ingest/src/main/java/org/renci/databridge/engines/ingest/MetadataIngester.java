package org.renci.databridge.engines.ingest;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.io.FileInputStream;

import org.renci.databridge.util.AMQPMessageListener;
import org.renci.databridge.message.IngestMetadataMessage;

/**
 * Entry point for metadata ingester subystem. 
 * This subsystem ingests metadata (from external sources) and persists it.
 * 
 * @author mrshoffn
 */
public class MetadataIngester {

  protected static Logger logger = Logger.getLogger ("org.renci.databridge.engines.ingest");

  /**
   * @param args must contain a path to a properties file that defines:
   * org.renci.databridge.primaryQueue
   * org.renci.databridge.exchange
   * org.renci.databridge.queueHost
   */
  public static void main (String [] args) throws Exception {

    if (args.length != 1) { 
      throw new RuntimeException ("Usage: MetadataIngester <abs_path_to_AMQPComms_props_file");
    }

    AMQPMessageListener aml = new AMQPMessageListener (args [0], new IngestMetadataMessage (), new IngestMetadataAMQPMessageHandler (), logger);

    aml.start ();
    aml.join (); // keeps main thread from exiting

  }

}
