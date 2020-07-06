package net.sourceforge.ondex.parser.owl;

import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.parser.TextMapper;
import net.sourceforge.ondex.parser.utils.ChainingMapper;

/**
 * A {@link ChainingMapper} that uses {@link LiteralMapper} as base mapper and implements 
 * the {@link TextMapper} interface, so that you can use it where that's required.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2018</dd></dl>
 *
 */
public class ChainingLiteralMapper extends ChainingMapper<RDFNode, String> implements TextMapper<RDFNode>
{
	public ChainingLiteralMapper () {
		this.setBaseMapper ( new LiteralMapper () );
	}
}
