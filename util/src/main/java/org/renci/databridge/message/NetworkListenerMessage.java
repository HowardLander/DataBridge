package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class NetworkListenerMessage implements AMQPMessageType {
  public static final String NAME_SPACE = "nameSpace";
  public static final String NAME = "name";
  public static final String SIMILARITY_ID = "similarityId";

  public static final String bindHeaders = "type:databridge;subtype:networklistener;x-match:all";

  // Message types for the network engine listener
  public static final String PROCESSED_METADATA_TO_NETWORKFILE ="Processed.Metadata.To.NetworkFile";

  @Override
  public String getBindHeaders () {
    return bindHeaders;
  }

}
