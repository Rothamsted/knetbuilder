package net.sourceforge.ondex.parser.owl;

/**
 * An interface to represent those entities that can configure the IRI/URI of an RDF property, to be used for various
 * purposes (e.g., to scan/map values). 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public interface RdfPropertyConfigurator
{
	public String getPropertyIri ();

	public void setPropertyIri ( String propertyIri );
}
