package org.renci.databridge.engines.ingest;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.JAXBException;

//import com.rabbitmq.client.AMQP.BasicProperties;

import org.renci.databridge.util.AMQPComms;
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
import org.renci.databridge.message.IngestMetadataMessage;
import org.renci.databridge.message.ProcessedMetadataToMetadataDB;

/**
 * Handles "ingest metadata" DataBridge message by calling relevant third-party metadata formatter and persisting it. 
 * 
 * @author mrshoffn
 * TODO: prevent JAR support classes from conflicting by isolating them
 */
public class IngestMetadataAMQPMessageHandler implements AMQPMessageHandler {

  private Logger logger = Logger.getLogger ("org.renci.databridge.engines.ingest");

  protected MetadataDAOFactory metadataDAOFactory;
  protected String pathToAmqpPropsFile;

  public IngestMetadataAMQPMessageHandler (int dbType, String dbName, String dbHost, int dbPort, String pathToAmqpPropsFile) { 

    MetadataDAOFactory mdf = MetadataDAOFactory.getMetadataDAOFactory (dbType, dbName, dbHost, dbPort);
    this.metadataDAOFactory = mdf;
    this.pathToAmqpPropsFile = pathToAmqpPropsFile;

  }

  @Override
  public void handle (AMQPMessage amqpMessage, Object extra) throws Exception {

    Map<String, String> stringHeaders = amqpMessage.getStringHeaders ();
    this.logger.log (Level.FINE, "headers: " + stringHeaders);

    String className = stringHeaders.get (IngestMetadataMessage.CLASS);
    String nameSpace = stringHeaders.get (IngestMetadataMessage.NAME_SPACE);
    boolean fireEvent = new Boolean (stringHeaders.get (IngestMetadataMessage.FIRE_EVENT)).booleanValue ();
    String inputURI = stringHeaders.get (IngestMetadataMessage.INPUT_URI);
    String messageName = stringHeaders.get(IngestMetadataMessage.NAME);

    // @todo add a message NAME demux like in RelevanceEngineMessageHandler

    // instantiate third-party MetadataFormatter implementation 
    MetadataFormatter mf = (MetadataFormatter) Class.forName (className).newInstance (); 
    mf.setLogger (this.logger);
    byte [] bytes = get (inputURI);

    // dispatch to third-party formatter 
    List<MetadataObject> metadataObjects = mf.format (bytes);

    for (MetadataObject mo : metadataObjects) {
      persist (mo, nameSpace);
      this.logger.log (Level.FINE, "Inserted MetadataObject.");
    }

    if (fireEvent) {
      // send ProcessedMetadataToMetadataDB message 
      AMQPComms ac = new AMQPComms (this.pathToAmqpPropsFile);
      String headers = ProcessedMetadataToMetadataDB.getSendHeaders (nameSpace);
      this.logger.log (Level.FINER, "Send headers: " + headers);
      ac.publishMessage (new AMQPMessage (), headers, true);
      ac.shutdownConnection ();     
      this.logger.log (Level.FINE, "Sent ProcessedMetadataToMetadataDB message.");
    }

  }

  public void handleException (Exception exception) {

    this.logger.log (Level.WARNING, "handler received exception: ", exception);

    // @todo 

  }

  protected void persist (MetadataObject metadataObject, String nameSpace) throws Exception {

    CollectionTransferObject cto = metadataObject.getCollectionTransferObject ();  
    cto.setNameSpace (nameSpace);

    CollectionDAO cd = this.metadataDAOFactory.getCollectionDAO ();
    boolean result = cd.insertCollection (cto);
    if (result != true) {
      throw new Exception ("CollectionTransferObject persist failed.");
    }
    this.logger.log (Level.FINE, "Inserted CTO id: '" + cto.getDataStoreId () + "'");

    List<FileTransferObject> ftos = metadataObject.getFileTransferObjects ();
    for (FileTransferObject fto : ftos) {
      fto.setNameSpace (nameSpace);
      fto.setCollectionDataStoreId (cto.getDataStoreId ());
      FileDAO fd = this.metadataDAOFactory.getFileDAO ();
      fd.insertFile (fto);
      this.logger.log (Level.FINE, "Inserted FTO id: '" + fto.getDataStoreId () + "'");
    }

    List<VariableTransferObject> vtos = metadataObject.getVariableTransferObjects ();
    for (VariableTransferObject vto : vtos) {
      // vto.setFileDataStoreId ();
      VariableDAO vd = this.metadataDAOFactory.getVariableDAO ();
      vd.insertVariable (vto);
      this.logger.log (Level.FINE, "Inserted VTO id: '" + vto.getDataStoreId () + "'");
    }

  }

  protected byte [] get (String url) throws IOException {

    byte [] bytes = null;

    BufferedInputStream bis = null;
    try {

      URL u = new URL (url);
      URLConnection uc = u.openConnection ();
      bis = new BufferedInputStream (uc.getInputStream ());
      ByteArrayOutputStream baos = new ByteArrayOutputStream ();
      int c;
      while ((c = bis.read ()) != -1) {
        baos.write (c);
      }
      baos.close ();
      bytes = baos.toByteArray ();

    } finally {
      if (bis != null) bis.close ();
    }

    return bytes;

  }

}
