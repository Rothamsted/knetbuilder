package net.sourceforge.ondex.parser2;

import java.util.stream.Stream;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Jul 2017</dd></dl>
 *
 */
public abstract class ScannerMapper<S, SI, OI> implements StreamMapper<S, OI>
{
	protected Scanner<S, SI> scanner;
	protected Mapper<SI, OI> sourceItemMapper; 
	
	protected ScannerMapper ( Scanner<S, SI> scanner, Mapper<SI, OI> sourceItemMapper )
	{
		super ();
		this.scanner = scanner;
		this.sourceItemMapper = sourceItemMapper;
	}

	@Override
	public Stream<OI> map ( S source )
	{
		return scanner.scan ( source ).map ( sourceItemMapper::map );
	}
}
