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
public class Overlap extends Measure {

    public Overlap(Collection<Collection<Collection<String>>> data) {
        super(data);
    }

    @Override
    protected double in(Object dummy) {
        return 1;
    }

    @Override
    protected double notIn(Object dummy) {
        return 0;
    }

//    @Override
//    protected double mostMatch(int xLen, int yLen) {
//        return Math.min(xLen, yLen);
//    }
    
}
