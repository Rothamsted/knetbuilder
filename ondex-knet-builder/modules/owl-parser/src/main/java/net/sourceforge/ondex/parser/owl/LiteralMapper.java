package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.TextMapper;

/**
 * Maps an RDF literal to a string, using its lexical representation.
 * 
 * TODO: allow for the conversion from strings to other types.
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
