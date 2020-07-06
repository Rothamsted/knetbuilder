package net.sourceforge.ondex.parser;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * Similarly to {@link ScannerMapper}, a scanner pair mapper takes the SI items returned by a scanner processing a 
 * data source S1 and maps them to ONDEX elements. Since this class is a {@link PairMapper}, it assumes that the 
 * second data source item SI2 passed to the {@link #map(Object, Object, ONDEXGraph)} method is a constant set by the 
 * invoker. For examples, @see {@link ScannerAccessionsMapper}. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public abstract class ScannerPairMapper<S1, SI, SI2, OI> implements StreamPairMapper<S1, SI2, OI>
{
	protected Scanner<S1, SI> scanner;
	protected PairMapper<SI, SI2, OI> mapper; 

	protected ScannerPairMapper ( Scanner<S1, SI> scanner, PairMapper<SI, SI2, OI> sourceItemMapper )
	{
		this.scanner = scanner;
		this.mapper = sourceItemMapper;
	}

	
	@Override
	public Stream<OI> map ( S1 src1, SI2 src2, ONDEXGraph graph )
	{
		//                          // This is Stream.map(), not our map(), which is the next one
		return scanner.scan ( src1 ).map ( si -> mapper.map ( si, src2, graph ) );
	}

	
	protected Scanner<S1, SI> getScanner ()
	{
		return scanner;
	}

	protected void setScanner ( Scanner<S1, SI> scanner )
	{
		this.scanner = scanner;
	}

	protected PairMapper<SI, SI2, OI> getMapper ()
	{
		return mapper;
	}

	protected void setMapper ( PairMapper<SI, SI2, OI> mapper )
	{
		this.mapper = mapper;
	}
}
