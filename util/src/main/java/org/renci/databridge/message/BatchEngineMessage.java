package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class BatchEngineMessage implements AMQPMessageType {
  public static final String CLASS = "className";
  public static final String NAME_SPACE = "nameSpace";
  public static final String PARAMS = "params";
  public static final String NAME = "name";
  public static final String OUTPUT_FILE = "outputFile";
  public static final String START_INDEX = "startIndex";
  public static final String COUNT = "count";
  public static final String INPUT_DIR = "inputDir";
  public static final String DIMENSION = "dimension";

  public static final String bindHeaders = "type:databridge;subtype:batch;x-match:all";

  // Message types for the relevance engine
  public static final String CREATE_SIMILARITYMATRIX_JAVA_BATCH_METADATADB_URI = "Create.SimilarityMatrix.Java.Batch.MetadataDB.URI";
  public static final String CREATE_SIMILARITYMATRIXSUBSET_JAVA_BATCH_FILE = "Create.SimilarityMatrixSubset.Java.Batch.File";
  public static final String CREATE_SIMILARITYMATRIXSUBSET_JAVA_BATCH_ACK = "Create.SimilarityMatrixSubset.Java.Batch.Ack";
  public static final String CREATE_SIMILARITYMATRIXSUBSET_JAVA_BATCH_DONE = "Create.SimilarityMatrixSubset.Java.Batch.Done";
  public static final String CREATE_SIMILARITYMATRIXSUBSET_JAVA_BATCH_WORKER_EXIT = "Create.SimilarityMatrixSubset.Java.Batch.Worker.Exit";

  @Override
  public String getBindHeaders () {
    return bindHeaders;
  }

}
