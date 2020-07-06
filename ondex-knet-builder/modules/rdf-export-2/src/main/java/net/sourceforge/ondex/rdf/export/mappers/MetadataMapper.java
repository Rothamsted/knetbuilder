package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import java.util.Map;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.rdf.OndexRDFUtils;
import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.LiteralPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.rdfgen.RdfUriGenerator;
import uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils;

/**
 * Maps common aspects of {@link MetaData}.
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
			return ns + OndexRDFUtils.idEncode ( id );
		}
	}
	
	{
		this.setRdfUriGenerator ( new UriGenerator<M> () );
		this.addPropertyMapper ( "id", new LiteralPropRdfMapper<> ( iri ( "dcterms:identifier" ) ) );
		this.addPropertyMapper ( "fullname", new LiteralPropRdfMapper<> ( iri ( "rdfs:label" ) ) );
		this.addPropertyMapper ( "description", new LiteralPropRdfMapper<> ( iri ( "dcterms:description" ) ) );
	}
	
}
