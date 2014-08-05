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

/** This is the version of the Data Analysis Object for the Variable objects
 *  stored in MongoDB.  It's got the normal CRUD pattern stuff, plus whatever
 *  other methods become needed. 
 */
public class MongoVariableDAO implements VariableDAO {

    // This is a little confusing because both the data model and mongo
    // use the term "collection". This is the name of the mongo collection 
    // into which we are storing the DataBridge collection info.
    private static final String MongoName = new String("DB_Variable");
    private static final String MongoExtraName = new String("extra");
    private static final String MongoIdFieldName = new String("_id");
    private static final String MongoFileIdFieldName = new String("file_id");


    /** 
     *  An iterator class for the VariableTransferObject.  An instance is returned to the
     *  the user by the getVariable call.  This implements the Iterator interface.
     */
    private class MongoVariableDAOIterator implements Iterator<VariableTransferObject> {
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
        * Returns the next VariableTransferObject from the cursor or null.
        */
       @Override
       public VariableTransferObject next() {
           VariableTransferObject theVariable = null; 
           try {
               if (cursor.hasNext()) {
                   // Translate the date from the MongoDB representation to the 
                   // representation presented to the users in the Transfer object.
                   theVariable = new VariableTransferObject();
                   DBObject theEntry = cursor.next();
                   theVariable.setName((String)theEntry.get("name"));
                   theVariable.setDescription((String)theEntry.get("description"));
                   theVariable.setVersion((int)theEntry.get("version"));
                   theVariable.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
                   theVariable.setFileDataStoreId(theEntry.get(MongoFileIdFieldName).toString());
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
                   theVariable.setExtra(extra);
               }
           } catch (MongoException e) {
               // should send this back using the message logs eventually
               e.printStackTrace();
           }
           return theVariable;
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
     * insert the specified Variable object into mongo. Note that if the collectionDataStorId field
     * is not filled out, the file won't be attached to a collection. Consider using
     * insertVariableForCollection instead.
     *
     * @param theVariable VariableTransfer object containing info to be inserted.
     */
    public boolean insertVariable(VariableTransferObject theVariable) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theVariable.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("name", theVariable.getName());
          thisDoc.put("description", theVariable.getDescription());
          thisDoc.put("version", theVariable.getVersion());
          String collId = theVariable.getFileDataStoreId();
          if (null != collId) {
              ObjectId theCollectionId = new ObjectId(collId);
              thisDoc.put(MongoFileIdFieldName, theCollectionId);
          }
          HashMap <String, String> extra = theVariable.getExtra();

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

    /** 
     * insert the specified Variable object into mongo. Note that if the DataStoreId field in the collection
     * object is not filled out, the file won't be attached to a collection. 
     *
     * @param theFile File object containing this file.
     * @param theVariable VariableTransfer object containing info to be inserted.
     */
    public boolean insertVariable(FileTransferObject theFile, VariableTransferObject theVariable) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theVariable.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("name", theVariable.getName());
          thisDoc.put("description", theVariable.getDescription());
          thisDoc.put("version", theVariable.getVersion());
          String fileId = theFile.getDataStoreId();
          if (null != fileId) {
              ObjectId theFileId = new ObjectId(fileId);
              thisDoc.put(MongoFileIdFieldName,theFileId);
          }
          HashMap <String, String> extra = theVariable.getExtra();

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

    /** 
     * retrieve an iterator for all records that match the given search key.
     *
     * @param searchMap A HashMap with search keys.
     */
    public Iterator<VariableTransferObject> getVariables(HashMap<String, String> searchMap) {
        MongoVariableDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                thisDoc.put(key, searchMap.get(key));
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoVariableDAOIterator();
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
     * @param theCollection The collection for which to find associated files
     */
    public Iterator<VariableTransferObject> getVariables(FileTransferObject theFile) {
        MongoVariableDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            String fileId = theFile.getDataStoreId();
            if (null != fileId) {
                ObjectId theId = new ObjectId(fileId);
                thisDoc.put(MongoFileIdFieldName,theId);

                DB theDB = MongoDAOFactory.getTheDB();
                DBCollection theTable = theDB.getCollection(MongoName);
                DBCursor cursor = theTable.find(thisDoc);
                theIterator = new MongoVariableDAOIterator();
                theIterator.cursor = cursor;
            }
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }


    public boolean updateVariable( VariableTransferObject theVariable, 
                                    Object collectionID) {
        return true;

    }

    /** 
     * delete the specified Variable object from mongo. 
     *
     * @param searchMap A HashMap with search keys.
     */
    public int deleteVariable(VariableTransferObject theVar) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(theVar.getDataStoreId());
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
     * delete the specified Variable object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param searchMap A HashMap with search keys.
     */
    public int deleteVariable(HashMap<String, String> searchMap) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                // Special case for the Id field though we would expect the user to use
                // deleteVariableById instead.  Still want it to work here...
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

