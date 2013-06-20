/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlsim;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Nerketur
 */
public class Find {
    private final ArrayList<Find> find = new ArrayList<Find>();
    private final String curr;
    
    public Find(String wanted) {
        curr = wanted;
    }
    
    public ArrayList<Find> getNextList() {
        return find;
    }
    
    public void addFilter(String string) {
        Find f = new Find(string);
        this.find.add(f);
    }

    String getStr() {
        return curr;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Find)
            return ((Find)o).curr.equals(this.curr);
        return false;
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.curr);
        return hash;
    }
    
}

