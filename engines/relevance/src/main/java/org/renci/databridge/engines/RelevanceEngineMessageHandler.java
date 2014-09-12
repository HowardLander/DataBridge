package org.renci.databridge.engines;
import com.rabbitmq.client.*;
import java.lang.Thread;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.io.IOException;
import org.renci.databridge.util.*;
import java.util.Map;
import java.util.HashMap;


/**
 * This class is executed in a thread of the Relevance Engine. The Relevance Engine
 * calls the constructor for this class with the AMQP message as a parameter.  It's
 * up to this class to decode the message according to the headers and implement the
 * required behaviors.
 *
 * @author Howard Lander -RENCI (www.renci.org)
 */

public class RelevanceEngineMessageHandler extends Thread {

  // These are the individual portions of the message.
  // The routing key.
  private String routingKey;

  // The properties class.
  private com.rabbitmq.client.AMQP.BasicProperties properties;

  // The headers, expressed as a map of strings.
  private Map<String, String> stringHeaders;

  // The byte array for the contents of the message.
  private byte[] bytes;
  
  public RelevanceEngineMessageHandler(AMQPMessage theMessage){
      // Get the individual components of the the message and store
      // them in the fields
      routingKey = theMessage.getRoutingKey();
      properties = theMessage.getProperties();
      stringHeaders = theMessage.getStringHeaders();
      bytes = theMessage.getBytes();
  }
 
  public void run(){
    try{
      doRun();
    } catch (IOException e){
      //TODO: send message of type error
      e.printStackTrace();
    } catch(InterruptedException e){
      //TODO: send message of type error
      e.printStackTrace();
    }
  }
 
  private void doRun() throws IOException, InterruptedException {
 
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
   * Set routingKey.
   *
   * @param routingKey the value to set.
   */
  public void setRoutingKey(String routingKey)
  {
      this.routingKey = routingKey;
  }

/**
 * Get properties.
 *
 * @return properties as BasicProperties.
 */
public com.rabbitmq.client.AMQP.BasicProperties getProperties()
{
    return properties;
}

/**
 * Set properties.
 *
 * @param properties the value to set.
 */
public void setProperties(com.rabbitmq.client.AMQP.BasicProperties properties)
{
    this.properties = properties;
}
  
  /**
   * Get bytes.
   *
   * @return bytes as byte[].
   */
  public byte[] getBytes()
  {
      return bytes;
  }
  
  /**
   * Get bytes element at specified index.
   *
   * @param index the index.
   * @return bytes at index as byte.
   */
  public byte getBytes(int index)
  {
      return bytes[index];
  }
  
  /**
   * Set bytes.
   *
   * @param bytes the value to set.
   */
  public void setBytes(byte[] bytes)
  {
      this.bytes = bytes;
  }
  
  /**
   * Set bytes at the specified index.
   *
   * @param bytes the value to set.
   * @param index the index.
   */
  public void setBytes(byte bytes, int index)
  {
      this.bytes[index] = bytes;
  }
}
