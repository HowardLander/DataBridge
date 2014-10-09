package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class RelevanceEngineMessage implements AMQPMessageType {
  public static final String CLASS = "className";
  public static final String METHOD = "methodName";
  public static final String NAME_SPACE = "nameSpace";

  @Override
  public String getBindHeaders () {
    return "type:databridge;subtype:relevance;xmatch:all";
  }

}
