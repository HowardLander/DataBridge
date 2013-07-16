package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import java.io.File;

public class RMQListener{

  private final static String QUEUE_NAME="hello";
  private final static String OUT_QUEUE="update";

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
      Runtime.getRuntime().exec("/Users/Ren/Documents/dbspace/server/runHandler");
      channel.basicPublish("", OUT_QUEUE, null, message.getBytes());
      System.out.println("forwarded " + message);
    }
  }
}
