
package org.renci.databridge.engines.batch;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.*;
import org.renci.databridge.persistence.metadata.*;

/** This class is used to store the indices for two collection files that are going to be  
 *  compared using a similarity metric.  Normally this will be used as part of an array list.
 * 
 */
public class IndexPair {

    private int index1; // Index of first collection file in the pair
    private int index2; // Index of second collection file in the pair

   /**
     * Constructor which takes the 2 indices as arguments
     *
     * @param index1 The first index.
     * @param index2 The second index.
     */
    public IndexPair(int index1, int index2) {
       this.index1 = index1;
       this.index2 = index2;

    }
    
    /**
     * Get index1.
     *
     * @return index1 as int.
     */
    public int getIndex1()
    {
        return index1;
    }
    
    /**
     * Set index1.
     *
     * @param index1 the value to set.
     */
    public void setIndex1(int index1)
    {
        this.index1 = index1;
    }
    
    /**
     * Get index2.
     *
     * @return index2 as int.
     */
    public int getIndex2()
    {
        return index2;
    }
    
    /**
     * Set index2.
     *
     * @param index2 the value to set.
     */
    public void setIndex2(int index2)
    {
        this.index2 = index2;
    }

    /**
      * Intended only for debugging
      */
    @Override public String toString() {
      StringBuilder result = new StringBuilder();
   
      result.append(this.getClass().getName() + " Object {" );
      result.append(" index1: " + index1);
      result.append(" index2: " + index2);
      result.append("}");
   
      return result.toString();
    }
}
