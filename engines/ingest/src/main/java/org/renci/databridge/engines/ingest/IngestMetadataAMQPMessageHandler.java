package org.renci.databridge.engines.ingest;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.renci.databridge.util.AMQPMessage;
import org.renci.databridge.util.AMQPMessageHandler;

/**
 * Handles "ingest metadata" DataBridge message by extracting relevant content and persisting it. 
 * 
 * @author mrshoffn
 */
public class IngestMetadataAMQPMessageHandler implements AMQPMessageHandler {

  private Logger logger = Logger.getLogger ("org.renci.databridge.engine.ingest");

  @Override
  public void handle (AMQPMessage amqpMessage) {

    this.logger.log (Level.INFO, "AMQPMessage: '" + new String (amqpMessage.getBytes ()) + "'");

  }

  public void handleException (Exception exception) {
    System.out.println ("IngestMetadataAMQPMessageHandler received exception: ");
    exception.printStackTrace ();
  }

}
