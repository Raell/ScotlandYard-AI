package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

//COMS10009 Live Programming Example Code
//DirectedGraph: implements a simple, directed graph
public class DirectedGraph<X, Y> implements Graph<X, Y>{

    private Map<X, Node<X>> nodeMap;
    private Map<Node<X>, List<Edge<X, Y>>> sourceEdges;
    private Map<Node<X>, List<Edge<X, Y>>> targetEdges;
    private List<Node<X>> allNodes;
    private List<Edge<X, Y>> allEdges;

    public DirectedGraph() {
        nodeMap = new HashMap<X, Node<X>>();
        sourceEdges = new HashMap<Node<X>, List<Edge<X, Y>>>();
        targetEdges = new HashMap<Node<X>, List<Edge<X, Y>>>();
        allNodes = new ArrayList<Node<X>>();
        allEdges = new ArrayList<Edge<X, Y>>();
    }

    @Override
    public void addNode(Node<X> node){
        if (node == null) throw new NullPointerException("node == null");
        if (nodeMap.containsKey(node.value()))
                throw new IllegalArgumentException(node + " is already in the graph");
        nodeMap.put(node.value(), node);
        allNodes.add(node);
        sourceEdges.put(node, new ArrayList<Edge<X, Y>>());
        targetEdges.put(node, new ArrayList<Edge<X, Y>>());
    }

    @Override
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
    public List<Edge<X, Y>> getEdges(){
        return Collections.unmodifiableList(allEdges);
    }

    @Override
    public List<Node<X>> getNodes(){
        return Collections.unmodifiableList(allNodes);
    }

    @Override
    public Node<X> getNode(X index){
        return nodeMap.get(index);
    }

    @Override
    public List<Edge<X, Y>> getEdgesTo(Node<X> node){
        return Collections.unmodifiableList(targetEdges.get(node));
    }

    @Override
    public List<Edge<X, Y>> getEdgesFrom(Node<X> node){
        return Collections.unmodifiableList(sourceEdges.get(node));
    }

    @Override
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
    public boolean containsNode(X value) {
        return nodeMap.containsKey(value);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return nodeMap.size();
    }
}
