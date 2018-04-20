/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 *
 * @author Raell
 */
//A NodeTree that contains a game state (Root and bottom nodes)
public class GameTree extends NodeTree {

    private final GameState GameState;

    public GameTree(GameState state, double value, int playerCount, int depth, Move move, Colour currentPlayer) {
        super(value, playerCount, depth, move, null, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, currentPlayer);
        GameState = state;       
    }
    
    //Returns game state
    public GameState getState() {
        return new GameState(GameState, super.getMove());
    }
    
        
    @Override
    //Returns string representation of node
    public String toString() {
        return GameState.getCurrentPlayer().colour() + ": " + GameState.getCurrentPlayer().location();
    }

}
