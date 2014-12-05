package org.renci.databridge.persistence.network;
import  java.util.*;

public class NetworkNodeTransferObject {
    private String nameSpace;          // A "lable"
    private String nodeId; // The id of the node in the metadata database.
    private HashMap<String, Object> attributes;
    private String dataStoreId; // The id assigned by the underlying data store
    
    /**
     * Get nameSpace.
     *
     * @return nameSpace as String.
     */
    public String getNameSpace()
    {
        return nameSpace;
    }
    
    /**
     * Set nameSpace.
     *
     * @param nameSpace the value to set.
     */
    public void setNameSpace(String nameSpace)
    {
        this.nameSpace = nameSpace;
    }
    
    /**
     * Get nodeId.
     *
     * @return nodeId as String.
     */
    public String getNodeId()
    {
        return nodeId;
    }
    
    /**
     * Set nodeId.
     *
     * @param nodeId the value to set.
     */
    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
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
}

