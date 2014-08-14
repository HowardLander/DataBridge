package org.renci.databridge.util;

/**
 * Implementer handles AMQP messages and errors that are dispatched to it.
 *
 * @author mrshoffn
 */
public interface AMQPMessageHandler {

  /**
   * Message that has been successfully received.
   */
  public void handle (AMQPMessage amqpMessage);


  /**
   * Any error that occurred during listening for messages.
   */
  public void handleException (Exception exception);

}
