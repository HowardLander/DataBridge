package org.renci.databridge.persistence.network;
import  java.util.*;
/**
 * The NetworkDyadTransferObject represents two nodes and the relationship between them, although in the
 * degenerate case only a single unconnected node is seen. This class is intended to be used as an
 * iterator for a parameter to an interface implemented by user supplied methods. These methods
 * need to build an in memory or streaming image of the entire network and do the social network
 * analyses.
 */
public class NetworkDyadTransferObject {
    private String node1DataStoreId; // First node id in the network database.
    private String node2DataStoreId; // Second node id the network database. null if the node is isolated
    private double similarity; // The similarity between these to nodes.
    
    
    /**
     * Get similarity.
     *
     * @return similarity as double.
     */
    public double getSimilarity()
    {
        return similarity;
    }
    
    /**
     * Set similarity.
     *
     * @param similarity the value to set.
     */
    public void setSimilarity(double similarity)
    {
        this.similarity = similarity;
    }
    
    /**
     * Get node1DataStoreId.
     *
     * @return node1DataStoreId as String.
     */
    public String getNode1DataStoreId()
    {
        return node1DataStoreId;
    }
    
    /**
     * Set node1DataStoreId.
     *
     * @param node1DataStoreId the value to set.
     */
    public void setNode1DataStoreId(String node1DataStoreId)
    {
        this.node1DataStoreId = node1DataStoreId;
    }
    
    /**
     * Get node2DataStoreId.
     *
     * @return node2DataStoreId as String.
     */
    public String getNode2DataStoreId()
    {
        return node2DataStoreId;
    }
    
    /**
     * Set node2DataStoreId.
     *
     * @param node2DataStoreId the value to set.
     */
    public void setNode2DataStoreId(String node2DataStoreId)
    {
        this.node2DataStoreId = node2DataStoreId;
    }

    /**
      * Intended only for debugging
      */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();

    result.append(this.getClass().getName() + " Object {" );
    result.append(" Id1: " + node1DataStoreId);
    result.append(" Similarity: " + similarity);
    result.append(" Id2: " + node2DataStoreId);
    result.append("}");

    return result.toString();
  }
}

