package org.renci.databridge.util;

import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.matchers.JUnitMatchers;
import org.junit.Rule;

import com.rabbitmq.client.*;

import org.renci.databridge.message.TestMessage;

public class AmqpMessageMiniFrameworkTest {

  @BeforeClass
  public static void setUpBeforeClass () throws Exception {
  }
  
  @AfterClass
  public static void tearDownAfterClass () throws Exception {
  }

  protected String receivedString;

  /**
   * @todo The first invocation of this test will fail, and subsequent succeed, because consumer receives from the exchange the message put in it by the last invocation. This is possibly related to the same process both producing and consuming.
   * @todo Ordering of producer/consumer seems very sensitive. Consumer/producer will not work. Again, possibly related to same process doing both.
   *
   * AMQPComms requires specification of a primary queue, so on the sender we don't do AMQPComms.bindTheQueue (headers) to prevent producer from getting the message it just produced sent to its own queue.
   */
  @Test
  public void testBaseTransport () throws Exception {

    System.out.println ("Testing message transport...");

    // set up test message (which contains headers)
    AMQPMessageType amt = new TestMessage ();
    String headers = amt.getBindHeaders ();

    // send the test message
    InputStream is2 = getClass ().getResourceAsStream ("/AmqpMessageMiniFrameworkTest-sender.conf");
    AMQPComms ac = new AMQPComms (is2);
    // ac.bindTheQueue (headers);
    String messageTestString = "Test sending and receiving TestMessage";
    AMQPMessage theMessage = new AMQPMessage (messageTestString.getBytes ());
    Boolean persistence = true;
    ac.publishMessage (theMessage, headers, persistence);

    // start handler thread 
    InputStream is = getClass ().getResourceAsStream ("/AmqpMessageMiniFrameworkTest-handler.conf");
    AMQPMessageHandler amh = new AMQPMessageHandler () {
      public void handle (AMQPMessage amqpMessage, Object extra) {
        receivedString = new String (amqpMessage.getBytes ());
        System.out.println ("AMQPMessageHandler message received: " + receivedString);
      }
      public void handleException (Exception exception) {
        System.out.println ("AMQPMessageHandler received exception: ");
        exception.printStackTrace ();
      }
    };
    Logger logger = null;
    AMQPMessageListener aml = new AMQPMessageListener (is, amt, amh, logger);
    aml.start ();

    Thread.sleep (2000); // just to be on the safe side...
    aml.terminate ();
    aml.join ();

    // did we receive the message?
    TestCase.assertEquals ("Received message content does not match what was sent.", messageTestString, receivedString);

  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

}
