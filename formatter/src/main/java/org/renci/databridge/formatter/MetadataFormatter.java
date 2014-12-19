package org.renci.databridge.formatter;

import java.util.List;

import org.renci.databridge.persistence.metadata.MetadataObject;

/**
 * Interface for all metadata formatters. 
 * 
 * @author mrshoffn
 */
public interface MetadataFormatter {

  /**
   * @param bytes "Document" that implementor understands the format for.
   * @returns the metadata elements from the bytes.
   */
  public List<MetadataObject> format (byte [] bytes) throws FormatterException;
 
}
