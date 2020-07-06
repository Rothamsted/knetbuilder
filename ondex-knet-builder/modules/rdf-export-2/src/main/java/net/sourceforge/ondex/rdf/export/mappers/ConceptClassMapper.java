package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.apache.commons.lang3.ArrayUtils.contains;

import java.util.Map;

import net.sourceforge.ondex.core.ConceptClass;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;

/**
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>27 Nov 2017</dd></dl>
 *
 */
public class ConceptClassMapper extends MetadataMapper<ConceptClass>
{
	/**
	 * These are all mapped to the top class {@code bk:Concept}.
	 */
	protected static final String[] IGNORED_IDS = { "Thing", "UndefinedSemantics" };

	@Override
	public boolean map ( ConceptClass cc, Map<String, Object> params )
	{
		if ( !super.map ( cc, params ) ) return false;

		String ccId = cc.getId ();
		if ( contains ( IGNORED_IDS, ccId )) return false;
		
		RdfMapperFactory xfact = this.getMapperFactory ();
		RdfUriGenerator<ConceptClass> uriGen = this.getRdfUriGenerator ();

		String myiri = uriGen.getUri ( cc, params );

		ConceptClass parent = cc.getSpecialisationOf ();
		String parentIri = parent == null || contains ( IGNORED_IDS, parent.getId () ) 
			? iri ( "bk:Concept" ) 
			: uriGen.getUri ( parent );
		COMMUTILS.assertResource ( xfact.getGraphModel (), myiri, iri ( "rdfs:subClassOf" ), parentIri );

		return true;
	}
	
}
