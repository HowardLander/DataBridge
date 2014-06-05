package org.renci.databridge.util;

/**
 * This class holds the message type and byte array for a 
 * message sent over the AMQP system. It is used for both sending
 * and receiving messages in the AMQPComms class.
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class AMQPMessage {

     /** The message topic */
     private String topic;

     /** The actual message */
     private byte[] bytes;

     /**
      * Default constructor with no arguments
      *
      */
     public AMQPMessage() {
     }


     /**
      * Constructor with the message and topic
      *
      *  @param  theTopic  The topic of the message
      *  @param  theBytes A byte array containing the actual message
      */
     public AMQPMessage(String theTopic, byte[] theBytes) {
         topic = theTopic;
         bytes = theBytes;
     }

     
     /**
      * Get topic.
      *
      * @return topic as String.
      */
     public String getTopic()
     {
         return topic;
     }
     
     /**
      * Set topic.
      *
      * @param topic the value to set.
      */
     public void setTopic(String topic)
     {
         this.topic = topic;
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
}
