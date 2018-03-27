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
public class HasValidVis implements MoveVisitor{
    ScotlandYardPlayer player;
    boolean hasTicket;
    
    HasValidVis(ScotlandYardPlayer player){
        this.player = player;
    }
    
    @Override
    public void visit(TicketMove move){
        hasTicket = player.hasTickets(move.ticket());
    }
    
    @Override
    public void visit(DoubleMove move){
        if(move.hasSameTicket())
            hasTicket = player.hasTickets(move.firstMove().ticket(), 2);
        else
            hasTicket = (player.hasTickets(move.firstMove().ticket()) && player.hasTickets(move.secondMove().ticket()));
    }
}
