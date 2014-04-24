package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import org.renci.databridge.util.AMQPLogger;

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
   * @param logger The AMQPLogger for log messages
   *
   * @return The message to return to original sender. Null for no response.
   */
  public String handle(String msg, AMQPLogger logger) throws Exception;

}
