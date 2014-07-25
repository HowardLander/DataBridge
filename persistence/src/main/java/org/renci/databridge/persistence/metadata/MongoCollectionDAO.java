package org.renci.databridge.persistence.metadata;
import  java.util.*;
import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoCollectionDAO implements CollectionDAO {

    // This is a little confusing because both the data model and mongo
    // use the term "collection". This is the name of the mongo collection 
    // into which we are storing the DataBridge collection info.
    private static final String MongoName = new String("DB_Collection");
    private static final String MongoExtraName = new String("extra");

    public boolean insertCollection(CollectionTransferObject theCollection) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          thisDoc.put("URL", theCollection.getURL());
          thisDoc.put("title", theCollection.getTitle());
          thisDoc.put("description", theCollection.getDescription());
          thisDoc.put("producer", theCollection.getProducer());
          thisDoc.put("subject", theCollection.getSubject());
          thisDoc.put("nameSpace", theCollection.getNameSpace());
          thisDoc.put("version", theCollection.getVersion());
          HashMap <String, String> extra = theCollection.getExtra();

          if (null != extra) {
             // Convert the hash map of extra metadata to a list so that
             // it can be inserted as an embedded array in the Mongo table.
             List<BasicDBObject> extraList = new ArrayList<BasicDBObject>();
             for (String key: extra.keySet()) {
                 extraList.add(new BasicDBObject(key, extra.get(key)));
             }
             thisDoc.put(MongoExtraName, extraList);
          }
          DB theDB = MongoDAOFactory.getTheDB();
          DBCollection theTable = theDB.getCollection(MongoName);
          theTable.insert(thisDoc);
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
            returnCode = false;
        }
        
        return returnCode;
    }

    public CollectionTransferObject getCollection(HashMap<String, String> searchMap) {
        CollectionTransferObject theCollection = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                thisDoc.put(key, searchMap.get(key));
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            if (cursor.hasNext()) {
                theCollection = new CollectionTransferObject();
                DBObject theEntry = cursor.next();
                theCollection.setURL((String)theEntry.get("URL"));
                theCollection.setTitle((String)theEntry.get("title"));
                theCollection.setDescription((String)theEntry.get("description"));
                theCollection.setProducer((String)theEntry.get("producer"));
                theCollection.setSubject((String)theEntry.get("subject"));
                theCollection.setNameSpace((String)theEntry.get("nameSpace"));
                theCollection.setVersion((int)theEntry.get("version"));
                @SuppressWarnings("unchecked")
                ArrayList<BasicDBObject> extraList = (ArrayList<BasicDBObject>) theEntry.get(MongoExtraName);
                HashMap<String, String> extra = new HashMap<String, String>();
                for (BasicDBObject extraObj : extraList) {
                    // Get the key set.  We sort of expect there to be only one but...
                    Set<String> keys = extraObj.keySet();
                    for (String thisKey : keys) {
                        extra.put(thisKey, (String) extraObj.get(thisKey));    
                    }            
                }
                theCollection.setExtra(extra);
            } 
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theCollection;
    }

    public boolean updateCollection( CollectionTransferObject theCollection, 
                                    Object collectionID) {
        return true;

    }

    public boolean deleteCollection(Object collectionID) {
        return true;

    }
}

