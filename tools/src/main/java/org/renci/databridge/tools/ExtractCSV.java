package org.renci.databridge.tools;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.*;
import org.renci.databridge.persistence.metadata.*;
/*
 * Use this to extract two csv files from a "viz" formatted json files. The first CSV
 * file will have the format: 
 *
 *    index,title,group,url
 *
 * while the second file will have the format
 *
 *   node1index,node2index,value
 *
 * usage: JsonToCsv
 *  -inputFile <fileName>      the json file from which to extract.
 *  -outputDir <outputDir>     the directory to which to write the csv files
 *  -properties <properties>   properties file
 *  -help                      print this message
 * 
 *  Example mvn command line:
 *   mvn -e exec:java -Dexec.mainClass=org.renci.databridge.tools.ExtractCSV -Dexec.arguments="-inputFile",inputFile,"-outputDir",outputDir
 */

public class ExtractCSV {

  /**
  * Private class used to represent nodes in the network
  */
  class JsonNode {
     public String name;
     public String title;
     public String group;
     public String URL;
     public String description;

    /**
      * Constructor that includes name (id) and the title of the nodes. 
      * @param name The id of the node, currently from the metadata database.
      * @param title The title of the node, currently from the metadata database.
      */
     public JsonNode(String name, String title, String group, String URL, String description) {
        this.name = name;
        this.title = title;
        this.group = group;
        this.URL = URL;
        this.description = description;
     }

     public String toString(int index) {
         return (index + ",\"" + this.title + "\"," + this.group + "," + this.URL);
     }
  }

  /**
   * Private class used to represent links (edges) in the network
   */
  class JsonLink {
     public int source;
     public int target;
     public Double value;

    /**
      * Constructor that includes source node id, target node id and the similarity of the nodes.
      * @param source The source node id as an index into the array of nodes
      * @param target The target node id as an index into the array of nodes
      * @param value  The similarity between the nodes.
      */
     public JsonLink(int source, int target, Double value) {
        this.source = source;
        this.target = target;
        this.value = value;
     }

     public String toString() {
         return (this.source + "," + this.target + "," + this.value);
     }
  }

  /**
   * Private class used to represent an entire JSON file as an array of nodes and links.
   */
  class JsonNetworkFile {
     public ArrayList<JsonNode> nodes;
     public ArrayList<JsonLink> links;

    /**
      * Constructor that initializes the node and link ArrayLists.
      */
     public JsonNetworkFile() {
         this.nodes = new ArrayList<JsonNode>();
         this.links = new ArrayList<JsonLink>();
     }

    /**
      * Add a node to the JSON file
      * @param newNode theNode to add
      */
     public void addNode(JsonNode newNode) {
         this.nodes.add(newNode);
     }

    /**
      * Add a link to the JSON file
      * @param newLink thelink to add
      */
     public void addLink(JsonLink newLink) {
         this.links.add(newLink);
     }

     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("Nodes: ");
         for (JsonNode jsn: this.nodes) {
             sb.append(System.getProperty("line.separator"));
             sb.append(jsn.toString());
         }

         sb.append(System.getProperty("line.separator"));
         sb.append("Links: ");
         for (JsonLink jsn: this.links) {
             sb.append(System.getProperty("line.separator"));
             sb.append(jsn.toString());
         }
         return sb.toString();
     }
  }
  

  private static CommandLine processArgs(String[] args) {
      Options options = new Options();
       Option inputFile = OptionBuilder.withArgName("inputFile").hasArg().withDescription("input file").create("inputFile");
      Option properties = OptionBuilder.withArgName("properties").hasArg().withDescription("properties file").create("properties");
       Option outputDir = OptionBuilder.withArgName("outputDir").hasArg().withDescription("output directory").create("outputDir");
       Option help = new Option("help", "print this message");
       options.addOption(inputFile);
       options.addOption(outputDir);
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
              formatter.printHelp( "ExtractCSV", options );
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
    String outputDir;
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
        // We'll need a collection DAO object.
        CollectionDAO theCollectionDAO = theFactory.getCollectionDAO();

        inputFile = theLine.getOptionValue("inputFile");
        outputDir = theLine.getOptionValue("outputDir");
        System.out.println("inputFile  " + inputFile);
        System.out.println("outputDir  " + outputDir);
        String bareInputFile = FilenameUtils.getBaseName(inputFile);

        // Create a reader for the input file
        BufferedReader br = new BufferedReader(new FileReader(inputFile));  

        // Create the node file and it's writer
        String nodeFileName = outputDir + System.getProperty("file.separator") + bareInputFile + "-nodes.csv";
        File nodeFile = new File(nodeFileName);
        if (!nodeFile.exists()) {
           nodeFile.createNewFile();
        }
        BufferedWriter nodeWriter = new BufferedWriter(new FileWriter(nodeFile.getAbsoluteFile()));  

        // write the header
        nodeWriter.write("index,title,group,url");
        nodeWriter.write(System.getProperty("line.separator"));

        // Create the link file and it's writer
        String linkFileName = outputDir + System.getProperty("file.separator") + bareInputFile + "-links.csv";
        File linkFile = new File(linkFileName);
        if (!linkFile.exists()) {
           linkFile.createNewFile();
        }
        BufferedWriter linkWriter = new BufferedWriter(new FileWriter(linkFile.getAbsoluteFile()));  

        // write the header
        linkWriter.write("node1index,node2index,value");
        linkWriter.write(System.getProperty("line.separator"));

        // Create the Gson object and read the file
        Gson gson = new Gson();
        JsonNetworkFile theFile = gson.fromJson(br, JsonNetworkFile.class);

        // Iterate throught the nodes, writing as we go
        Iterator<JsonNode> itrNodes = theFile.nodes.iterator();
        int i = 0;
        while(itrNodes.hasNext()) {
           JsonNode theNode = itrNodes.next();
           CollectionTransferObject thisCollectionObject = theCollectionDAO.getCollectionById(theNode.name);
           String keywords = ",\"";
           boolean notFirst = false;
           for (String thisKeyword : thisCollectionObject.getKeywords()) {
              if (notFirst) {
                 keywords += " " + thisKeyword; 
              } else {
                 // don't need the leading space this for the first keyword
                 keywords += thisKeyword; 
                 notFirst = true;
              } 
           }
           nodeWriter.write(theNode.toString(i) + keywords + "\""); 
           nodeWriter.write(System.getProperty("line.separator"));
           i++;
        } 
        nodeWriter.close();

        // Iterate throught the nodes, writing as we go
        Iterator<JsonLink> itrLinks = theFile.links.iterator();
        while(itrLinks.hasNext()) {
           JsonLink theLink = itrLinks.next();
           linkWriter.write(theLink.toString()); 
           linkWriter.write(System.getProperty("line.separator"));
        } 
        linkWriter.close();
     }  catch (Exception e) {
         e.printStackTrace();
     } finally {
         theFactory.closeTheDB();
     }
  }
}
