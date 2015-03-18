package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface ActionDAO {
    public boolean insertAction(ActionTransferObject theAction);
    public Iterator<ActionTransferObject> getActions(String currentMessage, String nameSpace);
    public int deleteAction(ActionTransferObject theAction);
    public int deleteAction(String currentMessage, String nameSpace);
}

