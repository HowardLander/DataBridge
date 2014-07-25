package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface FileDAO {
    public Object insertFile(FileTransferObject theFile);
    public FileTransferObject getFile(Object fileID);
    public boolean updateFile(FileTransferObject theFile, 
                                    Object fileID);
    public boolean deleteFile(Object fileID);
}

