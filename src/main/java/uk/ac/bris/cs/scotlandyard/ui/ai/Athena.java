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
    
    private final MyPlayer player = new MyPlayer();
    //The number of connections from the root to the bottom of tree (root = 0)
    private static final int SEARCH_DEPTH = 6;
    private static final double DANGER_SCORE = 30.0;
    
    // TODO create a new player here
    @Override
    public Player createPlayer(Colour colour) {
        if(colour != Colour.BLACK)
            throw new IllegalArgumentException("Can only use Athena for Mr X.");
        return player;
    }

    @Override
    public List<Spectator> createSpectators(ScotlandYardView view) {
        List<Spectator> spectators = new ArrayList<>();
        spectators.add(new AISpectator(this));
        return spectators;
    }

    @Override
    public void ready(Visualiser visualiser,
               ResourceProvider provider) {
        player.updateVisualiserAndProvider(visualiser, provider);
    }
    
    public GameTree getRoot() {
        // Not immutable but to make it would cause a lot of memory to be used as the entire tree needs to be copied
        return player.root;
    }
    
    public void setRoot(GameTree newRoot) {
        player.root = newRoot;
    }
    
    public void toRebuildTree() {
        System.out.println("Rebuild");
        player.rebuildTree = true;
    }

    // TODO A sample player that selects a random move
    private static class MyPlayer implements Player {

            private final Random random = new Random();
            private final Map<Colour,Map<Ticket, Integer>> initTickets = new HashMap<>();
            private Visualiser visualiser;
            private ResourceProvider provider;
            private GameTree root;
            private boolean restrictSpecial;
            private boolean rebuildTree = true;
            
            public void updateVisualiserAndProvider(Visualiser visualiser, ResourceProvider provider) {
                this.visualiser = visualiser;
                this.provider = provider;
            }
            
            @Override
            public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
                            Consumer<Move> callback) {
                // TODO do something interesting here; find the best move
                // picks a random move
                restrictSpecial = true;
                
                if(rebuildTree) {
                    initialMove(view, location);
                }
                else {
                    //Generate new states
                    int depth = SEARCH_DEPTH - root.getHeight();
                    for(GameTree b : root.getBottomNodes()) {
                        generateNextStates(b, b.getState(), depth);
                        b.toNodeTree();
                    }
                }

                ScoreVisitor.minimaxUpdate(root);
                
//                for(GameTree t : root.getBottomNodes()) {
//                    System.out.println(t.getValue());
//                }
                
                System.out.println(root.getHeight());

                //ScoreVisitor v = new ScoreVisitor(view.getGraph(), initTickets.get(Colour.BLACK));
                //root.accept(v);
                
                GameState state = new GameState(view, location); 
                ScotlandYardPlayer player = state.getCurrentPlayer(); 
                Set<Move> validmoves = ValidMoves.validMoves(player, state.getPlayerLocation(player.colour()), state.getCurrentRound(), state.getPlayers(), restrictSpecial);
                
                int ran = random.nextInt(validmoves.size());
                Move move = new ArrayList<>(validmoves).get(ran);

                changeRoot(move);

                displayMrXMove(move, location);

                callback.accept(move);

            }
            
            private void changeRoot(Move move) {
                GameTree newRoot = root;
                //System.out.println(move);
                //System.out.println("Old Root: " + root);

                for(NodeTree c : root.getChildren()) {
                    if(c.getMove().equals(move)) {
                        newRoot = GameTree.swapRoot(c, root.getState().nextState(move));
                        //System.out.println("New Root: " + newRoot);
                        System.out.println("Changed Root");
                        break;
                    }
                }
                root = newRoot;
            }
            
            private void initialize(ScotlandYardView view, GameState state) {
                state.getPlayers().forEach(p -> initTickets.put(p.colour(), state.getPlayerTickets(p.colour())));
                ScoreVisitor.initialize(view.getGraph(), initTickets.get(Colour.BLACK));
            }
            
            private void initialMove(ScotlandYardView view, int location) {
                GameState state = new GameState(view, location); 
                GameState.setRounds(view.getRounds());
                
                //state.getPlayers().forEach(p -> initTickets.put(p.colour(), state.getPlayerTickets(p.colour())));
                root = new GameTree(state, Double.NEGATIVE_INFINITY, state.getPlayers().size(), SEARCH_DEPTH, null);
                
                ValidMoves.initialize(view.getGraph(), view.getRounds());
                //ScoreVisitor.initialize(view.getGraph(), initTickets.get(Colour.BLACK));
                if(view.getCurrentRound() == 0)
                    initialize(view, state);
                
                //System.out.println(ScoreVisitor.scoreState(state));
                
                restrictSpecial = !(view.getRounds().get(state.getCurrentRound()) || ScoreVisitor.scoreState(state) <= DANGER_SCORE);
                generateNextStates(root, state, SEARCH_DEPTH);
                rebuildTree = false;
            }
            
            private void generateNextStates(NodeTree parent, GameState state, int depth) {
                if(depth <= 0)
                    return;
                
                ScotlandYardPlayer player = state.getCurrentPlayer(); 
                Set<Move> validmoves = ValidMoves.validMoves(player, state.getPlayerLocation(player.colour()), state.getCurrentRound(), state.getPlayers(), restrictSpecial);
                
                //System.out.println(validmoves.size());
                
                for(Move move : validmoves) {
                    GameState nextState = state.nextState(move);
                    
                    if(depth == 1) {
                        double value = ScoreVisitor.scoreState(nextState);
                        GameTree bottom = new GameTree(nextState, value, state.getPlayers().size(), SEARCH_DEPTH, move);
                        parent.add(bottom);
                    }
                    else {
                        NodeTree child = parent.add(move);
                        generateNextStates(child, nextState, depth - 1);
                    }
                }
                
            }
            
//            private void generateNextStates(GameTree parent, int depth) {
//                
//                if(depth > 0) {
//                    
//                    GameState state = parent.getState();
//                    ScotlandYardPlayer player = state.getCurrentPlayer();                   
//                    Set<Move> validmoves = ValidMoves.validMoves(player, state.getPlayerLocation(player.colour()), state.getCurrentRound(), state.getPlayers());
//                    
//                    for(Move move : validmoves) {
//                        GameState nextState = state.nextState(move);
//                        NodeTree child = parent.add(move);  
//                        //System.out.println("Depth: " + (SEARCH_DEPTH - depth + 1) + " " + player.colour());
//                        generateNextStates(child, depth - 1);
//                    }
//                }               
//            }
            
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
                    Circle s = new Circle(start.getX(), start.getY(), 30, Color.TRANSPARENT);
                    s.setStroke(Color.VIOLET);
                    s.setStrokeWidth(10);
                    pane.getChildren().add(s);
                    for(int i = 0; i < dest.size(); i++) {
                        Point2D d = dest.get(i);
                        Line l = new Line(start.getX(), start.getY(), d.getX(), d.getY());
                        l.setStroke(Color.VIOLET);
                        l.setStrokeWidth(10);
                        Circle c = new Circle(d.getX(), d.getY(), 30, Color.TRANSPARENT);
                        c.setStroke((i == 1 && dest.size() > 1) ? Color.CYAN : Color.VIOLET);
                        c.setStrokeWidth(10);
                        pane.getChildren().add(l);
                        pane.getChildren().add(c);
                        start = d;
                    }
                });
            }
            
            
    }
}
