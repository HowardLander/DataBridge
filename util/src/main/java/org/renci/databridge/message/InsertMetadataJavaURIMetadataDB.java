package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class InsertMetadataJavaURIMetadataDB extends IngestMetadataMessage {

  private static String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   *
   * @param className the class containing the method to be executed.
   * @param nameSpace the nameSpace to attach to the inserted data.
   * @param fireEvent true or false for whether Processed.Metadata.To.MetadataDB message should be fired by ingester.
   * @param inputURI the URI for the file containg the metadata to be inserted.
   */
  public static String getSendHeaders(String className, String nameSpace, String fireEvent, String inputURI) {
      return bindHeaders + ";" +
                    NAME + ":" + INSERT_METADATA_JAVA_URI_METADATADB  + ";" +
                    CLASS + ":" + className + ";" +
                    NAME_SPACE + ":" + nameSpace + ";" +
                    FIRE_EVENT + ":" + fireEvent + ";" + 
                    INPUT_URI + ":" + inputURI;
  }

}
