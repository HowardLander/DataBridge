Formatter package
=================

Formatters are third-party plug-ins for Databridge that map incoming metadata and data chunks to Databridge's metadata format.

Each formatter is a "module" consisting of a collection of JAR files that is loaded into the system in isolation from any others.  

Steps to create a new formatter:

1. ***

--- OAI-PMD formatter ------

Formatters are POJO classes that are mapped to XML documents. They are generated using JAXB against an XSD schema document. This happens during the generate-sources phase of the maven lifecycle. Then when a document that is an example of the XSD comes in to the formatter it is converted to an instance of the relevant POJOs.

Additional formatters can be added by:
  1. Add XSD to formatter/src/main/xsd.
  2. Add a new <execution> to jaxb2-maven-plugin in formatter/pom.xml to do each XSD separately and put it in its own generated package
  3. Create a new formatter.



