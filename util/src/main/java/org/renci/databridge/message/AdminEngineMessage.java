package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class AdminEngineMessage implements AMQPMessageType {
  public static final String CLASS = "className";
  public static final String NAME_SPACE = "nameSpace";
  public static final String NAME = "name";
  public static final String OUTPUT_FILE = "outputFile";

  public static final String bindHeaders = "type:databridge;subtype:relevance;x-match:all";

  // Message types for the relevance engine
  public static final String CREATE_SIMILARITYMATRIX_JAVA_METADATADB_URI = "Create.SimilarityMatrix.Java.MetadataDB.URI";

  @Override
  public String getBindHeaders () {
    return bindHeaders;
  }

}
