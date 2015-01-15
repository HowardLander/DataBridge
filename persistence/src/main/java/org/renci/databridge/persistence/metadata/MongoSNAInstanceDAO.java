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
import com.mongodb.WriteResult;
import org.bson.types.*;

/** This is the version of the Data Analysis Object for the SNA instance objects
 *  stored in MongoDB.  It's got the normal CRUD pattern stuff, plus whatever
 *  other methods become needed. 
 */
public class MongoSNAInstanceDAO implements SNAInstanceDAO {

    // This is a little confusing because both the data model and mongo
    // use the term "collection". This is the name of the mongo collection 
    // into which we are storing the DataBridge collection info.
    private static final String MongoName = new String("DB_SNAInstance");
    private static final String MongoExtraName = new String("extra");
    private static final String MongoIdFieldName = new String("_id");



    /** 
     *  An iterator class for the SimilarityInstanceTransferObject.  An instance is returned to the
     *  the user by the getCollection call.  This implements the Iterator interface.
     */
    private class MongoSNAInstanceDAOIterator implements Iterator<SNAInstanceTransferObject> {
       private DBCursor cursor;

       /** 
        * Returns whether or not there is a next item in this cursor.
        */
       @Override
       public boolean hasNext() {
           // Wrap the mongo cursor opened in the get call.
           return cursor.hasNext();
       }

       /** 
        * Returns the next CollectionTransferObject from the cursor or null.
        */
       @Override
       public SNAInstanceTransferObject next() {
           SNAInstanceTransferObject theSNAInstance = null; 
           try {
               if (cursor.hasNext()) {
                   // Translate the date from the MongoDB representation to the 
                   // representation presented to the users in the Transfer object.
                   theSNAInstance = new SNAInstanceTransferObject();
                   DBObject theEntry = cursor.next();
                   theSNAInstance.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
                   theSNAInstance.setNameSpace((String)theEntry.get("nameSpace"));
                   theSNAInstance.setClassName((String)theEntry.get("className"));
                   theSNAInstance.setMethod((String)theEntry.get("method"));
                   theSNAInstance.setVersion((int)theEntry.get("version"));
                   theSNAInstance.setNResultingClusters((int)theEntry.get("nResultingClusters"));
                   theSNAInstance.setSimilarityInstanceId((String)theEntry.get("similarityInstanceId"));
               }
           } catch (MongoException e) {
               // should send this back using the message logs eventually
               e.printStackTrace();
           }
           return theSNAInstance;
       }

       /** 
        * Remove the object. Currently and probably always unsupported.
        */
       @Override
       public void remove() throws UnsupportedOperationException{
           throw new UnsupportedOperationException();
       }
    }

    /** 
     * insert the specified SNAInstance object into mongo.
     *
     * @param theSNAInstance SNAInstanceTransfer object containing info to be inserted.
     */
    public boolean insertSNAInstance(SNAInstanceTransferObject theSNAInstance) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theSNAInstance.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("nameSpace", theSNAInstance.getNameSpace());
          thisDoc.put("className", theSNAInstance.getClassName());
          thisDoc.put("method", theSNAInstance.getMethod());
          thisDoc.put("version", theSNAInstance.getVersion());
          thisDoc.put("nResultingClusters", theSNAInstance.getNResultingClusters());
          thisDoc.put("similarityInstanceId", theSNAInstance.getSimilarityInstanceId());
          thisDoc.put("version", theSNAInstance.getVersion());
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


    /** 
     * retrieve an iterator for all records that match the given search key.
     *
     * @param searchMap A HashMap with search keys.
     */
    public Iterator<SNAInstanceTransferObject> getSNAInstances(HashMap<String, String> searchMap) {
        MongoSNAInstanceDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                //System.out.println("Adding key: "  + key + " with value " + searchMap.get(key));
                thisDoc.put(key, searchMap.get(key));
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoSNAInstanceDAOIterator();
            theIterator.cursor = cursor;
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }


    /** 
     * delete the specified SNAInstance object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param theSNAInstance The instance to delete
     */
    public int deleteSNAInstance(SNAInstanceTransferObject theSNAInstance) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(theSNAInstance.getDataStoreId());
            thisDoc.put(MongoIdFieldName, theId);
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            theResult  = theTable.remove(thisDoc);
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace();
        }
        return theResult.getN();
    }

    /** 
     * delete the specified SNAInstance object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param searchMap A HashMap with search keys.
     */
    public int deleteSNAInstance(HashMap<String, String> searchMap) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                // Special case for the Id field 
                if (key.compareTo(MongoIdFieldName) == 0) {
                    ObjectId theId = new ObjectId(searchMap.get(key));
                    thisDoc.put(key, theId);
                } else {
                    thisDoc.put(key, searchMap.get(key));
                }
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            theResult  = theTable.remove(thisDoc);
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace();
        }
        return theResult.getN();

    }
}

