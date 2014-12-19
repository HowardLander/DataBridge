package org.renci.databridge.persistence.network;
import  org.neo4j.graphdb.GraphDatabaseService;
import  org.neo4j.graphdb.factory.GraphDatabaseFactory;
import  java.util.*;

public class Neo4jDAOFactory extends NetworkDAOFactory {

    private static String databasePath;
    private static GraphDatabaseService theNetworkDB = null;

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it shuts down nicely when 
        // the VM exits (even if you "Ctrl-C" the running application). 
        // Courtesy of http://neo4j.com/docs/milestone/tutorials-java-embedded-setup.html
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    /**
     * This function returns null on failure.  It's up to the caller to check.
     * @return This function returns the GraphDatabaseService or null on failure.
     */
    public static GraphDatabaseService getTheNetworkDB () {
        if (null == theNetworkDB) {
           try {
               theNetworkDB = new GraphDatabaseFactory().newEmbeddedDatabase(databasePath);
 
               // close the database when this JVM exits.
               registerShutdownHook(theNetworkDB);
           } catch (Exception e) {
               // TODO: Instantiate some real logging for this server.
               e.printStackTrace();
           }
        } 
        return theNetworkDB;
    }

    /**
     * Constructor that takes the databasePath as an argument. Note that this only works for a local
     * (not network-accessed) database
     * @param the location of the database
     */
    public Neo4jDAOFactory(String databasePath){
        this.databasePath = databasePath;
    }

    /**
     * Factory method that returns a Neo4j version of the NetworkNodeDAO object.
     * @return A NetworkNodeDAO object
     */
    public NetworkNodeDAO getNetworkNodeDAO() {
        return new Neo4jNetworkNodeDAO();
    }

    /**
     * Factory method that returns a Neo4j version of the NetworkRelationshipDAO object.
     * @return A NetworkRelationshipDAO object
     */
    public NetworkRelationshipDAO getNetworkRelationshipDAO() {
        return new Neo4jNetworkRelationshipDAO();
    }

    /**
     * Factory method that returns a Neo4j version of the NetworkDyadDAO object.
     * @return A NetworkDyadDAO object
     */
    public NetworkDyadDAO getNetworkDyadDAO() {
        return new Neo4jNetworkDyadDAO();
    }


    
    /**
     * Get databasePath.
     *
     * @return databasePath as String.
     */
    public static String getDatabasePath()
    {
        return databasePath;
    }
    
    /**
     * Set databasePath.
     *
     * @param databasePath the value to set.
     */
    public static void setDatabasePath(String databasePath)
    {
        Neo4jDAOFactory.databasePath = databasePath;
    }
}
