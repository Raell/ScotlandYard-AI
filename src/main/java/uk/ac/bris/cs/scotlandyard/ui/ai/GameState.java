/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Ticket;

/**
 *
 * @author lucas
 */
class GameState {
    private List<ScotlandYardPlayer> players;
    private final Map<Colour, ScotlandYardPlayer> cToP;
    private int currentRound;
    private final List<Boolean> rounds;
    private Set<Colour> stuckDetectives;

    GameState(ScotlandYardView view){
        players = new ArrayList<>();
        view.getPlayers().forEach(p -> players.add(makePlayer(view, p)));
        cToP = coloursToPlayers();
        currentRound = view.getCurrentRound();
        rounds = view.getRounds();
    }

    int getCurrentRound() {
        return currentRound;
    }

    void setStuck(Colour detective){
        stuckDetectives.add(detective);
    }

    Map<Colour, ScotlandYardPlayer> getColourMap() {
        return cToP;
    }

    Map<Ticket, Integer> getTickets(ScotlandYardView view, Colour colour){
        Map<Ticket, Integer> m = new HashMap<>();
        m.put(Ticket.BUS, view.getPlayerTickets(colour, Ticket.BUS).get());
        m.put(Ticket.DOUBLE, view.getPlayerTickets(colour, Ticket.DOUBLE).get());
        m.put(Ticket.SECRET, view.getPlayerTickets(colour, Ticket.SECRET).get());
        m.put(Ticket.TAXI, view.getPlayerTickets(colour, Ticket.TAXI).get());
        m.put(Ticket.UNDERGROUND, view.getPlayerTickets(colour, Ticket.UNDERGROUND).get());
        return m;
    }

    private ScotlandYardPlayer makePlayer(ScotlandYardView view, Colour colour){
        ScotlandYardPlayer player  = new ScotlandYardPlayer(null, colour, 
                view.getPlayerLocation(colour).get(),
                getTickets(view, colour));
        return player;
    }

    //maps the colours to the corresponding player
    private Map<Colour, ScotlandYardPlayer> coloursToPlayers(){
        Map<Colour, ScotlandYardPlayer> m = new HashMap<>();
        players.forEach((p) -> {
            m.put(p.colour(), p);
        });
        return m;
    }

    int location(Colour colour) {
        return cToP.get(colour).location();
    }

    int tickets(Colour colour, Ticket ticket) {
        return cToP.get(colour).tickets().get(ticket);
    }

    void nextRound(int x){
        currentRound =+ x;
    }

}
