/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 *
 * @author admin
 */
public class ScoreVisitor implements Visitor{
    private ArrayList<Move> selectedPath;
    @Override
    public void visit(GameTree tree) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        //Visit bottomn nodes and use score function on the game state
        tree.getBottomNodes().forEach(t -> {
            t.setValue(scoreState(t.getState()));
        });
    }
    
    private int scoreState(GameState g) {
        //TODO implement
        return 0;
    }
    
}
