/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sax;

import java.util.HashMap;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Nerketur
 */
public class Parser extends DefaultHandler {
    boolean record = false;
    boolean header = false;
    
    private HashMap<String, HashMap> map;
    private Stack<String> currEleName;
    private Stack<Object> currEleValue;
    private int i = 0;
//    private HashMap<String, HashMap> currRecord;
//    private HashMap<String, HashMap> currHeader;
//    private HashMap<String, HashMap> currSumDesc;
    public Parser() {
        super();
        map = new HashMap();
        currEleName = new Stack<>();
        currEleValue = new Stack<>();
    }
    
    /**
     * @return the map
     */
    public HashMap<String, HashMap> getParsed() {
        return (HashMap<String, HashMap>) map; // We convert here because we will always return a hashmap of the requested hashmap data.
    }
        
    @Override
    public void startDocument() {
        System.out.println("Start of XML Document.");
    }
    
    @Override
    public void endDocument() {
        System.out.println("End of XML Document.");        
    }
    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        //We can call recursively for each element.
        //We check each element recursively
        if (name.equals("record")) {
            currEleName.push(name);
            currEleValue.push(new HashMap<String, HashMap>()); // There can only be one record at a time, no decendant records
        } else if (name.equals("header") && !currEleName.empty() && currEleName.peek().equals("record")) {
            currEleName.push(name);
            currEleValue.push(new HashMap<String, HashMap>()); // There can only be one record at a time, no decendant records
        } else if (name.equals("sumDscr") && !currEleName.empty() && currEleName.peek().equals("record")) {
            currEleName.push(name);
            currEleValue.push(new HashMap<String, HashMap>()); // There can only be one record at a time, no decendant records
        } else if (name.equals("keyword") && !currEleName.empty() && currEleName.peek().equals("record")) {
            currEleName.push(name);
            currEleValue.push(new StringBuilder()); // There can only be one record at a time, no decendant records
        } else if (name.equals("identifier") && !currEleName.empty() && currEleName.peek().equals("header")) {
            currEleName.push(name);
            currEleValue.push(new StringBuilder()); // There can only be one record at a time, no decendant records
        } else if (name.equals("titl") && !currEleName.empty() && currEleName.peek().equals("record")) {
            currEleName.push(name);
            currEleValue.push(new StringBuilder()); // There can only be one record at a time, no decendant records
        } else if (!currEleName.empty() && !currEleName.empty() && currEleName.peek().equals("sumDscr")) {
            currEleName.push(name);
            currEleValue.push(new StringBuilder()); // There can only be one record at a time, no decendant records
        } else
            return;
        System.out.println("Start element: " + (uri.isEmpty() ? qName : "{" + uri + "}" + name ));
    }
    @Override
    public void endElement(String uri, String name, String qName) {
        if (name.equals("record")) {
            //Finish record.  Pop off stack, add to map
            //Modify so the identifier is at the root.
            currEleName.pop();
            //TODO: Change to the title.  in Record
            HashMap currVal = (HashMap) currEleValue.pop();
            String currName = (String)currVal.get("titl");
            currVal.remove("header");
            currVal.remove("titl");
            if (map.containsKey(currName)) {
                if (map.containsKey(currName + "(" + i + ")"))
                    i++;
                map.put(currName+" ("+i+")", currVal);
            } else
                map.put(currName, currVal);
        } else if (name.equals("header")) {
            //Finish header.  Pop off stack, add
            String currName = currEleName.pop();
            Object currVal = currEleValue.pop();
            ((HashMap)currEleValue.peek()).put(currName, currVal);
        } else if (name.equals("sumDscr")) {
            //Finish summary Desc.  Pop off stack, add
            String currName = currEleName.pop();
            Object currVal = currEleValue.pop();
            ((HashMap)currEleValue.peek()).put(currName, currVal);
        } else if (name.equals("keyword")) {
            String currName = currEleName.pop();
            String currVal = ((StringBuilder)currEleValue.pop()).toString();
            ((HashMap)currEleValue.peek()).put(currName + i, currVal);
        } else if (name.equals("titl")) {
            String currName = currEleName.pop();
            String currVal = ((StringBuilder)currEleValue.pop()).toString();
            ((HashMap)currEleValue.peek()).put(currName, currVal);
        } else if (name.equals("identifier") && !currEleName.empty()) {
            String currName = currEleName.pop();
            String currVal = ((StringBuilder)currEleValue.pop()).toString();
            ((HashMap)currEleValue.peek()).put(currName, currVal);
        } else if (!currEleName.empty() && currEleName.contains("sumDscr")) {
            //Finish record.  Pop off stack, add
            String currName = currEleName.pop();
            Object currVal = currEleValue.pop();
            if (currVal instanceof StringBuilder)
                ((HashMap)currEleValue.peek()).put(currName, ((StringBuilder)currVal).toString());
            if (currVal instanceof HashMap)
                ((HashMap)currEleValue.peek()).put(currName, (HashMap)currVal);
        } else
            return;
        i++;
        System.out.println("End element: " + (uri.isEmpty() ? qName : "{" + uri + "}" + name ));
    }
    
    @Override
    public void characters(char[] ch, int start, int length) {
        if (!currEleName.empty() && (currEleName.peek().equals("identifier") || currEleName.peek().equals("keyword") || currEleName.contains("sumDscr") || currEleName.peek().equals("titl"))) {
            ((StringBuilder)currEleValue.peek()).append(String.valueOf(ch, start, length));
            System.out.print("Characters: \"");
            for (int i = start; i < start+length; i++) {
                switch(ch[i]) {
                    case '\\':
                        System.out.print("\\\\");
                        break;
                    case '"':
                        System.out.print("\\");
                        break;
                    case '\n':
                        System.out.print("\\n");
                        break;
                    case '\r':
                        System.out.print("\\r");
                        break;
                    case '\t':
                        System.out.print("\\t");
                        break;
                    default:
                        System.out.print(ch[i]);
                        break;
                }
            }
            System.out.println("\"");
        }
    }
    
}
