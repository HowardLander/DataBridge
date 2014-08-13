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

Formatter package
=================

Formatters are POJO classes that are mapped to XML documents. They are generated using JAXB against an XSD schema document. This happens during the generate-sources phase of the maven lifecycle. Then when a document that is an example of the XSD comes in to the formatter it is converted to an instance of the relevant POJOs.

Additional formatters can be added by:
  1. Add XSD to util/src/main/xsd.
  2. Add a new <execution> to jaxb2-maven-plugin in util/pom.xml to do each XSD separately and put it in its own generated package
  3. Create a new formatter.




