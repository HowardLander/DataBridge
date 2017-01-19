package org.renci.databridge.tools;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.*;
import org.renci.databridge.persistence.metadata.*;
/*
 * Use this to read/write CollectionTransferObjects from to json form.
 *
 * usage: CollectionTransferReadWrite
 *  -inputFile <fileName>      the json file from which to extract.
 *  -outputDir <outputDir>     the output file to write
 *  -nameSpace <nameSpace>     the nameSpace to read
 *  -properties <properties>   properties file
 *  -help                      print this message
 * 
 *  Example mvn command line:
 *   mvn -e exec:java -Dexec.mainClass=org.renci.databridge.tools.CollectionTransferReadWrite -Dexec.arguments="-inputFile",inputFile,"-outputDir",outputDir
 */

public class CollectionTransferReadWrite {


  private static CommandLine processArgs(String[] args) {
      Options options = new Options();
       Option inputFile = OptionBuilder.withArgName("inputFile").hasArg().withDescription("input file").create("inputFile");
      Option properties = OptionBuilder.withArgName("properties").hasArg().withDescription("properties file").create("properties");
       Option outputFile = OptionBuilder.withArgName("outputFile").hasArg().withDescription("output file").create("outputFile");
       Option nameSpace = OptionBuilder.withArgName("nameSpace").hasArg().withDescription("the nameSpace").create("nameSpace");
       Option help = new Option("help", "print this message");
       options.addOption(inputFile);
       options.addOption(outputFile);
       options.addOption(nameSpace);
       options.addOption(properties);
       options.addOption(help);

       // create the parser
       CommandLineParser parser = new GnuParser();
       CommandLine line = null;
       try {
           // parse the command line arguments
           line = parser.parse( options, args );
           if( line.hasOption( "help" ) ) {
              HelpFormatter formatter = new HelpFormatter();
              formatter.printHelp( "CollectionTransferReadWrite", options );
              System.exit(0);
           }
       }
       catch( ParseException exp ) {
           // oops, something went wrong
           System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
       }
       return line;
    }

  public static void main(String [] args) {

    String inputFile;
    String outputFile;
    String nameSpace;
    String dbType;
    String dbName;
    String dbHost;
    int    dbPort;
    String dbUser;
    String dbPwd;

    MetadataDAOFactory theFactory = null;

    try {
        CommandLine theLine = processArgs(args);

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

        inputFile = theLine.getOptionValue("inputFile");
        outputFile = theLine.getOptionValue("outputFile");
        nameSpace = theLine.getOptionValue("nameSpace");
        System.out.println("inputFile  " + inputFile);
        System.out.println("outputFile  " + outputFile);
        String bareInputFile = FilenameUtils.getBaseName(inputFile);

        // Create a reader for the input file
        BufferedReader br = new BufferedReader(new FileReader(inputFile));  

        // Create the node file and it's writer
        File oFile = new File(outputFile);
        if (!oFile.exists()) {
           oFile.createNewFile();
        }
        BufferedWriter nodeWriter = new BufferedWriter(new FileWriter(oFile.getAbsoluteFile()));  

        // Create the Gson object and read the file
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

        // Search for all of the collections in the nameSpace
        HashMap<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("nameSpace", nameSpace);
        Iterator<CollectionTransferObject> iterator1 = theCollectionDAO.getCollections(searchMap);

        if (iterator1.hasNext()) {
           CollectionTransferObject cto1 = iterator1.next();
           nodeWriter.write(gson.toJson(cto1));
        }

        nodeWriter.close();
     }  catch (Exception e) {
         e.printStackTrace();
     } finally {
         theFactory.closeTheDB();
     }
  }
}
