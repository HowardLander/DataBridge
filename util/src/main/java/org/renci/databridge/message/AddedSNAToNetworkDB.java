package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class AddedSNAToNetworkDB extends NetworkListenerMessage {

  private static String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   *
   * @param nameSpace the nameSpace to attach of the network analysis.
   * @param snaID the snaID of the network analysis.
   */
  public static String getSendHeaders(String nameSpace, String snaId) {
      return bindHeaders + ";" +
                    NAME + ":" + ADDED_SNA_TO_NETWORKDB  + ";" +
                    NAME_SPACE + ":" + nameSpace + ";" +
                    SNA_ID + ":" + snaId;

  }

}
