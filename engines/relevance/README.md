mvn -X-e exec:java -Dexec.mainClass=org.renci.databridge.engines.relevance.RelevanceEngine
mvn -X-e exec:java -Dexec.mainClass=org.renci.databridge.engines.network.NetworkEngine
mvn -X-e exec:java -Dexec.mainClass=org.renci.databridge.engines.relevance.LoadSomeData
mvn -e exec:java -Dexec.mainClass=org.renci.databridge.engines.relevance.PrintSimilarityFile -Dexec.arguments=fileName
mvn -e exec:java -Dexec.mainClass=org.renci.databridge.engines.network.PrintMatrix -Dexec.arguments=system_test,5457e86ae4b09b165b697e22,/projects/databridge/howard/DataBridge/persistence/testData
