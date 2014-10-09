package org.renci.databridge.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

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
   * @param propsInputStream properties for AMQPComms object initialization.
   * @param amqpMessageType 
   * @param amqpMessageHandler
   * @Param logger can be null.
   */
  public AMQPMessageListener (InputStream propsInputStream, AMQPMessageType amqpMessageType, AMQPMessageHandler amqpMessageHandler, Logger logger) throws IOException {

    this (amqpMessageType, amqpMessageHandler, logger);

    // creating AMQPComms here becausec passing it in would enable reusing 
    // the same AMQPComms instance, which is not safe across multiple clients
    this.amqpComms = new AMQPComms (propsInputStream);

  }

  /**
   * @param pathToPropsFile properties file for AMQPComms object initialization.
   * @param amqpMessageType
   * @param amqpMessageHandler
   * @Param logger can be null.
   */
  public AMQPMessageListener (String pathToPropsFile, AMQPMessageType amqpMessageType, AMQPMessageHandler amqpMessageHandler, Logger logger) throws IOException {

    this (amqpMessageType, amqpMessageHandler, logger);

    // creating AMQPComms here becausec passing it in would enable reusing
    // the same AMQPComms instance, which is not safe across multiple clients
    this.amqpComms = new AMQPComms (pathToPropsFile);

  }

  protected AMQPMessageListener (AMQPMessageType amqpMessageType, AMQPMessageHandler amqpMessageHandler, Logger logger) {
    this.amqpMessageType = amqpMessageType;
    this.amqpMessageHandler = amqpMessageHandler;
    this.logger = logger;
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
          this.amqpMessageHandler.handle (am, null);
        }

      } catch (Exception e) {

        // dispatch exception to handler st it doesn't stop dispatch thread
        this.amqpMessageHandler.handleException (e);

        // @todo deal with exceptions here.

      }

    } 

  }

}
