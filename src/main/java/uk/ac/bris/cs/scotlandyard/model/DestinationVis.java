/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.model;

/**
 *
 * @author lucas
 */
public class DestinationVis implements MoveVisitor{
    int destination;
    
    @Override
    public void visit(TicketMove move){
        destination = move.destination();
    }
    
    @Override
    public void visit(DoubleMove move){
        destination = move.finalDestination();
    }
    
    @Override
    public void visit(PassMove move){
        destination = -1;
    }
}
