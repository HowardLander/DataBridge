package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface SNAInstanceDAO {
    public boolean insertSNAInstance(SNAInstanceTransferObject theSNAInstance);
    public Iterator<SNAInstanceTransferObject> getSNAInstances(HashMap<String, String> searchMap);
    public int deleteSNAInstance(SNAInstanceTransferObject theSimilarityInstance);
    public int deleteSNAInstance(HashMap<String, String> searchMap);
}

