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
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Ticket;

/**
 *
 * @author lucas
 */
public class GameState {
    private List<ScotlandYardPlayer> players = new ArrayList<>();
    private Map<Colour, ScotlandYardPlayer> cToP;
    
    public GameState(ScotlandYardView view){
        view.getPlayers().forEach(p -> players.add(makePlayer(view, p)));
        cToP = coloursToPlayers();
    }
    
    private Map<Ticket, Integer> getTickets(ScotlandYardView view, Colour colour){
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
    
    public int location(Colour colour) {
        return cToP.get(colour).location();
    }
    
    public int tickets(Colour colour, Ticket ticket) {
        return cToP.get(colour).tickets().get(ticket);
    }
    
}
