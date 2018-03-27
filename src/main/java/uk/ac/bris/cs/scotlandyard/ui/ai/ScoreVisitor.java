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
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Transport;

/**
 *
 * @author admin
 */
public class ScoreVisitor implements Visitor{
    private Move selectedMove;
    private final Graph<Integer, Transport> graph;
    private final Map<Ticket, Integer> initMrXTickets;
    
    public ScoreVisitor(Graph<Integer, Transport> graph, Map<Ticket, Integer> initMrXTickets) {
        this.graph = graph;
        this.initMrXTickets = initMrXTickets;
    }
    
    @Override
    public void visit(GameTree tree) {        
        //Visit bottomn nodes and use score function on the game state
        /*List<Ticket> t = new ArrayList<>(Arrays.asList(Ticket.TAXI, Ticket.BUS));
        Set<Integer> r = testContextualFactor(128, t);
        System.out.println(r);*/
        
        tree.getBottomNodes().forEach(t -> {
            t.setValue(scoreState(t.getState()));
        });
        minimaxUpdate(tree);
    }
    
    private double scoreState(GameState g) {
        //TODO Run Dijkstra algorithm on each detecetive to get distance
        //DijkstraCalculator d = new DijkstraCalculator(graph);
        List<Double> distanceScore = new ArrayList<>();
        
        for(ScotlandYardPlayer p : g.getPlayers()) {
            if(p.isMrX())
                continue;
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
            Collections.sort(distanceScore);
            double smallest = distanceScore.get(0);
            double median;
            
            if(distanceScore.size() % 2 == 1)
                median = distanceScore.get(distanceScore.size()/2);
            else {
                median = (distanceScore.get(distanceScore.size()/2) 
                        + distanceScore.get((distanceScore.size()/2) - 1)) / 2;
            }
            
            double score = (smallest + median) / 2;
            score *= availableMovesFactor(g);
            score *= contextualFactor(g);
            score *= specialTicketsFactor(g);
            return score;        
        }
    }
    
    private double availableMovesFactor(GameState g) {
        Set<Move> validmoves = ValidMoves.validMoves(g.getPlayer(Colour.BLACK), g.getPlayerLocation(Colour.BLACK), g.getCurrentRound());
        int moveCount = validmoves.size();       
        return (moveCount <= 10) ? (moveCount == 0) ? 0.0 : 0.8 : 1.0;
    }
    
    private double contextualFactor(GameState g) {
        int lastKnownPos = g.getLastKnownPos();
        if(lastKnownPos != 0) {
            Node<Integer> start = graph.getNode(lastKnownPos);
            List<Ticket> ticketsUsed = g.getTicketsSinceLastKnown();
            Set<Integer> possiblePos = new HashSet<>();
            updatePositions(possiblePos, ticketsUsed, start);

            return (possiblePos.size() <= 2) ? (possiblePos.size() == 1) ? 0.2 : 0.5 : 1;
        }
        else
            return 1;
    }
    
    private double specialTicketsFactor(GameState g) {
        double currentSpecialTickets = g.getPlayerTickets(Colour.BLACK, Ticket.DOUBLE) + g.getPlayerTickets(Colour.BLACK, Ticket.SECRET);
        double totalSpecialTickets = initMrXTickets.get(Ticket.DOUBLE) + initMrXTickets.get(Ticket.SECRET);       
        double totalRounds = GameState.getRounds().size();
        double roundsLeft = totalRounds - g.getCurrentRound();
        
        double factor = 1 - ((roundsLeft/totalRounds) * (currentSpecialTickets/totalSpecialTickets));
        return factor;
    }
    
    /*private Set<Integer> testContextualFactor(int lastKnownPos, List<Ticket> ticketsUsed) {
        Node<Integer> start = graph.getNode(lastKnownPos);
        Set<Integer> possiblePos = new HashSet<>();
        updatePositions(possiblePos, ticketsUsed, start);
        return possiblePos;
    }*/
    
    private void updatePositions(Set<Integer> possiblePos, List<Ticket> path, Node<Integer> nextNode) {
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
    
    private double getDistanceValue(DirectedGraph<Integer, Double> path) {
        double value = 0;
        for(Edge<Integer, Double> e : path.getEdges()) {
            value += e.data();
        }
        return value * Math.pow(path.getEdges().size(), 2);
    }
    
    private void minimaxUpdate(GameTree tree) {
        
    }
    
}
