package net.sourceforge.ondex.mapping.tmbased;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;

/**
 * @author keywan
 * 
 * This class does a Breadth First Search for all reachable Concepts on the
 * ONDEXGraph starting at the root Concept. It returns the shotest paths to the
 * reachable Concepts assuming a constant edge weight of 1 and a given ONDEXGraph.
 * 
 * Note: this version considers only relations of type: is_a OR is_p
 *
 */
public class BreadthFirstSearch {
	
	private ONDEXGraph graph;
	private RelationType rtISA;
	private RelationType rtISP;


	public BreadthFirstSearch(ONDEXGraph graph){
		this.graph = graph;
		
		rtISA = graph.getMetaData().getRelationType(MetaData.is_a);
		if (rtISA == null) {
			RelationType rt = graph.getMetaData().getRelationType( MetaData.is_a);
			if (rt == null) {
				ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent("Missing: "+ MetaData.is_a, Mapping.getCurrentMethodName()));
			}
			rtISA = graph.getMetaData().getFactory().createRelationType(MetaData.is_a, rt);
		}
		
		rtISP = graph.getMetaData().getRelationType(MetaData.is_p);
		if (rtISP == null) {
			RelationType rt = graph.getMetaData().getRelationType(MetaData.is_p);
			if (rt == null) {
				ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent("Missing: "+ MetaData.is_p, Mapping.getCurrentMethodName()));
			}
			rtISP = graph.getMetaData().getFactory().createRelationType(MetaData.is_p, rt);
		}
		
	}
	
	/**
	 * performs a breadth first search
	 * 
	 * @param id of the root concept
	 * @param maxDepth considers only concepts to this depth
	 * @param undirected false when consider only outgoing edges
	 * @return Map<Integer, Integer> with distance of roout to connected concepts
	 */
	public Map<Integer, Integer> breadthFirstSearch(int id, int maxDepth, boolean undirected) {	
		
		// init data structures
		Map<Integer, Integer> distance = new HashMap<Integer, Integer>();
		ConcurrentLinkedQueue<ONDEXConcept> queue = new ConcurrentLinkedQueue<ONDEXConcept>();
		HashSet<ONDEXConcept> visited = new HashSet<ONDEXConcept>();
		
		// get root concept add it to queue and set distance to zero
		ONDEXConcept root = graph.getConcept(id);
		queue.add(root);
		distance.put(id,0);
		
		int depth = 0;
		
		while (!queue.isEmpty() && depth <= maxDepth) 
		{	
			// get one concept out (FIFO)
			ONDEXConcept u = queue.poll();
			
			// set u as visited, no self loop here
			visited.add(u);
			
			// get the actual depth
			depth = distance.get(u.getId()) + 1;
			
			// iterate on all outgoing realtions
			for (ONDEXRelation rel : graph.getRelationsOfConcept(u)) {
				// check if it's an outgoing relation
				if (rel.getFromConcept().equals(u)) {
					
					// only consider is_a or is_p relations
					if(rel.getOfType().equals(rtISA) || rel.getOfType().equals(rtISP)){
					
						// get to concept from relation
						ONDEXConcept toConcept = rel.getToConcept();
						
						// prevent loops
						if (!visited.contains(toConcept)) {
							
							// enqueue v for further visiting
							queue.add(toConcept);
							distance.put(toConcept.getId(),depth);
						}
					}		
				}
			}

			// if undirected, also iterate over all incoming relations
			if (undirected) {
				
				// iterate on all incoming realtions
				for (ONDEXRelation rel : graph.getRelationsOfConcept(u)) {
					 // check if it's  an incoming relation 
					if (rel.getToConcept().equals(u)) {
						
						// only consider is_a or is_p relations
						if(rel.getOfType().equals(rtISA) || rel.getOfType().equals(rtISP)){
						
							// get from concept from relation
							ONDEXConcept fromConcept = rel.getFromConcept();
							
							// prevent loops
							if (!visited.contains(fromConcept)) {
								
								// enqueue v for further visiting
								queue.add(fromConcept);
								distance.put(fromConcept.getId(),depth);
							}
						}	
					}
				}
			}
		}
	
		distance.remove(id);
		
		return distance;
	}

}
