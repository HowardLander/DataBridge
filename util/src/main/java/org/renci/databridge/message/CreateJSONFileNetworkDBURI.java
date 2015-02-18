package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class CreateJSONFileNetworkDBURI extends NetworkEngineMessage {

  private String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   * @param nameSpace the nameSpace to attach to the inserted data.
   * @param similarityId the URI for the file containg the metadata to be inserted.
   * @param snaId the Id for the sna instance to output
   * @param outputFile the file to which to output the requested JSON
   */
  public static String getSendHeaders (String nameSpace, String similarityId, String snaId, String outputFile) {
    return bindHeaders + ";" + 
       NetworkEngineMessage.NAME + ":" + NetworkEngineMessage.CREATE_JSON_FILE_NETWORKDB_URI + ";" +
       NetworkEngineMessage.NAME_SPACE + ":" + nameSpace + ";" +
       NetworkEngineMessage.SIMILARITY_ID + ":" + similarityId + ";" +
       NetworkEngineMessage.SNA_ID + ":" + snaId + ";" +
       NetworkEngineMessage.OUTPUT_FILE + ":" + outputFile;
  }

}
