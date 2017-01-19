package org.renci.databridge.engines.relevance;

import java.io.*;
import java.util.*;

import com.rabbitmq.client.*;
import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.util.*;
import org.renci.databridge.message.*;

public class LoadSomeData {


  public static void main(String [] args) {

     System.out.println("");
     System.out.println("");
     System.out.println("Inserting test Collection data");
     boolean result;

     // This won't work (in fact we maybe could get rid of this)
     MetadataDAOFactory theMongoFactory = 
        MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27017, "a", "b");
     CollectionTransferObject theCollection = new CollectionTransferObject();
     theCollection.setURL("http://www.renci.org");
     theCollection.setTitle("title");
     theCollection.setDescription("here's an example description");
     theCollection.setProducer("producer");
     theCollection.setSubject("physics");
     theCollection.setNameSpace("system_test");
     theCollection.setVersion(0);
     ArrayList<String> keywords = new ArrayList<String>();

     keywords.add("Keyword1");
     keywords.add("Keyword2");
     keywords.add("Keyword3");
     theCollection.setKeywords(keywords);

     HashMap<String, String> extra = new HashMap<String, String>();
     extra.put("author", "Howard Lander");
     extra.put("reason", "Testing the code");
     theCollection.setExtra(extra);

     try {
         CollectionDAO theCollectionDAO = theMongoFactory.getCollectionDAO();
         result = theCollectionDAO.insertCollection(theCollection);

         for (int i = 0; i < 5; i ++) {
             theCollection.setVersion(i + 1);
             result = theCollectionDAO.insertCollection(theCollection);
         }

     }  catch (Exception e) {
         e.printStackTrace();
     }
  }
}
