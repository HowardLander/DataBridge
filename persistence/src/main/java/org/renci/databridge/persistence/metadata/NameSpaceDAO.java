package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface NameSpaceDAO {
    /**
     *  Insert the specified nameSpace into the nameSpace table.
     *  @param theNameSpace The NameSpaceTransferObject to install.
     *  @return true on success, false or failure
     */
    public boolean insertNameSpace(NameSpaceTransferObject theNameSpace);
    /**
     *  Retrieve an iterator for all the nameSpaces
     *  @return the requested iterator
     */
    public Iterator<NameSpaceTransferObject> getNameSpaces();
    /**
     *  Delete the given nameSpace
     *  @param nameSpace The nameSpace to delete.
     *  @return the number of objects deleted.
     */
    public int deleteNameSpace(NameSpaceTransferObject nameSpace);
}

