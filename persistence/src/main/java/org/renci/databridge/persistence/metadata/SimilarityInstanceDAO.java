package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface SimilarityInstanceDAO {
    public boolean insertSimilarityInstance(SimilarityInstanceTransferObject theSimilarityInstance);
    public Iterator<SimilarityInstanceTransferObject> getSimilarityInstances(HashMap<String, String> searchMap);
    public SimilarityInstanceTransferObject getSimilarityInstanceById(String id);
    public Iterator<SimilarityInstanceTransferObject> getSimilarityInstances(HashMap<String, String> searchMap,
       HashMap<String, String> sortMap, Integer limit);
    public int deleteSimilarityInstance(SimilarityInstanceTransferObject theSimilarityInstance);
    public int deleteSimilarityInstance(HashMap<String, String> searchMap);
    public long countSimilarityInstances(HashMap<String, String> searchMap);

    public static final String SORT_ASCENDING = "ascending";
    public static final String SORT_DESCENDING = "descending";
}

