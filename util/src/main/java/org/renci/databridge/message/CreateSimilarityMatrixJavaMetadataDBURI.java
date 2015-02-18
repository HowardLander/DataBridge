package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class CreateSimilarityMatrixJavaMetadataDBURI extends RelevanceEngineMessage {

  private String sendHeaders = null;

 /**
   * This function returns a parametrized header string specific to sending this message.
   *
   * @param className the class containing the method to be executed.
   * @param methodName the method to be executed.
   * @param nameSpace the nameSpace to attach to the inserted data.
   * @param outputFile the outputFile
   */
  public static String getSendHeaders (String className, String methodName, String nameSpace, String outputFile) {
    return bindHeaders + ";" + 
       RelevanceEngineMessage.NAME + ":" + RelevanceEngineMessage.CREATE_SIMILARITYMATRIX_JAVA_METADATADB_URI+ ";" +
       RelevanceEngineMessage.CLASS + ":" + className + ";" +
       RelevanceEngineMessage.METHOD + ":" + methodName + ";" + 
       RelevanceEngineMessage.NAME_SPACE + ":" + nameSpace + ";" +
       RelevanceEngineMessage.OUTPUT_FILE + ":" + outputFile;
  }

}
