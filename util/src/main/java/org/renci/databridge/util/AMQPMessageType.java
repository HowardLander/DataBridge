package org.renci.databridge.util;

/**
 * Implementer supplies AMQP headers to bind to receive messages of its type. 
 */
public interface AMQPMessageType {

  /**
   * @return AMQP message header string for messages that implementer wants to receive, in format key1:value1;key2:value2;[x-match:any|x-match:all]
   */
  public String getBindHeaders ();

}
