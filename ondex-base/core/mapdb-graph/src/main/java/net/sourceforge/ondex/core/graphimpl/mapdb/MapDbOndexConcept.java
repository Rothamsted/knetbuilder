package net.sourceforge.ondex.core.graphimpl.mapdb;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXConcept;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Jan 2022</dd></dl>
 *
 */
class MapDbOndexConcept extends MemoryONDEXConcept
{
	private static final long serialVersionUID = 1L;

	MapDbOndexConcept ( 
		long sid, MapDbOndexGraph graph, int id, String pid, String annotation,
		String description, DataSource elementOf, ConceptClass ofType 
	)
	{
		super ( sid, graph, id, pid, annotation, description, elementOf, ofType );
	}
	
	void setGraph ( MapDbOndexGraph graph )
	{
		this.graph = graph;
	}
}
