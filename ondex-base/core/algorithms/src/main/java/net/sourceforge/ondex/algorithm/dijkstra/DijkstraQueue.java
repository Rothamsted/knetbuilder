package net.sourceforge.ondex.algorithm.dijkstra;

import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.PriorityQueue;

/**
 * This class is the core datatype for a shortest path search
 * like Dijkstra or BFS. It combines a priority ordered according
 * to the elements' distances to the root node and two hash tables
 * allowing quick access on any open or closed element.
 * It provides all methods necessary for the algorithm:
 * <code>moreOpenElements()</code> tells the algorithm if there are
 * more elements to be examined. <code>enqueueIfBetterOrNew()</code>
 * performs the basic operation of the shortest path algorithm:
 * It checks whether an element is better than its equivalent
 * already considered as examined. If so it enqueues them again.
 * <code>considerClosed()</code> marks the given element as already
 * examined. <code>dequeue()</code> retrieves and removes the head
 * element of the priority queue.
 * 
 * @author Jochen Weile
 *
 */
public class DijkstraQueue {
	
	//####FIELDS####
	
	/**
	 * A priority queue containing the elements yet to be examined.
	 */
	private PriorityQueue<PathNode> queueOpen;
	
	/**
	 * A hashtable mapping the concept IDs of the elements yet to be examined
	 * to their path node representations as they exist in the priority queue
	 * 'queueOpen'.
	 */
	private Hashtable<Integer,PathNode> registerOpen;
	
	/**
	 * A hashtable mapping the concept IDs of the elements already examined
	 * to their path node representations forming the result set.
	 */
	private Hashtable<Integer,PathNode> registerClosed;
	
	//####CONSTRUCTOR####
	
	/**
	 * The standard constructor setting up and configuring the fields.
	 */
	public DijkstraQueue(PathNode root) {
		
		queueOpen = new PriorityQueue<PathNode>(100, new Comparator<PathNode>() {
			
			public int compare(PathNode n1, PathNode n2) {
				double g1 = n1.getG();
				double g2 = n2.getG();
				return (g1 == g2) ? 0 : ((g1 < g2) ? -1 : 1);
			}
		});
		
		registerOpen = new Hashtable<Integer,PathNode>();
		
		registerClosed = new Hashtable<Integer,PathNode>();
		
		enqueueIfBetterOrNew(root); //root should be new ;)
		
	}
	
	//####METHODS####
	
	/**
	 * returns the number of elements yet to be examined.
	 */
	public boolean moreOpenElements() {
		return queueOpen.size() > 0;
	}
	
//	/**
//	 * returns the number of elements already examined.
//	 * @return the number of elements already examined.
//	 */
//	public int closed() {
//		return registerClosed.size();
//	}
	
	
	/**
	 * If there is no equivalent of node with a smaller distance to 
	 * the root already in either
	 * the the open or closed sets, then the given node is enqueued,
	 * otherwise it will be discarded.
	 * @param node
	 */
	public void enqueueIfBetterOrNew(PathNode node) {
		
		int cid = node.getCid();
		
		boolean abort = false;
		
		PathNode equivalentOpen = null;
		if (registerOpen.containsKey(cid)) {
			equivalentOpen = registerOpen.get(cid);
			if (node.getG() >= equivalentOpen.getG())
				//then it's worse
				abort = true;
		}
		
		PathNode equivalentClosed = null;
		if (registerClosed.containsKey(cid)) {
			equivalentClosed = registerClosed.get(cid);
			if (node.getG() >= equivalentClosed.getG())
				//then it's worse
				abort = true;
		}
		
		if (!abort) {
			if (equivalentOpen != null) {
				registerOpen.remove(cid);
				registerOpen.values().remove(equivalentOpen);
				queueOpen.remove(equivalentOpen);
			}
			if (equivalentClosed != null) {
				registerClosed.remove(cid);
				registerClosed.values().remove(equivalentClosed);
			}
			registerOpen.put(cid, node);
			queueOpen.offer(node);
		}
		
	}
	
	/**
	 * memorises that the given node shall henceforth be considered as
	 * already examined and hence be part of the result set.
	 * @param node
	 */
	public void considerClosed(PathNode node) {
		
		registerClosed.put(node.getCid(), node);
		
	}
	
	/**
	 * retrieves and removes the head of the queue.
	 * @return the first element of the queue.
	 */
	public PathNode dequeue() {
		
		PathNode node = queueOpen.poll();
		PathNode equivalent = registerOpen.get(node.getCid());
		registerOpen.remove(node.getCid());
		registerOpen.values().remove(equivalent);
		return node;
		
	}
	
	/**
	 * yields the result set.
	 * @return the result set as a collection of path nodes.
	 */
	public Collection<PathNode> getResultSet() {
		return registerClosed.values();
	}

}
