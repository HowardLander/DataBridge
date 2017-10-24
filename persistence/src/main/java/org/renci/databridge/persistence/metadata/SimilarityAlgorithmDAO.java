package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface SimilarityAlgorithmDAO {
    /**
     *  Insert the specified similarity algorithm into the database
     *  @param theSimilarityAlgorithm The SimilarityAlgorithm to insert.
     *  @return true on success, false or failure
     */
    public boolean insertSimilarityAlgorithm(SimilarityAlgorithmTransferObject theSimilarityAlgorithm);
    /**
      *  Retrieve an iterator for all the similarity algorithms in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @return the requested iterator
      */
    public Iterator<SimilarityAlgorithmTransferObject> getSimilarityAlgorithms(HashMap<String, String> searchMap);
    /**
      *  Retrieve the SimilarityAlgorithmTransferObject that matches the specified id
      *  @param id the id for the SimilarityAlgorithm to retrieve.
      *  @return the requested SimilarityAlgorithmTransferObject or null
      */
    public SimilarityAlgorithmTransferObject getSimilarityAlgorithmById(String id);
    /**
      *  Retrieve an iterator for all the similarity algorithms in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @param sortMap The HashMap containing the sorting criteria
      *  @param limit The max number of objects to return
      *  @return the requested iterator
      */
    public Iterator<SimilarityAlgorithmTransferObject> getSimilarityAlgorithms(HashMap<String, String> searchMap,
       HashMap<String, String> sortMap, Integer limit);
    /**
     *  Delete the similarity algorithm specified in the SimilarityAlgorithmTransferObject.
     *  @param theSimilarityAlgorithm The SimilarityAlgorithmTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteSimilarityAlgorithm(SimilarityAlgorithmTransferObject theSimilarityAlgorithm);
    /**
     *  Delete the similarity algorithm selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of objects deleted.
     */
    public int deleteSimilarityAlgorithm(HashMap<String, String> searchMap);
    /**
     *  Count the similarity algorithms selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of similarity algorithms matching the criteria.
     */
    public long countSimilarityAlgorithms(HashMap<String, String> searchMap);

    public static final String SORT_ASCENDING = "ascending";
    public static final String SORT_DESCENDING = "descending";
}

