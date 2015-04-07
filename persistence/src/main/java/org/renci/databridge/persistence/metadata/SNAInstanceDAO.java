package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface SNAInstanceDAO {
    /**
     *  Insert the specified SNA instance into the database
     *  @param theSNAInstance The SNA Instance to insert.
     *  @return true on success, false or failure
     */
    public boolean insertSNAInstance(SNAInstanceTransferObject theSNAInstance);
    /**
      *  Update the sna instance specified by the search map with the data in the update map
      *  @param searchMap The HashMap containing the search criteria
      *  @param updateMap The HashMap containing the update info
      *  @return boolean for success or failure.
      */
    public boolean updateSNAInstance(HashMap<String, String> searchMap, HashMap<String, String> updateMap);
    /**
      *  Update the sna instance specified by the transfer object with the data in the update map
      *  @param theSNAInstance The transfer object containing the search criteria
      *  @param updateMap The HashMap containing the update info
      *  @return boolean for success or failure.
      */
    public boolean updateSNAInstance(SNAInstanceTransferObject theSNAInstance, 
                                     HashMap<String, String> updateMap);
    /**
      *  Retrieve an iterator for all the SNA instances in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @return the requested iterator
      */
    public Iterator<SNAInstanceTransferObject> getSNAInstances(HashMap<String, String> searchMap);
    /**
      *  Retrieve an iterator for all the SNA instances in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @param sortMap The HashMap containing the sorting criteria
      *  @param limit The max number of objects to return
      *  @return the requested iterator
      */
    public Iterator<SNAInstanceTransferObject> getSNAInstances(HashMap<String, String> searchMap,
                                                               HashMap<String, String> sortMap, Integer limit);
    /**
      *  Retrieve the SNAInstanceTransferObject that matches the specified id
      *  @param id the id for the SNA Instance to retrieve.
      *  @return the requested SNAInstanceTransferObject or null
      */
    public SNAInstanceTransferObject getSNAInstanceById(String id);
    /**
     *  Delete the SNA instance specified in the SimilarityInstanceTransferObject.
     *  @param theSNAInstance The SNAInstanceTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteSNAInstance(SNAInstanceTransferObject theSNAInstance);
    /**
     *  Delete the sna instance selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of objects deleted.
     */
    public int deleteSNAInstance(HashMap<String, String> searchMap);
    public static final String SORT_ASCENDING = "ascending";
    public static final String SORT_DESCENDING = "descending";
}



