package org.renci.databridge.engines;
import  org.renci.databridge.util.*;

/**
 * This is the main module for the DataBridge relevance engine.  This is a standalone server which
 * waits on a queue connected to the header exchange.  For each incoming message, the server 
 * looks at the message and, if needed, will fork a thread to handle the message.
 *
 */
public class RelevanceEngine 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
