/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.List;

/**
 *
 * @author Raell
 */
class GameTree<V, E> {

    V GameState;
    E value;
    int playerCount;
    int depth;
    List<GameTree<V, E>> children;


    GameTree(V state, E value, int playerCount, int depth) {
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
