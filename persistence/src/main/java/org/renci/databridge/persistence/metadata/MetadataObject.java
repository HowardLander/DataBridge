package org.renci.databridge.persistence.metadata;
import  java.util.*;

public class MetadataObject {

    private CollectionTransferObject collectionTransferObject;
    private List<FileTransferObject> fileTransferObjects;
    private List<VariableTransferObject> variableTransferObjects;

    /**
     * Get collectionTransferObject.
     *
     * @return collectionTransferObject.
     */
    public CollectionTransferObject getCollectionTransferObject()
    {
        return collectionTransferObject;
    }
    
    /**
     * Set collectionTransferObject.
     *
     * @param collectionTransferObject the value to set.
     */
    public void setCollectionTransferObject(CollectionTransferObject collectionTransferObject)
    {
        this.collectionTransferObject = collectionTransferObject;
    }
    
    /**
     * Get fileTransferObjects.
     *
     * @return fileTransferObjects.
     */
    public List<FileTransferObject> getFileTransferObjects()
    {
        return fileTransferObjects;
    }
    
    /**
     * Set fileTransferObjects.
     *
     * @param fileTransferObjects the value to set.
     */
    public void setFileTransferObjects (List<FileTransferObject> fileTransferObjects)
    {
        this.fileTransferObjects = fileTransferObjects;
    }
    
    /**
     * Get variableTransferObjects.
     *
     * @return variableTransferObjects.
     */
    public List<VariableTransferObject> getVariableTransferObjects()
    {
        return variableTransferObjects;
    }
    
    /**
     * Set variableTransferObjects.
     *
     * @param variableTransferObjects the value to set.
     */
    public void setVariableTransferObjects(List<VariableTransferObject> variableTransferObjects)
    {
        this.variableTransferObjects = variableTransferObjects;
    }
    
}

