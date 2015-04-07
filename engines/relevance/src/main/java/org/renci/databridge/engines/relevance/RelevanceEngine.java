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
        String ingestProps = null;

        if (args.length > 1) {
            propFileName = args[0];
            ingestProps = args[1];
        } else {
            propFileName = new String("relevance.conf");
            ingestProps = new String("ingest.conf");
        }    
        try {
            RelevanceEngineMessageListener aml = 
                new RelevanceEngineMessageListener (propFileName, 
                                         new RelevanceEngineMessage(), 
                                         new RelevanceEngineMessageHandler(), logger);

            aml.start ();

            // Start a second thread to listen for messages from the ingester
            RelevanceEngineMessageListener ingestListener = 
                new RelevanceEngineMessageListener (ingestProps, 
                                         new IngestListenerMessage(), 
                                         new RelevanceEngineMessageHandler(), logger);

            ingestListener.start ();

            aml.join ();
            ingestListener.join (); // keeps main thread from exiting 
        } catch (Exception ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }

    }
}
