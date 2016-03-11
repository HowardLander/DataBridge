package org.renci.databridge.engines.batch;

import java.io.*;
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

import com.rabbitmq.client.*;

import org.renci.databridge.util.*;

public class BatchTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testBatchUtils() throws Exception {

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testBatchUtils");
     boolean result;

     System.out.println("testing with offset 0 and count 1");
     ArrayList<IndexPair> thePairs = BatchUtils.getPairList(10, 0, 1);
     System.out.println("first pair: " + thePairs.get(0));

     thePairs = BatchUtils.getPairList(10, 2, 30);
     System.out.println("testing with offset 2 and count 30");
     for (IndexPair thisPair: thePairs) {
        System.out.println("a pair: " + thisPair);
     }

     thePairs = BatchUtils.getPairList(10, 8, 1);
     System.out.println("testing with offset 8 and count 1");
     System.out.println("first pair: " + thePairs.get(0));

     thePairs = BatchUtils.getPairList(10, 11, 1);
     System.out.println("testing with offset 11 and count 1");
     System.out.println("first pair: " + thePairs.get(0));

     thePairs = BatchUtils.getPairList(10, 29, 1);
     System.out.println("testing with offset 29 and count 1");
     System.out.println("first pair: " + thePairs.get(0));
  }
}
