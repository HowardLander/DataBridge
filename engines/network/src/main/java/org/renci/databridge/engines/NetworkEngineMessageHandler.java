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
import java.util.*;
import java.lang.reflect.*;
import org.la4j.*;
import org.la4j.matrix.functor.*;


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
  
  public void handle (AMQPMessage amqpMessage, Object extra) throws Exception {
      // Get the individual components of the the message and store
      // them in the fields
      routingKey = amqpMessage.getRoutingKey();
      properties = amqpMessage.getProperties();
      stringHeaders = amqpMessage.getStringHeaders();
      bytes = amqpMessage.getBytes();

      // get the message name
      String messageName = stringHeaders.get(NetworkEngineMessage.NAME);
      System.out.println("messageName: " + messageName);

      // Call the function appropriate for the message
      if (messageName.compareTo(NetworkEngineMessage.INSERT_SIMILARITYMATRIX_JAVA_URI_NETWORKDB) == 0) {
         processInsertSimilarityMatrixJavaMessage(stringHeaders, extra);
      } else if (messageName.compareTo(NetworkEngineMessage.RUN_SNA_ALGORITHM_JAVA_NETWORKDB) == 0) {
         processRunSnaAlgorithmJavaMessage(stringHeaders, extra);
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
              System.out.println("result in apply is: " + result);
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
      // network factories.
      Object factoryArray[] = (Object[]) extra;

      MetadataDAOFactory theFactory = (MetadataDAOFactory) factoryArray[0];
      if (null == theFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 
      CollectionDAO theCollectionDAO = theFactory.getCollectionDAO();
      if (null == theCollectionDAO) {
         this.logger.log (Level.SEVERE, "CollectionDAO is null");
         return;
      } 

      NetworkDAOFactory theNetworkFactory = (NetworkDAOFactory) factoryArray[1];
      if (null == theNetworkFactory) {
         this.logger.log (Level.SEVERE, "NetworkDAOFactory is null");
         return;
      } 

      // We'll need a DAO
      NetworkNodeDAO theNetworkNodeDAO = theNetworkFactory.getNetworkNodeDAO();
      NetworkRelationshipDAO theNetworkRelationshipDAO = theNetworkFactory.getNetworkRelationshipDAO();

      // Let's start by reading in the URI/file. If the first character is a slash
      // then we'll assume it's a file, otherwise we'll assume it's a URI. Note, this may not
      // work on windows.
      SimilarityFile theFile = new SimilarityFile();
      try {
          theFile.readFromURL(inputURI);
      } catch (Exception e) {
          e.printStackTrace();
      }
      String nameSpace = theFile.getNameSpace();

      // Here is a classic space vs time tradeoff: we are going to keep all of the Node
      // structures in memory because we will need them later to insert the relationships. The 
      // alternative would be lots of search and retrieve.
      ArrayList<NetworkNodeTransferObject> nodeList = new ArrayList<NetworkNodeTransferObject>();

      //System.out.println("\tnameSpace: " + theFile.getNameSpace());
      // Now we'll add all of the nodes.
      for (String theId: theFile.getCollectionIds()) {
          NetworkNodeTransferObject theNode = new NetworkNodeTransferObject();
          theNode.setNameSpace(nameSpace);

          // Save for later
          theNode.setNodeId(theId);
          nodeList.add(theNode);
          int result = theNetworkNodeDAO.insertNetworkNode(theNode);
          if (result < 0) {
             this.logger.log (Level.SEVERE, "failure on insertNetworkNode");
             return;
          }
      }

      // Add the similarity matrix as relationships between nodes.
      org.la4j.matrix.sparse.CRSMatrix theMatrix = theFile.getSimilarityMatrix();

      // Create an instance of the inserter class, which actually does all the work.
      RelationshipInserter theInserter = 
          new RelationshipInserter(nodeList, theNetworkRelationshipDAO, theFile.getSimilarityInstanceId());
      theMatrix.eachNonZero(theInserter);
 
  }

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
      Class theClass = null;
      ClassLoader classLoader = NetworkEngineMessageHandler.class.getClassLoader();
      try {
         theClass = classLoader.loadClass(className);
      } catch (ClassNotFoundException e) {
         this.logger.log (Level.SEVERE, "Can't instantiate class " + className);
         e.printStackTrace();
         return;
      }

      // We'll need an object of this type as well.
      Constructor<?> cons = null;
      Object theObject = null;
      try {
         cons = (Constructor<?>) theClass.getConstructor(null);
         theObject = cons.newInstance(null);
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't create instance");
         return;
      }

      // 2) the method name
      String methodName = stringHeaders.get(NetworkEngineMessage.METHOD);    
      if (null == methodName) {
         this.logger.log (Level.SEVERE, "No method name in message");
         return;
      }

      java.lang.reflect.Method theMethod = null;
      // Try for the method
      try {
         Class[] paramList = new Class[2];
         paramList[0] = CollectionTransferObject.class;
         paramList[1] = CollectionTransferObject.class;
         theMethod = theClass.getMethod(methodName, paramList);
      } catch (NoSuchMethodException e) {
         this.logger.log (Level.SEVERE, "Can't instantiate method " + methodName);
         return;
      }

      // 3) the name space
      String nameSpace = stringHeaders.get(NetworkEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No name space in message");
         return;
      }

      // 4) the similarityId
      String similarityId = stringHeaders.get(NetworkEngineMessage.SIMILARITY_ID);    
      if (null == similarityId) {
         this.logger.log (Level.SEVERE, "No similarityId in message");
         return;
      }

      // 4) the version
      String version = stringHeaders.get(NetworkEngineMessage.VERSION);    
      if (null == version) {
         this.logger.log (Level.SEVERE, "No version in message");
         return;
      }

      // In this case the extra parameter is an array of 2 objects, which are the metadata and
      // network factories.
      Object[] factoryArray = (Object[]) extra;

      MetadataDAOFactory theFactory = (MetadataDAOFactory) factoryArray[0];
      if (null == theFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 
      CollectionDAO theCollectionDAO = theFactory.getCollectionDAO();
      if (null == theCollectionDAO) {
         this.logger.log (Level.SEVERE, "CollectionDAO is null");
         return;
      } 

      NetworkDAOFactory theNetworkFactory = (NetworkDAOFactory) factoryArray[1];
      if (null == theNetworkFactory) {
         this.logger.log (Level.SEVERE, "NetworkDAOFactory is null");
         return;
      } 

      SimilarityInstanceDAO theSimilarityInstanceDAO = theFactory.getSimilarityInstanceDAO();
      if (null == theSimilarityInstanceDAO) {
         this.logger.log (Level.SEVERE, "SimilarityInstanceDAO is null");
         return;
      }

      // Let's add the SimilarityInstance.
      SimilarityInstanceTransferObject theSimilarityInstance = new SimilarityInstanceTransferObject();
  }
 
  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

// todo

  }
} 
