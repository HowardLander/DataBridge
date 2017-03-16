package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class NameSpaceTransferObject {
    private String nameSpace;   // The name of the nameSpace
    private String description; // The description of the nameSpace

    // At some point, we will probably want some access controls by nameSpace.  I'm not yet sure what
    // the best way to do this is, so we'll leave it out now.

    // These attributes are specific to the DataBridge
    private String dataStoreId; // The id generated at insertion time.
    
    @Override
    public String toString () 
    {
        return "{" + getClass ().getName() + ": nameSpace: " + getNameSpace() + ", description: " + getDescription()  + ", dataStoreId: " + getDataStoreId () + "}";
    }
    
    
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
     * Get description.
     *
     * @return description as String.
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     * Set description.
     *
     * @param description the value to set.
     */
    public void setDescription(String description)
    {
        this.description = description;
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

