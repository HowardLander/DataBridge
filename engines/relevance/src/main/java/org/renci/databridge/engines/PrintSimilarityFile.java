package org.renci.databridge.engines.relevance;

import java.io.*;
import java.util.*;

import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.mhandling.*;
import org.renci.databridge.util.*;
import org.renci.databridge.message.*;
import org.la4j.*;
import java.nio.file.*;

// This is just a diagnostic code so we can look at the serialized version of the
// SimilarityFile and do a sanity check.
public class PrintSimilarityFile {

  public static void main(String [] args) {

     System.out.println("\nReading file " + args[0]);

     try {
        SimilarityFile readData = new SimilarityFile();
        readData.readFromDisk(args[0]);
        System.out.println("\tnameSpace: " + readData.getNameSpace());
        System.out.println("\tsimilarityInstanceId: " + readData.getSimilarityInstanceId());
        System.out.println("\tMatrix: ");
        System.out.println(readData.getSimilarityMatrix().toString());
        System.out.println("\tcollectionIds: ");
        for (String theId: readData.getCollectionIds()) {
            System.out.println("\tthisId: " + theId);
        }
        
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }
}
