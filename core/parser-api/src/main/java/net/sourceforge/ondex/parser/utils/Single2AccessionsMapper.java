package net.sourceforge.ondex.parser.utils;

import net.sourceforge.ondex.parser.AccessionMapper;
import net.sourceforge.ondex.parser.AccessionsMapper;
import net.sourceforge.ondex.parser.ScannerAccessionsMapper;

/**
 * An adapter from {@link AccessionMapper} to {@link AccessionsMapper}, passing a source straight to the accession
 * mapper, by means of an {@link IdentityScanner}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 May 2018</dd></dl>
 *
 */
public class Single2AccessionsMapper<S> extends ScannerAccessionsMapper<S, S> 
{
	public Single2AccessionsMapper ()
	{
		this ( null );
	}

	public Single2AccessionsMapper ( AccessionMapper<S> accessionMapper )
	{
		super ( new IdentityScanner<> (), accessionMapper );
	}
}
