/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.model;

import java.util.Set;

/**
 *
 * @author lucas
 */
public class IsValidVis implements MoveVisitor{
    Set<Move> validMoves;
    boolean isValid;
    
    IsValidVis(Set<Move> validMoves){
        this.validMoves = validMoves;
    }
    
    @Override
    public void visit(TicketMove move){
        isValid = validMoves.contains(move);
    }
    
    @Override
    public void visit(DoubleMove move){
        isValid = validMoves.contains(move);
    }
    
    @Override
    public void visit(PassMove move){
        isValid = validMoves.contains(move);
    }
    
}
