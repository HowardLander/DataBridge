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
  public static final String OUTPUT_FILE = "outputFile";
  public static final String SIMILARITY_ID = "similarityId";
  public static final String SNA_ID = "snaId";
  public static final String VERSION = "version";
  public static final String PARAMS = "params";

  // Message types for the network engine
  public static final String INSERT_SIMILARITYMATRIX_JAVA_URI_NETWORKDB = "Insert.SimilarityMatrix.Java.URI.NetworkDB";
  public static final String RUN_SNA_ALGORITHM_JAVA_NETWORKDB = "Run.SNA.Algorithm.Java.NetworkDB";
  public static final String CREATE_JSON_FILE_NETWORKDB_URI = "Create.JSON.File.NetworkDB.URI";

  public static final String bindHeaders = "type:databridge;subtype:network;xmatch:all";

  @Override
  public String getBindHeaders () {
    return bindHeaders;
  }

}
