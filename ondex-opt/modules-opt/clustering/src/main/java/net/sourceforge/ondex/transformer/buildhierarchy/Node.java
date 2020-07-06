package net.sourceforge.ondex.transformer.buildhierarchy;

import java.util.HashSet;

/**
 * Node in a hierarchy tree.
 * @author Jochen Weile, B.Sc.
 */
public class Node {

	//####FIELDS####
	
	/**
	 * the node's parent.
	 */
	public Node parent;
	
	/**
	 * The probability that edges exist between the nodes, whose
	 * lowest common ancestor this node is.
	 */
	public double theta;
	
	/**
	 * The number of edges between the nodes, whose lowest common
	 * ancestor this node is.
	 */
	public int e;
	
	/**
	 * The log of the probability that the nodes, whose lowest common ancestor
	 * this node is, are connected.
	 */
	public double logF;
	
	/**
	 * an array of size 2 containing the children of this node.
	 */
	private Node[] children;
	
	/**
	 * a hashset containing the leaves
	 */
	private HashSet<Integer> leaves;
	
	
	
	/**
	 * the id. for internal use
	 */
	private int id;
	
	/**
	 * stores the next id to be given.
	 */
	private static int next_id = 0;
	
	
	/**
	 * Index for left child.
	 */
	public static final int LEFT = 0;
	
	/**
	 * index for right child.
	 */
	public static final int RIGHT = 1;

	//####CONSTRUCTOR####
	
	/**
	 * simple constructor.
	 */
	public Node() {
		id = next_id++;
		children = new Node[2];
	}
	
	/**
	 * constructor for internal node.
	 * @param c1
	 * @param c2
	 */
	public Node(Node c1, Node c2) {
		this();
		children[0] = c1;
		children[1] = c2;
		c1.parent = this;
		c2.parent = this;
		
		refreshLeaves();
	}
	
	/**
	 * constructor for leaf node.
	 * @param leaf
	 */
	public Node(int leaf) {
		this();
		leaves = new HashSet<Integer>();
		leaves.add(leaf);
	}
	
	/**
	 * constructs a dummy node with the given values.
	 * @param e the E value.
	 * @param theta the theta value.
	 * @param logF the logarithm of f_i.
	 */
	public Node(int e, double theta, double logF) {
		this.e = e;
		this.theta = theta;
		this.logF = logF;
	}

	//####METHODS####
	/**
	 * returns the child at the given position.
	 */
	public Node getChild(int index) {
		return children[index];
	}
	
	/**
	 * sets the child at the given position.
	 * @param index left or right.
	 * @param c the child.
	 */
	public void setChild(int index, Node c) {
		children[index] = c;
	}
	
	/**
	 * gets the leaves.
	 * @return the leaves.
	 */
	public HashSet<Integer> getLeaves() {
		return leaves;
	}
	
	/**
	 * recalculates the leaf set.
	 */
	public void refreshLeaves() {
		leaves = new HashSet<Integer>();
		leaves.addAll(children[0].leaves);
		leaves.addAll(children[1].leaves);
	}
	
	/**
	 * resets the id counter.
	 */
	public static void resetIdCounter() {
		next_id = 0;
	}
	
	/**
	 * if this node is a leaf.
	 * @return if this node is a leaf.
	 */
	public boolean isLeaf() {
		return leaves.size() == 1;
	}
	
	/**
	 * if this node is the root.
	 * @return if this node is the root.
	 */
	public boolean isRoot() {
		return parent == null;
	}
	
	/**
	 * if this node is the parent of the node that this query
	 * is done for, it returns it's sibling.
	 * @param myself the query node.
	 * @return the sibling.
	 */
	public Node getSiblingOf(Node myself) {
		if (children[0].equals(myself))
			return children[1];
		else if (children[1].equals(myself))
			return children[0];
		else
			return null;
	}
	
	/**
	 * replaces the old child with the new child.
	 * @param old the old child.
	 * @param nu the new child.
	 */
	public void replaceChild(Node old, Node nu) {
		if (children[0].equals(old)) {
			children[0] = nu;
			nu.parent = this;
		}
		else if (children[1].equals(old)) {
			children[1] = nu;
			nu.parent = this;
		}
	}
	
	/**
	 * gets an unconnected clone of this node.
	 * @return the clone.
	 */
	public Node getUnconnectedClone() {
		Node c = new Node(e, theta, logF);
		c.id = id;
		c.leaves = new HashSet<Integer>();
		c.leaves.addAll(leaves);
		return c;
	}
	
	/**
	 * sets the leaves.
	 * @param l the leaves.
	 */
	public void setLeaves(HashSet<Integer> l) {
		this.leaves = l;
	}
	
	/**
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Node) {
			Node no = (Node)o;
			if (no.id == this.id)
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		/*
		 * Before you get excited: 
		 * The class Integer does it the same way! 
		 */
		return id;
	}
	
	/**
	 * id
	 * @return id
	 */
	public int getId() {
		return id;
	}
	
}
