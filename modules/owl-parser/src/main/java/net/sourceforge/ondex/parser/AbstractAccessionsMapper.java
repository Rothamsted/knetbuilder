package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * The default {@link AccessionsMapper} is based on {@link ScannerPairMapper}, that is, a {@link Scanner} decomposes an 
 * initial data source into smaller items, each of which is mapped to an {@link ONDEXConcept} by means of 
 * a {@link ConceptAccession single-accession mapper}. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public abstract class AbstractAccessionsMapper<S, SI> 
  extends ScannerPairMapper<S, SI, ONDEXConcept, ConceptAccession> 
  implements AccessionsMapper<S>
{	
	public AbstractAccessionsMapper () 
	{
		this ( null, null );
	}
	
	
	@SuppressWarnings ( "unchecked" )
	protected AbstractAccessionsMapper ( Scanner<S, SI> scanner, AccessionMapper<S> accessionMapper )
	{
		super ( scanner, (AccessionMapper<SI>) accessionMapper );
	}

	
	protected AccessionMapper<SI> getAccessionMapper ()
	{
		return (AccessionMapper<SI>) super.getMapper ();
	}

	protected void setAccessionMapper ( AccessionMapper<SI> accessionMapper )
	{
		super.setMapper ( (AccessionMapper<SI>) accessionMapper );
	}

	protected Scanner<S, SI> getScanner ()
	{
		return scanner;
	}

	protected void setScanner ( Scanner<S, SI> scanner )
	{
		this.scanner = scanner;
	}
}
