package org.renci.databridge.engines.network;
import org.renci.databridge.util.*;
import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.persistence.network.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.LinkedList;
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

public class NetworkEngineRPCHandler implements AMQPMessageHandler {

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

  // Default number of datasets to search for
  public static final int DEFAULT_DATASET_COUNT = 10;

  // Default version
  public static final int DEFAULT_VERSION = -1;
  
  /**
   * Private class used to represent the CollectionTransferObject plus the similarity to the
   * user provided Collection. Used in a linked list to maintain "most" similar collections.
   */
  class TransferSimilarity {
     public double similarity;
     public CollectionTransferObject theCollection;

    /**
      * Constructor that includes all of the fields
      * @param 
      * @param 
      */
     public TransferSimilarity(double similarity, CollectionTransferObject theCollection) {
        this.similarity = similarity;
        this.theCollection = theCollection;
     }

     public String toString() {
         return (this.similarity + " " + this.theCollection);
     }
  }

  
  /**
   * Private class used to represent a collection resulting from our similarity search. 
   * A list of these objects is what's passed to the GSON builder.
   */
  class SimilarCollection {
     public String dataStoreId;
     public String URL;
     public String title;
     public double similarity;

    /**
      * Constructor that includes all of the fields
      * @param 
      * @param 
      */
     public SimilarCollection(String dataStoreId, String URL, String title, double similarity) {
        this.dataStoreId = dataStoreId;
        this.URL = URL;
        this.title = title;
        this.similarity = similarity;
     }

     public String toString() {
         return (this.dataStoreId + " " + this.URL + " " + this.title + " " + this.similarity);
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
      if (messageName.compareTo(NetworkEngineMessage.FIND_CLOSEST_MATCHES_IN_NETWORK) == 0) {
         processFindClosestMatchesInNetwork(stringHeaders, extra, amqpMessage);
      } else {
         this.logger.log (Level.WARNING, "unimplemented messageName: " + messageName);
      }
  }
    /**
     * Handle the FIND_CLOSEST_MATCHES_IN_NETWORK message finding the relevant
     * @param stringHeaders A map of the headers provided in the message
     * @param extra An object containing the needed DAO objects
     * @param amqpMessage The tag incoming message, needed to populate the outgoing message
     */
  public void processFindClosestMatchesInNetwork( Map<String, String> stringHeaders, Object extra, 
                                                  AMQPMessage inMessage) {
      // We need several pieces of information before we can continue.  This info has to 
      // all be in the headers or we are toast.

      // 1) the class name
      String className = stringHeaders.get(NetworkEngineMessage.CLASS);    
      if (null == className) {
         this.logger.log (Level.SEVERE, "No class name in message");
         return;
      }

      // 2) the name space
      String nameSpace = stringHeaders.get(NetworkEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No name space in message");
         return;
      }

      // 3) the dataset for which we are going to look for the closest matches.
      String matchDataSet = stringHeaders.get(NetworkEngineMessage.MATCH_DATASET);    
      if (null == matchDataSet) {
         this.logger.log (Level.SEVERE, "No similarityId in message");
         return;
      }

      // 4) The params will identify the original algorithm that produced the network.
      String params = stringHeaders.get(NetworkEngineMessage.PARAMS);    
      if (null == params) {
         this.logger.log (Level.SEVERE, "No params in message");
         return;
      }

      // 5) The number of closest results to find.
      String countString = stringHeaders.get(NetworkEngineMessage.COUNT);
      int count;
      if (null == countString) {
         // Set a reasonable default
         count = DEFAULT_DATASET_COUNT; 
      } else {
         count = Integer.parseInt(countString);
      }

      // 6) The version of the similarity analyses to use. Currently unused, we just use the latest
      String stringVersion = stringHeaders.get(NetworkEngineMessage.VERSION);
      int version;
      if (null == stringVersion) {
         // Set a reasonable default
         version = DEFAULT_VERSION;
      } else {
         version = Integer.parseInt(stringVersion);
      }

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

      SimilarityInstanceDAO theSimilarityInstanceDAO = metadataFactory.getSimilarityInstanceDAO();
      if (null == theSimilarityInstanceDAO) {
         this.logger.log (Level.SEVERE, "SimilarityInstanceDAO is null");
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

      NetworkRelationshipDAO theRelationshipDAO = networkFactory.getNetworkRelationshipDAO();
      if (null == theRelationshipDAO) {
         this.logger.log (Level.SEVERE, "theRelationshipDAO is null");
         return;
      }

      NetworkNodeDAO theNetworkNodeDAO = networkFactory.getNetworkNodeDAO();
      if (null == theNetworkNodeDAO) {
         this.logger.log (Level.SEVERE, "SNAInstanceDAO is null");
         return;
      }

      // The first thing we need to do is to identify which network the user wants to query.
      HashMap<String, String> networkMap = new HashMap<String, String>();
      networkMap.put("nameSpace", nameSpace);
      networkMap.put("className", className);
      //networkMap.put("URL", matchDataSet);
      networkMap.put("params", params);

      HashMap<String, String> sortMap = new HashMap<String, String>();
      sortMap.put("version", SimilarityInstanceDAO.SORT_DESCENDING);
      Integer limit = new Integer(1);

      Iterator<SimilarityInstanceTransferObject> versionIterator;
      try {
         // Note that for now we are ignoring the version, if any, from the user
         // and just using the latest version
         versionIterator = theSimilarityInstanceDAO.getSimilarityInstances(networkMap, sortMap, limit);
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Exception trying to retrieve the required network");
         return;
      }

      SimilarityInstanceTransferObject theSimInstance;
      if (versionIterator.hasNext()) {
         theSimInstance = versionIterator.next();
      } else {
         this.logger.log (Level.SEVERE, "No network found");
         // TODO: This needs to somehow communicate back to the user.
         return;
      }

      // We'll need this later...
      String theJsonSimilars = null;

      // Now that we have the similarity instance used to create the network, we want to grab the 
      // node and all it's relationships from the Network database.  The similarityId from the network 
      // is the key we need to retrieve the correct network.
      // First we need to retrieve the node that represents the dataset the user specified. Remember
      // that we are storing the id the metadata system (currenly mongo) has generated for this dataset
      // in the metaDataNodeKey property of the network database node. 
      try {
         // First we need to retrieve the id from the metadata store.
         String dataStoreId;
         CollectionTransferObject getObj;
         HashMap<String, String> searchMap = new HashMap<String, String>();
         searchMap.put("nameSpace", nameSpace);
         searchMap.put("URL", matchDataSet);
         // We get back an iterator but there can't be more than one. So we'll grab the first if it exists.
         Iterator<CollectionTransferObject> collectionIterator = 
            theCollectionDAO.getCollections(searchMap);
         if (collectionIterator.hasNext()) {
            getObj = collectionIterator.next();
            dataStoreId = getObj.getDataStoreId();
         } else {
            this.logger.log (Level.SEVERE, "Specified URL not found in nameSpace");
            // TODO: This needs to somehow communicate back to the user.
            return;
         }

         // Now we can grab the node we need. Again, we get an iterator though there can only be one.
         NetworkNodeTransferObject returnedNode;
         Iterator<NetworkNodeTransferObject> theNodes =
               theNetworkNodeDAO.getNetworkNodes(nameSpace, NetworkNodeDAO.METADATA_NODE_KEY, dataStoreId);
         if (theNodes.hasNext()) {
             returnedNode = theNodes.next();
         } else {
            this.logger.log (Level.SEVERE, "Can't find the required node.");
            // TODO: This needs to somehow communicate back to the user.
            return;
         }

         // Now we get all of the relationships for this node. This is actually the easy part.
         // theDataStoreId we are passing is the similarityInstance and is used as the "key"
         // for the values we want in the network database
         Iterator<NetworkRelationshipTransferObject> theRelationships =
               theRelationshipDAO.getNetworkRelationships(returnedNode, theSimInstance.getDataStoreId());

         // Remember we are actually looking for the "count" closest relationships. Let's store them
         int nInList = 0;
        
  //       TreeMap<Double, CollectionTransferObject> relsToReturn = 
  //          new TreeMap<Double, CollectionTransferObject>();

         LinkedList<TransferSimilarity> relsToReturn = new LinkedList<TransferSimilarity>();
         HashMap<String,Object> relMap = new HashMap<String,Object>();

         while (theRelationships.hasNext()) {
            NetworkRelationshipTransferObject thisRel = theRelationships.next();
            relMap = thisRel.getAttributes();
            double thisSimValue = 
               (double) relMap.get(NetworkRelationshipDAO.METADATA_SIMILARITY_PROPERTY_NAME);
            if (relsToReturn.size() < count) {
               // we don't have as many as the user wants, so add this one in
               // We have to store the collection transfer object, but we don't know which one
               // we want.  Luckily we have the dataStoreId of the one the user sent.
               if (returnedNode.getDataStoreId().equals(thisRel.getNodeId1())) {
                  // the collection we want is the other one
                  // We get that from the node.
                  NetworkNodeTransferObject theOtherNode = 
                     theNetworkNodeDAO.getNetworkNode(thisRel.getNodeId2());
                  getObj = theCollectionDAO.getCollectionById(theOtherNode.getNodeId());
               } else {
                  NetworkNodeTransferObject theOtherNode = 
                     theNetworkNodeDAO.getNetworkNode(thisRel.getNodeId1());
                  getObj = theCollectionDAO.getCollectionById(theOtherNode.getNodeId());
               }
               // This loop runs the linked list and puts this one in the correct place
               TransferSimilarity tsToAdd = new TransferSimilarity(thisSimValue, getObj);
               int i = 0;
               for (i = 0; i < relsToReturn.size(); i++) {
                  TransferSimilarity thisTs = relsToReturn.get(i);
                  if (thisTs.similarity < tsToAdd.similarity) {
                      break;
                  }
               }
               // Now i has the index of the last value greater than the current, or 0 if it's
               // the first CTO being added.
               relsToReturn.add(i, tsToAdd);
            } else {
               int i = 0;
               // We have all the user asked for, so we just replace the least one or do nothing
               // and we know that the first "count" of these are sorted high to low.
               TransferSimilarity leastSimilar = relsToReturn.getLast();
               Double firstKey = leastSimilar.similarity;
               if (firstKey < thisSimValue) {
                  // The least similar in the list is less similar than the one we just found. So we 
                  // add it to the correct place in the list and delete the least similar in the list.
                  // We know we need to delete the last lement of the list so let's delete it now.
                  relsToReturn.removeLast();

                  if (returnedNode.getDataStoreId().equals(thisRel.getNodeId1())) {
                     // the collection we want is the other one
                     NetworkNodeTransferObject theOtherNode = 
                        theNetworkNodeDAO.getNetworkNode(thisRel.getNodeId2());
                     getObj = theCollectionDAO.getCollectionById(theOtherNode.getNodeId());
                  } else {
                     NetworkNodeTransferObject theOtherNode = 
                        theNetworkNodeDAO.getNetworkNode(thisRel.getNodeId1());
                     getObj = theCollectionDAO.getCollectionById(theOtherNode.getNodeId());
                  }

                  // find the place in the list at which to insert the new object.
                  for (i = 0; i < relsToReturn.size(); i++) {
                     TransferSimilarity thisTs = relsToReturn.get(i);
                     if (thisTs.similarity < thisSimValue) {
                         break;
                     }
                  }
                  // i is now the index at which to insert the new transfer similarity object.
                  // Add the new object at the correct place in the list as determined above.
                  TransferSimilarity tsToAdd = new TransferSimilarity(thisSimValue, getObj);
                  relsToReturn.add(i, tsToAdd);
               }
            }
         }

         // Let's convert this info into an array of jsons, so we can pass these into the next message
         Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

         // Let's make an ArrayList to store all of the data ready to be JSON formatted.
         ArrayList<SimilarCollection> theSimilars = new ArrayList<SimilarCollection>();

         for (int i = 0; i < relsToReturn.size(); i++) {
            TransferSimilarity thisTs = relsToReturn.get(i);
            Double simValue = thisTs.similarity;
            CollectionTransferObject theCollection = thisTs.theCollection;
            SimilarCollection thisSimilar = new SimilarCollection(theCollection.getDataStoreId(),
                                                                  theCollection.getURL(),
                                                                  theCollection.getTitle(),
                                                                  simValue);
            theSimilars.add(thisSimilar);
         }

      theJsonSimilars = gson.toJson(theSimilars);
      System.out.println("Json: " + theJsonSimilars);
      
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Caught processing request: " + e.getMessage(), e);
      }
      // Assuming we get this far, we want to send out the next message
      AMQPRpcComms ac = null;
      try {
         ac = new AMQPRpcComms (theProps);
         // At this point we can also ack the original message
         //ac.ackMessage(inMessage);
         AMQPMessage thisMessage = new AMQPMessage();
         thisMessage.setTag(inMessage.getTag());
         this.logger.log (Level.INFO, "setting tag: " + inMessage.getTag());
         thisMessage.setReplyQueue(inMessage.getReplyQueue());
         this.logger.log (Level.INFO, "setting replyQueue: " + inMessage.getReplyQueue());
         thisMessage.setContent(theJsonSimilars);
         thisMessage.setBytes(theJsonSimilars.getBytes());
         String headers = ReturnClosestMatchesInNetwork.getSendHeaders ("DataBridge_OK");
         this.logger.log (Level.INFO, "Send headers: " + headers);
         ac.publishMessage ( thisMessage, headers, true);
         this.logger.log (Level.INFO, "Sent ReturnClosestMatchesInNetwork message.");
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
