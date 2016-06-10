package org.renci.databridge.formatter;

import java.util.logging.Logger;
import java.util.List;

import org.renci.databridge.persistence.metadata.MetadataObject;

/**
 * Interface for all metadata formatters. 
 *
 * Implementors must have no-arg constructor so as to be instantiated by name.
 * 
 * @author mrshoffn
 */
public interface MetadataFormatter {

  /**
   * @param input A class specific object that tells the function where to find the objects
   *              to turn into bytes.
   * @return byte array representing the objects.
   */

  public byte[] getBytes (Object input) throws FormatterException;
  /**
   * @param bytes "Document" that implementor understands the format for.
   * @return the metadata elements from the bytes.
   */
  public List<MetadataObject> format (byte [] bytes) throws FormatterException;

  /**
   * Set a logger (e.g., parent's logger).
   */
  public void setLogger (Logger logger);
 
}
