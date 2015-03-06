package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author mrshoffn
 */
public class TestMessage implements AMQPMessageType {

  @Override
  public String getBindHeaders () {
    return "type:databridge;subtype:test;x-match:all";
  }

}
