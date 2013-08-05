package org.renci.databridge.mhandling;

import com.rabbitmq.client.*;
import org.renci.databridge.database.*;
import org.renci.databridge.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import cern.colt.matrix.impl.RCDoubleMatrix2D;

public class NetworkHandler implements BaseHandler {

  public void handle(String msg, Channel channel, String LOG_QUEUE) throws Exception{

    String fileLoc = msg;

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

  }

}
