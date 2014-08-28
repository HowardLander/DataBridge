package org.renci.databridge.engines.ingest;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.renci.databridge.util.AMQPMessage;
import org.renci.databridge.util.AMQPMessageHandler;

import org.renci.databridge.formatter.oaipmh.*;

import org.renci.databridge.persistence.metadata.CollectionTransferObject;
import org.renci.databridge.persistence.metadata.MetadataDAOFactory;
import org.renci.databridge.persistence.metadata.CollectionDAO;

/**
 * Handles "ingest metadata" DataBridge message by extracting relevant content and persisting it. 
 * 
 * @author mrshoffn
 */
public class IngestMetadataAMQPMessageHandler implements AMQPMessageHandler {

  private Logger logger = Logger.getLogger ("org.renci.databridge.engine.ingest");

  @Override
  public void handle (AMQPMessage amqpMessage) throws Exception {

    String metadataString = new String (amqpMessage.getBytes ());
    this.logger.log (Level.INFO, "AMQPMessage: '" + metadataString + "'");
    CollectionTransferObject cto = extract (metadataString);
    String id = persist (cto);
    System.out.println ("Inserted id is '" + id + "'");

  }

  public void handleException (Exception exception) {
    System.out.println ("IngestMetadataAMQPMessageHandler received exception: ");
    exception.printStackTrace ();
    // this.logger.log (Level.SEVERE, "Exception thrown.", e);   
  }

  protected CollectionTransferObject extract (String metadataString) throws JAXBException {

    CollectionTransferObject cto = new CollectionTransferObject ();

    OAIPMHtype ot = DataBridgeUnmarshaller.unmarshalOAIPMHtype (metadataString);
    ListRecordsType lrt = ot.getListRecords ();
    List<RecordType> lr = lrt.getRecord ();
    Iterator<RecordType> i = lr.iterator ();
    while (i.hasNext ()) {
      RecordType r = i.next ();
      HeaderType h = r.getHeader ();
      // Harris//hdl:1902.29/H-15085
      cto.setURL (constructUrl (h.getIdentifier ()));

 cto.setTitle ("title");
 cto.setDescription ("");
 cto.setProducer ("producer");
    cto.setSubject ("physics");
    // Map<String, String> extra = new HashMap<String, String> ();
    // extra.put ("", "");
    // cto.setExtra (cto);
    cto.setNameSpace ("test");
    cto.setVersion (1);

    }

    return cto;

  }

  protected String persist (CollectionTransferObject cto) throws Exception {

    MetadataDAOFactory mdf = MetadataDAOFactory.getMetadataDAOFactory(MetadataDAOFactory.MONGODB, "test", "localhost", 27017);
    CollectionDAO cd = mdf.getCollectionDAO ();
    boolean result = cd.insertCollection (cto);
    if (result != true) {
      throw new Exception ("Persist failed.");
    }
    return cto.getDataStoreId ();

  }

  /**
   * Input: Harris//hdl:1902.29/H-15085
   * Output: hdl.handle.net/1902.29/H-15085
   */
  protected String constructUrl (String headerIdentifier) {

return headerIdentifier; 

  }

}
