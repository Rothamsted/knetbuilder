package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * An {@link AccessionsMapper} based on {@link ScannerPairMapper}, that is, a {@link Scanner} that decomposes an 
 * initial data source into smaller items, each of which is mapped to an {@link ONDEXConcept} by means of 
 * a {@link AccessionMapper single-accession mapper}. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Jul 2017</dd></dl>
 *
 */
public class ScannerAccessionsMapper<S, SI> 
  extends ScannerPairMapper<S, SI, ONDEXConcept, ConceptAccession> 
  implements AccessionsMapper<S>
{	
	public ScannerAccessionsMapper () 
	{
		this ( null, null );
	}
	
	
	@SuppressWarnings ( "unchecked" )
	public ScannerAccessionsMapper ( Scanner<S, SI> scanner, AccessionMapper<S> accessionMapper )
	{
		super ( scanner, (AccessionMapper<SI>) accessionMapper );
	}

	
	public AccessionMapper<SI> getAccessionMapper ()
	{
		return (AccessionMapper<SI>) super.getMapper ();
	}

	public void setAccessionMapper ( AccessionMapper<SI> accessionMapper )
	{
		super.setMapper ( (AccessionMapper<SI>) accessionMapper );
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
