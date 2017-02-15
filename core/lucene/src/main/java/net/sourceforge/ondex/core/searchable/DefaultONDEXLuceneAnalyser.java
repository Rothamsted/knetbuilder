package net.sourceforge.ondex.core.searchable;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

/**
 * This analyser combines AlphanumericTokenizer and LowerCaseFilter, as well as
 * StopFilter for Attribute.
 * 
 * @author taubertj
 */
public final class DefaultONDEXLuceneAnalyser extends Analyzer implements
		ONDEXLuceneFields {

	private static final Set<?> stopSet = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

	public TokenStream tokenStream(String fieldName, Reader reader) {
		if (fieldName.startsWith(CONATTRIBUTE_FIELD + DELIM)
				|| fieldName.startsWith(RELATTRIBUTE_FIELD + DELIM)) {
			return new StopFilter(Version.LUCENE_36, new AlphanumericTokenizer(
					reader), stopSet);
		}
		return new LowerCaseFilter(Version.LUCENE_36,
				new AlphanumericTokenizer(reader));
	}

	/**
	 * Only letters and digits are accepted as tokens.
	 * 
	 * @author taubertj
	 */
	protected class AlphanumericTokenizer extends CharTokenizer {
		public AlphanumericTokenizer(Reader reader) {
			super(Version.LUCENE_36, reader);
		}

		protected boolean isTokenChar(int c) {
			return Character.isLetter(c) || Character.isDigit(c);
		}
	}

}
