package net.sourceforge.ondex.parser2;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class DefaultAccessionsMapper<S, SI> implements AccessionsMapper<S>
{
	private Scanner<S, SI> scanner;
	private AccessionMapper<SI> accessionMapper;
	
	@Override
	public Stream<ConceptAccession> map ( S src, ONDEXConcept concept, ONDEXGraph graph )
	{
		return scanner.scan ( src )
		.map ( si -> accessionMapper.map ( si, concept, graph ) );
	}

	public AccessionMapper<SI> getAccessionMapper ()
	{
		return accessionMapper;
	}

	public void setAccessionMapper ( AccessionMapper<SI> accessionMapper )
	{
		this.accessionMapper = accessionMapper;
	}

	public Scanner<S, SI> getScanner ()
	{
		return scanner;
	}

	public void setScanner ( Scanner<S, SI> scanner )
	{
		this.scanner = scanner;
	}
}
