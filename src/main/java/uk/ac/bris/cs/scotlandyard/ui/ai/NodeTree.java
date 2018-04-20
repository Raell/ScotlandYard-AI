/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 *
 * @author Raell
 */
//A tree that contains alpha-beta values for a sequence of moves originating from the root game state
public class NodeTree {
    private double value;
    private final Move move;
    protected final int playerCount;
    protected final int depth;
    private double alpha;
    private double beta;
    private final List<NodeTree> children;
    private NodeTree parent;
    private final Colour currentPlayer;
    
    public NodeTree(double value, int playerCount, int depth, Move move, NodeTree parent, double alpha, double beta, Colour currentPlayer) {
        this.value = value;
        this.playerCount = playerCount;
        this.depth = depth;
        this.children = new LinkedList<>();
        this.move = move;
        this.parent = parent;
        this.alpha = alpha;
        this.beta = beta;
        this.currentPlayer = currentPlayer;
    }
    
    //Returns list of children
    public List<NodeTree> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    //Add a new child from move
    public NodeTree add(Move move, double alpha, double beta, Colour currentPlayer) {
        Double initialValue = (isMaximiser()) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        NodeTree child = new NodeTree(initialValue, playerCount, depth, move, this, alpha, beta, currentPlayer);
        children.add(child);
        return child;
    }
    
    //Add a new child from NodeTree
    public void add(NodeTree t) {
        t.setParent(this);
        children.add(t);
    }
    
    //Sets the parent of the node
    public void setParent(NodeTree parent) {
        this.parent = parent;
    }
    
    //Returns current player
    public Colour getCurrentPlayer() {
        return currentPlayer;
    }
    
    //Returns parent node
    public NodeTree getParent() {
        return parent;
    }
    
    //Returns alpha value
    public double getAlpha() {
        return alpha;
    }
    
    //Sets alpha value
    public void setAlpha(double a) {
        this.alpha = a;
    }
    
    //Returns beta value
    public double getBeta() {
        return beta;
    }
    
    //Sets beta value
    public void setBeta(double b) {
        this.beta = b;
    }
    
    //Remove tree from list of children
    public void remove(NodeTree t) {
        this.children.remove(t);
    }
     
    //Checks if this is a maximiser node
    public final Boolean isMaximiser() {
        return (currentPlayer == Colour.BLACK);
    }
    
    //Returns move
    public Move getMove() {
        return move;
    }
    
    //Sets value of node
    public void setValue(double value) {
        this.value = value;
    }
    
    //Returns value of node
    public double getValue() {
        return value;
    }
    
}
