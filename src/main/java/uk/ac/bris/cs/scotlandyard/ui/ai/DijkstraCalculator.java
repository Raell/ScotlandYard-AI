/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;


/*import uk.ac.bris.cs.dijkstra.DirectedGraph;
import uk.ac.bris.cs.dijkstra.Edge;
import uk.ac.bris.cs.dijkstra.Graph;
import uk.ac.bris.cs.dijkstra.Node;*/
import java.util.List;
import java.util.Map;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Transport;

//Based on COMS10009 Live Programming Example Code
//Implements Dijkstra's algorithm
public class DijkstraCalculator extends GraphCalculator {
    
    public DijkstraCalculator(Graph<Integer, Transport> graph, Map<Ticket, Integer> tickets) {
        super(graph, tickets);
    }
  
    //Implements Dijkstra's update rule
    @Override
    protected Double update(Double distance, Double currentDistance, Double directDistance) {
        return Math.min(distance, currentDistance + directDistance);
    }
    
    //Returns distance value from spanning tree and destination
    public double getResult(DirectedGraph<Integer, Double> anyNodeToStart, Integer destinationNodeID) {
        // initialise current as end node and initialise graph that will hold the route to return
        Node<Integer> current = anyNodeToStart.getNode(destinationNodeID);  
        
        double value = 0;
        double moves = 0;

        // trace route from end node to start node
        while (!anyNodeToStart.getEdgesFrom(current).isEmpty()) {
            List<Edge<Integer, Double>> edges = anyNodeToStart.getEdgesFrom(current);
            Edge<Integer, Double> e = edges.get(edges.size() - 1);
            value += e.data();
            current = e.destination();
            moves++;
        }
        
        return value * Math.pow(moves, 2);
    } 
}
