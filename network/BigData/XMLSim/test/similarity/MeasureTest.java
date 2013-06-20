/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package similarity;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nerketur
 */
public class MeasureTest {
    
    public MeasureTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of overlap method, of class Measure.
     */
    @Test
    public void testOverlap() {
        System.out.println("overlap");
        Collection<Collection<Collection<String>>> data = new ArrayList<>();
        Collection<Collection<String>> SurveyX = new ArrayList<>();
        Collection<Collection<String>> SurveyY = new ArrayList<>();
        Collection<String> Xk = new ArrayList<>();
        Xk.add("Bob");
        Xk.add("Joe");
        Xk.add("Sue");
        Collection<String> Yk = new ArrayList<>();
        Yk.add("Cal");
        Yk.add("Bob");
        Yk.add("Ken");
        Yk.add("Joe");
        
        SurveyX.add(Xk);
        SurveyY.add(Yk);
        data.add(SurveyX);
        data.add(SurveyY);
        Overlap over = new Overlap(data);
        double[][] expResult = new double[2][2];
        expResult[0][0] = 1;
        expResult[0][1] = .4;
        expResult[1][0] = .4;
        expResult[1][1] = 1;
        double[][] result = over.compute();
        assertArrayEquals(expResult, result);
    }
    @Test
    public void testEskin() {
        System.out.println("Eskin");
        Collection<Collection<Collection<String>>> data = new ArrayList<>();
        Collection<Collection<String>> SurveyX = new ArrayList<>();
        Collection<Collection<String>> SurveyY = new ArrayList<>();
        Collection<String> Xk = new ArrayList<>();
        Xk.add("Bob");
        Xk.add("Joe");
        Xk.add("Sue");
        Collection<String> Yk = new ArrayList<>();
        Yk.add("Cal");
        Yk.add("Bob");
        Yk.add("Ken");
        Yk.add("Joe");
        
        SurveyX.add(Xk);
        SurveyY.add(Yk);
        data.add(SurveyX);
        data.add(SurveyY);
        double[][] expResult = new double[2][2];
        double there = 1, gone = 25.0/27.0;
        expResult[0][0] = 1;
        expResult[0][1] = Math.floor((2*there + 3*gone)/5.0*10000)/10000.0;
        expResult[1][0] = Math.floor((2*there + 3*gone)/5.0*10000)/10000.0;
        expResult[1][1] = 1;
        Measure eskin = new Eskin(data);
        double[][] result = eskin.compute();
        assertArrayEquals(expResult, result);        
    }
}
