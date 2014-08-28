Formatter package
=================

Formatters are POJO classes that are mapped to XML documents. They are generated using JAXB against an XSD schema document. This happens during the generate-sources phase of the maven lifecycle. Then when a document that is an example of the XSD comes in to the formatter it is converted to an instance of the relevant POJOs.

Additional formatters can be added by:
  1. Add XSD to formatter/src/main/xsd.
  2. Add a new <execution> to jaxb2-maven-plugin in formatter/pom.xml to do each XSD separately and put it in its own generated package
  3. Create a new formatter.




