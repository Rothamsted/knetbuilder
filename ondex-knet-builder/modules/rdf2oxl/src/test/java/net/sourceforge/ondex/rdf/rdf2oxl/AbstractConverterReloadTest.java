package net.sourceforge.ondex.rdf.rdf2oxl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.tuple.Pair;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * A common template to test `RDF->OXL->load graph` conversions.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Oct 2018</dd></dl>
 *
 */
public abstract class AbstractConverterReloadTest extends AbstractConverterTest
{
	
	protected static ONDEXGraph resultGraph;

	/**
	 * Uses {@link AbstractConverterTest#generateOxl(String, String, Pair...) to trigger the OXL conversion}, then loads the
	 * result as {@link ONDEXGraph}.  
	 */
	@SafeVarargs
	protected static ONDEXGraph loadOxl ( String outPath, String tdbPath, Pair<InputStream, String>... rdfInputs ) throws IOException
	{
		generateOxl ( outPath, tdbPath, rdfInputs );
		return Parser.loadOXL ( outPath );
	}
}