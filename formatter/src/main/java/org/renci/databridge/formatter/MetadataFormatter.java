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
   * @param bytes "Document" that implementor understands the format for.
   * @return the metadata elements from the bytes.
   */
  public List<MetadataObject> format (byte [] bytes) throws FormatterException;

  /**
   * Set a logger (e.g., parent's logger).
   */
  public void setLogger (Logger logger);
 
}
