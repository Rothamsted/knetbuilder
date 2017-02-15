package net.sourceforge.ondex.algorithm.dijkstra;

/**
 * This class is a storage data type for DIJKSTRA's shortest path
 * algorithm. It wraps around a concept being represented by its ID.
 * It is possible that several of these path nodes exist for
 * the same concept, because it can be be rediscovered by the algorithm.
 * In that case the one being part of a better scoring graph (with a 
 * smaller distance) will replace the other one. The will be organised
 * in priority queues according to their distance value g.
 * 
 * @author Jochen Weile
 *
 */
public class PathNode {
	
	//####FIELDS####
	
	/**
	 * The ID of the enclosed concept.
	 */
	private int cid;
	
	/**
	 * The ID of the edge that connects the enclosed concept to its parent.
	 */
	private int rid;
	
	/**
	 * The parent node.
	 */
	private PathNode parent;
	
	/**
	 * The distance to the root.
	 */
	private double g;
	
	//####CONSTRUCTOR####
	
	/**
	 * The constructor creates a wrapping path node around 
	 * a concept, represented by its ID.
	 * @param cid The ID of the concept to be enclosed.
	 */
	public PathNode(int cid) {
		this.cid = cid;
		this.rid = -1;
		this.parent = null;
		this.g = 0.0;
	}
	
	//####METHODS####
	
	/**
	 * returns the enclosed concept ID.
	 * @return the enclosed concept ID.
	 */
	public int getCid() {
		return cid;
	}
	
	/**
	 * returns the parent of this node.
	 * @return the parent of this node.
	 */
	public PathNode getParent() {
		return parent;
	}
	
	/**
	 * returns the ID of the edge that connects the enclosed concept to it's parent.
	 * @return the ID of the edge that connects the enclosed concept to it's parent.
	 */
	public int getRid() {
		return rid;
	}
	
	/**
	 * sets the parent node of this concept in the current path as well as the edge
	 * connecting both.
	 * @param p the parent of this node.
	 * @param rid the edge connecting this node to it's parent.
	 */
	public void setParent(PathNode p, int rid) {
		this.parent = p;
		this.rid = rid;
	}
	
	/**
	 * returns the distance in the path to the root node.
	 * @return the distance to the root node.
	 */
	public double getG() {
		return g;
	}
	
	/**
	 * sets the distance in the path to the root node.
	 * @param g the distance to the root node.
	 */
	public void setG(double g) {
		this.g = g;
	}
}
