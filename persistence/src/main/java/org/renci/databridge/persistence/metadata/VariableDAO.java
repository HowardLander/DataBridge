package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface VariableDAO {
    public Object insertVariable(VariableTransferObject theVariable);
    public VariableTransferObject getVariable(Object variableID);
    public boolean updateVariable(VariableTransferObject theVariable, 
                                    Object variableID);
    public boolean deleteVariable(Object variableID);
}

