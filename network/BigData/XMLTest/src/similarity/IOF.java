/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Nerketur
 */
public class IOF extends Measure {
    
    public IOF(Collection<Collection<Collection<String>>> data) {
        super(data);
    }

    @Override
    protected double in(Object dummy) {
        return 1;
    }

    @Override
    protected double notIn(Object XYk) {
        Iterator<String> xy = ((Collection<String>)XYk).iterator();
        
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
        double ans = 1/(1+Math.log10(tmpX.size()) * Math.log10(tmpY.size()));
        return ans;
    }

//    @Override
//    protected double mostMatch(int xLen, int yLen) {
//        double min = Math.min(xLen, yLen);
//        double extra = Math.abs(xLen-yLen) * min;
//        double out = 1/(1+Math.log10(2) * Math.log10(2));
//        //double out1 = 1/(1+Math.log10(2) * Math.log10(1));
//        double in = 1;
//        return in*min + in*(extra) + out*(min*min - min);
//    }
    
}
