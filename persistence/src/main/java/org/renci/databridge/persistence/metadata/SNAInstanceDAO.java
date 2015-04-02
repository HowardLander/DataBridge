package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface SNAInstanceDAO {
    public boolean insertSNAInstance(SNAInstanceTransferObject theSNAInstance);
    public boolean updateSNAInstance(HashMap<String, String> searchMap, HashMap<String, String> updateMap);
    public boolean updateSNAInstance(SNAInstanceTransferObject theSNAInstance, 
                                     HashMap<String, String> updateMap);
    public Iterator<SNAInstanceTransferObject> getSNAInstances(HashMap<String, String> searchMap);
    public Iterator<SNAInstanceTransferObject> getSNAInstances(HashMap<String, String> searchMap,
                                                               HashMap<String, String> sortMap, Integer limit);
    public SNAInstanceTransferObject getSNAInstanceById(String id);
    public int deleteSNAInstance(SNAInstanceTransferObject theSimilarityInstance);
    public int deleteSNAInstance(HashMap<String, String> searchMap);
    public static final String SORT_ASCENDING = "ascending";
    public static final String SORT_DESCENDING = "descending";
}



