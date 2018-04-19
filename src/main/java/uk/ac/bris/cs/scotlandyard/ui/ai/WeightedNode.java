/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.HashMap;
import java.util.Map;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Ticket;

/**
 *
 * @author Raell
 */
public class WeightedNode<X> implements Comparable<WeightedNode<X>> {

    private Node<X> node;
    private Double weight;
    private int moves;
    private Map<Ticket, Integer> tickets;
    
    public WeightedNode(Node<X> n) {
        this.node = n;
        this.weight = 0.0;
        this.moves = 0;
        this.tickets = new HashMap<>();
    }

    public X value() {
        return node.value();
    }

    public String toString() {
        return "Weighted" + node.toString();
    }
    
    public int getMoves() {
        return moves;
    }
    
    public void setMoves(int moves) {
        this.moves = moves;
    }
    
    public Double getWeight() {
        return weight;
    }  

    public void setWeight(Double weight) {
        this.weight = weight;
    }  
    
    @Override
    public int compareTo(WeightedNode<X> weightedNode) {
    	return Double.compare(this.weight, weightedNode.getWeight());  
    }

}
