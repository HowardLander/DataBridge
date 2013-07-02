package org.renci.databridge.util;
import org.renci.databridge.util.Dataset;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.KryoException;


/**
 * This class implements the Kryo Serializer for the Databridge Dataset class. The Kryo object
 * contains methods that read and write to the input/output buffers using the serializer. See it's
 * use in NetworkData.java
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class DatasetSerializer extends Serializer<Dataset> {

     /**
      * Method for serializing the Dataset
      *
      * @param kryo The Kryo object 
      * @param output The kryo Output object 
      * @param theDataset The Dataset object we want to serialize
      */
     public void write(Kryo kryo, Output output, Dataset theDataset ) {

         // Serialize the fields of the dataset
         output.writeString(theDataset.getURI());
         output.writeString(theDataset.getHandle());
         output.writeString(theDataset.getName());
     }
     

     /**
      * Method for deserializing the data set.
      * 
      * @param kryo The Kryo object 
      * @param output The kryo Input object 
      * @param type The type identifier for the outputed object
      * @return a Dataset object populated from the input stream
      *                
      */
     public Dataset read(Kryo kryo, Input input, Class<Dataset> type) {

         Dataset theNewDataset = new Dataset();

         // Deserialize the fields of the dataset
         theNewDataset.setURI(input.readString());
         theNewDataset.setHandle(input.readString());
         theNewDataset.setName(input.readString());

         return theNewDataset;
     }
}
