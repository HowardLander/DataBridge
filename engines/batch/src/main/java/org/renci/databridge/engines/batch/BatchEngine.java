package org.renci.databridge.engines.batch;
import  org.renci.databridge.util.*;
import  org.renci.databridge.message.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.util.Properties;

/**
 * This is the main module for the DataBridge batch engine.  This is a standalone server which
 * waits on a queue connected to the header exchange.  For each incoming message, the server 
 * looks at the message and, if needed, will fork a thread to handle the message.
 *
 */
public class BatchEngine {

    protected static Logger logger = Logger.getLogger ("org.renci.databridge.engines.batch");

    /**
     * @param args must contain a path to a properties file that defines:
     * org.renci.databridge.primaryQueue
     * org.renci.databridge.exchange
     * org.renci.databridge.queueHost
     */
    public static void main(String[] args ) {
        String propFileName = null;

        if (args.length > 0) {
            propFileName = args[0];
        } else {
            propFileName = new String("batch.conf");
        }    

        logger.log(Level.INFO, "propFileName: " + propFileName);

        try {
            // Make props from the config file to pass to AMQPComms
            Properties props= new Properties();
            props.load(new FileInputStream(propFileName));
            props.setProperty("org.renci.databridge.primaryQueue", 
                              props.getProperty("org.renci.databridge.relevanceEngine.primaryQueue"));
           logger.log(Level.INFO,
                "primaryQueue set to: " + props.getProperty("org.renci.databridge.primaryQueue"));
     
            BatchEngineMessageListener aml = 
                new BatchEngineMessageListener (props, new BatchEngineMessage(), 
                                         new BatchEngineMessageHandler(), logger);

            aml.start ();

            // Start a second thread to listen for actionable messages.
            // We need to reset the org.renci.databridge.primaryQueue property. This is the queue
            // this listener will listen to.
        /*
            props.setProperty("org.renci.databridge.primaryQueue", 
                              props.getProperty("org.renci.databridge.relevanceEngine.ingestQueue"));
           logger.log(Level.INFO,
                "primaryQueue set to: " + props.getProperty("org.renci.databridge.primaryQueue"));

            BatchEngineMessageListener batchListener = 
                new BatchEngineMessageListener (props, 
                                         new BatchListenerMessage(), 
                                         new BatchEngineMessageHandler(), logger);

            batchListener.start ();

            aml.join ();
            batchListener.join (); // keeps main thread from exiting 
           */
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception in main: " + ex.toString());
            System.exit(1);
        }

    }
}
