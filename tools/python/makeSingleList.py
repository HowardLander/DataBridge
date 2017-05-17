#!/usr/bin/env python
from rdflib.namespace import OWL, RDF, RDFS
from rdflib import Graph, Literal, Namespace, URIRef
import uuid
from xlrd import open_workbook
import sys
import io, json
from collections import defaultdict

workbook = sys.argv[1]
outFile = sys.argv[2]
comboMap = {}
outputList = defaultdict(list)
print workbook
wb = open_workbook(workbook)
for s in wb.sheets():
   if (s.name == ' One Patient Per Study'):
       # for each study, we want to print the first instance of each
       # assessment
       previousCombo = 'none'
       for row in range (s.nrows):
          thisStudy = (s.cell(row,1).value)
          thisSource = (s.cell(row,0).value)
          thisSite = (s.cell(row,2).value)
          thisAssessmentPath = (s.cell(row,5).value)
          theseAssessmentValues = thisAssessmentPath.split('\u003e')
          thisAssessment = theseAssessmentValues[-1]
          thisCombo = thisStudy + thisAssessment
          #if (thisCombo != previousCombo):
          test = comboMap.has_key(thisCombo)
          if (test == False):
                previousCombo = thisCombo
                comboMap[thisCombo] = True
                print 'Found new combo: ', thisStudy, ' ', thisAssessment
                outputList[thisStudy].append(thisAssessment)

with open(outFile, 'w') as outfile:
     json.dump(outputList, outfile, sort_keys = True, indent = 4, ensure_ascii = False)

