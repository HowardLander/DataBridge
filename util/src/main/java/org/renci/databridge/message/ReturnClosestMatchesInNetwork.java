package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class ReturnClosestMatchesInNetwork extends NetworkResultsMessage {

  private static String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   *
   * @param status the status result of the previous command.
   */
  public static String getSendHeaders(String status) {
      return bindHeaders + ";" +
                    NAME + ":" + RETURN_CLOSEST_MATCHES_IN_NETWORK  + ";" +
                    STATUS + ":" + status;

  }

}
