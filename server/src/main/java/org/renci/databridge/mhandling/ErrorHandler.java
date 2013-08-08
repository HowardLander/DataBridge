package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;

/**
 * Bottom level message handler for messages of type Error.
 *
 * @author Ren Bauer - RENCI (www.renci.org)
 */
public class ErrorHandler implements BaseHandler {

  /**
   * Handle the message appropriately. For errors, they should probably be logged
   * or thrown or something..
   *
   * @param msg The original message minus the filetype and delimiting colon
   * @param channel The output rabbitMQ channel for sending messages
   * @param LOG_QUEUE The queue on which to send log messages
   */
  public void handle(String msg, Channel channel, String LOG_QUEUE) throws Exception{
  }

}
