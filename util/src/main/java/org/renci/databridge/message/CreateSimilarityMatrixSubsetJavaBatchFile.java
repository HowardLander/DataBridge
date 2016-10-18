package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class CreateSimilarityMatrixSubsetJavaBatchFile extends BatchEngineMessage {

  private String sendHeaders = null;

 /**
   * This function returns a parametrized string specific to sending this message. The
   * message is used to tell the batch worker where to look for the json versions of
   * the metadata to compare, the startIndex for the metadata, the number of json files
   * for this batch invocation, the className to use for the comparison and the output
   * file name for the results.
   *
   * @param className the className to use for the comparison
   * @param nameSpace the nameSpace of the data to be compared
   * @param params the params that control what metadata to use
   * @param outputFile the file name for the similarity output
   * @param startIndex the startIndex for the metadata
   * @param count the number of json files to process
   * @param inputDir where to find the json files to process
   * @param dimension the dimension of the array
   */
  public static String getSendHeaders(String className, String nameSpace, String params, String outputFile, 
                                      int startIndex, int count, String inputDir, long dimension) {
      StringBuilder sb = new StringBuilder();
      sb.append(BatchEngineMessage.NAME);
      sb.append(":");
      sb.append(BatchEngineMessage.CREATE_SIMILARITYMATRIXSUBSET_JAVA_BATCH_FILE);
      sb.append(";");
      sb.append(BatchEngineMessage.CLASS);
      sb.append(":");
      sb.append(className);
      sb.append(";");
      sb.append(BatchEngineMessage.NAME_SPACE);
      sb.append(":");
      sb.append(nameSpace);
      sb.append(";");
      sb.append(BatchEngineMessage.PARAMS);
      sb.append(":");
      sb.append(params);
      sb.append(";");
      sb.append(BatchEngineMessage.OUTPUT_FILE);
      sb.append(":");
      sb.append(outputFile);
      sb.append(";");
      sb.append(BatchEngineMessage.START_INDEX);
      sb.append(":");
      sb.append(startIndex);
      sb.append(";");
      sb.append(BatchEngineMessage.COUNT);
      sb.append(":");
      sb.append(count);
      sb.append(";");
      sb.append(BatchEngineMessage.DIMENSION);
      sb.append(":");
      sb.append(dimension);
      sb.append(";");
      sb.append(BatchEngineMessage.INPUT_DIR);
      sb.append(":");
      sb.append(inputDir);
      return sb.toString(); 
  }

  
  /**
   * Get outputFile.
   *
   * @return outputFile as String.
   */
  public static String getOutputFile(String content)
  {
      String[] contentSplit = content.split(";");
      // We are assuming the content was produced by getSendHeaders above, so we can assume the order.
      String[] outputSplit = contentSplit[3].split(":");
      return outputSplit[1];
  }
  
  /**
   * Get inputDir.
   *
   * @return inputDir as String.
   */
  public static String getInputDir(String content)
  {
      String[] contentSplit = content.split(";");
      // We are assuming the content was produced by getSendHeaders above, so we can assume the order.
      String[] inputDirSplit = contentSplit[6].split(":");
      return inputDirSplit[1];
  }
  
}
