/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlsim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import sax.Parser;
import similarity.*;

/**
 *
 * @author Nerketur
 */
public class XMLSim {

    
    //private static double threshold = 0.5;
    private static String[] arr;
    private static double[][] data;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        Parser p = new Parser();
        //parseWithSax("harris.xml", p);
        parseWithSax("odum.xml", p);
        //parseWithSax("OAI_Odum_Harris.xml", p);
        System.out.println(p.getParsed());
        System.out.println("****************************");
        final HashMap<String, HashMap> keywords = new HashMap();
        copyMap(keywords, p.getParsed());
        Iterator<String> itK = keywords.keySet().iterator();
        while (itK.hasNext()) {
            keywords.get(itK.next()).remove("sumDscr");
        }
        HashMap<String, HashMap> sumDscr = new HashMap();
        copyMap(sumDscr, p.getParsed());
        itK = keywords.keySet().iterator();
        while (itK.hasNext()) {
            String ele = itK.next();
            Iterator itKe = keywords.get(ele).keySet().iterator();
            while (itKe.hasNext()) {
                sumDscr.get(ele).remove(itKe.next());
            }
        }
        System.out.println(keywords);
        System.out.println(sumDscr);
        double[][][] dataTmp = compare(keywords, sumDscr);
        data = dataTmp[2];
        //Update these numbers to avoid confusion if the array itself is changed.
//        //0, overlap
//        //x, eskin
//        //x, IOF
//        //1, OF
//        //x, Lin
//        //2, comprehensive
        arr = p.getParsed().keySet().toArray(new String[] {});
        StringBuilder sbFull = new StringBuilder();
        ArrayList result = shrink(arr, data);
        arr = (String[]) result.get(0);
        data = (double[][]) result.get(1);
        int maxLen = 0;
        for (String x : arr) {
            try {
                maxLen = Math.max(maxLen, x.length());
            } catch (Exception e) {
                //ignore
            }
        }
        sbFull.append("[");
       
        for (int i = 0; i < data.length; i++) {
            if (arr[i] == null)
                continue;
            StringBuilder sbLine = new StringBuilder();
            sbLine.append("[").append(arr[i]);
            for (int n = 0; n < maxLen - arr[i].length(); n++) {
                sbLine.append(" ");
            }
            sbLine.append(":");
            for (int j = 0; j < data[i].length; j++) {
                sbLine.append(" ").append(String.format("%.6f", data[i][j]));
            }
            sbLine.append("]");
            sbFull.append(sbLine).append("\r\n");
        }
        sbFull.append("]");
        PrintWriter writer = new PrintWriter("sims.txt", "UTF-8");
        writer.append(sbFull);
        writer.close();
    }
    
    private static void parseWithSax(String filename, DefaultHandler handler) {
        parseWithSax(filename, handler, handler);
    }
    private static void parseWithSax(String filename, DefaultHandler errHandler, DefaultHandler contHandler) {
        FileReader r = null;
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(errHandler);
            xr.setErrorHandler(contHandler);
            r = new FileReader(filename);
            xr.parse(new InputSource(r));
            //Here, everything parsed correctly.
            System.out.println("Parse successful");
        } catch (IOException | SAXException ex) {
            ex.printStackTrace(); //For now, we just do this.
            //TODO: Implement better error handling
            System.out.println("Parse Failed");
        } finally {
            try {
                r.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
    
    private static double[][][] compare(HashMap<String, HashMap> keywords, HashMap<String, HashMap> sumDscrs) {
        //For each survey, we create an ArrayList of ArrayLists of ArrayLists, with only the compared values.
        //We have a normalized version of each survey's results.
        Iterator<String> keysK = keywords.keySet().iterator();
        
        Collection<Collection<Collection<String>>> surveys = new ArrayList<>();
        
        while (keysK.hasNext()) {
            Collection<String> attrs = new ArrayList<>();
            Collection<Collection<String>> survey = new ArrayList<>();
            //This is every value for keywords
            String group = keysK.next();
            Iterator vals = null;
            try {
                vals = ((HashMap) keywords.get(group).values().iterator().next()).values().iterator();
            } catch (ClassCastException | NoSuchElementException e) {
                vals = keywords.get(group).values().iterator();
            }
            
            while (vals.hasNext()) {
                attrs.add((String)vals.next());
            }
            survey.add(attrs);
            attrs = new ArrayList<>();
            HashMap surAttr2 = sumDscrs.get(group);
            vals = surAttr2.keySet().iterator();
            while (vals.hasNext()) {
                String key = (String)vals.next();
                Object get = surAttr2.get(key);
                if (get instanceof String) {
                    String keyL = key;
                    while (keyL.charAt(keyL.length()-1) <= '9' && keyL.charAt(keyL.length()-1) >= '0')
                        keyL = keyL.substring(0, keyL.length()-1);
                    attrs.add(keyL + ": " + get);
                } else {
                    Iterator<String> it = ((HashMap)get).keySet().iterator();
                    while (it.hasNext()) {
                        key = it.next();
                        String keyL = key;
                        while (keyL.charAt(keyL.length()-1) <= '9' && keyL.charAt(keyL.length()-1) >= '0')
                            keyL = keyL.substring(0, keyL.length()-1);
                        attrs.add(keyL + ": " + ((HashMap)get).get(key));
                    }
                }
            }
            survey.add(attrs);
            surveys.add(survey);
        }
        System.out.println(surveys);
        ArrayList<Measure> measures = new ArrayList<>();
        ArrayList<double[][]> results = new ArrayList<>();
        //If any of these are commented out or added, update the index numbers where indicated in the Main Method
        measures.add(new Overlap(surveys));
        //measures.add(new Eskin(surveys)); 
        //measures.add(new IOF(surveys));
        measures.add(new OF(surveys));
        //measures.add(new Lin(surveys));
        double[][] res = new double[surveys.size()][surveys.size()];
        results.add(measures.get(0).compute());
        for (int i = 0; i < surveys.size(); i++)
            System.arraycopy(results.get(0)[i], 0, res[i], 0, res[i].length);
        for (int i = 1; i < measures.size(); i++) {
            results.add(measures.get(i).compute());
            for (int j = 0; j < results.get(i).length; j++) {
                for (int k = 0; k < results.get(i)[j].length; k++) {
                    res[j][k] += results.get(i)[j][k];
                }
            }
        }
        for (int j = 0; j < res.length; j++) {
            for (int k = 0; k < res[j].length; k++) {
                res[j][k] /= measures.size();
            }
        }
        results.add(res);
        return results.toArray(new double[][][] {});
    }

    private static void copyMap(HashMap<String, HashMap> keywords, HashMap<String, HashMap> parsed) {
        Iterator<String> keys = parsed.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            keywords.put(key, (HashMap)parsed.get(key).clone());
        }
    }

    private static ArrayList shrink(String[] names, double[][] data) {
        //Shrink by printing the array to a file, then reading it back as a new array.
        //To avoid memory concerns, set the array to null before reading back.
        
        //We do this by looking through the array to find the ekements we don't want,
        //then printing the ones we do.
        
        //Find the zeros:
        //For speed, use an array, since we know the exact size, with boolean
        //values for if the colum and row are missing.
        boolean[] use = new boolean[names.length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                //If we find a number, we should have it be true, and stop.
                //To do this without an if statement:
                //
                use[i] = (data[i][j] != 0.0);
                if (use[i])
                    break; // go to the next i.
            }
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new File("tmp.txt"));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        for (int i = 0; i < data.length; i++) {
            if (!use[i])
                continue;
            for (int j = 0; j < data[i].length; j++) {
                if (!use[j])
                    continue;
                out.print(data[i][j]);
                out.print(" ");
            }
            out.println();
        }
        out.flush();
        out.close();
        ArrayList<String> newNames = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            if (use[i])
                newNames.add(names[i]);
        }
        names = newNames.toArray(new String[0]);
        data = new double[names.length][names.length];
        Scanner input = null;
        try {
            input = new Scanner(new File("tmp.txt"));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] = input.nextDouble();
            }
        }
        ////////
        ArrayList res = new ArrayList();
        res.add(names);
        res.add(data);
        return res;
    }
}

