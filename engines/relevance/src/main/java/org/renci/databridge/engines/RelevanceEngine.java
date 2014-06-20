package org.renci.databridge.engines;
import  org.renci.databridge.util.*;
import java.io.*;
import java.util.Properties;

/**
 * This is the main module for the DataBridge relevance engine.  This is a standalone server which
 * waits on a queue connected to the header exchange.  For each incoming message, the server 
 * looks at the message and, if needed, will fork a thread to handle the message.
 *
 */
public class RelevanceEngine {

    private static String bindHeaders;
    private static AMQPComms theComms;
   

    public static void main( String[] args )
    {
        String propFileName = null;

        if (args.length > 0) {
            propFileName = args[0];
        } else {
            propFileName = new String("relevance.conf");
        }    
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(propFileName)); 
           
            // The bind headers control the messages this server will respond to. If no headers
            // are specified, the server listens for any message, which is pretty clearly the 
            // wrong idea, so be sure to specify some.
            bindHeaders = prop.getProperty("org.renci.databridge.relevance.engine.bindHeaders", "xmatch:any");
        } catch (IOException ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }

        try {

            // Create the comms object.
            theComms = new AMQPComms(propFileName);
 
            // Bind the queue
            theComms.bindTheQueue(bindHeaders);

            // Listen for messages on the queue in a loop.
            while (true) {
                // This is a blocking call.
                AMQPMessage theMessage = theComms.receiveMessage();

                // Start a new thread using the handler class with the message as the argument.
                new RelevanceEngineMessageHandler(theMessage).start();
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            System.exit(2);
        } finally {
            theComms.unbindTheQueue(bindHeaders);
        }
    }
}
