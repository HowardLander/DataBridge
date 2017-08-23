package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class LaneTransferObject {
    private ArrayList<String> nameSpaces;    // the name spaces associated with this lane
    private String name;                     // A user provided name for the lane.
    private String ingestImpl;               // the ingest implementation
    private String ingestParams;             // the ingest parameters
    private String signatureImpl;            // the signature implementation
    private String signatureParams;          // the signature parameters
    private String similarityImpl;           // the similarity implementation
    private String similarityParams;         // the similarity parameters
    private String SNAImpl;                  // the SNA implementation
    private String SNAParams;                // the SNA parameters

    // At some point, we will probably want some access controls by nameSpace.  I'm not yet sure what
    // the best way to do this is, so we'll leave it out now.

    // These attributes are specific to the DataBridge
    private String dataStoreId;              // The id generated at insertion time.
    private String creatorId;                // The id that created the Lane
    private int insertTime;                  // Seconds since the epoch
    
    @Override
    public String toString () 
    {
        return "{" + getClass ().getName() + ": nameSpace: " + getNameSpaces() + 
                ", ingestImpl: " + getIngestImpl()  + 
                ", ingestParams: " + getIngestParams()  + 
                ", signatureImpl: " + getSignatureImpl()  + 
                ", signatureParams: " + getSignatureParams()  + 
                ", similarityImpl: " + getSimilarityImpl()  + 
                ", similarityParams: " + getSimilarityParams()  + 
                ", SNAImpl: " + getSNAImpl()  + 
                ", SNAParams: " + getSNAParams()  + 
                ", creatorId: " + getCreatorId()  + 
                ", dataStoreId: " + getDataStoreId () + "}";
    }
    
    
    /**
     * Get nameSpaces.
     *
     * @return nameSpaces as ArrayList<String>.
     */
    public ArrayList<String> getNameSpaces()
    {
        return nameSpaces;
    }

    /**
     * Set nameSpaces.
     *
     * @param nameSpaces the value to set.
     */
    public void setNameSpaces(ArrayList<String> nameSpaces)
    {
        this.nameSpaces = nameSpaces;
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
     * Get ingestImpl.
     *
     * @return ingestImpl as String.
     */
    public String getIngestImpl()
    {
        return ingestImpl;
    }
    
    /**
     * Set ingestImpl.
     *
     * @param ingestImpl the value to set.
     */
    public void setIngestImpl(String ingestImpl)
    {
        this.ingestImpl = ingestImpl;
    }
    
    /**
     * Get ingestParams.
     *
     * @return ingestParams as String.
     */
    public String getIngestParams()
    {
        return ingestParams;
    }
    
    /**
     * Set ingestParams.
     *
     * @param ingestParams the value to set.
     */
    public void setIngestParams(String ingestParams)
    {
        this.ingestParams = ingestParams;
    }
    
    /**
     * Get signatureImpl.
     *
     * @return signatureImpl as String.
     */
    public String getSignatureImpl()
    {
        return signatureImpl;
    }
    
    /**
     * Set signatureImpl.
     *
     * @param signatureImpl the value to set.
     */
    public void setSignatureImpl(String signatureImpl)
    {
        this.signatureImpl = signatureImpl;
    }
    
    /**
     * Get signatureParams.
     *
     * @return signatureParams as String.
     */
    public String getSignatureParams()
    {
        return signatureParams;
    }
    
    /**
     * Set signatureParams.
     *
     * @param signatureParams the value to set.
     */
    public void setSignatureParams(String signatureParams)
    {
        this.signatureParams = signatureParams;
    }
    
    /**
     * Get similarityImpl.
     *
     * @return similarityImpl as String.
     */
    public String getSimilarityImpl()
    {
        return similarityImpl;
    }
    
    /**
     * Set similarityImpl.
     *
     * @param similarityImpl the value to set.
     */
    public void setSimilarityImpl(String similarityImpl)
    {
        this.similarityImpl = similarityImpl;
    }
    
    /**
     * Get similarityParams.
     *
     * @return similarityParams as String.
     */
    public String getSimilarityParams()
    {
        return similarityParams;
    }
    
    /**
     * Set similarityParams.
     *
     * @param similarityParams the value to set.
     */
    public void setSimilarityParams(String similarityParams)
    {
        this.similarityParams = similarityParams;
    }
    
    /**
     * Get SNAImpl.
     *
     * @return SNAImpl as String.
     */
    public String getSNAImpl()
    {
        return SNAImpl;
    }
    
    /**
     * Set SNAImpl.
     *
     * @param SNAImpl the value to set.
     */
    public void setSNAImpl(String SNAImpl)
    {
        this.SNAImpl = SNAImpl;
    }
    
    /**
     * Get SNAParams.
     *
     * @return SNAParams as String.
     */
    public String getSNAParams()
    {
        return SNAParams;
    }
    
    /**
     * Set SNAParams.
     *
     * @param SNAParams the value to set.
     */
    public void setSNAParams(String SNAParams)
    {
        this.SNAParams = SNAParams;
    }
    
    /**
     * Get creatorId.
     *
     * @return creatorId as String.
     */
    public String getCreatorId()
    {
        return creatorId;
    }
    
    /**
     * Set creatorId.
     *
     * @param creatorId the value to set.
     */
    public void setCreatorId(String creatorId)
    {
        this.creatorId = creatorId;
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

