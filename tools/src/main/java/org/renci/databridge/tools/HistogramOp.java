package org.renci.databridge.tools;

import java.io.*;
import java.util.*;
import org.apache.commons.cli.*;
import org.renci.databridge.persistence.metadata.*;
import org.renci.databridge.util.*;
import org.renci.databridge.message.*;
import org.la4j.*;
import org.la4j.matrix.functor.*;
import org.la4j.vector.functor.*;
import java.nio.file.*;

 class HistogramOp implements MatrixProcedure {
    public void apply(int i, int j, double value) {
       int thisBin = (int) ((value * AnalyzeSimilarityFile.nBins));
       if (value >= 1.) {
          thisBin = AnalyzeSimilarityFile.nBins - 1;
       }
       if (value < 0.) {
          return;
       }
       if (value > 0.0) {
          AnalyzeSimilarityFile.nonZeros ++;
       }
       AnalyzeSimilarityFile.histogram[thisBin] ++;
    }
 }
