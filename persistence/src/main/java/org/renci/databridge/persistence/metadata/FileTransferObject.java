package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class FileTransferObject {
    private String URL;
    private String name;
    private String description; // Free text metadata about the study
    private Map<String, String> additionalMetadata;

    // These attributes are specific to the DataBridge
    private int    version;
    
    
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
}

