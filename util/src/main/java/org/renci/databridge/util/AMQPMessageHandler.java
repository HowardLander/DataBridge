package org.renci.databridge.util;

/**
 * Implementer handles AMQP messages that are dispatched to it.
 *
 * @author mrshoffn
 */
public interface AMQPMessageHandler {

  public void handle (AMQPMessage amqpMessage);

}
