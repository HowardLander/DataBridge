package org.renci.databridge.persistence.metadata;
import  java.util.*;
import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.MongoException;

public class MongoDAOFactory extends MetadataDAOFactory {
 
    private static MongoClient theClient = null;
    private static String db;
    private static String host;
    private static String user;
    private static String password;
    private static int port;

    public MongoDAOFactory(String db, String host, int port, String user, String password) {
       this.db = db;
       this.host = host;
       this.port = port;
       this.user = user;
       this.password = password;
    }

    public static DB getTheDB() {
        DB theDB = null;
        MongoCredential theCredential = null;
        ServerAddress theAddress = null;
        try {
            if (null == theClient) {
                theCredential = MongoCredential.createCredential(user, db, password.toCharArray());
                theAddress = new ServerAddress(host, port);
                theClient = new MongoClient(theAddress, Arrays.asList(theCredential));
            }
            theDB = theClient.getDB(db);
        } catch (Exception e) {
            // should send this back using the message logs eventually
            e.printStackTrace();
        }

        return theDB;
    }

    public void closeTheDB() {
        try {
            if (null != theClient) {
                theClient.close();
                theClient = null;
            }
        } catch (Exception e) {
            // should send this back using the message logs eventually
            e.printStackTrace();
        }
    }

    public CollectionDAO getCollectionDAO() {
        return new MongoCollectionDAO();
    }

    public FileDAO getFileDAO() {
        return new MongoFileDAO();
    }

    public VariableDAO getVariableDAO() {
        return new MongoVariableDAO();
    } 

    public SimilarityInstanceDAO getSimilarityInstanceDAO() {
        return new MongoSimilarityInstanceDAO();
    } 

    public SimilarityAlgorithmDAO getSimilarityAlgorithmDAO() {
        return new MongoSimilarityAlgorithmDAO();
    } 

    public SNAInstanceDAO getSNAInstanceDAO() {
        return new MongoSNAInstanceDAO();
    } 

    public ActionDAO getActionDAO() {
        return new MongoActionDAO();
    } 

    public NameSpaceDAO getNameSpaceDAO() {
        return new MongoNameSpaceDAO();
    } 

    public LaneDAO getLaneDAO() {
        return new MongoLaneDAO();
    } 

    public IngestInstanceDAO getIngestInstanceDAO() {
        return new MongoIngestInstanceDAO();
    } 

    public SignatureInstanceDAO getSignatureInstanceDAO() {
        return new MongoSignatureInstanceDAO();
    } 

    /**
     * Get db.
     *
     * @return db as String.
     */
    public static String getDb()
    {
        return db;
    }
    
    /**
     * Set db.
     *
     * @param db the value to set.
     */
    public static void setDb(String db)
    {
        MongoDAOFactory.db = db;
    }
    
    /**
     * Get host.
     *
     * @return host as String.
     */
    public static String getHost()
    {
        return host;
    }
    
    /**
     * Set host.
     *
     * @param host the value to set.
     */
    public static void setHost(String host)
    {
        MongoDAOFactory.host = host;
    }
    
    /**
     * Get port.
     *
     * @return port as int.
     */
    public static int getPort()
    {
        return port;
    }
    
    /**
     * Set port.
     *
     * @param port the value to set.
     */
    public static void setPort(int port)
    {
        MongoDAOFactory.port = port;
    }

}
