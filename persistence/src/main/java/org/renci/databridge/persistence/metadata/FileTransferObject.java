package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class FileTransferObject {
    private String URL;
    private String name;
    private String description; // Free text metadata about the study
    private HashMap<String, String> extra;

    // These attributes are specific to the DataBridge
    private String nameSpace;
    private int    version;
    private String dataStoreId; // The id generated at insertion time.
    private String collectionDataStoreId; // The id for the "parent" collection
    
    
    /**
     * Get URL.
     *
     * @return URL as String.
     */
    public String getURL()
    {
        return URL;
    }
    
    /**
     * Set URL.
     *
     * @param URL the value to set.
     */
    public void setURL(String URL)
    {
        this.URL = URL;
    }
    
    /**
     * Get name.
     *
     * @return name as String.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Set name.
     *
     * @param name the value to set.
     */
    public void setName(String name)
    {
        this.name = name;
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
    
    /**
     * Get collectionDataStoreId.
     *
     * @return collectionDataStoreId as String.
     */
    public String getCollectionDataStoreId()
    {
        return collectionDataStoreId;
    }
    
    /**
     * Set collectionDataStoreId.
     *
     * @param collectionDataStoreId the value to set.
     */
    public void setCollectionDataStoreId(String collectionDataStoreId)
    {
        this.collectionDataStoreId = collectionDataStoreId;
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
     * Get extra.
     *
     * @return extra as HashMap<String, String>
     */
    public HashMap<String, String> getExtra()
    {
        return extra;
    }

    /**
     * Set extra.
     *
     * @param extra the value to set.
     */
    public void setExtra(HashMap<String, String> extra)
    {
        this.extra = extra;
    }
}

