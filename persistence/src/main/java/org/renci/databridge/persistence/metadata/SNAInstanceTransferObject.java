package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class SNAInstanceTransferObject {
    private String className;
    private String method;
    private int    version;
    private String nameSpace;
    private String nResultingClusters; // How many clusters were created
    private String similarityInstanceId; // The id for the similarity instance 
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
     * Get nResultingClusters.
     *
     * @return nResultingClusters as String.
     */
    public String getNResultingClusters()
    {
        return nResultingClusters;
    }
    
    /**
     * Set nResultingClusters.
     *
     * @param nResultingClusters the value to set.
     */
    public void setNResultingClusters(String nResultingClusters)
    {
        this.nResultingClusters = nResultingClusters;
    }
    
    /**
     * Get similarityInstanceId.
     *
     * @return similarityInstanceId as String.
     */
    public String getSimilarityInstanceId()
    {
        return similarityInstanceId;
    }
    
    /**
     * Set similarityInstanceId.
     *
     * @param similarityInstanceId the value to set.
     */
    public void setSimilarityInstanceId(String similarityInstanceId)
    {
        this.similarityInstanceId = similarityInstanceId;
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
}

