package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;

/**
 * Interface for bottom level message handlers dealing with specific message types.
 *
 * @author Ren Bauer - RENCI (www.renci.org)
 */
public interface BaseHandler{

  /**
   * Handle the message appropriately as per its file type
   *
   * @param msg The original message minus the filetype and delimiting colon
   * @param channel The output rabbitMQ channel for sending messages
   * @param LOG_QUEUE The queue on which to send log messages
   *
   * @return The message to return to original sender. Null for no response.
   */
  public String handle(String msg, Channel channel, String LOG_QUEUE) throws Exception;

}
