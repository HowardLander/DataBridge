package org.renci.databridge.persistence.network;
import  java.util.*;

public abstract class NetworkDAOFactory {
    // Right now we are only supporting Neo4j DB
    public static final int NEO4JDB = 1;

    public abstract NetworkNodeDAO getNetworkNodeDAO();
    public abstract NetworkRelationshipDAO getNetworkRelationshipDAO();

    /**
     * @param factoryType  The type of the underlying database.  Currently only NEO4JDB is supported.
     * @param databasePath The path to the database. Note that this enforces that this
     *                     server is running on the same machine as the database. For now, that's
     *                     OK.
     * @return A Neo4jDAOFactory object of the type specified in the factoryType parameter.
     */
    public static NetworkDAOFactory getNetworkDAOFactory(int factoryType, String databasePath) {

        switch (factoryType) {
            case NEO4JDB:
                return new Neo4jDAOFactory(databasePath);
            default:
                return null;
        }
    }
}
