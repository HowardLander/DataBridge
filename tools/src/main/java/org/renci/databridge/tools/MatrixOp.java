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

 class MatrixOp implements MatrixProcedure {
     public void apply(int i, int j, double value) {
        if (Double.isNaN(value)) {
            AnalyzeSimilarityFile.zeros ++;
        } else if (value > 0.) {
            AnalyzeSimilarityFile.total ++;
        } else {
            AnalyzeSimilarityFile.zeros ++;
        }
     }
     public int getTotal() {
        return AnalyzeSimilarityFile.total;
     }
 }
