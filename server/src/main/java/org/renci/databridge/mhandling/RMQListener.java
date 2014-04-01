package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Server class listening for incoming messages on rabbitMQ queue QUEUE_NAME
 * on receipt: creates a new instance of the MessageHandler to handle the message
 * and forwards the message to it along the OUT_QUEUE channel
 *
 * @author Ren Bauer -RENCI (www.renci.org)
 */

public class RMQListener{

  /** The queue on which to listen for incoming messages */
  private final static String QUEUE_NAME = "hello";

  /** The queue on which to forward messages */
  private final static String OUT_QUEUE = "update";

  private final static String LOG_QUEUE = "log";

  private enum DBs {Neo4j, Titan};
  
  /**
   * Main function starts listening on QUEUE_NAME, and loops indefinitely
   * waiting for anh messeges, forwarding them without modification
   */
  public static void main(String[] args) throws Exception{

    String DB = "Neo4j";
    String path = "data/";

    try{
       Properties prop = new Properties();
       prop.load(new FileInputStream("db.conf"));
       DB = prop.getProperty("database");
       path = prop.getProperty("path");
    } catch (IOException ex){ }

    System.out.println(" database: " + DB);
    System.out.println(" path: " + path);

    DBs type;
    Object dbService;
    if(DB.equals("Titan")){
	type = DBs.Titan;
	path += "titanHB/";
        dbService = TitanFactory.open(path);
        Runtime.getRuntime().addShutdownHook(new RMQShutdownHook<TitanGraph>((TitanGraph) dbService));
    } else if(DB.equals("Neo4j")){
	type = DBs.Neo4j;
	path += "neo4j/";
        dbService = new GraphDatabaseFactory().newEmbeddedDatabase(path);
        Runtime.getRuntime().addShutdownHook(new RMQShutdownHook<GraphDatabaseService>((GraphDatabaseService) dbService));
    } else throw new Exception("Invalid database specified in properties");
  
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.confirmSelect();

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(QUEUE_NAME, true, consumer);

    while (true) {
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      String message = new String(delivery.getBody());
      System.out.println("recieved " + message);
      channel.basicPublish("", LOG_QUEUE, null, new String("Listener: msg recieved").getBytes());
      //Runtime.getRuntime().exec("mvn exec:java -Dexec.mainClass='org.renci.databridge.mhandling.MessageHandler'");
      //Runtime.getRuntime().exec("./runHandler");
      switch(type){
      case Titan:
        new MessageHandler<TitanGraph>((TitanGraph) dbService).start();
      break;
      case Neo4j:
        new MessageHandler<GraphDatabaseService>((GraphDatabaseService) dbService).start();
      break;
      }
      //ProcessBuilder pb = new ProcessBuilder("./runHandler");
      //pb.redirectErrorStream(true);
      //pb.start();
      // Declaration is idempotent, so no need to worry about whether or not it is already there.
      channel.queueDeclare(OUT_QUEUE, false, false, false, null);
      channel.basicPublish("", OUT_QUEUE, null, message.getBytes());
      channel.basicPublish("", LOG_QUEUE, null, new String("Listener: msg forwarded").getBytes());
      System.out.println("forwarded " + message);
    }
  }
}
