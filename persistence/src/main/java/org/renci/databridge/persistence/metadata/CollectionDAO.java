package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface CollectionDAO {
    /**
     *  Insert the specified collection into the action table.
     *  @param theCollection The CollectionTransferObject to insert.
     *  @return true on success, false or failure
     */
    public boolean insertCollection(CollectionTransferObject theCollection);
    /**
      *  Retrieve an iterator for all the collections in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @return the requested iterator
      */
    public Iterator<CollectionTransferObject> getCollections(HashMap<String, String> searchMap);
    /**
      *  Retrieve the CollectionTransferObject that matches the specified id
      *  @param id the id for the Collection to retrieve.
      *  @return the requested CollectionTransferObject or null
      */
    public CollectionTransferObject getCollectionById(String id);
    /**
      *  Retrieve an iterator for all the collection nameSpaces.
      *  @return the requested iterator
      */
    public Iterator<String> getNamespaceList();
    /**
      *  Update the collection specified by the id with the data in the CollectionTransferObject
      *  @param theCollection The data to use for the update.
      *  @param id the id of the Collection to update.
      *  @return boolean for success or failure.
      */
    public boolean updateCollection(CollectionTransferObject theCollection, Object collectionID);
    /**
     *  Delete the collection specified in the CollectionTransferObject.
     *  @param theCollection The CollectionTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteCollection(CollectionTransferObject theCollection);
    /**
     *  Delete the collection selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of objects deleted.
     */
    public int deleteCollection(HashMap<String, String> searchMap);
    /**
     *  Count the collections selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of collections matching the criteria.
     */
    public long countCollections(HashMap<String, String> searchMap);
}

