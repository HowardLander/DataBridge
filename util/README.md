AMQP java.util.logging logging handler
======================================

AMQPJULHandler is a handler for the java.util.logging framework that publishes log messages to an AMQP exchange. The following java.util.logging configuration properties (with example values) configure it (and can be put, for example in a logging.properties file):

org.renci.databridge.util.AMQPJULHandler.level=ALL
org.renci.databridge.util.AMQPJULHandler.formatter=java.util.logging.SimpleFormatter
org.renci.databridge.util.AMQPJULHandler.host=localhost
org.renci.databridge.util.AMQPJULHandler.exchange=logging-exchange
org.renci.databridge.util.AMQPJULHandler.queue=logging-queue-0

AMQP message mini-framework
===========================

The util package contains a mini-framework for defining and handling messages transported using AMQP. For example, a GraphComputedMessage (org.renci.databridge.message.GraphComputedMessage) that signifies that a graph has been computed.

Listening for messages is done by AMQPMessageListener, which listens to the AMQP system for a given message type in a new thread and dispatches incoming messages of this type to a handler. AMQPMessageListener makes use of the AMQPComms class for on-the-wire AMQP.

Handlers work in the following way. A GraphComputedMessageHandler must implement AMQPMessageHandler then be registered with an instance of AMQPMessageListener:

```
   AMQPMessageListener aml = new AMQPMessageListener (ABS_PATH_TO_AMQPCOMMS_PROPERTIES_FILE, new GraphComputedMessage (), new GraphComputedAMQPMessageHandler (), logger);
   aml.start ();
   // aml.terminate () stops the listener thread.
```

Every time a GraphComputedMessage comes in the listener will dispatch it to the GraphComputedAMQPMessageHandler instance's _handle (AMQPMessage amqpMessage)_ method.

For messages that have a payload, Databridge serializes the payload as a Java object across the wire.
