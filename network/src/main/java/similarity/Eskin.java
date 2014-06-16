/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package similarity;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Nerketur
 */
public class Eskin extends Measure {

    public Eskin(Collection<Collection<Collection<String>>> data) {
        super(data);
    }
    
    @Override
    protected double in(Object dummy) {
        return 1;
    }
    @Override
    protected double notIn(Object dummy) {
        double valuesSqr = values.size() * values.size();
        return valuesSqr / (valuesSqr + 2.0);
    }

//    @Override
//    protected double mostMatch(int xLen, int yLen) {
//        //double max = 0.0;
//        double min = Math.min(xLen, yLen);
//        //for (int min = 0; min <= Math.min(xLen, yLen); min++) {
//            double val = xLen+yLen - min;
//            double valSqr = val*val;
//            double out = valSqr / (valSqr + 2.0);
//            //double in = 1;
//            //max = Math.max(max, min + out*(xLen*yLen - min));
//        //}
//        return min + out*(xLen*yLen - min);
//    }

}
