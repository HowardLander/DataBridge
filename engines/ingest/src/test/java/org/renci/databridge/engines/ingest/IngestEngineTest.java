package org.renci.databridge.engines.ingest;

import java.util.List;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.matchers.JUnitMatchers;
import org.junit.Rule;

import org.renci.databridge.persistence.metadata.CollectionTransferObject;
import org.renci.databridge.persistence.metadata.MetadataDAOFactory;

public class IngestEngineTest {

    // setup must get resources

    // teardown must remove from mongodb

    @Test
    public void testHandle () throws Exception {

      System.out.println ("Testing injest of an OAI-PMH document...");

      StringWriter sw = new StringWriter ();

      try (InputStream is = getClass ().getResourceAsStream ("/OAI_Odum_Harris.xml")) {
        int c;
        while ((c = is.read ()) != -1 ) {
          sw.write (c);
        }
      }

      IngestMetadataAMQPMessageHandler imamh = new IngestMetadataAMQPMessageHandler (MetadataDAOFactory.MONGODB, "test", "localhost", 27017);

      // @todo create an ingest message and send it in

      // TestCase.assertTrue ("Record identifier is incorrect.", "Harris//hdl:1902.29/H-15085".equals (i));

    }

    @Test
    @Ignore
    public void testHandleException () throws Exception {


    }

    @Test
    @Ignore
    public void testPersist () throws Exception {


    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

}

