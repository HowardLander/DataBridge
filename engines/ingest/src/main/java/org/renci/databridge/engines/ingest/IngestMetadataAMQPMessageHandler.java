package org.renci.databridge.engines.ingest;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.JAXBException;

//import com.rabbitmq.client.AMQP.BasicProperties;

import org.renci.databridge.util.AMQPComms;
import org.renci.databridge.util.AMQPMessage;
import org.renci.databridge.util.AMQPMessageHandler;
import org.renci.databridge.formatter.*;
import org.renci.databridge.persistence.metadata.MetadataObject;
import org.renci.databridge.persistence.metadata.MetadataDAOFactory;
import org.renci.databridge.persistence.metadata.CollectionDAO;
import org.renci.databridge.persistence.metadata.CollectionTransferObject;
import org.renci.databridge.persistence.metadata.FileDAO;
import org.renci.databridge.persistence.metadata.FileTransferObject;
import org.renci.databridge.persistence.metadata.VariableDAO;
import org.renci.databridge.persistence.metadata.VariableTransferObject;
import org.renci.databridge.persistence.metadata.SignatureProcessor;
import org.renci.databridge.message.DatabridgeResultsMessage;
import org.renci.databridge.message.IngestMetadataMessage;
import org.renci.databridge.message.IngestListenerMessage;
import org.renci.databridge.message.ProcessedMetadataToMetadataDB;
import org.renci.databridge.persistence.metadata.*;

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
  public static final String SUCCESS_MESSAGE = "DataBridge_OK";

  public IngestMetadataAMQPMessageHandler (int dbType, String dbName, String dbHost, int dbPort, 
                                           String dbUser, String dbPwd, String pathToAmqpPropsFile) { 

    MetadataDAOFactory mdf = 
       MetadataDAOFactory.getMetadataDAOFactory (dbType, dbName, dbHost, dbPort, dbUser, dbPwd);
    this.metadataDAOFactory = mdf;
    this.pathToAmqpPropsFile = pathToAmqpPropsFile;
  }

  /**
   * Insert the ingest instance record into the mongo database.
   * @param theFactory Used to instantiate the needed IngestInstanceDAO object
   * @param stringHeaders A map of the headers provided in the message
   * @param dataSource The souce of the ingested data
   */
  public DatabridgeResultsMessage insertIngestInstanceRecord(MetadataDAOFactory theFactory, 
                                                             Map<String, String> stringHeaders,
                                                             String dataSource) {
      DatabridgeResultsMessage results = new DatabridgeResultsMessage();

      // Set up the transfer object
      IngestInstanceTransferObject theIngestInstance = new IngestInstanceTransferObject();
      theIngestInstance.setClassName(stringHeaders.get(IngestMetadataMessage.CLASS));
      theIngestInstance.setNameSpace(stringHeaders.get(IngestMetadataMessage.NAME_SPACE));
      theIngestInstance.setInput(dataSource);
      theIngestInstance.setParams(stringHeaders.get(IngestMetadataMessage.PARAMS));
      boolean fireEvent = new Boolean (stringHeaders.get (IngestMetadataMessage.FIRE_EVENT)).booleanValue ();
      theIngestInstance.setFireEvent(fireEvent);

      // Get the needed DAO
      IngestInstanceDAO theDAO = theFactory.getIngestInstanceDAO();
      try {
         // let's find the highest version for this combination of nameSpace, className and method (if any)
         HashMap<String, String> versionMap = new HashMap<String, String>();
         versionMap.put("nameSpace", theIngestInstance.getNameSpace());
         versionMap.put("className", theIngestInstance.getClassName());

         HashMap<String, String> sortMap = new HashMap<String, String>();
         sortMap.put("version", IngestInstanceDAO.SORT_DESCENDING);
         Integer limit = new Integer(1);

         // This is for the case of no previous instance
         theIngestInstance.setVersion(1);
         Iterator<IngestInstanceTransferObject> versionIterator =
             theDAO.getIngestInstances(versionMap, sortMap, limit);
         if (versionIterator.hasNext()) {
            // Found a previous instance
            IngestInstanceTransferObject prevInstance = versionIterator.next();
            theIngestInstance.setVersion(prevInstance.getVersion() + 1);
         }
         boolean result = theDAO.insertIngestInstance(theIngestInstance);
         if (result) {
            results.setSuccess();
            results.setResults("Can't insert ingest instance");
         }
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't insert ingest instance");
         results.setError();
         results.setResults("Can't insert ingest instance");
      }
      return results;
  }

  /**
   * Insert the signature instance record into the mongo database.
   * @param theFactory Used to instantiate the needed IngestInstanceDAO object
   * @param stringHeaders A map of the headers provided in the message
   * @param dataSource The souce of the ingested data
   */
  public DatabridgeResultsMessage insertSignatureInstanceRecord(MetadataDAOFactory theFactory, 
                                                                Map<String, String> stringHeaders) {
      DatabridgeResultsMessage results = new DatabridgeResultsMessage();

      // Set up the transfer object
      SignatureInstanceTransferObject theSignatureInstance = new SignatureInstanceTransferObject();
      theSignatureInstance.setClassName(stringHeaders.get(IngestMetadataMessage.CLASS));
      theSignatureInstance.setSourceNameSpace(stringHeaders.get(IngestMetadataMessage.SOURCE_NAME_SPACE));
      theSignatureInstance.setTargetNameSpace(stringHeaders.get(IngestMetadataMessage.TARGET_NAME_SPACE));
      theSignatureInstance.setParams(stringHeaders.get(IngestMetadataMessage.PARAMS));
      boolean fireEvent = new Boolean (stringHeaders.get (IngestMetadataMessage.FIRE_EVENT)).booleanValue ();
      theSignatureInstance.setFireEvent(fireEvent);

      // Get the needed DAO
      SignatureInstanceDAO theDAO = theFactory.getSignatureInstanceDAO();
      try {
         // let's find the highest version for this combination of nameSpace, className and method (if any)
         HashMap<String, String> versionMap = new HashMap<String, String>();
         versionMap.put("sourceNameSpace", theSignatureInstance.getSourceNameSpace());
         versionMap.put("targetNameSpace", theSignatureInstance.getTargetNameSpace());
         versionMap.put("className", theSignatureInstance.getClassName());

         HashMap<String, String> sortMap = new HashMap<String, String>();
         sortMap.put("version", SignatureInstanceDAO.SORT_DESCENDING);
         Integer limit = new Integer(1);

         // This is for the case of no previous instance
         theSignatureInstance.setVersion(1);
         Iterator<SignatureInstanceTransferObject> versionIterator =
             theDAO.getSignatureInstances(versionMap, sortMap, limit);
         if (versionIterator.hasNext()) {
            // Found a previous instance
            SignatureInstanceTransferObject prevInstance = versionIterator.next();
            theSignatureInstance.setVersion(prevInstance.getVersion() + 1);
         }
         boolean result = theDAO.insertSignatureInstance(theSignatureInstance);
         if (result) {
            results.setSuccess();
            results.setResults("Can't insert signature instance");
         }
      } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't insert signature instance");
         results.setError();
         results.setResults("Can't insert signature instance");
      }
      return results;
  }
  @Override
  public void handle (AMQPMessage amqpMessage, Object extra) throws Exception {
     Map<String, String> stringHeaders = amqpMessage.getStringHeaders ();
     this.logger.log (Level.INFO, "headers: " + stringHeaders);
     String messageName = stringHeaders.get(IngestMetadataMessage.NAME);
     DatabridgeResultsMessage results = null;

     if (null == messageName) {
        logger.log(Level.WARNING, "messageName is missing");
     } else if
         (messageName.compareTo(IngestMetadataMessage.INSERT_METADATA_JAVA_URI_METADATADB) == 0) {
        results = processInsertMetadataMessage(stringHeaders, extra);
     } else if
         (messageName.compareTo(IngestMetadataMessage.INSERT_METADATA_JAVA_FILES_METADATADB) == 0) {
        results = processInsertMetadataFilesMessage(stringHeaders, extra);
     } else if
         (messageName.compareTo(IngestMetadataMessage.INSERT_METADATA_JAVA_FILEWITHPARAMS_METADATADB) == 0) {
        results = processInsertMetadataFileWithParamsMessage(stringHeaders, extra);
     } else if
         (messageName.compareTo(IngestMetadataMessage.INSERT_METADATA_JAVA_BINARYFILES_METADATADB) == 0) {
        results = processInsertBinaryMetadataFilesMessage(stringHeaders, extra);
     } else if
         (messageName.compareTo(IngestMetadataMessage.CREATE_METADATA_SIGNATURE_JAVA_METADATADB) == 0) {
        results = createMetadataSignatureJavaMetadataDbMessage(stringHeaders, extra);
     } else {
         logger.log(Level.WARNING, "unimplemented messageName: " + messageName);
     }
  }

  /**
   * Handle the INSERT_METADATA_JAVA_URI_METADATADB message.  
   * @param stringHeaders A map of the headers provided in the message
   * @param extra An object containing the needed DAO objects
   */
    public DatabridgeResultsMessage processInsertMetadataMessage(Map<String, String> stringHeaders, 
                                                                 Object extra) {

       String className = stringHeaders.get (IngestMetadataMessage.CLASS);
       String nameSpace = stringHeaders.get (IngestMetadataMessage.NAME_SPACE);
       boolean fireEvent = new Boolean (stringHeaders.get (IngestMetadataMessage.FIRE_EVENT)).booleanValue ();
       String inputURI = stringHeaders.get (IngestMetadataMessage.INPUT_URI);
       DatabridgeResultsMessage theInsertResult = new DatabridgeResultsMessage();

       // @todo add a message NAME demux like in RelevanceEngineMessageHandler

       // instantiate third-party MetadataFormatter implementation 
       MetadataFormatter mf = null;
       try {
          mf = (MetadataFormatter) Class.forName (className).newInstance (); 
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't instantiate class " + className);
         e.printStackTrace();
         return new DatabridgeResultsMessage(false, "Can't instantiate class " + className);
      }

       mf.setLogger (this.logger);
       byte [] bytes = null;
       try {
          bytes = get (inputURI);
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't read the inputURI:  " + inputURI);
         e.printStackTrace();
         return new DatabridgeResultsMessage(false, "Can't read the inputURI:  " + inputURI);
      }

       // dispatch to third-party formatter 
       List<MetadataObject> metadataObjects = null;
       try {
           metadataObjects = mf.format (bytes);
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't perform format operation");
         e.printStackTrace();
         return new DatabridgeResultsMessage(false, "Can't perform format operation");
       }

       for (MetadataObject mo : metadataObjects) {
         try {
           persist (mo, nameSpace);
           this.logger.log (Level.FINE, "Inserted MetadataObject.");
         } catch (Exception e) {
           this.logger.log (Level.SEVERE, "Can't insert MetadataObject.");
           e.printStackTrace();
           return new DatabridgeResultsMessage(false, "Can't insert MetadataObject.");
         }
       }

       // Add the Ingest Instance record.
       theInsertResult = insertIngestInstanceRecord(this.metadataDAOFactory, stringHeaders, inputURI);
       if (theInsertResult.isSuccess() == false) {
           return new DatabridgeResultsMessage(false, "Can't insert the IngestInstance Record.");
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

       return new DatabridgeResultsMessage(true, this.SUCCESS_MESSAGE);
    }


  /**
   * Handle the INSERT_METADATA_JAVA_FILES_METADATADB message.  
   * @param stringHeaders A map of the headers provided in the message
   * @param extra An object containing the needed DAO objects
   */
    public DatabridgeResultsMessage processInsertMetadataFilesMessage(Map<String, String> stringHeaders, 
                                                                      Object extra) {

       String className = stringHeaders.get (IngestMetadataMessage.CLASS);
       String nameSpace = stringHeaders.get (IngestMetadataMessage.NAME_SPACE);
       boolean fireEvent = new Boolean (stringHeaders.get (IngestMetadataMessage.FIRE_EVENT)).booleanValue ();
       String inputDir = stringHeaders.get (IngestMetadataMessage.INPUT_DIR);

       // instantiate third-party MetadataFormatter implementation 
       MetadataFormatter mf = null;
       try {
          mf = (MetadataFormatter) Class.forName (className).newInstance (); 
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't instantiate class " + className);
         e.printStackTrace();
         return new DatabridgeResultsMessage(false, "Can't instantiate class " + className);
      }

       mf.setLogger (this.logger);
       byte [] bytes = null;
       this.logger.log (Level.INFO, "inputDir token:  " + IngestMetadataMessage.INPUT_DIR);
       this.logger.log (Level.INFO, "inputDir:  " + inputDir);
      
       try {
          bytes = mf.getBytes((Object) inputDir);
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Problems with the inputDir:  " + inputDir, e );
         return new DatabridgeResultsMessage(false, "Problems with the inputDir:  " + inputDir);
      }

       // dispatch to third-party formatter 
       List<MetadataObject> metadataObjects = null;
       try {
           metadataObjects = mf.format (bytes);
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't perform format operation", e);
         return new DatabridgeResultsMessage(false, "Can't perform format operation");
       }

       for (MetadataObject mo : metadataObjects) {
         try {
           persist (mo, nameSpace);
           this.logger.log (Level.FINE, "Inserted MetadataObject.");
         } catch (Exception e) {
           this.logger.log (Level.SEVERE, "Can't insert MetadataObject.", e);
           return new DatabridgeResultsMessage(false, "Can't insert MetadataObject.");
         }
       }

       // Add the Ingest Instance record.
       DatabridgeResultsMessage theInsertResult = 
          insertIngestInstanceRecord(this.metadataDAOFactory, stringHeaders, inputDir);
       if (theInsertResult.isSuccess() == false) {
           return new DatabridgeResultsMessage(false, "Can't insert the IngestInstance Record.");
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

       return new DatabridgeResultsMessage(true, this.SUCCESS_MESSAGE);
    }

  /**
   * Handle the INSERT_METADATA_JAVA_BINARYFILES_METADATADB message.  
   * @param stringHeaders A map of the headers provided in the message
   * @param extra An object containing the needed DAO objects
   */
    public DatabridgeResultsMessage 
       processInsertBinaryMetadataFilesMessage(Map<String, String> stringHeaders, Object extra) {

       String className = stringHeaders.get (IngestMetadataMessage.CLASS);
       String nameSpace = stringHeaders.get (IngestMetadataMessage.NAME_SPACE);
       boolean fireEvent = new Boolean (stringHeaders.get (IngestMetadataMessage.FIRE_EVENT)).booleanValue ();
       String inputDir = stringHeaders.get (IngestMetadataMessage.INPUT_DIR);

       // instantiate third-party BinaryIngestor implementation 
       BinaryIngester ingester = null;
       try {
          ingester = (BinaryIngester) Class.forName (className).newInstance (); 
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't instantiate class " + className);
         e.printStackTrace();
         return new DatabridgeResultsMessage(false, "Can't instantiate class " + className);
      }

       ingester.setLogger (this.logger);
       this.logger.log (Level.INFO, "inputDir token:  " + IngestMetadataMessage.INPUT_DIR);
       this.logger.log (Level.INFO, "inputDir:  " + inputDir);
      
       // dispatch to third-party formatter 
       List<MetadataObject> metadataObjects = null;
       try {
           metadataObjects = ingester.binaryToMetadata (inputDir);
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't perform format operation", e);
         return new DatabridgeResultsMessage(false, "Can't perform format operation");
       }

       for (MetadataObject mo : metadataObjects) {
         try {
           persist (mo, nameSpace);
           this.logger.log (Level.FINE, "Inserted MetadataObject.");
         } catch (Exception e) {
           this.logger.log (Level.SEVERE, "Can't insert MetadataObject.", e);
           return new DatabridgeResultsMessage(false, "Can't insert MetadataObject.");
         }
       }

       // Add the Ingest Instance record.
       DatabridgeResultsMessage theInsertResult = 
          insertIngestInstanceRecord(this.metadataDAOFactory, stringHeaders, inputDir);
       if (theInsertResult.isSuccess() == false) {
           return new DatabridgeResultsMessage(false, "Can't insert the IngestInstance Record.");
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
       return new DatabridgeResultsMessage(true, this.SUCCESS_MESSAGE);
    }

  /**
   * Handle the INSERT_METADATA_JAVA_FILEWITHPARAMS_METADATADB message.
   * @param stringHeaders A map of the headers provided in the message
   * @param extra An object containing the needed DAO objects
   * @return A DatabridgeResultsMessage containing the results of the operation
   */
    public DatabridgeResultsMessage 
       processInsertMetadataFileWithParamsMessage(Map<String, String> stringHeaders, Object extra) {

       String className = stringHeaders.get (IngestMetadataMessage.CLASS);
       String nameSpace = stringHeaders.get (IngestMetadataMessage.NAME_SPACE);
       boolean fireEvent = new Boolean (stringHeaders.get (IngestMetadataMessage.FIRE_EVENT)).booleanValue ();
       String inputFile = stringHeaders.get (IngestMetadataMessage.INPUT_FILE);
       String params = stringHeaders.get (IngestMetadataMessage.PARAMS);

      // The params could be null.  This isn't neccessarily the most elegant way to 
      // handle this, but it allows the lower level code to function correctly.
      if (params == null) {
         params = new String("");
      }

       // instantiate third-party MetadataFormatter implementation 
       MetadataFormatter mf = null;
       try {
          mf = (MetadataFormatter) Class.forName (className).newInstance (); 
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't instantiate class " + className);
         e.printStackTrace();
         return (new DatabridgeResultsMessage(false, "Can't instantiate class " + className));
      }

       mf.setLogger (this.logger);
       byte [] bytes = null;
       this.logger.log (Level.INFO, "input file:  " + IngestMetadataMessage.INPUT_FILE);
      
       try {
          String filePlusParams = inputFile + "&" + params;
          this.logger.log (Level.INFO, "filePLusParams:  " + filePlusParams);
          bytes = mf.getBytes((Object) filePlusParams);
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Problems with the input file:  " + inputFile, e );
         return (new DatabridgeResultsMessage(false, "Problems with the input file:  " + inputFile));
      }

       // dispatch to third-party formatter 
       List<MetadataObject> metadataObjects = null;
       try {
           metadataObjects = mf.format (bytes);
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't perform format operation", e);
         return (new DatabridgeResultsMessage(false, "Can't perform format operation"));
       }

       for (MetadataObject mo : metadataObjects) {
         try {
           persist (mo, nameSpace);
           this.logger.log (Level.FINE, "Inserted MetadataObject.");
         } catch (Exception e) {
           this.logger.log (Level.SEVERE, "Can't insert MetadataObject.", e);
           return (new DatabridgeResultsMessage(false, "Can't insert MetadataObject."));
         }
       }

       // Add the Ingest Instance record.
       DatabridgeResultsMessage theInsertResult = 
          insertIngestInstanceRecord(this.metadataDAOFactory, stringHeaders, inputFile);
       if (theInsertResult.isSuccess() == false) {
           return new DatabridgeResultsMessage(false, "Can't insert the IngestInstance Record.");
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
       return (new DatabridgeResultsMessage(true, "Metadata successfully inserted"));
    }

  /**
   * Handle the CREATE_METADATA_SIGNATURE_JAVA_METADATADB
   * @param stringHeaders A map of the headers provided in the message
   * @param extra An object containing the needed DAO objects
   */
    public DatabridgeResultsMessage 
       createMetadataSignatureJavaMetadataDbMessage(Map<String, String> stringHeaders, Object extra) {

       String className = stringHeaders.get (IngestMetadataMessage.CLASS);
       String sourceNameSpace = stringHeaders.get (IngestMetadataMessage.SOURCE_NAME_SPACE);
       String targetNameSpace = stringHeaders.get (IngestMetadataMessage.TARGET_NAME_SPACE);
       boolean fireEvent = new Boolean (stringHeaders.get (IngestMetadataMessage.FIRE_EVENT)).booleanValue ();
       String params = stringHeaders.get (IngestMetadataMessage.PARAMS);

      // The params could be null.  This isn't neccessarily the most elegant way to 
      // handle this, but it allows the lower level code to function correctly.
      if (params == null) {
         params = new String("");
      }

       // instantiate third-party SignatureProcess
       SignatureProcessor theProcessor = null;
       try {
          theProcessor = (SignatureProcessor) Class.forName (className).newInstance (); 
       } catch (Exception e) {
         this.logger.log (Level.SEVERE, "Can't instantiate class " + className);
         e.printStackTrace();
         return new DatabridgeResultsMessage(false, "Can't instantiate class " + className);
      }

       theProcessor.setLogger (this.logger);

       // For each collection in the name space we call the signature processor, than persist the result.
       CollectionDAO cd = this.metadataDAOFactory.getCollectionDAO();
       FileDAO fd = this.metadataDAOFactory.getFileDAO();
       HashMap<String, String> searchMap = new HashMap<String, String>();
       searchMap.put("nameSpace", sourceNameSpace);
       Iterator<CollectionTransferObject> theCollections = cd.getCollections(searchMap);
       try {
          while (theCollections.hasNext()) {
             CollectionTransferObject inputCTO = theCollections.next();

             // We have to add the FileTransferObjects manually, which may be a weakness in the
             // CollectionDAO 
             ArrayList<FileTransferObject> fileArrayList = new ArrayList<FileTransferObject>();
             Iterator<FileTransferObject> theFiles = fd.getFiles(inputCTO);
             while (theFiles.hasNext()) {
                fileArrayList.add(theFiles.next());
             }
             // Add the files to the CTO
             inputCTO.setFileList(fileArrayList);

             CollectionTransferObject returnCTO = theProcessor.extractSignature(inputCTO, params);
             MetadataObject thisMeta = new MetadataObject();
             thisMeta.setCollectionTransferObject(returnCTO);
             thisMeta.setFileTransferObjects(returnCTO.getFileList());
             persist (thisMeta, targetNameSpace);
             this.logger.log (Level.FINE, "Inserted MetadataObject.");
           }
        } catch (Exception e) {
           this.logger.log (Level.SEVERE, "Can't insert MetadataObject.", e);
           return new DatabridgeResultsMessage(false, "Can't create the requested signature");
        }

       // Add the Signature Instance record.
       DatabridgeResultsMessage theInsertResult = 
           insertSignatureInstanceRecord(this.metadataDAOFactory, stringHeaders);
       if (theInsertResult.isSuccess() == false) {
           return new DatabridgeResultsMessage(false, "Can't insert the SignatureInstance Record.");
       }

// This doesn't quite make sense as written, maybe we should send something else out
// which is why I haven't deleted this code.
/*
       if (fireEvent) {
         // send ProcessedMetadataToMetadataDB message 
         AMQPComms ac = new AMQPComms (this.pathToAmqpPropsFile);
         String headers = ProcessedMetadataToMetadataDB.getSendHeaders (targetNameSpace);
         this.logger.log (Level.FINER, "Send headers: " + headers);
         ac.publishMessage (new AMQPMessage (), headers, true);
         ac.shutdownConnection ();     
         this.logger.log (Level.FINE, "Sent ProcessedMetadataToMetadataDB message.");
       }
 */
       return (new DatabridgeResultsMessage(true, "Signature successfully inserted"));
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
    if (ftos != null) {
       for (FileTransferObject fto : ftos) {
         fto.setNameSpace (nameSpace);
         fto.setCollectionDataStoreId (cto.getDataStoreId ());
         FileDAO fd = this.metadataDAOFactory.getFileDAO ();
         fd.insertFile (fto);
         this.logger.log (Level.FINE, "Inserted FTO id: '" + fto.getDataStoreId () + "'");
         List<VariableTransferObject> vtos = fto.getVariableList ();
         if (vtos != null) {
            for (VariableTransferObject vto : vtos) {
              vto.setFileDataStoreId(fto.getDataStoreId());
              VariableDAO vd = this.metadataDAOFactory.getVariableDAO ();
              vd.insertVariable (vto);
              this.logger.log (Level.FINE, "Inserted VTO id: '" + vto.getDataStoreId () + "'");
            }
         }
       }
    }
/* This code is commented out because it doesn't seem to properly preserve the relationship
   hierarchy between files and variables. Variables exist inside of files. See the code above 
    List<VariableTransferObject> vtos = metadataObject.getVariableTransferObjects ();
    if (vtos != null) {
       for (VariableTransferObject vto : vtos) {
         VariableDAO vd = this.metadataDAOFactory.getVariableDAO ();
         vd.insertVariable (vto);
         this.logger.log (Level.FINE, "Inserted VTO id: '" + vto.getDataStoreId () + "'");
       }
    }
 */

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
