package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;

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
	protected String visitedProp = iri ( "odx:isProcessedNode" );
	
	public OWLVisitable ( String visitedProp ) {
		this.visitedProp = iri ( visitedProp );
	}

	public OWLVisitable () {
	}

	
	@Override
	public boolean isVisited ( OntClass ontCls )
	{
		return
			JENAUTILS.getObject ( ontCls.getModel (), ontCls.getURI (), visitedProp, true ) 
			.flatMap ( JENAUTILS::literal2Boolean )
			.orElse ( false );
	}

	@Override
	public boolean setVisited ( OntClass ontCls, boolean isVisited )
	{
		boolean oldValue = this.isVisited ( ontCls );
		if ( oldValue == isVisited ) return oldValue;
		
		OntModel m = ontCls.getOntModel ();
		Property pisVisited = m.getProperty ( visitedProp );
		ontCls.removeAll ( pisVisited );
		ontCls.addLiteral ( pisVisited, isVisited );
		return oldValue;
	}
}
