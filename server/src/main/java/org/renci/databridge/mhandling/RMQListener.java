package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import java.io.File;

/**
 * Server class listening for incoming messages on rabbitMQ queue QUEUE_NAME
 * on receipt: creates a new instance of the MessageHandler to handle the message
 * and forwards the message to it along the OUT_QUEUE channel
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */

public class RMQListener{

  /** The queue on which to listen for incoming messages */
  private final static String QUEUE_NAME="hello";

  /** The queue on which to forward messages */
  private final static String OUT_QUEUE="update";

  /**
   * Main function starts listening on QUEUE_NAME, and loops indefinitely
   * waiting for anh messeges, forwarding them without modification
   */
  public static void main(String[] args) throws Exception{

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    //channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(QUEUE_NAME, true, consumer);

    while (true) {
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      String message = new String(delivery.getBody());
      System.out.println("recieved " + message);
      Runtime.getRuntime().exec("mvn -e exec:java -Dexec.mainClass='org.renci.databridge.mhandling.MessageHandler'");
      channel.basicPublish("", OUT_QUEUE, null, message.getBytes());
      System.out.println("forwarded " + message);
    }
  }
}
