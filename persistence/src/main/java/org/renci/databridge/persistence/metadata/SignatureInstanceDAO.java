package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface SignatureInstanceDAO {
    /**
     *  Insert the specified signature instance into the database
     *  @param theSignatureInstance The SignatureInstance to insert.
     *  @return true on success, false or failure
     */
    public boolean insertSignatureInstance(SignatureInstanceTransferObject theSignatureInstance);
    /**
      *  Retrieve an iterator for all the signature instances in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @return the requested iterator
      */
    public Iterator<SignatureInstanceTransferObject> getSignatureInstances(HashMap<String, String> searchMap);
    /**
      *  Retrieve the SignatureInstanceTransferObject that matches the specified id
      *  @param id the id for the SignatureInstance to retrieve.
      *  @return the requested SignatureInstanceTransferObject or null
      */
    public SignatureInstanceTransferObject getSignatureInstanceById(String id);
    /**
      *  Retrieve an iterator for all the signature instances in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @param sortMap The HashMap containing the sorting criteria
      *  @param limit The max number of objects to return
      *  @return the requested iterator
      */
    public Iterator<SignatureInstanceTransferObject> getSignatureInstances(HashMap<String, String> searchMap,
       HashMap<String, String> sortMap, Integer limit);
    /**
     *  Delete the signature instance specified in the SignatureInstanceTransferObject.
     *  @param theSignatureInstance The SignatureInstanceTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteSignatureInstance(SignatureInstanceTransferObject theSignatureInstance);
    /**
     *  Delete the signature instance selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of objects deleted.
     */
    public int deleteSignatureInstance(HashMap<String, String> searchMap);
    /**
     *  Count the signature instances selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of signature instances matching the criteria.
     */
    public long countSignatureInstances(HashMap<String, String> searchMap);

    public static final String SORT_ASCENDING = "ascending";
    public static final String SORT_DESCENDING = "descending";
}

