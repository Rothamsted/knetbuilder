package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.apache.commons.collections4.CollectionUtils.sizeIsEmpty;
import static uk.ac.ebi.utils.ids.IdUtils.hashUriSignature;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.rdf.api.Graph;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.rdf.OndexRDFUtils;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils;
import uk.ac.ebi.utils.ids.IdUtils;

/**
 * Maps {@link ONDEXRelation}. These are translated into straight RDF triples and, if further elements
 * are to be associated to a relation, a bk:Relation instance is created too and these elements linked to it.
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
	 * Two reified relations are considered the same (and hence the same URI is generated here) if they have 
	 * the same type, and the same attributes (which are compared based on name, type, unit, string value).  
	 *   
	 */
	public static class UriGenerator extends AbstractUriGenerator<ONDEXRelation>
	{
		@Override
		public String getUri ( ONDEXRelation rel, Map<String, Object> params )
		{
			String uriAttr = super.getUri ( rel, params );
			if ( uriAttr != null ) return uriAttr;
			
			String ns = Java2RdfUtils.getParam ( params, "instanceNamespace", NamespaceUtils.ns ( "bkr" ) );
			
			String rtPart = Optional
				.ofNullable ( rel.getOfType () )
				.flatMap ( rt -> Optional.ofNullable ( rt.getId () ) )
				.orElse ( "" );
			
			RdfMapperFactory xfact = this.getMapperFactory ();
			
			// We need the ID part of the URIs, so let's use an empty namespace 
			final Map<String, Object> noNs = Collections.singletonMap ( "instanceNamespace", "" );
			
			String fromPart = xfact.getUri ( rel.getFromConcept (), noNs );
			// Sometimes the URI already exists and, in that case, we don't want 'http://...' thrown inside the relation URI
			if ( fromPart.matches ( "^[a-z]+://.+" ) ) fromPart = hashUriSignature ( fromPart );
			
			String toPart = xfact.getUri ( rel.getToConcept (), noNs );
			// ditto
			if ( toPart.matches ( "^[a-z]+://.+" ) ) toPart = hashUriSignature ( toPart );

			// The attributes part.
			// So, we need this complex stringfication of attributes, to take relations that differ in attributes only
			// into account.
			String attrPart = rel
				.getAttributes ()
				.stream ()
				.map ( a -> 
				{
					String namePart = Optional.ofNullable ( a.getOfType () )
					  .map ( aname -> 
					  		aname.getId () 
					  		+ Optional.ofNullable ( aname.getUnit () ).map ( u -> u.getId () ).orElse ( "" )
					  		+ ObjectUtils.defaultIfNull ( aname.getDataTypeAsString (), "_" )
					  	)
					  .orElse ( "" );
					
					String valuePart = Optional.ofNullable ( a.getValue () ).map ( Object::toString ).orElse ( "" );
					
					return namePart + valuePart;
				})
				.sorted () // order can't count on ID building, so let's get a conventional order
				.collect ( Collectors.joining () );
			
			// Let's make something simpler of this ugly/long thing 
			attrPart = IdUtils.hashUriSignature ( attrPart ); 
			
			// The last parameter won't be used in this case
			return OndexRDFUtils.iri ( ns, rtPart, fromPart + '_' + toPart + '_' + attrPart, -1 );
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
		//
		
		if ( sizeIsEmpty ( rel.getAttributes () ) && sizeIsEmpty ( rel.getEvidence () ) && sizeIsEmpty ( rel.getTags () ) )
			// If we have nothing to add to the triple, let's just skip it.
			// With the current Ondex implementation, this should never happen, since evidence is mandatory
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
		
		// The OXL has the qualifier slot, which the OXL parser explicitly ignores when present, so, we 
		// do it here too.
		
		return true;
	}

}
