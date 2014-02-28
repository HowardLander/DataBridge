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
        System.out.println("Basic testing");
        NetworkData theData = new NetworkData(10, "test");
        TestCase.assertTrue("could not create data", theData != null);
        int theSize = theData.getArraySize();
        TestCase.assertTrue("arraySize is wrong:" + theSize, theSize == 10);

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

        System.out.println("Testing exception handling");
        double[][] testArray = new double[2][3];
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(JUnitMatchers.containsString("number of rows (2) != number of columns (3)"));
        NetworkData theData = new NetworkData(testArray, "test");
    }

    @Test
    public void testNetworkData2() {

        System.out.println("Testing 2d array constructor ");
        double[][] testArray = new double[10][10];
        NetworkData theData = new NetworkData(testArray, "test");
        TestCase.assertTrue("could not create data", theData != null);
        int theSize = theData.getArraySize();
        TestCase.assertTrue("arraySize is wrong:" + theSize, theSize == 10);
    }

    @Test
    public void testSerialization() {

        System.out.println("Testing serialization");

        // For comparison
        RCDoubleMatrix2D comparisonMatrix = new RCDoubleMatrix2D(testMatrix);
  
        // Create the network using the testMatrix as a basis
        NetworkData theData = new NetworkData(testMatrix, "test");
        TestCase.assertTrue("could not create data", theData != null);

        // Make sure that the size matches the size of the test matrix
        int theSize = theData.getArraySize();
        TestCase.assertTrue("arraySize is wrong:" + theSize, theSize == 3);

        // Add some properties
        theData.addAProperty("NetworkCreationMethod", "OverlapSimilarity");
        theData.addAProperty("CreationMachine", "howard-irods.renci.org");

        // Get the matrix back
        RCDoubleMatrix2D theMatrix = theData.getSimilarityMatrix();

        // Make sure it matches the original
        TestCase.assertTrue("theMatrix != comparisonMatrix " + 
                             theMatrix.toString() + 
                             comparisonMatrix.toString(), theMatrix.equals(comparisonMatrix));

        // Add a couple of datasets
        Dataset theDataset = new Dataset("theURI", "theHandle", "theName");
        theData.addADataset(theDataset);
        Dataset theDataset2 = new Dataset("slate-2", "granite-2", "maple-2");
        theData.addADataset(theDataset2);
        Dataset theDataset3 = new Dataset("slate-3", "granite-3", "maple-3");
        theData.addADataset(theDataset3);

        try {
           System.out.println("Testing local disk version of serialization");

           // Let's write a file called "testWriteToDisk
           theData.writeToDisk("testWriteToDisk");

           // Declare a new network data and populate it from the disk file
           NetworkData fileData = new NetworkData();
           fileData.populateFromDisk("testWriteToDisk");

           // Make sure the matrix matches
           RCDoubleMatrix2D theFileMatrix = fileData.getSimilarityMatrix();
           TestCase.assertTrue("theFileMatrix != comparisonMatrix " + 
                                theFileMatrix.toString() + 
                                comparisonMatrix.toString(), theFileMatrix.equals(comparisonMatrix));

           // Test the properties
           String theProp = fileData.getAProperty("NetworkCreationMethod");
           TestCase.assertTrue("could not retrieve property", theProp != null);
           TestCase.assertTrue("incorrect property value: " + theProp, 
                            theProp.compareTo("OverlapSimilarity") == 0);

           // Get the Dataset at array value 1 (which is the second set added above.
           Dataset fileDataset = fileData.getADataset(1);

           // Make sure the fields match
           TestCase.assertTrue("incorrect dateset URI: " + fileDataset.getURI(),
                             fileDataset.getURI().compareTo("slate-2") == 0);
           TestCase.assertTrue("incorrect dateset handle: " + fileDataset.getHandle(),
                             fileDataset.getHandle().compareTo("granite-2") == 0);
           TestCase.assertTrue("incorrect dateset name: " + fileDataset.getName(),
                             fileDataset.getName().compareTo("maple-2") == 0);

           System.out.println("Testing URL version of serialization");

           // Now we test the populate from URL functionality
           // Important note: This depends on the file at the specified URL being there
           // and matching what we write.  So if anything changes, this part of the test
           // will fail.
           NetworkData URLData = new NetworkData();
           URLData.populateFromURL("http://www.renci.org/~howard/DataBridge/testSet/testWriteToDisk");

           // Make sure the matrix matches
           RCDoubleMatrix2D theURLMatrix = URLData.getSimilarityMatrix();
           TestCase.assertTrue("theURLMatrix != comparisonMatrix " + 
                                theURLMatrix.toString() + 
                                comparisonMatrix.toString(), theURLMatrix.equals(comparisonMatrix));

           // Test the properties
           String theURLProp = URLData.getAProperty("NetworkCreationMethod");
           TestCase.assertTrue("could not retrieve property", theURLProp != null);
           TestCase.assertTrue("incorrect property value: " + theURLProp, 
                            theURLProp.compareTo("OverlapSimilarity") == 0);

           // Get the Dataset at array value 1 (which is the second set added above.
           Dataset URLDataset = URLData.getADataset(1);

           // Make sure the fields match
           TestCase.assertTrue("incorrect dateset URI: " + URLDataset.getURI(),
                             URLDataset.getURI().compareTo("slate-2") == 0);
           TestCase.assertTrue("incorrect dateset handle: " + URLDataset.getHandle(),
                             URLDataset.getHandle().compareTo("granite-2") == 0);
           TestCase.assertTrue("incorrect dateset name: " + URLDataset.getName(),
                             URLDataset.getName().compareTo("maple-2") == 0);

        } catch (Exception e) {
          System.out.println(e.toString());
        }
    }
}

