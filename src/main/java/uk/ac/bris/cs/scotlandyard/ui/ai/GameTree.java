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
        super(value, playerCount, depth, move, null);
        GameState = state;       
    }
    
    
    public static GameTree swapRoot(NodeTree newRoot, GameState newState) {
        GameTree root = new GameTree(newState, newRoot.getValue(), newRoot.playerCount, newRoot.depth, null);
        newRoot.getChildren().forEach(c -> {
            root.add(c);
        });
        return root;
    }
    
    public GameState getState() {
        return GameState;
    }
    
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
    
    public void toNodeTree() {
        double value = isMaximiser() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        NodeTree n = new NodeTree(value, this.playerCount, this.depth, GameState.getLastMove(), null);
        this.getChildren().forEach(c -> {
            n.add(c);
        });
        
        NodeTree p = this.getParent();
        
        if(p != null) {
            p.add(n);
            p.remove(this);
        }
        
    }
    
    public String toString() {
        return GameState.getCurrentPlayer().colour() + ": " + GameState.getCurrentPlayer().location();
    }

}
