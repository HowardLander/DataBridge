package org.renci.databridge.util;
import org.renci.databridge.message.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.matchers.JUnitMatchers;
import org.junit.Rule;

public class CommsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
       System.out.println("In the AfterClass");
    }

    @Test
    public void testValidation() {
        System.out.println("*********************************************");
        System.out.println("Testing validateStringHeaders");
        AMQPMessage theMessage = new AMQPMessage();
        HashMap<String,String> stringHeaders = new HashMap<String,String>();
        HashMap<String,String> validationMap = new HashMap<String,String>();

        stringHeaders.put("type", "executable");
        stringHeaders.put("similarityId", "12");
        stringHeaders.put("nameSpace", "test");
        validationMap.put("type", "");
        validationMap.put("similarityId", "");
        validationMap.put("nameSpace", "");

        String result = theMessage.validateStringHeaders(stringHeaders, validationMap);
        System.out.println("result of validation is: " + result);
        TestCase.assertTrue("validation failure " + result, 
                             result.compareTo(DatabridgeMessage.STATUS_OK) == 0);
 
        String nameSpace = validationMap.get("nameSpace");
        System.out.println("nameSpace is: " + nameSpace);
        TestCase.assertTrue("validation failure " + nameSpace, 
                            nameSpace.compareTo("test") == 0);

        validationMap.put("className", "");
        validationMap.put("subtype", "");
        String result2 = theMessage.validateStringHeaders(stringHeaders, validationMap);
        TestCase.assertTrue("validation failure " + result2, 
                            result2.compareTo("DataBridge_Error: The message is missing the following required fields:  subtype className") == 0);
        System.out.println("result of second validation is: " + result2);
        System.out.println("*********************************************");
    }

    @Test
    public void testProperties() {
        System.out.println("Testing the properties");
        AMQPComms theComm = new AMQPComms("testProperties.conf");
        String theHost = theComm.getTheHost();
        TestCase.assertTrue("Incorrect host name " + theHost, 
                             theHost.compareTo("localhost") == 0);

        Boolean durability = theComm.getQueueDurability();
        System.out.println("durability for queue  " + theComm.getPrimaryQueue() + " is " + durability);
        TestCase.assertTrue("Incorrect value for durability " + durability, 
                             durability == false);
    }

    @Test
    public void testSendAndReceiveLogMessage() {
        System.out.println("Testing the send of a log message");
        AMQPComms theComm = new AMQPComms("testProperties.conf");
        theComm.bindTheQueue("key1:val1;key2:val2;x-match:all");
        theComm.publishLog(AMQPComms.LOG_DEBUG, "Test Message", "key1:val1;key2:val2", true);
        System.out.println("Testing the receive of a log message");
        AMQPMessage theMessage = theComm.receiveMessage();
        System.out.println("printing retrieved headers");
        Map<String,String> mapHeaders = theMessage.getStringHeaders();
        for (Map.Entry<String, String> entry : mapHeaders.entrySet()){
            System.out.println("key: " + entry.getKey());
            System.out.println("value: " + entry.getValue());
        }

        // Test the headers
        String value1 = mapHeaders.get("key1");
        TestCase.assertTrue("Incorrect value for key1 " + value1, 
                             value1.compareTo("val1") == 0);
        
        String value2 = mapHeaders.get("key2");
        TestCase.assertTrue("Incorrect value for key2 " + value2, 
                             value2.compareTo("val2") == 0);
        
        String message1 = new String(theMessage.getBytes());
        System.out.println("message 1: " + message1);

        AMQPMessage theMessage2 = theComm.receiveMessage();
        Map<String,String> mapHeaders2 = theMessage.getStringHeaders();

        String message2 = new String(theMessage2.getBytes());
        System.out.println("message 2: " + message2);

        // The log code inserts a tab before the test message...
        TestCase.assertTrue("Incorrect value for message " + message2, 
                             message2.endsWith("Test Message") == true);
        
        // Test the message
        theComm.unbindTheQueue("key1:val1;key2:val2;x-match:all");
    }

    @Test
    public void testSendAndTimeout() {
        System.out.println("Testing the send/timeout of a log message");
        AMQPComms theComm = new AMQPComms("testProperties.conf");
        
        // The binding and the send headers don't match.
        theComm.bindTheQueue("key1:val1;key2:val3;x-match:all");
        theComm.publishLog(AMQPComms.LOG_DEBUG, "Test Message", "key1:val1;key2:val2", true);

        // Wait a 1000 milliseconds = 1 second
        long timeout = 1000;
        AMQPMessage theMessage = theComm.receiveMessage(timeout);
        TestCase.assertTrue("Message should not be received", theMessage == null);
        theComm.unbindTheQueue("key1:val1;key2:val3;x-match:all");

    }

    @Test
    public void testSendAndReceiveMessage() {
        System.out.println("Testing the send of a regular message");
        AMQPMessage sendMessage = new AMQPMessage(new String("Regular Message").getBytes());

        AMQPComms theComm = new AMQPComms("testProperties.conf");
        theComm.setRoutingKey("extra info");
        theComm.bindTheQueue("key3:val3;key4:val4;x-match:all");
        theComm.publishMessage(sendMessage, "key3:val3;key4:val4", true);

        System.out.println("Testing the receive of a regular message");
        AMQPMessage theMessage = theComm.receiveMessage();
        System.out.println("printing retrieved headers");
        Map<String,String> mapHeaders = theMessage.getStringHeaders();
        for (Map.Entry<String, String> entry : mapHeaders.entrySet()){
            System.out.println("key: " + entry.getKey());
            System.out.println("value: " + entry.getValue());
        }

        // Test the headers
        String value1 = mapHeaders.get("key3");
        TestCase.assertTrue("Incorrect value for key3 " + value1, 
                             value1.compareTo("val3") == 0);
        
        String value2 = mapHeaders.get("key4");
        TestCase.assertTrue("Incorrect value for key4 " + value2, 
                             value2.compareTo("val4") == 0);
        
        // Test the routingKey
        System.out.println("Testing the receive of the routingKey");
        TestCase.assertTrue("Incorrect value for routingKey " + theMessage.getRoutingKey(), 
                             theMessage.getRoutingKey().compareTo("extra info") == 0);
        
        // Test the message
        String message1 = new String(theMessage.getBytes());
        System.out.println("message 1: " + message1);
        Map<String,String> mapHeaders1 = theMessage.getStringHeaders();
        TestCase.assertTrue("Incorrect value for message " + message1, 
                             message1.compareTo("Regular Message") == 0);
        
        theComm.unbindTheQueue("key3:val3;key4:val4;x-match:all");
    }


    @Rule
    public ExpectedException thrown = ExpectedException.none();
}

