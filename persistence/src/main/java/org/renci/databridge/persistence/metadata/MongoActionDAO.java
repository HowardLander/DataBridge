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

/** This is the version of the Data Analysis Object for the Action objects
 *  stored in MongoDB.  It's got the normal CRUD pattern stuff, plus whatever
 *  other methods become needed. 
 */
public class MongoActionDAO implements ActionDAO {

    private static final String MongoName = new String("DB_Action");
    private static final String MongoIdFieldName = new String("_id");
    private static final String MongoNamespace = new String("nameSpace");



    /** 
     *  An iterator class for the ActionTransferObject.  An instance is returned to the
     *  the user by the getActions call.  This implements the Iterator interface.
     */
    private class MongoActionDAOIterator implements Iterator<ActionTransferObject> {
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
       @SuppressWarnings("unchecked")
       public ActionTransferObject next() {
           ActionTransferObject theAction = null; 
           try {
               if (cursor.hasNext()) {
                   // Translate the date from the MongoDB representation to the 
                   // representation presented to the users in the Transfer object.
                   theAction = new ActionTransferObject();
                   DBObject theEntry = cursor.next();
                   theAction.setCurrentMessage((String)theEntry.get("currentMessage"));
                   String nextMessage = 
                       (theEntry.get("nextMessage") != null) ? (String) theEntry.get("nextMessage") : "";
                   theAction.setNextMessage(nextMessage);
                   theAction.setNameSpace((String)theEntry.get("nameSpace"));
                   theAction.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
                   theAction.setInsertTime(((ObjectId)theEntry.get(MongoIdFieldName)).getTimestamp());
                   ArrayList<BasicDBObject> dbHeaders = (ArrayList<BasicDBObject>) theEntry.get("headers");
                   HashMap<String, String> headerList = new HashMap<String, String>();
                   for (BasicDBObject headerObj : dbHeaders) {
                       // Get the key set.  
                       Set<String> keys = headerObj.keySet();
                       for (String thisKey : keys) {
                           headerList.put(thisKey, (String) headerObj.get(thisKey));
                       }
                   }
                   theAction.setHeaders(headerList);
               }
           } catch (MongoException e) {
               // should send this back using the message logs eventually
               e.printStackTrace();
           }
           return theAction;
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
     * insert the specified Action object into mongo. Note that this routine does
     * not check for duplicates: the same action could be inserted repeatedly. If
     * that's a problem, use the delete first.
     *
     * @param theAction ActionTransfer object containing info to be inserted.
     */
    public boolean insertAction(ActionTransferObject theAction) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theAction.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("currentMessage", theAction.getCurrentMessage());
          thisDoc.put("nextMessage", theAction.getNextMessage());
          thisDoc.put("nameSpace", theAction.getNameSpace());
          HashMap <String, String> headers = theAction.getHeaders();

          if (null != headers) {
             System.out.println("adding headers");
             // Convert the hash map of headers to a list so that
             // it can be inserted as an embedded array in the Mongo table.
             List<BasicDBObject> headerList = new ArrayList<BasicDBObject>();
             for (String key: headers.keySet()) {
                 headerList.add(new BasicDBObject(key, headers.get(key)));
             }
             thisDoc.put("headers", headerList);
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
     * @param currentMessage The message we are processing
     * @param nameSpace The nameSpace we are processing.
     */
    public Iterator<ActionTransferObject> getActions(String currentMessage, String nameSpace) {
        MongoActionDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            thisDoc.put("currentMessage", currentMessage);
            thisDoc.put("nameSpace", nameSpace);

            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoActionDAOIterator();
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
     * @param currentMessage The message we are processing
     */
    public Iterator<ActionTransferObject> getActions(String currentMessage) {
        MongoActionDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            thisDoc.put("currentMessage", currentMessage);

            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoActionDAOIterator();
            theIterator.cursor = cursor;
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }


/*
    public boolean updateAction( ActionTransferObject theAction, 
                                    Object collectionID) {
        return true;

    }
 */

    /** 
     * delete the specified Action object from mongo. 
     *
     * @param theAction The collection object to be deleted.
     */
    public int deleteAction(ActionTransferObject theAction) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(theAction.getDataStoreId());
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
     * delete the specified Action object from mongo. The currentMessage and nameSpace fields 
     * should uniquely identify the action.
     *
     * @param currentMessage the type of the message action
     * @param nameSpace the nameSpace of the action to be deleted
     */
    public int deleteAction(String currentMessage, String nameSpace) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            thisDoc.put("currentMessage", currentMessage);
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

