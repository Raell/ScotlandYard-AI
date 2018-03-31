/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 *
 * @author Raell
 */
public class NodeTree {
    private double value;
    private final Move move;
    private final int playerCount;
    private final int depth;
    private final List<NodeTree> children;
    
    public NodeTree(double value, int playerCount, int depth, Move move) {
        this.value = value;
        this.playerCount = playerCount;
        this.depth = depth;
        this.children = new ArrayList<>();
        this.move = move;
    }
    
    public List<NodeTree> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public NodeTree add(Move move) {
        Double initialValue = (isMaximiser()) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        NodeTree child = new NodeTree(initialValue, playerCount, depth, move);
        children.add(child);
        return child;
    }
    
    public void add(NodeTree t) {
        children.add(t);
    }
     
    public Boolean isMaximiser() {
        int height = getHeight();
        return (height % playerCount == depth % playerCount);
    }

    public int getHeight() {
        if(children.isEmpty())
            return 0;
        else {
            int height = 0;
            for(NodeTree c : children) {
                height = Math.max(height, c.getHeight());
            }
            return height + 1;
        }
    }
    
    public Move getMove() {
        return move;
    }
    
    /*public List<NodeTree> getBottomNodes() {
        List<NodeTree> list = new ArrayList<>();
        
        if(getHeight() == 0)
            list.add((GameTree) this);                
        else {
            children.forEach((c) -> {
                list.addAll(c.getBottomNodes());
            });
        }
        return list;
    }*/
    
    public void setValue(double value) {
        this.value = value;
    }
    
    public double getValue() {
        return value;
    }
    
    //Visitor to score bottom nodes and uses minimax to generate path
    /*public void accept(Visitor v) {
        v.visit(this);
    }*/
    
}
