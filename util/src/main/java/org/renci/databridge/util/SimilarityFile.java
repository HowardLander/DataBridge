package org.renci.databridge.util;
import org.renci.databridge.util.Dataset;
import org.renci.databridge.util.DatasetSerializer;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.*;
import java.util.*;
import org.la4j.*;


/**
 * This class holds the data for a single network in 
 * the DataBridge system.
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class SimilarityFile {

     /** A string to store the dbID for the network - this becomes the databridge identifier
	 for all edges in the network. This dbID is unique amongst edges sharing the same nodes. */
     private String nameSpace;

     /** A string to store the similarity instance for this file. This identifies the actual run
         of the similarity instance that produced this file. This is a reference to a record
         stored in the Metadata database and is accessed through the DAO for the SimilarityInstance */
     private String similarityInstanceId;

     /** The similarity matrix stored as a Compressed row format.Note that
         we only need the top half of the matrix though we are currently storing it all.
         Also note that the x and y dimensions of original matrix have to be equal */
     private org.la4j.matrix.sparse.CRSMatrix similarityMatrix;

     /** The list of collectionIds in the metadata database making up the nameSpace. Each of these will
         become a node in the network database. Note that we could get this out of the metadata database
         by doing a query in the code that loads the network, but by storing the list in the file we are
         guaranteeing that we have the list as it existed when the file was created.
       */
     private java.util.ArrayList<String> collectionIds;

     /**
      * SimilarityFile constructor with no parameter. Note that this does not allocate the matrix
      * since there is not a zero argument constructor for the RCDoubleMatrix2D type.
      * This constructor should only be used when reading a network from disk, else a dbID should
      * always be provided.
      */
     public SimilarityFile() {
        similarityMatrix = null;
     }

     /**
      * SimilarityFile constructor with a single parameter: the nameSpace for the network.
      * Note that this does not allocate the matrix
      * since there is not a zero argument constructor for the RCDoubleMatrix2D type.
      *
      */
     public SimilarityFile(String nameSpace) {
        similarityMatrix = null;
        this.nameSpace = nameSpace;
     }

     /**
      * SimilarityFile constructor which takes a two parameters: the size of each
      * dimension of the similarity matrix and the dbID for the edges that make 
      * up the network
      *
      * @param nameSpace The nameSpace for the network
      */
     public SimilarityFile(int arraySize, String nameSpace) {
        similarityMatrix = new org.la4j.matrix.sparse.CRSMatrix(arraySize, arraySize);
        this.nameSpace = nameSpace;
     }

     /**
      * SimilarityFile constructor which takes two parameters: a 2 dimensional
      * array of doubles containing the similarity matrix, and the nameSpace for 
      * the nodes that make up the network.
      *
      * @param values A 2 dimensional array of doubles.  Note that the dimensions of the array
      *                must be equal or an IllegalArgumentException is thrown.
      */
     public SimilarityFile(double[][] values, String nameSpace) throws IllegalArgumentException {
        similarityMatrix = org.la4j.matrix.sparse.CRSMatrix.from2DArray(values);

        /* Remember that this is a similarity matrix, meaning that, in the non-sparse, form
           each dataset is compared to every other data set.  That means the matrix has to be
           square.  If it isn't we throw an IllegalArgumentException.
         */ 
        if (similarityMatrix.rows() != similarityMatrix.columns()) {
            String exceptionString = 
               new String("number of rows (" + similarityMatrix.rows() + 
                           ") != number of columns (" + similarityMatrix.columns() + ")");
            throw new IllegalArgumentException(exceptionString);
        }
        this.nameSpace = nameSpace;
     }

     /**
      * This method populates this SimilarityFile object from the data stored in the
      * specified local file. The filePath must be accessible from where the code is called.
      *
      * @param filePath The file from which the network data should be read.
      *                
      */
     public void readFromDisk(String filePath) throws Exception {
         // read from the requested file
         try {
             FileInputStream fos = new FileInputStream(new File(filePath));
             BufferedInputStream theStream = new BufferedInputStream(fos);
             ObjectInputStream input = new ObjectInputStream(theStream);
             readTheSimilarityFromInputObject(input);

          } catch (Exception e) {
              throw e;
          }
     }

     /**
      * This method populates this SimilarityData object from the data stored in the
      * specified file URL . The file URL must be accessible from where the code is called.
      *
      * @param userURL The file URL from which the data should be read.
      *                
      */
     public void readFromURL(String userURL) throws Exception {
         // read from the requested file
         try {
             URL theURL = new URL(userURL);
             URLConnection theConnection = theURL.openConnection();
             BufferedInputStream theStream = 
                new BufferedInputStream(theConnection.getInputStream());
             ObjectInputStream input = new ObjectInputStream(theStream);
             readTheSimilarityFromInputObject(input);

          } catch (Exception e) {
              throw e;
          }
     }

     /**
      * This is the lower level method that actually does the work of reading
      * the network data from the input object and storing it in the SimilarityData
      * structure.  Because the input object just requires a stream, this same
      * function can read data from both a file and a URL.
      *
      * @param input the ObjectInputStream from which to read.
      *                
      */
     private void readTheSimilarityFromInputObject(ObjectInputStream input) throws Exception {

         byte[] bytes = null;
         ByteArrayOutputStream bos = null;
         ObjectOutputStream oos = null;

         try {
             int matrixSize = input.readInt();
             byte [] matrixBytes = new byte[matrixSize];
             input.readFully(matrixBytes, 0, matrixSize);
             this.similarityMatrix = org.la4j.matrix.sparse.CRSMatrix.fromBinary(matrixBytes);
             this.nameSpace = (String) input.readObject();
             this.similarityInstanceId = (String) input.readObject();
             this.collectionIds = (java.util.ArrayList<String>) input.readObject();
         } catch (Exception e) {
             throw e;
         }
     }
     
     
     /**
      * Set an i,j value in the similarity matrix.
      *
      * @param i The row for the entry
      * @param j The column for the entry
      * @param value The value for the entry
      */
     public void setSimilarityValue(int i, int j, double value) {
         this.similarityMatrix.set(i, j, value);
     }

     /**
      * Get collectionIds
      *
      * @return collectionIds as ArrayList<String>
      */
     public ArrayList<String> getCollectionIds()
     {
         return collectionIds;
     }
     
     /**
      * Set collectionIds
      *
      * @param collectionIds the value to set.
      */
     public void setCollectionIds(ArrayList<String> collectionIds) 
     {
         this.collectionIds = collectionIds;
     }

     
     /**
      * Get similarityInstanceId.
      *
      * @return similarityInstanceId as String.
      */
     public String getSimilarityInstanceId()
     {
         return similarityInstanceId;
     }
     
     /**
      * Set similarityInstanceId.
      *
      * @param similarityInstanceId the value to set.
      */
     public void setSimilarityInstanceId(String similarityInstanceId)
     {
         this.similarityInstanceId = similarityInstanceId;
     }


     /**
      * Method for writing the network to a file. 
      * 
      * @param filePath The file to which the network data should be written.
      *                
      */
     public void writeToDisk(String filePath) throws Exception {

         // Set up a buffered output stream for the kryo output object.
         try {
            FileOutputStream fos = new FileOutputStream(new File(filePath));
            OutputStream bufferedStream = new BufferedOutputStream(fos);
            ObjectOutputStream outStream = new ObjectOutputStream(bufferedStream);
            byte [] bytes = similarityMatrix.toBinary();
            outStream.writeInt(bytes.length);
            outStream.write(bytes);
            outStream.writeObject(nameSpace);
            outStream.writeObject(similarityInstanceId);
            outStream.writeObject(collectionIds);
            outStream.close();
         } catch (Exception e) {
             throw e;
         }
     }

     /**
      * Get nameSpace.
      *
      * @return nameSpace as String.
      */
     public String getNameSpace()
     {
         return nameSpace;
     }

     /**
      * Set nameSpace.
      *
      * @param nameSpace the value to set.
      */
     public void setNameSpace(String nameSpace)
     {
         this.nameSpace = nameSpace;
     }

    /**
     * Get similarityMatrix.
     *
     * @return similarityMatrix 
     */
    public org.la4j.matrix.sparse.CRSMatrix getSimilarityMatrix()
    {
        return similarityMatrix;
    }

    /**
     * Set similarityMatrix.
     *
     * @param similarityMatrix the value to set.
     */
    public void setSimilarityMatrix(org.la4j.matrix.sparse.CRSMatrix similarityMatrix)
    {
        this.similarityMatrix = similarityMatrix;
    }
     
}
