package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.Literal;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.base.AttributeNameImpl;
import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.properties.CollectionPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.LiteralPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.ResourcePropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfLiteralGenerator;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;

/**
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Nov 2017</dd></dl>
 *
 */
public abstract class ONDEXEntityMapper<OE extends ONDEXEntity> extends BeanRdfMapper<OE>
{	
	public static abstract class AbstractUriGenerator<OE extends ONDEXEntity> extends RdfUriGenerator<OE>
	{
		/**
		 * Convenience {@link AttributeName} used to extract the URI attribute.
		 */
		@SuppressWarnings ( "serial" )
		private static class UriAttrType extends AttributeNameImpl
		{
			public UriAttrType () {
				// We just need the ID for search purposes, the rest is irrelevant
				super ( -1, "iri", "", "", null, String.class.getCanonicalName (), null );			
			}
		}
		
		/** 
		 * Convenience {@link AttributeName} used to extract the URI attribute.
		 */
		protected static final AttributeName URI_ATTR_TYPE = new UriAttrType ();	

		/**
		 * Returns the URI, if `oe` has the URI_ATTR_TYPE property, else returns null. So, we override
		 * this base to get the specific entity's URI, if not already stored.
		 */
		@Override
		public String getUri ( OE oe, Map<String, Object> params )
		{
			return Optional.ofNullable ( oe.getAttribute ( URI_ATTR_TYPE ) )
				.map ( uri -> (String) uri.getValue () )
				.orElse ( null );
		}
	}
	
	
	private RdfLiteralGenerator<Object> attrValGen = new RdfLiteralGenerator<> ();
	
	{
		this.addPropertyMapper ( "id", new LiteralPropRdfMapper<> ( iri ( "bk:ondexId" ) ) );
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
