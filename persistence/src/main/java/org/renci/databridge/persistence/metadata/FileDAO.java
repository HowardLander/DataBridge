package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface FileDAO {
    public boolean insertFile(FileTransferObject theFile);
    public boolean insertFile(CollectionTransferObject theCollection, FileTransferObject theFile);
    public Iterator<FileTransferObject> getFiles(HashMap<String, String> searchMap);
    public Iterator<FileTransferObject> getFiles(CollectionTransferObject theCollection);
    public boolean updateFile(FileTransferObject theFile, Object fileID);
    public int deleteFile(FileTransferObject theFile);
    public int deleteFile(HashMap<String, String> searchMap);
}

