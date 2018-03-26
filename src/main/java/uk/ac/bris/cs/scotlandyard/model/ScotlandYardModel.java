package uk.ac.bris.cs.scotlandyard.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;

public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {
    
    private final List<Boolean> rounds;
    private final Graph<Integer, Transport> graph;

    private List<Spectator> spectators;

    private ScotlandYardPlayer mrX;
    private List<ScotlandYardPlayer> detectives;
    private Map<Colour, ScotlandYardPlayer> cToP;

    private List<Colour> players;
    private Colour currentPlayer;
    private Set<Colour> winningPlayer;
    private final Set<Colour> stuckDetectives = new HashSet<>();

    private int currentRound;
    private int lastRevealed;

    private boolean gameOver;

    public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
                    PlayerConfiguration mrX, PlayerConfiguration firstDetective,
                    PlayerConfiguration... restOfTheDetectives) {
        
            //initialise the rounds for the game
            if(rounds.isEmpty()) {
                throw new IllegalArgumentException("Empty rounds.");
            }
            
            this.rounds = requireNonNull(rounds);
            this.currentRound = NOT_STARTED;
            this.lastRevealed = 0;
            
            //initialise the graph
            if(graph.isEmpty()) {
                throw new IllegalArgumentException("Empty map.");
            }
            
            this.graph = requireNonNull(graph);
            
            //initialise the players, and set the current player to mrX
            this.winningPlayer = new HashSet<>();
            this.currentPlayer = Colour.BLACK;
            players = new ArrayList<>();
            
            //initialise the list of spectators, currently empty
            spectators = new ArrayList<>();
            
            //initialise the detectives. There has to be at least one detective
            //in a game
            this.detectives = new ArrayList<>();
            requireNonNull(firstDetective);
            ScotlandYardPlayer firstDec = firstDetective.toScotlandYardPlayer();
            detectives.add(firstDec);
            
            //initialise the rest of the detectives
            for(PlayerConfiguration detective : restOfTheDetectives) {
                requireNonNull(detective);
                ScotlandYardPlayer dec = detective.toScotlandYardPlayer();
                detectives.add(dec);
            }
         
            //check whether the detectives can move
            boolean detectivesCanMove = detectives.stream()
                    .anyMatch(p -> validMoves(p, p.location()).iterator().next()
                            .getClass() != PassMove.class);
            gameOver = !detectivesCanMove;                      
            
            //initialise mrX and check if the players are valid
            requireNonNull(mrX);
            playersValid(mrX.toScotlandYardPlayer(), detectives);   
            
            //creates a map of colours to the corresponding players
            cToP = coloursToPlayers();
    }
    
    //maps the colours to the corresponding player
    private Map<Colour, ScotlandYardPlayer> coloursToPlayers(){
        Map<Colour, ScotlandYardPlayer> m = new HashMap<>();
        m.put(mrX.colour(), mrX);
        detectives.forEach((detective) -> {
            m.put(detective.colour(), detective);
        });
        return m;
    }
    
    //checks if the players are valid
    private void playersValid(ScotlandYardPlayer mrX, List<ScotlandYardPlayer> detectives) {    
        //checks mrX is black
        if(mrX.isDetective()) {
            throw new IllegalArgumentException("MrX should be the black player.");
        }
        
        //finishes initialising mrX
        this.mrX = mrX;
        if(mrX.isMissingTickets())
            throw new IllegalArgumentException("MrX is missing tickets.");
        players.add(Colour.BLACK);
        
        //finishes initialising the detectives
        for(ScotlandYardPlayer detective : detectives) {
            if(detective.isMissingTickets() || detective.hasTickets(Ticket.DOUBLE) || detective.hasTickets(Ticket.SECRET))
                throw new IllegalArgumentException("Detectives have incorrect tickets.");
            players.add(detective.colour());
        }
        
        //checks whether the players overlap
        if(detectiveLocations().size() != detectives.size() || detectives.stream().anyMatch(p -> p.location() == mrX.location()))
            throw new IllegalArgumentException("Player locations overlap.");
    }
   
    //checks whether a player has enough tickets to make the move
    private boolean hasValidTicket(ScotlandYardPlayer player, TicketMove move) {      
        return player.hasTickets(move.ticket());
    }
    
    //checks whether a player has enough tickets to make the move
    private boolean hasValidTicket(ScotlandYardPlayer player, DoubleMove move) {
        if(move.hasSameTicket())
            return player.hasTickets(move.firstMove().ticket(), 2);
        else
            return (player.hasTickets(move.firstMove().ticket()) && player.hasTickets(move.secondMove().ticket()));
    }
    
    //returns all the ticket moves a player can make, possibly with secret tickets 
    //if the player is mrX
    private Set<TicketMove> possibleStandardMoves(ScotlandYardPlayer player, int location, boolean secret) {   
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
    private Set<Move> possibleDoubleMoves(ScotlandYardPlayer player, Set<TicketMove> tMoves){
        Set<Move> doublemoves = new HashSet<>();
        
        //adds the doublemoves
        if(player.hasTickets(Ticket.DOUBLE) && currentRound + 2 <= rounds.size()){ 
            tMoves.forEach((firstMove) -> {
                //get the ticket moves from each of the first moves' destinations
                Set<TicketMove> secondMoves = possibleStandardMoves(player, firstMove.destination(), false);
                if(player.hasTickets(Ticket.SECRET))
                    secondMoves.addAll(possibleStandardMoves(player, firstMove.destination(), true));
                
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
    private Set<Move> mrXMoves(ScotlandYardPlayer player, int location) {
        Set<Move> moves = new HashSet<>();
        
        //adds all the standard and secret ticket moves mrX can make
        Set<TicketMove> tMoves = new HashSet<>();
        tMoves.addAll(possibleStandardMoves(player, location, false));

        if(player.hasTickets(Ticket.SECRET)){              
            tMoves.addAll(possibleStandardMoves(player, location, true));
        }
        
        moves.addAll(tMoves);

        //adds the double moves mrX can make
        Set<Move> doublemoves = possibleDoubleMoves(player, tMoves);
        moves.addAll(doublemoves);

        return moves;
    }
    
    //returns the valid mvoes for the given player
    private Set<Move> validMoves(ScotlandYardPlayer player, int location) {
        Set<Move> moves = new HashSet<>();
        
        if(player.isMrX())
            moves.addAll(mrXMoves(player, location));
        else {
            moves.addAll(possibleStandardMoves(player, location, false));
            //if the detective can't make a ticket move, they make a pass move
            if(moves.isEmpty())              
                moves.add(new PassMove(player.colour()));
        }
        
        return moves;
    }
       
    //register the spectators
    @Override
    public void registerSpectator(Spectator spectator) {
            requireNonNull(spectator);
            if(!spectators.contains(spectator))
                spectators.add(spectator);
            else
                throw new IllegalArgumentException("Already registered specator.");
    }

    //unregisters the spectators
    @Override
    public void unregisterSpectator(Spectator spectator) {
            requireNonNull(spectator);
            if(spectators.contains(spectator))
                spectators.remove(spectator);
            else
                throw new IllegalArgumentException("Invalid specatator.");
    }
    
    //gets the spectators
    @Override
    public Collection<Spectator> getSpectators() {
        return Collections.unmodifiableCollection(spectators);
    }

    //gets the set of the detective colours
    private Set<Colour> getDetectiveColours() {
        return new HashSet<>(players.subList(1, players.size()));      
    }
    
    //gets the set of mrX's colour
    private Set<Colour> getMrXColour() {
        return new HashSet<>(players.subList(0, 1));      
    }
    
    //gets the players
    @Override
    public List<Colour> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    //gets the current player
    @Override
    public Colour getCurrentPlayer() {
        return currentPlayer;
    }
        
    //calculate the next player
    private void nextPlayer(Colour prevPlayer) {      
        int index = players.indexOf(prevPlayer);
        if(index + 1 < players.size())
            currentPlayer =  players.get(index + 1);
        else
            currentPlayer = players.get(0);
    }
    
    //gets the winning players
    @Override
    public Set<Colour> getWinningPlayers() {
        return Collections.unmodifiableSet(winningPlayer);
    }
    
    //returns a set of the detectives locations, to make checking if they overlap
    //easier
    private Set<Integer> detectiveLocations(){
        Set<Integer> locs = new HashSet<>();
        for(ScotlandYardPlayer detective : detectives) {
            locs.add(detective.location());
        }
        return locs;
    }
    
    //gets the player location
    @Override
    public Optional<Integer> getPlayerLocation(Colour colour) {   
        ScotlandYardPlayer player = cToP.get(colour);
        
        //if it's a detective or a reveal round, show the correct location
        //otherwise, if it's mrX then show the last revealed location
        if(player != null)
            if(!colour.isMrX() || (currentRound != 0 && rounds.get(currentRound - 1))) {
                if(colour.isMrX())
                    lastRevealed = player.location();
                return Optional.ofNullable(player.location());
            } 
            else
                return Optional.ofNullable(lastRevealed);
        else
            return Optional.empty();  
    }

    //gets the player tickets
    @Override
    public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
        ScotlandYardPlayer player = cToP.get(colour);
        
        if(player != null) 
            return Optional.ofNullable(player.tickets().get(ticket));
        else
            return Optional.empty();            
    }
    
    //called when it's game over
    private void gameOver(Set<Colour> winners) {
        gameOver = true;
        winningPlayer = winners;
    }
    
    //checks whether the game is over
    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    //gets the current round
    @Override
    public int getCurrentRound() {
        return currentRound;
    }

    //gets the list of rounds
    @Override
    public List<Boolean> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    //gets the graph
    @Override
    public Graph<Integer, Transport> getGraph() {
        return new ImmutableGraph<>(graph);
    }
    
    //stats a rotation by calling make move on mrX
    @Override
    public void startRotate() {
        //makes sure the game can't be over before it begins
        if(gameOver && currentRound == 0)
                throw new IllegalStateException("Detectives cannot move.");

        mrX.player().makeMove(this, mrX.location(), validMoves(mrX, mrX.location()), this);            
    }
    
    //gets the hidden move
    private Move hiddenMove(Move move) {
            if(move.getClass() == DoubleMove.class)
                return hiddenMove((DoubleMove) move);
            else
                return hiddenMove((TicketMove) move);
    }
    
    private Move hiddenMove(DoubleMove move) {
        int firstDes = lastRevealed;
        int secondDes = lastRevealed;
        
        //if the first round is a reveal round, reveal that destination and
        //update the second one
        if(rounds.get(currentRound)) {
            firstDes = move.firstMove().destination();
            if(!rounds.get(currentRound + 1))
                secondDes = firstDes;
        }
        
        //if the second round is a reveal round, reveal that destination, too
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
    
    //deals with mrX making a move
    private void acceptMrXCallback(Move move) {      
        //if it's a ticket move, increment the round and update mrX's variables
        //otherwise remove a double ticket
        if(move.getClass() == TicketMove.class) {
            currentRound++;
            TicketMove tMove = (TicketMove) move;
            mrX.removeTicket(tMove.ticket());
            mrX.location(moveDestination(mrX, move)); 
        }
        else
            mrX.removeTicket(Ticket.DOUBLE);
        
        //hide the move to show it to the spectator
        Move shownMove = hiddenMove(move);
                    
        spectators.forEach((spectator) -> {
            if(move.getClass() == TicketMove.class)
                spectator.onRoundStarted(this, currentRound);
            spectator.onMoveMade(this, shownMove);
        });            
      
        //if it's a double move, run the callback again with each ticket move
        if(move.getClass() == DoubleMove.class) {
            DoubleMove doubleMove = (DoubleMove) move;           
            acceptMrXCallback(doubleMove.firstMove());
            acceptMrXCallback(doubleMove.secondMove());
        }  
    }
    
    //deals with a detective making a move
    private void acceptDetectiveCallback(Move move) {
        //updates the player's location
        ScotlandYardPlayer player = cToP.get(move.colour());
        player.location(moveDestination(player, move));   
        
        //removes the ticket and gives it to mrX
        if(move.getClass() == TicketMove.class) {
            TicketMove tMove = (TicketMove) move;
            player.removeTicket(tMove.ticket());
            mrX.addTicket(tMove.ticket());
        }
               
        //if the only move is a pass move, add the detective to stuck detectives      
        if(validMoves(player, player.location()).iterator().next().getClass() == PassMove.class)
            stuckDetectives.add(player.colour());
        
        //check if the detectives have won
        if(detectiveLocations().contains(mrX.location()) || (currentPlayer.isMrX() && validMoves(mrX, mrX.location()).isEmpty()))
            gameOver(getDetectiveColours());
        
        //check whether mrX has won
        if(currentPlayer.isMrX() && currentRound == rounds.size()) {
            gameOver(getMrXColour());
        }
        
        //inform the spectators
        spectators.forEach((spectator) -> {
            spectator.onMoveMade(this, move);
        });          
    }
    
    //deals with the callback
    @Override
    public void accept(Move move) {
        //if the move returned isn't valid, cause an exception
        if(!isValidMove(cToP.get(move.colour()), move))
            throw new IllegalArgumentException("Illegal move.");
        
        //update the currentPlayer
        nextPlayer(move.colour());             
        
        //run the corresponding callback
        if(move.colour() == Colour.BLACK)
            acceptMrXCallback(move);
        else
            acceptDetectiveCallback(move);
        
        //checks whether mrX has won by the detectives all being stuck
        if((stuckDetectives.size() == detectives.size()) && !isGameOver()) {
            gameOver(getMrXColour());
        } 
        
        //if the next player is a detective, call makeMove on them, otherwise
        //inform the spectators of rotation complete
        if(!currentPlayer.isMrX() && !gameOver){
            ScotlandYardPlayer sYPlayer = cToP.get(currentPlayer);
            sYPlayer.player().makeMove(this, sYPlayer.location(), validMoves(sYPlayer, sYPlayer.location()), this); 
        } else if(!gameOver){
            spectators.forEach((spectator) -> {
                spectator.onRotationComplete(this);
            });
        } else {
            spectators.forEach((spectator) -> {
                spectator.onGameOver(this, winningPlayer);
            });
        } 
    }
    
    //uses a visitor to get the move destination, returning the player's current
    //location if it's a pass move
    private int moveDestination(ScotlandYardPlayer player, Move move){
        DestinationVis v = new DestinationVis();
        move.visit(v);
        if(v.destination == -1) return player.location();
        else return v.destination;
    }
    
    //checks whether a move is valid
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
