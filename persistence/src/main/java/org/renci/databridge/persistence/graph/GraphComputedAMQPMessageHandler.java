package org.renci.databridge.persistence.graph;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.renci.databridge.util.AMQPMessage;
import org.renci.databridge.util.AMQPMessageHandler;

/**
 * Handles "graph computed" DataBridge message by persisting the graph. 
 * 
 * @author mrshoffn
 */
public class GraphComputedAMQPMessageHandler implements AMQPMessageHandler {

  private Logger logger = Logger.getLogger ("org.renci.databridge.persistence.graph");

  @Override
  public void handle (AMQPMessage amqpMessage) {

   this.logger.log (Level.INFO, "AMQPMessage: '" + new String (amqpMessage.getBytes ()) + "'");

  }

}
