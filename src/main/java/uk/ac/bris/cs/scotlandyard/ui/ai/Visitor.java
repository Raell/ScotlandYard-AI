/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bris.cs.scotlandyard.ui.ai;

/**
 *
 * @author admin
 */
public interface Visitor {
    void visit(GameTree tree);
    //void visit(NodeTree tree);
}
