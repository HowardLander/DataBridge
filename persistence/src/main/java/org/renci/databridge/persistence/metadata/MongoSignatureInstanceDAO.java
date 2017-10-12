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
public class MongoSignatureInstanceDAO implements SignatureInstanceDAO {

    // This is a little confusing because both the data model and mongo
    // use the term "collection". This is the name of the mongo collection 
    // into which we are storing the DataBridge collection info.
    private static final String MongoName = new String("DB_SignatureInstance");
    private static final String MongoExtraName = new String("extra");
    private static final String MongoIdFieldName = new String("_id");



    /** 
     *  An iterator class for the SignatureInstanceTransferObject.  An instance is returned to the
     *  the user by the getCollection call.  This implements the Iterator interface.
     */
    private class MongoSignatureInstanceDAOIterator implements Iterator<SignatureInstanceTransferObject> {
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
       public SignatureInstanceTransferObject next() {
           SignatureInstanceTransferObject theSignatureInstance = null; 
           try {
               if (cursor.hasNext()) {
                   // Translate the date from the MongoDB representation to the 
                   // representation presented to the users in the Transfer object.
                   theSignatureInstance = new SignatureInstanceTransferObject();
                   DBObject theEntry = cursor.next();
                   theSignatureInstance.setClassName((String)theEntry.get("className"));
                   theSignatureInstance.setSourceNameSpace((String)theEntry.get("sourceNameSpace"));
                   theSignatureInstance.setTargetNameSpace((String)theEntry.get("targetNameSpace"));
                   theSignatureInstance.setFireEvent((boolean)theEntry.get("fireEvent"));
   
                   // Added this because there were some entries with no params or count
                   String params = (theEntry.get("params") != null) ? (String) theEntry.get("params") : "";
                   theSignatureInstance.setParams(params);

                   // Non user provided
                   theSignatureInstance.setVersion((int)theEntry.get("version"));
                   theSignatureInstance.setInsertTime(((ObjectId)theEntry.get(MongoIdFieldName)).getTimestamp());
                   theSignatureInstance.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
               }
           } catch (MongoException e) {
               // should send this back using the message logs eventually
               e.printStackTrace();
           }
           return theSignatureInstance;
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
     * insert the specified SignatureInstance object into mongo.
     *
     * @param theSignatureInstance SignatureInstanceTransfer object containing info to be inserted.
     */
    public boolean insertSignatureInstance(SignatureInstanceTransferObject theSignatureInstance) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theSignatureInstance.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("className", theSignatureInstance.getClassName());
          thisDoc.put("sourceNameSpace", theSignatureInstance.getSourceNameSpace());
          thisDoc.put("targetNameSpace", theSignatureInstance.getTargetNameSpace());
          thisDoc.put("params", theSignatureInstance.getParams());
          thisDoc.put("version", theSignatureInstance.getVersion());
          thisDoc.put("fireEvent", theSignatureInstance.getFireEvent());
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
     * retrieve a SignatureInstanceTransfer object (or null) for the record matching the provided id
     *
     * @param id The id to search for.
     */
    public SignatureInstanceTransferObject getSignatureInstanceById(String id) {
        SignatureInstanceTransferObject theSignatureInstance = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(id);
            thisDoc.put(MongoIdFieldName, theId);
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            if (cursor.hasNext()) {
               theSignatureInstance = new SignatureInstanceTransferObject();
               DBObject theEntry = cursor.next();
               theSignatureInstance.setClassName((String)theEntry.get("className"));
               theSignatureInstance.setSourceNameSpace((String)theEntry.get("sourceNameSpace"));
               theSignatureInstance.setTargetNameSpace((String)theEntry.get("targetNameSpace"));
   
               // Added this because there were some entries with no params or count
               String params = (theEntry.get("params") != null) ? (String) theEntry.get("params") : "";
               theSignatureInstance.setParams(params);
               theSignatureInstance.setFireEvent((boolean)theEntry.get("fireEvent"));

               // non-user provide
               theSignatureInstance.setVersion((int)theEntry.get("version"));
               theSignatureInstance.setInsertTime(((ObjectId)theEntry.get(MongoIdFieldName)).getTimestamp());
               theSignatureInstance.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
            }
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theSignatureInstance;
    }


    /** 
     * retrieve an iterator for all records that match the given search key.
     *
     * @param searchMap A HashMap with search keys.
     */
    public Iterator<SignatureInstanceTransferObject> getSignatureInstances(HashMap<String, String> searchMap) {
        MongoSignatureInstanceDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                //System.out.println("Adding key: "  + key + " with value " + searchMap.get(key));
                thisDoc.put(key, searchMap.get(key));
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoSignatureInstanceDAOIterator();
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
    public Iterator<SignatureInstanceTransferObject> getSignatureInstances(HashMap<String, String> searchMap,
 HashMap<String, String> sortMap, Integer limit) {
        MongoSignatureInstanceDAOIterator theIterator = null;
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
                if (sortValue.compareTo(SignatureInstanceDAO.SORT_ASCENDING) == 0) {
                   thisSortObject.put(key, 1);
                } else if (sortValue.compareTo(SignatureInstanceDAO.SORT_DESCENDING) == 0) {
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
            theIterator = new MongoSignatureInstanceDAOIterator();
            theIterator.cursor = cursor;
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }



    /** 
     * delete the specified SignatureInstance object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param theSignatureInstance The instance to delete
     */
    public int deleteSignatureInstance(SignatureInstanceTransferObject theSignatureInstance) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(theSignatureInstance.getDataStoreId());
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
     * count the number of items in the "SignatureInstance" table that match the search keys in the search map.
     * @param searchMap A HashMap with search keys.
     *
     */
    public long countSignatureInstances(HashMap<String, String> searchMap) {
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
     * delete the specified SignatureInstance object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param searchMap A HashMap with search keys.
     */
    public int deleteSignatureInstance(HashMap<String, String> searchMap) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                // Special case for the Id field though we would expect the user to use
                // deleteSignatureInstanceById instead.  Still want it to work here...
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

