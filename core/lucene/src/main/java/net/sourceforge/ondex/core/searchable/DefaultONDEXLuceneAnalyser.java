//package net.sourceforge.ondex.core.searchable;
//
//import java.io.IOException;
//import java.io.Reader;
//import java.util.Set;
//
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.CharTokenizer;
//import org.apache.lucene.analysis.LowerCaseFilter;
//import org.apache.lucene.analysis.StopAnalyzer;
//import org.apache.lucene.analysis.StopFilter;
//import org.apache.lucene.analysis.StopwordAnalyzerBase;
//import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.Tokenizer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.analysis.standard.StandardTokenizer;
//import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//import org.apache.lucene.util.AttributeFactory;
//import org.apache.lucene.util.Version;
//
///**
// * This analyser combines AlphanumericTokenizer and LowerCaseFilter, as well as
// * StopFilter for Attribute.
// * 
// * @author taubertj
// */
//public final class DefaultONDEXLuceneAnalyser extends Analyzer implements
//		ONDEXLuceneFields {
//
//	private static final Set<?> stopSet = StandardAnalyzer.ENGLISH_STOP_WORDS_SET;
//
//	public TokenStream tokenStream(String fieldName, Reader reader) 
//	{
//		AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
//		StandardTokenizer tokenizer = new StandardTokenizer ( AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY );
//		tokenizer.addAttribute ( attClass )
//		
//		
//		if (fieldName.startsWith(CONATTRIBUTE_FIELD + DELIM)
//				|| fieldName.startsWith(RELATTRIBUTE_FIELD + DELIM)) {
//			return new StopFilter ( Version.LATEST, new AlphanumericTokenizer(
//					reader), stopSet);
//		}
//		return new LowerCaseFilter ( new AlphanumericTokenizer ( reader ) );
//	}
//
//	/**
//	 * Only letters and digits are accepted as tokens.
//	 * 
//	 * @author taubertj
//	 */
//	protected class AlphanumericTokenizer extends Tokenizer
//	{
//    protected String inputString;
//    protected int currentPosition = 0;
//    protected CharTermAttribute charTermAttribute = addAttribute ( CharTermAttribute.class );
//    
//
//		public AlphanumericTokenizer ()
//		{
//			super ();
//			inputString = null; // TODO: read the reader in setReader ()
//		}
//
//    @Override
//    public boolean incrementToken() throws IOException
//    {
//      this.charTermAttribute.setEmpty();
//    	if ( this.inputString == null || this.inputString.length () == 0 ) return false;
//
//    	for ( int i = this.currentPosition; i < this.inputString.; i)
//    }
//		
//		
//		protected boolean isTokenChar(int c) {
//			return Character.isLetter(c) || Character.isDigit(c);
//		}
//	}
//
//}
