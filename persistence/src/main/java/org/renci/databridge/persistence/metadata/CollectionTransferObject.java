package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class CollectionTransferObject {
    private String URL;
    private String title;
    private String description; // Free text metadata about the study
    private String producer;    // Producer of the collection
    private String subject;     // Subject of the study
    private Map<String, String> additionalMetadata;

    // These attributes are specific to the DataBridge
    private String nameSpace;
    private int    version;

    
    /**
     * Get name.
     *
     * @return name as String.
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * Set name.
     *
     * @param name the value to set.
     */
    public void setTitle(String title)
    {
        this.title = title;
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
     * Get producer.
     *
     * @return producer as String.
     */
    public String getProducer()
    {
        return producer;
    }
    
    /**
     * Set producer.
     *
     * @param producer the value to set.
     */
    public void setProducer(String producer)
    {
        this.producer = producer;
    }
    
    /**
     * Get subject.
     *
     * @return subject as String.
     */
    public String getSubject()
    {
        return subject;
    }
    
    /**
     * Set subject.
     *
     * @param subject the value to set.
     */
    public void setSubject(String subject)
    {
        this.subject = subject;
    }
    
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
}

