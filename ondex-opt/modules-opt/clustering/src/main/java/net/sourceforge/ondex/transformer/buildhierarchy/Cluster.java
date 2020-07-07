package net.sourceforge.ondex.transformer.buildhierarchy;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Vector;

/**
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class Cluster {
	/**
	 * the hashkey
	 */
	private BitSet key;
	
	/**
	 * size of the cluster
	 */
	private int size;
	
	/**
	 * the graph node elements
	 */
	private HashSet<Integer> nodes;
	
	/**
	 * the parent cluster.
	 */
	private Cluster parent;
	
	/**
	 * the children.
	 */
	private Vector<Cluster> children;
	
	/**
	 * constructor.
	 * @param n template node.
	 */
	public Cluster(Node n) {
		makeKey(n);
		size = n.getLeaves().size();
		nodes = n.getLeaves();
	}
	
	/**
	 * sets the parent.
	 * @param p
	 */
	public void setParent(Cluster p) {
		if (p.children == null)
			p.children = new Vector<Cluster>();
		p.children.add(this);
		this.parent = p;
	}
	
	/**
	 * gets the parent.
	 * @return
	 */
	public Cluster getParent(){
		return parent;
	}
	
	/**
	 * gets the size.
	 * @return
	 */
	public int size() {
		return size;
	}
	
	/**
	 * gets the children.
	 * @return
	 */
	public Vector<Cluster> getChildren() {
		return children;
	}
	
//	/**
//	 * O(l_n)
//	 * @param n
//	 * @return
//	 */
//	private void makeKey(Node n) {
//		char zero = '\u0000';
//		int charsize = 0xffff + 1;
//		int strlen = (max_nodes / charsize);
//		char[] str = new char[strlen];
//		for (int i = 0; i < strlen; i++)
//			str[i] = zero;
//
//		lowestEntry = Integer.MAX_VALUE;
//		for (int l : n.getLeaves()) {
//			if (l < lowestEntry) lowestEntry = l;
//			int arrayindex = l / charsize;
//			int bitindex = l % charsize;
//			int bits = (int)str[arrayindex];
//			int inc = 1 << bitindex;
//			str[arrayindex] = (char) (bits | inc);
//		}
//		
//		key = new String(str);
//	}
	
	/**
	 * constructs the hashkey.
	 */
	private void makeKey(Node n) {
		key = new BitSet();
		for (int l : n.getLeaves()) {
			key.set(l);
		}
	}
	
	/**
	 * gets the node elements.
	 * @return
	 */
	public HashSet<Integer> getNodes() {
		return nodes;
	}
	
//	/**
//	 * O(n/16)
//	 * @param nodeID
//	 * @return
//	 */
//	public boolean contains(int nodeID) {
//		int charsize = 0xffff + 1;
//		char[] str = key.toCharArray();//O(n/16)
//		int arrayindex = nodeID / charsize;
//		int bitindex = nodeID % charsize;
//		int bits = (int)str[arrayindex];
//		int probe = 1 << bitindex;
//		int indicator = bits & probe;
//		
//		return indicator > 0;
//	}
	
	/**
	 * contains this node?
	 */
	public boolean contains(int nodeID) {
		return key.get(nodeID);
	}
	
	/**
	 * yields random node entry.
	 * @return
	 */
	public int getRandomEntry() {
		return nodes.iterator().next();
	}
	
	/**
	 * O(n/64)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return key.hashCode();
	}
	
	/**
	 * 
	 * O(n/64) ; Omega(1)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof Cluster) {
			Cluster c = (Cluster) o;
			if (c.size == size) {
				if (c.key.equals(key))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * gets the key.
	 * @return the key.
	 */
	public BitSet getKey() {
		return key;
	}
	
	/**
	 * output representative string.
	 */
	public String toString() {
		return key.toString();
	}
}