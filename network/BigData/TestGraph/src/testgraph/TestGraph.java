/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testgraph;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import org.apache.commons.collections15.Transformer;
import testgraph.OwnGraphTest.MyLink;
import testgraph.OwnGraphTest.MyNode;

/**
 *
 * @author Nerketur
 */
public class TestGraph {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //SimpleGraphView sgv = new SimpleGraphView(); //We create our graph in here
        OwnGraphTest ogt = new OwnGraphTest();
        // The Layout<V, E> is parameterized by the vertex and edge types
        //Layout<Integer, String> layout = new SpringLayout(sgv.g2);
        Layout<MyNode, MyLink> layout = new CircleLayout(ogt.g);
        layout.setSize(new Dimension(300,300)); // sets the initial size of the space
        // The BasicVisualizationServer<V,E> is parameterized by the edge types
        VisualizationViewer<MyNode,MyLink> vv =
        new VisualizationViewer<MyNode,MyLink>(layout, new Dimension(350,350));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.CYAN));
        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<MyLink,String>() {
            @Override
            public String transform(MyLink input) {
                return ""+input.getWeight();
            }
        });
        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }
}
