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

public class AnalyzeSimilarityFile {
  public static int total = 0;
  public static int zeros = 0;
  public static long[] histogram = null;
  public static int nBins = 0;
  public static final String STATS = "stats";  
  public static final String MATRIX = "matrix";  
  public static final String HISTOGRAM = "histogram";  

  private static CommandLine processArgs(String[] args) {
       Options options = new Options();
       Option inputFile = OptionBuilder.withArgName("inputFile").hasArg().withDescription("input file").create("inputFile");
      Option operation = OptionBuilder.withArgName("operation").hasArg().withDescription("operation").create("operation");
      Option histArg = OptionBuilder.withArgName("bins").hasArg().withDescription("bins").create("bins");
       Option help = new Option("help", "print this message");
       options.addOption(inputFile);
       options.addOption(operation);
       options.addOption(histArg);
       options.addOption(help);

       // create the parser
       CommandLineParser parser = new GnuParser();
       CommandLine line = null;
       try {
           // parse the command line arguments
           line = parser.parse( options, args );
           if( line.hasOption( "help" ) ) {
              HelpFormatter formatter = new HelpFormatter();
              formatter.printHelp( "AnalyzeSimilarityFile", options );
              System.exit(0);
           }
       }
       catch( ParseException exp ) {
           // oops, something went wrong
           System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
       }
       return line;
    }

  public static void operationStats(String inputFile) {
     try {
        SimilarityFile readData = new SimilarityFile();
        readData.readFromDisk(inputFile);
        System.out.println("\tnameSpace: " + readData.getNameSpace());
        System.out.println("\tsimilarityInstanceId: " + readData.getSimilarityInstanceId());
        int nodeCount = readData.getCollectionIds().size();
        System.out.println("\tnumber of nodes: " + nodeCount);
        VectorOp theVectorClass = new VectorOp();
        org.la4j.matrix.sparse.CRSMatrix theMatrix = readData.getSimilarityMatrix();
        for (int i = 0; i < theMatrix.rows(); i++) {
            if (theMatrix.maxInRow(i) > 0.) {
               org.la4j.vector.Vector thisRow = theMatrix.getRow(i);
               thisRow.each(theVectorClass);
            } 
        }

        long totalEdges = ((nodeCount * nodeCount) / 2) - nodeCount;
        System.out.println("\tnumber of zeros in matrix: " + zeros);
        System.out.println("\tnon-zero edges: " + total + " out of " + totalEdges);

        System.out.println("\tcollectionIds: ");
        for (String theId: readData.getCollectionIds()) {
            System.out.println("\tthisId: " + theId);
        }
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  public static void operationMatrix(String inputFile) {
     try {
        SimilarityFile readData = new SimilarityFile();
        readData.readFromDisk(inputFile);
        System.out.println("\tnameSpace: " + readData.getNameSpace());
        System.out.println("\tsimilarityInstanceId: " + readData.getSimilarityInstanceId());

        System.out.println("\tMatrix: ");
        VectorOp theVectorClass = new VectorOp();
        org.la4j.matrix.sparse.CRSMatrix theMatrix = readData.getSimilarityMatrix();
        for (int i = 0; i < theMatrix.rows(); i++) {
            System.out.println("Row: " + i);
            if (theMatrix.maxInRow(i) > 0.) {
               org.la4j.vector.Vector thisRow = theMatrix.getRow(i);
               System.out.println(thisRow.toString());
               thisRow.each(theVectorClass);
            } else {
               System.out.println("Skipping empty row");
            }
            System.out.println("");
        }

        System.out.println("total of non-zero links: " + total);

        NonZeroOp theNonZero = new NonZeroOp();
        readData.getSimilarityMatrix().eachNonZero(theNonZero);

     }  catch (Exception e) {
         e.printStackTrace();
     }
  }

  public static void operationHistogram(String inputFile) {
     try {
        SimilarityFile readData = new SimilarityFile();
        readData.readFromDisk(inputFile);
        org.la4j.matrix.sparse.CRSMatrix theMatrix = readData.getSimilarityMatrix();
        int nodeCount = readData.getCollectionIds().size();
        long totalEdges = ((nodeCount * nodeCount) / 2) - nodeCount;

        HistogramOp theHistogram = new HistogramOp();
        readData.getSimilarityMatrix().each(theHistogram);

        System.out.println("\tTotal Edges: " + totalEdges);
        System.out.println("\tPrinting Histogram");
        for (int i = 0; i < nBins; i++) {
           double percent = ((float)(histogram[i])/totalEdges) * 100;      
           System.out.println("\t\t bin: " + i + " count: " + histogram[i] + "\tPercentage: " + percent + "%");
        }

     }  catch (Exception e) {
         e.printStackTrace();
     }
  }


  public static void main(String [] args) {

     String inputFile;
     String operation;

     CommandLine theLine = processArgs(args); 
     inputFile = theLine.getOptionValue("inputFile");
     operation = theLine.getOptionValue("operation");

     System.out.println("\nReading file " + inputFile); 

     try {
        if (operation.equalsIgnoreCase(STATS)) {
           operationStats(inputFile);
        } else if (operation.equalsIgnoreCase(MATRIX)) {
           operationMatrix(inputFile);
        } else if (operation.equalsIgnoreCase(HISTOGRAM)) {
           nBins = Integer.parseInt(theLine.getOptionValue("bins"));
           histogram = new long[nBins];
           operationHistogram(inputFile);
        }
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }
}
