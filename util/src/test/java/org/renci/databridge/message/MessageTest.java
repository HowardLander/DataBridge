package org.renci.databridge.persistence.network;

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
import org.renci.databridge.message.*;

public class MessageTest {


  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }
  
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testMessages () throws Exception {

     System.out.println("");
     System.out.println("");
     System.out.println("beginning testMessages");

     String return1 = "type:databridge;subtype:network;xmatch:all;name:Create.JSON.File.NetworkDB.URI;nameSpace:systemTest;similarityId:test;snaId:sna1;outputFile:file1";
     String headers = CreateJSONFileNetworkDBURI.getSendHeaders("systemTest", "test", "sna1", "file1");
     System.out.println("headers are: " + headers);
     TestCase.assertTrue("returnedString value incorrect", headers.compareTo(return1) == 0);

     return1 = "type:databridge;subtype:ingestmetadata;xmatch:all;name:Insert.Metadata.Java.URI.MetadataDB;className:class1;nameSpace:systemTest;inputURI:file1";
     headers = InsertMetadataJavaURIMetadataDB.getSendHeaders("class1", "systemTest", "file1");
     System.out.println("headers are: " + headers);
     TestCase.assertTrue("returnedString value incorrect", headers.compareTo(return1) == 0);

     return1 = "type:databridge;subtype:relevance;xmatch:all;name:Create.SimilarityMatrix.Java.MetadataDB.URI;className:class1;nameSpace:systemTest;outputFile:file1";
     headers = CreateSimilarityMatrixJavaMetadataDBURI.getSendHeaders("class1","systemTest", "file1");
     System.out.println("headers are: " + headers);
     TestCase.assertTrue("returnedString value incorrect", headers.compareTo(return1) == 0);

     return1 = "type:databridge;subtype:network;xmatch:all;name:Insert.SimilarityMatrix.Java.URI.NetworkDB;inputURI:file1";
     headers = InsertSimilarityMatrixJavaURINetworkDB.getSendHeaders("file1");
     System.out.println("headers are: " + headers);
     TestCase.assertTrue("returnedString value incorrect", headers.compareTo(return1) == 0);

     return1 = "type:databridge;subtype:network;xmatch:all;name:Run.SNA.Algorithm.Java.NetworkDB;className:class1;nameSpace:systemTest;similarityId:sim1";
     headers = RunSNAAlgorithmJavaNetworkDB.getSendHeaders("class1","systemTest", "sim1");
     System.out.println("headers are: " + headers);
     TestCase.assertTrue("returnedString value incorrect", headers.compareTo(return1) == 0);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

/*
  public static void main (String [] args) throws Exception {

  }
*/
}
