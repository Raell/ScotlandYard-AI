/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;

/**
 *
 * @author Raell
 */
public class ValidMoves{ 
    private static Graph<Integer, Transport> graph;
    //private List<ScotlandYardPlayer> detectives;
    private static List<Boolean> rounds;
    
    public static void initialise(Graph<Integer, Transport> g, List<Boolean> r) {
        graph = g;
        rounds = r;
    }
    
    //checks whether a player has enough tickets to make the move
    private static boolean hasValidTicket(ScotlandYardPlayer player, TicketMove move) {      
        return player.hasTickets(move.ticket());
    }

    //checks whether a player has enough tickets to make the move
    private static boolean hasValidTicket(ScotlandYardPlayer player, DoubleMove move) {
        if(move.hasSameTicket())
            return player.hasTickets(move.firstMove().ticket(), 2);
        else
            return (player.hasTickets(move.firstMove().ticket()) && player.hasTickets(move.secondMove().ticket()));
    }

    //returns all the ticket moves a player can make, possibly with secret tickets 
    //if the player is mrX
    private static Set<TicketMove> possibleStandardMoves(ScotlandYardPlayer player, int location, boolean secret, List<ScotlandYardPlayer> detectives) {   
        Set<TicketMove> moves = new HashSet<>();


        //gets the edges connected to the current node
        Node<Integer> locNode = graph.getNode(location);        
        Collection<Edge<Integer, Transport>> fromEdges = graph.getEdgesFrom(locNode);

        //creates the ticket moves
        fromEdges.forEach((edge) -> {
            Ticket ticket = secret ? Ticket.SECRET : Ticket.fromTransport(edge.data());
            TicketMove move = new TicketMove(player.colour(), ticket, edge.destination().value());
            if (detectives.stream().noneMatch(p -> p.location() == edge.destination().value())
                    && hasValidTicket(player, move))
                moves.add(move);
        });

        return moves;

    }

    //gets the possible double moves mrX can make
    private static Set<Move> possibleDoubleMoves(ScotlandYardPlayer player, Set<TicketMove> tMoves,
            int currentRound, List<ScotlandYardPlayer> detectives){
        Set<Move> doublemoves = new HashSet<>();

        //adds the doublemoves
        if(player.hasTickets(Ticket.DOUBLE) && currentRound + 2 <= rounds.size()){ 
            tMoves.forEach((firstMove) -> {
                //get the ticket moves from each of the first moves' destinations
                Set<TicketMove> secondMoves = possibleStandardMoves(player, firstMove.destination(), false, detectives);
                if(player.hasTickets(Ticket.SECRET))
                    secondMoves.addAll(possibleStandardMoves(player, firstMove.destination(), true, detectives));

                //creates the double moves
                for(TicketMove secondMove : secondMoves) {
                    DoubleMove doublemove = new DoubleMove(player.colour(), firstMove, secondMove);
                    if(hasValidTicket(player, doublemove))
                        doublemoves.add(doublemove);
                }
            });
        }
        return doublemoves;
    }

    //returns the set of possible moves mrX can make
    private static Set<Move> mrXMoves(ScotlandYardPlayer player, int location, 
            int currentRound, List<ScotlandYardPlayer> detectives,
            boolean restrictSpecial) {
        Set<Move> moves = new HashSet<>();

        //adds all the standard and secret ticket moves mrX can make
        Set<TicketMove> tMoves = new HashSet<>();
        moves.addAll(possibleStandardMoves(player, location, false, detectives));
        
        if(!restrictSpecial) {
            if(player.hasTickets(Ticket.SECRET)){              
                tMoves.addAll(possibleStandardMoves(player, location, true, detectives));
            }

            moves.addAll(tMoves);

            //adds the double moves mrX can make
            Set<Move> doublemoves = possibleDoubleMoves(player, tMoves, currentRound, detectives);
            moves.addAll(doublemoves);
        }
        
        return moves;
    }

    //returns the valid mvoes for the given player
    public static Set<Move> validMoves(ScotlandYardPlayer player, int location, int currentRound, List<ScotlandYardPlayer> detectives, boolean restrictSpecial) {
        Set<Move> moves = new HashSet<>();

        if(player.isMrX())
            moves.addAll(mrXMoves(player, location, currentRound, detectives, restrictSpecial));
        else {
            moves.addAll(possibleStandardMoves(player, location, false, detectives));
            //if the detective can't make a ticket move, they make a pass move
            if(moves.isEmpty())              
                moves.add(new PassMove(player.colour()));
        }

        return moves;
    }
}