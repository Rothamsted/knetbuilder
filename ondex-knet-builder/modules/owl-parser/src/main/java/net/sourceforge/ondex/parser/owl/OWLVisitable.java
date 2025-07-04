package net.sourceforge.ondex.parser.owl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.parser.Visitable;

/**
 * This implements the {@link Visitable} interface, by adding an RDF statement to the input passed to 
 * {@link #setVisited(OntClass, boolean)} that is a boolean flag.
 * 
 * We mark OWL classes as visited in {@link OWLMapper#scanTree(OntClass, OntClass, net.sourceforge.ondex.core.ONDEXGraph)}, 
 * in order to avoid loops when traversing a graph following some relation like rdfs:subClassOf. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Aug 2017</dd></dl>
 *
 */
public class OWLVisitable implements Visitable<OntClass>
{
	private Set<String> visitedClassUris = new HashSet<> ();
		
	public OWLVisitable () {
	}

	
	@Override
	public boolean isVisited ( OntClass ontCls )
	{
		return visitedClassUris.contains ( ontCls.getURI () );
	}

	@Override
	public boolean setVisited ( OntClass ontCls, boolean isVisited )
	{
		if ( isVisited ) return !visitedClassUris.add ( ontCls.getURI () );
		return visitedClassUris.remove ( ontCls.getURI () );
	}
	
}
