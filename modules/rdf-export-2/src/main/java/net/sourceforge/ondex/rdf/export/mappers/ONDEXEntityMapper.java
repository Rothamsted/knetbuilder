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
import net.sourceforge.ondex.core.ONDEXEntity;
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
public abstract class ONDEXEntityMapper<OE extends ONDEXEntity> extends BeanRdfMapper<OE>
{
	{
		this.addPropertyMapper ( "PID", new LiteralPropRdfMapper<> ( iri ( "dcterms:identifier" ) ) );
		this.addPropertyMapper ( "description", new LiteralPropRdfMapper<> ( iri ( "dcterms:description" ) ) );
		this.addPropertyMapper ( "annotation", new LiteralPropRdfMapper<> ( iri ( "rdfs:comment" ) ) );
	}

}
