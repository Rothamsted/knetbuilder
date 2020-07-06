package net.sourceforge.ondex.rdf.export.mappers;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import net.sourceforge.ondex.core.EvidenceType;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Nov 2017</dd></dl>
 *
 */
public class EvidenceMapper extends MetadataMapper<EvidenceType>
{
	{
		this.setRdfClassUri ( iri ( "bk:EvidenceType" ) );
	}
}
