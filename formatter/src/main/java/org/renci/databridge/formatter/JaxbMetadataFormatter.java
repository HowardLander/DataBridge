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

/**
 * Abstract superclass for MetadataFormatters that use JAXB.
 * 
 * @author mrshoffn
 */
public abstract class JaxbMetadataFormatter implements MetadataFormatter {

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
      Unmarshaller unmarshaller = jc.createUnmarshaller ();
      StreamSource ss = new StreamSource (new StringReader (xml));
      Object o = unmarshaller.unmarshal (ss, contentClass);
      // @todo this is gross but JAXB returns some content object roots in a wrapper. May be fixable with JAXB configuration.
      if (o instanceof JAXBElement) {
        content = ((JAXBElement<X>) o).getValue ();
      } else {
        content = (X) o;
      }

    } catch (JAXBException je) {

      throw new FormatterException (je);

    }

    return content;

  }

  /**
   * @todo 1: fix. 2: probably should be factored up.
   * Input: Harris//hdl:1902.29/H-15085
   * Output: hdl.handle.net/1902.29/H-15085
   */
  public String constructUrl (String headerIdentifier) {

    return headerIdentifier; 

  }

}
