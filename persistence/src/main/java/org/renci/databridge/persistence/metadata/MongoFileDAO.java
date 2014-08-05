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

/** This is the version of the Data Analysis Object for the File objects
 *  stored in MongoDB.  It's got the normal CRUD pattern stuff, plus whatever
 *  other methods become needed. 
 */
public class MongoFileDAO implements FileDAO {

    // This is a little confusing because both the data model and mongo
    // use the term "collection". This is the name of the mongo collection 
    // into which we are storing the DataBridge collection info.
    private static final String MongoName = new String("DB_File");
    private static final String MongoExtraName = new String("extra");
    private static final String MongoIdFieldName = new String("_id");
    private static final String MongoCollectionIdFieldName = new String("collection_id");


    /** 
     *  An iterator class for the FileTransferObject.  An instance is returned to the
     *  the user by the getFile call.  This implements the Iterator interface.
     */
    private class MongoFileDAOIterator implements Iterator<FileTransferObject> {
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
        * Returns the next FileTransferObject from the cursor or null.
        */
       @Override
       public FileTransferObject next() {
           FileTransferObject theFile = null; 
           try {
               if (cursor.hasNext()) {
                   // Translate the date from the MongoDB representation to the 
                   // representation presented to the users in the Transfer object.
                   theFile = new FileTransferObject();
                   DBObject theEntry = cursor.next();
                   theFile.setURL((String)theEntry.get("URL"));
                   theFile.setName((String)theEntry.get("name"));
                   theFile.setDescription((String)theEntry.get("description"));
                   theFile.setNameSpace((String)theEntry.get("nameSpace"));
                   theFile.setVersion((int)theEntry.get("version"));
                   theFile.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
                   theFile.setCollectionDataStoreId(theEntry.get(MongoCollectionIdFieldName).toString());
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
                   theFile.setExtra(extra);
               }
           } catch (MongoException e) {
               // should send this back using the message logs eventually
               e.printStackTrace();
           }
           return theFile;
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
     * insert the specified File object into mongo. Note that if the collectionDataStorId field
     * is not filled out, the file won't be attached to a collection. Consider using
     * insertFileForCollection instead.
     *
     * @param theFile FileTransfer object containing info to be inserted.
     */
    public boolean insertFile(FileTransferObject theFile) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theFile.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("URL", theFile.getURL());
          thisDoc.put("name", theFile.getName());
          thisDoc.put("description", theFile.getDescription());
          thisDoc.put("nameSpace", theFile.getNameSpace());
          thisDoc.put("version", theFile.getVersion());
          String collId = theFile.getCollectionDataStoreId();
          if (null != collId) {
              ObjectId theCollectionId = new ObjectId(collId);
              thisDoc.put(MongoCollectionIdFieldName, theCollectionId);
          }
          HashMap <String, String> extra = theFile.getExtra();

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
     * insert the specified File object into mongo. Note that if the DataStoreId field in the collection
     * object is not filled out, the file won't be attached to a collection. 
     *
     * @param theCollection Collection object containing this file.
     * @param theFile FileTransfer object containing info to be inserted.
     */
    public boolean insertFile(CollectionTransferObject theCollection, FileTransferObject theFile) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theFile.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("URL", theFile.getURL());
          thisDoc.put("name", theFile.getName());
          thisDoc.put("description", theFile.getDescription());
          thisDoc.put("nameSpace", theFile.getNameSpace());
          thisDoc.put("version", theFile.getVersion());
          String collId = theCollection.getDataStoreId();
          if (null != collId) {
              ObjectId theCollectionId = new ObjectId(collId);
              thisDoc.put(MongoCollectionIdFieldName,theCollectionId);
          }
          HashMap <String, String> extra = theFile.getExtra();

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
    public Iterator<FileTransferObject> getFiles(HashMap<String, String> searchMap) {
        MongoFileDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                thisDoc.put(key, searchMap.get(key));
            }
            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoFileDAOIterator();
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
    public Iterator<FileTransferObject> getFiles(CollectionTransferObject theCollection) {
        MongoFileDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            String collId = theCollection.getDataStoreId();
            if (null != collId) {
                ObjectId theId = new ObjectId(collId);
                thisDoc.put(MongoCollectionIdFieldName,theId);

                DB theDB = MongoDAOFactory.getTheDB();
                DBCollection theTable = theDB.getCollection(MongoName);
                DBCursor cursor = theTable.find(thisDoc);
                theIterator = new MongoFileDAOIterator();
                theIterator.cursor = cursor;
            }
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }


    public boolean updateFile( FileTransferObject theFile, 
                                    Object collectionID) {
        return true;

    }

    /** 
     * delete the specified File object from mongo. 
     *
     * @param searchMap A HashMap with search keys.
     */
    public int deleteFile(FileTransferObject theFile) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(theFile.getDataStoreId());
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
     * delete the specified File object from mongo. Note that this API deletes whatever matches
     * the search keys in the search map.
     *
     * @param searchMap A HashMap with search keys.
     */
    public int deleteFile(HashMap<String, String> searchMap) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            for (String key : searchMap.keySet()) {
                // Special case for the Id field though we would expect the user to use
                // deleteFileById instead.  Still want it to work here...
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

