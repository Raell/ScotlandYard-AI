package uk.ac.bris.cs.scotlandyard.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {
    
    private final List<Boolean> rounds;
    private final Graph<Integer, Transport> graph;
    private ScotlandYardPlayer mrX;
    private Set<Colour> winningPlayer;
    private List<ScotlandYardPlayer> detectives;
    private List<Colour> players;
    private List<Spectator> spectators;
    private Colour currentPlayer;
    private final Set<Colour> stuckDetectives = new HashSet<>();
    private int currentRound;
    private int lastRevealed;
    private boolean gameOver;
    private boolean callback;

    public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
                    PlayerConfiguration mrX, PlayerConfiguration firstDetective,
                    PlayerConfiguration... restOfTheDetectives) {
            // TODO
            if(rounds.isEmpty()) {
                throw new IllegalArgumentException("Empty rounds");
            }
            this.rounds = requireNonNull(rounds);
            this.currentRound = NOT_STARTED;
            this.lastRevealed = 0;
            
            if(graph.isEmpty()) {
                throw new IllegalArgumentException("Empty map");
            }
            this.graph = requireNonNull(graph);
            
            this.winningPlayer = new HashSet<>();
            this.currentPlayer = Colour.BLACK;
            players = new ArrayList<>();
            spectators = new ArrayList<>();
                      
            this.detectives = new ArrayList<>();
            requireNonNull(firstDetective);
            ScotlandYardPlayer firstDec = firstDetective.toScotlandYardPlayer();
            detectives.add(firstDec);
  
            boolean detectivesCanMove = validMoves(firstDec, firstDec.location()).iterator().next().getClass() != PassMove.class;
            
            for(PlayerConfiguration detective : restOfTheDetectives) {
                requireNonNull(detective);
                ScotlandYardPlayer dec = detective.toScotlandYardPlayer();
                detectivesCanMove = detectivesCanMove || validMoves(dec, dec.location()).iterator().next().getClass() != PassMove.class;
                detectives.add(dec);
            }
         
            gameOver = !detectivesCanMove;                      
            
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
        mrXTicketValid();
        players.add(Colour.BLACK);
        
        for(ScotlandYardPlayer detective : detectives) {
            if(detective.isMissingTickets() || detective.hasTickets(Ticket.DOUBLE) || detective.hasTickets(Ticket.SECRET))
                throw new IllegalArgumentException("Detectives cannot have double or secret tickets");
            players.add(detective.colour());
        }
        
        if(detectiveLocations().size() != detectives.size())
            throw new IllegalArgumentException("Player locations overlap.");  
        
    } 
  
    private void mrXTicketValid() {
        
        if(mrX.isMissingTickets())
            throw new IllegalArgumentException("MrX is missing tickets");
        
        for(ScotlandYardPlayer detective : detectives) {
                if(detective.location() == mrX.location())
                    throw new IllegalArgumentException("MrX and detective(s) overlap.");
            }
    }

    @Override
    public void registerSpectator(Spectator spectator) {
            requireNonNull(spectator);
            if(!spectators.contains(spectator))
                spectators.add(spectator);
            else
                throw new IllegalArgumentException("Already registered specator");
    }

    @Override
    public void unregisterSpectator(Spectator spectator) {
            requireNonNull(spectator);
            if(spectators.contains(spectator))
                spectators.remove(spectator);
            else
                throw new IllegalArgumentException("Invalid specatator");
    }
    
    private boolean playerAtNode(Node<Integer> node){
        for(ScotlandYardPlayer detective : detectives) {
            if(detective.location() == node.value()) return true;
        }
        return false;
    }
    
    private Collection<Edge<Integer, Transport>> connectedEdges(Node<Integer> locNode, Graph<Integer, Transport> graph){
        //gets connected edges
        Collection<Edge<Integer, Transport>> fromEdges = graph.getEdgesFrom(locNode);
        return fromEdges;
    }
    
    private Set<Move> mrXMoves(ScotlandYardPlayer player, int location) {
          
        Set<Move> moves = new HashSet<>();
        Set<TicketMove> tMoves = new HashSet<>();
        tMoves.addAll(possibleStandardMoves(player, location, false));

        if(player.hasTickets(Ticket.SECRET)){              
            tMoves.addAll(possibleStandardMoves(player, location, true));
        } 
        
        moves.addAll(tMoves);

        if(player.hasTickets(Ticket.DOUBLE) && currentRound + 2 <= rounds.size()){ 
            Set<Move> doublemoves = new HashSet<>();
            for(TicketMove firstMove : tMoves) {                                      
                
                Set<TicketMove> secondMoves = possibleStandardMoves(player, firstMove.destination(), false);
                
                if(player.hasTickets(Ticket.SECRET))
                    secondMoves.addAll(possibleStandardMoves(player, firstMove.destination(), true));
                
                for(TicketMove secondMove : secondMoves) {
                    DoubleMove doublemove = new DoubleMove(player.colour(), firstMove, secondMove);
                    if(hasValidTicket(player, doublemove))
                        doublemoves.add(doublemove);
                }
            }
            moves.addAll(doublemoves);
        } 

        return moves;
    }
    
    private Set<Move> validMoves(ScotlandYardPlayer player, int location) {
        Set<Move> moves = new HashSet<>();
        if(player.isMrX()) {
            moves.addAll(mrXMoves(player, location));
        }
        else {
            moves.addAll(possibleStandardMoves(player, location, false));
            if(moves.isEmpty())              
                moves.add(new PassMove(player.colour()));
        }
        
        return moves;
    }
    
    private Set<TicketMove> possibleStandardMoves(ScotlandYardPlayer player, int location, boolean secret) {
        
        Set<TicketMove> moves = new HashSet<>();
        
        Node<Integer> locNode = graph.getNode(location);
        
        Collection<Edge<Integer, Transport>> fromEdges = connectedEdges(locNode, graph);
                 
        for(Edge<Integer, Transport> edge : fromEdges) {
            
            Ticket ticket = secret ? Ticket.SECRET : Ticket.fromTransport(edge.data());
            
            TicketMove move = new TicketMove(player.colour(), ticket, edge.destination().value());
            
            if(!playerAtNode(edge.destination()) && hasValidTicket(player, move))
                moves.add(move);
            
        }      
        return moves;
        
    }
    
    private boolean hasValidTicket(ScotlandYardPlayer player, TicketMove move) {      
        return player.hasTickets(move.ticket());
    }
    
    private boolean hasValidTicket(ScotlandYardPlayer player, DoubleMove move) {
        if(move.hasSameTicket())
            return player.hasTickets(move.firstMove().ticket(), 2);
        else
            return (player.hasTickets(move.firstMove().ticket()) && player.hasTickets(move.secondMove().ticket()));
    }
    
    @Override
    public void startRotate() {

        if(gameOver && currentRound == 0)
                throw new IllegalStateException("Detectives cannot move");
        
        ScotlandYardPlayer currplayer = playerFromColour(currentPlayer);
        currplayer.player().makeMove(this, currplayer.location(), validMoves(currplayer, currplayer.location()), this);

        if(gameOver)
            return;                
    }
    
    private Set<Colour> getDetectiveColours() {
        return new HashSet<>(players.subList(1, players.size()));      
    }
    
    private Set<Colour> getMrXColours() {
        return new HashSet<>(players.subList(0, 1));      
    }
    
    @Override
    public Collection<Spectator> getSpectators() {
            return Collections.unmodifiableCollection(spectators);
    }

    @Override
    public List<Colour> getPlayers() {
            return Collections.unmodifiableList(players);
    }

    @Override
    public Set<Colour> getWinningPlayers() {
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
            if(colour != Colour.BLACK || (currentRound != 0 && rounds.get(currentRound - 1))) {
                if(colour == Colour.BLACK)
                    lastRevealed = player.location();
                return Optional.ofNullable(player.location());
            } 
            else {
                return Optional.ofNullable(lastRevealed);
            }
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
    
    private void gameOver(Set<Colour> winners) {
        gameOver = true;
        winningPlayer = winners;
    }
    
    @Override
    public boolean isGameOver() {
            return gameOver;
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
    
    private Move hiddenMove(Move move) {
            if(move.getClass() == DoubleMove.class)
                return hiddenMove((DoubleMove) move);
            else
                return hiddenMove((TicketMove) move);
    }
    
    private Move hiddenMove(DoubleMove move) {
        int firstDes = lastRevealed;
        int secondDes = lastRevealed;
        if(rounds.get(currentRound)) {
            firstDes = move.firstMove().destination();
            if(!rounds.get(currentRound + 1))
                secondDes = firstDes;
        }
        if(rounds.get(currentRound + 1))
            secondDes = move.secondMove().destination();
        return new DoubleMove(move.colour(), move.firstMove().ticket(), firstDes, move.secondMove().ticket(), secondDes);
    }
    
    private Move hiddenMove(TicketMove move) {
        int des = lastRevealed;
        if(currentRound > 0 && rounds.get(currentRound - 1))
            des = move.destination();
        return new TicketMove(move.colour(), move.ticket(), des);
    }
    
    private void acceptMrXCallback(Move move) {      
        
        if(move.getClass() == TicketMove.class) {
            currentRound++;
            TicketMove tMove = (TicketMove) move;
            mrX.removeTicket(tMove.ticket());
            mrX.location(moveDestination(mrX, move)); 
        }
        else {
            mrX.removeTicket(Ticket.DOUBLE);
        }
        
        Move shownMove = hiddenMove(move);
                    
        for(Spectator spectator : spectators) {              
            if(move.getClass() == TicketMove.class)
                spectator.onRoundStarted(this, currentRound);
            spectator.onMoveMade(this, shownMove);              
        }            
      
        if(move.getClass() == DoubleMove.class) {
            DoubleMove doubleMove = (DoubleMove) move;           
            acceptMrXCallback(doubleMove.firstMove());
            acceptMrXCallback(doubleMove.secondMove());
        }
        
    }
    
    private void acceptDetectiveCallback(Move move) {
        
        ScotlandYardPlayer player = playerFromColour(move.colour());
        player.location(moveDestination(player, move));   
        
        if(move.getClass() == TicketMove.class) {
            TicketMove tMove = (TicketMove) move;
            player.removeTicket(tMove.ticket());
            mrX.addTicket(tMove.ticket());
        }
               
              
        if(validMoves(player, player.location()).iterator().next().getClass() == PassMove.class)
            stuckDetectives.add(player.colour());
               
        if(detectiveLocations().contains(mrX.location()) || (currentPlayer == Colour.BLACK && validMoves(mrX, mrX.location()).isEmpty()))
            gameOver(getDetectiveColours());
        
        if(currentPlayer == Colour.BLACK && currentRound == rounds.size()) {
            gameOver(getMrXColours());
        }
        
        spectators.forEach((spectator) -> {
            spectator.onMoveMade(this, move);
        });
               
    }
    
    @Override
    public void accept(Move move) {

        if(!isValidMove(playerFromColour(move.colour()), move))
            throw new IllegalArgumentException("Illegal Move");
        
        nextPlayer(move.colour());             
        
        if(move.colour() == Colour.BLACK)
            acceptMrXCallback(move);
        
        else
            acceptDetectiveCallback(move);
        
        if(((currentPlayer.isMrX() && currentRound == rounds.size()) || stuckDetectives.size() == detectives.size()) && !isGameOver()) {
            gameOver(getMrXColours());
        } 
        
        if(gameOver) {
            spectators.forEach((spectator) -> {
                spectator.onGameOver(this, winningPlayer);
            });
        }
        else {
            if(currentPlayer.isMrX()) {
                spectators.forEach((spectator) -> {
                       spectator.onRotationComplete(this);
                });
            } 
            else
                startRotate();
        }       
        
    }
    
    private int moveDestination(ScotlandYardPlayer player, Move move) {
        if(move.getClass() == TicketMove.class)
            return moveDestination((TicketMove) move);
        else if(move.getClass() == DoubleMove.class)
            return moveDestination((DoubleMove) move);
        else
            return player.location();
    }
    
    private int moveDestination(TicketMove move) {
        return move.destination();
    }
    
    private int moveDestination(DoubleMove move) {
        return move.finalDestination();
    }
    
    
    private void nextPlayer(Colour prevPlayer) {      
        int index = players.indexOf(prevPlayer);
        if(index + 1 < players.size())
            currentPlayer =  players.get(index + 1);
        else
            currentPlayer = players.get(0);
    }
    
    private boolean isValidMove(ScotlandYardPlayer player, Move move) {
        if(move.getClass() == TicketMove.class)
            return isValidMove(player, (TicketMove) move);
        else if(move.getClass() == DoubleMove.class)
            return isValidMove(player, (DoubleMove) move);
        else
            return isValidMove(player, (PassMove) move);
    }
    
    private boolean isValidMove(ScotlandYardPlayer player, PassMove move) {
        return (validMoves(player, player.location()).contains(move));
    }
    
    private boolean isValidMove(ScotlandYardPlayer player, TicketMove move) {
        return (validMoves(player, player.location()).contains(move));
    }
    
    private boolean isValidMove(ScotlandYardPlayer player, DoubleMove move) {      
        return (validMoves(player, player.location()).contains(move));
    }

}
