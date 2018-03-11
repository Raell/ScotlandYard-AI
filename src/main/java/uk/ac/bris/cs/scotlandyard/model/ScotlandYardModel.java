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
import uk.ac.bris.cs.gamekit.graph.Edge;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {
    
    private List<Boolean> rounds;
    private Graph<Integer, Transport> graph;
    private ScotlandYardPlayer mrX;
    private Set<Colour> winningPlayer;
    private List<ScotlandYardPlayer> detectives;
    private List<Colour> players;
    private Colour currentPlayer;
    private int currentRound;

    public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
                    PlayerConfiguration mrX, PlayerConfiguration firstDetective,
                    PlayerConfiguration... restOfTheDetectives) {
            // TODO
            if(rounds.isEmpty()) {
                throw new IllegalArgumentException("Empty rounds");
            }
            this.rounds = requireNonNull(rounds);
            this.currentRound = 0;
            
            if(graph.isEmpty()) {
                throw new IllegalArgumentException("Empty map");
            }
            this.graph = requireNonNull(graph);
            
            this.winningPlayer = new HashSet<>();
            this.currentPlayer = Colour.BLACK;
            players = new ArrayList<>();
            
            List<ScotlandYardPlayer> detectives = new ArrayList<>();
            requireNonNull(firstDetective);
            detectives.add(firstDetective.toScotlandYardPlayer());
            for(PlayerConfiguration detective : restOfTheDetectives) {
                requireNonNull(detective);
                detectives.add(detective.toScotlandYardPlayer());
            }
            
            this.detectives = detectives;
            requireNonNull(mrX);
            if(mrX != null)
                playersValid(mrX.toScotlandYardPlayer(), detectives);
            
            
    }
         
    private Set<Integer> detectiveLocations(){
        Set<Integer> locs = new HashSet<>();
        for(ScotlandYardPlayer detective : detectives) {
            locs.add(detective.location());
        }
        return locs;
    }
    
    private void playersValid(ScotlandYardPlayer mrX, List<ScotlandYardPlayer> detectives) {
        
        
        if(mrX.isDetective()) { // or mr.colour.isDetective()
            throw new IllegalArgumentException("MrX should be Black");
        }
        
        this.mrX = mrX;
        mrXTicketValid(mrX.tickets());
        players.add(Colour.BLACK);
        
        for(ScotlandYardPlayer detective : detectives) {
            if(detective.isMissingTickets() || detective.hasTickets(Ticket.DOUBLE) || detective.hasTickets(Ticket.SECRET))
                throw new IllegalArgumentException("Detectives cannot have double or secret tickets");
            players.add(detective.colour());
        }
        
        if(detectiveLocations().size() != detectives.size())
            throw new IllegalArgumentException("Player locations overlap.");
        
        
        
    } 
  
    private void mrXTicketValid(Map<Ticket, Integer> tickets) {
        
        if(mrX.isMissingTickets())
            throw new IllegalArgumentException("MrX is missing tickets");
        
        for(ScotlandYardPlayer detective : detectives) {
                if(detective.location() == mrX.location())
                    throw new IllegalArgumentException("MrX and detective(s) overlap.");
            }
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
            for(ScotlandYardPlayer detective : detectives) {
                if(detective.location() == mrX.location())
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

    private ScotlandYardPlayer playerFromColour(Colour colour) {
        if(colour.isMrX()) { return mrX; }
        else {
            for(ScotlandYardPlayer detective : detectives) {
                if(detective.colour() == colour) return detective;
            }
        }
        return null;
    }
    
    @Override
    public Optional<Integer> getPlayerLocation(Colour colour) {
            
        ScotlandYardPlayer player = playerFromColour(colour);
        
        if(player != null) 
            if(colour != Colour.BLACK)
                return Optional.ofNullable(player.location());
            else
                return Optional.ofNullable(0);
        else
            return Optional.empty();
            
    }

    @Override
    public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
        
        ScotlandYardPlayer player = playerFromColour(colour);
        
        if(player != null) 
            return Optional.ofNullable(player.tickets().get(ticket));
        else
            return Optional.empty();
                   
    }
    
    /*private Set<Integer> getConnectedLocs(Integer loc) {
        Node locNode = graph.getNode(loc);
        Collection<Edge> fromEdges = graph.getEdgesFrom(locNode);
        Set<Integer> setLocs = new HashSet<>();
        for(Edge edge : fromEdges) {
            setLocs.add((Integer) edge.destination().value());
        }
        return setLocs;
    }*/
    
    @Override
    public boolean isGameOver() {
            for(int i : detectiveLocations()) { System.out.println(i); }
            if(detectiveLocations().contains(mrX.location())) return true;
            else return false;
    }

    @Override
    public Colour getCurrentPlayer() {
            return currentPlayer;
    }

    @Override
    public int getCurrentRound() {
            return currentRound;
    }

    @Override
    public List<Boolean> getRounds() {
            return Collections.unmodifiableList(rounds);
    }

    @Override
    public Graph<Integer, Transport> getGraph() {
            return new ImmutableGraph<>(graph);
    }

}
