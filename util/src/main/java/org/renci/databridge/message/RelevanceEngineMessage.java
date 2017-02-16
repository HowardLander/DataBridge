package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class RelevanceEngineMessage implements AMQPMessageType {
  public static final String CLASS = "className";
  public static final String NAME_SPACE = "nameSpace";
  public static final String NAME = "name";
  public static final String OUTPUT_FILE = "outputFile";
  public static final String ENGINE_PARAMS = "engineParams";
  public static final String INCLUDE_ALL = "includeAll";
  public static final String PARAMS = "params";
  public static final String COUNT = "count";
  public static final String NORMALIZE = "normalize";
  public static final String DISTANCE = "distance";

  public static final String bindHeaders = "type:databridge;subtype:relevance;x-match:all";

  // Message types for the relevance engine
  public static final String CREATE_SIMILARITYMATRIX_JAVA_METADATADB_URI = "Create.SimilarityMatrix.Java.MetadataDB.URI";

  @Override
  public String getBindHeaders () {
    return bindHeaders;
  }

}
