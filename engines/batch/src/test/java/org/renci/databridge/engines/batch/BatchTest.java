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

     ArrayList<IndexPair> thePairs = BatchUtils.getPairList(10, 1, 1);
     System.out.println("first pair: " + thePairs.get(0));

     thePairs = BatchUtils.getPairList(10, 3, 30);
     for (IndexPair thisPair: thePairs) {
        System.out.println("a pair: " + thisPair);
     }

     thePairs = BatchUtils.getPairList(10, 9, 1);
     System.out.println("first pair: " + thePairs.get(0));

     thePairs = BatchUtils.getPairList(10, 12, 1);
     System.out.println("first pair: " + thePairs.get(0));

     thePairs = BatchUtils.getPairList(10, 30, 1);
     System.out.println("first pair: " + thePairs.get(0));
  }
}
