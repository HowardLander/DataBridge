The AMQP message mini-framework
===============================

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
