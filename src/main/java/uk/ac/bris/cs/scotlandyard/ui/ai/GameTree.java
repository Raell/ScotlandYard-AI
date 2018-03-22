/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Raell
 */
public class GameTree {

    private final GameState GameState;
    private int value;
    private final int playerCount;
    private final int depth;
    private List<GameTree> children;


    public GameTree(GameState state, int value, int playerCount, int depth) {
        GameState = state;
        this.value = value;
        this.playerCount = playerCount;
        this.depth = depth;
    }
    
    public List<GameTree> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public Boolean add(GameState state, int value) {
        return children.add(new GameTree(state, value, playerCount, depth));
    }
    
    public GameState getState() {
        return GameState;
    }
    
    public Boolean isMaximiser() {
        int height = getHeight();
        return (height % playerCount == depth % playerCount);
    }

    private int getHeight() {
        if(children.isEmpty())
            return 0;
        else {
            int height = 0;
            for(GameTree c : children) {
                height = Math.max(height, c.getHeight());
            }
            return height + 1;
        }
    }
    
    public List<GameTree> getBottomNodes() {
        List<GameTree> list = new ArrayList<>();
        
        if(getHeight() == 0)
            list.add(this);                
        else {
            children.forEach((c) -> {
                list.addAll(c.getBottomNodes());
            });
        }
        return list;
    }
    
    /*public void performMiniMax() {
        //TODO: Update tree values by minimax algorithm from bottom nodes
        scoreLeafValue();
    }
    
    private void scoreLeafValue() {
        //TODO: Updates value of leaf nodes
    }*/
    
    public void setValue(int value) {
        this.value = value;
    }
    
    //Visitor to score bottom nodes and uses minimax to generate path
    public void accept(Visitor v) {
        v.visit(this);
    }

}
