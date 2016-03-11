package org.renci.databridge.engines.batch;

import org.renci.databridge.util.*;
import org.renci.databridge.persistence.metadata.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * A thread that listens to AMQP for messages based on specified headers and 
 * dispatches them to a handler as they arrive.
 *
 * @author mrshoffn
 */
public class BatchWorkerMessageListener extends Thread {

  protected static final long LISTENER_TIMEOUT_MS = 1000;

  protected AMQPComms amqpComms;
  protected AMQPMessageType amqpMessageType;
  protected AMQPMessageHandler amqpMessageHandler;
  protected Logger logger;
  protected Properties theProps = null;

  /**
   * @param props Properties object used for AMQPComms object initialization.
   * @param amqpMessageType
   * @param amqpMessageHandler
   * @param logger can be null.
   */
  public BatchWorkerMessageListener (Properties props, 
                                     AMQPMessageType amqpMessageType, 
                                     AMQPMessageHandler amqpMessageHandler, 
                                     Logger logger) throws IOException {

    this.amqpMessageType = amqpMessageType;
    this.amqpMessageHandler = amqpMessageHandler;
    this.logger = logger;

    // creating AMQPComms here becausec passing it in would enable reusing
    // the same AMQPComms instance, which is not safe across multiple clients
    this.amqpComms = new AMQPDirectComms (props);
    this.theProps = props;

  }

  protected volatile boolean terminate;
 
  /**
   * Call this to tell the listener to stop. The listener will stop in at most LISTENER_TIMEOUT_MS 
   * + any current handler processing time.
   */
  public void terminate () {
    this.terminate = true;
  }

  @Override
  public void run () {

      while (!terminate) {
          try {
              AMQPMessage am = this.amqpComms.receiveMessage (LISTENER_TIMEOUT_MS);
              if (am != null) {
                  // The message handler needs the property file so it can send action messages, so we 
                  // store it in an array of Objects along with the needed factory.
                  logger.log(Level.INFO, "received a message: " + am.getContent());
                  Object thePassedObjects[] = new Object[1];
                  thePassedObjects[0] = (Object) theProps;
                  this.amqpMessageHandler.handle (am, (Object) thePassedObjects);
              }
          } catch (Exception e) {
              // dispatch exception to handler st it doesn't stop dispatch thread
              this.amqpMessageHandler.handleException (e);

              // @todo deal with exceptions here.
          }
      } 
  }
}
