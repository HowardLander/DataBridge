package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import java.lang.Thread;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.io.IOException;
import org.renci.databridge.util.*;

/**
 * This class receives messages from the RMQListener and uses a DBWriter
 * to store the information contained in the messages into a database
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */

public class MessageHandler<T> extends Thread{

  /** Queue from which to receive incoming messages */
  private final static String QUEUE_NAME = "update";

  private T       dbService;
  private String  logQueue;
  private Channel logChannel;
  private String  message;

  public void setB(T graphDB){
    dbService = graphDB;
  }

  public MessageHandler(T dbService, String _message, Channel _channel, String _logQueue){
    setB(dbService);
    message = _message;
    logChannel = _channel;
    logQueue = _logQueue;
  }
 
  /**
   * Main class receives 1 message from queue QUEUE_NAME and processes it
   * A new MessageHandler must be created for each message pushed onto
   * queue QUEUE_NAME, as each processes exactly 1 message
   */

  public void run(){
    try{
      exceptionThrowingRun();
    } catch (IOException e){
      //TODO: send message of type error
      e.printStackTrace();
    } catch(InterruptedException e){
      //TODO: send message of type error
      e.printStackTrace();
    }
  }
 
  private void exceptionThrowingRun() throws IOException, InterruptedException {
 
    AMQPLogger logger = new AMQPLogger();
    logger.setTheExchange("");
    logger.setTheChannel(logChannel);
    logger.setTheQueue(logQueue);
    
    logger.publish("Handler: initiated");

    String[] msgParts = message.split(":", 2);
    logger.publish("Handler: message split - size: " + msgParts.length);
    int msgType;
    if(msgParts.length < 2){
      msgType = MessageTypes.NONE;
      message = msgParts[0];
    }
    else{
      msgType = new Integer(msgParts[0]);
      message = msgParts[1];
    }

    logger.publish("Handler: message type determined - " + msgType);

    BaseHandler handler;
    switch(msgType){
      case MessageTypes.NONE:
        logger.publish("Handler: No message type found: Ensure message type is defined for all messages in the form '{type}:{message}'");
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
        logger.publish("Handler: Unknown message type : " + msgType);
        return;
    }

    logger.publish("Handler: BaseHandler initiated");

    try{
      handler.handle(message, logger);
    }
    catch(Exception e){
      e.printStackTrace();
      String trace = e.toString();
      for(int i = 0; i < e.getStackTrace().length; i++){
        trace += "\n" + e.getStackTrace()[i].toString();
      }
      logger.publish("Handler: ERROR " + trace);
    }
  }
}
