package org.renci.databridge.engines.relevance;
import  org.renci.databridge.util.*;
import  org.renci.databridge.message.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.util.Properties;

/**
 * This is the main module for the DataBridge relevance engine.  This is a standalone server which
 * waits on a queue connected to the header exchange.  For each incoming message, the server 
 * looks at the message and, if needed, will fork a thread to handle the message.
 *
 */
public class RelevanceEngine {

    protected static Logger logger = Logger.getLogger ("org.renci.databridge.engines.relevance");

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
            propFileName = new String("relevance.conf");
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
     
            RelevanceEngineMessageListener aml = 
                new RelevanceEngineMessageListener (props, 
                                         new RelevanceEngineMessage(), 
                                         new RelevanceEngineMessageHandler(), logger);

            aml.start ();

            // Start a second thread to listen for actionable messages.
            // We need to reset the org.renci.databridge.primaryQueue property. This is the queue
            // this listener will listen to.
            props.setProperty("org.renci.databridge.primaryQueue", 
                              props.getProperty("org.renci.databridge.relevanceEngine.ingestQueue"));
            logger.log(Level.INFO,
                "primaryQueue set to: " + props.getProperty("org.renci.databridge.primaryQueue"));

            RelevanceEngineMessageListener ingestListener = 
                new RelevanceEngineMessageListener (props, 
                                         new IngestListenerMessage(), 
                                         new RelevanceEngineMessageHandler(), logger);

            ingestListener.start ();

            // Start a third thread to listen for rpc messages.
            // We need to reset the org.renci.databridge.primaryQueue property. This is the queue
            // this listener will listen to.
            props.setProperty("org.renci.databridge.rpcQueue",
                              props.getProperty("org.renci.databridge.relevanceEngine.rpcQueue"));
            logger.log(Level.INFO,
                "primaryQueue for RPCHandler set to: " + props.getProperty("org.renci.databridge.primaryQueue"));

            RelevanceEngineRPCListener rpcHandler =
                new RelevanceEngineRPCListener (props,
                                         new RelevanceEngineMessage(),
                                         new RelevanceEngineRPCHandler(), logger);
            rpcHandler.start ();

            rpcHandler.join();
            ingestListener.join (); // keeps main thread from exiting 
            aml.join ();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception in main: " + ex.toString());
            System.exit(1);
        }

    }
}
