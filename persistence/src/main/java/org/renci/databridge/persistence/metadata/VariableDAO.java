package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface VariableDAO {
    /**
     *  Insert the specified variable into the database
     *  @param theVariable The VariableTransferObject to insert.
     *  @return true on success, false or failure
     */
    public boolean insertVariable(VariableTransferObject theVariable);
    /**
     *  Insert the specified variable and attach it to the specified file.
     *  @param theFile The FileTransferObject to which to attach the file.
     *  @param theVariable The VariableTransferObject to attach
     *  @return true on success, false or failure
     */
    public boolean insertVariable(FileTransferObject theFile, VariableTransferObject theVariable);
    /**
      *  Retrieve an iterator for all the variables matching the criteria in the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @return the requested iterator
      */
    public Iterator<VariableTransferObject> getVariables(HashMap<String, String> searchMap);
    /**
      *  Retrieve an iterator for all the variables attached to the specified file.
      *  @param theFile the file for which to retrieve the variables.
      *  @return the requested iterator
      */
    public Iterator<VariableTransferObject> getVariables(FileTransferObject theFile);
    /**
      *  Update the variable specified by the id with the data in the VariableTransferObject
      *  @param theVariable The data to use for the update.
      *  @param variableId the id of the Collection to update.
      *  @return boolean for success or failure.
      */
    public boolean updateVariable(VariableTransferObject theVariable, Object variableID);
    /**
     *  Delete the variable specified in the VariableTransferObject.
     *  @param theVariable The VariableTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteVariable(VariableTransferObject theVar);
    /**
     *  Delete the variable selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of objects deleted.
     */
    public int deleteVariable(HashMap<String, String> searchMap);
}

