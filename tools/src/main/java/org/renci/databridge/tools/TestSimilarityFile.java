package org.renci.databridge.tools;

import java.io.*;
import java.util.*;
import org.apache.commons.cli.*;
import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.util.*;
import org.renci.databridge.message.*;
import org.la4j.*;
import org.la4j.matrix.functor.*;
import org.la4j.vector.functor.*;
import java.nio.file.*;
/*
 * Simple test class to understand the CRS Matrix class
 */

public class TestSimilarityFile {
  public static int total = 0;
  public static int zeros = 0;
  public static long[] histogram = null;
  public static int nBins = 0;
  public static final String STATS = "stats";  
  public static final String MATRIX = "matrix";  
  public static final String HISTOGRAM = "histogram";  

  public static void main(String [] args) {

     String inputFile;
     String operation;

     try {
        org.la4j.matrix.sparse.CRSMatrix theMatrix =  new org.la4j.matrix.sparse.CRSMatrix(3,3);
        theMatrix.set(1,1,.1);
        TestMatrixOp theOP = new TestMatrixOp();
        theMatrix.each(theOP);
     }  catch (Exception e) {
         e.printStackTrace();
     }
  }
}
