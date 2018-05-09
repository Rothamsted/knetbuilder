package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.apache.commons.lang3.ArrayUtils.contains;

import java.util.Map;

import net.sourceforge.ondex.core.RelationType;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;

/**
 * Maps a {@link RelationType} to a subproperty of bk:conceptsRelation.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>27 Nov 2017</dd></dl>
 *
 */
public class RelationTypeMapper extends MetadataMapper<RelationType>
{
	/**
	 * These are all mapped to the top property {@code bk:conceptsRelation}.
	 */	
	protected static final String[] IGNORED_IDS = { "relatedTo", "r", "undefined_semantics", "none" };

	@Override
	public boolean map ( RelationType rt, Map<String, Object> params )
	{
		if ( !super.map ( rt, params ) ) return false;

		String ccId = rt.getId ();
		if ( contains ( IGNORED_IDS, ccId )) return false;
		
		RdfMapperFactory xfact = this.getMapperFactory ();
		RdfUriGenerator<RelationType> uriGen = this.getRdfUriGenerator ();

		String myiri = uriGen.getUri ( rt, params );

		RelationType parent = rt.getSpecialisationOf ();
		String parentIri = parent == null || contains ( IGNORED_IDS, parent.getId () ) 
			? iri ( "bk:conceptsRelation" ) 
			: uriGen.getUri ( parent );
		
			COMMUTILS.assertResource ( xfact.getGraphModel (), myiri, iri ( "rdfs:subPropertyOf" ), parentIri );

		return true;
	}
	
}
