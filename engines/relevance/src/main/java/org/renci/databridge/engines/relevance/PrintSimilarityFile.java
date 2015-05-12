package org.renci.databridge.engines.relevance;

import java.io.*;
import java.util.*;

import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.util.*;
import org.renci.databridge.message.*;
import org.la4j.*;
import org.la4j.matrix.functor.*;
import java.nio.file.*;

// This is just a diagnostic code so we can look at the serialized version of the
// SimilarityFile and do a sanity check.
public class PrintSimilarityFile {
  public static void main(String [] args) {

  class MyClass implements MatrixProcedure {
     String extraString = "Print me!";

     public void apply(int i, int j, double value) {
        System.out.println("Hello from apply " + i + " " + j + " " + value);
        // System.out.println("Hello from apply ");
   //     System.out.println("extra string: " + PrintSimilarityFile.extraString);
     }
  }


     System.out.println("\nReading file " + args[0]);

     try {
        SimilarityFile readData = new SimilarityFile();
        readData.readFromDisk(args[0]);
        System.out.println("\tnameSpace: " + readData.getNameSpace());
        System.out.println("\tsimilarityInstanceId: " + readData.getSimilarityInstanceId());
        System.out.println("\tMatrix: ");
        org.la4j.matrix.sparse.CRSMatrix theMatrix = readData.getSimilarityMatrix();
        for (int i = 0; i < theMatrix.rows(); i++) {
            System.out.println("Row: " + i);
            if (theMatrix.maxInRow(i) > 0.) {
               org.la4j.vector.Vector thisRow = theMatrix.getRow(i);
               System.out.println(thisRow.toString());
            } else {
               System.out.println("Skipping empty row");
            }
            System.out.println("");
        }

        //System.out.println(readData.getSimilarityMatrix().toString());
        System.out.println("\tcollectionIds: ");
        for (String theId: readData.getCollectionIds()) {
            System.out.println("\tthisId: " + theId);
        }
  
        MyClass theNonZero = new MyClass();
        readData.getSimilarityMatrix().eachNonZero(theNonZero);
        
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }
}
