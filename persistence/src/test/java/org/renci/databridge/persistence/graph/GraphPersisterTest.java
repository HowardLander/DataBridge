package org.renci.databridge.persistence.graph;

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

import org.renci.databridge.mhandling.*;
import org.renci.databridge.util.*;
import org.renci.databridge.message.*;

public class GraphPersisterTest {

  private final static String QUEUE_NAME = "hello";

  private final static String LOG_QUEUE = "log";

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }
  
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @todo This is a transport test. Needs to be factored out properly.
   * @todo Put in a proper junit test assertion...
   */
  @Test
  public void testiBaseTransport () throws Exception {

    System.out.println ("Testing message transport");

    String path = System.getProperty ("user.dir") + "/db.conf";
    File file = new File (path);
    TestCase.assertTrue ("Cannot read file: " + path, file.canRead ());

    // send GraphComputed message to queue

    AMQPMessageType amt = new GraphComputedMessage ();

    AMQPComms ac = new AMQPComms (path);

    String messageTestString = "Test GraphComputedMessage";

    AMQPMessage theMessage = new AMQPMessage (messageTestString.getBytes ());
    String headers = amt.getBindHeaders ();
    Boolean persistence = true;

    ac.publishMessage (theMessage, headers, persistence);

    // see if we can get the message off the queue

    AMQPMessageHandler amh = new AMQPMessageHandler () {
      public void handle (AMQPMessage amqpMessage) {
        String msg = new String (amqpMessage.getBytes ());
        System.out.println ("message received: " + msg);
      }
    };

    Logger logger = null;

    AMQPMessageListener aml = new AMQPMessageListener (path, amt, amh, logger);
    aml.start ();
    Thread.sleep (2000);
    aml.terminate ();

    // TestCase.assertTrue ("unexpected message content: " + message, found);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

/*
  public static void main (String [] args) throws Exception {

  }
*/
}
