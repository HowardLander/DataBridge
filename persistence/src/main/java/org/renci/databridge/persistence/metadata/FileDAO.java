package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface FileDAO {
    public boolean insertFile(FileTransferObject theFile);
    public boolean insertFileForCollection(CollectionTransferObject theCollection, FileTransferObject theFile);
    public Iterator<FileTransferObject> getFile(HashMap<String, String> searchMap);
    public Iterator<FileTransferObject> getFileForCollection(CollectionTransferObject theCollection);
    public boolean updateFile(FileTransferObject theFile, 
                                    Object fileID);
    public int deleteFileById(String id);
    public int deleteFile(HashMap<String, String> searchMap);
}

