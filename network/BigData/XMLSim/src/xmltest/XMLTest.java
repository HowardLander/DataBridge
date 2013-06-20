/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmltest;

import gui.DisplayGraph;
import java.awt.Container;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import similarity.*;

/**
 *
 * @author Nerketur
 */
public class XMLTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory x = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = x.newDocumentBuilder();
        Document parsed = db.parse("OAI_Odum_Harris.xml");
        System.out.println("-----Keyword list-----");
        printAllNodes(parsed, "record", "keyword");
        System.out.println("-----Summary Desc-----");
        //Uses a modified version, with the full words.  Here, we will use an ArrayList, untyped, so that we can store a list at the end.
        Find needed = new Find("record");
        needed.addFilter("sumDscr");
        needed.getNextList().get(0).addFilter("");
        printAllNodes(parsed, needed);
        System.out.println("-------------------------");
        HashMap<String, HashMap> allNodes = allNodesHash(parsed, needed);
        System.out.println(allNodes);
        HashMap<String, HashMap> allNodes1 = shorten("sumDscr", allNodes);
        System.out.println(allNodes1);
        System.out.println(allNodes1.get("record0"));
        allNodes = allNodesHash(parsed, "record", "keyword");
        System.out.println(allNodes);
        HashMap<String, HashMap> allNodes2 = shorten("keyword", allNodes);
        System.out.println(allNodes2);
        System.out.println(allNodes2.get("record0"));
        System.out.println("----------------------------------");
        System.out.println("Comparing all surveys:");
        double[][][] dataTmp = compare(allNodes2, allNodes1);
        final double[][] data = dataTmp[0];
        //0, overlap
        //1, eskin
        //x, IOF
        //2, OF
        //x, Lin
        //3, comprehensive
        System.out.println("[");
        final String[] arr = allNodes1.keySet().toArray(new String[] {});
        for (int i = 0; i < data.length; i++) {
            System.out.print(" [" + arr[i] + ":");
            for (int j = 0; j < data[i].length; j++) {
                System.out.print(" " + data[i][j]);
            }
            System.out.println("]");
        }
        System.out.println("]");
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Container content = frame.getContentPane();
                content.add(new DisplayGraph(arr,data));
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

//    private static void printAllNodes(Document parsed) {
//        printAllNodes(parsed.getChildNodes(), "", 0);
//    }
//    private static void printAllNodes(Document parsed, String wanted) {
//        printAllNodes(parsed.getElementsByTagName(wanted), "", 0);
//    }
    
    private static void printAllNodes(Document parsed, Find wanted) {
        printAllNodes(parsed.getElementsByTagName(wanted.getStr()), wanted, 0);
    }
    private static void printAllNodes(Document parsed, String wanted, String filter) {
        Find f = new Find(wanted);
        f.addFilter(filter);
        printAllNodes(parsed.getElementsByTagName(wanted), f, 0);
    }
//    private static void printAllNodes(NodeList eles, int depth) {
//        StringBuilder pre = new StringBuilder();
//        for (int i = 0; i < depth; i++)
//            pre.append("    ");
//        
//        for (int i=0; i < eles.getLength(); i++) {
//            System.out.print(pre.toString() + eles.item(i).getNodeName() + ": ");
//            if (eles.item(i).hasChildNodes()) {
//                if (eles.item(i).getChildNodes().item(0).getNodeName().equalsIgnoreCase("#text")) {
//                    System.out.println(eles.item(i).getTextContent());
//                } else {
//                    System.out.println();
//                    printAllNodes(eles.item(i).getChildNodes(), depth+1);
//                }
//            }
//        }
//    }
    private static HashMap<String, HashMap> allNodesHash(Document parsed, String wanted, String filter) {
        Find f = new Find(wanted);
        f.addFilter(filter);
        return allNodesHash(parsed.getElementsByTagName(wanted), f, 0);
    }
    private static HashMap<String, HashMap> allNodesHash(Document parsed, Find wanted) {
        HashMap<String, HashMap> allNodesHash = allNodesHash(parsed.getElementsByTagName(wanted.getStr()), wanted, 0);
        //allNodesHash.keySet().toArray(new String[] {});
//        Iterator<String> keys = allNodesHash.keySet().iterator();
//        for (String key = keys.next(); !keys.hasNext(); key = keys.next()) {
//            if (allNodesHash.get(key).size() == 1) {
//                allNodesHash.
//            }
//        }
        return allNodesHash;
    }
    private static HashMap<String, HashMap> allNodesHash(NodeList eles, Find wanted, int depth) {
        StringBuilder pre = new StringBuilder();
        HashMap<String, HashMap> newMap = new HashMap<>();
        for (int i = 0; i < depth; i++)
            pre.append("    ");
        
        for (int i=0; i < eles.getLength(); i++) {
            Node ele = eles.item(i);
            NodeList childs = ele.getChildNodes();
            int foundIdx = wanted.getNextList().indexOf(new Find(ele.getNodeName()));
            boolean printed = false;
            if (((wanted.getNextList().isEmpty() || foundIdx != -1 || wanted.getNextList().get(0).getStr().isEmpty()) || depth == 0) && (!ele.getNodeName().equalsIgnoreCase("#text"))) {
                System.out.print(pre + ele.getNodeName() + ": "); // Only when depth == 0 OR ele.getNodeName() = wanted
                if (ele.hasChildNodes() && childs.item(0).getNodeName().equalsIgnoreCase("#text")) {
                    System.out.println(ele.getTextContent());
                    if (childs.getLength() <= 1) {
                        HashMap<String, String> fin = new HashMap<>();
                        fin.put(ele.getNodeName(), ele.getTextContent());
                        if (!newMap.containsValue(fin))
                            newMap.put(ele.getNodeName()+depth+"-"+i, fin);
                        continue;
                    }
                }
                System.out.println(); // needed end
                printed = true;
            }
            Find newFind = wanted;
            if (foundIdx != -1) {
                newFind = wanted.getNextList().get(foundIdx);
                if (newFind == null)
                    newFind = wanted;
            }
            HashMap<String, HashMap> posHash = allNodesHash(childs, newFind, (printed?depth+1:depth));
            if (!posHash.isEmpty())
                newMap.put(ele.getNodeName() + i, posHash);
            //if this node is NOT printed, then we use the inner node as our result.
            //The issue is we can't exactly do that because we don't know which node
            //the result is in.  o we normalize on the workhorse function.
        }
        return newMap;
    }
    private static void printAllNodes(NodeList eles, String wanted, int depth) {
        StringBuilder pre = new StringBuilder();
        for (int i = 0; i < depth; i++)
            pre.append("    ");
        
        for (int i=0; i < eles.getLength(); i++) {
            Node ele = eles.item(i);
            NodeList childs = ele.getChildNodes();
            if (((wanted.isEmpty() || ele.getNodeName().equals(wanted)) || depth == 0) && (!ele.getNodeName().equalsIgnoreCase("#text"))) {
                System.out.print(pre + ele.getNodeName() + ": "); // Only when depth == 0 OR ele.getNodeName() = wanted
                if (ele.hasChildNodes() && childs.item(0).getNodeName().equalsIgnoreCase("#text")) {
                    System.out.println(ele.getTextContent());
                    if (childs.getLength() <= 1)
                        continue;
                }
                System.out.println(); // needed end
            }
            printAllNodes(childs, wanted, depth+1); // Always, but depth if ele.getNodeName() != wanted
        }
    }
    private static void printAllNodes(NodeList eles, Find wanted, int depth) {
        StringBuilder pre = new StringBuilder();
        for (int i = 0; i < depth; i++)
            pre.append("    ");
        
        for (int i=0; i < eles.getLength(); i++) {
            Node ele = eles.item(i);
            NodeList childs = ele.getChildNodes();
            int foundIdx = wanted.getNextList().indexOf(new Find(ele.getNodeName()));
            boolean printed = false;
            if (((wanted.getNextList().isEmpty() || foundIdx != -1 || wanted.getNextList().get(0).getStr().isEmpty()) || depth == 0) && (!ele.getNodeName().equalsIgnoreCase("#text"))) {
                System.out.print(pre + ele.getNodeName() + ": "); // Only when depth == 0 OR ele.getNodeName() = wanted
                if (ele.hasChildNodes() && childs.item(0).getNodeName().equalsIgnoreCase("#text")) {
                    System.out.println(ele.getTextContent());
                    if (childs.getLength() <= 1)
                        continue;
                }
                System.out.println(); // needed end
                printed = true;
            }
            Find newFind = wanted;
            if (foundIdx != -1) {
                newFind = wanted.getNextList().get(foundIdx);
                if (newFind == null)
                    newFind = wanted;
            }
            printAllNodes(childs, newFind, (printed?depth+1:depth)); // Always, but depth if ele.getNodeName() != wanted
        }
    }

    private static HashMap<String, HashMap> shorten(String label, HashMap<String, HashMap> allNodes) {
        HashMap<String, HashMap> newMap = new HashMap<>();
        Iterator<String> it = allNodes.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            HashMap shortened = doShorten(label, allNodes.get(key));
            newMap.put(key, shortened);
        }
        return newMap;
    }

    private static HashMap doShorten(String label, HashMap<String, Object> get) {
        boolean nextString = ((HashMap)get.values().iterator().next()).values().iterator().next() instanceof String;
        if (!nextString) {
            return doShorten(label, (HashMap)get.get(get.keySet().iterator().next()));
        } else {
            //Check all values for strings (one level)
            Iterator<Object> it = get.values().iterator();
            HashMap<String, String> newMap = new HashMap<>();
            int i = 0;
            while (it.hasNext()) {
                HashMap val = (HashMap)it.next();
                if (val.values().iterator().next() instanceof String) {
                    newMap.put(val.keySet().iterator().next().toString()+i, val.values().iterator().next().toString());
                    it.remove();
                }
                i++;
            }
            get.clear();
            if (newMap.size() > 1)
                get.put(label, newMap);
            else
                get.putAll(newMap);
        }
        return get;
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
            } catch (ClassCastException e) {
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
                    if (key.charAt(key.length()-1) <= '9' && key.charAt(key.length()-1) >= '0')
                        keyL = key.substring(0, key.length()-1);
                    attrs.add(keyL + ": " + get);
                } else {
                    Iterator<String> it = ((HashMap)get).keySet().iterator();
                    while (it.hasNext()) {
                        key = it.next();
                        String keyL = key;
                        if (key.charAt(key.length()-1) <= '9' && key.charAt(key.length()-1) >= '0')
                            keyL = key.substring(0, key.length()-1);
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
        measures.add(new Overlap(surveys));
        measures.add(new Eskin(surveys));
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

}

