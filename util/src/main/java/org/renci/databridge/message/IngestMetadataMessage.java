package org.renci.databridge.message;
import org.renci.databridge.util.AMQPMessageType;

/**
 * @author mrshoffn
 */
public class IngestMetadataMessage implements AMQPMessageType {

  public static final String bindHeaders = "type:databridge;subtype:ingestmetadata;x-match:all";

  public static final String NAME = "name";
  public static final String CLASS = "className";
  public static final String NAME_SPACE = "nameSpace";
  public static final String SOURCE_NAME_SPACE = "sourceNameSpace";
  public static final String TARGET_NAME_SPACE = "targetNameSpace";
  public static final String FIRE_EVENT = "fireEvent";
  public static final String INPUT_URI = "inputURI";
  public static final String INPUT_FILE = "inputFile";
  public static final String INPUT_DIR = "inputDir";
  public static final String PARAMS = "params";

  // Message types for the ingest engine
  public static final String INSERT_METADATA_JAVA_URI_METADATADB = 
                            "Insert.Metadata.Java.URI.MetadataDB";
  public static final String INSERT_METADATA_JAVA_FILES_METADATADB = 
                            "Insert.Metadata.Java.Files.MetadataDB";
  public static final String INSERT_METADATA_JAVA_BINARYFILES_METADATADB = 
                            "Insert.Metadata.Java.BinaryFiles.MetadataDB";
  public static final String INSERT_METADATA_JAVA_FILEWITHPARAMS_METADATADB = 
                            "Insert.Metadata.Java.FileWithParams.MetadataDB";
  public static final String INSERT_METADATA_JAVA_FILEWITHPARAMS_METADATADB_RPC = 
                            "Insert.Metadata.Java.FileWithParams.MetadataDB.RPC";
  public static final String CREATE_METADATA_SIGNATURE_JAVA_METADATADB = 
                            "Create.Metadata.Signature.Java.MetadataDB";
  @Override
  public String getBindHeaders () {
     return bindHeaders;
  }

}
