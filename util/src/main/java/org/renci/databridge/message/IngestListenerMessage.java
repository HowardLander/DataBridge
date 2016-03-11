package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class IngestListenerMessage implements AMQPMessageType {
  public static final String NAME_SPACE = "nameSpace";
  public static final String NAME = "name";
  public static final String COLLECTION_ID = "collectionId";

  public static final String bindHeaders = "type:databridge;subtype:ingestlistener;x-match:all";

  // Message types for the relevance engine
  public static final String PROCESSED_METADATA_TO_METADATADB ="Processed.Metadata.To.MetadataDB";
  public static final String INSERT_METADATA_JAVA_URI_METADATADB ="Insert.Metadata.Java.URI.MetadataDB";

  @Override
  public String getBindHeaders () {
    return bindHeaders;
  }

}
