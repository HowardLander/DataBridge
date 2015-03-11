package org.renci.databridge.util;

import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.LogManager;
import java.io.IOException;

import org.renci.databridge.util.AMQPComms;
import org.renci.databridge.util.AMQPMessage;

/** 
 * Java Util Logging handler that publishes to an AMQP queue.
 *
 * Host, queue, exchange properties must be configured, for example (in logging properties file): 
 * org.renci.databridge.util.AMQPJULHandler.host=<hostname>
 * org.renci.databridge.util.AMQPJULHandler.exchange=<exchange>
 * org.renci.databridge.util.AMQPJULHandler.queue=<queue>
 *
 * @todo how to deal with nulls (e.g., config) where exceptions would be thrown
 * 
 */
public class AMQPJULHandler extends Handler {

  public static final String HEADERS = "type:databridge;subtype:authority;x-match:all";
 
  protected AMQPComms amqpComms;

  public AMQPJULHandler () throws Exception {

    LogManager lm = LogManager.getLogManager ();

    // set basic properties 
    String f = lm.getProperty ("org.renci.databridge.util.AMQPJULHandler.formatter");
    setFormatter ((Formatter) Class.forName (f).newInstance ());
    String lev = lm.getProperty ("org.renci.databridge.util.AMQPJULHandler.level");
    setLevel (parseLogLevel (lev)); 

    // @todo set a default formatter? ow going to get NPE...
    // @todo get/set filter?

    // set properties for AMQP
    String host = lm.getProperty ("org.renci.databridge.util.AMQPJULHandler.host");
    String exchange = lm.getProperty ("org.renci.databridge.util.AMQPJULHandler.exchange");
    String queue = lm.getProperty ("org.renci.databridge.util.AMQPJULHandler.queue");

    Properties p = new Properties ();
    p.setProperty ("org.renci.databridge.queueHost", host);
    p.setProperty ("org.renci.databridge.exchange", exchange);
    p.setProperty ("org.renci.databridge.primaryQueue", queue);

    try {

      this.amqpComms = new AMQPComms (p);
      this.amqpComms.bindTheQueue (HEADERS);

    } catch (IOException ie) {
      ie.printStackTrace ();
    }

  }

  public void close () {
    this.amqpComms.shutdownConnection ();
  }

  public void flush () {
  }

  /**
   * Not called if record not at log level of either the handler or logger.
   * @todo make non-blocking/not long-running, localized.
   */
  public synchronized void publish (LogRecord record) {

    String s = getFormatter ().format (record);
    AMQPMessage message = new AMQPMessage (s.getBytes ());
    this.amqpComms.publishMessage (message, HEADERS, true);

  }

  protected Level parseLogLevel (String lev) {

    Level level = Level.ALL;
    if (lev != null) {
      if ("ALL".equals (lev)) { level = Level.ALL; }
      else if ("CONFIG".equals (lev)) { level = Level.CONFIG; }
      else if ("FINE".equals (lev)) { level = Level.FINE; }
      else if ("FINER".equals (lev)) { level = Level.FINER; }
      else if ("FINEST".equals (lev)) { level = Level.FINEST; }
      else if ("INFO".equals (lev)) { level = Level.INFO; }
      else if ("OFF".equals (lev)) { level = Level.OFF; }
      else if ("SEVERE".equals (lev)) { level = Level.SEVERE; }
      else if ("WARNING".equals (lev)) { level = Level.WARNING; }
    }

    return level; 
 
 }

}
