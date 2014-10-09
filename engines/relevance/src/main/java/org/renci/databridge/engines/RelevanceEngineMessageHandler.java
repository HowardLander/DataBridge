package org.renci.databridge.engines.relevance;
import org.renci.databridge.util.*;
import org.renci.databridge.persistence.metadata.*;
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


/**
 * This class is executed in a thread of the Relevance Engine. The Relevance Engine
 * calls the constructor for this class with the AMQP message as a parameter.  It's
 * up to this class to decode the message according to the headers and implement the
 * required behaviors.
 *
 * @author Howard Lander -RENCI (www.renci.org)
 */

public class RelevanceEngineMessageHandler implements AMQPMessageHandler {

   private Logger logger = Logger.getLogger ("org.renci.databridge.engine.relevance");

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

      // We need three pieces of information before we can continue.  This info has to all be
      // in the headers or we are toast.
      // 1) the class name
      String className = stringHeaders.get(RelevanceEngineMessage.CLASS);    
      if (null == className) {
         this.logger.log (Level.SEVERE, "No class name in message");
         return;
      }

      // Let's try to load the class.
      Class theClass = null;
      ClassLoader classLoader = RelevanceEngineMessageHandler.class.getClassLoader();
      try {
         theClass = classLoader.loadClass(className);
      } catch (ClassNotFoundException e) {
         this.logger.log (Level.SEVERE, "Can't instantiate class " + className);
         e.printStackTrace();
         return;
      }

      // We'll need an object of this type as well.
      Constructor<?> cons = theClass.getConstructor(null);
      Object theObject = cons.newInstance(null);

      // 2) the method name
      String methodName = stringHeaders.get(RelevanceEngineMessage.METHOD);    
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
      String nameSpace = stringHeaders.get(RelevanceEngineMessage.NAME_SPACE);    
      if (null == nameSpace) {
         this.logger.log (Level.SEVERE, "No name space in message");
         return;
      }

      // The "extra" parameter in this case must be of type MetadataDAOFactory
      MetadataDAOFactory theFactory = (MetadataDAOFactory) extra;
      if (null == theFactory) {
         this.logger.log (Level.SEVERE, "MetadataDAOFactory is null");
         return;
      } 
      CollectionDAO theCollectionDAO = theFactory.getCollectionDAO();
      if (null == theCollectionDAO) {
         this.logger.log (Level.SEVERE, "CollectionDAO is null");
         return;
      } 

      // Search for all of the collections in the nameSpace
      HashMap<String, String> searchMap = new HashMap<String, String>();
      searchMap.put("nameSpace", nameSpace);

      // For each pair of collection objects, we are going to call the user provided function.
      Iterator<CollectionTransferObject> iterator1 = theCollectionDAO.getCollections(searchMap);
      Iterator<CollectionTransferObject> iterator2 = null;
      
      // Note that the following code is known to be ugly, but it's not easy to do better since
      // you can't really copy java iterators.
      int counter = 1;
      while (iterator1.hasNext()) { 
         CollectionTransferObject cto1 = iterator1.next();
         CollectionTransferObject cto2 = null;

         // This is the weird part. Since you can't really copy iterators in java
         // we re-declare the inner iterator for each iteration of the outer loop, than
         // spin it forward so it is at the position of the outer iterator.
         iterator2 = theCollectionDAO.getCollections(searchMap);
         for (int i = 0; i < counter; i++) {
             iterator2.next();
         }

         // Now spin through the rest of the iterator 2 list.
         while (iterator2.hasNext()) {
               cto2 = iterator2.next();

               // Now we have our 2 CollectionTransferObjects, so we want to call the method.
               //double similarity =  (double) theMethod.invoke(theClass, cto1, cto2);
               double similarity =  (double) theMethod.invoke(theObject, cto1, cto2);
               System.out.println("Similarity is: " + similarity);
         }
         counter ++;
      }
  }
 
  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

// todo

  }
} 
