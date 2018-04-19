/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

/**
 *
 * @author Raell
 */
import java.util.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Transport;

//CCOMS10009 Live Programming Example Code
//GraphCalculator: implements base algorithm for Prim's and Dijkstra's
public abstract class GraphCalculator {

    // reference to input graph
    protected Graph<Integer, Transport> graph;
    protected BiMap<Node<Integer>, WeightedNode<Integer>> weightedNodeMap;
    protected Map<Ticket, Integer> tickets;
    protected int totalTickets;

    // constructor initialising graph to calculate on
    public GraphCalculator(Graph<Integer, Transport> graph, Map<Ticket, Integer> tickets) {
        this.graph = graph;
        this.tickets = tickets;
        this.totalTickets = tickets.values().stream().mapToInt(Integer::intValue).sum();
        this.weightedNodeMap = HashBiMap.create();
    }

    // applies algorithm to input graph given a start node
    public DirectedGraph<Integer, Double> getResult(Integer startNodeID) {

        // make data structures required / node collections
        //final Set<Node<Integer>> visited = new HashSet<Node<Integer>>();
        final PriorityQueue<WeightedNode<Integer>> unvisited = new PriorityQueue<>(); 
        // make a result graph
        final DirectedGraph<Integer, Double> ourResult = new DirectedGraph<>();
        
        // initialise starting node
        Node<Integer> currentNode = graph.getNode(startNodeID);               

        // initialise data structures required / node collections
        for (Node<Integer> node : graph.getNodes()) {
            ourResult.addNode(node);
            
            WeightedNode<Integer> wNode = new WeightedNode(node);
            weightedNodeMap.put(node, wNode);
            
            if(!node.value().equals(startNodeID)) {
                wNode.setWeight(Double.POSITIVE_INFINITY);
                unvisited.add(wNode);
            } 
            else {
                wNode.setWeight(0.0);
            }
        }
        
        // find initial direct distances to start node
        updateDistances(unvisited,currentNode,ourResult);

        // greedily update nodes
        while (!unvisited.isEmpty()) {
            currentNode = weightedNodeMap.inverse().get(unvisited.poll());
            updateDistances(unvisited,currentNode,ourResult);
        }

        // return result graph with every edge pointing towards path to starting node
        return ourResult;
    }

    // update rule to be specified by subclasses
    protected abstract Double update(Double distance, Double currentDistance, Double directDistance);

    // updates all unvisited node distances by considering routes via currentNode
    private void updateDistances(PriorityQueue<WeightedNode<Integer>> unvisited,
                                    Node<Integer> currentNode,
                                    Graph<Integer, Double> ourResult) {
        
        // consider neighbours of current node (others can't gain from update)
        graph.getEdgesFrom(currentNode).forEach((e) -> {
            Node<Integer> neighbour = e.destination();
            WeightedNode<Integer> wNeighbour = weightedNodeMap.get(neighbour);
            WeightedNode<Integer> wCurrent = weightedNodeMap.get(currentNode);
            // only update unvisited nodes (others already have shortest connection)
            if (unvisited.contains(wNeighbour) && e.data() != Transport.FERRY) {// && wCurrent.getTicketsCount(Ticket.fromTransport(e.data())) > 0) {
                // get current distance of neighbour before
                Double distance = wNeighbour.getWeight();
                Double dScaled = (distance != Double.POSITIVE_INFINITY) ? (distance * Math.pow(wNeighbour.getMoves(), 2)) : Double.POSITIVE_INFINITY;
                // get possible distance of neighbour AFTER considering 'newly added'

                Double transportWeight = (double) totalTickets / tickets.get(Ticket.fromTransport(e.data()));
                
                Double possibleUpdate = update(distance, wCurrent.getWeight(), transportWeight);//* Math.pow(weightedNodeMap.get(currentNode).getMoves() + 1, 1);             
                Double PUScaled = possibleUpdate * Math.pow(wCurrent.getMoves() + 1, 2);
                // implement update only if beneficial
                if (dScaled > PUScaled) {
                    unvisited.remove(wNeighbour);
                    wNeighbour.setWeight(possibleUpdate);
                    wNeighbour.setMoves(wCurrent.getMoves() + 1);
                    unvisited.add(wNeighbour);
                    ourResult.addEdge(new Edge<>(neighbour, currentNode, transportWeight));
                }
            }
        }); 
    } 
} 
