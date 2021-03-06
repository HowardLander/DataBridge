package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class SimilarityInstanceTransferObject {
    private String className;
    private String method;
    private int    version;
    private String nameSpace;
    private String output;      // currently a file, could later be a database.
    private long count;
    private String params;
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
     * Get method.
     *
     * @return method as String.
     */
    public String getMethod()
    {
        return method;
    }
    
    /**
     * Set method.
     *
     * @param method the value to set.
     */
    public void setMethod(String method)
    {
        this.method = method;
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
     * Get output.
     *
     * @return output as String.
     */
    public String getOutput()
    {
        return output;
    }
    
    /**
     * Set output.
     *
     * @param output the value to set.
     */
    public void setOutput(String output)
    {
        this.output = output;
    }
    
    /**
     * Get count.
     *
     * @return count as long.
     */
    public long getCount()
    {
        return count;
    }
    
    /**
     * Set count.
     *
     * @param count the value to set.
     */
    public void setCount(long count)
    {
        this.count = count;
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
}

