package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author mrshoffn
 */
public class IngestMetadataMessage implements AMQPMessageType {

  @Override
  public String getBindHeaders () {
    return "type:databridge;subtype:ingestmetadata;xmatch:all";
  }

}
