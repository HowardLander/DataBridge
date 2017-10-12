package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface IngestInstanceDAO {
    /**
     *  Insert the specified ingest instance into the database
     *  @param theIngestInstance The IngestInstance to insert.
     *  @return true on success, false or failure
     */
    public boolean insertIngestInstance(IngestInstanceTransferObject theIngestInstance);
    /**
      *  Retrieve an iterator for all the ingest instances in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @return the requested iterator
      */
    public Iterator<IngestInstanceTransferObject> getIngestInstances(HashMap<String, String> searchMap);
    /**
      *  Retrieve the IngestInstanceTransferObject that matches the specified id
      *  @param id the id for the IngestInstance to retrieve.
      *  @return the requested IngestInstanceTransferObject or null
      */
    public IngestInstanceTransferObject getIngestInstanceById(String id);
    /**
      *  Retrieve an iterator for all the ingest instances in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @param sortMap The HashMap containing the sorting criteria
      *  @param limit The max number of objects to return
      *  @return the requested iterator
      */
    public Iterator<IngestInstanceTransferObject> getIngestInstances(HashMap<String, String> searchMap,
       HashMap<String, String> sortMap, Integer limit);
    /**
     *  Delete the ingest instance specified in the IngestInstanceTransferObject.
     *  @param theIngestInstance The IngestInstanceTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteIngestInstance(IngestInstanceTransferObject theIngestInstance);
    /**
     *  Delete the ingest instance selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of objects deleted.
     */
    public int deleteIngestInstance(HashMap<String, String> searchMap);
    /**
     *  Count the ingest instances selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of ingest instances matching the criteria.
     */
    public long countIngestInstances(HashMap<String, String> searchMap);

    public static final String SORT_ASCENDING = "ascending";
    public static final String SORT_DESCENDING = "descending";
}

