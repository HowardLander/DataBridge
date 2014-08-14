package org.renci.databridge.util;
import com.rabbitmq.client.*;
import java.util.*;
import java.lang.InterruptedException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This is intended to handle all communication tasks for an AMQP based 
 * communications network for the DataBridge project. As of this writing
 * our network consists of a header based exchange for interchanging messages
 * amongst various system components. In the AMQP model, messages are sent to
 * an "exchange" whose responsibility is to distribute these messages as appropriate
 * to the correct set of queues.  In our case, we are going to use a header based 
 * exchange with each system component creating it's own queue and asserting the set
 * of headers it's interested in.  Messages therefore have to be sent to the exchange 
 * with one or more headers. It's up to the producing application to specify the headers, either hard
 * coded or from a properties file.  Each consumer specifies the headers set that it's interested in.
 * Note that since each instance of each consuming application will have it's own set of queues,
 * more than one application instance can receive each message.  In fact this is the behavior
 * we want in the DataBridge.

 * The idea for this
 * class is to abstract as much detail as possible from the Databridge system
 * components  so that we can change the underlying comm architecture without
 * requiring code changes in the components. 
 *
 * The issues of concurrency and message ordering are not currently addressed in 
 * this class.  That may have to change...
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class AMQPComms {

     /** The AMQP connection for this comms object */
     private Connection theConnection;

     /** The AMOP Channel for this comms object */
     private Channel theChannel;

     /** The AMOP Queue for this comms object */
     private String primaryQueue;

     /** The consumer for this queue */
     private QueueingConsumer consumer;

     /** Header map for publishing message */
     private Map<String, Object> publishMap = new HashMap<String, Object>();

     /** Header map for publishing message */
     private String publishHeaders;

     /** Header map for publishing message */
     private String receiveHeaders;

     // The API requires a routing key, but in fact if you are using a header exchange the
     // value of the routing key is not used in the routing. So we define a dummy key
     // Note that the applications can still pass information between publishers and 
     // receivers in the routing key.
     String routingKey = "unused";

     /** The tag for this consumer */
     private String theTag;

     /** The durability property for the main queue */
     private boolean queueDurability;

     /** The AMQP hostname for this comms object */
     private String theHost;

     /** The AMQP exchange for this comms object */
     private String theExchange;

     /** The logging level for this logger.  Any message
         <= this level will be queued*/
     private int theLevel = 4;

     /** The static logging levels */
     public static final int LOG_EMERG =   0;
     public static final int LOG_ALERT =   1;
     public static final int LOG_CRIT  =   2;
     public static final int LOG_ERR   =   3;
     public static final int LOG_WARNING = 4;
     public static final int LOG_NOTICE =  5;
     public static final int LOG_INFO =    6;
     public static final int LOG_DEBUG =   7;

     /** The log topic */
     public static final String DATABRIDGE_LOG_TOPIC = "databridge.log";

     /** The log key */
     public static final String DATABRIDGE_LOG_KEY = "databridge.log";

     /** The log value */
     public static final String DATABRIDGE_LOG_VALUE = "databridge.log";

     /**
      * AMQPComms constructor with no arguments.
      */
     public AMQPComms() {
     }

     /**
      * Convenience constructor to load properties from a file path.
      */
     public AMQPComms (String propFilePath) {
       try {
         Properties p = new Properties ();
         p.load (new FileInputStream (propFilePath));
         init (p);
       } catch (Exception e) {
         e.printStackTrace ();
       }
     }

     /**
      * Convenience method to load properties from an InputStream.
      */
     public AMQPComms (InputStream propInputStream) throws IOException {
       Properties p = new Properties ();
       p.load (propInputStream);
       init (p);
     }

     /**
      * AMQPComms constructor with a properties file to read.
      *
      *  @param  prop  The prop file to read to configure the communication channel. The property
      *                    file has to define at least the following properties: 
      *                    org.renci.databridge.primaryQueue, org.renci.databridge.exchange and
      *                    org.renci.databridge.queueHost.  Other relevant properties are
      *                    org.renci.databridge.queueDurability and org.renci.databridge.logLevel
      */
     public AMQPComms (Properties prop) throws IOException {
       init (prop);
     }

     protected void init (Properties prop) throws IOException {
       primaryQueue = prop.getProperty("org.renci.databridge.primaryQueue", "primary");
       theHost = prop.getProperty("org.renci.databridge.queueHost", "localhost");
       theExchange = prop.getProperty("org.renci.databridge.exchange", "localhost");
       theLevel = Integer.parseInt(prop.getProperty("org.renci.databridge.logLevel", "4"));
       queueDurability = Boolean.parseBoolean(prop.getProperty("org.renci.databridge.queueDurability", "false"));
       // These headers are a single string that contains multiple key value pairs. The format is key1:value1;key2:value2 etc
       publishHeaders = prop.getProperty("org.renci.databridge.publishHeaders", "localhost");
       receiveHeaders = prop.getProperty("org.renci.databridge.receiveHeaders", "localhost");

       // Here's the Rabbit specific code.
       ConnectionFactory theFactory = new ConnectionFactory();
       theFactory.setHost(theHost);
       theConnection = theFactory.newConnection();
       theChannel = theConnection.createChannel();

       // Declare a queue to be the main queue.  If the queue is durable and messages sent to it
       // are also durable, then those messages will be recieved by the app at start time, even
       // if they were queued before the app was started.
       theChannel.queueDeclare(primaryQueue, queueDurability, false, false, null);

       // Declare a headers exchange.  Note that the declaration of the exchange is idempotent,
       // so at execution time this will primarily makes sure the exchange is of the expected
       // type.  If the exchange exists and is not a headers exchange, an error will be thrown.
       // Note also that we are declaring the exchange as durable, without checking for a 
       // property.  We don't want various apps to declare this differently, but we do (I think)
       // want the exchange to be durable.
       theChannel.exchangeDeclare(theExchange, "headers", true);

       // Create the consumer
       consumer = new QueueingConsumer(theChannel);
     }

     /**
      *  Code to publish a message 
      *
      *  @param  theMessage The user provided AMQPMessage.
      *  @param  persistence Whether or not to set MessageProperties.PERSISTENT_TEXT_PLAIN in the message.
      */
     public void publishMessage(AMQPMessage theMessage, String headers, Boolean persistence) {
         try {
            String[] splitHeaders = headers.split(";");
   
            for (String thisHeader : splitHeaders) {
                // Add a map entry for each of these headers
                String[] thisSplitHeader = thisHeader.split(":");
                publishMap.put(thisSplitHeader[0], thisSplitHeader[1]);
            }
   
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
       
            if (persistence) {
               // MessageProperties.PERSISTENT_TEXT_PLAIN is a static instance of AMQP.BasicProperties
               // that contains a delivery mode and a priority. So we pass them to the builder.
               builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
               builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
            }
       
            // Add the headers to the builder.
            builder.headers(publishMap);
       
            // Use the builder to create the BasicProperties object.
            AMQP.BasicProperties theProps = builder.build();

            theChannel.basicPublish(theExchange, routingKey, theProps, theMessage.getBytes());
   
         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }
     }

     /**
      *  This method publishs a message using the DATABRIDGE_LOG_TOPIC. This task could be handled with
      *  the normal publish message code, but because we want formatting and some of the stack trace
      *  we provide a dedicated method.
      *
      *  @param  thisLevel The level for this message provided message.
      *  @param  theMessage The user provided message.
      *  @param  headers The headers to add to the message
      *  @param  persistence Whether or not to set MessageProperties.PERSISTENT_TEXT_PLAIN in the message.
      */
     public void publishLog(int thisLevel, String theMessage, String headers, Boolean persistence) {

         System.out.println("thisLevel: " + thisLevel + " theLevel: "  + theLevel);
         if (thisLevel <= theLevel) {
            try {
               String[] splitHeaders = headers.split(";");
   
               for (String thisHeader : splitHeaders) {
                   // Add a map entry for each of these headers
                   String[] thisSplitHeader = thisHeader.split(":");
                   publishMap.put(thisSplitHeader[0], thisSplitHeader[1]);
               }

               // Let's add the DataBridge log key value header
               publishMap.put(DATABRIDGE_LOG_KEY, DATABRIDGE_LOG_VALUE);
   
               AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
          
               if (persistence) {
                  // MessageProperties.PERSISTENT_TEXT_PLAIN is a static instance of AMQP.BasicProperties
                  // that contains a delivery mode and a priority. So we pass them to the builder.
                  builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
                  builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
               }
          
               // Add the headers to the builder.
               builder.headers(publishMap);
          
               // Use the builder to create the BasicProperties object.
               AMQP.BasicProperties theProps = builder.build();

               // Let's format a date and time
               Formatter myFormatter = new Formatter();
               String myDate = myFormatter.format("%tc", new Date()).toString();
   
               // We also want some stack info
               String stackInfo = new Throwable().getStackTrace()[1].getFileName() + ":" + 
                                  new Throwable().getStackTrace()[1].getLineNumber();
       
               String formattedMessage = new String("    " + theMessage);
               String logMessage = myDate + " " + stackInfo;
               
               System.out.println("Publishing: " + logMessage);
               theChannel.basicPublish(theExchange, DATABRIDGE_LOG_TOPIC, theProps, logMessage.getBytes());
               System.out.println("Publishing: " + formattedMessage);
               theChannel.basicPublish(theExchange, DATABRIDGE_LOG_TOPIC, theProps, formattedMessage.getBytes());
   
            } catch (Exception e){
               // Not much we can do with the exception ...
               e.printStackTrace();
            }
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
     
     /**
      *  Bind the receive queue to the given headers
      *
      *  @param  headers a string of headers in the format key1:value1;key2:value2
      */
     public void bindTheQueue(String headers) {
         Map<String, Object> receiveMap = new HashMap<String, Object>();

         try {
             String[] splitHeaders = headers.split(";");

             for (String thisHeader : splitHeaders) {
                 // Add a map entry for each of these headers
                 String[] thisSplitHeader = thisHeader.split(":");
                 receiveMap.put(thisSplitHeader[0], thisSplitHeader[1]);
             }

             // Bind the queue and the exchange to every header in the list
             theChannel.queueBind(primaryQueue, theExchange, routingKey, receiveMap);

         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }
     }

     
     /**
      *  Unbind the receive queue from the  given headers
      *
      *  @param  headers a string of headers in the format key1:value1;key2:value2
      */
     public void unbindTheQueue(String headers) {
         Map<String, Object> receiveMap = new HashMap<String, Object>();

         try {
             String[] splitHeaders = headers.split(";");

             for (String thisHeader : splitHeaders) {
                 // Add a map entry for each of these headers
                 String[] thisSplitHeader = thisHeader.split(":");
                 receiveMap.put(thisSplitHeader[0], thisSplitHeader[1]);
             }

             // Unbind the queue and the exchange from every header in the list
             theChannel.queueUnbind(primaryQueue, theExchange, routingKey, receiveMap);

         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }
     }

     /**
      * Get theConnection.
      *
      * @return theConnection as Connection.
      */
     public Connection getTheConnection()
     {
         return theConnection;
     }
     
     /**
      * Set theConnection.
      *
      * @param theConnection the value to set.
      */
     public void setTheConnection(Connection theConnection)
     {
         this.theConnection = theConnection;
     }
     
     /**
      * Get theChannel.
      *
      * @return theChannel as Channel.
      */
     public Channel getTheChannel()
     {
         return theChannel;
     }
     
     /**
      * Set theChannel.
      *
      * @param theChannel the value to set.
      */
     public void setTheChannel(Channel theChannel)
     {
         this.theChannel = theChannel;
     }
     
     /**
      * Get theHost.
      *
      * @return theHost as String.
      */
     public String getTheHost()
     {
         return theHost;
     }
     
     /**
      * Set theHost.
      *
      * @param theHost the value to set.
      */
     public void setTheHost(String theHost)
     {
         this.theHost = theHost;
     }
     
     /**
      * Get theExchange.
      *
      * @return theExchange as String.
      */
     public String getTheExchange()
     {
         return theExchange;
     }
     
     /**
      * Set theExchange.
      *
      * @param theExchange the value to set.
      */
     public void setTheExchange(String theExchange)
     {
         this.theExchange = theExchange;
     }
     
     /**
      * Get level.
      *
      * @return level as int.
      */
     public int getTheLevel()
     {
         return theLevel;
     }
     
     /**
      * Set level.
      *
      * @param theLevel the value to set.
      */
     public void setTheLevel(int theLevel)
     {
         this.theLevel = theLevel;
     }
     
     /**
      * Get primaryQueue.
      *
      * @return primaryQueue as String.
      */
     public String getPrimaryQueue()
     {
         return primaryQueue;
     }
     
     /**
      * Set primaryQueue.
      *
      * @param primaryQueue the value to set.
      */
     public void setPrimaryQueue(String primaryQueue)
     {
         this.primaryQueue = primaryQueue;
     }
     
     /**
      * Get queueDurability.
      *
      * @return queueDurability as boolean.
      */
     public boolean getQueueDurability()
     {
         return queueDurability;
     }
     
     /**
      * Set queueDurability.
      *
      * @param queueDurability the value to set.
      */
     public void setQueueDurability(boolean queueDurability)
     {
         this.queueDurability = queueDurability;
     }
     

     /**
      * Get publishMap.
      *
      * @return publishMap as a Map<String, Object>
      */
     public Map<String, Object> getPublishMap()
     {
         return publishMap;
     }
     
     /**
      * Set publishMap.
      *
      * @param publishMap the value to set.
      */
     public void setPublishMap(Map<String, Object> publishMap)
     {
         this.publishMap = publishMap;
     }

     /**
      * Get publishHeaders.
      *
      * @return publishHeaders as String.
      */
     public String getPublishHeaders()
     {
         return publishHeaders;
     }
     
     /**
      * Set publishHeaders.
      *
      * @param publishHeaders the value to set.
      */
     public void setPublishHeaders(String publishHeaders)
     {
         this.publishHeaders = publishHeaders;
     }
     
     /**
      * Get receiveHeaders.
      *
      * @return receiveHeaders as String.
      */
     public String getReceiveHeaders()
     {
         return receiveHeaders;
     }
     
     /**
      * Set receiveHeaders.
      *
      * @param receiveHeaders the value to set.
      */
     public void setReceiveHeaders(String receiveHeaders)
     {
         this.receiveHeaders = receiveHeaders;
     }
     
     /**
      * Get routingKey.
      *
      * @return routingKey as String.
      */
     public String getRoutingKey()
     {
         return routingKey;
     }
     
     /**
      * Set routingKey. Note that the AMQP API requires a routing key, but in fact if you are 
      * using a headers exchange (as we are) the value of the routing key is not used in the routing. 
      * However applications can still pass information between publishers and
      * receivers in the routing key.
      *
      * @param routingKey the value to set.
      */
     public void setRoutingKey(String routingKey)
     {
         this.routingKey = routingKey;
     }
}
