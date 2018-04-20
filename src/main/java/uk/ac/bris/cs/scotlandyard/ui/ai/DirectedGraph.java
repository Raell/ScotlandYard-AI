package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

//DirectedGraph: implements a simple, directed graph
public class DirectedGraph<X, Y> implements Graph<X, Y>{

    private final Map<X, Node<X>> nodeMap;
    private final Map<Node<X>, List<Edge<X, Y>>> sourceEdges;
    private final Map<Node<X>, List<Edge<X, Y>>> targetEdges;
    private final List<Node<X>> allNodes;
    private final List<Edge<X, Y>> allEdges;
    
    public DirectedGraph() {
        nodeMap = new HashMap<>();
        sourceEdges = new HashMap<>();
        targetEdges = new HashMap<>();
        allNodes = new ArrayList<>();
        allEdges = new ArrayList<>();
    }

    @Override
    //Adds a new node
    public void addNode(Node<X> node){
        if (node == null) throw new NullPointerException("node == null");
        if (nodeMap.containsKey(node.value()))
                throw new IllegalArgumentException(node + " is already in the graph");
        nodeMap.put(node.value(), node);
        allNodes.add(node);
        sourceEdges.put(node, new ArrayList<>());
        targetEdges.put(node, new ArrayList<>());
    }

    @Override
    //Adds a new edge
    public void addEdge(Edge<X, Y> edge){
        if (edge == null) throw new NullPointerException("edge == null");
        Node<X> source = getNode(edge.source().value());
        if (source == null) throw new IllegalArgumentException(
                        "source of edge(" + edge.source() + ") is not in the graph");
        Node<X> destination = getNode(edge.destination().value());
        if (destination == null) throw new IllegalArgumentException(
                        "destination of edge(" + edge.destination() + ") is not in the graph");
        
        sourceEdges.get(source).add(edge);
        targetEdges.get(destination).add(edge);
        allEdges.add(edge);
    }

    @Override
    //Returns edges
    public List<Edge<X, Y>> getEdges(){
        return Collections.unmodifiableList(allEdges);
    }

    @Override
    //Returns nodes
    public List<Node<X>> getNodes(){
        return Collections.unmodifiableList(allNodes);
    }

    @Override
    //Gets node at index
    public Node<X> getNode(X index){
        return nodeMap.get(index);
    }

    @Override
    //Gets edges with node as destination
    public List<Edge<X, Y>> getEdgesTo(Node<X> node){
        return Collections.unmodifiableList(targetEdges.get(node));
    }

    @Override
    //Gets edges with node as origin
    public List<Edge<X, Y>> getEdgesFrom(Node<X> node){
        return Collections.unmodifiableList(sourceEdges.get(node));
    }

    @Override
    //Returns string representation of graph
    public String toString() {
        String output = "";
        for (Node<X> node : allNodes) {
            output += node.toString() + "\n";
        }

        for (Edge<X, Y> edge : allEdges) {
            output += edge.toString() + "\n";
        }

        return output;
    }

    @Override
    //Checks whether graph contains node
    public boolean containsNode(X value) {
        return nodeMap.containsKey(value);
    }

    @Override
    //Checks if graph is empty
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    //Returns number of nodes
    public int size() {
        return nodeMap.size();
    }
}
