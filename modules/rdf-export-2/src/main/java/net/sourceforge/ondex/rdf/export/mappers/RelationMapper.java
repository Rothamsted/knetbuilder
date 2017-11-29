package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.apache.commons.collections4.CollectionUtils.sizeIsEmpty;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.rdf.api.Graph;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.rdf.OndexRDFUtils;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;
import uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Nov 2017</dd></dl>
 *
 */
public class RelationMapper extends ONDEXEntityMapper<ONDEXRelation>
{
	/**
	 * This provides the URI of the reified relation, the map() method uses the 
	 * {@link MetadataMapper.UriGenerator RelationType's URI generator} to get the OWL property that defines
	 * the straight relation/statement as well. 
	 *   
	 */
	public static class UriGenerator extends RdfUriGenerator<ONDEXRelation>
	{
		@Override
		public String getUri ( ONDEXRelation rel, Map<String, Object> params )
		{
			String ns = Java2RdfUtils.getParam ( params, "instanceNamespace", NamespaceUtils.ns ( "bkr" ) );
			
			String rtPart = Optional
				.ofNullable ( rel.getOfType () )
				.flatMap ( rt -> Optional.ofNullable ( rt.getId () ) )
				.orElse ( "" );
			
			RdfMapperFactory xfact = this.getMapperFactory ();
			
			// We reuse the Concept URI generator to get the ID part, so let's pass it the empty namespace
			// TODO: for the moment it works fine, a safer version should merge this with the original params and
			// pass them all to getUri() below.
			final Map<String, Object> noNs = Collections.singletonMap ( "instanceNamespace", "" );
			
			String fromPart = xfact.getUri ( rel.getFromConcept (), noNs ); 
			String toPart = xfact.getUri ( rel.getToConcept (), noNs );
			
			// The rel.getId() shouldn't be used here, but just in case
			return OndexRDFUtils.iri ( ns, rtPart, fromPart + "_" + toPart, rel.getId () );
		}				
	}
	
	{
		this.setRdfUriGenerator ( new UriGenerator () );
	}

	@Override
	public boolean map ( ONDEXRelation rel, Map<String, Object> params )
	{
		if ( !super.map ( rel, params ) ) return false;

		RdfMapperFactory xfact = this.getMapperFactory ();
		Graph graph = xfact.getGraphModel (); 

		// Straight relation
		String fromIri = xfact.getUri ( rel.getFromConcept (), params );
		String toIri = xfact.getUri ( rel.getToConcept (), params );
		String relTypeIri = xfact.getUri ( rel.getOfType (), params );

		COMMUTILS.assertResource ( graph, fromIri, relTypeIri, toIri );

		// And the corresponding reified relation
		
		
		if ( sizeIsEmpty ( rel.getAttributes () ) && sizeIsEmpty ( rel.getEvidence () ) && sizeIsEmpty ( rel.getTags () ) )
			// If we have nothing to add to the triple, let's just skip it.
			return true;
		
		String reifiedIri = this.getRdfUriGenerator ().getUri ( rel, params );
		
		// We prefer to control these here (instead of creating the usual property mappers), since
		// the reified relation must not be created if there aren't further things to attach to it
		// evidences/etc are mapped via property mappers, and only if these have values.
		//
		COMMUTILS.assertResource ( graph, reifiedIri, iri ( "rdf:type" ), iri ( "bk:Relation" ) );
		COMMUTILS.assertResource ( graph, reifiedIri, iri ( "bk:relTypeRef" ), relTypeIri );
		COMMUTILS.assertResource ( graph, reifiedIri, iri ( "bk:relFrom" ), fromIri );
		COMMUTILS.assertResource ( graph, reifiedIri, iri ( "bk:relTo" ), toIri );
		
		return true;
	}

}
