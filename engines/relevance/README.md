mvn -X-e exec:java -Dexec.mainClass=org.renci.databridge.engines.relevance.RelevanceEngine
mvn -X-e exec:java -Dexec.mainClass=org.renci.databridge.engines.network.NetworkEngine
mvn -X-e exec:java -Dexec.mainClass=org.renci.databridge.engines.relevance.LoadSomeData
mvn -e exec:java -Dexec.mainClass=org.renci.databridge.engines.relevance.PrintSimilarityFile -Dexec.arguments=fileName
mvn -e exec:java -Dexec.mainClass=org.renci.databridge.engines.network.PrintMatrix -Dexec.arguments=system_test,5457e86ae4b09b165b697e22,/projects/databridge/howard/DataBridge/persistence/testData

mvn -e exec:java -Dexec.mainClass=org.renci.databridge.contrib.example.messagesend.SendHeader -Dexec.arguments=/projects/databridge/howard/DataBridge/engines/network/network.conf,"name:Create.JSON.File.NetworkDB.URI;type:databridge;subtype:network;nameSpace:test_ingest_4;similarityId:54f6068ae4b0301e26264675;outputFile:/home/howard/ingest_5.json"

mvn exec:javaDexec.mainClass=org.renci.databridge.contrib.dataloader.DataLoader -Dexec.args="/projects/databridge/howard/DataBridge/engines/relevance/relevance.conf org.renci.databridge.contrib.formatter.oaipmh.OaipmhMetadataFormatterImpl crabtreetest file:///projects/databridge/OAI-set/crabtreetest3162015"

mvn -e exec:java -Dexec.mainClass=org.renci.databridge.tools.AddAction -Dexec.arguments=tools.conf,Processed.Metadata.To.MetadataDB,test_action_1,org.renci.databridge.contrib.similarity.ncat.Measure,/home/howard/generatedNet/

# To test the basic GUI
mvn -e exec:java -Dexec.mainClass=org.renci.databridge.gui.DataBridgeGUI -Dexec.arguments=../config/DataBridge.conf

To run mongo-express:
howard@databridge.renci.org:/projects/databridge/howard/tools/node-v0.12.4-linux-x64/bin/node_modules/mongo-express $ node app

Then connect local firefox to 8081
