package org.renci.databridge.persistence.metadata;
import org.renci.databridge.persistence.metadata.*;
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

    CollectionTransferObject compareCollections(CollectionTransferObject theCollection, 
                              String params);
}
