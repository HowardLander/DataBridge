package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class InsertSimilarityMatrixJavaURINetworkDB extends NetworkEngineMessage {

  private String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   * @param inputURI the URI for the file containg the network data to be inserted.
   */
  public static String getSendHeaders (String inputURI) {
    return bindHeaders + ";" + 
       NetworkEngineMessage.NAME + ":" + NetworkEngineMessage.INSERT_SIMILARITYMATRIX_JAVA_URI_NETWORKDB + ";" +
       NetworkEngineMessage.INPUT_URI + ":" + inputURI;
  }

}
