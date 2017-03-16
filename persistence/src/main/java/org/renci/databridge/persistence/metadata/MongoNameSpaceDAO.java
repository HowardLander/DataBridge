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

/** This is the version of the Data Analysis Object for the NameSpace objects
 *  stored in MongoDB.  It's got the normal CRUD pattern stuff, plus whatever
 *  other methods become needed. 
 */
public class MongoNameSpaceDAO implements NameSpaceDAO {

    private static final String MongoName = new String("DB_NameSpace");
    private static final String MongoIdFieldName = new String("_id");



    /** 
     *  An iterator class for the NameSpaceTransferObject.  An instance is returned to the
     *  the user by the getNameSpaces call.  This implements the Iterator interface.
     */
    private class MongoNameSpaceDAOIterator implements Iterator<NameSpaceTransferObject> {
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
       public NameSpaceTransferObject next() {
           NameSpaceTransferObject theNameSpace = null; 
           try {
               if (cursor.hasNext()) {
                   // Translate the date from the MongoDB representation to the 
                   // representation presented to the users in the Transfer object.
                   theNameSpace = new NameSpaceTransferObject();
                   DBObject theEntry = cursor.next();
                   theNameSpace.setNameSpace((String)theEntry.get("nameSpace"));
                   theNameSpace.setDescription((String)theEntry.get("description"));
                   theNameSpace.setDataStoreId(theEntry.get(MongoIdFieldName).toString());
               }
           } catch (MongoException e) {
               // should send this back using the message logs eventually
               e.printStackTrace();
           }
           return theNameSpace;
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
     * insert the specified NameSpace object into mongo. Note that this routine does
     * not check for duplicates: the same action could be inserted repeatedly. If
     * that's a problem, use the delete first.
     *
     * @param theNameSpace NameSpaceTransfer object containing info to be inserted.
     */
    public boolean insertNameSpace(NameSpaceTransferObject theNameSpace) {
        boolean returnCode = true;
        try {
          BasicDBObject thisDoc = new BasicDBObject();
          // We don't want Mongo to create an ID for us, as we will need it later
          // to files and variables.  So we are going to create the id, use it with the 
          // insert and store it in the transfer object.
          ObjectId theId = new ObjectId();
          theNameSpace.setDataStoreId(theId.toString());
          
          thisDoc.put(MongoIdFieldName, theId);
          thisDoc.put("nameSpace", theNameSpace.getNameSpace());
          thisDoc.put("description", theNameSpace.getDescription());

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
     * retrieve an iterator for all nameSpaces
     *
     */
    public Iterator<NameSpaceTransferObject> getNameSpaces() {
        MongoNameSpaceDAOIterator theIterator = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();

            DB theDB = MongoDAOFactory.getTheDB();
            DBCollection theTable = theDB.getCollection(MongoName);
            DBCursor cursor = theTable.find(thisDoc);
            theIterator = new MongoNameSpaceDAOIterator();
            theIterator.cursor = cursor;
        } catch (MongoException e) {
            // should send this back using the message logs eventually
            e.printStackTrace(); 
        }

        return theIterator;
    }

    /** 
     * delete the specified NameSpace object from mongo. 
     *
     * @param theNameSpace The collection object to be deleted.
     */
    public int deleteNameSpace(NameSpaceTransferObject theNameSpace) {
        WriteResult theResult = null;
        try {
            BasicDBObject thisDoc = new BasicDBObject();
            ObjectId theId = new ObjectId(theNameSpace.getDataStoreId());
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

}

