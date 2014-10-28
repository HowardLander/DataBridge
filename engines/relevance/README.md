mvn -X-e exec:java -Dexec.mainClass=org.renci.databridge.engines.relevance.RelevanceEngine
mvn -X-e exec:java -Dexec.mainClass=org.renci.databridge.engines.relevance.LoadSomeData
mvn -e exec:java -Dexec.mainClass=org.renci.databridge.engines.relevance.PrintSimilarityFile -Dexec.arguments=fileName
