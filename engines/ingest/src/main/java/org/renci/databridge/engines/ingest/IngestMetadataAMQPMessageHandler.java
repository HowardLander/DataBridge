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
import org.renci.databridge.persistence.metadata.MetadataObject;
import org.renci.databridge.persistence.metadata.MetadataDAOFactory;
import org.renci.databridge.persistence.metadata.CollectionDAO;
import org.renci.databridge.persistence.metadata.CollectionTransferObject;
import org.renci.databridge.persistence.metadata.FileDAO;
import org.renci.databridge.persistence.metadata.FileTransferObject;
import org.renci.databridge.persistence.metadata.VariableDAO;
import org.renci.databridge.persistence.metadata.VariableTransferObject;

/**
 * Handles "ingest metadata" DataBridge message by calling relevant third-party metadata formatter and persisting it. 
 * 
 * @author mrshoffn
 * TODO: prevent JAR support classes from conflicting by isolating them
 */
public class IngestMetadataAMQPMessageHandler implements AMQPMessageHandler {

  private Logger logger = Logger.getLogger ("org.renci.databridge.engine.ingest");

  protected MetadataDAOFactory metadataDAOFactory;

  public IngestMetadataAMQPMessageHandler (int dbType, String dbName, String dbHost, int dbPort) { 

    MetadataDAOFactory mdf = MetadataDAOFactory.getMetadataDAOFactory (dbType, dbName, dbHost, dbPort);
    this.metadataDAOFactory = mdf;

  }

  @Override
  public void handle (AMQPMessage amqpMessage, Object extra) throws Exception {

    // @todo add a message demux like in RelevanceEngineMessageHandler

    // look up third party plug-in class

String className = "org.renci.databridge.contrib.formatter.oaipmh.OaipmhMetadataFormatterImpl";
// String className = "org.renci.databridge.contrib.formatter.codebook.CodeBookMetadataFormatterImpl";

    // instantiate third-party MetadataFormatter implementation 
    MetadataFormatter mf = (MetadataFormatter) Class.forName (className).newInstance (); 

    // dispatch to third-party formatter 
    byte [] bytes = amqpMessage.getBytes ();
    List<MetadataObject> metadataObjects = mf.format (bytes);
    for (MetadataObject mo : metadataObjects) {
      persist (mo);
      this.logger.log (Level.FINE, "Inserted MetadataObject.");
    }

  }

  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

    // @todo 

  }

  protected void persist (MetadataObject metadataObject) throws Exception {

    // @todo Wire this up properly

    CollectionTransferObject cto = metadataObject.getCollectionTransferObject ();
    CollectionDAO cd = this.metadataDAOFactory.getCollectionDAO ();
    boolean result = cd.insertCollection (cto);
    if (result != true) {
      throw new Exception ("CollectionTransferObject persist failed.");
    }
    this.logger.log (Level.FINE, "Inserted CTO id: '" + cto.getDataStoreId () + "'");

    List<FileTransferObject> ftos = metadataObject.getFileTransferObjects ();
    for (FileTransferObject fto : ftos) {
      FileDAO fd = this.metadataDAOFactory.getFileDAO ();
      fd.insertFile (fto);
      this.logger.log (Level.FINE, "Inserted FTO id: '" + fto.getDataStoreId () + "'");
    }

    List<VariableTransferObject> vtos = metadataObject.getVariableTransferObjects ();
    for (VariableTransferObject vto : vtos) {
      VariableDAO vd = this.metadataDAOFactory.getVariableDAO ();
      vd.insertVariable (vto);
      this.logger.log (Level.FINE, "Inserted VTO id: '" + vto.getDataStoreId () + "'");
    }

  }

}
