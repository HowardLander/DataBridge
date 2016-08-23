#!/usr/bin/env python
import sys, getopt
import csv
import scipy.sparse
from scipy.sparse import csr_matrix
import numpy as np
from sklearn.cluster import SpectralClustering
from sklearn import metrics
from sklearn.datasets.samples_generator import make_blobs
from sklearn.preprocessing import StandardScaler
import subprocess

def file_len(fname):
    p = subprocess.Popen(['wc', '-l', fname], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    result, err = p.communicate()
    if p.returncode != 0:
       raise IOError(err)
    return int(result.strip().split()[0])

def main(argv):
   inputFile = ''
   outputFile = ''
   inputFile = sys.argv[1]
   outputFile = sys.argv[2]
   clusterList = []
   print 'Input file is "', inputFile
   nLines = file_len(inputFile)
   print 'Lines in Input file is ', nLines
   print 'Output file is "', outputFile

   for i in range(nLines):
       clusterList.append(0)

   with open(inputFile, 'rb') as csvfile:
      csvReader = csv.reader(csvfile, delimiter=',',quotechar='|')
      for row in csvReader:
         print row[1]
         thisCluster = int(row[1])
         clusterList[thisCluster] += 1

   with open(outputFile, 'wb') as csvoutfile:
      csvWriter = csv.writer(csvoutfile, delimiter=',',quotechar='|')
      for i in range(0, nLines):
          if clusterList[i] > 0:
             csvWriter.writerow([i, clusterList[i]])

if __name__ == "__main__":
   main(sys.argv[1:])

   
