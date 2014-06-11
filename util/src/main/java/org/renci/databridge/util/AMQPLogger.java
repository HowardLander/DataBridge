package org.renci.databridge.util;
import com.rabbitmq.client.*;
import java.util.*;
import java.lang.InterruptedException;
import java.io.IOException;

/**
 * This class writes diagnostic data to a AMQP based log queue.  We are currently
 * using RabbitMQ.
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class AMQPLogger {

     /** The AMQP connection for this logger */
     private Connection theConnection;

     /** The AMOP Channel for this logger */
     private Channel theChannel;

     /** The AMOP Queue for this logger */
     private String theQueue;

     /** The AMQP hostname for this logger */
     private String theHost;

     /** The AMQP exchange for this logger */
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

     /**
      * AMQPLogger constructor with no arguments.
      */
     public AMQPLogger() {
     }

     /**
      * AMQP constructor with host and queue name;
      *
      *  @param  _theQueue The queue for this logger
      *  @param  _theHost  The host for this logger
      *  @param  _theExchange  The exchange for this logger, often set to ""
      *  @param  _theLevel  The level for the logger
      */
     public AMQPLogger(String _theQueue, String _theHost, String _theExchange, int _theLevel) {
         theHost = _theHost;
         theQueue = _theQueue;
         theExchange = _theExchange;
         theLevel = _theLevel;

         try {
             ConnectionFactory theFactory = new ConnectionFactory();
             theFactory.setHost(theHost);
             theConnection = theFactory.newConnection();
             theChannel = theConnection.createChannel();
         } catch (Exception e){
            e.printStackTrace();
         }
     }

     /**
      *  Code to publish a message to the log queue.
      *
      *  @param  thisLevel The level for this message provided message.
      *  @param  theMessage The user provided message.
      */
     public void publish(int thisLevel, String theMessage) {
         if (thisLevel <= theLevel) {
            try {
               // Let's format a date and time
               Formatter myFormatter = new Formatter();
               String myDate = myFormatter.format("%tc", new Date()).toString();
   
               // We also want some stack info
               String stackInfo = new Throwable().getStackTrace()[1].getFileName() + ":" + 
                                  new Throwable().getStackTrace()[1].getLineNumber();
       
               String formattedMessage = new String("    " + theMessage);
               String logMessage = myDate + " " + stackInfo;
               theChannel.basicPublish(theExchange, theQueue, null, logMessage.getBytes());
               theChannel.basicPublish(theExchange, theQueue, null, formattedMessage.getBytes());
   
            } catch (Exception e){
               // Not much we can do with the exception ...
               e.printStackTrace();
            }
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
      * Get theQueue.
      *
      * @return theQueue as String.
      */
     public String getTheQueue()
     {
         return theQueue;
     }
     
     /**
      * Set theQueue.
      *
      * @param theQueue the value to set.
      */
     public void setTheQueue(String theQueue)
     {
         this.theQueue = theQueue;
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
}
