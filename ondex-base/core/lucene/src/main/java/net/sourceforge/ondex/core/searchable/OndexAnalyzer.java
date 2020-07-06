package net.sourceforge.ondex.core.searchable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import static net.sourceforge.ondex.core.searchable.LuceneEnv.*;

/**
 * TODO: Remove, it's a test for Luke.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>2 Jun 2020</dd></dl>
 *
 */
public class OndexAnalyzer extends DelegatingAnalyzerWrapper
{
	private static Map<String,Analyzer> FIELD_ANALYZERS = new HashMap<> ();
	private static final Analyzer DEFAULT_ANALYZER = new StandardAnalyzer ();
	
	static {
		Stream.of ( 
			CONID_FIELD, PID_FIELD, CC_FIELD, DataSource_FIELD, 
			RELID_FIELD, FROM_FIELD, TO_FIELD, OFTYPE_FIELD, 
			"iri"
		).forEach ( f -> FIELD_ANALYZERS.put ( f, new KeywordAnalyzer () ) );
	}
	
	/**
	 * 
	 */
	public OndexAnalyzer ()
	{
		super( PER_FIELD_REUSE_STRATEGY );
	}

	@Override
	protected Analyzer getWrappedAnalyzer ( String fieldName )
	{
		return FIELD_ANALYZERS.getOrDefault ( fieldName, DEFAULT_ANALYZER );
	}
}
