/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Node;

/**
 *
 * @author Raell
 */
//Node containing nodes for Prim's Algorithm
public class WeightedNode<X> implements Comparable<WeightedNode<X>> {

    private final Node<X> node;
    private Double weight;
    private int moves;
    
    public WeightedNode(Node<X> n) {
        this.node = n;
        this.weight = 0.0;
        this.moves = 0;
    }
    
    //Returns value of node
    public X value() {
        return node.value();
    }

    @Override
    //Returns string representation of node
    public String toString() {
        return "Weighted" + node.toString();
    }
    
    //Returns number of moves taken to reach node
    public int getMoves() {
        return moves;
    }
    
    //Sets number of move
    public void setMoves(int moves) {
        this.moves = moves;
    }
    
    //Returns weight of node
    public Double getWeight() {
        return weight;
    }  

    //Sets weight of node
    public void setWeight(Double weight) {
        this.weight = weight;
    }  
    
    @Override
    //Comparator method based on weight
    public int compareTo(WeightedNode<X> weightedNode) {
    	return Double.compare(this.weight, weightedNode.getWeight());  
    }

}
