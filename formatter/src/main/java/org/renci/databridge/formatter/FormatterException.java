package org.renci.databridge.formatter;

/**
 * Superclass for exceptions arising during formatting.
 * 
 * @author mrshoffn
 */
public class FormatterException extends Exception {

  public FormatterException (String message) {
    super (message);
  }

  public FormatterException (Throwable throwable) {
    super (throwable);
  }

}
