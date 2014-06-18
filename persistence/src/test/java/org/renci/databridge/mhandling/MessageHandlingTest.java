package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import java.io.*;

import org.renci.databridge.mhandling.*;
import org.renci.databridge.util.*;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.matchers.JUnitMatchers;
import org.junit.Rule;

public class MessageHandlingTest{

  private final static String QUEUE_NAME = "hello";

  private final static String LOG_QUEUE = "log";

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }
  
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Ignore
  @Test
  public void testMessageHandling() throws IOException, InterruptedException{

    System.out.println("Testing good message (assuming NetworkDataTest successful)");

    String[] expectedMessages = {
	"Listener: msg recieved",
	"Listener: msg forwarded",
	"Handler: initiated",
	"Handler: msg recieved",
	"Handler: message split",
	"Handler: message type determined",
        "Handler: BaseHandler initiated",
	"Handler: file location determined",
        "Handler: edge ID",
        "Handler: Populated from URL",
	"Handler: complete"};
    
    String path = System.getProperty("user.dir") + "/../util/testWriteToDisk";
    String message = MessageTypes.NETWORK + ":file://localhost" + path;
    File file = new File(path);
    TestCase.assertTrue("Cannot read file: " + path + ", ensure NetworkDataTest has run", file.canRead());

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queuePurge(LOG_QUEUE);

    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(LOG_QUEUE, true, consumer);
    
    boolean found = false;
    while(!message.equals(expectedMessages[expectedMessages.length - 1])){
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      message = new String(delivery.getBody());
      found = false;
      for(String m : expectedMessages){
	if(message.contains(m)){
	  found = true;
	}
      }
      //System.out.println(message);
      if(!found){
        break;
      }
    }

    channel.close();
    connection.close();

    TestCase.assertTrue("unexpected log message: " + message, found);
  }

  @Ignore
  @Test
  public void testBadFilename() throws IOException, InterruptedException{

    System.out.println("Testing bad filename");

    String expectedError = "Handler: ERROR - Invalid filename";
    String finishedMessage = "Handler: complete";

    String message = MessageTypes.NETWORK + ":file://localhost"+System.getProperty("user.dir")+"/__NOT_REAL";
    File file = new File(message);
    TestCase.assertTrue("Please delete file: " + message, !file.canRead());

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queuePurge(LOG_QUEUE);

    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(LOG_QUEUE, true, consumer);
    
    while(!message.contains("ERROR")){
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      message = new String(delivery.getBody());
      //System.out.println(message);
      boolean notFound = true;
      if(message.equals(finishedMessage)){
	System.out.println("Error message found");
        notFound = false;
      }
      TestCase.assertTrue("Completion message found: " + message, notFound);
    }

    TestCase.assertTrue("Error message incorrect: " + message, message.equals(expectedError));

    channel.close();
    connection.close();
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

}
