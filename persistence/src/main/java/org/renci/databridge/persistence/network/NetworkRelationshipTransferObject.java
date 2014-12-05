package org.renci.databridge.persistence.network;
import  java.util.*;

public class NetworkRelationshipTransferObject {
    private String nodeId1; // The id of the first node in the metadata database.
    private String nodeId2; // The id of the second node in the metadata database.
    private String type;    // the relationship type.
    private HashMap<String, Object> attributes;
    private String dataStoreId;
    private String node1DataStoreId;
    private String node2DataStoreId;

	/**
	 * get the value of type
	 * @return the value of type
	 */
	public String getType(){
		return this.type;
	}
	/**
	 * set a new value to type
	 * @param type the new value to be used
	 */
	public void setType(String type) {
		this.type=type;
	}
    
    /**
     * Get nodeId.
     *
     * @return nodeId1 as String.
     */
    public String getNodeId1()
    {
        return nodeId1;
    }
    
    /**
     * Set nodeId1.
     *
     * @param nodeId1 the value to set.
     */
    public void setNodeId1(String nodeId)
    {
        this.nodeId1 = nodeId1;
    }

    /**
     * Get attributes.
     *
     * @return attributes as HashMap<String, Object>
     */
    public HashMap<String, Object> getAttributes()
    {
        return attributes;
    }

    /**
     * Set attributes.
     *
     * @param attributes the value to set.
     */
    public void setAttributes(HashMap<String, Object> attributes)
    {
        this.attributes = attributes;
    }
    
    /**
     * Get nodeId2.
     *
     * @return nodeId2 as String.
     */
    public String getNodeId2()
    {
        return nodeId2;
    }
    
    /**
     * Set nodeId2.
     *
     * @param nodeId2 the value to set.
     */
    public void setNodeId2(String nodeId2)
    {
        this.nodeId2 = nodeId2;
    }
    
    /**
     * Get dataStoreId.
     *
     * @return dataStoreId as String.
     */
    public String getDataStoreId()
    {
        return dataStoreId;
    }
    
    /**
     * Set dataStoreId.
     *
     * @param dataStoreId the value to set.
     */
    public void setDataStoreId(String dataStoreId)
    {
        this.dataStoreId = dataStoreId;
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
}

