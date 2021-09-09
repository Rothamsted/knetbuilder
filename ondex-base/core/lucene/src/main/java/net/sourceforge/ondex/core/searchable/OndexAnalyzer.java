package net.sourceforge.ondex.core.searchable;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import static net.sourceforge.ondex.core.searchable.LuceneEnv.*;

/**
 * The default analyser set the {@link KeywordAnalyzer} for fields where we need exact searches
 * and then uses the {@link StandardAnalyzer} for all the rest.
 * 
 * This is similar to {@link PerFieldAnalyzerWrapper}, except we need to discriminate 
 * all fields matching criteria like {@link #RAW} postfix.
 * 
 * This kind of analyzer differentiation is necessary to ensure exact searches where necessary. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>2 Jun 2020</dd></dl>
 *
 */
public class OndexAnalyzer extends DelegatingAnalyzerWrapper
{
	private static final Map<String, Analyzer> FIELD_ANALYZERS;
	private static final Analyzer DEFAULT_ANALYZER = new StandardAnalyzer ();
	private static final Analyzer KEYWORD_ANALYZER = new KeywordAnalyzer ();
	
	static
	{
		FIELD_ANALYZERS = Stream.of 
		( 
			CONID_FIELD, PID_FIELD, CC_FIELD, DataSource_FIELD, 
			RELID_FIELD, FROM_FIELD, TO_FIELD, OFTYPE_FIELD,
			"iri"
		)
		.collect ( Collectors.toUnmodifiableMap ( 
			Function.identity (), f -> KEYWORD_ANALYZER 
		));
	}
	
	public OndexAnalyzer ()
	{
		super( PER_FIELD_REUSE_STRATEGY );
	}

	@Override
	protected Analyzer getWrappedAnalyzer ( String fieldName )
	{
		if ( fieldName == null ) throw new IllegalArgumentException (
			"Preventing Ondex from using null field names"
		);
		if ( fieldName.endsWith ( DELIM + RAW ) ) return KEYWORD_ANALYZER;
		return FIELD_ANALYZERS.getOrDefault ( fieldName, DEFAULT_ANALYZER );
	}	
}
