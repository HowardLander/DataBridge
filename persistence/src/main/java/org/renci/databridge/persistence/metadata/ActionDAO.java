package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface ActionDAO {
    /**
     *  Insert the specified action into the action table.
     *  @param theAction The ActionTransferObject to install.
     *  @return true on success, false or failure
     */
    public boolean insertAction(ActionTransferObject theAction);
    /**
     *  Retrieve an iterator for all the actions that match the given message and nameSpace.
     *  @param currentMessage The message to search for
     *  @param nameSpace The nameSpace to search for
     *  @return the requested iterator
     */
    public Iterator<ActionTransferObject> getActions(String currentMessage, String nameSpace);
    /**
     *  Retrieve an iterator for all the actions that match the given message and nameSpace.
     *  @param currentMessage The message to search for
     *  @return the requested iterator
     */
    public Iterator<ActionTransferObject> getActions(String currentMessage);
    /**
     *  Delete the action specified in the ActionTransferObject.
     *  @param theAction The ActionTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteAction(ActionTransferObject theAction);
    /**
     *  Delete all the actions that match the given message and nameSpace.
     *  @param currentMessage The message to delete.
     *  @param nameSpace The nameSpace to delete.
     *  @return the number of objects deleted.
     */
    public int deleteAction(String currentMessage, String nameSpace);
}

