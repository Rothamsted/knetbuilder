package net.sourceforge.ondex.algorithm.annotationquality;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.ondex.algorithm.dijkstra.DijkstraQueue;
import net.sourceforge.ondex.algorithm.dijkstra.PathNode;


/**
 * a minimalistic memory representation of a GO term.
 * @author Jochen Weile, B.Sc.
 *
 */
public class GoTerm {
	
	
	//####CONSTANTS####
	
	/**
	 * a set of constants defining the meaning of the namespace values.
	 */
	public static final int DOMAIN_BIOLOGICAL_PROCESS = 0, 
							DOMAIN_CELLULAR_COMPONENT = 1, 
							DOMAIN_MOLECULAR_FUNCTION = 2;
	
	//####FIELDS####
	
	/**
	 * the go term id.
	 */
	private int id = -1;
	
	/**
	 * the go term namespace.
	 */
	private int domain = -1;
	
	/**
	 * the shortest depth of the term. (computed on demand)
	 */
	private int shortestDepth = -1;
	
	/**
	 * the term's parents.
	 */
	private Vector<GoTerm> parents = new Vector<GoTerm>();
	
	//####CONSTRUCTORS####
	
	/**
	 * constructor with fix id.
	 * @param id
	 */
	public GoTerm(int id) {
		this.id = id;
	}
	
	/**
	 * overridden equals method. compares the ids.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof GoTerm) {
			GoTerm g2 = (GoTerm)o;
			if (g2.id == this.id)
				return true;
		}
		return false;
	}
	
	/**
	 * returns the shortest depth of the term. 
	 * if unknown it is computed and then returned.
	 * this method is protected and should only be executed by the GOTreeParser.
	 * (if you want the depth of a term, query it from the GOTreeParser).
	 * @param id2go index of all go terms
	 * @return
	 */
	protected int getShortestDepth(HashMap<Integer,GoTerm> id2go) {
		if (shortestDepth == -1)
			traceDepth(id2go);
		return shortestDepth;
	}
	
	/**
	 * runs the breadth first search algorithm to find the shortest depth.
	 * @param id2go
	 */
	private void traceDepth(HashMap<Integer,GoTerm> id2go) {
		PathNode node_curr, node_succ;
		GoTerm term_curr, term_succ;
		Iterator<GoTerm> parent_terms;
		
		PathNode node_root = new PathNode(id);
		DijkstraQueue queue = new DijkstraQueue(node_root);
		
		while (queue.moreOpenElements()) {
			node_curr = queue.dequeue();
			term_curr = id2go.get(node_curr.getCid());
			if (term_curr.parents.size() == 0) {
				shortestDepth = (int) node_curr.getG();
				break;
			}
			else {
				parent_terms = term_curr.parents.iterator();
				while (parent_terms.hasNext()) {
					term_succ = parent_terms.next();
					node_succ = new PathNode(term_succ.id);
					node_succ.setParent(node_curr, term_curr.id);
					node_succ.setG(node_curr.getG() + 1.0d);
					
					queue.enqueueIfBetterOrNew(node_succ);
				}
			}
			queue.considerClosed(node_curr);
		}
	}

	/**
	 * gets the id.
	 * @return the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * sets the id.
	 * @param id the id.
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * gets the namespace.
	 * @return the namespace.
	 */
	public int getDomain() {
		return domain;
	}

	/**
	 * sets the namespace.
	 * @param domain the namespace.
	 */
	protected void setDomain(int domain) {
		this.domain = domain;
	}

	/**
	 * gets the parents.
	 * @return the parents.
	 */
	public Vector<GoTerm> getParents() {
		return parents;
	}

	/**
	 * sets the parents.
	 * @param parents the parents.
	 */
	protected void setParents(Vector<GoTerm> parents) {
		this.parents = parents;
	}

}
