package net.sourceforge.ondex.core.graphimpl.mapdb;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXRelation;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Jan 2022</dd></dl>
 *
 */
public class MapDbOndexRelation extends MemoryONDEXRelation
{

	private static final long serialVersionUID = 1L;

	MapDbOndexRelation ( 
		long sid, MapDbOndexGraph graph, int id, ONDEXConcept fromConcept, ONDEXConcept toConcept, RelationType ofType )
	{
		super ( sid, graph, id, fromConcept, toConcept, ofType );
	}

	void setGraph ( MapDbOndexGraph graph )
	{
		this.graph = graph;
	}
	
}
