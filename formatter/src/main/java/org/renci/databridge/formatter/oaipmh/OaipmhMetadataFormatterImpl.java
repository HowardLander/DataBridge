package org.renci.databridge.formatter.oaipmh;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.bind.JAXBElement;

import org.renci.databridge.formatter.MetadataFormatter;
import org.renci.databridge.formatter.FormatterException;

import org.renci.databridge.persistence.metadata.CollectionTransferObject;

/**
 * MetadataFormatter implementation for OAI-PMH.
 
 * @author mrshoffn
 */
public class OaipmhMetadataFormatterImpl implements MetadataFormatter {

  private Logger logger = Logger.getLogger ("org.renci.databridge.formatter.oaipmh");

  @Override
  public CollectionTransferObject format (byte [] bytes) throws FormatterException {

    String metadataString = new String (bytes);
    this.logger.log (Level.FINER, "bytes: '" + metadataString + "'");

    CollectionTransferObject cto = new CollectionTransferObject ();

    OAIPMHtype ot = unmarshal (metadataString);
    ListRecordsType lrt = ot.getListRecords ();
    List<RecordType> lr = lrt.getRecord ();
    Iterator<RecordType> i = lr.iterator ();
    while (i.hasNext ()) {
      RecordType r = i.next ();
      HeaderType h = r.getHeader ();
      // Harris//hdl:1902.29/H-15085
      cto.setURL (constructUrl (h.getIdentifier ()));

 cto.setTitle ("title");
 cto.setDescription ("");
 cto.setProducer ("producer");
    cto.setSubject ("physics");
    // Map<String, String> extra = new HashMap<String, String> ();
    // extra.put ("", "");
    // cto.setExtra (cto);
    cto.setNameSpace ("test");
    cto.setVersion (1);

    }

    return cto;

  }

  protected OAIPMHtype unmarshal (String xml) throws FormatterException {

    OAIPMHtype ot = null;

    try { 

      JAXBContext jc = JAXBContext.newInstance (OAIPMHtype.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller ();
      StreamSource ss = new StreamSource (new StringReader (xml));
      JAXBElement<OAIPMHtype> root = unmarshaller.unmarshal (ss, OAIPMHtype.class);
      ot = root.getValue ();

    } catch (JAXBException je) {

      throw new FormatterException (je);

    }

    return ot;

  }


  /**
   * Input: Harris//hdl:1902.29/H-15085
   * Output: hdl.handle.net/1902.29/H-15085
   */
  protected String constructUrl (String headerIdentifier) {

return headerIdentifier; 

  }

}
