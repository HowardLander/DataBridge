package org.renci.databridge.persistence.network;
import org.renci.databridge.persistence.network.*;
import java.io.*;
import java.util.*;

/**
 * This is the interface for classes that are to be instantiated and executed by the
 * Network engine to produce a set of clusters for a given network
 *
 * @author Howard Lander -RENCI (www.renci.org)
 *
 */
public interface SNAInterface {

    /**
     *  Process a network through an SNA algorithm and return a partition
     *  of the network into a set of clusters. The network is selected based on user
     *  provided information in the message that forked this off.
     *  @param  theDyads an iterator of NetworkTransferObjects that represents the network requested.
     *  @param  params any extra parameters the caller wants to pass to the algorithm.
     *  @return ArrayList of clusters.  For each cluster, the key is the cluster id, which we are 
     *          representing as a string. The value is an array of strings that are the identifiers of
     *          the nodes in that cluster.  Note that a cluster with only one node represents an 
     *          isolated node.
     */
    HashMap<String, String[]> processNetwork(Iterator<NetworkDyadTransferObject> theDyads, String params);
}
