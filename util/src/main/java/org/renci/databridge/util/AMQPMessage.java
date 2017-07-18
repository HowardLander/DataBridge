package org.renci.databridge.util;
import org.renci.databridge.message.*;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.util.Map;
import java.util.HashMap;

/**
 * This class holds the message type and byte array for a 
 * message sent over the AMQP system. It is used for both sending
 * and receiving messages in the AMQPComms class.
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class AMQPMessage {

     /** The routing key */
     private String routingKey;

     /** The message properties */
     private BasicProperties properties;

     /** The replyQueue */
     private String replyQueue;

     /** The actual message */
     private byte[] bytes;

     // The string version of the contents of the message.
     private String content = null;

     // The delivery tag from the envelope
     private long deliveryTag = 0;

     // The tag from the correlationId field
     private String tag;

     // The AMQPDirectComms object that produced this message.  Needed for ack/reply
     private AMQPDirectComms comms = null;

     /**
      * Default constructor with no arguments
      *
      */
     public AMQPMessage() {
     }


     /**
      * Constructor with the message as bytes
      *
      *  @param  theBytes A byte array containing the actual message
      */
     public AMQPMessage( byte[] theBytes) {
         bytes = theBytes;
     }

     
     /**
      * Get bytes for the message.
      *
      * @return message as byte[].
      */
     public byte[] getBytes()
     {
         return bytes;
     }
     
     /**
      * Get message element at specified index.
      *
      * @param index the index.
      * @return message at index as byte.
      */
     public byte getBytes(int index)
     {
         return bytes[index];
     }
     
     /**
      * Set message.
      *
      * @param message the value to set.
      */
     public void setBytes(byte[] message)
     {
         this.bytes = message;
     }
     
     /**
      * Set message at the specified index.
      *
      * @param message the value to set.
      * @param index the index.
      */
     public void setBytes(byte message, int index)
     {
         this.bytes[index] = message;
     }

     /**
      * Get properties.
      *
      * @return properties as AMQP.BasicProperties.
      */
     public BasicProperties getProperties()
     {
         return properties;
     }
     
     /**
      * Set properties.
      *
      * @param properties the value to set.
      */
     public void setProperties(BasicProperties properties)
     {
         this.properties = properties;
     }

     /**
      * Get Headers.
      *
      * @return headers as a Map<java.lang.String,java.lang.Object>
      */
     public Map<java.lang.String,java.lang.Object> getHeaders()
     {
         return properties.getHeaders();
     }

     /**
      * Get String Headers.
      *
      * @return headers whose value is a string as a Map<java.lang.String,java.lang.String>
      */
     public Map<String,String> getStringHeaders()
     {
         // Note that we expect all of the headers to have string values, but we want the user
         // to be able to depend on it. At the moment we are just going to drop any headers
         // with non-string valuse, but we could just as easily convert those values to string.
         Map<String, String> stringHeaders = new HashMap<String, String>();

         // Get the map<String,Object>
         Map<String,Object> props = properties.getHeaders();
         System.out.println("Getting ready to map properties");

         for (Map.Entry<String, Object> entry : props.entrySet()){
              // Note that this is assuming there is a useful toString method.  Rabbit seems
              // to be assuring that this is true, and since all of our headers will initially be 
              // Strings, this should be OK.  But if this code starts to misbehave, check
              // the type of the value object. 
              stringHeaders.put(entry.getKey(), entry.getValue().toString());
         }
         return stringHeaders;
     }

     /**
      * Validate the string headers against a map of desired headers. Return a string indicating
      * either success or failure. If it's success the 
      * values are returned in the "value" portion of the validation Map
      *
      * @param stringHeaders The set of headers to validate
      * @param validationMap The set of headers to validate
      *
      * @return either DatabridgeMessage.STATUS_OK or DatabridgeMessage.STATUS_ERROR plus a message
      * detailing which of the validation headers is missing.
      */
     public String validateStringContent(Map<String,String> stringHeaders, Map<String,String> validationMap)
     {
        String successReturn = DatabridgeMessage.STATUS_OK;
        String failureReturn = DatabridgeMessage.STATUS_ERROR + 
           ": The message is missing the following required fields: ";
        String returnValue = successReturn;
        // Loop through all of the keys in the validation map.
        for (String validationKey : validationMap.keySet()) {
           String validationValue = stringHeaders.get(validationKey);
           if (null == validationValue) {
              failureReturn = failureReturn + " " + validationKey;
              returnValue = failureReturn; 
           } else {
             // copy the value from the stringHeaders to the validationMap
             validationMap.put(validationKey, validationValue);
           }
        }
        return returnValue;
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
      * Get content.
      *
      * @return content as String.
      */
     public String getContent()
     {
         return content;
     }
     
     /**
      * Set content.
      *
      * @param content the value to set.
      */
     public void setContent(String content)
     {
         this.content = content;
     }
     
     /**
      * Get deliveryTag.
      *
      * @return deliveryTag as long.
      */
     public long getDeliveryTag()
     {
         return deliveryTag;
     }
     
     /**
      * Set deliveryTag.
      *
      * @param deliveryTag the value to set.
      */
     public void setDeliveryTag(long deliveryTag)
     {
         this.deliveryTag = deliveryTag;
     }
     
     /**
      * Get tag.
      *
      * @return tag as String.
      */
     public String getTag()
     {
         return tag;
     }
     
     /**
      * Set tag.
      *
      * @param tag the value to set.
      */
     public void setTag(String tag)
     {
         this.tag = tag;
     }
     
     /**
      * Get comms.
      *
      * @return comms as AMQPDirectComms.
      */
     public AMQPDirectComms getDirectComms()
     {
         return comms;
     }
     
     /**
      * Set comms.
      *
      * @param comms the value to set.
      */
     public void setDirectComms(AMQPDirectComms comms)
     {
         this.comms = comms;
     }
     
     /**
      * Get replyQueue.
      *
      * @return replyQueue as String.
      */
     public String getReplyQueue()
     {
         return replyQueue;
     }
     
     /**
      * Set replyQueue.
      *
      * @param replyQueue the value to set.
      */
     public void setReplyQueue(String replyQueue)
     {
         this.replyQueue = replyQueue;
     }
}
