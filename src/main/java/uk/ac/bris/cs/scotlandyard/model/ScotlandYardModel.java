package uk.ac.bris.cs.scotlandyard.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {
    
    private List<Boolean> rounds;
    private Graph<Integer, Transport> graph;
    private PlayerConfiguration mrX;
    private Set<Colour> winningPlayer;
    private List<PlayerConfiguration> detectives;
    private List<Colour> players;

    public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
                    PlayerConfiguration mrX, PlayerConfiguration firstDetective,
                    PlayerConfiguration... restOfTheDetectives) {
            // TODO
            if(rounds.isEmpty()) {
                throw new IllegalArgumentException("Empty rounds");
            }
            this.rounds = requireNonNull(rounds);
            
            if(graph.isEmpty()) {
                throw new IllegalArgumentException("Empty map");
            }
            this.graph = requireNonNull(graph);
            
            this.winningPlayer = new HashSet<>();
            players = new ArrayList<>();
            
            List<PlayerConfiguration> detectives = new ArrayList<>();
            detectives.add(firstDetective);
            for(PlayerConfiguration detective : restOfTheDetectives)
                detectives.add(detective);
            
            this.detectives = detectives;
            playersValid(mrX, detectives);
            
            
    }
    
    private boolean isMissingTickets(PlayerConfiguration player, Map<Ticket, Integer> tickets){
        return(!(tickets.containsKey(Ticket.BUS) && tickets.containsKey(Ticket.DOUBLE) && 
                tickets.containsKey(Ticket.SECRET) && tickets.containsKey(Ticket.TAXI) && 
                tickets.containsKey(Ticket.UNDERGROUND)) );
    }
        
    private boolean playerHasTicket(PlayerConfiguration player, Ticket ticket){
        return(player.tickets.get(ticket) != 0);
    }
    
    private boolean playerNumTickets(PlayerConfiguration player, Ticket ticket, int number){
        return(player.tickets.get(ticket) == number);
    }
    
    private void playersValid(PlayerConfiguration mrX, List<PlayerConfiguration> detectives) {
        
        if(mrX.colour != Colour.BLACK) { // or mr.colour.isDetective()
            throw new IllegalArgumentException("MrX should be Black");
        }
        
        this.mrX = mrX;
        mrXTicketValid(mrX.tickets);
        players.add(Colour.BLACK);
        
        Set<Integer> locs = new HashSet<>();
        
        for(PlayerConfiguration detective : detectives) {
            requireNonNull(detective);
            locs.add(detective.location);
            if(isMissingTickets(detective, detective.tickets) || playerHasTicket(detective, Ticket.DOUBLE) || playerHasTicket(detective, Ticket.SECRET))
                throw new IllegalArgumentException("Detectives cannot have double or secret tickets");
            players.add(detective.colour);
        }
        
        if(locs.size() != detectives.size())
            throw new IllegalArgumentException("Player locations overlap.");
        
        
        
    } 
  
    private void mrXTicketValid(Map<Ticket, Integer> tickets) {
        
        if(isMissingTickets(mrX, tickets))
            throw new IllegalArgumentException("MrX is missing tickets");
        
        for(PlayerConfiguration detective : detectives) {
                if(detective.location == mrX.location)
                    throw new IllegalArgumentException("MrX and detective(s) overlap.");
            }
        
        /*if(!(playerNumTickets(mrX, Ticket.BUS, 3) && playerNumTickets(mrX, Ticket.DOUBLE, 2) && 
                playerNumTickets(mrX, Ticket.TAXI, 4) && playerNumTickets(mrX, Ticket.SECRET, 5) && 
                playerNumTickets(mrX, Ticket.UNDERGROUND, 3)) )
            throw new IllegalArgumentException("MrX has the wrong number of tickets.");*/
    }

    @Override
    public void registerSpectator(Spectator spectator) {
            // TODO
            throw new RuntimeException("Implement me");
    }

    @Override
    public void unregisterSpectator(Spectator spectator) {
            // TODO
            throw new RuntimeException("Implement me");
    }

    @Override
    public void startRotate() {
            for(PlayerConfiguration detective : detectives) {
                if(detective.location == mrX.location)
                    throw new IllegalArgumentException("MrX and detective(s) overlap.");
            }
    }

    @Override
    public Collection<Spectator> getSpectators() {
            // TODO
            throw new RuntimeException("Implement me");
    }

    @Override
    public List<Colour> getPlayers() {
            // TODO
            return Collections.unmodifiableList(players);
    }

    @Override
    public Set<Colour> getWinningPlayers() {
            // TODO
            return Collections.unmodifiableSet(winningPlayer);
    }

    private PlayerConfiguration playerFromColour(Colour colour) {
        if(colour.isMrX()) { return mrX; }
        else if(colour.isDetective()) {
            for(PlayerConfiguration detective : detectives) {
                if(detective.colour == colour) return detective;
            }
        } else {
            throw new IllegalArgumentException("Colour invalid");
        }
        return null;
    }
    
    @Override
    public Optional<Integer> getPlayerLocation(Colour colour) {
            Optional<Integer> loc = Optional.of(playerFromColour(colour).location);
            return loc;
    }

    @Override
    public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
        Optional<Integer> no = Optional.of(playerFromColour(colour).tickets.get(ticket));    
        return no;
            
    }
    
    @Override
    public boolean isGameOver() {
            // TODO
            throw new RuntimeException("Implement me");
    }

    @Override
    public Colour getCurrentPlayer() {
            // TODO
            throw new RuntimeException("Implement me");
    }

    @Override
    public int getCurrentRound() {
            // TODO
            throw new RuntimeException("Implement me");
    }

    @Override
    public List<Boolean> getRounds() {
            // TODO
            return Collections.unmodifiableList(rounds);
    }

    @Override
    public Graph<Integer, Transport> getGraph() {
            // TODO
            return new ImmutableGraph<>(graph);
    }

}
