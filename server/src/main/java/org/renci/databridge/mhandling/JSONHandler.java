package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import org.json.simple.parser.*;
import org.json.*;
import org.renci.databridge.util.AMQPLogger;

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
   * @param logger The AMQPLogger on which to send log messages
   *
   * @return The message to return to original sender.
   */
  public String handle(String msg, Channel channel, AMQPLogger logger) throws Exception{
    JSONParser parser = new JSONParser();
    Object obj = parser.parse(msg);
    //JSONObject req = (JSONObject) obj;
    //JSONArray 
    return null;
  }
 
}
