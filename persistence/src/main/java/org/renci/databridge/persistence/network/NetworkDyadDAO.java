package org.renci.databridge.persistence.network;
import  java.util.*;
/*
 * This DAO does not add or subtract anything from the database, it just allows us to produce a list
 * of connected nodes (know as Dyads) and the similarity value (strength of similarity) that connects
 * the 2 nodes. Unconnected nodes in the network are also included in the iterator.
 */
public interface NetworkDyadDAO {
    /**
     *  Retrieve an iterator for all the dyads in the network that exist in the given
     *  nameSpace and were produced using the given similarityID
     *  @param nameSpace The nameSpace in which to search for dyads
     *  @param similarityId The similarity metric to use in the search for dyads
     *  @return the requested iterator
     */
    public Iterator<NetworkDyadTransferObject> getNetworkDyads(String nameSpace, String similarityId);

    /**
     *  Retrieve an iterator for all the dyads in the network that exist in the given
     *  nameSpace and were produced using the given similarityID
     *  @param nameSpace The nameSpace in which to search for dyads
     *  @param similarityId The similarity metric to use in the search for dyads
     *  @param returnSingletons boolean for whether or not the user wants the singleton nodes
     *  @return the requested iterator
     */
    public Iterator<NetworkDyadTransferObject> getNetworkDyads(String nameSpace, 
                                                               String similarityId,
                                                               boolean returnSingletons);

    /**
     *  Retrieve the count of nodes that are not singletons in the specified
     *  nameSpace with the given similarityID
     *  @param nameSpace The nameSpace in which to search for dyads
     *  @param similarityId The similarity metric to use in the search for dyads
     *  @return the number of non singleton nodes
     */
    public long countConnectedNodes(String nameSpace, String similarityId);

    /**
     *  Retrieve an arraylist for all the singleton dyads in the network that exist in the given
     *  nameSpace and were produced using the given similarityID
     *  @param nameSpace The nameSpace in which to search for singletons
     *  @param similarityId The similarity metric to use in the search for singletons
     *  @return the requested iterator
     */
    public ArrayList<NetworkDyadTransferObject> getNetworkSingletonArray(String nameSpace, 
                                                                         String similarityId);
}
