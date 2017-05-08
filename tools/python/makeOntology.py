#!/usr/bin/env python
from rdflib.namespace import OWL, RDF, RDFS
from rdflib import Graph, Literal, Namespace, URIRef
import uuid
import xlrd
from xlrd import open_workbook
import sys

DBfN   = Namespace("http:/maven.renci.org/ontologies/databridgeforneuroscience.owl")
graph = Graph()
Clinical = Literal("Clinical")
graph.add((Clinical, RDF.type, OWL.Class))
graph.add((Clinical, RDFS.subClassOf, OWL.Thing))
graph.add((Clinical, RDFS.label, Literal(uuid.uuid4())))
graph.add((Clinical, RDFS.comment, Literal("Highest level of ontology")))
workbook = sys.argv[1]
outFile = sys.argv[2]

wb = open_workbook(workbook)
for s in wb.sheets():
   if (s.name == 'SCZ Clin Model Groups'):
       for row in range (2,54):
          if (s.cell(row,1).value != ""):
             thisValue = (s.cell(row,0).value)
             theseValues = thisValue.split('>')
             print "length of theseValue: " + str(len(theseValues))
             thisLiteral = Literal(theseValues[1]);
             if (thisLiteral, RDFS.subClassOf, Clinical) in graph:
                print "already added"
             else:
                graph.add((thisLiteral, RDF.type, OWL.Class))
                graph.add((thisLiteral, RDFS.subClassOf, Clinical))
                graph.add((thisLiteral, RDFS.label, Literal(uuid.uuid4())))
                graph.add((thisLiteral, RDFS.comment, Literal(theseValues[1])))
             if (len(theseValues) >= 3):
                previousLiteral = thisLiteral
                thisLiteral = Literal(theseValues[2]);
                if (thisLiteral, RDFS.subClassOf, previousLiteral) in graph:
                   print "already added"
                else:
                   graph.add((thisLiteral, RDF.type, OWL.Class))
                   graph.add((thisLiteral, RDFS.subClassOf, previousLiteral))
                   graph.add((thisLiteral, RDFS.label, Literal(uuid.uuid4())))
                   graph.add((thisLiteral, RDFS.comment, Literal(theseValues[2])))
             if (len(theseValues) >= 4):
                previousLiteral = thisLiteral
                thisLiteral = Literal(theseValues[3]);
                if (thisLiteral, RDFS.subClassOf, previousLiteral) in graph:
                   print "already added"
                else:
                   graph.add((thisLiteral, RDF.type, OWL.Class))
                   graph.add((thisLiteral, RDFS.subClassOf, previousLiteral))
                   graph.add((thisLiteral, RDFS.label, Literal(uuid.uuid4())))
                   graph.add((thisLiteral, RDFS.comment, Literal(theseValues[3])))

graph.bind("owl", OWL)
graph.bind("DBfN", DBfN)
graph.serialize(destination=outFile, format='turtle')
