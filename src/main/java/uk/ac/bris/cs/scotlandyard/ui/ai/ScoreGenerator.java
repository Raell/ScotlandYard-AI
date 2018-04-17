/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class ScoreGenerator {
    //private Move selectedMove;
    private static Graph<Integer, Transport> graph;
    private static Map<Ticket, Integer> initMrXTickets;
    
    public static void initialize(Graph<Integer, Transport> graph, Map<Ticket, Integer> initMrXTickets) {
        ScoreGenerator.graph = graph;
        ScoreGenerator.initMrXTickets = initMrXTickets;
    }
    
    public static Move selectMove(GameTree root) {
        return root.getChildren()
                .stream()
                .filter(c -> c.getValue() == root.getValue())
                .findFirst()
                .get()
                .getMove();
        
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
        Set<Move> validmoves = ValidMoves.validMoves(g.getPlayer(Colour.BLACK), g.getPlayerLocation(Colour.BLACK), g.getCurrentRound(), g.getPlayers(), false, true);
        int moveCount = validmoves.size();       
        return (moveCount <= 5) ? ((moveCount == 0) ? 0.0 : 0.8) : 1.0;
    }
    
    private static double contextualFactor(GameState g) {
        int lastKnownPos = g.getLastKnownPos();
        if(lastKnownPos != 0) {
            Node<Integer> start = graph.getNode(lastKnownPos);
            List<Ticket> ticketsUsed = g.getTicketsSinceLastKnown();
            Set<Integer> possiblePos = new HashSet<>();
            updatePositions(possiblePos, ticketsUsed, start);

            return (possiblePos.size() <= 2) ? ((possiblePos.size() == 1) ? 0.5 : 0.8) : 1;
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
        
        double factor = 1 - (0.25 * (roundsLeft/totalRounds) * (specialTicketsUsed/totalSpecialTickets));
        return factor;
    }
    
    private static void updatePositions(Set<Integer> possiblePos, List<Ticket> path, Node<Integer> nextNode) {
        if(path.isEmpty())
            possiblePos.add(nextNode.value());
        
        else {
            Ticket t = path.get(0);
            graph.getEdgesFrom(nextNode).stream().filter((e) -> (Ticket.fromTransport(e.data()) == t)).forEachOrdered((e) -> {
                List<Ticket> nextPath = new LinkedList<>(path.subList(1, path.size()));
                updatePositions(possiblePos, nextPath, e.destination());
            });
        }
    }
    
    private static double getDistanceValue(DirectedGraph<Integer, Double> path) {
        double value = 0;
        value = path.getEdges().stream().map((e) -> e.data()).reduce(value, (accumulator, _item) -> accumulator + _item);
        return value * Math.pow(path.getEdges().size(), 2);
    }
    
}
