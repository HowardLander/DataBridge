/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sax;

import java.util.HashMap;
import java.util.Stack;
import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.renci.databridge.util.*;

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
    
    //Code added by Ren Bauer for util implementation
    private Dataset dataset;
    private ArrayList<Dataset> datasets;
    private int dsProperty;
    private String nextKey;
    private StringBuilder nextVal;
    //End Ren's code
    
    private int i = 0;
//    private HashMap<String, HashMap> currRecord;
//    private HashMap<String, HashMap> currHeader;
//    private HashMap<String, HashMap> currSumDesc;
    public Parser() {
        super();
        map = new HashMap();
        currEleName = new Stack<>();
        currEleValue = new Stack<>();
        //Code added by Ren Bauer for util support
        datasets = new ArrayList<Dataset>();
        dsProperty = -1;
        /* dsProperty - store information from start tag on whether or not we should set the dataset's
         * properties to the contents.
         * 0 - dbID
         * 1 - name
         * 2 - handle
         * 3 - URI
         * 4 - Some other property (store key in nextKey)
         */
        //End Ren's code
    }
    
    /**
     * @return the map
     */
    public HashMap<String, HashMap> getParsed() {
        return (HashMap<String, HashMap>) map; // We convert here because we will always return a hashmap of the requested hashmap data.
    }
    
    /**
     * Return an array of the Datasets
     * 
     * @author Ren Bauer -RENCI (www.renci.org)
     * 
     * @return The datasets
     */
    public Dataset[] getDatasets() {
    	return datasets.toArray(new Dataset[datasets.size()]);
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
            
            dataset = new Dataset(); //Ren Bauer
        } else if(!currEleName.empty() && currEleName.peek().equals("record")){
	        if (name.equals("header")) {
	            currEleName.push(name);
	            currEleValue.push(new HashMap<String, HashMap>()); // There can only be one record at a time, no decendant records
	        } else if (name.equals("sumDscr")) {
	            currEleName.push(name);
	            currEleValue.push(new HashMap<String, HashMap>()); // There can only be one record at a time, no decendant records
	        } else if (name.equals("keyword")) {
	            currEleName.push(name);
	            currEleValue.push(new StringBuilder()); // There can only be one record at a time, no decendant records
	        
	        } else if (name.equals("titl")) {
	            currEleName.push(name);
	            currEleValue.push(new StringBuilder()); // There can only be one record at a time, no decendant records
	            
	            dsProperty = 1; //Ren - set to store 'Name' property
            	nextVal = new StringBuilder();
	
	        } else if (name.equals("holdings")) {
	        	
	        	dataset.setURI(atts.getValue("URI")); //Ren - since this property is in an attribute, set it directly here
	        	dsProperty = 3; //Ren - set to store 'URI' property for consistency
            	nextVal = new StringBuilder();
	        	
	        } else if (name.equals("IDNo") && atts.getValue("agency").equals("handle")){
	        	
	        	dsProperty = 2; //Ren - set to store 'Handle' property
            	nextVal = new StringBuilder(); 
            	
	        }
        } else if (!currEleName.empty() && currEleName.peek().equals("header")){
            if (name.equals("identifier")) {
            	currEleName.push(name);
            	currEleValue.push(new StringBuilder()); // There can only be one record at a time, no decendant records
            
            	dsProperty = 0; //Ren - set to store 'dbID' property
            	nextVal = new StringBuilder();
            	
            } else if(name.equals("datestamp")){
            	
            	dsProperty = 4; //Ren = set to stare additional property
            	nextKey = "date"; //set the key for that property to date
            	nextVal = new StringBuilder();
            	
            }
        } else if (!currEleName.empty() && !currEleName.empty() && currEleName.peek().equals("sumDscr")) {
            currEleName.push(name);
            currEleValue.push(new StringBuilder()); // There can only be one record at a time, no decendant records
        } else
            return;
        //System.out.println("Start element: " + (uri.isEmpty() ? qName : "{" + uri + "}" + name ));
    }
    @Override
    public void endElement(String uri, String name, String qName) {
    	
        //Code add by Ren Bauer for util support
	      switch(dsProperty){
	      case 0:
	      	dataset.setDbID(nextVal.toString());
	      	break;
	      case 1:
	      	dataset.setName(nextVal.toString());
	      	break;
	      case 2:
	      	dataset.setHandle(nextVal.toString());
	      	break;
	      case 3:
	      	//dataset.setURI(nextVal.toString()); URI, as an attribute, set in start tag code
	      	break;
	      case 4:
	      	dataset.addProperty(nextKey, nextVal.toString());
	      	break;
	      }
	      dsProperty = -1; //Ren - no dataset properties should span end tags, so this can be reset
	    //End Ren's code
	      
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
            

            //Ren's code to support util
            datasets.add(dataset); //Ren - add the dataset to the datasets array
            dataset = null; //clear the dataset reference, will be recreated at next record start tag
            nextKey = null;
            nextVal = null;
            //end Ren's code
            
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
        //System.out.println("End element: " + (uri.isEmpty() ? qName : "{" + uri + "}" + name ));
       
    }
    
    @Override
    public void characters(char[] ch, int start, int length) {
        if (!currEleName.empty() && (currEleName.peek().equals("identifier") || currEleName.peek().equals("keyword") || currEleName.contains("sumDscr") || currEleName.peek().equals("titl"))) {
            ((StringBuilder)currEleValue.peek()).append(String.valueOf(ch, start, length));
            /*
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
            */
        }
        
        //Code added by Ren Bauer for util support
        if(dsProperty >= 0){
        	nextVal.append(String.valueOf(ch, start, length));
        }
        //End Ren's code
        
    }
    
}
