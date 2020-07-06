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

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;

/**
 * index for known metadata
 * @author jweile
 *
 */
public class MDIndex {
	
	/**
	 * map for indexing concept classes by id and name.
	 */
	private Map<String,ConceptClass> ccmap;
	
	/**
	 * map for indexing relation types by id, name and inverse name.
	 */
	private Map<String,RelationType> rtmap;

        private ONDEXGraph graph;
	
	/**
	 * constructor.
	 */
	public MDIndex(ONDEXGraph graph) {
            this.graph = graph;
            index();
	}
	
	
	/**
	 * performs indexing over complete set of metadata.
	 * reports possible clashes to STDERR.
	 */
	private void index() {
		ccmap = new HashMap<String, ConceptClass>();
		rtmap = new HashMap<String, RelationType>();
		
		ONDEXGraphMetaData md = graph.getMetaData();
		
		for (ConceptClass cc : md.getConceptClasses()) {
			ccmap.put(cc.getId(),cc);
			
			ConceptClass clash = ccmap.get(cc.getFullname());
			if (clash != null && !clash.equals(cc)) {
				System.err.println("The name \""+cc.getFullname()+"\" of concept class \""+cc.getId()+
								   "\" is already in use by \""+clash.getId()+"\"");
			} else {
				ccmap.put(cc.getFullname(), cc);
			}
		}
		
		for (RelationType rt : md.getRelationTypes()) {
			rtmap.put(rt.getId(), rt);
			
			RelationType clash = rtmap.get(rt.getFullname());
			if (clash != null && !clash.equals(rt)) {
				System.err.println("The name \""+rt.getFullname()+"\" of concept class \""+rt.getId()+
								   "\" is already in use by \""+clash.getId()+"\"");
			} else {
				rtmap.put(rt.getFullname(), rt);
			}
			
			clash = rtmap.get(rt.getInverseName());
			if (clash != null && !clash.equals(rt)) {
				System.err.println("The name \""+rt.getInverseName()+"\" of concept class \""+rt.getId()+
								   "\" is already in use by \""+clash.getId()+"\"");
			} else {
				rtmap.put(rt.getInverseName(), rt);
			}
		}
	}
	
	/**
	 * returns concept class that carries the given key as id or name.
	 * @param key
	 * @return
	 */
	public ConceptClass resolveConceptClass(String key) {
		return ccmap.get(key);
	}
	
	/**
	 * returns relation type that carries the given key as id, name or inverse name.
	 * @param key
	 * @return
	 */
	public RelationType resolveRelationType(String key) {
		return rtmap.get(key);
	}
}
