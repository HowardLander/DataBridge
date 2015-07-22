package org.renci.databridge.persistence.metadata;
import  java.util.*;

public interface FileDAO {
    /**
     *  Insert the specified file into the database
     *  @param theFile The FileTransferObject to insert.
     *  @return true on success, false or failure
     */
    public boolean insertFile(FileTransferObject theFile);
    /**
     *  Insert the specified file and attach it to the specified collection.
     *  @param theCollection The CollectionTransferObject to which to attach the file.
     *  @param theFile The FileTransferObject to insert.
     *  @return true on success, false or failure
     */
    public boolean insertFile(CollectionTransferObject theCollection, FileTransferObject theFile);
    /**
      *  Retrieve an iterator for all the files matching the search map.
      *  @param searchMap The HashMap containing the search criteria.
      *  @return the requested iterator
      */
    public Iterator<FileTransferObject> getFiles(HashMap<String, String> searchMap);
    /**
      *  Retrieve an iterator for all the files attached to the specified collection.
      *  @param theCollection the collection for which to retrieve the files.
      *  @return the requested iterator
      */
    public Iterator<FileTransferObject> getFiles(CollectionTransferObject theCollection);
    /**
      *  Update the file specified by the id with the data in the FileTransferObject
      *  @param theFile The data to use for the update.
      *  @param id the id of the File to update.
      *  @return boolean for success or failure.
      */
    public boolean updateFile(FileTransferObject theFile, Object fileID);
    /**
     *  Delete the file specified in the FileTransferObject.
     *  @param theFile The FileTransferObject to delete.
     *  @return the number of objects deleted.
     */
    public int deleteFile(FileTransferObject theFile);
    /**
     *  Delete the file selected by the search map.
     *  @param searchMap The HashMap containing the search criteria.
     *  @return the number of objects deleted.
     */
    public int deleteFile(HashMap<String, String> searchMap);
}

