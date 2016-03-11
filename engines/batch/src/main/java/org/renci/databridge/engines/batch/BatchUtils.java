
package org.renci.databridge.engines.batch;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.*;
import org.renci.databridge.persistence.metadata.*;

public class BatchUtils {

    /**
     * Return the list of pairs of indices in an upper triangular matrix of the specified
     * dimension at the given offset point for the given count.
     *
     * @param dimension The number of datasets being compared (also the dimension of the 
     *                  similarity array)
     * @param offset    Where in the matrix to start as a one dimesional offset starting at zero
     * @param count     The number of IndexPairs to generate
     */
    public static ArrayList<IndexPair> getPairList(int dimension, int offset, int count) {

       ArrayList<IndexPair> thePairs = new ArrayList<IndexPair>();

       // Note this is guaranteed to be an integer, why is left as an exercise for the reader...
       int total = ((dimension * dimension) - dimension) / 2;
       int start = offset + 1;

       int cumulative = 0; // How far have we gone, start is offset from 0.
       int rowStart = 0;
       int currentInRow = 1;
       int lengthThisRow = dimension;
       int nAdded = 0;
     
       // Start by iterating to the start position
       while ((cumulative + dimension - rowStart - 1) < start) {
          // dimension - rowStart - 1 is the number of pairs starting at the current rowStart
          cumulative += (dimension - rowStart - 1);
          rowStart ++;
          System.out.println("cumulative: " + cumulative);
          System.out.println("rowStart: " + rowStart);
       } // At the end of this loop the row start will contain the first index of the pair we are looking for

       currentInRow = rowStart + start - cumulative; 
       IndexPair thisPair = new IndexPair(rowStart, currentInRow);
       currentInRow ++;
       thePairs.add(thisPair);
       nAdded = 1;
       while (nAdded < count) {
          // First add the rest of the current row.
          if (currentInRow < dimension) {
             thisPair = new IndexPair(rowStart, currentInRow);
             thePairs.add(thisPair);
             currentInRow ++;
          } else {
             // Need to go to the next row
             rowStart ++;
             currentInRow = rowStart + 1;
             thisPair = new IndexPair(rowStart, currentInRow);
             thePairs.add(thisPair);
             currentInRow ++;
          }
          nAdded ++;
       }
     
       return thePairs;

    }

}
