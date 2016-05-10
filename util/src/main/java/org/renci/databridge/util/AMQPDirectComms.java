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
     private String batchQueue;
     private String replyQueue;

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

     /**
      * AMQPComms constructor with a properties object and boolean for consumer
      *
      *  @param  prop  The prop file to read to configure the communication channel. The property
      *                    object has to define at least the following properties: 
      *                    org.renci.databridge.primaryQueue, org.renci.databridge.exchange and
      *                    org.renci.databridge.queueHost.  Other relevant properties are
      *                    org.renci.databridge.queueDurability and org.renci.databridge.logLevel
      *  @param isConsumer True for consumer, false for producer.
      */
     public AMQPDirectComms (Properties prop, boolean isConsumer) throws IOException {
       init (prop, isConsumer);
     }


     protected void init (Properties prop, boolean isConsumer) throws IOException {
       primaryQueue = prop.getProperty("org.renci.databridge.primaryQueue", "primary");
       batchQueue = prop.getProperty("org.renci.databridge.batchEngine.directWorkerQueue", "batch");
       replyQueue = prop.getProperty("org.renci.databridge.batchEngine.replyQueue", "batchReply");
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
       // are also durable, then those messages will be received by the app at start time, even
       // if they were queued before the app was started.
       // We are going to declare a reply queue.  We are (roughly) following the AMQP RPC pattern
       // See https://www.rabbitmq.com/tutorials/tutorial-six-java.html for more details.
       if (isConsumer) {
          logger.log(Level.INFO, "declaring consumer queue: " + replyQueue);
          theChannel.queueDeclare(replyQueue, queueDurability, false, false, null);
          theChannel.queueBind(replyQueue, theExchange, replyQueue);
          // We want to route the messages to the workers, who are listening on the batch queue
          this.routingKey = batchQueue;
          this.primaryQueue = replyQueue;
       } else {
          logger.log(Level.INFO, "declaring non-consumer queue: " + batchQueue);
          theChannel.queueDeclare(batchQueue, queueDurability, false, false, null);
          theChannel.queueBind(batchQueue, theExchange, batchQueue);
          // We want to route the messages to the batch server, which is listening on the reply queue
          this.routingKey = replyQueue;
          this.primaryQueue = batchQueue;
       }

       // Declare a direct exchange.  Note that the declaration of the exchange is idempotent,
       // so at execution time this will primarily makes sure the exchange is of the expected
       // type.  If the exchange exists and is not a direct exchange, an error will be thrown.
       // Note also that we are declaring the exchange as durable, without checking for a 
       // property.  We don't want various apps to declare this differently, but we do (I think)
       // want the exchange to be durable.
       theChannel.exchangeDeclare(theExchange, "direct", true);

       // Create the consumer
      consumer = new QueueingConsumer(theChannel);
       if (isConsumer == false) {
          // We are the producer so we tell Rabbit not to send more than one message to any one worker
          theChannel.basicQos(1);
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

            theChannel.basicPublish(theExchange, this.routingKey, theProps, theMessage.getBytes());
   
         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }
     }

     /**
      *  Code to publish a message using a "correlationId"
      *
      *  @param  theMessage The user provided AMQPMessage.
      *  @param  persistence Whether or not to set MessageProperties.PERSISTENT_TEXT_PLAIN in the message.
      *  @param  id The id for this message.
      */
     public void publishMessage(AMQPMessage theMessage, boolean persistence, String id) {
         try {
   
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
       
            if (persistence) {
               // MessageProperties.PERSISTENT_TEXT_PLAIN is a static instance of AMQP.BasicProperties
               // that contains a delivery mode and a priority. So we pass them to the builder.
               builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
               builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
            }

            // add the "RPC" properties
            builder.correlationId(id);
            builder.replyTo(replyQueue);
       
            // Use the builder to create the BasicProperties object.
            AMQP.BasicProperties theProps = builder.build();
            String stringMessage = new String(theMessage.getBytes());

            logger.log(Level.INFO, "publishing message " + stringMessage + " to exchange: " + theExchange +
               " using routingKey " + this.routingKey);
            theChannel.basicPublish(theExchange, this.routingKey, theProps, theMessage.getBytes());
   
         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }
     }

     /**
      *  Code to ack a message using a "correlationId"
      *
      *  @param  theMessage The AMQPMessage that needs to be acknowledge.
      */
     public void ackMessage(AMQPMessage theMessage) {
         try {
   
            theChannel.basicAck(theMessage.getDeliveryTag(), false);
   
         } catch (Exception e){
            // Not much we can do with the exception ...
            this.logger.log (Level.SEVERE, "Caught Exception sending acknowledgment: " + e.getMessage());
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
             theTag = theChannel.basicConsume(primaryQueue, false, consumer);

             // Wait for message to arrive
             QueueingConsumer.Delivery delivery = consumer.nextDelivery();

             // Build the AMQPMessage to return
             thisMessage.setRoutingKey(delivery.getEnvelope().getRoutingKey());
             thisMessage.setBytes(delivery.getBody());
             String bytesAsString = new String(thisMessage.getBytes(), "UTF-8");
             logger.log(Level.INFO, "bytesAsString: " + bytesAsString);
             thisMessage.setProperties(delivery.getProperties());
             if (delivery.getBody() != null) {
                String thisBody = new String(delivery.getBody(), "UTF-8");
                logger.log(Level.INFO, "thisBody: " + thisBody);
                thisMessage.setContent(thisBody);
             }

             // Save the delivery tag for later
             thisMessage.setDeliveryTag(delivery.getEnvelope().getDeliveryTag());

             // Set the ordinary tag for later
             thisMessage.setTag(delivery.getProperties().getCorrelationId());

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
             theTag = theChannel.basicConsume(primaryQueue, false, consumer);

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

               // Save the delivery tag for later
               thisMessage.setDeliveryTag(delivery.getEnvelope().getDeliveryTag());
               logger.log(Level.INFO, "setting delivery tag: " + delivery.getEnvelope().getDeliveryTag());

               // Set the ordinary tag for later
               thisMessage.setTag(delivery.getProperties().getCorrelationId());
             } 

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
