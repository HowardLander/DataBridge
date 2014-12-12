package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class NetworkEngineMessage implements AMQPMessageType {
  public static final String CLASS = "className";
  public static final String METHOD = "methodName";
  public static final String NAME_SPACE = "nameSpace";
  public static final String NAME = "name";
  public static final String INPUT_URI = "inputURI";
  public static final String SIMILARITY_ID = "similarity_id";
  public static final String VERSION = "version";

  // Message types for the network engine
  public static final String INSERT_SIMILARITYMATRIX_JAVA_URI_NETWORKDB = "Insert.SimilarityMatrix.Java.URI.NetworkDB";
  public static final String RUN_SNA_ALGORITHM_JAVA_NETWORKDB = "Run.SNA.Algorithm.Java.NetworkDB";

  @Override
  public String getBindHeaders () {
    return "type:databridge;subtype:network;xmatch:all";
  }

}
