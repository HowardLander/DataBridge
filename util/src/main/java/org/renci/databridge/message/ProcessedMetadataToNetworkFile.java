package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class ProcessedMetadataToNetworkFile extends NetworkListenerMessage {

  private static String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   *
   * @param nameSpace the nameSpace to attach of the data.
   * @param similarityId the similarityId of the data.
   */
  public static String getSendHeaders(String nameSpace, String similarityId) {
      return bindHeaders + ";" +
                    NAME + ":" + PROCESSED_METADATA_TO_NETWORKFILE  + ";" +
                    NAME_SPACE + ":" + nameSpace + ";" +
                    SIMILARITY_ID + ":" + similarityId;

  }

}
