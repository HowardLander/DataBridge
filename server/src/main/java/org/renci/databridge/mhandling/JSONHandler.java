package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;

/**
 * Bottom level message handler for messages of type JSON. These are JSON requests
 * from the front end.
 *
 * @author Ren Bauer - RENCI (www.renci.org)
 */

public class JSONHandler implements BaseHandler{

  /**
   * Handle the message appropriately. For JSON requests, a response should be
   * sent with the information requested.
   *
   * @param msg The original message minus the filetype and delimiting colon
   * @param channel The output rabbitMQ channel for sending messages
   * @param LOG_QUEUE The queue on which to send log messages
   */
  public void handle(String msg, Channel channel, String LOG_QUEUE) throws Exception{
  }

}
