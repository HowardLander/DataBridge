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

/** This is the version of the Data Analysis Object for the Lane objects
 *  stored in MongoDB.  It's got the normal CRUD pattern stuff, plus whatever
 *  other methods become needed. 
 */
public class MongoLaneDAO implements LaneDAO {

    private static final String MongoName = new String("DB_Lane");
    private static final String MongoExtraName = new String("extra");
    private static final String MongoIdFieldName = new String("_id");
    private static final String MongoNamespace = new String("nameSpace");



    /** 
     *  An iterator class for the LaneTransferObject.  An instance is returned to the
     *  the user by the getLane call.  This implements the Iterator interface.
     */
    private class MongoLaneDAOIterator implements Iterator<LaneTransferObject> {
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
        * Returns the next LaneTransferObject from the cursor or null.
        */
       @Override
       @SuppressWarnings("unchecked")
       public LaneTransferObject next() {
           LaneTransferObject theLane = null; 
           try {
               if (cursor.hasNext()) {
                   // Translate the date from the MongoDB representation to the 
                   // representation presented to the users in the Transfer object.
                   theLane = new LaneTransferObject();
                   DBObject theEntry = cursor.next();
                   theLane.setNameSpaces((ArrayList<String>)theEntry.get("nameSpaces"));
                   theLane.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
                   theLane.setCreatorId(theEntry.get("creatorId").toString());
                   theLane.setInsertTime(((ObjectId)theEntry.get(MongoIdFieldName)).getTimestamp());
                   theLane.setName((String)theEntry.get("name"));
                   theLane.setDescription((String)theEntry.get("description"));
                   theLane.setIngestImpl((String)theEntry.get("ingestImpl"));
                   theLane.setIngestParams((String)theEntry.get("ingestParams"));
                   theLane.setSignatureParams((String)theEntry.get("signatureParams"));
                   theLane.setSignatureImpl((String)theEntry.get("signatureImpl"));
                   theLane.setSimilarityParams((String)theEntry.get("similarityParams"));
                   theLane.setSimilarityImpl((String)theEntry.get("similarityImpl"));
                   theLane.setSNAParams((String)theEntry.get("SNAParams"));
                   theLane.setSNAImpl((String)theEntry.get("SNAImpl"));
               }
           } catch (MongoException e) {
               // should send this back using the message logs eventually
               e.printStackTrace();
           }
           return theLane;
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
     * insert the specified Lane object into mongo.
     *
     * @param theLane LaneTransfer object containing info to be inserted.
     */
    public boolean insertLane(LaneTransferObject theLane) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theLane.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("creatorId", theLane.getCreatorId());
          thisDoc.put("ingestImpl", theLane.getIngestImpl());
          thisDoc.put("ingestParams", theLane.getIngestParams());
          thisDoc.put("signatureImpl", theLane.getSignatureImpl());
          thisDoc.put("signatureParams", theLane.getSignatureParams());
          thisDoc.put("similarityImpl", theLane.getSimilarityImpl());
          thisDoc.put("similarityParams", theLane.getSimilarityParams());
          thisDoc.put("SNAImpl", theLane.getSNAImpl());
          thisDoc.put("SNAParams", theLane.getSNAParams());
          thisDoc.put("nameSpaces", theLane.getNameSpaces());
          thisDoc.put("name", theLane.getName());
          thisDoc.put("description", theLane.getDescription());

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
    public Iterator<LaneTransferObject> getLanes(HashMap<String, String> searchMap) {
        MongoLaneDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                //System.out.println("Adding key: "  + key + " with value " + searchMap.get(key));
                thisDoc.put(key, searchMap.get(key));
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoLaneDAOIterator();
            theIterator.cursor = cursor;
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }

    /** 
     * retrieve either a lane transfer object for the lane with the specified id
     * or null
     *
     * @param id The id of the lane to return
     */
    @SuppressWarnings("unchecked")
    public LaneTransferObject getLaneById(String id) {
        LaneTransferObject theLane = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(id);
            thisDoc.put(MongoIdFieldName, theId);
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            if (cursor.hasNext()) {
                // Translate the date from the MongoDB representation to the
                // representation presented to the users in the Transfer object.
                theLane = new LaneTransferObject();
                DBObject theEntry = cursor.next();
                theLane.setNameSpaces((ArrayList<String>)theEntry.get("nameSpaces"));
                theLane.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
                theLane.setCreatorId(theEntry.get("creatorId").toString());
                theLane.setInsertTime(((ObjectId)theEntry.get(MongoIdFieldName)).getTimestamp());
                theLane.setName((String)theEntry.get("name"));
                theLane.setDescription((String)theEntry.get("description"));
                theLane.setIngestImpl((String)theEntry.get("ingestImpl"));
                theLane.setIngestParams((String)theEntry.get("ingestParams"));
                theLane.setSignatureParams((String)theEntry.get("signatureParams"));
                theLane.setSignatureImpl((String)theEntry.get("signatureImpl"));
                theLane.setSimilarityParams((String)theEntry.get("similarityParams"));
                theLane.setSimilarityImpl((String)theEntry.get("similarityImpl"));
                theLane.setSNAParams((String)theEntry.get("SNAParams"));
                theLane.setSNAImpl((String)theEntry.get("SNAImpl"));
            }
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theLane;
    }


    public boolean updateLane( LaneTransferObject theLane, 
                                    Object collectionID) {
        return true;

    }

    /** 
     * delete the specified Lane object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param theLane The lane object to be deleted.
     */
    public int deleteLane(LaneTransferObject theLane) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(theLane.getDataStoreId());
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
     * count the number of items in the "Lane" table that match the search keys in the search map.
     * @param searchMap A HashMap with search keys.
     *
     */
    public long countLanes(HashMap<String, String> searchMap) {
        long theResult = 0;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                 thisDoc.put(key, searchMap.get(key));
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            theResult  = theTable.count(thisDoc);
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace();
            theResult = 0;
        }
        return theResult;
    }

    /** 
     * delete the specified Lane object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param searchMap A HashMap with search keys.
     */
    public int deleteLane(HashMap<String, String> searchMap) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                // Special case for the Id field though we would expect the user to use
                // deleteLaneById instead.  Still want it to work here...
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

