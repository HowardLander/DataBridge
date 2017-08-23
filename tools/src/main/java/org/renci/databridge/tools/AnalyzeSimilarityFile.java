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
 *  -properties <propertyFile> Property file (currently only used if op is CSV)
 *  -outputFile <outputFile>   Output file (currently only used if op is CSV)
 *  -help                      print this message
 *
 *  Example mvn command line:
 *   mvn -e exec:java -Dexec.mainClass=org.renci.databridge.tools.AnalyzeSimilarityFile -Dexec.arguments="-inputFile",similarityFile,"-operation",operation
 * mvn -e exec:java -Dexec.mainClass=org.renci.databridge.tools.AnalyzeSimilarityFile -Dexec.arguments="-inputFile",/home/howard/DbFNNetwork-v7.2.net,"-operation",CSV,"-properties",/projects/databridge/howard/DataBridge/config/DataBridge.conf,-outputFile",test.csv
 */

public class AnalyzeSimilarityFile {
  public static int total = 0;
  public static int zeros = 0;
  public static int nonZeros = 0;
  public static long[] histogram = null;
  public static int nBins = 0;
  public static final String CSV = "csv";  
  public static final String STATS = "stats";  
  public static final String MATRIX = "matrix";  
  public static final String HISTOGRAM = "histogram";  

  private static CommandLine processArgs(String[] args) {
       Options options = new Options();
       Option inputFile = OptionBuilder.withArgName("inputFile").hasArg().withDescription("input file").create("inputFile");
      Option operation = OptionBuilder.withArgName("operation").hasArg().withDescription("operation").create("operation");
      Option properties = OptionBuilder.withArgName("properties").hasArg().withDescription("properties file").create("properties");
      Option outputFile = OptionBuilder.withArgName("outputFile").hasArg().withDescription("outputFile file").create("outputFile");
      Option histArg = OptionBuilder.withArgName("bins").hasArg().withDescription("bins").create("bins");
       Option help = new Option("help", "print this message");
       options.addOption(inputFile);
       options.addOption(operation);
       options.addOption(properties);
       options.addOption(outputFile);
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
        //VectorOp theVectorClass = new VectorOp();
        MatrixOp theMatrixClass = new MatrixOp();
        org.la4j.matrix.sparse.CRSMatrix theMatrix = readData.getSimilarityMatrix();
        theMatrix.each(theMatrixClass);

        /*
        for (int i = 0; i < theMatrix.rows(); i++) {
            if (theMatrix.maxInRow(i) > 0.) {
               org.la4j.vector.Vector thisRow = theMatrix.getRow(i);
               thisRow.each(theVectorClass);
            } 
        }
        */

        long totalEdges = ((nodeCount * nodeCount) / 2) - nodeCount;
        System.out.println("\tnumber of zeros in matrix: " + zeros);
        System.out.println("\tnon-zero edges: " + total + " out of " + totalEdges);

        System.out.println("\tcollectionIds: ");
        for (String theId: readData.getCollectionIds()) {
            System.out.println("\t\tthisId: " + theId);
        }
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }


  public static void operationCSV(String inputFile, String outputFile, CollectionDAO theCollectionDAO) {
     try {
        SimilarityFile readData = new SimilarityFile();
        readData.readFromDisk(inputFile);
        String nameSpace = readData.getNameSpace();
        org.la4j.matrix.sparse.CRSMatrix theMatrix = readData.getSimilarityMatrix();
        PrintWriter pw = new PrintWriter(new File(outputFile));

        // It may help to remember that the matrix must be symetric
        int dimension = theMatrix.rows();
        String studyNames[] = new String[dimension];

        // Print the header line. It's going to be the namespace, followed by each of the study names
        StringBuilder headerString = new StringBuilder();
        headerString.append(nameSpace);
        int index = 0;
        for (String theId: readData.getCollectionIds()) {
            CollectionTransferObject theCTO = theCollectionDAO.getCollectionById(theId);
            headerString.append(",");
            headerString.append(theCTO.getTitle());
            studyNames[index] = theCTO.getTitle();
            index++;
        }

        pw.write(headerString.toString());
        pw.write("\n");

        double completeMatrix[][] = new double[dimension][dimension];

        VectorOp theVectorClass = new VectorOp();
        for (int i = 0; i < theMatrix.rows(); i++) {
           StringBuilder rowString = new StringBuilder();
           rowString.append(studyNames[i]);
            if (theMatrix.maxInRow(i) > 0.) {
               org.la4j.Vector thisRow = theMatrix.getRow(i);
               for (int j = i; j < dimension; j++) {
                  completeMatrix[i][j] = thisRow.get(j);
                  completeMatrix[j][i] = thisRow.get(j);
                  completeMatrix[i][i] = 1.0;
               }
            } else {
               for (int j = 0; j < dimension; j++) {
                  if (i == j) {
                     completeMatrix[i][i] = 1.0;
                  } else {
                     completeMatrix[i][i] = 0.0;
                  }
               }
            }
        }
 
        for (int i = 0; i < theMatrix.rows(); i++) {
           StringBuilder rowString = new StringBuilder();
           rowString.append(studyNames[i]);
           for (int j = 0; j < dimension; j++) {
              rowString.append(",");
              rowString.append(completeMatrix[i][j]);
           }
           pw.write(rowString.toString());
           pw.write("\n");
         }

         pw.close();
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
               org.la4j.Vector thisRow = theMatrix.getRow(i);
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
        long totalEdges = (((nodeCount * nodeCount) - nodeCount) / 2);

        HistogramOp theHistogram = new HistogramOp();
        readData.getSimilarityMatrix().each(theHistogram);

        // let's subtract out the lower triangle and the diagonal from bin 0
        histogram[0] = histogram[0] - ((nodeCount * nodeCount) - totalEdges);
        int totalInBins = 0;
        for (int i = 0; i < nBins; i++) {
            totalInBins += histogram[i];
        }
        System.out.println("\tTotal Edges: " + totalEdges);
        System.out.println("\tTotal Non Zeros: " + nonZeros);
        System.out.println("\tTotal In Bins (should match Total Edges): " + totalInBins);
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
     String outputFile = theLine.getOptionValue("outputFile");
     operation = theLine.getOptionValue("operation");

     System.out.println("\nReading file " + inputFile); 

     try {
        if (operation.equalsIgnoreCase(STATS)) {
           operationStats(inputFile);
        } else if (operation.equalsIgnoreCase(MATRIX)) {
           operationMatrix(inputFile);
        } else if (operation.equalsIgnoreCase(CSV)) {
           String dbType;
           String dbName;
           String dbHost;
           int    dbPort;
           String dbUser;
           String dbPwd;
           MetadataDAOFactory theFactory = null;
        
           // open the preferences file
           Properties prop = new Properties();
           prop.load(new FileInputStream(theLine.getOptionValue("properties")));
           dbType = prop.getProperty("org.renci.databridge.relevancedb.dbType", "mongo");
           dbName = prop.getProperty("org.renci.databridge.relevancedb.dbName", "test");
           dbHost = prop.getProperty("org.renci.databridge.relevancedb.dbHost", "localhost");
           dbPort = Integer.parseInt(prop.getProperty("org.renci.databridge.relevancedb.dbPort", "27017"));
           dbUser = prop.getProperty("org.renci.databridge.relevancedb.dbUser", "localhost");
           dbPwd = prop.getProperty("org.renci.databridge.relevancedb.dbPassword", "localhost");
    
           // Connect to the mongo database
           if (dbType.compareToIgnoreCase("mongo") == 0) {
               theFactory = MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB,
                                                                     dbName, dbHost, dbPort, dbUser, dbPwd);
               if (null == theFactory) {
                  System.out.println("Couldn't produce the MetadataDAOFactory");
                  return;
               }
           }
    
           // We'll need a collection DAO object.
           CollectionDAO theCollectionDAO = theFactory.getCollectionDAO();
           operationCSV(inputFile, outputFile, theCollectionDAO);
           theFactory.closeTheDB();
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
