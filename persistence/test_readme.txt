Notes for testing:

test files using 'mvn test' after reading these notes.

In order for testing to be successful, a rabbitMQ server must be running on your machine, with channels 'hello', 'update', and 'log'. It is recommended to place a TTL property of around 1000ms for log messages so they expire when not being used for testing.

There must also be an instance of an RMQListener running, start this by running 'mvn exec:java -Dexec.mainClass='org.renci.databridge.mhandling.RMQListener'.

Additionally, the message handling tests will fail if the network data tests have not yet ran. Currently, the tests are being run by maven in the order database -> message handling -> network data: This means there will be failures the first time running 'mvn test', but the second time should go smoothly once the 'testWriteToDisk' file has been created.
