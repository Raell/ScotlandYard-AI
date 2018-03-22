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
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.ai.ResourceProvider;
import uk.ac.bris.cs.scotlandyard.ai.Visualiser;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;

// TODO name the AI
@ManagedAI(value = "Athena", visualiserType = ManagedAI.VisualiserType.MAP_OVERLAP)
public class Athena implements PlayerFactory {
      
    private Visualiser visualiser;
    private ResourceProvider provider;
    
    // TODO create a new player here
    @Override
    public Player createPlayer(Colour colour) {
            return new MyPlayer(visualiser, provider);
    }

    @Override
    public List<Spectator> createSpectators(ScotlandYardView view) {
        List<Spectator> spectators = new ArrayList<>();
        return spectators;
    }

    @Override
    public void ready(Visualiser visualiser,
               ResourceProvider provider) {
        this.visualiser = visualiser;
        this.provider = provider;
        /*Pane pane = visualiser.surface();
        ImageView map = new ImageView();
        map.setImage(provider.getTicket(Ticket.SECRET));
        pane.getChildren().add(map);*/
    }

    // TODO A sample player that selects a random move
    private static class MyPlayer implements Player {

            private final Random random = new Random();
            private final Map<Colour,Map<Ticket, Integer>> initTickets = new HashMap<>();
            private ScotlandYardView view;
            private final Visualiser visualiser;
            private final ResourceProvider provider;
            
            MyPlayer(Visualiser visualiser, ResourceProvider provider) {
                this.visualiser = visualiser;
                this.provider = provider;
            }

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
                    currentState.setStuck(move.colour());
                    next = currentState;
                }
                return next;
            }

            private GameState nextState(GameState currentState, TicketMove move){
                if(move.colour().isMrX())
                    currentState.nextRound(1);
                ScotlandYardPlayer player = currentState.getColourMap().get(move.colour());
                player.removeTicket(move.ticket());
                player.location(move.destination());
                return currentState;
            }

            private GameState nextState(GameState currentState, TicketMove firstMove, TicketMove secondMove){
                if(secondMove.colour().isMrX())
                    currentState.nextRound(2);
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
                    /*if(view.getCurrentRound() == 0) {
                        GameState initState = new GameState(view);
                        view.getPlayers().forEach(p -> initTickets.put(p, initState.getTickets(view, p)));
                        this.view = view;
                    }*/


                    //GameState state = new GameState(view);
                    //if (view.getCurrentRound() > 0)
                        //System.out.println("Distance is: " + distance(view, state, 1));
                    int ran = random.nextInt(moves.size());
                    Move move = new ArrayList<>(moves).get(ran);
                    displayMrXMove(move, location);
                                                         
                    callback.accept(move);

            }
            
            private void displayMrXMove(Move move, int location) {
                List<Point2D> dest = new ArrayList<>();

                if(move.getClass() == TicketMove.class) {
                    TicketMove tMove = (TicketMove) move;
                    dest.add(provider.coordinateAtNode(tMove.destination()));
                }
                else {
                    DoubleMove dMove = (DoubleMove) move;
                    dest.add(provider.coordinateAtNode(dMove.firstMove().destination()));
                    dest.add(provider.coordinateAtNode(dMove.finalDestination()));
                }
                Pane pane = visualiser.surface();
                Platform.runLater(() -> {
                    Point2D start = provider.coordinateAtNode(location);
                    pane.getChildren().clear();
                    for(Point2D d : dest) {
                        Line l = new Line(start.getX(), start.getY(), d.getX(), d.getY());
                        l.setStroke(Color.VIOLET);
                        l.setStrokeWidth(10);
                        Circle c = new Circle(d.getX(), d.getY(), 30, Color.TRANSPARENT);
                        c.setStroke(Color.VIOLET);
                        c.setStrokeWidth(10);
                        pane.getChildren().add(l);
                        pane.getChildren().add(c);
                        start = d;
                    }
                });
            }
            
            
    }
}
