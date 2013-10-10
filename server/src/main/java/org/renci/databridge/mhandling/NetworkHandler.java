package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import org.renci.databridge.database.*;
import org.renci.databridge.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import cern.colt.matrix.impl.RCDoubleMatrix2D;

/**
 * Bottom level message handler for messages of type Network. These are messages
 * containing network information which must be stored in the database.
 *
 * @author Ren Bauer - RENCI (www.renci.org)
 */

public class NetworkHandler<T> implements BaseHandler {

  T dbService = null;

  public NetworkHandler(T dbService){
    this.dbService = dbService;
  }

  /**
   * Handle the message appropriately. For network messages, the network is read
   * from the URL contained in the message, parsed, and entered into the database.
   *
   * @param msg The original message minus the filetype and delimiting colon
   * @param channel The output rabbitMQ channel for sending messages
   * @param LOG_QUEUE The queue on which to send log messages
   *
   * @return The message to return to sender. Null for no response.
   */
  public String handle(String msg, Channel channel, String LOG_QUEUE) throws Exception{

    String fileLoc = msg;

    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: file location determined: " + msg).getBytes());

    NetworkData retriever = new NetworkData();
    try{
      retriever.populateFromURL(fileLoc);
      channel.basicPublish("", LOG_QUEUE, null, new String("Handler: Populated from URL").getBytes());
    }
    catch (IOException e){
      channel.basicPublish("", LOG_QUEUE, null, new String("Handler: ERROR - Invalid filename").getBytes());
      System.exit(0);
    }

    DBWriter dbw = null;
    if(dbService instanceof GraphDatabaseService)
      dbw = new DBWriterNeo4j(dbService);
    else if(dbService instanceof TitanGraph)
      dbw = new DBWriterTitanHB(dbService);
    try{
      ArrayList<Dataset> datasets = retriever.getDatasets();
      int i = 0;
      for(Dataset d : datasets){
        dbw.writeNode(new DBNode(i++, "dataset", d.getDbID(), makeProps(d.getProperties())));
      }
      String[][] edgeProps = makeProps(retriever.getProperties());
      String edgeID = retriever.getDbID();
      channel.basicPublish("", LOG_QUEUE, null, new String("Handler: edge ID = " + edgeID).getBytes());
      RCDoubleMatrix2D similMx = retriever.getSimilarityMatrix();
      for(int y = 0; y < similMx.columns(); y++){
        for(int x = 0; x < similMx.rows(); x++){
          if(x != y){
            DBEdge e = new DBEdge(x, y, "similarity", edgeID, edgeProps);
            e.addProperty("value", similMx.getQuick(x, y) + "");
  	    dbw.writeEdge(e);
          }
        }
      }
    }
    catch(Exception e){
      throw e;
    }
    finally {
      dbw.shutDown();
    }

   // Temporary code generating JSON output for visualization bit
    try{
      makeJSON(retriever);
    }
    catch (JSONException e){
      channel.basicPublish("", LOG_QUEUE, null, new String("Handler: JSON creation failed - JSONException").getBytes());
    }
    catch (IOException e){
      channel.basicPublish("", LOG_QUEUE, null, new String("Handler: JSON creation failed - IOException").getBytes());
    }
   //End temporary JSON code

    channel.basicPublish("", LOG_QUEUE, null, new String("Handler: complete").getBytes());

    return null;

  }

  /**
   * Convert a Map of properties to a 2D String array of properties, so they
   * can be stored in the DB database classes.
   *
   * @param propMap the Map of the properties.
   *
   * @return a 2D String Array representation of the properties.
   */ 
  private String[][] makeProps(Map<String, String> propMap){
    if(propMap == null || propMap.keySet() == null) return new String[0][0];
    String[][] propArray = new String[propMap.keySet().size()][2];
    int i = 0;
    for(Map.Entry<String, String> e : propMap.entrySet()){
      propArray[i][0] = e.getKey();
      propArray[i++][1] = e.getValue();
    }
    return propArray;
  }
  
  /**
   * Duplicate output to a JSON object which can be viewed by the visualizer. This 
   * code is temporary and should be removed once the infrastructure to make and
   * respond to JSON requests in an intelligent manner is in place.
   *
   * @param retriever The NetworkData object created from the specified file
   */
  private void makeJSON(NetworkData retriever) throws JSONException, IOException{
    ArrayList<Dataset> datasets = retriever.getDatasets();
    int i = 0;
    JSONArray nodes = new JSONArray();
    for(Dataset d : datasets){
      JSONObject JSONd = new JSONObject();
      JSONd.put("label", "dataset");
      JSONd.put("dbID", d.getDbID());
      JSONd.put("name", d.getName());
      JSONd.put("URI", d.getURI());
      JSONd.put("handle", d.getHandle());
      JSONd.put("index", i);
      for(String[] prop : makeProps(d.getProperties())){
        JSONd.put(prop[0], prop[1]);
      }
      nodes.put(JSONd);
    }
    String[][] edgeProps = makeProps(retriever.getProperties());
    String edgeID = retriever.getDbID();
    RCDoubleMatrix2D similMx = retriever.getSimilarityMatrix();
    JSONArray links = new JSONArray();
    for(int y = 0; y < similMx.columns(); y++){
      for(int x = 0; x < similMx.rows(); x++){
        if(x == y) continue;
        JSONObject JSONl = new JSONObject();
        JSONl.put("label", "similarity");
        JSONl.put("dbID", edgeID);
        JSONl.put("source", x);
        JSONl.put("target", y);
	JSONl.put("value", similMx.getQuick(x, y));
        for(String[] prop : edgeProps){
          JSONl.put(prop[0], prop[1]);
        }
        links.put(JSONl);
      }
    }
    JSONObject out = new JSONObject();
    out.put("nodes", nodes);
    out.put("links", links);
   
    File f = new File("data/JSON/" + edgeID + ".json");
    OutputStream os = new FileOutputStream(f);
    os.write(out.toString().getBytes());
    os.close();
  }

}
