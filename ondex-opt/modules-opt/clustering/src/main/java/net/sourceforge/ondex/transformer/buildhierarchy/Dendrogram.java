package net.sourceforge.ondex.transformer.buildhierarchy;

public class Dendrogram {
	
	/**
	 * stores all nodes under their ids.
	 */
	private Node[] id2node;
	
	/**
	 * the root node.
	 */
	private Node root;
	
	/**
	 * the log likelihood of the model 
	 */
	private double logLikelihood = Double.NEGATIVE_INFINITY;
	
	/**
	 * constructor.
	 * @param r root.
	 * @param t nodetable.
	 */
	public Dendrogram(Node r, Node[] t) {
		root = r;
		id2node = t;
		calcLogLikelihood();
	}
	
	/**
	 * gets the root.
	 */
	public Node getRoot() {
		return root;
	}
	
	/**
	 * gets the node table.
	 */
	public Node[] getNodeTable() {
		return id2node;
	}
	
	/**
	 * gets the log likelihood.
	 */
	public double getLogLikelihood() {
		return logLikelihood;
	}
		
	/**
	 * calculates the log likelihood of the 
	 * current tree
	 * @return its subtree's log likelihood.
	 */
	public void calcLogLikelihood() {
		logLikelihood = 0.0;
		for (Node n : id2node) {
			if (!n.isLeaf()) {
				logLikelihood += n.logF;
			}
		}
	}
	
	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Dendrogram clone() {
		Node[] nid2node_tmp = new Node[id2node.length];
		Node newroot = cloneSubtree(root, null, nid2node_tmp);
		Dendrogram d_clone = new Dendrogram(newroot, nid2node_tmp);
		return d_clone;
	}
	
	/**
	 * recursively clones the whole tree.
	 * @param n the current node to be cloned.
	 * @param parent the clone of the parent.
	 * @return a clone of the query node.
	 */
	private Node cloneSubtree(Node n, Node parent, Node[] nid2node_tmp) {
		Node c = n.getUnconnectedClone();
		c.parent = parent;
		
		nid2node_tmp[c.getId()] = c;
		
		if (!n.isLeaf()) {
			Node n_ch_l = n.getChild(Node.LEFT);
			Node n_ch_r = n.getChild(Node.RIGHT);
			
			Node c_ch_l = cloneSubtree(n_ch_l, c, nid2node_tmp);
			Node c_ch_r = cloneSubtree(n_ch_r, c, nid2node_tmp);
			
			c.setChild(Node.LEFT, c_ch_l);
			c.setChild(Node.RIGHT, c_ch_r);
		}
		
		return c;
	}

}
