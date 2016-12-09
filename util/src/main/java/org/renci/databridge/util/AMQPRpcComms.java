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
 * This is intended to handle all communication tasks for the RPC portion  
 * of the AMQP based  communications network for the DataBridge project. At this writing,
 * this is used for communication between the network engine and the Web GUI.
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class AMQPRpcComms extends AMQPComms {
     protected static Logger logger = Logger.getLogger ("org.renci.databridge.util");
     private String batchQueue;
     private String rpcQueue;

     public AMQPRpcComms() {
     }

     /**
      * Convenience constructor to load properties from a file path.
      */
     public AMQPRpcComms (String propFilePath) {
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
     public AMQPRpcComms (String propFilePath, boolean isConsumer) {
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
     public AMQPRpcComms (InputStream propInputStream) throws IOException {
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
     public AMQPRpcComms (Properties prop) throws IOException {
       init (prop, true);
     }

     /**
      * AMQPComms constructor with a properties object and boolean for consumer. NOTE that we aren't 
      *  using the isConsumer param, because this code is currently only used for the server
      *  side of the RPC.  If that changes, we can always add back what we need.
      *
      *  @param  prop  The prop file to read to configure the communication channel. The property
      *                    object has to define at least the following properties: 
      *                    org.renci.databridge.rpcQueue, org.renci.databridge.exchange and
      *                    org.renci.databridge.queueHost.  Other relevant properties are
      *                    org.renci.databridge.queueDurability and org.renci.databridge.logLevel
      *  @param isConsumer True for consumer, false for producer.
      */
     public AMQPRpcComms (Properties prop, boolean isConsumer) throws IOException {
       init (prop, isConsumer);
     }


     protected void init (Properties prop, boolean isConsumer) throws IOException {
       rpcQueue = prop.getProperty("org.renci.databridge.rpcQueue", "dataBridgeRPC");
       theHost = prop.getProperty("org.renci.databridge.queueHost", "localhost");
       theExchange = prop.getProperty("org.renci.databridge.direct.exchange", "localhost");
       theLevel = Integer.parseInt(prop.getProperty("org.renci.databridge.logLevel", "4"));
       queueDurability = Boolean.parseBoolean(prop.getProperty("org.renci.databridge.queueDurability", "false"));
       // Here's the Rabbit specific code.
       ConnectionFactory theFactory = new ConnectionFactory();
       theFactory.setHost(theHost);
       theConnection = theFactory.newConnection();
       theChannel = theConnection.createChannel();

       // Declare a queue to be the main queue that receives the RPC requests.  If the queue 
       // is durable and messages sent to it
       // are also durable, then those messages will be received by the app at start time, even
       // if they were queued before the app was started.
       // We are (roughly) following the AMQP RPC pattern
       // See https://www.rabbitmq.com/tutorials/tutorial-six-java.html for more details.
       theChannel.queueDeclare(rpcQueue, queueDurability, false, false, null);
       theChannel.basicQos(1);

       // Create the consumer
       consumer = new QueueingConsumer(theChannel);
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
      *  @param  headers The headers for the message
      *  @param  persistence Whether or not to set MessageProperties.PERSISTENT_TEXT_PLAIN in the message.
      */
     public void publishMessage(AMQPMessage theMessage, String headers, boolean persistence) {
         try {
   
            String[] splitHeaders = headers.split(";");
            for (String thisHeader : splitHeaders) {
                // Add a map entry for each of these headers
                String[] thisSplitHeader = thisHeader.split(":", 2);
                publishMap.put(thisSplitHeader[0], thisSplitHeader[1]);
                System.out.println("Adding headers: " + thisSplitHeader[0] + " " + thisSplitHeader[1]);
            }

            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
       
            if (persistence) {
               // MessageProperties.PERSISTENT_TEXT_PLAIN is a static instance of AMQP.BasicProperties
               // that contains a delivery mode and a priority. So we pass them to the builder.
               builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
               builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
            }

            // add the "RPC" properties
            builder.correlationId(theMessage.getTag());

            // Add the headers to the builder.
            builder.headers(publishMap);
       
            // Use the builder to create the BasicProperties object.
            AMQP.BasicProperties theProps = builder.build();
            String stringMessage = new String(theMessage.getBytes());

            logger.log(Level.INFO, "publishing message " + stringMessage + 
                                   " to queue: " + theMessage.getReplyQueue());
            theChannel.basicPublish("", theMessage.getReplyQueue(), theProps, theMessage.getBytes());
   
         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
            System.exit(3);
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
            System.exit(4);
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
             theTag = theChannel.basicConsume(rpcQueue, false, consumer);

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

             // save the reply tag for later
             thisMessage.setReplyQueue(delivery.getProperties().getReplyTo());

             // Save the delivery tag for later
             thisMessage.setDeliveryTag(delivery.getEnvelope().getDeliveryTag());

             // Set the ordinary tag for later
             thisMessage.setTag(delivery.getProperties().getCorrelationId());

             // Turn off the consumer till the next time
             theChannel.basicCancel(theTag);

         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
            System.exit(2);
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
             theTag = theChannel.basicConsume(rpcQueue, true, consumer);

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
  
                // save the reply tag for later
                thisMessage.setReplyQueue(delivery.getProperties().getReplyTo());

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
            System.exit(1);
         }

         // let's not forget to return the received message or null.
         return thisMessage;
     }
}
