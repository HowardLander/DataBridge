package org.renci.databridge.message;

import org.renci.databridge.util.AMQPMessageType;

/**
 * @author lander
 */
public class NetworkResultsMessage implements AMQPMessageType {
  public static final String STATUS = "status";
  public static final String NAME = "name";
  public static final String bindHeaders = "type:databridge;subtype:networkresults";

  // Message types for the network engine listener
  public static final String RETURN_CLOSEST_MATCHES_IN_NETWORK ="Return.Closest.Matches.In.Network";
  public static final String RETURN_RUN_SNA_ALGORITHM = "Return.Run.SNA.Algorithm";

  @Override
  public String getBindHeaders () {
    return bindHeaders;
  }

}
