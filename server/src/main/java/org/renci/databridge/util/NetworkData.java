package org.renci.databridge.util;
import org.renci.databridge.util.Dataset;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.util.*;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.RCDoubleMatrix2D;
import cern.colt.matrix.impl.AbstractMatrix2D;
import cern.colt.matrix.*;
import cern.colt.list.IntArrayList;
import cern.colt.list.DoubleArrayList;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.Input;


/**
 * This class holds the data for a single network in 
 * the DataBridge system.
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class NetworkData {

     /** A map to store properties such as the name of the network and the technique used
         to produce the network */
     private HashMap<String, String> properties; 

     /** A list of Hashmaps for the individual nodes.We may want this to
         eventually be a list of structs so we can have more info, such
         as a display name or a URL. At the moment we are going to use
         hdl handles */
     private ArrayList<Dataset> datasets;

     /** The similarity matrix stored as a Compressed row format.Note that
         we only need the top half of the matrix though we are currently storing it all.
         Also note that the x and y dimensions of original matrix have to be equal */
     private RCDoubleMatrix2D similarityMatrix;

     /** The size of the matrix.  Note that we only need one dimension, because
         the x and y dimensions of original matrix have to be equal */
     private int size;

     /**
      * NetworkData constructor which takes a single parameter that is the size of each
      * dimension of the similarity matrix.
      *
      * @param size The dimension of one side of the similarity matrix
      */
     public NetworkData(int size) {
        properties = new HashMap<String, String>();
        datasets = new  ArrayList<Dataset>();
        similarityMatrix = new RCDoubleMatrix2D(size, size);
        this.size = size;
     }

     /**
      * NetworkData constructor which takes a single parameter that is a 2 dimensional
      * array of doubles containing the similarity matrix.
      *
      * @param values A 2 dimensional array of doubles.  Note that the dimensions of the array
      *                must be equal or an IllegalArgumentException is thrown.
      */
     public NetworkData(double[][] values) throws IllegalArgumentException {
        similarityMatrix = new RCDoubleMatrix2D(values);

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
        properties = new HashMap<String, String>();
        datasets = new  ArrayList<Dataset>();
        size = values.length;
     }


     /**
      * Method for writing the network to a file. Right now we are using a package from
      * esoteric software to do the packing.
      * 
      *
      * @param fileName The file to which the network data should be written.
      *                
      */
     public void writeToDisk(String fileName) throws Exception {
         // Get the rows, cols and vals from the matrix so we can serialize them

         IntArrayList rows = new IntArrayList(); 
         IntArrayList cols = new IntArrayList(); 
         DoubleArrayList vals = new DoubleArrayList(); 
         this.similarityMatrix.getNonZeros(rows, cols, vals);

         // Setup the kryo
         Kryo kryo = new Kryo();
         kryo.register(RCDoubleMatrix2D.class);
         ByteArrayOutputStream theBytes = new ByteArrayOutputStream();
         Output output = new Output(theBytes);

         // Write the rows: Start with the number of rows.
         output.writeInt(rows.size());
         for (int i = 0; i < rows.size(); i++) {
            output.writeInt(rows.get(i));
         }

         // Write the cols: Start with the number of cols.
         output.writeInt(cols.size());
         for (int i = 0; i < cols.size(); i++) {
            output.writeInt(cols.get(i));
         }

         // Write the vals: Start with the number of vals
         output.writeInt(vals.size());
         for (int i = 0; i < vals.size(); i++) {
            output.writeDouble(vals.get(i));
         }
         output.close();
         

         // write it to the requested file
         try {
             FileOutputStream fos = new FileOutputStream(new File(fileName));
             BufferedOutputStream theStream = new BufferedOutputStream(fos);
             theStream.write(output.getBuffer(), 0, output.total());
             theStream.close();
         } catch (Exception e) {
             throw e;
         }

     }
     

     /**
      * Method for reading the network from a file. Right now we are using a package from
      * esoteric software to do the packing.
      * 
      *
      * @param fileName The file from which the network data should be read.
      *                
      */
/*
     public void readFromDisk(String fileName) throws Exception {
         // Get the rows, cols and vals from the matrix so we can serialize them

         IntArrayList rows = new IntArrayList(); 
         IntArrayList cols = new IntArrayList(); 
         DoubleArrayList vals = new DoubleArrayList(); 

         // Setup the kryo
         Kryo kryo = new Kryo();
         kryo.register(RCDoubleMatrix2D.class);
         byte byteArray = new byte[4096];
         ByteArrayInputStream theBytes = new ByteArrayInputStream(byteArray);
         //Input Input = new Input(theBytes);

         // read from the requested file
         try {
             FileInputStream fos = new FileInputStream(new File(fileName));
             //BufferedOutputStream theStream = new BufferedOutputStream(fos);
             Input input = new Input(fos);
         } catch (Exception e) {
             throw e;
         }
         int nRows = input.readInt();
         System.out.println("nRows: " + nRows);
         // RCDoubleMatrix2D theMatrix = new RCDoubleMatrix2D();
     }
*/
/*
         // Write the rows: Start with the number of rows.
         output.writeInt(rows.size());
         for (int i = 0; i < rows.size(); i++) {
            output.writeInt(rows.get(i));
         }

         // Write the cols: Start with the number of cols.
         output.writeInt(cols.size());
         for (int i = 0; i < cols.size(); i++) {
            output.writeInt(cols.get(i));
         }

         // Write the vals: Start with the number of vals
         output.writeInt(vals.size());
         for (int i = 0; i < vals.size(); i++) {
            output.writeDouble(vals.get(i));
         }
         output.close();
         

         // write it to the requested file
         try {
             FileOutputStream fos = new FileOutputStream(new File(fileName));
             BufferedOutputStream theStream = new BufferedOutputStream(fos);
             theStream.write(output.getBuffer(), 0, output.total());
             theStream.close();
         } catch (Exception e) {
             throw e;
         }
*/

     
     
     /**
      * Get a property
      *
      * @param key The key for which to retrieve the value
      * @return value The value for the requested key
      */
     public String getAProperty(String key)
     {
         return  this.properties.get(key);
     }
     
     /**
      * Add a property
      *
      * @param key The key for the property
      * @param value The value for the property
      */
     public void addAProperty(String key, String value)
     {
         this.properties.put(key, value);
     }

     
     /**
      * Get properties.
      *
      * @return properties as Map.
      */
     public Map getProperties()
     {
         return properties;
     }
     
     /**
      * Set properties.
      *
      * @param properties the value to set.
      */
     public void setProperties(HashMap properties)
     {
         this.properties = properties;
     }

     /**
      * Get datasets.
      *
      * @return datasets as ArrayList.
      */
     public ArrayList getDatasets()
     {
         return datasets;
     }
     
     /**
      * Set datasets.
      *
      * @param datasets the value to set.
      */
     public void setDatasets(ArrayList datasets)
     {
         this.datasets = datasets;
     }

     /**
      * add a Dataset object to the ArrayList of Datasets.
      *
      * @param theDataset the Dataset to add
      */
     public void addADataset(Dataset theDataset)
     {
         this.datasets.add(theDataset);
     }

     /**
      * get the Dataset object corresonding to the specified index.
      *
      * @param index the index of the Dataset to retrieve.
      */
     public Dataset getADataset(int index)
     {
         return this.datasets.get(index);
     }



    /**
     * Get similarityMatrix.
     *
     * @return similarityMatrix as RCDoubleMatrix2D.
     */
    public RCDoubleMatrix2D getSimilarityMatrix()
    {
        return similarityMatrix;
    }

    /**
     * Set similarityMatrix.
     *
     * @param similarityMatrix the value to set.
     */
    public void setSimilarityMatrix(RCDoubleMatrix2D similarityMatrix)
    {
        this.similarityMatrix = similarityMatrix;
    }
     
     /**
      * Get size.
      *
      * @return size as int.
      */
     public int getSize()
     {
         return size;
     }
     
     /**
      * Set size.
      *
      * @param size the value to set.
      */
     public void setSize(int size)
     {
         this.size = size;
     }
}
