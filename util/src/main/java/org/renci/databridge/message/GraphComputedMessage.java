package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author mrshoffn
 */
public class GraphComputedMessage implements AMQPMessageType {

  @Override
  public String getBindHeaders () {
    return "type:databridge;subtype:graphcomputed;x-match:all";
  }

}
