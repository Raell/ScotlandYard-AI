package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.HashMap;
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
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;

// TODO name the AI
@ManagedAI(value = "Athena", visualiserType = ManagedAI.VisualiserType.MAP_OVERLAP)
public class Athena implements PlayerFactory {
    
    private final MyPlayer player = new MyPlayer();
    //The number of connections from the root to the bottom of tree (root = 0)
    private static final int SEARCH_DEPTH = 6;
    private static final double DANGER_SCORE = 25.0;
    
    @Override
    public Player createPlayer(Colour colour) {
        if(colour != Colour.BLACK)
            throw new IllegalArgumentException("Can only use Athena for Mr X.");
        return player;
    }

    @Override
    public void ready(Visualiser visualiser,
               ResourceProvider provider) {
        player.updateVisualiserAndProvider(visualiser, provider);
    }
    
    // AI player that selects the best move from a tree of game states 
    private static class MyPlayer implements Player {
        
        private final Map<Colour, Map<Ticket, Integer>> initTickets = new HashMap<>();
        private Visualiser visualiser;
        private ResourceProvider provider;
        private GameTree root;

        public void updateVisualiserAndProvider(Visualiser visualiser, ResourceProvider provider) {
            this.visualiser = visualiser;
            this.provider = provider;
        }

        @Override
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
                        Consumer<Move> callback) {
            
            initialize(view, location);
            Move move = ScoreGenerator.selectMove(root);
            //displayMrXMove(move, location);           
            callback.accept(move);

        }

        private void initializeGame(ScotlandYardView view, GameState state) {
            state.getPlayers().forEach(p -> initTickets.put(p.colour(), state.getPlayerTickets(p.colour())));
            ScoreGenerator.initialize(view.getGraph(), initTickets);
        }

        private void initialize(ScotlandYardView view, int location) {
            GameState state = new GameState(view, location); 
            GameState.setRounds(view.getRounds());
            root = new GameTree(state, Double.NEGATIVE_INFINITY, state.getPlayers().size(), SEARCH_DEPTH, null, Colour.BLACK);
            ValidMoves.initialize(view.getGraph(), view.getRounds());
            if(view.getCurrentRound() == 0)
                initializeGame(view, state);     
            generateNextStates(root, state, SEARCH_DEPTH);         
        }

        private void generateNextStates(NodeTree parent, GameState state, int depth) {

            if(depth <= 0 || parent.getAlpha() >= parent.getBeta())
                return;

            ScotlandYardPlayer player = state.getCurrentPlayer();             
            Set<Move> validmoves = ValidMoves.validMoves(
                    player, 
                    state.getPlayerLocation(player.colour()), 
                    state.getCurrentRound(), 
                    state.getDetectives(), 
                    useDouble(state), 
                    useSecret(state)
            );

            for(Move move : validmoves) { 
                GameState nextState = state.nextState(move);
                NodeTree child;
                
                if(depth == 1 || isMrXCaught(nextState.getPlayerLocation(Colour.BLACK), move)) {
                    double value = ScoreGenerator.scoreState(nextState);
                    child = new GameTree(nextState, value, state.getPlayers().size(), SEARCH_DEPTH, move, nextState.getCurrentPlayer().colour());
                    parent.add(child);                                                         
                }
                else {
                    child = parent.add(move, parent.getAlpha(), parent.getBeta(), nextState.getCurrentPlayer().colour());
                    generateNextStates(child, nextState, depth - 1);
                }
                
                updateAlphaBeta(child);
                if(parent != root)
                    parent.remove(child);

            }  
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
            if(state.getCurrentPlayer().isDetective())
                return false;
            int currentRound = state.getCurrentRound();
            boolean currRoundIsReveal = currentRound < GameState.getRounds().size() && GameState.getRounds().get(currentRound);
            boolean lastRoundIsReveal = currentRound > 0 && GameState.getRounds().get(currentRound - 1);
            double score = ScoreGenerator.scoreState(state);
            return ((currRoundIsReveal || lastRoundIsReveal) && inDanger(score, true)) || inDanger(score, false);
        }
        
        private boolean useSecret(GameState state) {
            if(state.getCurrentPlayer().isDetective())
                return false;
            int currentRound = state.getCurrentRound();
            List<Boolean> rounds = GameState.getRounds();
            double score = ScoreGenerator.scoreState(state);
            return ((currentRound == rounds.size() || rounds.get(currentRound)) && inDanger(score, true)) || inDanger(score, false);
        }

        private boolean inDanger(double score, boolean revealed) {
            double danger = revealed ? DANGER_SCORE : DANGER_SCORE * 0.5;
            return score <= danger;
        }

        private boolean isMrXCaught(int mrXLocation, Move move) {
            if(move.colour().isMrX() || move.getClass() == PassMove.class)
                return false;
            else {
                TicketMove tMove = (TicketMove) move;
                return tMove.destination() == mrXLocation;
            }
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
