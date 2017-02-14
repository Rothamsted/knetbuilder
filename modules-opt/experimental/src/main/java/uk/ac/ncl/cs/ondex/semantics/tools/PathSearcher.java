/*
 * OndexView plug-in for Cytoscape
 * Copyright (C) 2010  University of Newcastle upon Tyne
 * 
 * This file is part of OndexView.
 * 
 * OndexView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OndexView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with OndexView.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ncl.cs.ondex.semantics.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

/**
 * this class searches instances of a metagraph path in the ondex graph
 * given a path description, specified as a concept class array and a 
 * relation type array.
 * 
 * CCs[A,B,C,...], RTs[1,2,...] = A 1 B 2 C ...
 * 
 * @author jweile
 *
 */
public class PathSearcher {

	/**
	 * ondex graph to search in.
	 */
	private ONDEXGraph og;
	
	/**
	 * path description arrays
	 */
	private ConceptClass[] ccs;
	private RelationType[] rts;
	
	private RelationType rt_eq;
	
	/**
	 * result vector each entry in the vector is a path represented
	 * by an integer array storing concept and relation ids in turn.
	 */
	private Vector<List<Integer>> paths = new Vector<List<Integer>>();
		
	/**
	 * iteration cursor for result querying.
	 */
	private int cursor = 0;
			
	/**
	 * constructor.
	 * @throws IllegalArgumentException if ccs.length != rts.length + 1
	 */
	public PathSearcher(PathTemplate template, ONDEXGraph graph) throws IllegalArgumentException {
            
		this.ccs = template.getCcs();
		this.rts = template.getRts();

		if (ccs.length != rts.length + 1)
			throw new IllegalArgumentException("Invalid path definition");
		
		og = graph;
		
		rt_eq = og.getMetaData().getRelationType("equ");
	}
	
	/**
	 * moves the cursor forward
	 * @return whether there is a next path to query
	 */
	public Path nextPath() {
		try {
			if (cursor < paths.size()-1) {
				return new Path(og, paths.get(++cursor));
			} else {
				return null;
			}
		} catch (MalformedPathException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * resets the cursor to the first path
	 */
	public void resetCursor() {
		cursor = 0;
	}
		
	
	private boolean instanceOf(ConceptClass cc, ONDEXConcept c) {
		ConceptClass cc_query = c.getOfType();
		while (!cc_query.equals(cc)) {
			if (cc_query.getSpecialisationOf() == null) {
				return false;
			}
			cc_query = cc_query.getSpecialisationOf();
		}
		return true;
	}
	
	private boolean instanceOf(RelationType rt, ONDEXRelation r) {
		RelationType rt_query = r.getOfType();
		while (!rt_query.equals(rt)) {
			if (rt_query.getSpecialisationOf() == null) {
				return false;
			}
			rt_query = rt_query.getSpecialisationOf();
		}
		return true;
	}
	
	/**
	 * searches for paths.
	 */
	public void search() {
//		og = OndexPlugin.getInstance().getOndexGraph();
		
		for (ONDEXConcept c : og.getConcepts()){
			if (!instanceOf(ccs[0], c)) {
				continue;
			}
			PathNode pn = new PathNode(c.getId());
			searchRelations(c, pn, 1, false);
		}
	}

	/**
	 * recursive helper method
	 */
	private void searchRelations(ONDEXConcept c, PathNode pn, int depth, boolean allowSkip) {
		//recursion base
		if (depth >= ccs.length) {
			List<Integer> a = new ArrayList<Integer>();
			traceback(pn, a);
			paths.add(a);
			return;
		}

		//get neighbours
		for (ONDEXRelation r : og.getRelationsOfConcept(c)){

                    if (instanceOf(rts[depth-1], r)) {
                        //get neighbour
                        ONDEXConcept cn = r.getFromConcept().equals(c) ? r.getToConcept() : r.getFromConcept();
                        if (instanceOf(ccs[depth], cn)) {
                            if (noDuplication(pn,cn)) {
                                PathNode pn_next = new PathNode(cn.getId(), r.getId(), pn);
                                searchRelations(cn, pn_next, depth+1, true);
                            }
                        }
                    } else if (allowSkip && r.getOfType().equals(rt_eq)) {
                        //get neighbour
                        ONDEXConcept cn = r.getFromConcept().equals(c) ? r.getToConcept() : r.getFromConcept();
                        if (instanceOf(ccs[depth-1], cn)) {
                            if (noDuplication(pn,cn)) {
                                PathNode pn_next = new PathNode(cn.getId(), r.getId(), pn);
                                searchRelations(cn, pn_next, depth, false);
                            }
                        }
                    }
		}
	}

	/**
	 * traces the results back. private helper method.
	 *
	 */
	private void traceback(PathNode pn, List<Integer> a) {
		a.add(pn.cid);
		if (pn.parent != null) {
			a.add(pn.rid);
			traceback(pn.parent, a);
		}
	}

        private boolean noDuplication(PathNode pn, ONDEXConcept cn) {
            if (pn == null) {
                return true;
            } else if (pn.cid == cn.getId()) {
                return false;
            } else {
                return noDuplication(pn.parent, cn);
            }
        }
	
	/**
	 * private class representing a node in a path, that is back-traceable.
	 */
	private class PathNode {
		public int cid, rid;
		public PathNode parent;
		public PathNode(int cid, int rid, PathNode parent) {
			this(cid);
			this.rid = rid;
			this.parent = parent;
		}
		public PathNode(int cid) {
			this.cid = cid;
		}
	}
}
