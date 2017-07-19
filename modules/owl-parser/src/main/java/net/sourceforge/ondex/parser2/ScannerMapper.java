package net.sourceforge.ondex.parser2;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ONDEXGraph;

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
	protected Mapper<SI, OI> mapper; 
	
	protected ScannerMapper ( Scanner<S, SI> scanner, Mapper<SI, OI> sourceItemMapper )
	{
		super ();
		this.setScanner ( scanner );;
		this.mapper = sourceItemMapper;
	}

	@Override
	public Stream<OI> map ( S source, ONDEXGraph graph ) {
		return scanner.scan ( source ).map ( si -> mapper.map ( si, graph ) );
	}
	

	protected Scanner<S, SI> getScanner ()
	{
		return scanner;
	}

	protected void setScanner ( Scanner<S, SI> scanner )
	{
		this.scanner = scanner;
	}

	protected Mapper<SI, OI> getMapper ()
	{
		return mapper;
	}

	protected void setMapper ( Mapper<SI, OI> mapper )
	{
		this.mapper = mapper;
	}
}
