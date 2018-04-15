/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.model;

import java.util.List;

/**
 *
 * @author lucas
 */
public class HiddenMoveVis implements MoveVisitor{
    int destination;
    int currentRound;
    List<Boolean> rounds;
    
    boolean isDouble;
    int firstDes;
    int secondDes;
    TicketMove firstMove;
    TicketMove secondMove;
    
    HiddenMoveVis(int lastRevealed, List<Boolean> rounds, int currentRound) {
        destination = lastRevealed;
        firstDes = lastRevealed;
        secondDes = lastRevealed;
        this.currentRound = currentRound;
        this.rounds = rounds;
    }
    
    @Override
    public void visit(TicketMove move){
        isDouble = false;
        firstMove = move;
        
        if(currentRound > 0 && rounds.get(currentRound - 1))
            destination = move.destination();
    }
    
    @Override
    public void visit(DoubleMove move) {
        isDouble = true;
        firstDes = destination;
        secondDes = destination;
        firstMove = move.firstMove();
        secondMove = move.secondMove();
        
        //if the first round is a reveal round, reveal that destination and
        //update the second one
        if(rounds.get(currentRound)) {
            firstDes = move.firstMove().destination();
            if(!rounds.get(currentRound + 1))
                secondDes = firstDes;
        }
        
        //if the second round is a reveal round, reveal that destination, too
        if(rounds.get(currentRound + 1))
            secondDes = move.secondMove().destination();
    }
    
}
