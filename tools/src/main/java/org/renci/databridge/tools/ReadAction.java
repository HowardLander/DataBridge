package org.renci.databridge.tools;

import java.io.*;
import java.util.*;

import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.util.*;

/*
 * Use this class to add to the action table. 
 *  arg 0: the properties file
 *  arg 1: the message to be reacted to
 *  arg 2: The name space to operate on
 * 
 *  Example mvn command line:
 *   mvn -e exec:java -Dexec.mainClass=org.renci.databridge.tools.ReadAction -Dexec.arguments=tools.conf,Processed.Metadata.To.MetadataDB,test_action_1
 */

public class ReadAction {
  public static void main(String [] args) {

    String dbType;
    String dbName;
    String dbHost;
    int    dbPort;
    String dbUser;
    String dbPwd;

    MetadataDAOFactory theFactory = null;

    try {

        // open the preferences file
        Properties prop = new Properties();
        prop.load(new FileInputStream(args[0]));
        dbType = prop.getProperty("org.renci.databridge.relevancedb.dbType", "mongo");
        dbName = prop.getProperty("org.renci.databridge.relevancedb.dbName", "test");
        dbHost = prop.getProperty("org.renci.databridge.relevancedb.dbHost", "localhost");
        dbPort = Integer.parseInt(prop.getProperty("org.renci.databridge.relevancedb.dbPort", "27017"));
        dbUser = prop.getProperty("org.renci.databridge.relevancedb.dbUser", "localhost");
        dbPwd =  prop.getProperty("org.renci.databridge.relevancedb.dbPassword", "localhost");

        // Connect to the mongo database
        if (dbType.compareToIgnoreCase("mongo") == 0) {
            theFactory = MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB,
                                                                  dbName, dbHost, dbPort, dbUser, dbPwd);
            if (null == theFactory) {
               System.out.println("Couldn't produce the MetadataDAOFactory");
               return;
            }
        }

        // insert the action 
        ActionTransferObject theAction = new ActionTransferObject();

        ActionDAO theActionDAO = theFactory.getActionDAO();
        Iterator<ActionTransferObject> actionIt = theActionDAO.getActions(args[1], args[2]);
        System.out.println("Searching action table for: " + args[1] + " nameSpace: " + args[2]);
        String outputFile = null;
        while (actionIt.hasNext()) {
           ActionTransferObject returnedObject = actionIt.next();
           System.out.println("Found action: " + returnedObject.getDataStoreId());
       } 
    }  catch (Exception e) {
         e.printStackTrace();
    } 
  }
}
