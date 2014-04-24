package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import org.renci.databridge.util.AMQPLogger;

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
   * @param logger The AMQPLogger on which to send log messages
   *
   * @return The message to send back to sender. Null if no response.
   */
  public String handle(String msg, AMQPLogger logger) throws Exception{
    return null;
  }

}
