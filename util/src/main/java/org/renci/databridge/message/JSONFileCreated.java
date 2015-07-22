package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class JSONFileCreated extends NetworkListenerMessage {

  private static String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   *
   * @param nameSpace the nameSpace to attach of the data.
   * @param jsonFile the created json file
   */
  public static String getSendHeaders(String nameSpace, String jsonFile) {
      return bindHeaders + ";" +
                    NAME + ":" + JSON_FILE_CREATED  + ";" +
                    NAME_SPACE + ":" + nameSpace + ";" +
                    JSON_FILE + ":" + jsonFile;

  }

}
