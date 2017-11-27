package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.apache.commons.lang3.ArrayUtils.contains;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.MetaData;
import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.properties.LiteralPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;
import uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils;
import uk.ac.ebi.utils.ids.IdUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>27 Nov 2017</dd></dl>
 *
 */
public abstract class MetadataMapper<M extends MetaData> extends BeanRdfMapper<M>
{
	public static class UriGenerator<M extends MetaData> extends RdfUriGenerator<M>
	{
		@Override
		public String getUri ( M meta, Map<String, Object> params )
		{
			String ns = Java2RdfUtils.getParam ( params, "ontologyNamespace", NamespaceUtils.ns ( "bk" ) );
			String id = meta.getId (); 
			return ns + IdUtils.urlEncode ( id );
		}
	}
	
	{
		this.setRdfUriGenerator ( new UriGenerator<M> () );
		this.addPropertyMapper ( "id", new LiteralPropRdfMapper<> ( iri ( "dcterms:identifier" ) ) );
		this.addPropertyMapper ( "fullname", new LiteralPropRdfMapper<> ( iri ( "rdfs:label" ) ) );
		this.addPropertyMapper ( "description", new LiteralPropRdfMapper<> ( iri ( "dcterms:description" ) ) );
	}
	
}
