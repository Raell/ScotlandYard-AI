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
    public static final int SEARCH_DEPTH = 10;
    
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
    }

    // TODO A sample player that selects a random move
    private static class MyPlayer implements Player {

            private final Random random = new Random();
            private final Map<Colour,Map<Ticket, Integer>> initTickets = new HashMap<>();
            //private ScotlandYardView view;
            private final Visualiser visualiser;
            private final ResourceProvider provider;
            
            MyPlayer(Visualiser visualiser, ResourceProvider provider) {
                this.visualiser = visualiser;
                this.provider = provider;
            }
            
            @Override
            public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
                            Consumer<Move> callback) {
                    // TODO do something interesting here; find the best move
                    // picks a random move
                    GameState state = new GameState(view);
                    if(view.getCurrentRound() == 0) {
                        view.getPlayers().forEach(p -> initTickets.put(p, state.getPlayerTickets(p)));
                    }
                    
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
