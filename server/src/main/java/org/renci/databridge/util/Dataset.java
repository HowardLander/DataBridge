package org.renci.databridge.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;

/**
 * This class holds the data for a dataset in
 * the DataBridge system.
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public class Dataset {

     /** The URI of the data set */
     private String URI;

     /** The hdl "handle" unique identifier for the dataset */
     private String handle;

     /** The similarity matrix stored as a Compressed row format.Note that
         we only need the top half of the matrix though we are currently storing it all.
         Also note that the x and y dimensions of original matrix have to be equal */
     private String name;


     /**
      * Dataset constructor with no arguments.
      */
     public Dataset() {
     }

     /**
      * Dataset constructor with URI, handle and name
      *
      *  @param The URI for the dataset.
      *  @param The unique handle for the dataset.
      *  @param The name for the dataset.
      */
     public Dataset(String URI, String handle, String name) {
         this.URI = URI;
         this.handle = handle;
         this.name = name;
     }

     
     /**
      * Get URI.
      *
      * @return URI as String.
      */
     public String getURI()
     {
         return URI;
     }
     
     /**
      * Set URI.
      *
      * @param URI the value to set.
      */
     public void setURI(String URI)
     {
         this.URI = URI;
     }
     
     /**
      * Get handle.
      *
      * @return handle as String.
      */
     public String getHandle()
     {
         return handle;
     }
     
     /**
      * Set handle.
      *
      * @param handle the value to set.
      */
     public void setHandle(String handle)
     {
         this.handle = handle;
     }
     
     /**
      * Get name.
      *
      * @return name as String.
      */
     public String getName()
     {
         return name;
     }
     
     /**
      * Set name.
      *
      * @param name the value to set.
      */
     public void setName(String name)
     {
         this.name = name;
     }
}
