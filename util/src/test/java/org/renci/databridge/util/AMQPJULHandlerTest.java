package org.renci.databridge.util;

import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogManager;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.matchers.JUnitMatchers;
import org.junit.Rule;

import com.rabbitmq.client.*;

import org.renci.databridge.message.TestMessage;

public class AMQPJULHandlerTest {

  @BeforeClass
  public static void setUpBeforeClass () throws Exception {
  }
  
  @AfterClass
  public static void tearDownAfterClass () throws Exception {
  }

  protected String receivedString;

  @Test
  public void testLogMessage () throws Exception {

    System.out.println ("Testing logger sending message to queue...");

    // load logging properties file into LogManager

    InputStream is = getClass ().getResourceAsStream ("/logger-test-logging.properties");
    LogManager lm = LogManager.getLogManager ();
    lm.readConfiguration (is);

    // log something that should go to the handler
    Logger logger = Logger.getLogger ("org.renci.databridge.util.AMQPJULHandlerTest");
    logger.log (Level.SEVERE, "This message should go to AMQP queue.");

    // unhook this handler from LogManager 
    // reset the LogManager properties?

  }

  @Test
  public void testNotLogMessage () throws Exception {

    System.out.println ("Testing logger not sending message to queue...");

    // load logging properties file into LogManager

    InputStream is = getClass ().getResourceAsStream ("/logger-test-logging.properties");
    LogManager lm = LogManager.getLogManager ();
    lm.readConfiguration (is);

    // log something that should go to the handler
    Logger logger = Logger.getLogger ("org.renci.databridge.util.AMQPJULHandlerTest");
    logger.log (Level.FINEST, "This message should NOT go to AMQP queue.");

  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

}
