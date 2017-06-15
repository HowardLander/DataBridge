package org.renci.databridge.persistence.metadata;
import org.renci.databridge.persistence.metadata.*;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * This is the interface for classes that are executed by the relavance engine to extract
 * a signature from a collection transfer object.  The signature is still expressed as a 
 * collection transfer object so it can be stored back in the metadata database, in most
 * cases as a separate nameSpace.
 * 
 *  @param theCollection The CollectionTransferObject from which to extract a signature
 *  @params params Any class/instantiation parameters to be passed to the implementing class.
 *  @return A collection transfer object containing the extracted signature.
 * 
 * @author Howard Lander -RENCI (www.renci.org)
 * 
 */
public interface SignatureProcessor {

  /**
   * Process a CollectionTransferObject and return a CollectionTransferObject whose metadata
   * represents a signature derived from the original metadata.
   * 
   * @param theCollection The CTO representing the original metadata
   * @param params A string containing key value pairs in the format
   *
   *               key1,value1|key2,value2
   *
   *        These paramters are specific to the implementing class
   * 
   * @return A collection transfer object containing a metadata signature that the method invocation
   *         has produced from the original data
   */
   CollectionTransferObject extractSignature(CollectionTransferObject theCollection, String params);

  /**
   * Set a logger (e.g., parent's logger).
   */
  public void setLogger (Logger logger);
}
