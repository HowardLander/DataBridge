package org.renci.databridge.engines.network;
import org.renci.databridge.util.*;
import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.persistence.network.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.rabbitmq.client.*;
import java.lang.Thread;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.io.IOException;
import org.renci.databridge.message.*;
import java.lang.reflect.*;
import org.la4j.*;
import org.la4j.matrix.functor.*;
import java.util.*;
import java.net.*;
import java.io.*;
import com.google.gson.*;
import java.nio.file.*;
import java.text.*;

/**
 * This class is executed in a thread of the Network Engine. The Network Engine
 * calls the constructor for this class with the AMQP message as a parameter.  It's
 * up to this class to decode the message according to the headers and implement the
 * required behaviors.
 *
 * @author Howard Lander -RENCI (www.renci.org)
 */

public class NetworkEngineMessageHandler implements AMQPMessageHandler {

   private Logger logger = Logger.getLogger ("org.renci.databridge.engine.network");

  // These are the individual portions of the message.
  // The routing key.
  private String routingKey;

  // The properties class.
  private com.rabbitmq.client.AMQP.BasicProperties properties;

  // The headers, expressed as a map of strings.
  private Map<String, String> stringHeaders;

  // The byte array for the contents of the message.
  private byte[] bytes;

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

     public String toString() {
         return (this.name + " " + this.title + " " + this.group);
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
         return (this.source + " " + this.target + " " + this.value);
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
  

  /**
   * This function essentially de-multiplexes the message by calling the
   * appropriate lower level handler based on the headers.
   *
   * @param amqpMessage The message to handle.
   * @param extra An object containing the needed DAO objects plus the Properties object
   */
  public void handle (AMQPMessage amqpMessage, Object extra) throws Exception {
      // Get the individual components of the the message and store
      // them in the fields
      routingKey = amqpMessage.getRoutingKey();
      properties = amqpMessage.getProperties();
      stringHeaders = amqpMessage.getStringHeaders();
      this.logger.log (Level.INFO, "headers: " + stringHeaders);
      bytes = amqpMessage.getBytes();

      // get the message name
      String messageName = stringHeaders.get(NetworkEngineMessage.NAME);
      if (null == messageName) {
         this.logger.log (Level.WARNING, "messageName is missing");
         return;
      }
      this.logger.log (Level.INFO, "messageName: " + messageName);

      // Call the function appropriate for the message
      if (messageName.compareTo(NetworkEngineMessage.INSERT_SIMILARITYMATRIX_JAVA_URI_NETWORKDB) == 0) {
         processInsertSimilarityMatrixJavaMessage(stringHeaders, extra);
      } else if (messageName.compareTo(NetworkEngineMessage.RUN_SNA_ALGORITHM_JAVA_NETWORKDB) == 0) {
         processRunSnaAlgorithmJavaMessage(stringHeaders, extra);
      } else if (messageName.compareTo(NetworkEngineMessage.RUN_SNA_ALGORITHM_FILEIO_NETWORKDB) == 0) {
         processRunSnaAlgorithmFileIOMessage(stringHeaders, extra);
      } else if (messageName.compareTo(NetworkEngineMessage.CREATE_JSON_FILE_NETWORKDB_URI) == 0) {
         processCreateJSONFileMessage(stringHeaders, extra);
      } else if (messageName.compareTo(NetworkListenerMessage.PROCESSED_METADATA_TO_NETWORKFILE) == 0) {
         processMetadataToNetworkFileMessage(stringHeaders, extra);
      } else if (messageName.compareTo(NetworkListenerMessage.ADDED_METADATA_TO_NETWORKDB) == 0) {
         processAddedMetadataToNetworkDBMessage(stringHeaders, extra);
      } else if (messageName.compareTo(NetworkListenerMessage.ADDED_SNA_TO_NETWORKDB) == 0) {
         processAddedSNAToNetworkDBMessage(stringHeaders, extra);
      } else if (messageName.compareTo(NetworkListenerMessage.JSON_FILE_CREATED) == 0) {
         processJSONFileCreated(stringHeaders, extra);
      } else {
         this.logger.log (Level.WARNING, "unimplemented messageName: " + messageName);
      }
  }

    /**
     * Handle the JSON_FILE_CREATED message. The only goal of this message is to take the
     * existing JSON file, copy it into the correct public_html directory and edit the filelist.txt
     * file.
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     */
  public void processJSONFileCreated( Map<String, String> stringHeaders, Object extra) {

      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the nameSpace
      String nameSpace = stringHeaders.get(NetworkListenerMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No nameSpace in message");
         return;
      }

      // 2) the json file
      String jsonFile = stringHeaders.get(NetworkListenerMessage.JSON_FILE);    
      if (null == jsonFile) {
         this.logger.log (Level.SEVERE, "No jsonFile in message");
         return;
      }

      // In this case the extra parameter is an array of 3 objects, which are the metadata and
      // network factories.
      Object extraArray[] = (Object[]) extra;

      MetadataDAOFactory theFactory = (MetadataDAOFactory) extraArray[0];
      if (null == theFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 

      // Preliminaries are out of the way. We can proceed to the main algorithm
      // Our task is to find the the action tasks corresponding to this message. If there
      // are any, we are going to copy the specfied json file to the required directory and add the
      // file name to the list of file names.  Note that this is HIGHLY specific to out current viz
      // application.
      // Get the list of needed actions
      ActionTransferObject theAction = new ActionTransferObject();
      ActionDAO theActionDAO = theFactory.getActionDAO();

      this.logger.log (Level.INFO, "Searching for message: " + NetworkListenerMessage.JSON_FILE_CREATED + " and nameSpace " + nameSpace);
      Iterator<ActionTransferObject> actionIt =
             theActionDAO.getActions(NetworkListenerMessage.JSON_FILE_CREATED, nameSpace);

      String outputFile = null;
      while (actionIt.hasNext()) {
         theAction = actionIt.next();
         this.logger.log (Level.INFO, "Found action: " + theAction.getDataStoreId());
         HashMap<String, String> actionHeaders = theAction.getHeaders();
         String vizTargetDir = (String) actionHeaders.get(NetworkListenerMessage.VIZ_TARGET_DIR); 
         String vizListFile = (String) actionHeaders.get(NetworkListenerMessage.VIZ_LIST_FILE); 

         // Copy the file into the correct directory
         try {
            Path existingPath = FileSystems.getDefault().getPath(jsonFile);
            Path newPath = FileSystems.getDefault().getPath(vizTargetDir + 
                           System.getProperty("file.separator") + 
                           "data" + System.getProperty("file.separator") +
                            existingPath.getFileName().toString());
            Files.copy(existingPath, newPath, java.nio.file.StandardCopyOption.COPY_ATTRIBUTES);

            // Now add the file name to the "filelist.txt" (I told you this was specific!).
            File fileList = new File(vizTargetDir + System.getProperty("file.separator") + "filelist.txt");
            Writer output = new BufferedWriter(new FileWriter(fileList, true));

            // The file names have a .json extension, but the filelist.txt wants the name without the 
            // extension
            String completeFileName = existingPath.getFileName().toString();
            String fileNameWithoutExtension = 
                completeFileName.substring(0, completeFileName.lastIndexOf("."));
            output.append(fileNameWithoutExtension + System.getProperty("line.separator"));
            output.close();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
  }


    /**
     * Handle the PROCESSED_METADATA_TO_NETWORKFILE message.  Primarily, we are going to search
     * the action table and call the processInsertSimilarityMatrixJavaMessage code for each matching
     * action. There is some remapping of the headers involved as well.
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     */
  public void processMetadataToNetworkFileMessage( Map<String, String> stringHeaders, Object extra) {

      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the nameSpace
      String nameSpace = stringHeaders.get(NetworkEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No nameSpace in message");
         return;
      }

      // 2) the similarity_id
      String similarityId = stringHeaders.get(NetworkEngineMessage.SIMILARITY_ID);    
      if (null == similarityId) {
         this.logger.log (Level.SEVERE, "No similarityId in message");
         return;
      }

      // In this case the extra parameter is an array of 3 objects, which are the metadata and
      // network factories.
      Object extraArray[] = (Object[]) extra;

      MetadataDAOFactory theFactory = (MetadataDAOFactory) extraArray[0];
      if (null == theFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 

      // Preliminaries are out of the way. We can proceed to the main algorithm
      // Our task is to find the the action tasks corresponding to this message. If there
      // are, we find the file name, and re-transmit the rquired headers to the appropriate
      // lower level call.
          // Get the list of needed actions
     ActionTransferObject theAction = new ActionTransferObject();
     ActionDAO theActionDAO = theFactory.getActionDAO();

     Iterator<ActionTransferObject> actionIt =
            theActionDAO.getActions(NetworkListenerMessage.PROCESSED_METADATA_TO_NETWORKFILE, nameSpace);

        String outputFile = null;
     while (actionIt.hasNext()) {
        // At the moment, we really aren't expecting there to be more than one entry for
        // the combination of nameSpace and action.  Not only that, but we don't currently need
        // anything out of the action table for this op. But we still need to make a call to next
        // or else we get stuck in an infinite loop.
        theAction = actionIt.next();
        HashMap<String, String> passedHeaders = new HashMap<String, String>();
        SimilarityInstanceDAO theSimilarityInstanceDAO = theFactory.getSimilarityInstanceDAO();
        SimilarityInstanceTransferObject theSimilarity =
             theSimilarityInstanceDAO.getSimilarityInstanceById(similarityId);
        outputFile = theSimilarity.getOutput();

        passedHeaders.put(NetworkEngineMessage.INPUT_URI, outputFile);
        this.logger.log(Level.INFO, 
            "passing headers to processInsertSimilarityMatrixJavaMessage: " + passedHeaders);
        processInsertSimilarityMatrixJavaMessage(passedHeaders, extra);
     }
  }


    /**
     * Handle the ADDED_METADATA_TO_NETWORKDB message.  Primarily, we are going to search
     * the action table and call the processRunSnaAlgorithmJavaMessage code for each matching
     * action. There is some remapping of the headers involved as well.
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     */
  public void processAddedMetadataToNetworkDBMessage( Map<String, String> stringHeaders, Object extra) {

      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the nameSpace
      String nameSpace = stringHeaders.get(NetworkEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No nameSpace in message");
         return;
      }

      // 2) the similarity_id
      String similarityId = stringHeaders.get(NetworkEngineMessage.SIMILARITY_ID);    
      if (null == similarityId) {
         this.logger.log (Level.SEVERE, "No similarityId in message");
         return;
      }

      // In this case the extra parameter is an array of 2 objects, which are the metadata and
      // network factories.
      Object extraArray[] = (Object[]) extra;

      MetadataDAOFactory theFactory = (MetadataDAOFactory) extraArray[0];
      if (null == theFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 

      // Preliminaries are out of the way. We can proceed to the main algorithm
      // Our task is to find the the action tasks corresponding to this message. If there
      // are, we find the file name, and re-transmit the rquired headers to the appropriate
      // lower level call.
          // Get the list of needed actions
     ActionTransferObject actionObject = new ActionTransferObject();
     ActionDAO theActionDAO = theFactory.getActionDAO();

     Iterator<ActionTransferObject> actionIt =
            theActionDAO.getActions(NetworkListenerMessage.ADDED_METADATA_TO_NETWORKDB, nameSpace);

        String outputFile = null;
     while (actionIt.hasNext()) {
        // For each of the actions we find we want to retrieve the className so we can pass it along.
        actionObject = actionIt.next();

        // Headers to pass forward
        HashMap<String, String> passedHeaders = new HashMap<String, String>();

        // Get the class from the action object
        HashMap<String, String> actionHeaders = actionObject.getHeaders();
        passedHeaders.put(RelevanceEngineMessage.CLASS,
                          (String) actionHeaders.get(RelevanceEngineMessage.CLASS));

        passedHeaders.put(NetworkListenerMessage.NAME_SPACE, nameSpace);
        passedHeaders.put(NetworkListenerMessage.SIMILARITY_ID, similarityId);
        this.logger.log(Level.INFO, "passing headers to processRunSnaAlgorithmJavaMessage: " + passedHeaders);
        processRunSnaAlgorithmJavaMessage(passedHeaders, extra);
     }
  }


    /**
     * Handle the ADDED_SNA_TO_NETWORKDB message.  Primarily, we are going to search
     * the action table and call the processCreateJSONFileMessage code for each matching
     * action. There is some remapping of the headers involved as well.
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     */
  public void processAddedSNAToNetworkDBMessage( Map<String, String> stringHeaders, Object extra) {

      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the nameSpace
      String nameSpace = stringHeaders.get(NetworkListenerMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No nameSpace in message");
         return;
      }

      // 2) the sna_id
      String snaId = stringHeaders.get(NetworkListenerMessage.SNA_ID);    
      if (null == snaId) {
         this.logger.log (Level.SEVERE, "No snaId in message");
         return;
      }

      // In this case the extra parameter is an array of 2 objects, which are the metadata and
      // network factories.
      Object extraArray[] = (Object[]) extra;

      MetadataDAOFactory theFactory = (MetadataDAOFactory) extraArray[0];
      if (null == theFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 

      // Preliminaries are out of the way. We can proceed to the main algorithm
      // Our task is to find the the action tasks corresponding to this message. If there
      // are, we find the file name, and re-transmit the rquired headers to the appropriate
      // lower level call.
      // Get the list of needed actions
      ActionTransferObject actionObject = new ActionTransferObject();
      ActionDAO theActionDAO = theFactory.getActionDAO();

      Iterator<ActionTransferObject> actionIt =
            theActionDAO.getActions(NetworkListenerMessage.ADDED_SNA_TO_NETWORKDB, nameSpace);

      String outputFile = null;
      while (actionIt.hasNext()) {
         // For each of the actions we find we want to retrieve the similarityId so we can pass it along.
         actionObject = actionIt.next();

         // Headers to pass forward
         HashMap<String, String> passedHeaders = new HashMap<String, String>();

         // We need the SNA instance to retrieve the similarity id.
         SNAInstanceDAO theSNAInstanceDAO = theFactory.getSNAInstanceDAO();
         SNAInstanceTransferObject snaObject = theSNAInstanceDAO.getSNAInstanceById(snaId);

         // We want to add the class name that generated the similarity to the fileName
         // We can retrieve this from the similarity instance
         SimilarityInstanceDAO theSimilarityInstanceDAO = theFactory.getSimilarityInstanceDAO();
         SimilarityInstanceTransferObject similarityObject = 
            theSimilarityInstanceDAO.getSimilarityInstanceById(snaObject.getSimilarityInstanceId());

         // Let's get the last element of the similarity class name for the file name
         String fullSimClassName = similarityObject.getClassName();
         String simClass = fullSimClassName.substring(fullSimClassName.lastIndexOf('.') + 1); 

         // Let's get the last element of the sna class name for the file name
         String fullSNAClassName = snaObject.getClassName();
         String SNAClass = null;

         // Since the className can also be an executable, we check the first character. If it's a 
         // "/" than we have an executable and we want to use "/" instead of "." as the seperator.
         if (fullSNAClassName.startsWith("/")) {
             SNAClass = fullSNAClassName.substring(fullSNAClassName.lastIndexOf('/') + 1); 
         } else {
             SNAClass = fullSNAClassName.substring(fullSNAClassName.lastIndexOf('.') + 1); 
         }
         // Get the class from the action object
         HashMap<String, String> actionHeaders = actionObject.getHeaders();
         outputFile = (String) actionHeaders.get(NetworkListenerMessage.OUTPUT_FILE);
         // If the outputFile specified is a file, we pass it on. If it's a directory, we generate
         // a file name in that directory and pass that on.
         String lastChar = outputFile.substring(outputFile.length() - 1);
         String fileName = null;
         if (lastChar.compareTo("/") == 0) {
            // The user has given us a directory, so we need to append a fileName
            // We'll let java create the tmp file name, then delete the file.
            try {
               File outFileObject = new File(outputFile);
   
               // Create the directory if it does not already exist
               if (outFileObject.exists() == false) {
                   boolean result = outFileObject.mkdirs();
                   if (false == result) {
                       this.logger.log (Level.WARNING, "can't create path: " + outputFile);
                       return;
                   }
               }

               // Let's add a the date and time as well.
               Date now = new Date();
               SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
               String dateString = format.format(now);
               String labeledFileName = nameSpace + "-" + simClass + "-" + SNAClass + "-" + dateString + ".json";
               fileName = outputFile + labeledFileName;
            } catch (Exception e) {
               e.printStackTrace();
            }
         } else {
            // the user has given us a path
            fileName = outputFile;
         } 

         passedHeaders.put(NetworkListenerMessage.NAME_SPACE, nameSpace);
         passedHeaders.put(NetworkListenerMessage.SIMILARITY_ID, snaObject.getSimilarityInstanceId());
         passedHeaders.put(NetworkListenerMessage.SNA_ID, snaId);
         passedHeaders.put(NetworkListenerMessage.OUTPUT_FILE, fileName);
         this.logger.log (Level.INFO, "passing headers to processCreateJSONFileMessage: " + passedHeaders);
         processCreateJSONFileMessage(passedHeaders, extra);
      }
  }

    /**
     * Handle the CREATE_JSON_FILE_NETWORKDB_URI  message by reading the network information and using
     * the Google gson code to write out a Json formatted file for the use of several applications. Note
     * that the initial supported JSON file format has been defined by an existing viz application. So
     * some of the field names don't currently match the DataBridge terminology.  
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     */
  public void processCreateJSONFileMessage( Map<String, String> stringHeaders, Object extra) {

      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the nameSpace
      String nameSpace = stringHeaders.get(NetworkEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No nameSpace in message");
         return;
      }

      // 2) the similarity_id
      String similarityId = stringHeaders.get(NetworkEngineMessage.SIMILARITY_ID);    
      if (null == similarityId) {
         this.logger.log (Level.SEVERE, "No similarityId in message");
         return;
      }

      // 3) the sna_id: This can be null, user just won't get any cluster info
      String snaId = stringHeaders.get(NetworkEngineMessage.SNA_ID);    

      // 4) the output file
      String outputFile = stringHeaders.get(NetworkEngineMessage.OUTPUT_FILE);    
      if (null == outputFile) {
         this.logger.log (Level.SEVERE, "No output file in message");
         return;
      }

      // In this case the extra parameter is an array of 2 objects, which are the metadata and
      // network factories.
      Object extraArray[] = (Object[]) extra;

      MetadataDAOFactory metadataFactory = (MetadataDAOFactory) extraArray[0];
      if (null == metadataFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 
      CollectionDAO theCollectionDAO = metadataFactory.getCollectionDAO();
      if (null == theCollectionDAO) {
         this.logger.log (Level.SEVERE, "CollectionDAO is null");
         return;
      } 

      NetworkDAOFactory networkFactory = (NetworkDAOFactory) extraArray[1];
      if (null == networkFactory) {
         this.logger.log (Level.SEVERE, "NetworkDAOFactory is null");
         return;
      } 

      NetworkDyadDAO theDyadDAO = networkFactory.getNetworkDyadDAO();
      if (null == theDyadDAO) {
         this.logger.log (Level.SEVERE, "theDyadDAO is null");
         return;
      } 

      NetworkNodeDAO theNodeDAO = networkFactory.getNetworkNodeDAO();
      if (null == theNodeDAO) {
         this.logger.log (Level.SEVERE, "theNodeDAO is null");
         return;
      } 

      Properties theProps = (Properties) extraArray[2];
      if (null == theProps) {
         this.logger.log (Level.SEVERE, "Properties object is null");
         return;
      }

      // Preliminaries are out of the way. We can proceed to the main algorithm
      // Used to assure that we only add each node to the file once.  
      ArrayList<String> theNames = new ArrayList<String>();
      JsonNetworkFile theJson = new JsonNetworkFile();

      // All the dyads are accounted for so we can use the google gson lib to write this out.
      Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

      // Let's get all of the Dyads
      Iterator<NetworkDyadTransferObject> theDyads = theDyadDAO.getNetworkDyads(nameSpace, similarityId);
      while (theDyads.hasNext()) {
         NetworkDyadTransferObject thisDyad = theDyads.next();
         String id1 = thisDyad.getNode1MetadataId();
         String id2 = thisDyad.getNode2MetadataId();
         int index1 = theNames.indexOf(id1);
         if (index1 == -1) {
            theNames.add(id1);
            index1 = theNames.indexOf(id1);

            // Get the relevant collection from the metadata database
            CollectionTransferObject node1Collection = theCollectionDAO.getCollectionById(id1);

            // if the user has provided an snaID, than they want cluster info for the node.
            String clusterString = "";
            if (null != snaId) {
                NetworkNodeTransferObject node1 = theNodeDAO.getNetworkNode(thisDyad.getNode1DataStoreId());
                clusterString = (String)theNodeDAO.getPropertyFromNetworkNode(node1, snaId);
            }
   
            // Now we can add the first node to the file
            JsonNode jNode = new JsonNode(id1, node1Collection.getTitle(), clusterString,
                                          node1Collection.getURL(), node1Collection.getDescription());
            theJson.addNode(jNode);
         }

         // Dyad could be a singleton so check for null.
         if (id2 != null) {
            // There is a second node, so we can add both it and the link;
            int index2 = theNames.indexOf(id2);
            if (index2 == -1) {
               theNames.add(id2);
               index2 = theNames.indexOf(id2);
               // Get the relevant collection from the metadata database
               CollectionTransferObject node2Collection = theCollectionDAO.getCollectionById(id2);
   
               // if the user has provided an snaID, than they want cluster info for the node.
               String clusterString2 = "";
               if (null != snaId) {
                   NetworkNodeTransferObject node2 = theNodeDAO.getNetworkNode(thisDyad.getNode2DataStoreId());
                   clusterString2= (String)theNodeDAO.getPropertyFromNetworkNode(node2, snaId);
               }

               // Now we can add the second node to the file
               JsonNode jNode2 = new JsonNode(id2, node2Collection.getTitle(), clusterString2,
                                              node2Collection.getURL(), node2Collection.getDescription());
               theJson.addNode(jNode2);
            }

            // We can also add the link
            JsonLink theLink = new JsonLink(index1, index2, thisDyad.getSimilarity());
            theJson.addLink(theLink);
         }
      }

      try {
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));
         writer.write(gson.toJson(theJson));
         writer.close();
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "exception in processCreateJSONFileMessage: "+ e.getMessage(), e);
         return;
      }

      // Assuming we get this far, we want to send out the next message
      AMQPComms ac = null;
      try {
         ac = new AMQPComms (theProps);
         String headers = JSONFileCreated.getSendHeaders (
                             nameSpace, outputFile);
         this.logger.log (Level.FINER, "Send headers: " + headers);
         ac.publishMessage (new AMQPMessage (), headers, true);
         this.logger.log (Level.FINE, "Sent JSONFileCreated message.");
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception sending action message: " + e.getMessage());
      } finally {
         if (null != ac) {
             ac.shutdownConnection ();
         }
      }
  }


    /**
     * Handle the INSERT_SIMILARITYMATRIX_JAVA_URI_NETWORKDB message by inserting the
     * nodes and relationships into the database.
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     */
  public void processInsertSimilarityMatrixJavaMessage( Map<String, String> stringHeaders, Object extra) {

      // This function is going to need a private class to use with the eachNonZero call in the 
      // matrix, so here it is.
      class RelationshipInserter implements MatrixProcedure {
          private ArrayList<NetworkNodeTransferObject> nodeList;
          private NetworkRelationshipDAO theNetworkRelationshipDAO; 
          private String similarityInstanceId;

          /**
           * Constructor that includes the nodeList, DAO and similarity ID
           * needed by the callback function
           * @param nodeList An ArrayList of all of the node objects
           * @param theNetworkRelationshipDAO Needed to do the insert
           * @param similarityInstanceId Used as the type of the relationship. Comes from
           *                             the metadata database.
           */
          RelationshipInserter(ArrayList<NetworkNodeTransferObject> nodeList,
                               NetworkRelationshipDAO theNetworkRelationshipDAO,
                               String similarityInstanceId) {
              this.nodeList = nodeList;
              this.theNetworkRelationshipDAO = theNetworkRelationshipDAO;
              this.similarityInstanceId = similarityInstanceId;
          }

          /**
           * Callback function called for every non zero element in the sparse matrix.
           * The function is used to insert the relationships into the network that
           * represent the similarity values for the matrix. The lable for the relationship
           * is the id in the metadata database that represents the similarity instance that 
           * produced the similarity matrix.
           * @param i The row for the entry
           * @param j The column for the entry
           * @param value The value in the (i,j) entry for the matrix
           */
          public void apply(int i, int j, double value) {

              if (value <= 0.) {
                 // This is neccesary because we have set some cells to -1 in order to avoid a bug with
                 // empty rows in the CRS matrix class we are using.
                 return;
              }

              NetworkRelationshipTransferObject theNetworkTransfer = new NetworkRelationshipTransferObject();

              // Set the type using the similarityInstanceId from the metadata database
              theNetworkTransfer.setType(this.similarityInstanceId);

              // Add the value attribute to the relationship
              HashMap<String, Object> netAttributes = new HashMap<String, Object>();
              netAttributes.put(NetworkRelationshipDAO.METADATA_SIMILARITY_PROPERTY_NAME, value);
              theNetworkTransfer.setAttributes(netAttributes);

              // Should eventually log the result
              boolean result = this.theNetworkRelationshipDAO.insertNetworkRelationship(theNetworkTransfer, 
                                                                                        this.nodeList.get(i),
                                                                                        this.nodeList.get(j));
          }
      }

      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the input uri
      String inputURI = stringHeaders.get(NetworkEngineMessage.INPUT_URI);    
      if (null == inputURI) {
         this.logger.log (Level.SEVERE, "No inputURI in message");
         return;
      }

      // In this case the extra parameter is an array of 2 objects, which are the metadata and
      // network factories plus the Properties object used to send the next message.
      Object extraArray[] = (Object[]) extra;

      MetadataDAOFactory metadataFactory = (MetadataDAOFactory) extraArray[0];
      if (null == metadataFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 
      CollectionDAO theCollectionDAO = metadataFactory.getCollectionDAO();
      if (null == theCollectionDAO) {
         this.logger.log (Level.SEVERE, "CollectionDAO is null");
         return;
      } 

      NetworkDAOFactory networkFactory = (NetworkDAOFactory) extraArray[1];
      if (null == networkFactory) {
         this.logger.log (Level.SEVERE, "NetworkDAOFactory is null");
         return;
      } 

      Properties theProps = (Properties) extraArray[2];
      if (null == theProps) {
         this.logger.log (Level.SEVERE, "Properties object is null");
         return;
      }

      // We'll need a DAO
      NetworkNodeDAO theNetworkNodeDAO = networkFactory.getNetworkNodeDAO();
      NetworkRelationshipDAO theNetworkRelationshipDAO = networkFactory.getNetworkRelationshipDAO();
      // Let's start by reading in the URI/file. If the first character is a slash
      // then we'll assume it's a file, otherwise we'll assume it's a URI. Note, this may not
      // work on windows.
      SimilarityFile theFile = new SimilarityFile();
      try {
          theFile.readFromURL(inputURI);
      } catch (Exception e) {
          this.logger.log (Level.SEVERE, "exception in processInsertSimilarityMatrixJavaMessage: "+ e.getMessage(), e);
          e.printStackTrace();
      }

      // First things first: if a matrix with this similariId is already in the network database, we
      // are done.  The Id's are unique for every run, so the only thing we would be doing is mucking
      // up the network database with duplicate relationships.  Nobody wants that ...
      String theSimId = theFile.getSimilarityInstanceId();
      String nameSpace = theFile.getNameSpace();

      Iterator<NetworkNodeTransferObject> theNodes = theNetworkNodeDAO.getNetworkNodesForNameSpace(nameSpace);
      if (theNodes.hasNext()) {
         // This nameSpace has been inserted, how about the unique combo of nameSpace and similarityInstance
         // Because each node in the nameSpace gets the similarityId whether or not it has any non-zero
         // similarities, we only need to check the first node. Also, if there are no nodes, than certainly
         // the similarity id has not been inserted.
         NetworkNodeTransferObject firstNode = theNodes.next();
         Object value = theNetworkNodeDAO.getPropertyFromNetworkNode(firstNode, theSimId);
         if (null != value) {
             // similarity matrix has already been inserted.
             this.logger.log (Level.INFO, "similarityId " + theSimId + " already inserted" );
             return;
         }
      } 

      // Here is a classic space vs time tradeoff: we are going to keep all of the Node
      // structures in memory because we will need them later to insert the relationships. The 
      // alternative would be lots of search and retrieve.
      ArrayList<NetworkNodeTransferObject> nodeList = new ArrayList<NetworkNodeTransferObject>();

      // Now we'll add all of the nodes. Note that is all of the nodes in the file, not just the
      // ones with non-zero values.
      int indexInMatrix = 0;
      for (String theId: theFile.getCollectionIds()) {
          NetworkNodeTransferObject theNode = new NetworkNodeTransferObject();
          theNode.setNameSpace(nameSpace);

          // Save for later
          theNode.setNodeId(theId);
          nodeList.add(theNode);
          // If a node is already in the database, this is not a failure
          int result = theNetworkNodeDAO.insertNetworkNode(theNode);
          if (result < 0) {
             this.logger.log (Level.SEVERE, "failure on insertNetworkNode");
             return;
          }

          // Store the index in the original matrix of this node for this similarity.  This
          // will be used later on to reconstruct the original matrix. Note that the matrix
          // is symetric, so we only need to store one index.
          theNetworkNodeDAO.addPropertyToNetworkNode(theNode, theFile.getSimilarityInstanceId(), indexInMatrix);
          indexInMatrix ++;
      }

      // Add the similarity matrix as relationships between nodes.
      org.la4j.matrix.sparse.CRSMatrix theMatrix = theFile.getSimilarityMatrix();

      // Create an instance of the inserter class, which actually does all the work.
      // note that we were using the eachNonZero call, but it seems to have a bug in it, so 
      // we essentially wrote our own and called it. Didn't really need a class as it turned out.
      // Here's the commented out line: theMatrix.eachNonZero(theInserter);
      RelationshipInserter theInserter = 
          new RelationshipInserter(nodeList, theNetworkRelationshipDAO, theFile.getSimilarityInstanceId());
      for (int i = 0; i < theMatrix.rows(); i++) {
         for (int j = 0; j < theMatrix.columns(); j++) {
            if (theMatrix.get(i,j) != 0.) {
               theInserter.apply(i, j, theMatrix.get(i,j));
            }
         }
      }
 
      // Assuming we get this far, we want to send out the next message
      AMQPComms ac = null;
      try {
         ac = new AMQPComms (theProps);
         String headers = AddedMetadataToNetworkDB.getSendHeaders (
                             nameSpace, theFile.getSimilarityInstanceId());
         this.logger.log (Level.FINER, "Send headers: " + headers);
         ac.publishMessage (new AMQPMessage (), headers, true);
         this.logger.log (Level.FINE, "Sent AddedMetadataToNetworkDB message.");
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception sending action message: " + e.getMessage());
      } finally {
         if (null != ac) {
             ac.shutdownConnection ();
         }
      }
  }

    /**
     * Handle the RUN_SNA_ALGORITHM_JAVA_NETWORKDB message by executing the specified
     * code to run the SNA algorithm.
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     */
  public void processRunSnaAlgorithmJavaMessage( Map<String, String> stringHeaders, Object extra) {
      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the class name
      String className = stringHeaders.get(NetworkEngineMessage.CLASS);    
      if (null == className) {
         this.logger.log (Level.SEVERE, "No class name in message");
         return;
      }

      // Let's try to load the class.
      Class<?> theClass = null;
      ClassLoader classLoader = NetworkEngineMessageHandler.class.getClassLoader();
      try {
         theClass = classLoader.loadClass(className);
      } catch (ClassNotFoundException e) {
         this.logger.log (Level.SEVERE, "Can't instantiate class " + className + ": " + e.getMessage(), e);
         return;
      }

      // We'll need an object of this type as well.
      Constructor<?> cons = null;
      Object theObject = null;
      NetworkProcessor thisProcessor = null;
      try {
         cons = (Constructor<?>) theClass.getConstructor(null);
         theObject = cons.newInstance(null);
         thisProcessor = (NetworkProcessor) theObject;
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't create instance" + e.getMessage(), e);
         return;
      }

      // 2) the name space
      String nameSpace = stringHeaders.get(NetworkEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No name space in message");
         return;
      }

      // 3) the similarityId
      String similarityId = stringHeaders.get(NetworkEngineMessage.SIMILARITY_ID);    
      if (null == similarityId) {
         this.logger.log (Level.SEVERE, "No similarityId in message");
         return;
      }

      // 4) any extra params to pass.  This can be null
      String params = stringHeaders.get(NetworkEngineMessage.PARAMS);    

      // In this case the extra parameter is an array of 2 objects, which are the metadata and
      // network factories plus the Properties object used to send the next message.
      Object[] extraArray = (Object[]) extra;

      MetadataDAOFactory metadataFactory = (MetadataDAOFactory) extraArray[0];
      if (null == metadataFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 
      CollectionDAO theCollectionDAO = metadataFactory.getCollectionDAO();
      if (null == theCollectionDAO) {
         this.logger.log (Level.SEVERE, "CollectionDAO is null");
         return;
      } 

      NetworkDAOFactory networkFactory = (NetworkDAOFactory) extraArray[1];
      if (null == networkFactory) {
         this.logger.log (Level.SEVERE, "NetworkDAOFactory is null");
         return;
      } 

      Properties theProps = (Properties) extraArray[2];
      if (null == theProps) {
         this.logger.log (Level.SEVERE, "Properties object is null");
         return;
      }

      SNAInstanceDAO theSNAInstanceDAO = metadataFactory.getSNAInstanceDAO();
      if (null == theSNAInstanceDAO) {
         this.logger.log (Level.SEVERE, "SNAInstanceDAO is null");
         return;
      }

      NetworkNodeDAO theNetworkNodeDAO = networkFactory.getNetworkNodeDAO();
      if (null == theSNAInstanceDAO) {
         this.logger.log (Level.SEVERE, "SNAInstanceDAO is null");
         return;
      }

      // Let's add the SNAInstance.
      SNAInstanceTransferObject theSNAInstance = new SNAInstanceTransferObject();
      theSNAInstance.setNameSpace(nameSpace);
      theSNAInstance.setClassName(className);
      theSNAInstance.setMethod("processNetwork");
      theSNAInstance.setSimilarityInstanceId(similarityId);

      // let's find the highest version for this combination of nameSpace, className and method (if any)
      HashMap<String, String> versionMap = new HashMap<String, String>();
      versionMap.put("nameSpace", nameSpace);
      versionMap.put("className", className);
      versionMap.put("method", "processNetwork");
      versionMap.put("similarityInstanceId", similarityId);

      HashMap<String, String> sortMap = new HashMap<String, String>();
      sortMap.put("version", SNAInstanceDAO.SORT_DESCENDING);
      Integer limit = new Integer(1);

      // This is for the case of no previous instance
      theSNAInstance.setVersion(1);
      Iterator<SNAInstanceTransferObject> versionIterator =
          theSNAInstanceDAO.getSNAInstances(versionMap, sortMap, limit);
      if (versionIterator.hasNext()) {
         // Found a previous instance
         SNAInstanceTransferObject prevInstance = versionIterator.next();
         theSNAInstance.setVersion(prevInstance.getVersion() + 1);
      }

      try {
         boolean result = theSNAInstanceDAO.insertSNAInstance(theSNAInstance);
         this.logger.log(Level.INFO, "Inserted Instance: " + theSNAInstance.getDataStoreId());
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't insert SNA instance");
         return;
      }

      // TODO: make sure we only pass the connected nodes to the processNetwork call.  Add the
      // the singletons to the network later.  See how this is done in processRunSnaAlgorithmFileIOMessage 
      // code.
      NetworkDyadDAO theNetworkDyadDAO = networkFactory.getNetworkDyadDAO();
      Iterator<NetworkDyadTransferObject> theDyads = 
           theNetworkDyadDAO.getNetworkDyads(nameSpace, similarityId);

      HashMap<String, String[]> clusterList = null;
      try {
          // Invoke the method
          clusterList = (HashMap<String, String[]>)thisProcessor.processNetwork(theDyads, params);
          String nReturnedClusters = Integer.toString(clusterList.size());
          HashMap<String, String> updateMap = new HashMap<String, String>();
          updateMap.put("nResultingClusters", nReturnedClusters);
          boolean result = theSNAInstanceDAO.updateSNAInstance(theSNAInstance, updateMap);
          String SNAId = theSNAInstance.getDataStoreId();
         
          // For each returned cluster, add the cluster info to the nodes.
          // We are allowing the possibility of a node being in more than one cluster
          for (Map.Entry<String, String[]> thisCluster : clusterList.entrySet()){
             String clusterKey = thisCluster.getKey();
             String[] nodesInThisCluster = thisCluster.getValue();
             for (String thisNodeId: nodesInThisCluster) {
                 NetworkNodeTransferObject theNode = theNetworkNodeDAO.getNetworkNode(thisNodeId);
                 String clusterString = 
                    (String) theNetworkNodeDAO.getPropertyFromNetworkNode(theNode, SNAId);
                 if (null == clusterString) {
                    // No problem, just means this node is not already in a cluster
                    theNetworkNodeDAO.addPropertyToNetworkNode(theNode, SNAId, clusterKey);
                 } else {
                    // This node was already assigned to a cluster, so we add this id
                    String multiClusterString = clusterString = "," + clusterKey;
                    theNetworkNodeDAO.deletePropertyFromNetworkNode(theNode, SNAId);
                    theNetworkNodeDAO.addPropertyToNetworkNode(theNode, SNAId, multiClusterString);
                 }
             }
          }
          
      } catch (Exception e) {
          this.logger.log (Level.SEVERE, "Can't invoke method " + "processNetwork" + " " + e.getMessage());
          return;
      }

      // Assuming we get this far, we want to send out the next message
      AMQPComms ac = null;
      try {
         ac = new AMQPComms (theProps);
         String headers = AddedSNAToNetworkDB.getSendHeaders (
                             nameSpace, theSNAInstance.getDataStoreId());
         this.logger.log (Level.FINER, "Send headers: " + headers);
         ac.publishMessage (new AMQPMessage (), headers, true);
         this.logger.log (Level.FINE, "Sent AddedSNAToNetworkDB message.");
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception sending action message: " + e.getMessage());
      } finally {
         if (null != ac) {
             ac.shutdownConnection ();
         }
      }
  }

    /**
     * Handle the RUN_SNA_ALGORITHM_FILEIO_NETWORKDB message by executing the specified
     * code to run the SNA algorithm. In this case the algorithm has to be contained in a 
     * file that can be executed using the java Process implementation. The executable
     * will be called with three positional arguments: 
     *    the input file: a CSV file starting with a single line containing the number of distinct nodes
     *    in the file followed by a set of triples representing the 
     *    i,j and similarity values where i and j are the indices of the 2 nodes.
     *    the output file: The CSV file to be produced by the executable consisting of a set of pairs
     *    representing the index value of the node and the integer group id into which the node has been
     *    assigned. The group ids start at 0 and are positive integers.
     *    params: A string containing any additional arguments to pass to the executable.
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     */
  public void processRunSnaAlgorithmFileIOMessage( Map<String, String> stringHeaders, Object extra) {
      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the exectable name
      String executable = stringHeaders.get(NetworkEngineMessage.EXECUTABLE);    
      if (null == executable) {
         this.logger.log (Level.SEVERE, "No executable in message");
         return;
      }

      // 2) the name space
      String nameSpace = stringHeaders.get(NetworkEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No name space in message");
         return;
      }

      // 3) the similarityId
      String similarityId = stringHeaders.get(NetworkEngineMessage.SIMILARITY_ID);    
      if (null == similarityId) {
         this.logger.log (Level.SEVERE, "No similarityId in message");
         return;
      }

      // 4) any extra params to pass.  This can be null
      String params = stringHeaders.get(NetworkEngineMessage.PARAMS);    

      // In this case the extra parameter is an array of 2 objects, which are the metadata and
      // network factories plus the Properties object used to send the next message.
      Object[] extraArray = (Object[]) extra;

      MetadataDAOFactory metadataFactory = (MetadataDAOFactory) extraArray[0];
      if (null == metadataFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 
      CollectionDAO theCollectionDAO = metadataFactory.getCollectionDAO();
      if (null == theCollectionDAO) {
         this.logger.log (Level.SEVERE, "CollectionDAO is null");
         return;
      } 

      NetworkDAOFactory networkFactory = (NetworkDAOFactory) extraArray[1];
      if (null == networkFactory) {
         this.logger.log (Level.SEVERE, "NetworkDAOFactory is null");
         return;
      } 

      Properties theProps = (Properties) extraArray[2];
      if (null == theProps) {
         this.logger.log (Level.SEVERE, "Properties object is null");
         return;
      }

      // We need a directory for the output and input files.
      String tmpDir = theProps.getProperty("org.renci.databridge.misc.tmpDir", "/tmp");
      File tmpFileDir = new File(tmpDir);

      SNAInstanceDAO theSNAInstanceDAO = metadataFactory.getSNAInstanceDAO();
      if (null == theSNAInstanceDAO) {
         this.logger.log (Level.SEVERE, "SNAInstanceDAO is null");
         return;
      }

      NetworkNodeDAO theNetworkNodeDAO = networkFactory.getNetworkNodeDAO();
      if (null == theSNAInstanceDAO) {
         this.logger.log (Level.SEVERE, "SNAInstanceDAO is null");
         return;
      }

      // Let's add the SNAInstance.
      SNAInstanceTransferObject theSNAInstance = new SNAInstanceTransferObject();
      theSNAInstance.setNameSpace(nameSpace);

      // Here we we use the executable name instead of the class name.
      theSNAInstance.setClassName(executable);
      theSNAInstance.setMethod("main");
      theSNAInstance.setSimilarityInstanceId(similarityId);
      theSNAInstance.setParams(params);

      // let's find the highest version for this combination of nameSpace, className and method (if any)
      HashMap<String, String> versionMap = new HashMap<String, String>();
      versionMap.put("nameSpace", nameSpace);
      versionMap.put("className", executable);
      versionMap.put("method", "main");
      versionMap.put("similarityInstanceId", similarityId);

      HashMap<String, String> sortMap = new HashMap<String, String>();
      sortMap.put("version", SNAInstanceDAO.SORT_DESCENDING);
      Integer limit = new Integer(1);

      // This is for the case of no previous instance
      theSNAInstance.setVersion(1);
      Iterator<SNAInstanceTransferObject> versionIterator =
          theSNAInstanceDAO.getSNAInstances(versionMap, sortMap, limit);
      if (versionIterator.hasNext()) {
         // Found a previous instance
         SNAInstanceTransferObject prevInstance = versionIterator.next();
         theSNAInstance.setVersion(prevInstance.getVersion() + 1);
      }

      try {
         boolean result = theSNAInstanceDAO.insertSNAInstance(theSNAInstance);
         this.logger.log(Level.INFO, "Inserted Instance: " + theSNAInstance.getDataStoreId());
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't insert SNA instance");
         return;
      }

      NetworkDyadDAO theNetworkDyadDAO = networkFactory.getNetworkDyadDAO();

      // Get the connected dyads only.
      Iterator<NetworkDyadTransferObject> theDyads = 
           theNetworkDyadDAO.getNetworkDyads(nameSpace, similarityId, false);

      // Need the count of connected node for the input csv file.
      long connectedNodeCount = theNetworkDyadDAO.countConnectedNodes(nameSpace, similarityId);

      HashMap<String, ArrayList<String>> clusterList = new HashMap<String, ArrayList<String>>();
      try {
          // Invoke the method. To do this we need to create the tmp file names for the input, and
          // output files, write the network info to the new input file, call the method, read the output 
          // file, add the info to the database and remove the input and output files.

          // Step 1: The input and output files
          File inputTmpFile = File.createTempFile("DataBridge-input", ".csv", tmpFileDir);
          String inputFileString = inputTmpFile.getAbsolutePath();
          File outputTmpFile = File.createTempFile("DataBridge-output", ".csv", tmpFileDir);
          String outputFileString = outputTmpFile.getAbsolutePath();
          File log = File.createTempFile("DataBridge-log", ".log", tmpFileDir);

          // we'll also want a FileWriter for the inputFile and a reader for the output file
          FileWriter inputFileWriter = new FileWriter(inputTmpFile);
          BufferedReader outputFileReader = new BufferedReader(new FileReader(outputTmpFile));

          // Step 2: write the network info into the tmp file
          // Start with the number of connected nodes (the size of the matrix)
          this.logger.log(Level.INFO, "number of connected nodes: " + connectedNodeCount);
          inputFileWriter.append(Long.toString(connectedNodeCount));
          inputFileWriter.append(System.getProperty("line.separator"));
          inputFileWriter.flush();
          while (theDyads.hasNext()) {
             NetworkDyadTransferObject thisDyad = theDyads.next();
             String id1 = thisDyad.getNode1DataStoreId();
             String id2 = thisDyad.getNode2DataStoreId();
             if (null == id2) {
                // a singleton, no reason to emit this
                continue;
             } 
             // A dyad with 2 nodes: let's add it to csv file
             //inputFileWriter.append(Integer.toString(thisDyad.getI()));
             //inputFileWriter.append(Integer.toString(thisDyad.getJ()));
             inputFileWriter.append(id1);
             inputFileWriter.append(",");
             inputFileWriter.append(id2);
             inputFileWriter.append(",");
             inputFileWriter.append(Double.toString(thisDyad.getSimilarity()));
             inputFileWriter.append(System.getProperty("line.separator"));
             inputFileWriter.flush();
          }
          inputFileWriter.close();

          // Step 3: call the executable
          ProcessBuilder theBuilder = new ProcessBuilder(executable, inputFileString, outputFileString, params);
          this.logger.log(Level.INFO, "ProcessBuilder args: " + executable + " " + inputFileString + " " + outputFileString + " " + params);
          theBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
          theBuilder.redirectError(ProcessBuilder.Redirect.appendTo(log));
          Process theProcess = theBuilder.start();
          int returnCode = theProcess.waitFor();
          if (returnCode != 0) {
             this.logger.log (Level.SEVERE, "Executable " + executable + "failed: see " + log.getAbsolutePath());
             inputTmpFile.delete(); 
             outputTmpFile.delete(); 
             return;
          }

          // Step 4: read the output file. Each line is index,cluster
          String line;
          while ((line = outputFileReader.readLine()) != null) {
             System.out.println("line: " + line);
             String[] lineParts = line.split(",");
             String thisNode = lineParts[0];
             String thisCluster = lineParts[1];
             // Look for an array list of nodes for this cluster.  If it's not found, than this is the
             // first node in the cluster, so we add the list in. Otherwise we add this node to the 
             // existing list.
             ArrayList<String> indicesForCluster = (ArrayList<String>)clusterList.get(thisCluster);
             if (null == indicesForCluster) {
                ArrayList<String> newList = new ArrayList<String>();
                newList.add(thisNode);
                clusterList.put(thisCluster, newList);
             } else {
                indicesForCluster.add(thisNode);
             }
          }
 
          // Delete the tmp and log files.
          inputTmpFile.delete(); 
          outputTmpFile.delete(); 
          log.delete();

          String nReturnedClusters = Integer.toString(clusterList.size());
          HashMap<String, String> updateMap = new HashMap<String, String>();
          updateMap.put("nResultingClusters", nReturnedClusters);
          boolean result = theSNAInstanceDAO.updateSNAInstance(theSNAInstance, updateMap);
          String SNAId = theSNAInstance.getDataStoreId();
         
          // For each returned cluster, add the cluster info to the nodes.
          // We are allowing the possibility of a node being in more than one cluster
          for (Map.Entry<String, ArrayList<String>> thisCluster : clusterList.entrySet()){
             String clusterKey = thisCluster.getKey();
             System.out.println("clusterKey: " + clusterKey);
             ArrayList<String> nodesInThisCluster = thisCluster.getValue();
             for (String thisNodeId: nodesInThisCluster) {
                 NetworkNodeTransferObject theNode = theNetworkNodeDAO.getNetworkNode(thisNodeId);
                 String clusterString = 
                    (String) theNetworkNodeDAO.getPropertyFromNetworkNode(theNode, SNAId);
                 if (null == clusterString) {
                    // No problem, just means this node is not already in a cluster
                    theNetworkNodeDAO.addPropertyToNetworkNode(theNode, SNAId, clusterKey);
                 } else {
                    // This node was already assigned to a cluster, so we add this id
                    String multiClusterString = clusterString + "," + clusterKey;
                    theNetworkNodeDAO.deletePropertyFromNetworkNode(theNode, SNAId);
                    theNetworkNodeDAO.addPropertyToNetworkNode(theNode, SNAId, multiClusterString);
                 }
             }
          }

          // We also want to add the singleton nodes.  By convention, they are added with group id -1
          ArrayList<NetworkDyadTransferObject> theSingletonDyads = 
              theNetworkDyadDAO.getNetworkSingletonArray(nameSpace, similarityId);
          for (NetworkDyadTransferObject theSingletonDyad : theSingletonDyads) {
             String nodeId = theSingletonDyad.getNode1DataStoreId();
             NetworkNodeTransferObject theSingletonNode = theNetworkNodeDAO.getNetworkNode(nodeId);
             theNetworkNodeDAO.addPropertyToNetworkNode(theSingletonNode, SNAId, "-1");
          }
          
      } catch (Exception e) {
          this.logger.log (Level.SEVERE, 
                           "Exception in processRunSnaAlgorithmFileIOMessage: "  + e.getMessage(), e);
          return;
      }

      // Assuming we get this far, we want to send out the next message
      AMQPComms ac = null;
      try {
         ac = new AMQPComms (theProps);
         String headers = AddedSNAToNetworkDB.getSendHeaders (
                             nameSpace, theSNAInstance.getDataStoreId());
         this.logger.log (Level.FINER, "Send headers: " + headers);
         ac.publishMessage (new AMQPMessage (), headers, true);
         this.logger.log (Level.FINE, "Sent AddedSNAToNetworkDB message.");
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught Exception sending action message: " + e.getMessage(), e);
      } finally {
         if (null != ac) {
             ac.shutdownConnection ();
         }
      }
  }
 
 
  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

// todo

  }
} 
