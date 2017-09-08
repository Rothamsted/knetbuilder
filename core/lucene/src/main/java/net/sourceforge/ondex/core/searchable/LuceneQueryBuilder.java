package net.sourceforge.ondex.core.searchable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.machinezoo.noexception.Exceptions;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;

/**
 * Static methods for building Lucene queries on an ONDEX graph
 * 
 * @author hindlem
 */
public class LuceneQueryBuilder implements ONDEXLuceneFields {

	/** boost certain attribute names in Attribute search */
	public static final Map<String, Float> DEFAULT_ATTR_BOOSTS = new HashMap<String, Float>();

	static {
		// boost hits found in headers
		DEFAULT_ATTR_BOOSTS.put("ConceptAttribute_AbstractHeader", 2f);
		DEFAULT_ATTR_BOOSTS.put("ConceptAttribute_Abstract", 1f);
		DEFAULT_ATTR_BOOSTS.put("ConceptAttribute_FullText", 1f);
	}

	private static Query search ( String field, String value, Analyzer analyzer ) throws ParseException
	{
		QueryParser qp = new QueryParser( field, analyzer );
		return qp.parse ( value );
	}
	
	private static PhraseQuery searchByWords ( String field, String words )
	{
		return searchByWords ( field, words, false );
	}

	/**
	 * Split the term and pass its chunks to a {@link PhraseQuery}.
	 */
	private static PhraseQuery searchByWords ( String field, String words, boolean singleTermTrim ) 
	{
		PhraseQuery.Builder qb = new PhraseQuery.Builder ();
		for ( String element : LuceneEnv.stripText ( words ).split ( SPACE ) ) {
			if ( singleTermTrim ) element = StringUtils.trimToNull ( element );
			if ( element == null ) continue; // TODO: warning?
			qb.add ( new Term ( field, element ) );
		}
		return qb.build ();
	}
	
	private static FuzzyQuery searchFuzzy ( String field, String term, int editDistance )
	{
		return new FuzzyQuery ( new Term ( field, term ), editDistance );
	}

	/**
	 * Lucene doesn't have similarity anymore, so we make a reasonable convertion to edit distance, consisting in
	 * distance = 2 if similarity < 0.667, 1 if higher. 
	 */
	private static FuzzyQuery searchFuzzy ( String field, String term, float similarity )
	{
		return searchFuzzy ( field, term, similarity < 0.667 ? 2 : 1 );
	}

	
	/**
	 * Searches a concept by annotation.
	 * 
	 * @param query
	 *            Query for annotation
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchConceptByAnnotation(String query, Analyzer analyzer) throws ParseException 
	{
		return search ( ANNO_FIELD, query, analyzer );
	}

	/**
	 * Searches a concept by annotation.
	 * 
	 * @param term
	 *            Term to search in annotation
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByAnnotationExact(String term) 
	{
		return searchByWords ( ANNO_FIELD, term );
	}

	/**
	 * Searches a concept by description.
	 * 
	 * @param query
	 *            Query for description
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchConceptByDescription(String query, Analyzer analyzer) throws ParseException 
	{
		return search ( DESC_FIELD, query, analyzer );
	}

	/**
	 * Searches a concept by description.
	 * 
	 * @param term
	 *            Term to search in description
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByDescriptionExact(String term) {
		return searchByWords ( DESC_FIELD, term );
	}

	/**
	 * Searches a concept by concept names.
	 * 
	 * @param query
	 *            Query for concept names
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchConceptByConceptName(String query, Analyzer analyzer) throws ParseException {
		return searchConceptByConceptName(query, null, null, analyzer);
	}

	/**
	 * Searches a concept by concept names and not DataSource.
	 * 
	 * @param query
	 *            Query for concept names
	 * @param notDataSource
	 *            not DataSource of AbstractConcept
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchConceptByConceptName(
			String query, DataSource notDataSource, Analyzer analyzer) throws ParseException {
		return searchConceptByConceptName(query, notDataSource, null, analyzer);
	}

	/**
	 * Searches a concept by concept names and CC.
	 * 
	 * @param query
	 *            Query for concept names
	 * @param cc
	 *            ConceptClass of AbstractConcept
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Set<AbstractConcepts>
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchConceptByConceptName(String query, ConceptClass cc, Analyzer analyzer) throws ParseException {
		return searchConceptByConceptName(query, null, cc, analyzer);
	}

	/**
	 * Searches a concept by concept names, not DataSource and ConceptClass.
	 * 
	 * @param query
	 *            Query for concept names
	 * @param notDataSource
	 *            not DataSource of AbstractConcept
	 * @param cc
	 *            ConceptClass of AbstractConcept
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchConceptByConceptName(String query, DataSource notDataSource, ConceptClass cc, Analyzer analyzer) throws ParseException 
	{
		QueryParser qp = new QueryParser( CONNAME_FIELD, analyzer);
		BooleanQuery.Builder qb = new BooleanQuery.Builder ();

		if (notDataSource != null) {
			String cvID = notDataSource.getId();
			qb.add(new TermQuery(new Term(DataSource_FIELD, cvID)), BooleanClause.Occur.MUST_NOT); // query for not DataSource
		}

		if (cc != null) {
			String ccID = cc.getId();
			qb.add(new TermQuery(new Term(CC_FIELD, ccID)), BooleanClause.Occur.MUST); // query for CC
		}

		// parsed query
		qb.add(qp.parse(query), BooleanClause.Occur.MUST);
		return qb.build ();
	}

	/**
	 * Searches a concept by concept names.
	 * 
	 * @param term
	 *            Term to search in concept names
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptNameExact(String term) {
		return searchConceptByConceptNameExact(term, null, null);
	}

	/**
	 * Searches a concept by concept names and not DataSource.
	 * 
	 * @param term
	 *            Term to search in concept names
	 * @param notDataSource
	 *            not DataSource of AbstractConcept
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptNameExact(String term,
			DataSource notDataSource) {
		return searchConceptByConceptNameExact(term, notDataSource, null);
	}

	/**
	 * Searches a concept by concept names and ConceptClass.
	 * 
	 * @param term
	 *            Term to search in concept names
	 * @param cc
	 *            ConceptClass of AbstractConcept
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptNameExact(String term,
			ConceptClass cc) {
		return searchConceptByConceptNameExact(term, null, cc);
	}

	/**
	 * Searches a concept by concept names, not DataSource and ConceptClass.
	 * 
	 * @param term
	 *            Term to search in concept names
	 * @param notDataSource
	 *            not DataSource of AbstractConcept
	 * @param cc
	 *            ConceptClass of AbstractConcept
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptNameExact(String term, DataSource notDataSource, ConceptClass cc)
	{
		BooleanQuery.Builder boolQb = new BooleanQuery.Builder ();

		if (notDataSource != null) {
			String cvID = notDataSource.getId();
			boolQb.add(new TermQuery(new Term(DataSource_FIELD, cvID)), BooleanClause.Occur.MUST_NOT); // query for not DataSource
		}

		if (cc != null) {
			String ccID = cc.getId();
			boolQb.add(new TermQuery(new Term(CC_FIELD, ccID)), BooleanClause.Occur.MUST); // query for CC
		}

		PhraseQuery.Builder phraseQb = new PhraseQuery.Builder ();
		for (String element : LuceneEnv.stripText(term).split(SPACE)) {
			phraseQb.add(new Term(CONNAME_FIELD, element));
		}
		boolQb.add(phraseQb.build (), BooleanClause.Occur.MUST);
		return boolQb.build ();
	}

	/**
	 * Searches a concept by concept names.
	 * <p/>
	 * For example, for a minimumSimilarity of 0.5 a term of the same length as
	 * the query term is considered similar to the query term if the edit
	 * distance between both terms is less than length(term)*0.5.
	 * 
	 * @param term
	 *            Term to search in concept names
	 * @param minimumSimilarity
	 *            a value between 0 and 1 to set the required similarity between
	 *            the query term and the matching terms
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptNameFuzzy(String term,
			float minimumSimilarity) {
		return searchConceptByConceptNameFuzzy(term, null, null,
				minimumSimilarity);
	}

	/**
	 * Searches a concept by concept names and not DataSource.
	 * <p/>
	 * For example, for a minimumSimilarity of 0.5 a term of the same length as
	 * the query term is considered similar to the query term if the edit
	 * distance between both terms is less than length(term)*0.5.
	 * 
	 * @param term
	 *            Term to search in concept names
	 * @param notDataSource
	 *            not DataSource of AbstractConcept
	 * @param minimumSimilarity
	 *            a value between 0 and 1 to set the required similarity between
	 *            the query term and the matching terms
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptNameFuzzy(String term,
			DataSource notDataSource, float minimumSimilarity) {
		return searchConceptByConceptNameFuzzy(term, notDataSource, null,
				minimumSimilarity);
	}

	/**
	 * Searches a concept by concept names and CC.
	 * <p/>
	 * For example, for a minimumSimilarity of 0.5 a term of the same length as
	 * the query term is considered similar to the query term if the edit
	 * distance between both terms is less than length(term)*0.5.
	 * 
	 * @param term
	 *            Term to search in concept names
	 * @param cc
	 *            ConceptClass of AbstractConcept
	 * @param minimumSimilarity
	 *            a value between 0 and 1 to set the required similarity between
	 *            the query term and the matching terms
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptNameFuzzy(String term,
			ConceptClass cc, float minimumSimilarity) {
		return searchConceptByConceptNameFuzzy(term, null, cc,
				minimumSimilarity);
	}

	/**
	 * Searches a concept by concept names, not DataSource and ConceptClass.
	 * 
	 * @param term
	 *            Term to search in concept names
	 * @param notDataSource
	 *            not DataSource of AbstractConcept
	 * @param cc
	 *            ConceptClass of AbstractConcept
	 * @param minimumSimilarity @see {@link #searchFuzzy(String, String, float)}
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptNameFuzzy ( String term, DataSource notDataSource, ConceptClass cc,
			float minimumSimilarity )
	{
		BooleanQuery.Builder boolQb = new BooleanQuery.Builder ();

		if ( notDataSource != null )
		{
			String cvID = notDataSource.getId ();
			// Exclude DS
			boolQb.add ( new TermQuery ( new Term ( DataSource_FIELD, cvID ) ), BooleanClause.Occur.MUST_NOT ); 
		}

		if ( cc != null )
		{
			String ccID = cc.getId ();
			boolQb.add ( new TermQuery ( new Term ( CC_FIELD, ccID ) ), BooleanClause.Occur.MUST ); // query for CC
		}

		String[] split = LuceneEnv.stripText ( term ).split ( " " );
		for ( String word : split )
		{
			FuzzyQuery qfuzzy = searchFuzzy ( CONNAME_FIELD, word, minimumSimilarity );
			boolQb.add ( qfuzzy, BooleanClause.Occur.MUST );
		}
		return boolQb.build ();
	}

	/**
	 * Searches a concept by concept accession.
	 * 
	 * @param query
	 *            Query for concept accession
	 * @param ambiguous
	 *            Include Ambiguous accessions in results
	 * @param listOfConceptAccDataSources
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchConceptByConceptAccession ( 
		String query, boolean ambiguous, Set<String> listOfConceptAccDataSources, Analyzer analyzer 
	) throws ParseException
	{
		BooleanQuery.Builder boolQb = new BooleanQuery.Builder ();
		boolQb.setMinimumNumberShouldMatch ( 1 );

		Iterator<String> it_cvs = listOfConceptAccDataSources.iterator ();
		while ( it_cvs.hasNext () )
		{
			String cv = it_cvs.next ();
			QueryParser qp = new QueryParser ( CONACC_FIELD + DELIM + cv, analyzer );
			boolQb.add ( qp.parse ( query.toLowerCase () ), BooleanClause.Occur.SHOULD );

			if ( ambiguous )
			{
				QueryParser qpamb = new QueryParser ( CONACC_FIELD + DELIM + cv + DELIM + AMBIGUOUS, analyzer );
				boolQb.add ( qpamb.parse ( query.toLowerCase () ), BooleanClause.Occur.SHOULD );
			}
		}
		return boolQb.build ();
	}

	/**
	 * Searches a concept by concept accession.
	 * 
	 * @param term
	 *            Term to search in concept accession
	 * @param ambiguous
	 *            Include Ambiguous accessions in results
	 * @param listOfConceptAccDataSources
	 *            cv fields
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptAccessionExact ( String term, boolean ambiguous,
			Set<String> listOfConceptAccDataSources )
	{
		BooleanQuery.Builder boolQb = new BooleanQuery.Builder ();
		boolQb.setMinimumNumberShouldMatch ( 1 );

		Iterator<String> it_cvs = listOfConceptAccDataSources.iterator ();
		while ( it_cvs.hasNext () )
		{
			String cv = it_cvs.next ();

			Term t = new Term ( CONACC_FIELD + DELIM + cv, LuceneEnv.stripText ( term ) );
			TermQuery termQuery = new TermQuery ( t );

			boolQb.add ( termQuery, BooleanClause.Occur.SHOULD );

			if ( ambiguous )
			{
				t = new Term ( CONACC_FIELD + DELIM + cv + DELIM + AMBIGUOUS, LuceneEnv.stripText ( term ) );
				termQuery = new TermQuery ( t );
				boolQb.add ( termQuery, BooleanClause.Occur.SHOULD );
			}
		}
		return boolQb.build ();
	}

	/**
	 * Searches a concept by concept accession and DataSource of accession.
	 * 
	 * @param dataSource
	 *            DataSource of concept accession
	 * @param term
	 *            Term to search in concept accession
	 * @param ambiguous
	 *            Include Ambiguous accessions in results
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptAccessionExact ( DataSource dataSource, String term, boolean ambiguous )
	{
		return searchConceptByConceptAccessionExact ( dataSource, term, null, null, ambiguous );
	}

	/**
	 * Searches a concept by concept accession and not DataSource.
	 * 
	 * @param dataSource
	 *            DataSource of concept accession
	 * @param term
	 *            Term to search in concept accession
	 * @param notDataSource
	 *            not DataSource of AbstractConcept
	 * @param ambiguous
	 *            Include Ambiguous accessions in results
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptAccessionExact ( DataSource dataSource, String term,
			DataSource notDataSource, boolean ambiguous )
	{
		return searchConceptByConceptAccessionExact ( dataSource, term, notDataSource, null, ambiguous );
	}

	/**
	 * Searches a concept by concept accession and ConceptClass.
	 * 
	 * @param dataSource
	 *            DataSource of concept accession
	 * @param term
	 *            Term to search in concept accession
	 * @param cc
	 *            ConceptClass of AbstractConcept
	 * @param ambiguous
	 *            Include Ambiguous accessions in results
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptAccessionExact ( DataSource dataSource, String term, ConceptClass cc,
			boolean ambiguous )
	{
		return searchConceptByConceptAccessionExact ( dataSource, term, null, cc, ambiguous );
	}

	/**
	 * Searches a concept by concept accession, not DataSource and ConceptClass.
	 * 
	 * @param dataSource
	 *            DataSource of concept accession
	 * @param term
	 *            Term to search in concept accession
	 * @param notDataSource
	 *            not DataSource of AbstractConcept
	 * @param cc
	 *            ConceptClass of AbstractConcept
	 * @param ambiguous
	 *            Include Ambiguous accessions in results
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptAccessionExact ( 
		DataSource dataSource, String term, DataSource notDataSource, ConceptClass cc, boolean ambiguous 
	)
	{
		return searchConceptByConceptAccessionExact ( dataSource, new String[] { term }, notDataSource, cc, ambiguous );
	}

	/**
	 * Searches a concept by concept accessions, not DataSource and
	 * ConceptClass.
	 * 
	 * @param dataSource
	 *            DataSource of concept accession
	 * @param terms
	 *            Terms to search in concept accession
	 * @param notDataSource
	 *            not DataSource of AbstractConcept
	 * @param cc
	 *            ConceptClass of AbstractConcept
	 * @param ambiguous
	 *            Include Ambiguous accessions in results
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptAccessionExact ( 
		DataSource dataSource, String[] terms, DataSource notDataSource, ConceptClass cc, boolean ambiguous 
	)
	{

		BooleanQuery.Builder boolQb = new BooleanQuery.Builder ()
		.setDisableCoord ( true )
		.setMinimumNumberShouldMatch ( 1 );

		if ( notDataSource != null )
		{
			String notcvID = notDataSource.getId ();
			// Data source exclusion
			boolQb.add ( new TermQuery ( new Term ( DataSource_FIELD, notcvID ) ), BooleanClause.Occur.MUST_NOT ); 
		}

		if ( cc != null )
		{
			String ccID = cc.getId ();
			boolQb.add ( new TermQuery ( new Term ( CC_FIELD, ccID ) ), BooleanClause.Occur.MUST ); // query for CC
		}

		for ( String term : terms )
		{
			TermQuery termQuery = new TermQuery (
				new Term ( CONACC_FIELD + DELIM + dataSource.getId (), LuceneEnv.stripText ( term ) ) 
			);
			boolQb.add ( termQuery, BooleanClause.Occur.SHOULD );

			if ( ambiguous )
			{
				termQuery = new TermQuery (
					new Term ( CONACC_FIELD + DELIM + dataSource.getId () + DELIM + AMBIGUOUS, LuceneEnv.stripText ( term ) ) 
				);

				boolQb.add ( termQuery, BooleanClause.Occur.SHOULD );
			}
		}

		return boolQb.build ();
	}


	private static Query searchByAttributes ( 
		String attrFieldPrefix, Set<String> attrNames, Function<String, Query> singleAttributeSearcher
	) 
	{
		BooleanQuery.Builder boolQb = new BooleanQuery.Builder ();
		for ( String attrname: attrNames )
		{
			Query qattr = singleAttributeSearcher.apply ( attrname ); 
			boolQb.add ( qattr, BooleanClause.Occur.SHOULD );
		}
		return boolQb.build ();
	}
	
	private static Query searchByAttributes ( 
		String attrFieldPrefix, AttributeName[] attrNames, Function<String, Query> singleAttributeSearcher
	) 
	{
		Set<String> nameStr = Stream.of ( attrNames )
		.map ( AttributeName::getId )
		.collect ( Collectors.toSet () );
		
		return searchByAttributes ( attrFieldPrefix, nameStr, singleAttributeSearcher ); 
	}
	
	
	
	private static Query searchByAttributes (
		String attrFieldPrefix, String value, Set<String> attrNames, Analyzer analyzer 
	) throws ParseException
	{
		return searchByAttributes ( 
		  attrFieldPrefix,
		  attrNames,
		  n -> Exceptions.sneak ().get ( () -> searchByAttribute ( attrFieldPrefix, n, value, analyzer ) ) 
		);
	}

	
	
	private static Query searchByAttributesMulti (
		String attrFieldPrefix, String term, String[] attrNames, Map<String, Float> boosts, Analyzer analyzer 
	) throws ParseException
	{
		for ( int i = 0; i < attrNames.length; i++ )
			attrNames [ i ] = attrFieldPrefix + DELIM + attrNames [ i ];
		return new MultiFieldQueryParser ( attrNames, analyzer, boosts ).parse ( term );
	}
	
	private static Query searchByAttributesMulti (
		String attrFieldPrefix, String term, AttributeName[] attrNames, Map<String, Float> boosts, Analyzer analyzer 
	) throws ParseException
	{
		String [] attrStr = new String [ attrNames.length ];
		for ( int i = 0; i < attrNames.length; i++ )
			attrStr [ i ] = attrNames [ i ].getId ();
		return searchByAttributesMulti ( attrFieldPrefix, term, attrStr, boosts, analyzer );
	}
	
	private static Query searchByAttributeWords ( String attrFieldPrefix, String value, Set<String> attrNames )
		throws ParseException
	{
		return searchByAttributeWords ( attrFieldPrefix, value, attrNames, false );
	}
	
	private static Query searchByAttributeWords ( String attrFieldPrefix, String value, Set<String> attrNames, boolean singleTermTrim ) 
		throws ParseException
	{
		return searchByAttributes ( 
		  attrFieldPrefix,
		  attrNames,
		  n -> Exceptions.sneak ().get ( () -> searchByAttributeWords ( attrFieldPrefix, n, value, singleTermTrim ) ) 
		);
	}

	
	
	
	
	private static Query searchByAttribute ( 
		String attrFieldPrefix, AttributeName an, String term, Analyzer analyzer
	) throws ParseException 
	{
		return searchByAttribute ( attrFieldPrefix, an.getId(), term, analyzer );
	}
	
	private static Query searchByAttribute ( 
		String attrFieldPrefix, String attrName, String term, Analyzer analyzer
	) throws ParseException 
	{
		return search ( attrFieldPrefix + DELIM + attrName, term, analyzer );
	}

	
	private static Query searchByAttributeWords ( String attrFieldPrefix, String attrName, String term )
		throws ParseException 	
	{
		return searchByAttributeWords ( attrFieldPrefix, attrName, term, false );
	}
	
	private static Query searchByAttributeWords ( String attrFieldPrefix, String attrName, String term, boolean singleTermTrim ) 
		throws ParseException 
	{
		return searchByWords ( attrFieldPrefix + DELIM + attrName, term, singleTermTrim );
	}


	private static Query searchByAttributeWords ( String attrFieldPrefix, AttributeName attrName, String term )
		throws ParseException 	
	{
		return searchByAttributeWords ( attrFieldPrefix, attrName, term, false );
	}
	
	private static Query searchByAttributeWords ( String attrFieldPrefix, AttributeName attrName, String term, boolean singleTermTrim ) 
		throws ParseException 
	{
		return searchByAttributeWords ( attrFieldPrefix, attrName.getId (), term, singleTermTrim );
	}
	
	
	
	
	private static Query searchByAttributeFuzzy ( String attrFieldPrefix, String attrName, String term, float similarity ) 
		throws ParseException 
	{
		String fieldName = attrFieldPrefix + DELIM + attrName;
		BooleanQuery.Builder boolQb = new BooleanQuery.Builder ();
		String[] split = LuceneEnv.stripText ( term ).split ( " " );
		
		for ( String subterm : split )
		{
			FuzzyQuery qfuzzy = searchFuzzy ( fieldName, subterm.trim (), similarity );
			boolQb.add ( qfuzzy, BooleanClause.Occur.MUST );
		}
		return boolQb.build ();
	}

	private static Query searchByAttributeFuzzy ( String attrFieldPrefix, AttributeName attrName, String term, float similarity ) 
		throws ParseException 
	{
		return searchByAttributeFuzzy ( attrFieldPrefix, attrName.getId (), term, similarity );
	}
	
	
	
	private static Query searchByAttributesFuzzy ( String attrFieldPrefix, AttributeName[] attrNames, String term, float similarity ) 
		throws ParseException 
	{
		return searchByAttributes (
			attrFieldPrefix, 
			attrNames, 
			n -> Exceptions.sneak ().get ( () -> searchByAttributeFuzzy ( attrFieldPrefix, n, term, similarity ) )
		);
	}
	
	
	/**
	 * Searches a concept by all of its Attribute.
	 * 
	 * @param query
	 *            Query for concept Attribute
	 * @param listOfConceptAttrNames
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchConceptByConceptAttribute ( 
		String query, Set<String> listOfConceptAttrNames, Analyzer analyzer 
	) throws ParseException
	{
		return searchByAttributes ( CONATTRIBUTE_FIELD, query, listOfConceptAttrNames, analyzer );
	}


	/**
	 * Searches multiple concept Attribute such as title and abstract for a
	 * given query.
	 * 
	 * @param an
	 *            AttributeNames for Attribute to search in
	 * @param query
	 *            Query to search in concept Attribute
	 * @param analyzer
	 *            used to find terms in the query text
	 * @param boosts
	 *            Allows passing of a map with term to Boost, and the boost to
	 *            apply to each term
	 * @return Query a query object to be submitted to the LuceneEnv
	 * 
	 */
	public static Query searchConceptByConceptAttributeMulti ( 
		AttributeName[] an, String query, Analyzer analyzer, Map<String, Float> boosts 
	) throws ParseException
	{
		return searchByAttributesMulti ( CONATTRIBUTE_FIELD, query, an, boosts, analyzer );
	}

	/**
	 * Searches a concept by concept Attribute.
	 * 
	 * @param an
	 *            AttributeName for Attribute
	 * @param query
	 *            Query for concept Attribute
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchConceptByConceptAttribute ( AttributeName an, String query, Analyzer analyzer) throws ParseException 
	{
		return searchByAttribute ( CONATTRIBUTE_FIELD, an, query, analyzer );
	}

	/**
	 * Searches a concept by concept Attribute.
	 * 
	 * @param term
	 *            Term to search in concept Attribute
	 * @param listOfConceptAttrNames
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException 
	 */
	public static Query searchConceptByConceptAttributeExact(String term, Set<String> listOfConceptAttrNames ) 
		throws ParseException 
	{
		return searchByAttributeWords ( CONATTRIBUTE_FIELD, term, listOfConceptAttrNames );
	}

	/**
	 * Searches a concept by concept Attribute and AttributeName.
	 * 
	 * @param an
	 *            AttributeName for Attribute
	 * @param term
	 *            Term to search in concept Attribute
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptAttributeExact ( AttributeName an, String term )
	{
		return searchConceptByConceptAttributeExact ( an.getId (), term );
	}

	public static Query searchConceptByConceptAttributeExact ( String attributeName, String term )
	{
		return searchByWords ( CONATTRIBUTE_FIELD + DELIM + attributeName, term, true );
	}

	/**
	 * Searches a concept by concept Attribute and AttributeName. A FuzzyQuery
	 * matches documents that contain terms similar to the specified term.
	 * <p/>
	 * For example, for a minimumSimilarity of 0.5 a term of the same length as
	 * the query term is considered similar to the query term if the edit
	 * distance between both terms is less than length(term)*0.5.
	 * 
	 * @param an
	 *            AttributeName for Attribute
	 * @param term
	 *            Term to search in concept Attribute
	 * @param minimumSimilarity
	 *            a value between 0 and 1 to set the required similarity between
	 *            the query term and the matching terms
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException 
	 */
	public static Query searchConceptByConceptAttributeFuzzy( AttributeName an, String term, float minimumSimilarity )
	  throws ParseException 
	{
		return searchByAttributeFuzzy ( CONATTRIBUTE_FIELD, an, term, minimumSimilarity );
	}

	/**
	 * Searches a relation by relation Attribute.
	 * 
	 * @param query
	 *            Query for Relation Attribute
	 * @param listOfRelationAttrNames
	 * @param analyzer
	 *            used to find terms in the query text used to find terms in the
	 *            query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchRelationByRelationAttribute
		( String query, Set<String> listOfRelationAttrNames, Analyzer analyzer ) throws ParseException
	{
		return searchByAttributes ( RELATTRIBUTE_FIELD, query, listOfRelationAttrNames, analyzer );
	}

	/**
	 * Searches a relation by relation Attribute.
	 * 
	 * @param an
	 *            AttributeName for Attribute
	 * @param query
	 *            Query for relation Attribute
	 * @param analyzer
	 *            used to find terms in the query text
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException
	 *             on an error in your query construction
	 */
	public static Query searchRelationByRelationAttribute ( AttributeName an, String query, Analyzer analyzer )
		throws ParseException
	{
		return searchByAttribute ( RELATTRIBUTE_FIELD, an, query, analyzer );
	}

	/**
	 * Searches a relation by relation Attribute.
	 * 
	 * @param term
	 *            Term to search in Relation Attribute
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException 
	 */
	public static Query searchRelationByRelationAttributeExact ( String term, Set<String> listOfRelationAttrNames ) 
		throws ParseException
	{
		return searchByAttributeWords ( RELATTRIBUTE_FIELD, term, listOfRelationAttrNames );
	}

	/**
	 * Searches a relation by relation Attribute and AttributeName.
	 * 
	 * @param an
	 *            AttributeName for Attribute
	 * @param term
	 *            Term to search in relation Attribute
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchRelationByRelationAttributeExact ( AttributeName an, String term )
		throws ParseException
	{
		return searchByAttributeWords ( RELATTRIBUTE_FIELD, an, term, true );
	}

	/**
	 * Searches a relation by relation Attribute and AttributeName. A FuzzyQuery
	 * matches documents that contain terms similar to the specified term.
	 * <p/>
	 * For example, for a minimumSimilarity of 0.5 a term of the same length as
	 * the query term is considered similar to the query term if the edit
	 * distance between both terms is less than length(term)*0.5.
	 * 
	 * @param an
	 *            AttributeName for Attribute
	 * @param term
	 *            Term to search in relation Attribute
	 * @param minimumSimilarity
	 *            a value between 0 and 1 to set the required similarity between
	 *            the query term and the matching terms
	 * @return Query a query object to be submitted to the LuceneEnv
	 * @throws ParseException 
	 */
	public static Query searchRelationByRelationAttributeFuzzy( AttributeName an, String term, float minimumSimilarity )
		throws ParseException	
	{
		return searchByAttributeFuzzy ( RELATTRIBUTE_FIELD, an, term, minimumSimilarity );
	}

}
