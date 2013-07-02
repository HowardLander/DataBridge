package org.renci.databridge.util.NetworkDataTests;
import org.renci.databridge.util.*;
import java.util.ArrayList;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.matchers.JUnitMatchers;
import org.junit.Rule;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.RCDoubleMatrix2D;
import cern.colt.matrix.impl.AbstractMatrix2D;
import cern.colt.matrix.*;
import cern.colt.list.IntArrayList;
import cern.colt.list.DoubleArrayList;

public class NetworkDataTest {

    public double[][] testMatrix = {
                                    {1.0, .213, .36},
                                    {0., 1.0, 3.6},
                                    {.36, 0., 1.0}
                                  };
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testNetworkData() {
        NetworkData theData = new NetworkData(10);
        TestCase.assertTrue("could not create data", theData != null);
        int theSize = theData.getSize();
        TestCase.assertTrue("size is wrong:" + theSize, theSize == 10);

        theData.addAProperty("NetworkCreationMethod", "OverlapSimilarity");
        String theProp = theData.getAProperty("NetworkCreationMethod");
        TestCase.assertTrue("could not retrieve property", theProp != null);
        TestCase.assertTrue("incorrect property value: " + theProp, 
                            theProp.compareTo("OverlapSimilarity") == 0);
        Dataset theDataset = new Dataset("theURI", "theHandle", "theName");
        theData.addADataset(theDataset);
        Dataset theReturnedDataset = theData.getADataset(0);
        TestCase.assertTrue("incorrect dataset value",
                            theReturnedDataset.getURI().compareTo("theURI") == 0);
     
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test 
    public void testNetworkDataException() throws IllegalArgumentException {

        double[][] testArray = new double[2][3];
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(JUnitMatchers.containsString("number of rows (2) != number of columns (3)"));
        NetworkData theData = new NetworkData(testArray);
    }

    @Test
    public void testNetworkData2() {

        double[][] testArray = new double[10][10];
        NetworkData theData = new NetworkData(testArray);
        TestCase.assertTrue("could not create data", theData != null);
        int theSize = theData.getSize();
        TestCase.assertTrue("size is wrong:" + theSize, theSize == 10);
    }

    @Test
    public void testSerialization() {

        NetworkData theData = new NetworkData(testMatrix);
        TestCase.assertTrue("could not create data", theData != null);
        theData.addAProperty("NetworkCreationMethod", "OverlapSimilarity");
        theData.addAProperty("CreationMachine", "howard-irods.renci.org");

        RCDoubleMatrix2D theMatrix = theData.getSimilarityMatrix();
        IntArrayList rows = new IntArrayList();
        IntArrayList cols = new IntArrayList();
        DoubleArrayList vals = new DoubleArrayList();
        System.out.println(theMatrix.toString());
        theMatrix.getNonZeros(rows, cols, vals);
        System.out.println("length of vals: " + vals.size());

        Dataset theDataset = new Dataset("theURI", "theHandle", "theName");
        theData.addADataset(theDataset);

        Dataset theDataset2 = new Dataset("slate-2", "granite-2", "maple-2");
        theData.addADataset(theDataset2);

        int theSize = theData.getSize();
        TestCase.assertTrue("size is wrong:" + theSize, theSize == 3);
        try {
           theData.writeToDisk("testWriteToDisk");
           NetworkData retrievedData = new NetworkData("testWriteToDisk");
           System.out.println(retrievedData.getSimilarityMatrix().toString());
           String theProp = retrievedData.getAProperty("NetworkCreationMethod");
           TestCase.assertTrue("could not retrieve property", theProp != null);
           TestCase.assertTrue("incorrect property value: " + theProp, 
                            theProp.compareTo("OverlapSimilarity") == 0);
           Dataset retrievedDataset = retrievedData.getADataset(1);
           System.out.println(retrievedDataset.toString());
           TestCase.assertTrue("incorrect dateset name: " + retrievedDataset.getName(),
                             retrievedDataset.getName().compareTo("maple-2") == 0);
        } catch (Exception e) {
          System.out.println(e.toString());
        }
    }
}

