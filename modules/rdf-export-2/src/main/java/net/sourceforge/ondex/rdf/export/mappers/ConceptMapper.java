package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;

import info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.rdf.OndexRDFUtils;
import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.properties.LiteralPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;
import uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Nov 2017</dd></dl>
 *
 */
public class ConceptMapper extends ONDEXEntityMapper<ONDEXConcept>
{
	public static class UriGenerator extends RdfUriGenerator<ONDEXConcept>
	{
		@Override
		public String getUri ( ONDEXConcept c, Map<String, Object> params )
		{
			
			String ns = Java2RdfUtils.getParam ( params, "instanceNamespace", NamespaceUtils.ns ( "bkr" ) );
			
			String ccPart = Optional
				.ofNullable ( c.getOfType () )
				.flatMap ( cc -> Optional.ofNullable ( cc.getId () ) )
				.orElse ( null );

// TODO: remove
//			String idPart = c.getConceptAccessions ()
//			.stream ()
//			.filter ( acc -> acc != null && !acc.isAmbiguous () )
//			.map ( ConceptAccession::getAccession )
//			.sorted ()
//			.findFirst ()
//			.orElse ( String.valueOf ( c.getId () ) );

			return OndexRDFUtils.iri ( ns, ccPart, c.getPID (), c.getId () );
		}				
	}
	
	{
/*
* 		
		c.getAttributes ();
		c.getConceptAccessions ();
		c.getConceptName ();
		c.getElementOf ();
		c.getEvidence ();
		c.getTags ();

*/
		this.setRdfUriGenerator ( new UriGenerator () );
	}

	@Override
	public boolean map ( ONDEXConcept concept, Map<String, Object> params )
	{
		if ( !super.map ( concept, params ) ) return false;

		RdfMapperFactory xfact = this.getMapperFactory ();

		String myiri = this.getRdfUriGenerator ().getUri ( concept, params );
		
		ConceptClass cc = concept.getOfType ();
		String cciri = cc == null ? iri ( "bk:Concept" ) : xfact.getUri ( cc, params );
		COMMUTILS.assertResource ( xfact.getGraphModel (), myiri, iri ( "rdf:type" ), cciri );
		
		return true;
	}

}
