package org.renci.databridge.engines.network;
import  org.renci.databridge.util.*;
import  org.renci.databridge.message.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.util.Properties;

/**
 * This is the main module for the DataBridge network engine.  This is a standalone server which
 * waits on a queue connected to the header exchange.  For each incoming message, the server 
 * looks at the message and, if needed, will fork a thread to handle the message.
 *
 */
public class NetworkEngine {

    protected static Logger logger = Logger.getLogger ("org.renci.databridge.engines.network");
    public static void main(String[] args ) {
        String propFileName = null;
        String listenerProps = null;

        if (args.length > 1) {
            propFileName = args[0];
            listenerProps = args[1];
        } else {
            propFileName = new String("network.conf");
            listenerProps = new String("networkListener.conf");
        }    
        try {
            NetworkEngineMessageListener aml = 
                new NetworkEngineMessageListener (propFileName, new NetworkEngineMessage(), 
                                                  new NetworkEngineMessageHandler(), logger);
            aml.start ();

            NetworkEngineMessageListener networkListener = 
                new NetworkEngineMessageListener (listenerProps, new NetworkListenerMessage(), 
                                                  new NetworkEngineMessageHandler(), logger);
            networkListener.start ();
            networkListener.join (); // keeps main thread from exiting 
            aml.join (); // keeps main thread from exiting 
        } catch (Exception ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }

    }
}
