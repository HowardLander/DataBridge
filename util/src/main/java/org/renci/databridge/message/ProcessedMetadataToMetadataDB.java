package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class ProcessedMetadataToMetadataDB extends IngestListenerMessage {

  private static String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   *
   * @param nameSpace the nameSpace to attach to the inserted data.
   */
  public static String getSendHeaders(String nameSpace) {
      return bindHeaders + ";" +
                    NAME + ":" + PROCESSED_METADATA_TO_METADATADB  + ";" +
                    NAME_SPACE + ":" + nameSpace;

  }

}
