package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.rdf.api.Graph;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.rdf.OndexRDFUtils;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.properties.CollectionPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.LiteralPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.ResourcePropRdfMapper;
import uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils;

/**
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Nov 2017</dd></dl>
 */
public class ConceptMapper extends ONDEXEntityMapper<ONDEXConcept>
{
	public static class UriGenerator extends AbstractUriGenerator<ONDEXConcept>
	{
		@Override
		public String getUri ( ONDEXConcept c, Map<String, Object> params )
		{
			String uriAttr = super.getUri ( c, params );
			if ( uriAttr != null ) return uriAttr;
			
			String ns = Java2RdfUtils.getParam ( params, "instanceNamespace", NamespaceUtils.ns ( "bkr" ) );
			
			String ccPart = Optional
				.ofNullable ( c.getOfType () )
				.flatMap ( cc -> Optional.ofNullable ( cc.getId () ) )
				.orElse ( null );
			
			return OndexRDFUtils.iri ( ns, ccPart, c.getPID (), c.getId () );
		}				
	}
	
	{
		this.setRdfUriGenerator ( new UriGenerator () );

		this.addPropertyMapper ( "PID", new LiteralPropRdfMapper<> ( iri ( "dcterms:identifier" ) ) );
		this.addPropertyMapper ( "description", new LiteralPropRdfMapper<> ( iri ( "dcterms:description" ) ) );
		this.addPropertyMapper ( "annotation", new LiteralPropRdfMapper<> ( iri ( "rdfs:comment" ) ) );
		this.addPropertyMapper ( "elementOf", new ResourcePropRdfMapper<ONDEXConcept, DataSource> ( iri ( "bk:dataSource" ) ) );
		this.addPropertyMapper ( "conceptAccessions", new CollectionPropRdfMapper<ONDEXConcept, ConceptAccession, String> ( 
			new ResourcePropRdfMapper<> ( iri ( "dc:identifier" ) )
		));
	}

	@Override
	public boolean map ( ONDEXConcept concept, Map<String, Object> params )
	{
		if ( !super.map ( concept, params ) ) return false;

		RdfMapperFactory xfact = this.getMapperFactory ();
		Graph graphModel = xfact.getGraphModel ();

		String myiri = this.getRdfUriGenerator ().getUri ( concept, params );
		
		ConceptClass cc = concept.getOfType ();
		String cciri = cc == null ? iri ( "bk:Concept" ) : xfact.getUri ( cc, params );
		
		COMMUTILS.assertResource ( graphModel, myiri, iri ( "rdf:type" ), cciri );
		
		// Names split into 1 preferred and alternatives
		Pair<Optional<String>, Stream<String>> nameStrings = OndexRDFUtils.normalizeNames ( 
			concept.getConceptNames (), ConceptName::isPreferred, ConceptName::getName 
		);
		
		nameStrings.getLeft ()
		.ifPresent ( 
			prefName -> COMMUTILS.assertLiteral ( graphModel, myiri, iri ( "bk:prefName" ), prefName ) 
		);
		
		nameStrings.getRight ()
		.forEach ( 
			altName -> COMMUTILS.assertLiteral ( graphModel, myiri, iri ( "bk:altName" ), altName )
		);
						
		return true;
	}

}
