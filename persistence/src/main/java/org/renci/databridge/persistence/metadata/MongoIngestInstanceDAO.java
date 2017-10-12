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
public class MongoIngestInstanceDAO implements IngestInstanceDAO {

    // This is a little confusing because both the data model and mongo
    // use the term "collection". This is the name of the mongo collection 
    // into which we are storing the DataBridge collection info.
    private static final String MongoName = new String("DB_IngestInstance");
    private static final String MongoExtraName = new String("extra");
    private static final String MongoIdFieldName = new String("_id");



    /** 
     *  An iterator class for the IngestInstanceTransferObject.  An instance is returned to the
     *  the user by the getCollection call.  This implements the Iterator interface.
     */
    private class MongoIngestInstanceDAOIterator implements Iterator<IngestInstanceTransferObject> {
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
       public IngestInstanceTransferObject next() {
           IngestInstanceTransferObject theIngestInstance = null; 
           try {
               if (cursor.hasNext()) {
                   // Translate the date from the MongoDB representation to the 
                   // representation presented to the users in the Transfer object.
                   theIngestInstance = new IngestInstanceTransferObject();
                   DBObject theEntry = cursor.next();
                   theIngestInstance.setClassName((String)theEntry.get("className"));
                   theIngestInstance.setNameSpace((String)theEntry.get("nameSpace"));
                   theIngestInstance.setInput((String)theEntry.get("input"));
                   theIngestInstance.setFireEvent((boolean)theEntry.get("fireEvent"));
   
                   // Added this because there were some entries with no params or count
                   String params = (theEntry.get("params") != null) ? (String) theEntry.get("params") : "";
                   theIngestInstance.setParams(params);

                   // Non user provided
                   theIngestInstance.setVersion((int)theEntry.get("version"));
                   theIngestInstance.setInsertTime(((ObjectId)theEntry.get(MongoIdFieldName)).getTimestamp());
                   theIngestInstance.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
               }
           } catch (MongoException e) {
               // should send this back using the message logs eventually
               e.printStackTrace();
           }
           return theIngestInstance;
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
     * insert the specified IngestInstance object into mongo.
     *
     * @param theIngestInstance IngestInstanceTransfer object containing info to be inserted.
     */
    public boolean insertIngestInstance(IngestInstanceTransferObject theIngestInstance) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theIngestInstance.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("className", theIngestInstance.getClassName());
          thisDoc.put("nameSpace", theIngestInstance.getNameSpace());
          thisDoc.put("input", theIngestInstance.getInput());
          thisDoc.put("params", theIngestInstance.getParams());
          thisDoc.put("version", theIngestInstance.getVersion());
          thisDoc.put("fireEvent", theIngestInstance.getFireEvent());
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
     * retrieve a IngestInstanceTransfer object (or null) for the record matching the provided id
     *
     * @param id The id to search for.
     */
    public IngestInstanceTransferObject getIngestInstanceById(String id) {
        IngestInstanceTransferObject theIngestInstance = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(id);
            thisDoc.put(MongoIdFieldName, theId);
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            if (cursor.hasNext()) {
               theIngestInstance = new IngestInstanceTransferObject();
               DBObject theEntry = cursor.next();
               theIngestInstance.setClassName((String)theEntry.get("className"));
               theIngestInstance.setNameSpace((String)theEntry.get("nameSpace"));
               theIngestInstance.setInput((String)theEntry.get("input"));
   
               // Added this because there were some entries with no params or count
               String params = (theEntry.get("params") != null) ? (String) theEntry.get("params") : "";
               theIngestInstance.setParams(params);
               theIngestInstance.setFireEvent((boolean)theEntry.get("fireEvent"));

               // non-user provide
               theIngestInstance.setVersion((int)theEntry.get("version"));
               theIngestInstance.setInsertTime(((ObjectId)theEntry.get(MongoIdFieldName)).getTimestamp());
               theIngestInstance.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
            }
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIngestInstance;
    }


    /** 
     * retrieve an iterator for all records that match the given search key.
     *
     * @param searchMap A HashMap with search keys.
     */
    public Iterator<IngestInstanceTransferObject> getIngestInstances(HashMap<String, String> searchMap) {
        MongoIngestInstanceDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                //System.out.println("Adding key: "  + key + " with value " + searchMap.get(key));
                thisDoc.put(key, searchMap.get(key));
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoIngestInstanceDAOIterator();
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
    public Iterator<IngestInstanceTransferObject> getIngestInstances(HashMap<String, String> searchMap,
 HashMap<String, String> sortMap, Integer limit) {
        MongoIngestInstanceDAOIterator theIterator = null;
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
                if (sortValue.compareTo(IngestInstanceDAO.SORT_ASCENDING) == 0) {
                   thisSortObject.put(key, 1);
                } else if (sortValue.compareTo(IngestInstanceDAO.SORT_DESCENDING) == 0) {
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
            theIterator = new MongoIngestInstanceDAOIterator();
            theIterator.cursor = cursor;
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }



    /** 
     * delete the specified IngestInstance object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param theIngestInstance The instance to delete
     */
    public int deleteIngestInstance(IngestInstanceTransferObject theIngestInstance) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(theIngestInstance.getDataStoreId());
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
     * count the number of items in the "IngestInstance" table that match the search keys in the search map.
     * @param searchMap A HashMap with search keys.
     *
     */
    public long countIngestInstances(HashMap<String, String> searchMap) {
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
     * delete the specified IngestInstance object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param searchMap A HashMap with search keys.
     */
    public int deleteIngestInstance(HashMap<String, String> searchMap) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                // Special case for the Id field though we would expect the user to use
                // deleteIngestInstanceById instead.  Still want it to work here...
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

