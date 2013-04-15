/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testgraph;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 *
 * @author Nerketur
 */
class OwnGraphTest {
    public final DirectedSparseMultigraph<MyNode, MyLink> g;
    private final MyNode n1;
    private final MyNode n2;
    private final MyNode n3;
    private final MyNode n4;
    private final MyNode n5;
    private int edgeCount = 0;
    
    public OwnGraphTest()  {
       g = new DirectedSparseMultigraph<MyNode, MyLink>();
       // Create some MyNode objects to use as vertices
       n1 = new MyNode(1); n2 = new MyNode(2); n3 = new MyNode(3);
       n4 = new MyNode(4); n5 = new MyNode(5); // note n1-n5 declared elsewhere.
       // Add some directed edges along with the vertices to the graph
       g.addEdge(new MyLink(2.0, 48),n1, n2, EdgeType.DIRECTED); // This method
       g.addEdge(new MyLink(2.0, 48),n2, n3, EdgeType.DIRECTED);
       g.addEdge(new MyLink(3.0, 192), n3, n5, EdgeType.DIRECTED);
       g.addEdge(new MyLink(2.0, 48), n5, n4, EdgeType.DIRECTED); // or we can use
       g.addEdge(new MyLink(2.0, 48), n4, n2); // In a directed graph the
       g.addEdge(new MyLink(2.0, 48), n3, n1); // first node is the source
       g.addEdge(new MyLink(10.0, 48), n2, n5);// and the second the destination
    }
    
    class MyNode {
        private int id; // good coding practice would have this as private
        public MyNode(int id) {
            this.id = id;
        }
        public String toString() { // Always a good idea for debuging
            return "V"+id;
            // JUNG2 makes good use of these.
        }
    }
    class MyLink {
        private double capacity; // should be private
        private double weight; // should be private for good practice
        private int id;
        public MyLink(double weight, double capacity) {
            this.id = edgeCount++; // This is defined in the outer class.
            this.weight = weight;
            this.capacity = capacity;
        }
        
        @Override
        public String toString() { // Always good for debugging
            return "E"+id;
        }

        /**
         * @return the weight
         */
        public double getWeight() {
            return weight;
        }
    }
    
}
