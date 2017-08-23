package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface LaneDAO {
    /**
     *  Insert the specified lane into the database
     *  @param theLane The LaneTransferObject to insert.
     *  @return true on success, false or failure
     */
    public boolean insertLane(LaneTransferObject theLane);
    /**
      *  Retrieve an iterator for all the lanes in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @return the requested iterator
      */
    public Iterator<LaneTransferObject> getLanes(HashMap<String, String> searchMap);
    /**
      *  Retrieve the LaneTransferObject that matches the specified id
      *  @param id the id for the Lane to retrieve.
      *  @return the requested LaneTransferObject or null
      */
    public LaneTransferObject getLaneById(String id);
    /**
      *  Update the lane specified by the id with the data in the LaneTransferObject
      *  @param theLane The data to use for the update.
      *  @param id the id of the Collection to update.
      *  @return boolean for success or failure.
      */
    public boolean updateLane(LaneTransferObject theLane, Object laneID);
    /**
     *  Delete the lane specified in the LaneTransferObject.
     *  @param theLane The LaneTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteLane(LaneTransferObject theLane);
    /**
     *  Delete the lane selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of objects deleted.
     */
    public int deleteLane(HashMap<String, String> searchMap);
    /**
     *  Count the lanes selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of lanes matching the criteria.
     */
    public long countLanes(HashMap<String, String> searchMap);
}

