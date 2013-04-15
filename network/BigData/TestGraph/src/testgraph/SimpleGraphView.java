/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testgraph;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 *
 * @author Nerketur
 */
class SimpleGraphView {
    public final SparseMultigraph<Integer, String> g;
    public final SparseMultigraph<Integer, String> g2;
    
    public SimpleGraphView()  {
         // Graph<V, E> where V is the type of the vertices
        // and E is the type of the edges
        g = new SparseMultigraph<Integer, String>();
        // Add some vertices. From above we defined these to be type Integer.
        g.addVertex((Integer)1);
        g.addVertex((Integer)2);
        g.addVertex((Integer)3);
        g.addVertex((Integer)4);
        // Add some edges. From above we defined these to be of type String
        // Note that the default is for undirected edges.
        g.addEdge("Edge-A", 1, 2); // Note that Java 1.5 auto-boxes primitives
        g.addEdge("Edge-B", 2, 3);
        g.addEdge("Edge-C", 3, 4); // Note that Java 1.5 auto-boxes primitives
        g.addEdge("Edge-D", 4, 1);
        // Let's see what we have. Note the nice output from the
        // SparseMultigraph<V,E> toString() method
        System.out.println("The graph g = " + g.toString());
        // Note that we can use the same nodes and edges in two different graphs.
        g2 = new SparseMultigraph<Integer, String>();
        g2.addVertex((Integer)1);
        g2.addVertex((Integer)2);
        g2.addVertex((Integer)3);
        g2.addEdge("Edge-A", 1,3);
        g2.addEdge("Edge-B", 2,3, EdgeType.DIRECTED);
        g2.addEdge("Edge-C", 3, 2, EdgeType.DIRECTED);
        g2.addEdge("Edge-P", 2,3);
        g2.addEdge("Edge-D", 1, 2); // A parallel edge
        System.out.println("The graph g2 = " + g2.toString()); 
    }
    
}
