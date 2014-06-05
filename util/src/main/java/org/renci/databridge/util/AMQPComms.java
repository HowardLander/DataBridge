package org.renci.databridge.util;
import com.rabbitmq.client.*;
import java.util.*;
import java.lang.InterruptedException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This is intended to handle all communication tasks for an AMQP based 
 * communications network for the DataBridge project. As of this writing
 * our network consists of a topic based exchange for interchanging messages
 * amongst various system components. In the AMQP model, messages are sent to
 * an "exchange" whose responsibility is to distribute these messages as appropriate
 * to the correct set of queues.  In our case, we are going to use a topic based 
 * exchange with each system component creating it's own queue and asserting the set
 * of topics it's interested in.  Messages therefore have to be sent to the exchange 
 * with a topic. It's up to the producing application to specify the topic, either hard
 * coded or from a properties file.  Each consumer specifies the topics it wants to receive.
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

     /** The durability property for the main queue */
     private boolean queueDurability;

     /** The AMOP logging Queue for this comms object */
     private String logQueue;

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

     /**
      * AMQPComms constructor with no arguments.
      */
     public AMQPComms() {
     }

     /**
      * AMQPComms constructor with a properties file to read.
      *
      *  @param  propFile  The prop file to read for the host, exchange, etc...
      */
     public AMQPComms(String propFile) {
         try {
             // Read the property file for the communication related properties
             Properties prop = new Properties();
             prop.load(new FileInputStream(propFile));
             primaryQueue = prop.getProperty("org.renci.databridge.primaryQueue", "primary");
             logQueue = prop.getProperty("org.renci.databridge.logQueue", "log");
             theHost = prop.getProperty("org.renci.databridge.queueHost", "localhost");
             theExchange = prop.getProperty("org.renci.databridge.exchange", "localhost");
             theLevel = Integer.parseInt(prop.getProperty("org.renci.databridge.logLevel", "4"));
             queueDurability = Boolean.parseBoolean(prop.getProperty("org.renci.databridge.queueDurability", "false"));

             // Here's the Rabbit specific code.
             ConnectionFactory theFactory = new ConnectionFactory();
             theFactory.setHost(theHost);
             theConnection = theFactory.newConnection();
             theChannel = theConnection.createChannel();

             // Declare a queue to be the main queue.  If the queue is durable and messages sent to it
             // are also durable, then those messages will be recieved by the app at start time, even
             // if they were queued before the app was started.
             theChannel.queueDeclare(primaryQueue, queueDurability, false, false, null);

             // Declare a topic exchange.  Note that the declaration of the exchange is idempotent,
             // so at execution time this will primarily makes sure the exchange is of the expected
             // type.  If the exchange exists and is not a topic exchange, an error will be thrown.
             // Note also that we are declaring the exchange as durable, without checking for a 
             // property.  We don't want various apps to declare this differently, but we do (I think)
             // want the exchange to be durable.
             theChannel.exchangeDeclare(theExchange, "topic", true);
         } catch (Exception e){
            e.printStackTrace();
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
            // Resolve the persistence flag.
            AMQP.BasicProperties theProp = null;
            if (persistence) {
                theProp = MessageProperties.PERSISTENT_TEXT_PLAIN;
            }
           
            theChannel.basicPublish(theExchange, theMessage.getTopic(), theProp, theMessage.getBytes());
   
         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }
     }

     /**
      *  This method publishs a message using the DATABRIDGE_LOG_TOPIC. This task could be handled with
      *  the normal publish message code, but because we want formatting, some of the stack trace
      *  and a defined log topic, we provide a dedicated method.
      *
      *  @param  thisLevel The level for this message provided message.
      *  @param  theMessage The user provided message.
      *  @param  persistence Whether or not to set MessageProperties.PERSISTENT_TEXT_PLAIN in the message.
      */
     public void publishLog(int thisLevel, String theMessage, Boolean persistence) {
         if (thisLevel <= theLevel) {
            try {
               // Resolve the persistence flag.
               AMQP.BasicProperties theProp = null;
               if (persistence) {
                   theProp = MessageProperties.PERSISTENT_TEXT_PLAIN;
               }
              
               // Let's format a date and time
               Formatter myFormatter = new Formatter();
               String myDate = myFormatter.format("%tc", new Date()).toString();
   
               // We also want some stack info
               String stackInfo = new Throwable().getStackTrace()[1].getFileName() + ":" + 
                                  new Throwable().getStackTrace()[1].getLineNumber();
       
               String formattedMessage = new String("    " + theMessage);
               String logMessage = myDate + " " + stackInfo;
               
               theChannel.basicPublish(theExchange, DATABRIDGE_LOG_TOPIC, theProp, logMessage.getBytes());
               theChannel.basicPublish(theExchange, DATABRIDGE_LOG_TOPIC, theProp, formattedMessage.getBytes());
   
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
      *  @param  topics An ArrayList of topics of interest.
      */
     public AMQPMessage receiveMessage(ArrayList<String> topics) {

         AMQPMessage thisMessage = new AMQPMessage();

         try {
             for (String thisTopic : topics) {
                 // Bind the queue and the exchange to every topic in the list
                 theChannel.queueBind(primaryQueue, theExchange, thisTopic);
             }

             // Create the consumer
             QueueingConsumer consumer = new QueueingConsumer(theChannel);
 
             // Start the consumer
             theChannel.basicConsume(primaryQueue, true, consumer);

             // Wait for message to arrive
             QueueingConsumer.Delivery delivery = consumer.nextDelivery();

             // Build the AMQPMessage to return
             thisMessage.setTopic(delivery.getEnvelope().getRoutingKey());
             thisMessage.setBytes(delivery.getBody());

             // Now we unbind the topics, since the next request could have a different set
             for (String thisTopic : topics) {
                 // Bind the queue and the exchange to every topic in the list
                 theChannel.queueUnbind(primaryQueue, theExchange, thisTopic);
             }

         } catch (Exception e){
            // Not much we can do with the exception ...
            e.printStackTrace();
         }

         // let's not forget to return the received message.
         return thisMessage;
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
      * @param level the value to set.
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
      * Get logQueue.
      *
      * @return logQueue as String.
      */
     public String getLogQueue()
     {
         return logQueue;
     }
     
     /**
      * Set logQueue.
      *
      * @param logQueue the value to set.
      */
     public void setLogQueue(String logQueue)
     {
         this.logQueue = logQueue;
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
}
