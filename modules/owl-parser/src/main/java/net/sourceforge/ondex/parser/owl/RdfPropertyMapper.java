package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.parser.ONDEXMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Apr 2017</dd></dl>
 *
 */
public abstract class RdfPropertyMapper<O, OT> implements ONDEXMapper<O, OntClass, OT>
{
	protected String propertyIri;

	/**
	 * The property that this mapper deals with. Examples are rdfs:label, rdfs:comment, skos:label.
	 */
	public String getPropertyIri ()
	{
		return propertyIri;
	}

	public void setPropertyIri ( String propertyIri )
	{
		this.propertyIri = propertyIri;
	}

}