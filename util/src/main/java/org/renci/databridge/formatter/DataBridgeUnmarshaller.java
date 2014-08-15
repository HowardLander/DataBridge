package org.renci.databridge.formatter;

import java.io.StringReader;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import org.renci.databridge.formatter.oaipmh.OAIPMHtype;

/**
 * Utility class to unmarshal XML files based on XSD schemas.
 * 
 * @author mrshoffn
 */
public class DataBridgeUnmarshaller {

  public static OAIPMHtype unmarshalOAIPMHtype (String xml) throws JAXBException {

    JAXBContext jc = JAXBContext.newInstance (OAIPMHtype.class);
    Unmarshaller unmarshaller = jc.createUnmarshaller ();
    StreamSource ss = new StreamSource (new StringReader (xml));
    JAXBElement<OAIPMHtype> root = unmarshaller.unmarshal (ss, OAIPMHtype.class);
    return root.getValue (); 

  }
 
}
