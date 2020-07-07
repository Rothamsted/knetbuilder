package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.parser.Scanner;

/**
 * Fetches RDF literals, associated to OWL classes by means of a {@link #getPropertyIri() property}.
 * This is used with {@link OWLTextsMapper}.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class RdfPropertyScanner implements Scanner<OntClass, RDFNode>
{
	private String propertyIri;
	
	@Override
	public Stream<RDFNode> scan ( OntClass ontCls )
	{
		OntModel model = ontCls.getOntModel ();
		Property prop = model.getProperty ( this.getPropertyIri () );
		
		return JENAUTILS.toStream ( 
			ontCls.listPropertyValues ( prop ), true 
		);
	}

	public String getPropertyIri ()
	{
		return propertyIri;
	}

	public void setPropertyIri ( String propertyIri )
	{
		this.propertyIri = propertyIri;
	}
}
