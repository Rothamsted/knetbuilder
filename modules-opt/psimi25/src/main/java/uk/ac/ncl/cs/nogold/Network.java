/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nogold;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class Network {

    /**
     * network id.
     */
    private int id;

    /**
     * set of nodes.
     */
    private Set<Integer> nodes = new HashSet<Integer>();

    /**
     * set of edges.
     */
    private Set<SetOfTwo<Integer>> edges = new HashSet<SetOfTwo<Integer>>();

    /**
     * creates a new network with the given id.
     * @param id
     */
    public Network(int id) {
        this.id = id;
    }

    /**
     * adds the given pair as a new edge.
     * @param e
     */
    public void add(SetOfTwo<Integer> e) {
        nodes.add(e.getA());
        nodes.add(e.getB());
        edges.add(e);
    }

    /**
     * Adds only the nodes of the given pair, but not an edge for it.
     * @param e
     */
    public void addNegative(SetOfTwo<Integer> e) {
        nodes.add(e.getA());
        nodes.add(e.getB());
    }

    /**
     * adds all nodes and edges of the given network.
     * @param ex
     */
    public void addAll(Network ex) {
        nodes.addAll(ex.nodes);
        edges.addAll(ex.edges);
    }

    /**
     * Checks whether the network contains the given node.
     * @param id
     * @return
     */
    public boolean contains(int id) {
        return nodes.contains(id);
    }

    /**
     * Checks whether the network contains the given edge.
     * @param edge
     * @return
     */
    public boolean contains(SetOfTwo<Integer> edge) {
        return edges.contains(edge);
    }

    /**
     * gets all edges of the network
     * @return
     */
    public Set<SetOfTwo<Integer>> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    /**
     * gets all nodes of the network.
     * @return
     */
    public Set<Integer> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

}
