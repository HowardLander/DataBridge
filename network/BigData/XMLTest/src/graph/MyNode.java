/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

/**
 *
 * @author Nerketur
 */
public class MyNode {
    private String id;
    private final SurveyGraph graph;

    // good coding practice would have this as private
    public MyNode(String id, final SurveyGraph graph) {
        this.graph = graph;
        this.id = id;
    }

    public String toString() {
        // Always a good idea for debuging
        return "V" + id;
        // JUNG2 makes good use of these.
    }
    
    public String label() {
        return id;
    }
}
