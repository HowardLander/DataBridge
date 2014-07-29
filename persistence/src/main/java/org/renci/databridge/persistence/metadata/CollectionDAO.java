package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface CollectionDAO {
    public boolean insertCollection(CollectionTransferObject theCollection);
    public CollectionTransferObject getCollection(HashMap<String, String> searchMap);
    public boolean updateCollection(CollectionTransferObject theCollection, 
                                    Object collectionID);
    public int deleteCollectionById(String id);
    public int deleteCollection(HashMap<String, String> searchMap);
}

