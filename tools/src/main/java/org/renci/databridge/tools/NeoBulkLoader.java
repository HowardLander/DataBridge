package org.renci.databridge.tools;

import java.io.*;
import java.util.*;
import org.apache.commons.cli.*;
import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.util.*;
import org.renci.databridge.message.*;
import org.la4j.*;
import org.la4j.matrix.functor.*;
import org.la4j.vector.functor.*;
import java.nio.file.*;

// Neo imports
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
/*
 * Use this class to perform a variety of actions on a similarity file
 *
 * usage: AnalyzeSimilarityFile
 *  -inputFile <inputFile>     The name of the file to analyze
 *  -operation <operation>     one of "stats", "histogram", matrix
 *  -bins <operation>          number of bins for the histogram
 *  -help                      print this message
 *
 *  Example mvn command line:
 *   mvn -e exec:java -Dexec.mainClass=org.renci.databridge.tools.AnalyzeSimilarityFile -Dexec.arguments="-inputFile",similarityFile,"-operation",operation
 */

public class NeoBulkLoader {

  public static int count = 0;
  public static int total = 0;
  public static final String INSERT = "insert";
  public static BatchInserter theInserter;
  public static ArrayList<Long> nodeList = new ArrayList<Long>();
  public static String nameSpace = null;
  public static String similarity = null;

  class InsertOp implements MatrixProcedure {
     public void apply(int i, int j, double value) {
        if ((value >= 0. && value <= 1.0)  && (total < count)) {
           total++; 
        } 
     }
  }

  private static CommandLine processArgs(String[] args) {
       Options options = new Options();
       Option inputFile = OptionBuilder.withArgName("inputFile").hasArg().withDescription("input file").create("inputFile");
       Option operation = OptionBuilder.withArgName("operation").hasArg().withDescription("operation").create("operation");
       Option directoryArg = OptionBuilder.withArgName("directory").hasArg().withDescription("directory").create("directory");
       Option countArg = OptionBuilder.withArgName("count").hasArg().withDescription("count").create("count");
       Option help = new Option("help", "print this message");
       options.addOption(inputFile);
       options.addOption(operation);
       options.addOption(directoryArg);
       options.addOption(countArg);
       options.addOption(help);

       // create the parser
       CommandLineParser parser = new GnuParser();
       CommandLine line = null;
       try {
           // parse the command line arguments
           line = parser.parse( options, args );
           if( line.hasOption( "help" ) ) {
              HelpFormatter formatter = new HelpFormatter();
              formatter.printHelp( "NeoBulkLoader", options );
              System.exit(0);
           }
       }
       catch( ParseException exp ) {
           // oops, something went wrong
           System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
       }
       return line;
    }

  public static void operationInsert(String inputFile) {
     try {
        SimilarityFile readData = new SimilarityFile();
        readData.readFromDisk(inputFile);
        nameSpace = readData.getNameSpace();
        similarity = readData.getSimilarityInstanceId();
        int index = 0;
        int relationshipCount = 0;

        // Let's insert the nodes
        for (String theId: readData.getCollectionIds()) {
           Map<String, Object> properties = new HashMap<String, Object>();
           properties.put(similarity, index);
           //TODO: Replace the hardcoded value
           properties.put("metaDataNodeKey",theId);
           long nodeId = theInserter.createNode(properties);
           Label newLabel = Label.label(nameSpace);
           theInserter.setNodeLabels(nodeId, newLabel);
           nodeList.add(nodeId);
           index ++;
        }

        // Now we run through the matrix, adding every value > 0 as a relationship.
        org.la4j.matrix.sparse.CRSMatrix theMatrix = readData.getSimilarityMatrix();
        for (int i = 0; i < theMatrix.rows(); i++) {
           for (int j = 0; j < theMatrix.columns(); j++) {
              if ((count < 0) || (relationshipCount < count)) {
                 if ((relationshipCount % 1000000) == 0) {
                    System.out.println("working on relationship " + relationshipCount);
                 }
                 relationshipCount ++;
                 double thisValue = theMatrix.get(i,j);
                 if (thisValue >= 0.) {
                    RelationshipType simRelation = RelationshipType.withName(similarity);
                    Map<String, Object> properties = new HashMap<String, Object>();
                    //TODO: Replace the hardcoded value
                    properties.put("value", thisValue);
                    theInserter.createRelationship(nodeList.get(i), nodeList.get(j), simRelation, properties);
                 } 
              }
           }
        }

        theInserter.shutdown();
        System.out.println("added " + relationshipCount + " relationships");
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  public static void operationDelete(String inputFile) {
  }

  public static void main(String [] args) {

     String inputFile;
     String operation;
     String directory;

     CommandLine theLine = processArgs(args); 
     inputFile = theLine.getOptionValue("inputFile");
     operation = theLine.getOptionValue("operation");
     directory = theLine.getOptionValue("directory");
     count = Integer.parseInt(theLine.getOptionValue("count"));

     System.out.println("\nReading file " + inputFile); 

     try {
        if (operation.equalsIgnoreCase(INSERT)) {
           theInserter = BatchInserters.inserter(new File(directory));
           operationInsert(inputFile);
        } 
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }
}
