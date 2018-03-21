package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;

// TODO name the AI
@ManagedAI("Athena")
public class Athena implements PlayerFactory {
        
	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
		return new MyPlayer();
	}

	// TODO A sample player that selects a random move
	private static class MyPlayer implements Player {

		private final Random random = new Random();
                private final Map<Colour,Map<Ticket, Integer>> initTickets = new HashMap<>();
                
                private GameState nextState(GameState currentState, Move move){
                    GameState next;
                    if(move.getClass() == DoubleMove.class){
                        DoubleMove doubleMove = (DoubleMove) move;
                        TicketMove firstMove = (TicketMove) doubleMove.firstMove();
                        TicketMove secondMove = (TicketMove) doubleMove.secondMove();
                        currentState.getColourMap().get(move.colour()).removeTicket(Ticket.DOUBLE);
                        next = nextState(currentState, firstMove, secondMove);
                    } else if(move.getClass() == TicketMove.class){
                        next = nextState(currentState, (TicketMove) move);      
                    } else {
                        next = currentState;
                    }
                    return next;
                }
                
                private GameState nextState(GameState currentState, TicketMove move){
                    ScotlandYardPlayer player = currentState.getColourMap().get(move.colour());
                    player.removeTicket(move.ticket());
                    player.location(move.destination());
                    return currentState;
                }
                
                private GameState nextState(GameState currentState, TicketMove firstMove, TicketMove secondMove){
                    ScotlandYardPlayer player = currentState.getColourMap().get(secondMove.colour());
                    player.removeTicket(firstMove.ticket());
                    player.removeTicket(secondMove.ticket());
                    player.location(secondMove.destination());
                    return currentState;
                }
                
                /*private int distance(ScotlandYardView view, GameState state, int destination){
                    ScotlandYardPlayer player = state.cToP.get(view.getCurrentPlayer());
                    Graph<Integer, Transport> graph = view.getGraph();
                    Node currentNode = graph.getNode(player.location());
                    Node destinationNode = graph.getNode(destination);
                    Collection<Edge<Integer, Transport>> connected = graph.getEdgesFrom(currentNode);
                    Set<Edge<Integer, Transport>> visited = new HashSet<>();
                    int distance = 0;
                    
                    while(!visited.contains(destinationNode)){
                        distance += 1;
                        visited.addAll(connected);
                        Collection<Edge<Integer, Transport>> newConnected = new HashSet<>();
                        connected.forEach(e -> newConnected.addAll(graph.getEdgesFrom(e.destination())));
                        connected = newConnected;
                    }

                    return distance;
                }*/
                
                
                @Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
				Consumer<Move> callback) {
			// TODO do something interesting here; find the best move
			// picks a random move
                        if(view.getCurrentRound() == 0) {
                            GameState initState = new GameState(view);
                            view.getPlayers().forEach(p -> initTickets.put(p, initState.getTickets(view, p)));
                            //ValidMoves.setGraph(view.getGraph());
                        }
                        
                        
                        
			callback.accept(new ArrayList<>(moves).get(random.nextInt(moves.size())));

		}
                
                private static class ValidMoves{
                    Graph<Integer, Transport> graph;
                    List<ScotlandYardPlayer> detectives;
                    List<Boolean> rounds;
                    
                    private void setGraph(Graph<Integer, Transport> g) {
                        graph = g;
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
                    private static Set<TicketMove> possibleStandardMoves(ScotlandYardPlayer player, int location, boolean secret, 
                            Graph<Integer, Transport> graph, List<ScotlandYardPlayer> detectives) {   
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
                            int currentRound, List<Boolean> rounds, 
                            Graph<Integer, Transport> graph, List<ScotlandYardPlayer> detectives){
                        Set<Move> doublemoves = new HashSet<>();

                        //adds the doublemoves
                        if(player.hasTickets(Ticket.DOUBLE) && currentRound + 2 <= rounds.size()){ 
                            tMoves.forEach((firstMove) -> {
                                //get the ticket moves from each of the first moves' destinations
                                Set<TicketMove> secondMoves = possibleStandardMoves(player, firstMove.destination(), false, graph, detectives);
                                if(player.hasTickets(Ticket.SECRET))
                                    secondMoves.addAll(possibleStandardMoves(player, firstMove.destination(), true, graph, detectives));

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
                            int currentRound, List<Boolean> rounds,
                            Graph<Integer, Transport> graph, List<ScotlandYardPlayer> detectives) {
                        Set<Move> moves = new HashSet<>();

                        //adds all the standard and secret ticket moves mrX can make
                        Set<TicketMove> tMoves = new HashSet<>();
                        tMoves.addAll(possibleStandardMoves(player, location, false, graph, detectives));

                        if(player.hasTickets(Ticket.SECRET)){              
                            tMoves.addAll(possibleStandardMoves(player, location, true, graph, detectives));
                        }

                        moves.addAll(tMoves);

                        //adds the double moves mrX can make
                        Set<Move> doublemoves = possibleDoubleMoves(player, tMoves, currentRound, rounds, graph, detectives);
                        moves.addAll(doublemoves);

                        return moves;
                    }

                    //returns the valid mvoes for the given player
                    private static Set<Move> validMoves(ScotlandYardPlayer player, int location, int currentRound,
                            List<Boolean> rounds, Graph<Integer, Transport> graph,
                            List<ScotlandYardPlayer> detectives) {
                        Set<Move> moves = new HashSet<>();

                        if(player.isMrX())
                            moves.addAll(mrXMoves(player, location, currentRound, rounds, graph, detectives));
                        else {
                            moves.addAll(possibleStandardMoves(player, location, false, graph, detectives));
                            //if the detective can't make a ticket move, they make a pass move
                            if(moves.isEmpty())              
                                moves.add(new PassMove(player.colour()));
                        }

                        return moves;
                    }
                }
                
                private class GameState {
                    private List<ScotlandYardPlayer> players = new ArrayList<>();
                    private Map<Colour, ScotlandYardPlayer> cToP;

                    public GameState(ScotlandYardView view){
                        view.getPlayers().forEach(p -> players.add(makePlayer(view, p)));
                        cToP = coloursToPlayers();
                    }
                    
                    public Map<Colour, ScotlandYardPlayer> getColourMap() {
                        return cToP;
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
                
                private class GameTree<V, E> {
                    
                    V GameState;
                    E value;
                    int playerCount;
                    int depth;
                    List<GameTree<V, E>> children;
                    
                    
                    public GameTree(V state, E value, int playerCount, int depth) {
                        GameState = state;
                        this.value = value;
                        this.playerCount = playerCount;
                        this.depth = depth;
                    }
                    
                    public Boolean add(V state, E value) {
                        return children.add(new GameTree<>(state, value, playerCount, depth));
                    }
                    
                    public Boolean isMaximiser() {
                        int height = getHeight();
                        return (height % playerCount == depth % playerCount);
                    }
                    
                    private int getHeight() {
                        if(children.isEmpty())
                            return 0;
                        else {
                            int height = 0;
                            for(GameTree<V, E> c : children) {
                                height = Math.max(height, c.getHeight());
                            }
                            return height + 1;
                        }
                    }
                                       
                }
	}
}