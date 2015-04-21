package org.renci.databridge.tools;

import java.io.*;
import java.util.*;
import org.apache.commons.cli.*;
import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.util.*;

/*
 * Use this class to add to the action table. 
 *
 * usage: AddAction
 *  -database <database>       use as mongo database
 *  -headers <headers>         the headers
 *  -help                      print this message
 *  -message <message>         message name
 *  -nameSpace <nameSpace>     the nameSpace
 *  -properties <properties>   properties file
 * 
 *  Example mvn command line:
 *   mvn -e exec:java -Dexec.mainClass=org.renci.databridge.tools.AddAction -Dexec.arguments=tools.conf,Processed.Metadata.To.MetadataDB,test_action_1,"outputFile:/home/howard/generatedNet/;className:org.renci.databridge.contrib.similarity.ncat.Measure"
 */

public class AddAction {

  private static CommandLine processArgs(String[] args) {
      Options options = new Options();
       Option properties = OptionBuilder.withArgName("properties").hasArg().withDescription("properties file").create("properties");
    //   Option database = OptionBuilder.withArgName("database").hasArg().withDescription("use as mongo database").create("database");
       Option message = OptionBuilder.withArgName("message").hasArg().withDescription("message name").create("message");
       Option nameSpace = OptionBuilder.withArgName("nameSpace").hasArg().withDescription("the nameSpace").create("nameSpace");
       Option headers = OptionBuilder.withArgName("headers").hasArg().withDescription("the headers").create("headers");
       Option help = new Option("help", "print this message");
       options.addOption(properties);
     //  options.addOption(database);
       options.addOption(message);
       options.addOption(nameSpace);
       options.addOption(headers);
       options.addOption(help);

       // create the parser
       CommandLineParser parser = new GnuParser();
       CommandLine line = null;
       try {
           // parse the command line arguments
           line = parser.parse( options, args );
           if( line.hasOption( "help" ) ) {
              HelpFormatter formatter = new HelpFormatter();
              formatter.printHelp( "CLITest", options );
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

    String dbType;
    String dbName;
    String dbHost;
    int    dbPort;

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

        // Connect to the mongo database
        if (dbType.compareToIgnoreCase("mongo") == 0) {
            theFactory = MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB,
                                                                  dbName, dbHost, dbPort);
            if (null == theFactory) {
               System.out.println("Couldn't produce the MetadataDAOFactory");
               return;
            }
        }

        // insert the action 
        ActionTransferObject theAction = new ActionTransferObject();
        theAction.setCurrentMessage(theLine.getOptionValue("message"));
        theAction.setNameSpace(theLine.getOptionValue("nameSpace"));
        HashMap<String, String> actionHeaders = new HashMap<String, String>();
        String[] pairs = theLine.getOptionValue("headers").split(";");

        for (int i = 0; i < pairs.length; i++) {
           String[] thisPair = pairs[i].split(":");
           System.out.println("thisPair: " + thisPair);
           actionHeaders.put(thisPair[0], thisPair[1]);
        }
        theAction.setHeaders(actionHeaders);
        ActionDAO theActionDAO = theFactory.getActionDAO();
        boolean result = theActionDAO.insertAction(theAction);
        System.out.println("Result of insert is " + result);
  
     }  catch (Exception e) {
         e.printStackTrace();
     } 
  }
}
