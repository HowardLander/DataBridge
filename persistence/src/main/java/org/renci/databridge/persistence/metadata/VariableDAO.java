package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface VariableDAO {
    public boolean insertVariable(VariableTransferObject theVariable);
    public boolean insertVariable(FileTransferObject theFile, VariableTransferObject theVariable);
    public Iterator<VariableTransferObject> getVariables(HashMap<String, String> searchMap);
    public Iterator<VariableTransferObject> getVariables(FileTransferObject theFile);
    public boolean updateVariable(VariableTransferObject theVariable, Object variableID);
    public int deleteVariable(VariableTransferObject theVar);
    public int deleteVariable(HashMap<String, String> searchMap);
}

