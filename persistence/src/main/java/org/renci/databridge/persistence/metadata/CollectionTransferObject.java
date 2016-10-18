package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class CollectionTransferObject {
    private String URL;
    private String title;
    private String description; // Free text metadata about the study
    private String producer;    // Producer of the collection
    private String subject;     // Subject of the study
    private ArrayList<String>   keywords;  // The keywords
    private HashMap<String, String> extra;
    private int insertTime;     // Seconds since the epoch

    // These attributes are specific to the DataBridge
    private String nameSpace;
    private int    version;
    private String dataStoreId; // The id generated at insertion time.

    
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
     * @param title the value to set.
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
     * Get keywords.
     *
     * @return keywords as ArrayList<String>.
     */
    public ArrayList<String> getKeywords()
    {
        return keywords;
    }
    
    /**
     * Set keywords.
     *
     * @param keywords the value to set.
     */
    public void setKeywords(ArrayList<String> keywords)
    {
        this.keywords = keywords;
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

    @Override
    public String toString () 
    {
        return "{" + getClass ().getName () + ": title: " + getTitle () + ", nameSpace: " + getNameSpace () + ", version: " + getVersion () + ", description: " + getDescription () + ", producer: " + getProducer () + ", subject: " + getSubject () + ", keywords: " + getKeywords () + ", URL: " + getURL () + ", extra: " + getExtra () + ", dataStoreId: " + getDataStoreId () + "}";
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
     * This function returns the set of strings associated with the fieldName given. The
     * interesting part is that the fieldName can be either one of the primary field names
     * like URL, title or description or it can be one of the keys in the "extra" data.  This
     * allows the user to not know where the data they are asking for is stored.
     *
     * @return keywords as ArrayList<String>.
     */
    public String getDataByFieldName(String fieldName)
    {
        String theData = null;
  
        switch (fieldName) {
           // First we check to see if the user has requested one of the fields of this object.
           case "URL" :
              theData = this.URL;
              break;

           case "title" :
              theData = this.title;
              break;

           case "description" :
              theData = this.description;
              break;

           case "producer" :
              theData = this.producer;
              break;

           case "subject" :
              theData = this.subject;
              break;

           case "keywords" :
              theData = String.join(" ", this.keywords);
              break;

           default:
              // If we didn't find it in the defined fields of the objects, maybe it's in the extra field
              if (extra.containsKey(fieldName)) {
                 theData = this.extra.get(fieldName);
              }
        }
        return theData;
    }
}

