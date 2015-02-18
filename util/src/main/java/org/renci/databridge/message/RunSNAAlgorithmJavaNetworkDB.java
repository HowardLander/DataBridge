package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class RunSNAAlgorithmJavaNetworkDB extends NetworkEngineMessage {

  private String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   * @param className the class containing the method to be executed.
   * @param methodName the method to be executed.
   * @param nameSpace the nameSpace to attach to the inserted data.
   * @param similarityId the URI for the file containg the metadata to be inserted.
   */
  public static String getSendHeaders (String className, String methodName, String nameSpace, String similarityId) {
    return bindHeaders + ";" + 
       NetworkEngineMessage.NAME + ":" + NetworkEngineMessage.RUN_SNA_ALGORITHM_JAVA_NETWORKDB + ";" +
       NetworkEngineMessage.CLASS + ":" + className + ";" +
       NetworkEngineMessage.METHOD + ":" + methodName + ";" + 
       NetworkEngineMessage.NAME_SPACE + ":" + nameSpace + ";" +
       NetworkEngineMessage.SIMILARITY_ID + ":" + similarityId;
  }

}
