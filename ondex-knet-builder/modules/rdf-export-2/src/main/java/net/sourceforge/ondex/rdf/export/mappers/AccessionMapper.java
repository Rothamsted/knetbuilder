package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import java.util.Map;
import java.util.Optional;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.rdf.OndexRDFUtils;
import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.LiteralPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.ResourcePropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfTrueGenerator;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;
import uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Nov 2017</dd></dl>
 *
 */
public class AccessionMapper extends BeanRdfMapper<ConceptAccession>
{
	public static class UriGenerator extends RdfUriGenerator<ConceptAccession>
	{
		@Override
		public String getUri ( ConceptAccession acc, Map<String, Object> params )
		{
			String ns = Java2RdfUtils.getParam ( params, "instanceNamespace", NamespaceUtils.ns ( "bkr" ) );
			String accValue = acc.getAccession ();
			String dsPart = Optional.ofNullable ( acc.getElementOf () ).map ( DataSource::getId ).orElse ( "generic" );
			
			// The accession is always present, so -1 will never be used
			return OndexRDFUtils.iri ( ns + "accsn_", dsPart, accValue, -1 );
		}		
	}
	
	{
		this.setRdfUriGenerator ( new UriGenerator () );
		this.setRdfClassUri ( NamespaceUtils.iri ( "bk:Accession" ) );
		
		this.addPropertyMapper ( "accession", new LiteralPropRdfMapper<> ( iri ( "dcterms:identifier" ) ) );
		this.addPropertyMapper ( "elementOf", new ResourcePropRdfMapper<ConceptAccession, DataSource> ( iri ( "bk:dataSource" ) ) );
		this.addPropertyMapper ( "ambiguous", 
			new LiteralPropRdfMapper<> ( iri ( "bk:isAmbiguousAccession" ), new RdfTrueGenerator () ) 
		);		
	}
}
