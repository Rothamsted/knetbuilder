package net.sourceforge.ondex.transformer.buildhierarchy;

import java.util.HashMap;
import java.util.Vector;

/**
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class ConsensusSet {

	//####FIELDS####
	
	/**
	 * the central hashmap for occurrence counting.
	 */
	private HashMap<Cluster,Double> map;
	
	/**
	 * the number of nodes in the graph.
	 */
	private int max_nodes;
	
	/**
	 * accumulated weight.
	 */
	private double acc_weight;
	
	/**
	 * the root of the cluster tree.
	 */
	private Cluster root;
	
	/**
	 * number of feeds.
	 */
	private int feeds = 0;

	//####CONSTRUCTOR####
	
	/**
	 * constructor.
	 */
	public ConsensusSet(int nodes) {
		max_nodes = nodes;
		map = new HashMap<Cluster,Double>();
		
	}

	//####METHODS####
	
	/**
	 * registers a whole dendrogram with the set.
	 * O(n * E(l_n)) = O((((n+1)*n)/2)-1) = O(n^2)
	 */
	public void registerTree(Dendrogram d) {
		feeds++;
		double weight = 1.0;//d.getLogLikelihood();
		acc_weight += weight;
		for (Node n : d.getNodeTable()) {
			if (!n.isLeaf())
				registerNode(n,weight);
		}
	}
	
	/**
	 * registers one single node.
	 * O(l_n)
	 * @param n
	 * @param weight
	 */
	private void registerNode(Node n, double weight) {
		Cluster c = new Cluster(n);
		Double oldval = map.get(c);
		map.put(c, oldval == null ? weight : oldval + weight);
	}
	
	/**
	 * gets the number of significant consensus clusters.
	 * @return number of clusters in consensus.
	 */
	public int getNumberOfConsensusClusters() {
		//build up cluster table
		double threshold = acc_weight / 2.0;
		int n = 0;
		for (Cluster cluster : map.keySet()) {
			double w = map.get(cluster);
			if (w > threshold)
				n++;
		}
		return n;
	}
	
	/**
	 * gets the number of feeds.
	 * @return
	 */
	public int getFeeds() {
		return feeds;
	}
	
	/**
	 * builds the consensus cluster tree.
	 * @return the root node of the cluster tree.
	 */
	public Cluster buildConsensus() {
		
		//build up cluster table
		double threshold = acc_weight / 2.0;
		HashMap<Integer,Vector<Cluster>> table = new HashMap<Integer,Vector<Cluster>>();
//		Vector<Vector<Cluster>> table = new Vector<Vector<Cluster>>(max_nodes);
		for (Cluster cluster : map.keySet()) {
			double w = map.get(cluster);
			if (w > threshold) {
				int s = cluster.size();
				Vector<Cluster> group = table.get(s-1);
				if (group == null) {
					group = new Vector<Cluster>();
					table.put(s-1, group);
				}
				group.add(cluster);
			}
		}
		
		//make consistency check
		if (table.get(max_nodes - 1) == null || table.get(max_nodes - 1).size() > 1) {
			System.err.println("Currupt consensus set!");
			return null;
		}
		else
			root = table.get(max_nodes - 1).elementAt(0);
		
		//iterate through table and construct internal tree of clusters
		for (int l = max_nodes - 1; l >= 0; l--) {
			Vector<Cluster> group_l = table.get(l);
			if (group_l == null) 
				continue;
			for (Cluster c : group_l) {
				Cluster parent = null;
				int entry = c.getRandomEntry();
				for (int li = l+1; li < max_nodes; li++) {
					Vector<Cluster> group_li = table.get(li);
					if (group_li == null)
						continue;
					for (Cluster ci : group_li) {
						if (ci.contains(entry)) {
							parent = ci;
							break;
						}
					}
					if (parent != null)
						break;
				}
				if (parent != null) {
					c.setParent(parent);
				}
			}
		}
		
		return root;
		
	}
	
	
	
}
