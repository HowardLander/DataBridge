package org.renci.databridge.engines.relevance;

import org.renci.databridge.util.*;
import org.renci.databridge.persistence.metadata.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * A thread that listens to AMQP for messages based on specified headers and 
 * dispatches them to a handler as they arrive.
 *
 * @author mrshoffn
 */
public class RelevanceEngineMessageListener extends Thread {

  protected static final long LISTENER_TIMEOUT_MS = 1000;

  protected AMQPComms amqpComms;
  protected AMQPMessageType amqpMessageType;
  protected AMQPMessageHandler amqpMessageHandler;
  protected Logger logger;
  protected String pathToPropsFile = null;

  /**
   * @param pathToPropsFile properties file for AMQPComms object initialization.
   * @param amqpMessageType
   * @param amqpMessageHandler
   * @param logger can be null.
   */
  public RelevanceEngineMessageListener (String pathToPropsFile, 
                                         AMQPMessageType amqpMessageType, 
                                         AMQPMessageHandler amqpMessageHandler, 
                                         Logger logger) throws IOException {

    this.amqpMessageType = amqpMessageType;
    this.amqpMessageHandler = amqpMessageHandler;
    this.logger = logger;

    // creating AMQPComms here becausec passing it in would enable reusing
    // the same AMQPComms instance, which is not safe across multiple clients
    this.amqpComms = new AMQPComms (pathToPropsFile);
    this.pathToPropsFile = pathToPropsFile;

  }

  protected volatile boolean terminate;
 
  /**
   * Call this to tell the listener to stop. The listener will stop in at most LISTENER_TIMEOUT_MS 
   * + any current handler processing time.
   */
  public void terminate () {
    this.terminate = true;
  }

  @Override
  public void run () {

    // Set up to talk to the database.
    // Only mongo is supported at the moment.
    String dbType;
    String dbName;
    String dbHost;
    int    dbPort;

    MetadataDAOFactory theFactory = null;

    try {
        Properties prop = new Properties();
        prop.load(new FileInputStream(this.pathToPropsFile));
        dbType = prop.getProperty("org.renci.databridge.relevancedb.dbType", "mongo");
        dbName = prop.getProperty("org.renci.databridge.relevancedb.dbName", "test");
        dbHost = prop.getProperty("org.renci.databridge.relevancedb.dbHost", "localhost");
        dbPort = Integer.parseInt(prop.getProperty("org.renci.databridge.relevancedb.dbPort", "27017"));
    } catch (IOException ex) { 
        this.logger.log (Level.SEVERE, "Could not open property file: " + this.pathToPropsFile);
        return;
    }

    if (dbType.compareToIgnoreCase("mongo") != 0) {
        this.logger.log (Level.SEVERE, "Unsupported database type: " + dbType);
        return;
    }

    if (dbType.compareToIgnoreCase("mongo") == 0) {
        theFactory = MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, 
                                                              dbName, dbHost, dbPort);
        if (null == theFactory) {
           this.logger.log (Level.SEVERE, "Couldn't produce the MetadataDAOFactory");
           return;
        }
    }

    // Grab the desired headers from the type.
    String bindHeaders = this.amqpMessageType.getBindHeaders ();
    this.amqpComms.bindTheQueue (bindHeaders);

    if (this.logger != null) {
      this.logger.log (Level.FINE, "Bound '" + bindHeaders + "' to AMQPComms");
    }

    while (!terminate) { 

      try {

        AMQPMessage am = this.amqpComms.receiveMessage (LISTENER_TIMEOUT_MS);
        if (am != null) {
           if (null == theFactory) {
              this.logger.log (Level.SEVERE, "theFactory is null");
              return;
           } 
          this.amqpMessageHandler.handle (am, (Object) theFactory);
        }

      } catch (Exception e) {

        // dispatch exception to handler st it doesn't stop dispatch thread
        this.amqpMessageHandler.handleException (e);

        // @todo deal with exceptions here.

      }

    } 

  }

}
