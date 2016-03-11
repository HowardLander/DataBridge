package org.renci.databridge.util;
import com.rabbitmq.client.*;
import java.util.*;
import java.lang.InterruptedException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This is intended to handle all communication tasks for the direct exchange portion  
 * of the AMQP based  communications network for the DataBridge project. At this writing,
 * this is used for communication between the batch server and the multiple worker nodes.
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class AMQPDirectComms extends AMQPComms {
     protected static Logger logger = Logger.getLogger ("org.renci.databridge.util");

     public AMQPDirectComms() {
     }

     /**
      * Convenience constructor to load properties from a file path.
      */
     public AMQPDirectComms (String propFilePath) {
       try {
         Properties p = new Properties ();
         p.load (new FileInputStream (propFilePath));
         init (p, true);
       } catch (Exception e) {
         e.printStackTrace ();
       }
     }

     /**
      * Convenience constructor to load properties from a file path and include a boolean to specify
      * consumer or producer.  Default is consumer. True for consumer, false for producer.
      */
     public AMQPDirectComms (String propFilePath, boolean isConsumer) {
       try {
         Properties p = new Properties ();
         p.load (new FileInputStream (propFilePath));
         init (p, isConsumer);
       } catch (Exception e) {
         e.printStackTrace ();
       }
     }

     /**

     /**
      * Convenience method to load properties from an InputStream.
      */
     public AMQPDirectComms (InputStream propInputStream) throws IOException {
       Properties p = new Properties ();
       p.load (propInputStream);
       init (p, true);
     }

     /**
      * AMQPComms constructor with a properties file to read.
      *
      *  @param  prop  The prop file to read to configure the communication channel. The property
      *                    object has to define at least the following properties: 
      *                    org.renci.databridge.primaryQueue, org.renci.databridge.exchange and
      *                    org.renci.databridge.queueHost.  Other relevant properties are
      *                    org.renci.databridge.queueDurability and org.renci.databridge.logLevel
      */
     public AMQPDirectComms (Properties prop) throws IOException {
       init (prop, true);
     }

     protected void init (Properties prop, boolean isConsumer) throws IOException {
       primaryQueue = prop.getProperty("org.renci.databridge.primaryQueue", "primary");
       theHost = prop.getProperty("org.renci.databridge.queueHost", "localhost");
       theExchange = prop.getProperty("org.renci.databridge.direct.exchange", "localhost");
       theLevel = Integer.parseInt(prop.getProperty("org.renci.databridge.logLevel", "4"));
       queueDurability = Boolean.parseBoolean(prop.getProperty("org.renci.databridge.queueDurability", "false"));
       logger.log(Level.INFO, "theExchange: " + theExchange);
       // Here's the Rabbit specific code.
       ConnectionFactory theFactory = new ConnectionFactory();
       theFactory.setHost(theHost);
       theConnection = theFactory.newConnection();
       theChannel = theConnection.createChannel();

       // Declare a queue to be the main queue.  If the queue is durable and messages sent to it
       // are also durable, then those messages will be recieved by the app at start time, even
       // if they were queued before the app was started.
       if (isConsumer) {
          theChannel.queueDeclare(primaryQueue, queueDurability, false, false, null);
       }

       // Declare a direct exchange.  Note that the declaration of the exchange is idempotent,
       // so at execution time this will primarily makes sure the exchange is of the expected
       // type.  If the exchange exists and is not a direct exchange, an error will be thrown.
       // Note also that we are declaring the exchange as durable, without checking for a 
       // property.  We don't want various apps to declare this differently, but we do (I think)
       // want the exchange to be durable.
       theChannel.exchangeDeclare(theExchange, "direct", true);

       // Create the consumer
       if (isConsumer) {
          consumer = new QueueingConsumer(theChannel);
       }
     }

     /**
      *  Code to publish a message 
      *
      *  @param  theMessage The user provided AMQPMessage.
      *  @param  persistence Whether or not to set MessageProperties.PERSISTENT_TEXT_PLAIN in the message.
      */
     public void publishMessage(AMQPMessage theMessage, Boolean persistence) {
         try {
   
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
       
            if (persistence) {
               // MessageProperties.PERSISTENT_TEXT_PLAIN is a static instance of AMQP.BasicProperties
               // that contains a delivery mode and a priority. So we pass them to the builder.
               builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
               builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
            }
       
            // Use the builder to create the BasicProperties object.
            AMQP.BasicProperties theProps = builder.build();

            theChannel.basicPublish(theExchange, routingKey, theProps, theMessage.getBytes());
   
         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }
     }

     /**
      *  Code to receive a message.  Note that this code blocks until a message is
      *  received.
      *
      */
     public AMQPMessage receiveMessage() {

         AMQPMessage thisMessage = new AMQPMessage();

         try {

             // Start the consumer
             theTag = theChannel.basicConsume(primaryQueue, true, consumer);

             // Wait for message to arrive
             QueueingConsumer.Delivery delivery = consumer.nextDelivery();

             // Build the AMQPMessage to return
             thisMessage.setRoutingKey(delivery.getEnvelope().getRoutingKey());
             thisMessage.setBytes(delivery.getBody());
             thisMessage.setProperties(delivery.getProperties());
             if (delivery.getBody() != null) {
                String thisBody = new String(delivery.getBody(), "UTF-8");
                logger.log(Level.INFO, "thisBody: " + thisBody);
                thisMessage.setContent(thisBody);
             }

             // Turn off the consumer till the next time
             theChannel.basicCancel(theTag);

         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }

         // let's not forget to return the received message.
         return thisMessage;
     }
     

     /**
      *  Code to receive a message.  Note that this code blocks until a message is
      *  received or the timeout value is exceeded.
      *
      * @param timeout in milliseconds
      * @return either the received message or null indicating a timeout.
      */
     public AMQPMessage receiveMessage(long timeout) {

         AMQPMessage thisMessage = null;

         try {

             // Start the consumer
             theTag = theChannel.basicConsume(primaryQueue, true, consumer);

             // Wait for either message to arrive or timeout.  Returns null on 
             // timeout
             QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeout);

             if (null != delivery) {
                // Build the AMQPMessage to return
                thisMessage = new AMQPMessage();
                thisMessage.setRoutingKey(delivery.getEnvelope().getRoutingKey());
                thisMessage.setBytes(delivery.getBody());
                if (delivery.getBody() != null) {
                   String thisBody = new String(delivery.getBody(), "UTF-8");
                   logger.log(Level.INFO, "thisBody: " + thisBody);
                   thisMessage.setContent(thisBody);
                }
                thisMessage.setProperties(delivery.getProperties());
             } else 

             // Turn off the consumer till the next time
             theChannel.basicCancel(theTag);

         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }

         // let's not forget to return the received message or null.
         return thisMessage;
     }
}
