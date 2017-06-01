package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;

/**
 * A wrapper of {@link OWLAxiomMapper} to map accessions in owl:Axiom.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public class OWLAccsMapperFromAxiom extends OWLAccessionsMapper
{
	private String mappedPropertyIri = null;

	public static class HelperMapper extends OWLAxiomMapper<Stream<String>, Void>
	{
		@Override
		public Stream<String> map ( OntClass ontCls, Void ondexTarget )
		{
			return this.getMappedNodes ( ontCls )
			.map ( accNode -> JENAUTILS.literal2Value ( accNode ).get () );
		}
	}
	
	private HelperMapper helperMapper = new HelperMapper ();

	@Override
	protected Stream<String> getAccessionStrings ( OntClass ontCls )
	{
		this.helperMapper.setPropertyIri ( this.getPropertyIri () );
		this.helperMapper.setMappedPropertyIri ( this.getMappedPropertyIri () );
		return this.helperMapper.map ( ontCls, null );
	}
	
	public String getMappedPropertyIri ()
	{
		return mappedPropertyIri;
	}

	public void setMappedPropertyIri ( String mappedPropertyIri )
	{
		this.mappedPropertyIri = mappedPropertyIri;
	}

}
