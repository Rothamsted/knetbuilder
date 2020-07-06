package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.rdf.api.Graph;

import info.marcobrandizi.rdfutils.XsdMapper;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Unit;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.properties.ResourcePropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;
import uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils;
import uk.ac.ebi.utils.ids.IdUtils;

/**
 * Maps an attribute type as a subproperty of bk:attribute, as expected by the bioknet ontology.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Nov 2017</dd></dl>
 *
 */
public class AttributeNameMapper extends MetadataMapper<AttributeName>
{
	public static class UriGenerator extends RdfUriGenerator<AttributeName>
	{
		@Override
		public String getUri ( AttributeName aname, Map<String, Object> params )
		{
			String ns = Java2RdfUtils.getParam ( params, "ontologyNamespace", NamespaceUtils.ns ( "bka" ) );
			String id = aname.getId (); 
			return ns + IdUtils.urlEncode ( id );
		}
	}


	{
		this.setRdfUriGenerator ( new UriGenerator () );
		this.addPropertyMapper ( "unit", new ResourcePropRdfMapper<AttributeName, Unit> ( iri ( "bk:attributeUnit" ) ) );
	}
	
	
	/**
	 * Note that the range is mapped using {@link XsdMapper}. You need a custom implementation for more complicated
	 * cases.
	 * 
	 */
	@Override
	public boolean map ( AttributeName aname, Map<String, Object> params )
	{
		if ( !super.map ( aname, params ) ) return false;
		
		RdfMapperFactory xfact = this.getMapperFactory ();
		Graph graphModel = xfact.getGraphModel ();
		RdfUriGenerator<AttributeName> uriGen = this.getRdfUriGenerator ();

		String myiri = uriGen.getUri ( aname, params );

		// The parent attribute type
		//
		AttributeName parent = aname.getSpecialisationOf ();
		
		String parentIri = parent == null
			? iri ( "bk:attribute" ) 
			: uriGen.getUri ( parent );
		COMMUTILS.assertResource ( graphModel, myiri, iri ( "rdfs:subPropertyOf" ), parentIri );
		
		String dataTypeIri = Optional.ofNullable ( aname.getDataType () )
			.map ( clazz -> XsdMapper.dataTypeIri ( clazz ) )
			.orElse ( null );
		
		// The range, if a proper mapping exists.
		if ( dataTypeIri != null ) 
			COMMUTILS.assertResource ( graphModel, myiri, iri ( "rdfs:range" ), dataTypeIri );
		
		return true;
	}

}
