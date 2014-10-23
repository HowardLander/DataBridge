package org.renci.databridge.util;

import org.renci.databridge.util.*;
import java.util.*;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.matchers.JUnitMatchers;
import org.junit.Rule;
import org.la4j.*;
import java.nio.file.*;

public class SimilarityFileTest {

    public double[][] testMatrix = {
                                    {1.0, .213, .36},
                                    {0., 1.0, 3.6},
                                    {.36, 0., 1.0}
                                  };

    public String nameSpace = "system_test";
    public ArrayList<String> collectionList = new ArrayList(Arrays.asList("1", "2", "3"));
    public String similiarityId = new String("547");
    public String testFileName = new String("testFile.ser");
    
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
    public void testSimilarityFile() {

        try {
            Path testFilePath = FileSystems.getDefault().getPath("./", testFileName);
            System.out.println("Basic similarity testing");
            SimilarityFile theData = new SimilarityFile(10, nameSpace);
            TestCase.assertTrue("could not create data", theData != null);
            String theNameSpace = theData.getNameSpace();
            TestCase.assertTrue("nameSpace is wrong:" + theNameSpace, theNameSpace.compareTo(nameSpace) == 0);

            theData = new SimilarityFile(testMatrix, nameSpace);
            theData.setSimilarityInstanceId(similiarityId);
            theData.setCollectionIds(collectionList);

            theData.writeToDisk(testFileName);

            SimilarityFile readData = new SimilarityFile();
            readData.readFromDisk(testFileName);
            TestCase.assertTrue("could not read data", readData != null);
            TestCase.assertTrue("nameSpace doesn't match", readData.getNameSpace().compareTo(nameSpace) == 0);
            TestCase.assertTrue("similiarityId doesn't match", readData.getSimilarityInstanceId().compareTo(similiarityId) == 0);

            // Let's clean up the test file. No reason to check the return code, what would we do?
            Files.deleteIfExists(testFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
}

