package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.Literal;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXEntity;
import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.properties.CollectionPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.ResourcePropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfLiteralGenerator;

/**
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Nov 2017</dd></dl>
 *
 */
public abstract class ONDEXEntityMapper<OE extends ONDEXEntity> extends BeanRdfMapper<OE>
{
	private RdfLiteralGenerator<Object> attrValGen = new RdfLiteralGenerator<> ();
	
	{
		this.addPropertyMapper ( "evidence", new CollectionPropRdfMapper<OE, EvidenceType, String> ( 
			new ResourcePropRdfMapper<> ( iri ( "bk:evidence" ) )
		));
		this.addPropertyMapper ( "tags", new CollectionPropRdfMapper<OE, EvidenceType, String> ( 
			new ResourcePropRdfMapper<> ( iri ( "bk:relatedConcept" ) )
		));
	}
	
	@Override
	public boolean map ( OE oe, Map<String, Object> params )
	{
		if ( !super.map ( oe, params ) ) return false;
		
		RdfMapperFactory xfact = this.getMapperFactory ();
		Graph graphModel = xfact.getGraphModel ();
		
		String myiri = this.getRdfUriGenerator ().getUri ( oe, params );		
		
		
		// Map the attributes
		//
		for ( Attribute attr: oe.getAttributes () )
		{
			Object aval = attr.getValue ();

			// It might be a collection, so...
			@SuppressWarnings ( "unchecked" )
			Collection<Object> avals = 
				aval instanceof Collection ? (Collection<Object>) aval
				: aval.getClass ().isArray () ? Arrays.asList ( (Object[]) aval )
				: Collections.singleton ( aval ); 
			
			for ( Object thisVal: avals )
			{
				Literal vl = attrValGen.getLiteral ( thisVal, params );
				if ( vl == null ) continue;
				
				String attrProp = xfact.getUri ( attr.getOfType (), params );
				COMMUTILS.assertLiteral ( graphModel, myiri, attrProp, vl );
				
				if ( !attr.isDoIndex () ) continue;
					// This goes at the level of the attribute type, as explained in the bioknet ontology file.
				COMMUTILS.assertLiteral ( 
					graphModel, attrProp, iri ( "bk:isIndexed" ), COMMUTILS.value2TypedLiteral ( graphModel, true ).get () 
				);
			}			
		}
				
		return true;		
	}

	@Override
	public void setMapperFactory ( RdfMapperFactory mapperFactory ) 
	{
		super.setMapperFactory ( mapperFactory );
		this.attrValGen.setMapperFactory ( mapperFactory );
	}
	
}
