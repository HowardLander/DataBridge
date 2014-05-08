DataBridge
==========

NSF Sponsored DataBridge Project

For more information see https://databridge.web.unc.edu

To Clone:

From a command line, run:

git clone https://github.com/HowardLander/DataBridge.git

or, on a windows machine:

download github for windows
http://windows.github.com/

search for repository HowardLander/DataBridge and clone from there

To Setup

Customize the config files as follows:

Edit DataBridge/network/BigData/XMLSim/xmlsim.conf
Edit DataBridge/server/db.conf

To Install (requires maven):

run the command:

mvn compile

in each of the following directories:

DataBridge/server/
DataBridge/network/BigData/XMLSim/

(you should see a pom.xml in each of these directories)

information for setting up maven on windows can be found here:
http://maven.apache.org/guides/getting-started/windows-prerequisites.html


To Run:

To run Xing's distance generation code, use the following command from the XMLSim directory:

mvn -e exec:java -D exec.mainClass=xmlsim.XMLSim

This class currently operates on an XML file, the location of which is hardcoded

To run the rabbitMQ listening server:

First, a rabbitMQ server must be running, there's an init script in /etc/init.d

Examples:

# status
sudo /etc/init.d/rabbitmq-server status

# start 
sudo /etc/init.d/rabbitmq-server start

# stop 
sudo /etc/init.d/rabbitmq-server stop

next, use the following command from the server directory to run the listener

mvn -e exec:java -D exec.mainClass=org.renci.databridge.mhandling.RMQListener


To run the producer (written by Xing Fang at NCAT and altered to use the queues by us)

from the XMLSim directory:

mvn test -Dtest=similarity.MeasureTest

Howard's network classes and Ren's code can be tested using the following command from the server directory:

mvn test

for more information on tests please see 'test_readme.txt' in the server directory

to see the logs, we have a simple low queue reader.  It's in DataBridge/tools/python

example usage:

./logs.py -s localhost -q log

The last argument is whatever you set the value of org.renci.databridge.logQueue 

Note that, as of this writing the XMLSim program doesn't write to the log...
