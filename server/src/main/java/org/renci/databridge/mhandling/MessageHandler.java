package org.renci.databridge.mhandling;

import java.io.*;
import java.net.*;
import java.util.*;
import com.rabbitmq.client.*;
import org.renci.databridge.database.*;
import org.renci.databridge.util.*;
import cern.colt.matrix.impl.RCDoubleMatrix2D;

/**
 * This class recieves messages from the RMQListener and uses a DBWriter
 * to store the information contained in the messages into a database
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */

public class MessageHandler {

  /** Queue from which to recieve incoming message */
  private final static String QUEUE_NAME = "update";

  private final static String LOG_QUEUE = "log";

  /**
   * Main class recieves 1 message from queue QUEUE_NAME and processes it
   * A new MessageHandler must be created for each message pushed onto
   * queue QUEUE_NAME, as each processes exactly 1 message
   */
  public static void main(String[] args) throws Exception{


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
    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: msg recieved").getBytes());
    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    String fileLoc = new String(delivery.getBody());
    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: file location determined").getBytes());
    
    NetworkData retriever = new NetworkData();
    try{
      retriever.populateFromURL(fileLoc);
    }
    catch (IOException e){
      channel.basicPublish("", LOG_QUEUE, null, new String("Handler: ERROR - Invalid filename").getBytes());
      System.exit(0);
    }

    ArrayList<Dataset> datasets = retriever.getDatasets();
    for(Dataset d : datasets){
      System.out.println("dataset named : " + d.getName());
    }
    Map<String, String> properties = retriever.getProperties();
    for(Map.Entry<String, String> e : properties.entrySet()){
      System.out.println("property: " + e.getKey() + ", " + e.getValue());
    }
    RCDoubleMatrix2D similMx = retriever.getSimilarityMatrix();
    for(int y = 0; y < similMx.columns(); y++){
      for(int x = 0; x < similMx.rows(); x++){
        System.out.print(similMx.getQuick(x, y) + "   ");
      }
      System.out.println();
    }

    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: complete").getBytes());

    channel.close();
    connection.close();

    //URL fileURL = new URL(fileLoc);
    /*
    byte[] bytes = new byte[2048];
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    InputStream in = fileURL.openStream();
    int i = 0;
    while(in.read(bytes) != -1){
      buf.write(bytes, i * 2048, 2048);
      bytes = new byte[2048];
    }
    */
    /*
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
    */
  }
}
