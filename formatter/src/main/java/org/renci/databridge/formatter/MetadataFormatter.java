package org.renci.databridge.formatter;

import org.renci.databridge.persistence.metadata.CollectionTransferObject;

/**
 * Interface for all metadata formatters. 
 * Formatter implementations are instantiated and invoked by the Ingest Engine.
 * 
 * @author mrshoffn
 */
public interface MetadataFormatter {

  /**
   * @param bytes Array of bytes that the implementor knows how to format.
   */
  public CollectionTransferObject format (byte [] bytes) throws FormatterException;
 
}
