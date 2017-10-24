package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class SimilarityAlgorithmTransferObject {
    // User provided items
    private String className;
    private String type;
    private String description;
    private String engineParams; // parameters about this algorithm that the engine needs to access

    // DataBridge generated items
    private int    version;
    private int insertTime;     // Seconds since the epoch
    private String dataStoreId; // The id generated at insertion time.
    
    /**
     * Get className.
     *
     * @return className as String.
     */
    public String getClassName()
    {
        return className;
    }
    
    /**
     * Set className.
     *
     * @param className the value to set.
     */
    public void setClassName(String className)
    {
        this.className = className;
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
     * Get insertTime.
     *
     * @return insertTime as int.
     */
    public int getInsertTime()
    {
        return insertTime;
    }
    
    /**
     * Set insertTime.
     *
     * @param insertTime the value to set.
     */
    public void setInsertTime(int insertTime)
    {
        this.insertTime = insertTime;
    }
    
    
    /**
     * Get type.
     *
     * @return type as String.
     */
    public String getType()
    {
        return type;
    }
    
    /**
     * Set type.
     *
     * @param type the value to set.
     */
    public void setType(String type)
    {
        this.type = type;
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
     * Get engineParams.
     *
     * @return engineParams as String.
     */
    public String getEngineParams()
    {
        return engineParams;
    }
    
    /**
     * Set engineParams.
     *
     * @param engineParams the value to set.
     */
    public void setEngineParams(String engineParams)
    {
        this.engineParams = engineParams;
    }
    
    /**
     * Get version.
     *
     * @return version as int.
     */
    public int getVersion()
    {
        return version;
    }
    
    /**
     * Set version.
     *
     * @param version the value to set.
     */
    public void setVersion(int version)
    {
        this.version = version;
    }
}

