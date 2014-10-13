package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface CollectionDAO {
    public boolean insertCollection(CollectionTransferObject theCollection);
    public Iterator<CollectionTransferObject> getCollections(HashMap<String, String> searchMap);
    public boolean updateCollection(CollectionTransferObject theCollection, Object collectionID);
    public int deleteCollection(CollectionTransferObject theCollection);
    public int deleteCollection(HashMap<String, String> searchMap);
    public long countCollections(HashMap<String, String> searchMap);
}

