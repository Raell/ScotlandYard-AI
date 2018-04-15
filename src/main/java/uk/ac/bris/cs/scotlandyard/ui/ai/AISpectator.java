/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Set;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Spectator;

/**
 *
 * @author Raell
 */
public class AISpectator implements Spectator {
    
    private final Athena ai;
    
    public AISpectator(Athena ai) {
        this.ai = ai;
    }
    
    @Override
    public void onMoveMade(ScotlandYardView view, Move move) {
        //TODO: Swap root with corresponding child
        if(move.colour() == Colour.BLACK)
            return;
        
        ai.setRoot(move);
    }

    @Override
    public void onRoundStarted(ScotlandYardView view, int round) {
        
    }

    @Override
    public void onRotationComplete(ScotlandYardView view) {
        
    }

    @Override
    public void onGameOver(ScotlandYardView view, Set<Colour> winningPlayers) {
        
    }
    
}
