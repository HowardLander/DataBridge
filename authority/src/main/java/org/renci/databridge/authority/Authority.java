package org.renci.databridge.authority;

import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;

//import com.rabbitmq.client.*;

import org.renci.databridge.util.AMQPLogger;

/**
 * Receives a message asking whether a term is valid in the Databridge 
 * vocabulary and responds to the message.
 *
 * @author mrshoffn
 */
public class Authority {

  public Authority () throws IOException {

     Properties p = new Properties ();
     p.load (new FileInputStream ("authority.properties"));

  }

  protected void listen () throws Exception {


  }

  public static void main (String [] args) throws Exception {

    new Authority ().listen ();  

  }

}
