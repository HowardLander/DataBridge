#!/usr/bin/env python
from rdflib.namespace import OWL, RDF, RDFS
from rdflib import Graph, Literal, Namespace, URIRef
import uuid
import xlrd
from xlrd import open_workbook
import sys

def addANode(graph, subject, object):
   subjectURI = URIRef(DBfN[subject])
   objectURI = URIRef(DBfN[object])
   if (subjectURI, RDFS.subClassOf, objectURI) in graph:
      print subject, "already added"
   else:
      graph.add((subjectURI, RDF.type, OWL.Class))
      graph.add((subjectURI, RDFS.subClassOf, objectURI))
      graph.add((subjectURI, RDFS.label, Literal(subject)))
      graph.add((subjectURI, RDFS.comment, Literal(subject)))

DBfN   = Namespace("http://maven.renci.org/ontologies/databridgeforneuroscience/")
graph = Graph()
#Clinical = URIRef("Clinical")
graph.add((DBfN.Clinical, RDF.type, OWL.Class))
graph.add((DBfN.Clinical, RDFS.subClassOf, OWL.Thing))
graph.add((DBfN.Clinical, RDFS.label, Literal("Clinical")))
graph.add((DBfN.Clinical, RDFS.comment, Literal("Highest level of ontology")))
workbook = sys.argv[1]
outFile = sys.argv[2]
lastRow = int(sys.argv[3])

wb = open_workbook(workbook)
for s in wb.sheets():
   if (s.name == 'SCZ Clin Model Groups'):
       for row in range (2,lastRow):
          if (s.cell(row,1).value != ""):
             thisValue = (s.cell(row,0).value)
             theseValues = thisValue.split('>')
             addANode(graph, theseValues[1], "Clinical")
             if (len(theseValues) >= 3):
                addANode(graph, theseValues[2], theseValues[1])
             if (len(theseValues) >= 4):
                addANode(graph, theseValues[3], theseValues[2])

graph.bind("owl", OWL)
graph.bind("DBfN", DBfN)
graph.serialize(destination=outFile, format='turtle')
