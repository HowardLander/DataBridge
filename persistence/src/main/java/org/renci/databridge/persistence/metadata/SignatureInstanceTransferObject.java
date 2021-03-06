package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class SignatureInstanceTransferObject {
    // User provided items
    private String className;
    private String sourceNameSpace;
    private String targetNameSpace;
    private String params;
    private boolean fireEvent; // Controls whether or not to emit the Processed.Metadata.To.MetadataDB. 
                               // This is particulary import for bulk loading of metadata (multiple calls)

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
     * Get params.
     *
     * @return params as String.
     */
    public String getParams()
    {
        return params;
    }
    
    /**
     * Set params.
     *
     * @param params the value to set.
     */
    public void setParams(String params)
    {
        this.params = params;
    }
    
    /**
     * Get fireEvent.
     *
     * @return fireEvent as boolean.
     */
    public boolean getFireEvent()
    {
        return fireEvent;
    }
    
    /**
     * Set fireEvent.
     *
     * @param fireEvent the value to set.
     */
    public void setFireEvent(boolean fireEvent)
    {
        this.fireEvent = fireEvent;
    }
    
    /**
     * Get sourceNameSpace.
     *
     * @return sourceNameSpace as String.
     */
    public String getSourceNameSpace()
    {
        return sourceNameSpace;
    }
    
    /**
     * Set sourceNameSpace.
     *
     * @param sourceNameSpace the value to set.
     */
    public void setSourceNameSpace(String sourceNameSpace)
    {
        this.sourceNameSpace = sourceNameSpace;
    }
    
    /**
     * Get targetNameSpace.
     *
     * @return targetNameSpace as String.
     */
    public String getTargetNameSpace()
    {
        return targetNameSpace;
    }
    
    /**
     * Set targetNameSpace.
     *
     * @param targetNameSpace the value to set.
     */
    public void setTargetNameSpace(String targetNameSpace)
    {
        this.targetNameSpace = targetNameSpace;
    }
}

