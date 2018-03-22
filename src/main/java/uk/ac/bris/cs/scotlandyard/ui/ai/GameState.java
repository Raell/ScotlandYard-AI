/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;

/**
 *
 * @author lucas
 */
public class GameState {
    private List<ScotlandYardPlayer> players;
    private final Map<Colour, ScotlandYardPlayer> cToP;
    private int currentRound;
    private final List<Boolean> rounds;
    private Set<Colour> stuckDetectives;
    private ScotlandYardPlayer currentPlayer;
    private Move lastMove;

    public GameState(ScotlandYardView view){
        players = new ArrayList<>();
        view.getPlayers().forEach(p -> players.add(makePlayer(view, p)));
        cToP = setColoursToPlayersMap();
        currentRound = view.getCurrentRound();
        rounds = view.getRounds();
        currentPlayer = cToP.get(view.getCurrentPlayer());
        lastMove = null;
    }
    
    private GameState(GameState g, Move move) {
        this(g);
        this.lastMove = move;
    }
    
    private GameState(GameState g) {
        this.players = g.players;
        this.cToP = g.cToP;
        this.currentRound = g.currentRound;
        this.rounds = g.rounds;
        this.stuckDetectives = g.stuckDetectives;
        this.currentPlayer = g.currentPlayer;
    }
    
    public Move getLastMove() {
        if(lastMove.getClass() == TicketMove.class) {
            TicketMove tMove = (TicketMove) lastMove;
            return new TicketMove(tMove.colour(), tMove.ticket(), tMove.destination());
        }
        else if(lastMove.getClass() == DoubleMove.class) {
            DoubleMove dMove = (DoubleMove) lastMove;
            return new DoubleMove(dMove.colour(), dMove.firstMove(), dMove.secondMove());
        }
        else {
            return new PassMove(lastMove.colour());
        }
    }

    public int getCurrentRound() {
        return currentRound;
    }

    private void setStuck(Colour detective){
        stuckDetectives.add(detective);
    }

    public Map<Colour, ScotlandYardPlayer> getColourMap() {
        return Collections.unmodifiableMap(cToP);
    }
    
    public Map<Ticket, Integer> getPlayerTickets(Colour colour) {
        return Collections.unmodifiableMap(cToP.get(colour).tickets());
    }

    private Map<Ticket, Integer> getPlayerTicketsFromView(ScotlandYardView view, Colour colour){
        Map<Ticket, Integer> m = new HashMap<>();
        m.put(Ticket.BUS, view.getPlayerTickets(colour, Ticket.BUS).get());
        m.put(Ticket.DOUBLE, view.getPlayerTickets(colour, Ticket.DOUBLE).get());
        m.put(Ticket.SECRET, view.getPlayerTickets(colour, Ticket.SECRET).get());
        m.put(Ticket.TAXI, view.getPlayerTickets(colour, Ticket.TAXI).get());
        m.put(Ticket.UNDERGROUND, view.getPlayerTickets(colour, Ticket.UNDERGROUND).get());
        return m;
    }

    private ScotlandYardPlayer makePlayer(ScotlandYardView view, Colour colour){
        ScotlandYardPlayer player = new ScotlandYardPlayer(
                null, colour, 
                view.getPlayerLocation(colour).get(),
                getPlayerTicketsFromView(view, colour)
        );
        return player;
    }

    //maps the colours to the corresponding player
    private Map<Colour, ScotlandYardPlayer> setColoursToPlayersMap(){
        Map<Colour, ScotlandYardPlayer> m = new HashMap<>();
        players.forEach((p) -> {
            m.put(p.colour(), p);
        });
        return m;
    }

    public int getPlayerLocation(Colour colour) {
        return cToP.get(colour).location();
    }

    public int getPlayerTickets(Colour colour, Ticket ticket) {
        return cToP.get(colour).tickets().get(ticket);
    }

    private void nextRound(int x){
        currentRound =+ x;
    }
    
    private void nextPlayer(Colour prevPlayer) {      
        int index = players.indexOf(prevPlayer);
        if(index + 1 < players.size())
            currentPlayer =  players.get(index + 1);
        else
            currentPlayer = players.get(0);
    }
    
    public GameState nextState(GameState currentState, Move move){
        if(move.getClass() == DoubleMove.class)
            return nextState(this, (DoubleMove) move);
        else if(move.getClass() == TicketMove.class)
            return nextState(this, (TicketMove) move);
        else {
            GameState nextState = new GameState(currentState, move);
            nextState.setStuck(move.colour());
            nextState.nextPlayer(move.colour());
            return nextState;
        }     
    }

    private GameState nextState(GameState currentState, TicketMove move){
        GameState nextState = new GameState(currentState, move);
        if(move.colour().isMrX())
            nextState.nextRound(1);
        ScotlandYardPlayer player = nextState.getColourMap().get(move.colour());
        player.removeTicket(move.ticket());
        player.location(move.destination());
        nextState.nextPlayer(move.colour());
        return nextState;
    }

    private GameState nextState(GameState currentState, DoubleMove move){
        GameState nextState = new GameState(currentState, move);
        currentState.nextRound(2);
        ScotlandYardPlayer player = nextState.getColourMap().get(move.colour());
        player.removeTicket(move.firstMove().ticket());
        player.removeTicket(move.secondMove().ticket());
        player.location(move.finalDestination());
        nextState.nextPlayer(move.colour());
        return nextState;
    }
            

}
