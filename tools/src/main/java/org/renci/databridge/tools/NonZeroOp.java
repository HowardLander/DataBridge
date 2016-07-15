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

 class NonZeroOp implements MatrixProcedure {
    public void apply(int i, int j, double value) {
       System.out.println("Hello from apply " + i + " " + j + " " + value);
    }
 }
