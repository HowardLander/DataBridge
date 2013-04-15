/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

//Note that data here is a simple Collection of the Data size, followed by data elements

//Each data element will have number of attributes, (d), followed by the attribute list
public abstract class Measure {
    protected Collection<Collection<Collection<String>>> data;
    protected Collection<String> fullData;
    protected Collection<String> values;
    protected ArrayList<String> fullValues;
    private ArrayList<HashSet<String>> attrs;

    public Measure(Collection<Collection<Collection<String>>> data) {
        this.data = data;
        this.fullData = new ArrayList<>();
        Iterator<Collection<Collection<String>>> itSur = data.iterator();
        while (itSur.hasNext()) {
            Iterator<Collection<String>> itAttr = itSur.next().iterator();
            while (itAttr.hasNext()) {
                fullData.addAll(itAttr.next());
            }
        }
    }

    public double[][] compute() {
        double[][] ans = new double[data.size()][data.size()];
        Iterator<Collection<Collection<String>>> itX = data.iterator();
        for (int i = 0; itX.hasNext(); i++) {
            Collection<Collection<String>> surveyX = itX.next();
            Iterator<Collection<Collection<String>>> itY = data.iterator();
            for (int j = 0; itY.hasNext(); j++) {
                Collection<Collection<String>> surveyY = itY.next();
                ans[i][j] = Math.round(computeSim(surveyX, surveyY) * 100000)/100000.0;
                //put answer in matrix
            } //end for
            //end for
        } //end for
        //end for
        return ans;
    }

    protected double computeSim(Collection<Collection<String>> x, Collection<Collection<String>> y) {
        double ans = 0.0;
//        double[][] mat = new double[x.size()][y.size()];
//        for (int i = 0; i < mat.length; i++) {
//            for (int j = 0; j < mat[i].length; j++) {
//                mat[i][j] = 0;
//            }
//        }
        attrs = new ArrayList<>();
        ArrayList<ArrayList<String>> fullAttrs = new ArrayList<>();
        HashSet<String> comb;
        ArrayList<String> fullComb;
        Iterator<Collection<String>> itX = x.iterator();
        Iterator<Collection<String>> itY = y.iterator();
        while (itX.hasNext()) {
            Collection<String> valX = itX.next();
            Collection<String> valY = itY.next();
            comb = new HashSet();
            comb.addAll(valX);
            comb.addAll(valY);
            attrs.add(comb);
            fullComb = new ArrayList<>();
            fullComb.addAll(valX);
            fullComb.addAll(valY);
            fullAttrs.add(fullComb);
        }
        Iterator<HashSet<String>> itA = attrs.iterator();
        Iterator<ArrayList<String>> itF = fullAttrs.iterator();
        itX = x.iterator();
        itY = y.iterator();
        while (itA.hasNext()) {
            values = itA.next();
            fullValues = itF.next();
            Collection<String> Xk = itX.next();
            Collection<String> Yk = itY.next();
            ans += computeSimK(Xk, Yk);
        }
        return ans;
    }

    protected double computeSimK(Collection<String> Xk, Collection<String> Yk) {
        double[][] ans = new double[Xk.size()][Yk.size()];
        int i = 0;
        //Iterator<String> it = values.iterator();
        Iterator<String> itX = Xk.iterator();
        while (itX.hasNext()) {
            String xk = itX.next();
            Iterator<String> itY = Yk.iterator();
            int j = 0;
            while (itY.hasNext()) {
                String yk = itY.next();
                ArrayList<String> s = new ArrayList<>();
                s.add(xk);
                s.add(yk);
                //Collection[] arr = new Collection[] {Xk, Yk, s};
                if (xk.equalsIgnoreCase(yk))
                    ans[i][j] = weight(Xk, Yk) * in(s);
                else {
                    ans[i][j] = weight(Xk, Yk) * notIn(s);
                }
                j++;
            }
            i++;
        }
        //double match = mostMatch(Xk.size(), Yk.size());
        //double res = ans / match;
//        if (res > 1) {
//            System.out.println("---------------------------------");
//            System.out.println("Error: Value greater than 1");
//            System.out.println("Value of ans: " + ans);
//            System.out.println("Value of mostMatch: " + match);
//            System.out.println("Lists:");
//            System.out.println("   Xk: " + Xk);
//            System.out.println("   Yk: " + Yk);
//        }
        //Convert into double
        //Store all values into an ArrayList, then sort them.
        ArrayList<Double> tmp = new ArrayList<>();
        for (int k = 0; k < ans.length; k++)
            for (int j = 0; j < ans[k].length; j++)
                tmp.add(ans[k][j]);
        Collections.sort(tmp, Collections.reverseOrder());
        int min = Math.min(Xk.size(), Yk.size());
        double res = 0.0;
        for (int j = 0; j < min; j++) {
            res += tmp.get(j);
        }
        return res/min;
    }
    
    protected abstract double in(Object dummy);
    
    protected abstract double notIn(Object dummy);

    //protected abstract double mostMatch(int xLen, int yLen);

    protected double weight(Collection<String> Xk, Collection<String> Yk) {
        return 1.0 / attrs.size();
    }
    
}
