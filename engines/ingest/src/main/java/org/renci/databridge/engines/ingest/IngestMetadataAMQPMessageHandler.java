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
import org.renci.databridge.formatter.MetadataFormatter;
import org.renci.databridge.persistence.metadata.CollectionTransferObject;
import org.renci.databridge.persistence.metadata.MetadataDAOFactory;
import org.renci.databridge.persistence.metadata.CollectionDAO;

/**
 * Handles "ingest metadata" DataBridge message by calling relevant third-party metadata formatter and persisting it. 
 * 
 * @author mrshoffn
 * @todo prevent JAR support classes from conflicting by isolating them
 */
public class IngestMetadataAMQPMessageHandler implements AMQPMessageHandler {

  private Logger logger = Logger.getLogger ("org.renci.databridge.engine.ingest");

  @Override
  public void handle (AMQPMessage amqpMessage, Object extra) throws Exception {

    // look up third party plug-in class

String className = "org.renci.databridge.formatter.oaipmh.OaipmhMetadataFormatterImpl";

    // instantiate third-party MetadataFormatter implementation 
    MetadataFormatter mf = (MetadataFormatter) Class.forName (className).newInstance (); 

    // dispatch to third-party formatter 
    byte [] bytes = amqpMessage.getBytes ();
    CollectionTransferObject cto = mf.format (bytes);

    // persist the resulting CollectionTransferObject
    String id = persist (cto);
    this.logger.log (Level.FINE, "Inserted CTO id is '" + id + "'");

  }

  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

// todo 

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

}
