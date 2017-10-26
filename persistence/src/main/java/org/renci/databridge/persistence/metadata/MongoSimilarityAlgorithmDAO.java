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

/** This is the version of the Data Analysis Object for the Collection objects
 *  stored in MongoDB.  It's got the normal CRUD pattern stuff, plus whatever
 *  other methods become needed. 
 */
public class MongoSimilarityAlgorithmDAO implements SimilarityAlgorithmDAO {

    // This is a little confusing because both the data model and mongo
    // use the term "collection". This is the name of the mongo collection 
    // into which we are storing the DataBridge collection info.
    private static final String MongoName = new String("DB_Similarity_Algorithms");
    private static final String MongoExtraName = new String("extra");
    private static final String MongoIdFieldName = new String("_id");



    /** 
     *  An iterator class for the SimilarityAlgorithmTransferObject.  An algorithm is returned to the
     *  the user by the getCollection call.  This implements the Iterator interface.
     */
    private class MongoSimilarityAlgorithmDAOIterator implements Iterator<SimilarityAlgorithmTransferObject> {
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
       public SimilarityAlgorithmTransferObject next() {
           SimilarityAlgorithmTransferObject theSimilarityAlgorithm = null; 
           try {
               if (cursor.hasNext()) {
                   // Translate the date from the MongoDB representation to the 
                   // representation presented to the users in the Transfer object.
                   theSimilarityAlgorithm = new SimilarityAlgorithmTransferObject();
                   DBObject theEntry = cursor.next();
                   theSimilarityAlgorithm.setClassName((String)theEntry.get("className"));
                   theSimilarityAlgorithm.setType((String)theEntry.get("type"));
                   theSimilarityAlgorithm.setDescription((String)theEntry.get("description"));
   
                   // Added this because there were some entries with no engine params
                   String engineParams = 
                      (theEntry.get("engineParams") != null) ? (String) theEntry.get("engineParams") : "";
                   theSimilarityAlgorithm.setEngineParams(engineParams);

                   // Non user provided
                   String stringVersion = (theEntry.get("version") != null) ? (String) theEntry.get("version") : "";
                   if (stringVersion.compareTo("")  == 0) {
                      // There was no version in the db, set it to 0
                      theSimilarityAlgorithm.setVersion(0);
                   } else {
                      theSimilarityAlgorithm.setVersion((int)theEntry.get("version"));
                   }
                   theSimilarityAlgorithm.setInsertTime(((ObjectId)theEntry.get(MongoIdFieldName)).getTimestamp());
                   theSimilarityAlgorithm.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
               }
           } catch (MongoException e) {
               // should send this back using the message logs eventually
               e.printStackTrace();
           }
           return theSimilarityAlgorithm;
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
     * insert the specified SimilarityAlgorithm object into mongo.
     *
     * @param theSimilarityAlgorithm SimilarityAlgorithmTransfer object containing info to be inserted.
     */
    public boolean insertSimilarityAlgorithm(SimilarityAlgorithmTransferObject theSimilarityAlgorithm) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theSimilarityAlgorithm.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("className", theSimilarityAlgorithm.getClassName());
          thisDoc.put("type", theSimilarityAlgorithm.getType());
          thisDoc.put("description", theSimilarityAlgorithm.getDescription());
          thisDoc.put("engineParams", theSimilarityAlgorithm.getEngineParams());
          thisDoc.put("version", theSimilarityAlgorithm.getVersion());
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
     * retrieve a SimilarityAlgorithmTransfer object (or null) for the record matching the provided id
     *
     * @param id The id to search for.
     */
    public SimilarityAlgorithmTransferObject getSimilarityAlgorithmById(String id) {
        SimilarityAlgorithmTransferObject theSimilarityAlgorithm = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(id);
            thisDoc.put(MongoIdFieldName, theId);
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            if (cursor.hasNext()) {
               theSimilarityAlgorithm = new SimilarityAlgorithmTransferObject();
               DBObject theEntry = cursor.next();
               theSimilarityAlgorithm.setClassName((String)theEntry.get("className"));
               theSimilarityAlgorithm.setType((String)theEntry.get("type"));
               theSimilarityAlgorithm.setDescription((String)theEntry.get("description"));
   
               // Added this because there were some entries with no params or count
               String engineParams = 
                  (theEntry.get("engineParams") != null) ? (String) theEntry.get("engineParams") : "";
               theSimilarityAlgorithm.setEngineParams(engineParams);

               // non-user provide
               theSimilarityAlgorithm.setVersion((int)theEntry.get("version"));
               theSimilarityAlgorithm.setInsertTime(((ObjectId)theEntry.get(MongoIdFieldName)).getTimestamp());
               theSimilarityAlgorithm.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
            }
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theSimilarityAlgorithm;
    }


    /** 
     * retrieve an iterator for all records that match the given search key.
     *
     * @param searchMap A HashMap with search keys.
     */
    public Iterator<SimilarityAlgorithmTransferObject> getSimilarityAlgorithms(HashMap<String, String> searchMap) {
        MongoSimilarityAlgorithmDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                //System.out.println("Adding key: "  + key + " with value " + searchMap.get(key));
                thisDoc.put(key, searchMap.get(key));
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoSimilarityAlgorithmDAOIterator();
            theIterator.cursor = cursor;
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }

    /** 
     * retrieve an iterator for all records that match the given search key.
     *
     * @param searchMap A HashMap with search keys.
     * @param sortMap   A Hashmap with sort keys that are fields in the record.
     * @param limit     An Integer giving the limit on records returned.
     */
    public Iterator<SimilarityAlgorithmTransferObject> getSimilarityAlgorithms(HashMap<String, String> searchMap,
 HashMap<String, String> sortMap, Integer limit) {
        MongoSimilarityAlgorithmDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                //System.out.println("Adding key: "  + key + " with value " + searchMap.get(key));
                thisDoc.put(key, searchMap.get(key));
            }

            BasicDBObject thisSortObject = new BasicDBObject();
            for (String key : sortMap.keySet()) {
                // For mongo, -1 is descending and 1 is ascending
                String sortValue = sortMap.get(key);
                if (sortValue.compareTo(SimilarityAlgorithmDAO.SORT_ASCENDING) == 0) {
                   thisSortObject.put(key, 1);
                } else if (sortValue.compareTo(SimilarityAlgorithmDAO.SORT_DESCENDING) == 0) {
                   thisSortObject.put(key, -1);
                } else {
                   // The user may have passed a value themselves.
                   thisSortObject.put(key, Integer.parseInt(sortMap.get(key)));
                }
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            cursor = cursor.sort(thisSortObject);
            if (null != limit) {
               cursor = cursor.limit(limit.intValue());
            }
            theIterator = new MongoSimilarityAlgorithmDAOIterator();
            theIterator.cursor = cursor;
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }



    /** 
     * delete the specified SimilarityAlgorithm object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param theSimilarityAlgorithm The algorithm to delete
     */
    public int deleteSimilarityAlgorithm(SimilarityAlgorithmTransferObject theSimilarityAlgorithm) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(theSimilarityAlgorithm.getDataStoreId());
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
     * count the number of items in the "SimilarityAlgorithm" table that match the search keys in the search map.
     * @param searchMap A HashMap with search keys.
     *
     */
    public long countSimilarityAlgorithms(HashMap<String, String> searchMap) {
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
     * delete the specified SimilarityAlgorithm object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param searchMap A HashMap with search keys.
     */
    public int deleteSimilarityAlgorithm(HashMap<String, String> searchMap) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                // Special case for the Id field though we would expect the user to use
                // deleteSimilarityAlgorithmById instead.  Still want it to work here...
                if (key.compareTo(MongoIdFieldName) == 0) {
                    ObjectId theId = new ObjectId(searchMap.get(key));
                    thisDoc.put(key, theId);
                } else {
                    thisDoc.put(key, searchMap.get(key));
                }
                System.out.println("Inserting key: " + key + " with value " + searchMap.get(key));
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

