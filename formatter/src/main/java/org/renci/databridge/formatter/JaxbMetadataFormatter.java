package org.renci.databridge.formatter;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.StringReader;
import java.io.Serializable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.XMLConstants;
import java.io.File;
import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * Abstract superclass for MetadataFormatters that use JAXB.
 * 
 * @author mrshoffn
 */
public abstract class JaxbMetadataFormatter implements MetadataFormatter {

  protected Logger logger = Logger.getLogger ("org.renci.databridge.formatter");

  @Override
  public void setLogger (Logger logger) {
    this.logger = logger;
  }

  /**
   * "Flattens" content object from XmlMixed type to a string.
   * @returns first entry of list, cast to String, or null
   */
  public String flatten (List<Serializable> list) {
    String s = null;
    if (list != null && list.size () > 0) {
      s = (String) list.get (0);
    }
    return s;
  }

  /**
   * Unmarshalls a given content object root type from a string.
   *
   * @param xml The string.
   * @param contentClass The class of the root of the content tree that will be unmarshalled from the xml parameter.
   * @param contextClasses Needs a class object for the root of every static class hierarchy that the unmarshaller requires; for example JAXB-generated classes built elsewhere and imported. Must include the contentClass object as well.
   * @see https://jaxb.java.net/nonav/2.2.4/docs/api/javax/xml/bind/Unmarshaller.html
   */
  public <X> X unmarshal (String xml, Class<X> contentClass, Class... contextClasses) throws FormatterException {

    X content = null;
    try { 

      JAXBContext jc = JAXBContext.newInstance (contextClasses);
      Unmarshaller u = jc.createUnmarshaller ();

      File f = getValidationSchema ();
      if (f != null) {
        SchemaFactory sf = SchemaFactory.newInstance (XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema s = sf.newSchema (f);
        u.setSchema (s);
        u.setEventHandler (new ValidationEventHandlerImpl ());
      }

      StreamSource ss = new StreamSource (new StringReader (xml));
      Object o = u.unmarshal (ss, contentClass);
      content = (X) JAXBIntrospector.getValue (o);

    } catch (SAXException se) {
      throw new FormatterException (se);
    } catch (JAXBException je) {
      throw new FormatterException (je);
    } catch (IOException ie) {
      throw new FormatterException (ie); 
    }

    return content;

  }

  /**
   * Implementers must override to allow for possible validation.
   *
   * @returns a java.io.File that is an XML Schema (an XSD document) describing the format that the formatter understands for validation purposes. Returning a null means that no validation will be done.
   */ 
  protected abstract File getValidationSchema () throws IOException;

}
