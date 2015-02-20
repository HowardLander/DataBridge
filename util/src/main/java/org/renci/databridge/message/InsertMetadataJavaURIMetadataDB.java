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
   * @param methodName the method to be executed.
   * @param nameSpace the nameSpace to attach to the inserted data.
   * @param inputURI the URI for the file containg the metadata to be inserted.
   */
  public static String getSendHeaders(String className, String methodName, String nameSpace, String inputURI) {
      sendHeaders = bindHeaders + ";" +
                    NAME + ":" + INSERT_METADATA_JAVA_URI_METADATADB  + ";" +
                    CLASS + ":" + className + ";" +
                    METHOD + ":" + methodName + ";" +
                    NAME_SPACE + ":" + nameSpace + ";" +
                    INPUT_URI + ":" + inputURI;

      return sendHeaders;
  }

}
