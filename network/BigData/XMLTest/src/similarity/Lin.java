/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author Nerketur
 */
public class Lin extends Measure {

    public Lin(Collection<Collection<Collection<String>>> data) {
        super(data);
    }
        
    @Override
    protected double weight(Collection<String> Xk, Collection<String> Yk) {
        
        double ans = 0.0;
        Collection<String> tmpX = new ArrayList<>();
        Collection<String> tmpY = new ArrayList<>();
        Iterator<String> itX = Xk.iterator();
        while (itX.hasNext()) {
            
            Collection<String> valX = new ArrayList<>();
            valX.add(itX.next());
            tmpX.clear();
            tmpX.addAll(fullValues);
            tmpX.retainAll(valX); // Since we're only doing main diagonal.
            Iterator<String> itY = Yk.iterator();
            while (itY.hasNext()) {
                Collection<String> valY = new ArrayList<>();
                valY.add(itY.next());
                tmpY.clear();
                tmpY.addAll(fullValues);
                tmpY.retainAll(valY); // Since we're only doing main diagonal.
                ans += Math.log10(tmpX.size()/(double)data.size()) + Math.log10(tmpY.size()/(double)data.size());
            }
        }
        return 1/ans;
    }
    
    @Override
    protected double in(Object dummy) {
        Iterator<String> xy = ((Collection<String>)dummy).iterator();
        
        Collection<String> valX = new ArrayList<>();
        valX.add(xy.next());
 
        Collection<String> tmpX = new ArrayList<>();
        tmpX.addAll(fullValues);
        tmpX.retainAll(valX); // Since we're only doing main diagonal.
        return 2*Math.log10(tmpX.size()/(double)data.size());
    }

    @Override
    protected double notIn(Object dummy) {
        Iterator<String> xy = ((Collection<String>)dummy).iterator();
        
        Collection<String> valX = new ArrayList<>();
        valX.add(xy.next());
        Collection<String> valY = new ArrayList<>();
        valY.add(xy.next());

        Collection<String> tmpX = new ArrayList<>();
        Collection<String> tmpY = new ArrayList<>();
        tmpX.addAll(fullValues);
        tmpY.addAll(fullValues);
        tmpX.retainAll(valX); // Since we're only doing main diagonal.
        tmpY.retainAll(valY); // Since we're only doing main diagonal.
        return 2*Math.log10(tmpX.size()/(double)data.size() + tmpY.size()/(double)data.size());
    }
    
//    @Override
//    protected double mostMatch(int xLen, int yLen) {
//        double min = Math.min(xLen, yLen);
//        double out = 1/(1+Math.log10(2) * Math.log10(2));
//        double in = 1;
//        return in*min + out*(xLen*yLen - min);
//    }
}
