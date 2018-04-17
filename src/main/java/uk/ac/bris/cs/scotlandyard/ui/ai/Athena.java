package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.ai.ResourceProvider;
import uk.ac.bris.cs.scotlandyard.ai.Visualiser;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;

// TODO name the AI
@ManagedAI(value = "Athena", visualiserType = ManagedAI.VisualiserType.MAP_OVERLAP)
public class Athena implements PlayerFactory {
    
    private final MyPlayer player = new MyPlayer();
    //The number of connections from the root to the bottom of tree (root = 0)
    private static final int SEARCH_DEPTH = 6;
    private static final double DANGER_SCORE = 20.0;
    
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
    
    /*public GameTree getRoot() {
        // Not immutable but to make it would cause a lot of memory to be used as the entire tree needs to be copied
        return player.root.clone();
    }*/
    
    public void setRoot(Move move) {
        for(NodeTree child : player.root.getChildren()) {
            if(child.getMove().equals(move)) {
                player.root = GameTree.swapRoot(child, player.root.getState().nextState(move));
                return;
            }
        }
        toRebuildTree();
    }
    
    public void toRebuildTree() {
        //System.out.println("Rebuild");
        player.rebuildTree = true;
    }

    // TODO A sample player that selects a random move
    private static class MyPlayer implements Player {

            //private final Random random = new Random();
            private final Map<Colour,Map<Ticket, Integer>> initTickets = new HashMap<>();
            private Visualiser visualiser;
            private ResourceProvider provider;
            private GameTree root;
            //private boolean restrictSpecial;
            private boolean rebuildTree = true;
            
            public void updateVisualiserAndProvider(Visualiser visualiser, ResourceProvider provider) {
                this.visualiser = visualiser;
                this.provider = provider;
            }
            
            @Override
            public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
                            Consumer<Move> callback) {
                
                //final long startTime = System.currentTimeMillis();
                
                if(rebuildTree) {
                    initialMove(view, location);
                }
                else {
                    //Generate new states
                    int depth = SEARCH_DEPTH - root.getHeight();
                    root.getBottomNodes().forEach((b) -> {                    
                        generateNextStates(b, b.getState(), depth, 0);
                        b.toNodeTree();
                    });
                    recalculateValues();
                }
                
                //final long endTime = System.currentTimeMillis();
                //System.out.println("Execution Time: " + (double) (endTime - startTime) / 1000 + " secs");
                
                Move move = ScoreGenerator.selectMove(root);
                changeRoot(move);
                //displayMrXMove(move, location);              
                callback.accept(move);

            }
            
            private void changeRoot(Move move) {
                GameTree newRoot = root;

                for(NodeTree c : root.getChildren()) {
                    if(c.getMove().equals(move)) {
                        newRoot = GameTree.swapRoot(c, root.getState().nextState(move));
                        break;
                    }
                }
                root = newRoot;
            }
            
            private void initialize(ScotlandYardView view, GameState state) {
                state.getPlayers().forEach(p -> initTickets.put(p.colour(), state.getPlayerTickets(p.colour())));
                ScoreGenerator.initialize(view.getGraph(), initTickets.get(Colour.BLACK));
            }
            
            private void initialMove(ScotlandYardView view, int location) {
                GameState state = new GameState(view, location); 
                GameState.setRounds(view.getRounds());
                
                //state.getPlayers().forEach(p -> initTickets.put(p.colour(), state.getPlayerTickets(p.colour())));
                root = new GameTree(state, Double.NEGATIVE_INFINITY, state.getPlayers().size(), SEARCH_DEPTH, null, Colour.BLACK);
                
                ValidMoves.initialise(view.getGraph(), view.getRounds());
                //ScoreVisitor.initialize(view.getGraph(), initTickets.get(Colour.BLACK));
                if(view.getCurrentRound() == 0)
                    initialize(view, state);
                
                //System.out.println(ScoreVisitor.scoreState(state));
                
                //restrictSpecial = !(view.getRounds().get(state.getCurrentRound()) || ScoreVisitor.scoreState(state) <= DANGER_SCORE);
                generateNextStates(root, state, SEARCH_DEPTH, 0);
                rebuildTree = false;
            }
            
            private void generateNextStates(NodeTree parent, GameState state, int depth, int indent) {
                //final long startTime = System.currentTimeMillis();
                if(depth <= 0)
                    return;
                
                ScotlandYardPlayer player = state.getCurrentPlayer();             
                Set<Move> validmoves = ValidMoves.validMoves(player, state.getPlayerLocation(player.colour()), state.getCurrentRound(), state.getDetectives(), useDouble(state), false);
                
                //final long check1 = System.currentTimeMillis();
                //System.out.println("State/Valid: " + (double) (check1 - startTime) / 1000 + " secs");
                
                for(Move move : validmoves) {
                    if(parent.getAlpha() >= parent.getBeta())
                        break;
                    
                    GameState nextState = state.nextState(move);                   
                    if(depth == 1 || isMrXCaught(nextState)) {
                        double value = ScoreGenerator.scoreState(nextState);
                        GameTree bottom = new GameTree(nextState, value, state.getPlayers().size(), SEARCH_DEPTH, move, nextState.getCurrentPlayer().colour());
                        parent.add(bottom);                    
                        updateAlphaBeta(bottom);
                    }
                    else {
                        NodeTree child = parent.add(move, parent.getAlpha(), parent.getBeta(), nextState.getCurrentPlayer().colour());
                        generateNextStates(child, nextState, depth - 1, indent + 1);
                        updateAlphaBeta(child);
                    }

                }
                                                              
            }
            
            private void recalculateValues() {
                root.resetTree();
                reverseIterateTree(new HashSet<>(root.getBottomNodes()));
            }
            
            private void reverseIterateTree(Set<NodeTree> treeRow) {
                
                Set<NodeTree> parents = new HashSet<>();
                treeRow.stream().filter((b) -> (b.getParent() != null)).map((b) -> {
                    updateAlphaBeta(b);
                    return b;
                }).forEachOrdered((b) -> {
                    parents.add(b.getParent());
                });
                
                if(!parents.isEmpty())
                    reverseIterateTree(parents);
            }
            
            private void updateAlphaBeta(NodeTree t) {
                NodeTree parent = t.getParent();
                if(parent == null)
                    return;
                if(parent.isMaximiser()) {
                    if(parent.getAlpha() < t.getValue()) {
                        parent.setAlpha(t.getValue());
                        parent.setValue(t.getValue());
                    }
                }
                else {
                    if(parent.getBeta() > t.getValue()) {
                        parent.setBeta(t.getValue());
                        parent.setValue(t.getValue());
                    }
                }
            }

            private boolean useDouble(GameState state) {
                int currentRound = state.getCurrentRound();
                boolean currRoundIsReveal = currentRound < GameState.getRounds().size() && GameState.getRounds().get(currentRound);
                boolean lastRoundIsReveal = currentRound > 0 && GameState.getRounds().get(currentRound - 1);
                double score = ScoreGenerator.scoreState(state);
                return ((currRoundIsReveal || lastRoundIsReveal) && inDanger(score, true)) || inDanger(score, false);
            }
            
            private boolean inDanger(double score, boolean revealed) {
                double danger = revealed ? DANGER_SCORE : DANGER_SCORE * 0.5;
                return score <= danger;
            }
            
            private boolean isMrXCaught(GameState g) {
                int mrXPos = g.getPlayerLocation(Colour.BLACK);
                return g.getDetectives()
                        .stream()
                        .anyMatch(d -> d.location() == mrXPos);
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
                        c.setStroke((i == 1 && dest.size() > 1) || dest.size() == 1? Color.CYAN : Color.VIOLET);
                        c.setStrokeWidth(10);
                        pane.getChildren().add(l);
                        pane.getChildren().add(c);
                        start = d;
                    }
                });
            }
            
            
    }
}
