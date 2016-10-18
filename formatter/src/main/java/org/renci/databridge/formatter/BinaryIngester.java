package org.renci.databridge.formatter;

import java.util.logging.Logger;
import java.util.List;

import org.renci.databridge.persistence.metadata.MetadataObject;

/**
 * Interface for importing metadata from binary files.
 *
 * Implementors must have no-arg constructor so as to be instantiated by name.
 * 
 */
public interface BinaryIngester {

  /**
   * @param input A URI for a directory containing binary files from which to extract a Metadata Object
   * @return byte array representing the objects.
   */

  public List<MetadataObject> binaryToMetadata (String binaryURI) throws FormatterException;

  /**
   * Set a logger (e.g., parent's logger).
   */
  public void setLogger (Logger logger);
 
}
