package org.renci.databridge.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;

/**
 * A thread that listens to AMQP for messages based on specified headers and 
 * dispatches them to a handler as they arrive.
 *
 * @author mrshoffn
 */
public class AMQPMessageListener extends Thread {

  protected static final long LISTENER_TIMEOUT_MS = 1000;

  protected AMQPComms amqpComms;
  protected AMQPMessageType amqpMessageType;
  protected AMQPMessageHandler amqpMessageHandler;
  protected Logger logger;

  /** 
   * @param pathToPropsFile properties file for AMQPComms object initialization.
   * @param amqpMessageType 
   * @param amqpMessageHandler
   * @Param logger can be null.
   */
  public AMQPMessageListener (String pathToPropsFile, AMQPMessageType amqpMessageType, AMQPMessageHandler amqpMessageHandler, Logger logger) {

    // creating AMQPComms here bc passing it in would enable reusing the same
    // AMQPComms instance, which is not safe across multiple clients
    this.amqpComms = new AMQPComms (pathToPropsFile);

    this.amqpMessageType = amqpMessageType;
    this.amqpMessageHandler = amqpMessageHandler;
  }

  protected volatile boolean terminate;
 
  /**
   * Call this to tell the listener to stop. The listener will stop in at most LISTENER_TIMEOUT_MS + any current handler processing time.
   */
  public void terminate () {
    this.terminate = true;
  }

  @Override
  public void run () {

    String bindHeaders = this.amqpMessageType.getBindHeaders ();

    this.amqpComms.bindTheQueue (bindHeaders);
    if (this.logger != null) {
      this.logger.log (Level.FINE, "Bound '" + bindHeaders + "' to AMQPComms");
    }

    while (!terminate) { 

      try {

        AMQPMessage am = this.amqpComms.receiveMessage (LISTENER_TIMEOUT_MS);
        if (am != null) {
          amqpMessageHandler.handle (am);
        }

      } catch (Exception e) {

        // sink any exception from handler st it doesn't affect dispatching
        if (this.logger != null) {
          this.logger.log (Level.WARNING, "Sunk exception: ", e);
        }

      }

    } 

  }

}
