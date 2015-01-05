package org.renci.databridge.util;

/**
 * Implementer handles AMQP messages and errors that are dispatched to it.
 *
 * @author mrshoffn
 */
public interface AMQPMessageHandler {

  /**
   * Message that has been successfully received.
   * @throws Exception. The dispatcher should hand the exception back on the handleException method.
   * @param amqpMessage The amqp message
   * @param extra Implementation specific extra info needed to handle the request.
   */
  public void handle (AMQPMessage amqpMessage, Object extra) throws Exception;


  /**
   * Any error that occurred during listening for messages.
   */
  public void handleException (Exception exception);

}
