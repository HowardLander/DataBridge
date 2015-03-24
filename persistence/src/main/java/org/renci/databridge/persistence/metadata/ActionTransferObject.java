package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class ActionTransferObject {
    private String currentMessage; // The message currently being executed.
    private String nameSpace;      // The name space on which the current message is executing
    private HashMap<String, String> headers; // Headers for the action message
    private int insertTime;     // Seconds since the epoch
    private String dataStoreId; // The id generated at insertion time.

    @Override
    public String toString ()
    {
        return "{" + getClass ().getName () + ": currentMessage: " + getCurrentMessage () + ", nameSpace: " + getNameSpace () + ", headers: " + getHeaders () + ", dataStoreId: " + getDataStoreId () + ", insertTime: " + getInsertTime() + "}";
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
     * Get currentMessage.
     *
     * @return currentMessage as String.
     */
    public String getCurrentMessage()
    {
        return currentMessage;
    }
    
    /**
     * Set currentMessage.
     *
     * @param currentMessage the value to set.
     */
    public void setCurrentMessage(String currentMessage)
    {
        this.currentMessage = currentMessage;
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
     * Get headers.
     *
     * @return headers as HashMap<String, String>
     */
    public HashMap<String, String> getHeaders()
    {
        return headers;
    }

    /**
     * Set headers.
     *
     * @param headers the value to set.
     */
    public void setHeaders(HashMap<String, String> headers)
    {
        this.headers = headers;
    }
}

