package databridge.server;

import com.rabbitmq.client.*;
import org.msgpack.MessagePack;
import java.io.*;

public class TestSender{

  public static class PackFile {
    public static final int NETWORK = 1;
    public int fileType;
    public byte[] file;
  }

  public static class PackNetwork {
    public DBNode[] nodes;
    public DBEdge[] edges;
  }

  private final static String QUEUE_NAME="hello";

  public static void main(String[] args) throws Exception {

    DBNode[] nodes = new DBNode[2];
    nodes[0] = new DBNode(1, "Basic", "Test-Node-1", null);
    nodes[1] = new DBNode(2, "Basic", "Test-Node-2", new String[][]{new String[]{"prop1", "test"}});
    DBEdge[] edges = new DBEdge[1];
    edges[0] = new DBEdge(1, 2, "Basic", "Test-Edge-1", new String[][]{new String[]{"prop1", "test"}});

    PackNetwork pn = new PackNetwork();
    pn.nodes = nodes;
    pn.edges = edges;
    
    PackFile pf = new PackFile();
    pf.fileType = PackFile.NETWORK;

    MessagePack msgpack = new MessagePack();
    msgpack.register(DBNode.class);
    msgpack.register(DBEdge.class);
    msgpack.register(PackFile.class);
    msgpack.register(PackNetwork.class);
    byte[] file = msgpack.write(pn);
    pf.file = file;
    byte[] store = msgpack.write(pf);

    FileOutputStream fos = new FileOutputStream(new File("store"));
    fos.write(store);
    fos.close();

    String message = "file://localhost/Users/Ren/Documents/dbspace/server/store";

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

    channel.close();
    connection.close();

  }

}
