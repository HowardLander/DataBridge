package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface SimilarityInstanceDAO {
    public boolean insertSimilarityInstance(SimilarityInstanceTransferObject theSimilarityInstance);
    public Iterator<SimilarityInstanceTransferObject> getSimilarityInstances(HashMap<String, String> searchMap);
    public int deleteSimilarityInstance(SimilarityInstanceTransferObject theSimilarityInstance);
    public int deleteSimilarityInstance(HashMap<String, String> searchMap);
    public long countSimilarityInstances(HashMap<String, String> searchMap);
}

