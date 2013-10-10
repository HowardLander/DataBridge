package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import java.lang.Exception;
import org.renci.databridge.util.*;

/**
 * This class recieves messages from the RMQListener and uses a DBWriter
 * to store the information contained in the messages into a database
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */

public class MessageHandler<T> implements Runnable{

  /** Queue from which to recieve incoming messages */
  private final static String QUEUE_NAME = "update";

  /** Queue on which to send log messages */
  private final static String LOG_QUEUE = "log";

  private T dbService;

  public void setB(T graphDB){
    dbService = graphDB;
  }

  public MessageHandler(T dbService){
    setB(dbService);
  }
 
  /**
   * Main class receives 1 message from queue QUEUE_NAME and processes it
   * A new MessageHandler must be created for each message pushed onto
   * queue QUEUE_NAME, as each processes exactly 1 message
   */
  //public static void main(String[] args) throws Exception{

  public void run(){
    //Set up connaction to rabbitMQ server
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: initiated").getBytes());

    //Set to retrieve only one message from queue
    channel.basicQos(1);

    //Consume message and store file information
    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(QUEUE_NAME, false, consumer);
    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: msg recieved").getBytes());
    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
 
    String msg = new String(delivery.getBody());
    String[] msgParts = msg.split(":", 2);
    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: message split - size: " + msgParts.length).getBytes());
    int msgType;
    if(msgParts.length < 2){
      msgType = MessageTypes.NONE;
      msg = msgParts[0];
    }
    else{
      msgType = new Integer(msgParts[0]);
      msg = msgParts[1];
    }

    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: message type determined - " + msgType).getBytes());

    BaseHandler handler;
    switch(msgType){
      case MessageTypes.NONE:
        channel.basicPublish("", LOG_QUEUE, null, new String("Handler: No message type found: Ensure message type is defined for all messages in the form '{type}:{message}'").getBytes());
        return;
      case MessageTypes.NETWORK:
        handler = new NetworkHandler<T>(dbService);
      break;
      case MessageTypes.JSONREQUEST:
        handler = new JSONHandler();
      break;
      case MessageTypes.ERROR:
        handler = new ErrorHandler();
      break;
      default:
        channel.basicPublish("", LOG_QUEUE, null, new String("Handler: Unknown message type : " + msgType).getBytes());
        return;
    }

    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: BaseHandler initiated").getBytes());

    try{
      handler.handle(msg, channel, LOG_QUEUE);
    }
    catch(Exception e){
      e.printStackTrace();
      String trace = e.toString();
      for(int i = 0; i < e.getStackTrace().length; i++){
        trace += "\n" + e.getStackTrace()[i].toString();
      }
      channel.basicPublish("", LOG_QUEUE, null, new String("Handler: ERROR " + trace).getBytes());
    }
   
    channel.close();
    connection.close();

  }
}
