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
public class GameTree extends NodeTree {

    private final GameState GameState;
    /*private double value;
    private final int playerCount;
    private final int depth;
    private final List<GameTree> children;*/


    public GameTree(GameState state, double value, int playerCount, int depth, Move move) {
        super(value, playerCount, depth, move);
        GameState = state;       
        /*this.value = value;
        this.playerCount = playerCount;
        this.depth = depth;
        this.children = new ArrayList<>();*/
    }
    
    /*public List<GameTree> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public GameTree add(GameState state) {
        Double initialValue = (isMaximiser()) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        GameTree child = new GameTree(state, initialValue, playerCount, depth);
        children.add(child);
        return child;
    }*/
    
    public GameState getState() {
        return GameState;
    }
    
    /*public Boolean isMaximiser() {
        int height = getHeight();
        return (height % playerCount == depth % playerCount);
    }

    public int getHeight() {
        if(children.isEmpty())
            return 0;
        else {
            int height = 0;
            for(GameTree c : children) {
                height = Math.max(height, c.getHeight());
            }
            return height + 1;
        }
    }*/
    
    public List<GameTree> getBottomNodes() {
        return reachBottom(this);
    }
    
    private List<GameTree> reachBottom(NodeTree t) {
        List<GameTree> list = new ArrayList<>();
        if(t.getChildren().isEmpty() && t.getClass() == GameTree.class)
            list.add((GameTree) t);
        else {
            t.getChildren().forEach((c) -> {
                list.addAll(reachBottom(c));
            });
        }
        return list;           
    }
    
    /*public void setValue(double value) {
        this.value = value;
    }
    
    public double getValue() {
        return value;
    }*/
    
    //Visitor to score bottom nodes and uses minimax to generate path
    public void accept(Visitor v) {
        v.visit(this);
    }

}
