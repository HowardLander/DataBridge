/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import edu.uci.ics.jung.graph.SparseGraph;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 *
 * @author Nerketur
 */
public class SurveyGraph {
    
    int edgeCount = 0;
    private final SparseGraph<MyNode, MyLink> g;
    private final MyNode[] nodes;
    
    public SurveyGraph(String[] verts, double[][] surveyData) {
        //We create the graph here
        g = new SparseGraph<>();
        nodes = new MyNode[surveyData.length];
        for (int i = 0; i < surveyData.length; i++) {
            nodes[i] = new MyNode(verts[i], this);
        }
        for (int i = 0; i < surveyData.length; i++)
            for (int j = i+1; j < surveyData[i].length; j++)
                if (surveyData[i][j] > 0 && i != j)
                    g.addEdge(new MyLink(surveyData[i][j], this), nodes[i], nodes[j]);
    }

    public SparseGraph<MyNode, MyLink> getGraph() {
        return g;
    }

    public JButton getButton() {
        JButton b = new JButton("Hi");
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                g.removeVertex(nodes[0]);
                
            }
        });
        return b;
    }
}
