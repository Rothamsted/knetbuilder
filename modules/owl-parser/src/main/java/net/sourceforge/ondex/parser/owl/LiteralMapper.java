package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.SimpleDescriptionMapper;
import net.sourceforge.ondex.parser.SimpleLabelMapper;
import net.sourceforge.ondex.parser2.TextMapper;

/**
 * 
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class LiteralMapper implements TextMapper<RDFNode> 
{	
	@Override
	public String map ( RDFNode literalNode, ONDEXGraph graph )
	{
		return JENAUTILS.literal2Value ( literalNode ).orElse ( null );
	}	
}
