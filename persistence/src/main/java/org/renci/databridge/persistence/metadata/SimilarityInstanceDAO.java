package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface SimilarityInstanceDAO {
    /**
     *  Insert the specified similarity instance into the database
     *  @param theSimilarityInstance The SimilarityInstance to insert.
     *  @return true on success, false or failure
     */
    public boolean insertSimilarityInstance(SimilarityInstanceTransferObject theSimilarityInstance);
    /**
      *  Retrieve an iterator for all the similarity instances in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @return the requested iterator
      */
    public Iterator<SimilarityInstanceTransferObject> getSimilarityInstances(HashMap<String, String> searchMap);
    /**
      *  Retrieve the SimilarityInstanceTransferObject that matches the specified id
      *  @param id the id for the SimilarityInstance to retrieve.
      *  @return the requested SimilarityInstanceTransferObject or null
      */
    public SimilarityInstanceTransferObject getSimilarityInstanceById(String id);
    /**
      *  Retrieve an iterator for all the similarity instances in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @param sortMap The HashMap containing the sorting criteria
      *  @param limit The max number of objects to return
      *  @return the requested iterator
      */
    public Iterator<SimilarityInstanceTransferObject> getSimilarityInstances(HashMap<String, String> searchMap,
       HashMap<String, String> sortMap, Integer limit);
    /**
     *  Delete the similarity instance specified in the SimilarityInstanceTransferObject.
     *  @param theSimilarityInstance The SimilarityInstanceTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteSimilarityInstance(SimilarityInstanceTransferObject theSimilarityInstance);
    /**
     *  Delete the similarity instance selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of objects deleted.
     */
    public int deleteSimilarityInstance(HashMap<String, String> searchMap);
    /**
     *  Count the similarity instances selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of similarity instances matching the criteria.
     */
    public long countSimilarityInstances(HashMap<String, String> searchMap);

    public static final String SORT_ASCENDING = "ascending";
    public static final String SORT_DESCENDING = "descending";
}

