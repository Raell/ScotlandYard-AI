/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
//Holds the state of a game at a particular instance
public class GameState {
    private final List<ScotlandYardPlayer> players;
    private final Map<Colour, ScotlandYardPlayer> cToP;
    private int currentRound;
    private static List<Boolean> rounds;
    private final Set<Colour> stuckDetectives;
    private ScotlandYardPlayer currentPlayer;
    private final Move lastMove;
    private int lastKnownPos;
    private final List<Ticket> ticketsSinceLastKnown;

    public GameState(ScotlandYardView view, int mrXLocation){
        players = makePlayers(view, mrXLocation);
        cToP = setColoursToPlayersMap();
        currentRound = view.getCurrentRound();
        currentPlayer = cToP.get(view.getCurrentPlayer());
        stuckDetectives = new HashSet<>();
        lastMove = null;
        lastKnownPos = view.getPlayerLocation(Colour.BLACK).get();
        ticketsSinceLastKnown = new LinkedList<>();
    } 
    
    //Generates new game state from previous state and move
    public GameState(GameState g, Move move) {
        this.lastMove = move;
        this.players = clonePlayers(g.players);
        this.cToP = this.setColoursToPlayersMap();
        this.currentRound = g.currentRound;
        this.stuckDetectives = cloneStuck(g.stuckDetectives);
        this.currentPlayer = this.players.get(g.players.lastIndexOf(g.currentPlayer));
        this.lastKnownPos = g.lastKnownPos;
        this.ticketsSinceLastKnown = new LinkedList<>(g.ticketsSinceLastKnown);
    }
    
    //Sets last known position of MrX
    private void setLastKnownPos(int lkp) {
        this.lastKnownPos = lkp;
    }
    
    //Gets last known position of MrX
    public int getLastKnownPos() {
        return lastKnownPos;
    }  
    
    //Gets list of tickets MrX used since last revealed
    public List<Ticket> getTicketsSinceLastKnown() {
        return Collections.unmodifiableList(ticketsSinceLastKnown);
    }
    
    //Sets list of reveal rounds
    public static void setRounds(List<Boolean> r) {
        rounds = r;
    }
    
    //Returns list of reveal rounds
    public static List<Boolean> getRounds() {
        return rounds;
    }
    
    //Gets current player
    public ScotlandYardPlayer getCurrentPlayer() {
        return currentPlayer;
    }
    
    //Gets list of detectives
    public List<ScotlandYardPlayer> getDetectives() {
        return players
                .stream()
                .filter(d -> d.colour() != Colour.BLACK)
                .collect(Collectors.toList());
    }
    
    //Gets last move made
    public Move getLastMove() {
        if(lastMove == null)
            return null;
        else if(lastMove.getClass() == TicketMove.class) {
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

    //Gets current round
    public int getCurrentRound() {
        return currentRound;
    }
    
    //Sets detective as stuck
    private void setStuck(Colour detective){
        stuckDetectives.add(detective);
    }
    
    //Checks if game is over
    public boolean isGameOver() {                          
        return stuckDetectives.size() == getDetectives().size() || mrXCaught();
    }
    
    //Checks if MrX is caught
    public boolean mrXCaught() {
        int mrXLocation = getPlayerLocation(Colour.BLACK);  
        return getDetectives().stream().anyMatch(d -> d.location() == mrXLocation);
    }

    //Returns map of player tickets
    public Map<Ticket, Integer> getPlayerTickets(Colour colour) {
        return Collections.unmodifiableMap(cToP.get(colour).tickets());
    }

    //Updates player tickets from view
    private Map<Ticket, Integer> getPlayerTicketsFromView(ScotlandYardView view, Colour colour){
        Map<Ticket, Integer> m = new HashMap<>();
        m.put(Ticket.BUS, view.getPlayerTickets(colour, Ticket.BUS).get());
        m.put(Ticket.DOUBLE, view.getPlayerTickets(colour, Ticket.DOUBLE).get());
        m.put(Ticket.SECRET, view.getPlayerTickets(colour, Ticket.SECRET).get());
        m.put(Ticket.TAXI, view.getPlayerTickets(colour, Ticket.TAXI).get());
        m.put(Ticket.UNDERGROUND, view.getPlayerTickets(colour, Ticket.UNDERGROUND).get());
        return m;
    }
    
    //Make ScotlandYardPlayer from view
    private List<ScotlandYardPlayer> makePlayers(ScotlandYardView view, int mrXLocation){      
        List<ScotlandYardPlayer> ps = new ArrayList<>();
                
        view.getPlayers().forEach(colour -> {          
            int playerLocation = (colour == Colour.BLACK) ? mrXLocation : view.getPlayerLocation(colour).get();
        
            ps.add(new ScotlandYardPlayer(
                    null, colour, 
                    playerLocation,
                    getPlayerTicketsFromView(view, colour)
            ));
        });
        
        return ps;
    }
    
    //Clone ScotlandYardPlayer object
    private List<ScotlandYardPlayer> clonePlayers(List<ScotlandYardPlayer> players) {
        List<ScotlandYardPlayer> clone = new ArrayList<>();
        players.forEach(p -> {
            clone.add(new ScotlandYardPlayer(
                    null, p.colour(), p.location(),
                    p.tickets()
            ));
        });
        return clone;
    }

    //Clones set of stuck detectives
    private Set<Colour> cloneStuck(Set<Colour> stuck) {
        Set<Colour> clone = new HashSet<>();
        stuck.forEach(s -> {
            clone.add(s);
        });
        return clone;
    }

    //Maps colours to corresponding players
    private Map<Colour, ScotlandYardPlayer> setColoursToPlayersMap(){
        Map<Colour, ScotlandYardPlayer> m = new HashMap<>();
        players.forEach((p) -> {
            m.put(p.colour(), p);
        });
        return m;
    }

    //Gets player location
    public int getPlayerLocation(Colour colour) {
        return cToP.get(colour).location();
    }

    //Gets player tickets
    public int getPlayerTickets(Colour colour, Ticket ticket) {
        return cToP.get(colour).tickets().get(ticket);
    }
    
    //Returns player object from colour
    public ScotlandYardPlayer getPlayer(Colour colour) {
        return cToP.get(colour);
    }
    
    //Gets list of players
    public List<ScotlandYardPlayer> getPlayers() {
        return Collections.unmodifiableList(players);
    }
    
    //Rotates current player to next player
    private void nextPlayer(ScotlandYardPlayer prevPlayer) {      
        int index = players.indexOf(prevPlayer);
        if(index + 1 < players.size())
            currentPlayer =  players.get(index + 1);
        else
            currentPlayer = players.get(0);
    }
    
    //Generate next game state from move
    public GameState nextState(Move move){
        if(move.getClass() == DoubleMove.class)
            return nextState((DoubleMove) move);
        else if(move.getClass() == TicketMove.class)
            return nextState((TicketMove) move);
        else {
            GameState nextState = new GameState(this, move);
            nextState.setStuck(move.colour());
            nextState.nextPlayer(nextState.cToP.get(move.colour()));
            return nextState;
        }     
    }

    //Generate next game state from TicketMove
    private GameState nextState(TicketMove move){
        GameState nextState = new GameState(this, move);
        
        if(move.colour().isMrX()) {
            nextState.currentRound++;
            if(nextState.currentRound - 1 != rounds.size() && rounds.get(nextState.currentRound - 1)) {
                nextState.setLastKnownPos(move.destination());
                nextState.ticketsSinceLastKnown.clear();
            }
            else
                nextState.ticketsSinceLastKnown.add(move.ticket());
        }
        
        ScotlandYardPlayer player = nextState.cToP.get(move.colour());
        player.removeTicket(move.ticket());
        player.location(move.destination());
        nextState.nextPlayer(nextState.cToP.get(move.colour()));
        return nextState;
    }

    //Generate next game state from DoubleMove
    private GameState nextState(DoubleMove move){
        GameState nextState = new GameState(this, move);
        nextState.currentRound += 2;
        if(rounds.get(nextState.currentRound - 1)) {
            nextState.setLastKnownPos(move.finalDestination());  
            nextState.ticketsSinceLastKnown.clear();
        }
        else if(rounds.get(nextState.currentRound - 2)) {
            nextState.setLastKnownPos(move.firstMove().destination());
            nextState.ticketsSinceLastKnown.clear();
            nextState.ticketsSinceLastKnown.add(move.firstMove().ticket());
        }
        else {
            nextState.ticketsSinceLastKnown.add(move.firstMove().ticket());
            nextState.ticketsSinceLastKnown.add(move.secondMove().ticket());
        }
        ScotlandYardPlayer player = nextState.cToP.get(move.colour());
        player.removeTicket(Ticket.DOUBLE);
        player.removeTicket(move.firstMove().ticket());
        player.removeTicket(move.secondMove().ticket());
        player.location(move.finalDestination());
        nextState.nextPlayer(nextState.cToP.get(move.colour()));
        return nextState;
    }
            

}
