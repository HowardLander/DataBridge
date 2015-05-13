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
    private String node1MetadataId;  // Id of node 1 in the metadata database
    private String node2MetadataId;  // Id of node 2 in the metadata database, null if the node is isolated
    private int i; // The i value of the first node in the original similarity matrix. origMatrix(i,j) = similarity
    private int j; // The j value of the second node in the original similarity matrix
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
    result.append(" metaDataId1: " + node1MetadataId);
    result.append(" Similarity: " + similarity);
    result.append(" Id2: " + node2DataStoreId);
    result.append(" metaDataId2: " + node2MetadataId);
    result.append("}");

    return result.toString();
  }
    
    /**
     * Get node1MetadataId.
     *
     * @return node1MetadataId as String.
     */
    public String getNode1MetadataId()
    {
        return node1MetadataId;
    }
    
    /**
     * Set node1MetadataId.
     *
     * @param node1MetadataId the value to set.
     */
    public void setNode1MetadataId(String node1MetadataId)
    {
        this.node1MetadataId = node1MetadataId;
    }
    
    /**
     * Get node2MetadataId.
     *
     * @return node2MetadataId as String.
     */
    public String getNode2MetadataId()
    {
        return node2MetadataId;
    }
    
    /**
     * Set node2MetadataId.
     *
     * @param node2MetadataId the value to set.
     */
    public void setNode2MetadataId(String node2MetadataId)
    {
        this.node2MetadataId = node2MetadataId;
    }
    
    /**
     * Get i.
     *
     * @return i as int.
     */
    public int getI()
    {
        return i;
    }
    
    /**
     * Set i.
     *
     * @param i the value to set.
     */
    public void setI(int i)
    {
        this.i = i;
    }
    
    /**
     * Get j.
     *
     * @return j as int.
     */
    public int getJ()
    {
        return j;
    }
    
    /**
     * Set j.
     *
     * @param j the value to set.
     */
    public void setJ(int j)
    {
        this.j = j;
    }
}

