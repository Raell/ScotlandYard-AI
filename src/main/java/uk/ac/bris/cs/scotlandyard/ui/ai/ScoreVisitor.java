/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Transport;

/**
 *
 * @author admin
 */
public class ScoreVisitor {
    private Move selectedMove;
    private static Graph<Integer, Transport> graph;
    private static Map<Ticket, Integer> initMrXTickets;
    
    public static void initialize(Graph<Integer, Transport> graph, Map<Ticket, Integer> initMrXTickets) {
        ScoreVisitor.graph = graph;
        ScoreVisitor.initMrXTickets = initMrXTickets;
    }
    
    /*@Override
    public void visit(GameTree tree) {        
        //Visit bottomn nodes and use score function on the game state
        //List<Ticket> t = new ArrayList<>(Arrays.asList(Ticket.TAXI, Ticket.BUS));
        //Set<Integer> r = testContextualFactor(128, t);
        //System.out.println(r);
        
        tree.getBottomNodes().forEach(t -> {
            t.setValue(scoreState(t.getState()));
        });
        minimaxUpdate(tree);
    }*/
    
    public static Move selectMove(GameTree root) {
        Double value = Double.NEGATIVE_INFINITY;
        Move selected = root.getMove();
        for(NodeTree child : root.getChildren()){
            if(child.getValue() > value) {
                selected = child.getMove();
                value = child.getValue();
            }
                
            System.out.println("Possible: " + child.getMove().toString() + " : " + child.getValue());
        }
        System.out.println("Chosen: " + selected.toString() + "\n");
        return selected;
    }
    
    public static double scoreState(GameState g) {
        //TODO Run Dijkstra algorithm on each detecetive to get distance
        //DijkstraCalculator d = new DijkstraCalculator(graph);
        List<Double> distanceScore = new ArrayList<>();
        
        for(ScotlandYardPlayer p : g.getPlayers()) {         
            if(p.isMrX())
                continue;           
            if(g.getPlayerLocation(Colour.BLACK) == g.getPlayerLocation(p.colour()))
                return 0;
            
            Map<Ticket, Integer> tickets = g.getPlayerTickets(p.colour());
            DijkstraCalculator d = new DijkstraCalculator(graph, tickets);         
            DirectedGraph<Integer, Double> path = d.getResult(g.getPlayerLocation(Colour.BLACK), g.getPlayerLocation(p.colour()));
            double distance = getDistanceValue(path);
            if(distance > 0)
                distanceScore.add(distance);
        }
        
        if(distanceScore.isEmpty())
            return Double.POSITIVE_INFINITY;
        else {
            double score = calculateDistanceScore(distanceScore, g.getPlayers().size() - 1);
            score *= availableMovesFactor(g);
            score *= contextualFactor(g);
            score *= specialTicketsFactor(g);
            return score;        
        }
    }
    
    private static double calculateDistanceScore(List<Double> distances, int detectiveCount) {
        Collections.sort(distances);
        double score = 0;
        
        int size = distances.size();
        for(int i = 0; i < size; i++) {
            score += distances.get(i) * ((detectiveCount - i) / size);
        }
        
        return score;
    }
    
    private static double availableMovesFactor(GameState g) {
        Set<Move> validmoves = ValidMoves.validMoves(g.getPlayer(Colour.BLACK), g.getPlayerLocation(Colour.BLACK), g.getCurrentRound(), g.getPlayers(), false);
        int moveCount = validmoves.size();       
        return (moveCount <= 10) ? ((moveCount == 0) ? 0.0 : 0.8) : 1.0;
    }
    
    private static double contextualFactor(GameState g) {
        int lastKnownPos = g.getLastKnownPos();
        if(lastKnownPos != 0) {
            Node<Integer> start = graph.getNode(lastKnownPos);
            List<Ticket> ticketsUsed = g.getTicketsSinceLastKnown();
            Set<Integer> possiblePos = new HashSet<>();
            updatePositions(possiblePos, ticketsUsed, start);

            return (possiblePos.size() <= 2) ? ((possiblePos.size() == 1) ? 0.2 : 0.5) : 1;
        }
        else
            return 1;
    }
    
    private static double specialTicketsFactor(GameState g) {             
        double totalSpecialTickets = initMrXTickets.get(Ticket.DOUBLE) + initMrXTickets.get(Ticket.SECRET);      
        if(totalSpecialTickets == 0)
            return 1;
        
        double currentSpecialTickets = g.getPlayerTickets(Colour.BLACK, Ticket.DOUBLE) + g.getPlayerTickets(Colour.BLACK, Ticket.SECRET);
        double specialTicketsUsed = totalSpecialTickets - currentSpecialTickets;
        double totalRounds = GameState.getRounds().size();
        double roundsLeft = totalRounds - g.getCurrentRound();
        
        double factor = 1 - ((roundsLeft/totalRounds) * (specialTicketsUsed/totalSpecialTickets));
        return factor;
    }
    
    /*private Set<Integer> testContextualFactor(int lastKnownPos, List<Ticket> ticketsUsed) {
        Node<Integer> start = graph.getNode(lastKnownPos);
        Set<Integer> possiblePos = new HashSet<>();
        updatePositions(possiblePos, ticketsUsed, start);
        return possiblePos;
    }*/
    
    private static void updatePositions(Set<Integer> possiblePos, List<Ticket> path, Node<Integer> nextNode) {
        if(path.isEmpty())
            possiblePos.add(nextNode.value());
        
        else {
            Ticket t = path.get(0);
            for(Edge<Integer, Transport> e : graph.getEdgesFrom(nextNode)) {              
                if(Ticket.fromTransport(e.data()) == t) {
                    List<Ticket> nextPath = new LinkedList<>(path.subList(1, path.size()));
                    updatePositions(possiblePos, nextPath, e.destination());
                }
            }
        }
    }
    
    private static double getDistanceValue(DirectedGraph<Integer, Double> path) {
        double value = 0;
        for(Edge<Integer, Double> e : path.getEdges()) {
            value += e.data();
        }
        return value * Math.pow(path.getEdges().size(), 2);
    }
    
}
