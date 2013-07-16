package org.renci.databridge.mhandling;

import java.io.*;
import java.net.*;
import com.rabbitmq.client.*;
import org.renci.databridge.database.*;
import org.msgpack.MessagePack;

public class MessageHandler {

  public static class PackFile {
    public static final int NETWORK = 1;
    public int fileType;
    public byte[] file;
  }

  public static class PackNetwork {
    public DBNode[] nodes;
    public DBEdge[] edges;
  }

  private final static String QUEUE_NAME = "update";

  public static void main(String[] args) throws Exception{

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.basicQos(1);

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(QUEUE_NAME, false, consumer);
    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    String fileLoc = new String(delivery.getBody());
    
    channel.close();
    connection.close();

    URL fileURL = new URL(fileLoc);
    byte[] bytes = new byte[2048];
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    InputStream in = fileURL.openStream();
    int i = 0;
    while(in.read(bytes) != -1){
      buf.write(bytes, i * 2048, 2048);
      bytes = new byte[2048];
    }
    MessagePack msgpack = new MessagePack();
    msgpack.register(DBNode.class);
    msgpack.register(DBEdge.class);
    msgpack.register(PackFile.class);
    msgpack.register(PackNetwork.class);
    PackFile pf = msgpack.read(buf.toByteArray(), PackFile.class);

    switch(pf.fileType){
      case PackFile.NETWORK:
        PackNetwork pn = msgpack.read(pf.file, PackNetwork.class);
	DBWriter dbw = new DBWriterNeo4j();
        int index = 0;
        for(DBNode n : pn.nodes){
	  dbw.writeNode(n);
	}
	for(DBEdge e : pn.edges){
	  dbw.writeEdge(e);
        }
      break;
    }
  }
}
