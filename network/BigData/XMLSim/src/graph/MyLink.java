/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

/**
 *
 * @author Nerketur
 */
public class MyLink {
    private double weight;
    private int id;
    private final SurveyGraph graph;

    public MyLink(double weight, final SurveyGraph graph) {
        this.graph = graph;
        this.id = graph.edgeCount++; // This is defined in the outer class.
        this.weight = weight;
    }

    @Override
    public String toString() {
        // Always good for debugging
        return "E" + id;
    }

    /**
     * @return the label wanted.
     */
    public String label() {
        return String.valueOf(weight);
    }
    
}
