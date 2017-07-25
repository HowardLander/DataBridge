package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class IngestResultsMessage implements AMQPMessageType {
  public static final String STATUS = "status";
  public static final String NAME = "name";
  public static final String bindHeaders = "type:databridge;subtype:ingestresults";

  // Message types for the network engine listener
  public static final String RETURN_INSERT_METADATA_JAVA_FILEWITHPARAMS ="Return.Insert.Metadata.Java.Filewithparams";

  @Override
  public String getBindHeaders () {
    return bindHeaders;
  }

}
