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
   * @param nameSpace the nameSpace to attach to the inserted data.
   */
  public static String getSendHeaders(String similarityId) {
      return bindHeaders + ";" +
                    NAME + ":" + PROCESSED_METADATA_TO_NETWORKFILE  + ";" +
                    SIMILARITY_ID + ":" + similarityId;

  }

}
