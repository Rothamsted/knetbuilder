package net.sourceforge.ondex.core.searchable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

/**
 * Static methods for building Lucene queries on an ONDEX graph
 * 
 * @author hindlem
 */
public class LuceneQueryBuilder implements ONDEXLuceneFields {

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
	public static Query searchConceptByAnnotation(String query,
			Analyzer analyzer) throws ParseException {
		QueryParser qp = new QueryParser(Version.LUCENE_36, ANNO_FIELD,
				analyzer);
		return qp.parse(query);
	}

	/**
	 * Searches a concept by annotation.
	 * 
	 * @param term
	 *            Term to search in annotation
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByAnnotationExact(String term) {
		PhraseQuery phraseQuery = new PhraseQuery();
		for (String element : LuceneEnv.stripText(term).split(SPACE)) {
			phraseQuery.add(new Term(ANNO_FIELD, element));
		}
		return phraseQuery;
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
	public static Query searchConceptByDescription(String query,
			Analyzer analyzer) throws ParseException {
		QueryParser qp = new QueryParser(Version.LUCENE_36, DESC_FIELD,
				analyzer);
		return qp.parse(query);
	}

	/**
	 * Searches a concept by description.
	 * 
	 * @param term
	 *            Term to search in description
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByDescriptionExact(String term) {
		PhraseQuery phraseQuery = new PhraseQuery();
		for (String element : LuceneEnv.stripText(term).split(SPACE)) {
			phraseQuery.add(new Term(DESC_FIELD, element));
		}
		return phraseQuery;
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
	public static Query searchConceptByConceptName(String query,
			Analyzer analyzer) throws ParseException {
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
	public static Query searchConceptByConceptName(String query,
			DataSource notDataSource, Analyzer analyzer) throws ParseException {
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
	public static Query searchConceptByConceptName(String query,
			ConceptClass cc, Analyzer analyzer) throws ParseException {
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
	public static Query searchConceptByConceptName(String query,
			DataSource notDataSource, ConceptClass cc, Analyzer analyzer)
			throws ParseException {
		QueryParser qp = new QueryParser(Version.LUCENE_36, CONNAME_FIELD,
				analyzer);
		BooleanQuery booleanQuery = new BooleanQuery();

		if (notDataSource != null) {
			String cvID = notDataSource.getId();
			booleanQuery.add(new TermQuery(new Term(DataSource_FIELD, cvID)),
					BooleanClause.Occur.MUST_NOT); // query for not DataSource
		}

		if (cc != null) {
			String ccID = cc.getId();
			booleanQuery.add(new TermQuery(new Term(CC_FIELD, ccID)),
					BooleanClause.Occur.MUST); // query for CC
		}

		// parsed query
		booleanQuery.add(qp.parse(query), BooleanClause.Occur.MUST);
		return booleanQuery;
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
	public static Query searchConceptByConceptNameExact(String term,
			DataSource notDataSource, ConceptClass cc) {
		BooleanQuery booleanQuery = new BooleanQuery();

		if (notDataSource != null) {
			String cvID = notDataSource.getId();
			booleanQuery.add(new TermQuery(new Term(DataSource_FIELD, cvID)),
					BooleanClause.Occur.MUST_NOT); // query for not DataSource
		}

		if (cc != null) {
			String ccID = cc.getId();
			booleanQuery.add(new TermQuery(new Term(CC_FIELD, ccID)),
					BooleanClause.Occur.MUST); // query for CC
		}

		PhraseQuery phraseQuery = new PhraseQuery();
		for (String element : LuceneEnv.stripText(term).split(SPACE)) {
			phraseQuery.add(new Term(CONNAME_FIELD, element));
		}
		booleanQuery.add(phraseQuery, BooleanClause.Occur.MUST);
		return booleanQuery;
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
	 * @param minimumSimilarity
	 *            a value between 0 and 1 to set the required similarity between
	 *            the query term and the matching terms
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptNameFuzzy(String term,
			DataSource notDataSource, ConceptClass cc, float minimumSimilarity) {
		BooleanQuery booleanQuery = new BooleanQuery();

		if (notDataSource != null) {
			String cvID = notDataSource.getId();
			booleanQuery.add(new TermQuery(new Term(DataSource_FIELD, cvID)),
					BooleanClause.Occur.MUST_NOT); // query for not DataSource
		}

		if (cc != null) {
			String ccID = cc.getId();
			booleanQuery.add(new TermQuery(new Term(CC_FIELD, ccID)),
					BooleanClause.Occur.MUST); // query for CC
		}

		String[] split = LuceneEnv.stripText(term).split(" ");
		for (String word : split) {
			FuzzyQuery fuzzyQuerry = new FuzzyQuery(new Term(CONNAME_FIELD,
					word), minimumSimilarity);
			booleanQuery.add(fuzzyQuerry, BooleanClause.Occur.MUST);
		}
		return booleanQuery;
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
	public static Query searchConceptByConceptAccession(String query,
			boolean ambiguous, Set<String> listOfConceptAccDataSources,
			Analyzer analyzer) throws ParseException {
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.setMinimumNumberShouldMatch(1);

		Iterator<String> it_cvs = listOfConceptAccDataSources.iterator();
		while (it_cvs.hasNext()) {
			String cv = it_cvs.next();
			QueryParser qp = new QueryParser(Version.LUCENE_36, CONACC_FIELD
					+ DELIM + cv, analyzer);
			booleanQuery.add(qp.parse(query.toLowerCase()),
					BooleanClause.Occur.SHOULD);

			if (ambiguous) {
				QueryParser qpamb = new QueryParser(Version.LUCENE_36,
						CONACC_FIELD + DELIM + cv + DELIM + AMBIGUOUS, analyzer);
				booleanQuery.add(qpamb.parse(query.toLowerCase()),
						BooleanClause.Occur.SHOULD);
			}

		}
		return booleanQuery;
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
	public static Query searchConceptByConceptAccessionExact(String term,
			boolean ambiguous, Set<String> listOfConceptAccDataSources) {
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.setMinimumNumberShouldMatch(1);

		Iterator<String> it_cvs = listOfConceptAccDataSources.iterator();
		while (it_cvs.hasNext()) {
			String cv = it_cvs.next();

			Term t = new Term(CONACC_FIELD + DELIM + cv,
					LuceneEnv.stripText(term));
			TermQuery termQuery = new TermQuery(t);

			booleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);

			if (ambiguous) {
				t = new Term(CONACC_FIELD + DELIM + cv + DELIM + AMBIGUOUS,
						LuceneEnv.stripText(term));
				termQuery = new TermQuery(t);
				booleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);
			}
		}
		return booleanQuery;
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
	public static Query searchConceptByConceptAccessionExact(
			DataSource dataSource, String term, boolean ambiguous) {
		return searchConceptByConceptAccessionExact(dataSource, term, null,
				null, ambiguous);
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
	public static Query searchConceptByConceptAccessionExact(
			DataSource dataSource, String term, DataSource notDataSource,
			boolean ambiguous) {
		return searchConceptByConceptAccessionExact(dataSource, term,
				notDataSource, null, ambiguous);
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
	public static Query searchConceptByConceptAccessionExact(
			DataSource dataSource, String term, ConceptClass cc,
			boolean ambiguous) {
		return searchConceptByConceptAccessionExact(dataSource, term, null, cc,
				ambiguous);
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
	public static Query searchConceptByConceptAccessionExact(
			DataSource dataSource, String term, DataSource notDataSource,
			ConceptClass cc, boolean ambiguous) {
		return searchConceptByConceptAccessionExact(dataSource,
				new String[] { term }, notDataSource, cc, ambiguous);
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
	public static Query searchConceptByConceptAccessionExact(
			DataSource dataSource, String[] terms, DataSource notDataSource,
			ConceptClass cc, boolean ambiguous) {

		BooleanQuery booleanQuery = new BooleanQuery(true);
		booleanQuery.setMinimumNumberShouldMatch(1);

		if (notDataSource != null) {
			String notcvID = notDataSource.getId();
			booleanQuery.add(
					new TermQuery(new Term(DataSource_FIELD, notcvID)),
					BooleanClause.Occur.MUST_NOT); // query for DataSource
		}

		if (cc != null) {
			String ccID = cc.getId();
			booleanQuery.add(new TermQuery(new Term(CC_FIELD, ccID)),
					BooleanClause.Occur.MUST); // query for CC
		}

		for (String term : terms) {
			TermQuery termQuery = new TermQuery(new Term(CONACC_FIELD + DELIM
					+ dataSource.getId(), LuceneEnv.stripText(term)));
			booleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);

			if (ambiguous) {
				termQuery = new TermQuery(new Term(CONACC_FIELD + DELIM
						+ dataSource.getId() + DELIM + AMBIGUOUS,
						LuceneEnv.stripText(term)));

				booleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);
			}
		}

		return booleanQuery;
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
	public static Query searchConceptByConceptAttribute(String query,
			Set<String> listOfConceptAttrNames, Analyzer analyzer)
			throws ParseException {
		BooleanQuery booleanQuery = new BooleanQuery();
		Iterator<String> it_attrnames = listOfConceptAttrNames.iterator();
		while (it_attrnames.hasNext()) {
			String attrname = it_attrnames.next();
			QueryParser qp = new QueryParser(Version.LUCENE_36,
					CONATTRIBUTE_FIELD + DELIM + attrname, analyzer);
			booleanQuery.add(qp.parse(query), BooleanClause.Occur.SHOULD);
		}
		return booleanQuery;
	}

	// boost certain attribute names in Attribute search
	public static final Map<String, Float> DEFAULT_ATTR_BOOSTS = new HashMap<String, Float>();

	static {
		// boost hits found in headers
		DEFAULT_ATTR_BOOSTS.put("ConceptAttribute_AbstractHeader", 2f);
		DEFAULT_ATTR_BOOSTS.put("ConceptAttribute_Abstract", 1f);
		DEFAULT_ATTR_BOOSTS.put("ConceptAttribute_FullText", 1f);
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
	 */
	public static Query searchConceptByConceptAttribute(AttributeName[] an,
			String query, Analyzer analyzer, Map<String, Float> boosts) {

		String[] fields = new String[an.length];
		for (int i = 0; i < an.length; i++) {
			fields[i] = CONATTRIBUTE_FIELD + DELIM + an[i].getId();
		}
		MultiFieldQueryParser multiFieldParser = new MultiFieldQueryParser(
				Version.LUCENE_36, fields, analyzer, boosts);

		Query multiFieldQuery = null;
		try {
			multiFieldQuery = multiFieldParser.parse(query);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return multiFieldQuery;
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
	public static Query searchConceptByConceptAttribute(AttributeName an,
			String query, Analyzer analyzer) throws ParseException {
		String fieldname = CONATTRIBUTE_FIELD + DELIM + an.getId();
		QueryParser qp = new QueryParser(Version.LUCENE_36, fieldname, analyzer);
		return qp.parse(query);
	}

	/**
	 * Searches a concept by concept Attribute.
	 * 
	 * @param term
	 *            Term to search in concept Attribute
	 * @param listOfConceptAttrNames
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchConceptByConceptAttributeExact(String term,
			Set<String> listOfConceptAttrNames) {
		BooleanQuery booleanQuery = new BooleanQuery();
		Iterator<String> it_attrnames = listOfConceptAttrNames.iterator();
		while (it_attrnames.hasNext()) {
			String attrname = it_attrnames.next();
			PhraseQuery phraseQuery = new PhraseQuery();
			String[] split = LuceneEnv.stripText(term).split(" ");
			for (String subTerm : split) {
				phraseQuery.add(new Term(CONATTRIBUTE_FIELD + DELIM + attrname,
						subTerm.trim()));
			}
			booleanQuery.add(phraseQuery, BooleanClause.Occur.SHOULD);
		}
		return booleanQuery;
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
	public static Query searchConceptByConceptAttributeExact(AttributeName an,
			String term) {

		PhraseQuery phraseQuery = new PhraseQuery();
		String fieldname = CONATTRIBUTE_FIELD + DELIM + an.getId();
		String[] split = LuceneEnv.stripText(term).split(" ");
		for (String subterm : split) {
			phraseQuery.add(new Term(fieldname, subterm.trim()));
		}
		return phraseQuery;

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
	 */
	public static Query searchConceptByConceptAttributeFuzzy(AttributeName an,
			String term, float minimumSimilarity) {

		BooleanQuery booleanQuery = new BooleanQuery();
		String fieldname = CONATTRIBUTE_FIELD + DELIM + an.getId();
		String[] split = LuceneEnv.stripText(term).split(" ");
		for (String subterm : split) {
			FuzzyQuery fuzzyQuerry = new FuzzyQuery(new Term(fieldname,
					subterm.trim()), minimumSimilarity);
			booleanQuery.add(fuzzyQuerry, BooleanClause.Occur.MUST);
		}
		return booleanQuery;
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
	public static Query searchRelationByRelationAttribute(String query,
			Set<String> listOfRelationAttrNames, Analyzer analyzer)
			throws ParseException {
		BooleanQuery booleanQuery = new BooleanQuery();
		Iterator<String> it_attrnames = listOfRelationAttrNames.iterator();
		while (it_attrnames.hasNext()) {
			String attrname = it_attrnames.next();
			QueryParser qp = new QueryParser(Version.LUCENE_36,
					RELATTRIBUTE_FIELD + DELIM + attrname, analyzer);
			booleanQuery.add(qp.parse(query), BooleanClause.Occur.SHOULD);
		}
		return booleanQuery;
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
	public static Query searchRelationByRelationAttribute(AttributeName an,
			String query, Analyzer analyzer) throws ParseException {
		String fieldname = RELATTRIBUTE_FIELD + DELIM + an.getId();
		QueryParser qp = new QueryParser(Version.LUCENE_36, fieldname, analyzer);
		return qp.parse(query);
	}

	/**
	 * Searches a relation by relation Attribute.
	 * 
	 * @param term
	 *            Term to search in Relation Attribute
	 * @return Query a query object to be submitted to the LuceneEnv
	 */
	public static Query searchRelationByRelationAttributeExact(String term,
			Set<String> listOfRelationAttrNames) {
		BooleanQuery booleanQuery = new BooleanQuery();
		Iterator<String> it_attrnames = listOfRelationAttrNames.iterator();
		while (it_attrnames.hasNext()) {
			String attrname = it_attrnames.next();
			PhraseQuery phraseQuery = new PhraseQuery();
			String[] split = LuceneEnv.stripText(term).split(" ");
			for (String subterm : split) {
				phraseQuery.add(new Term(RELATTRIBUTE_FIELD + DELIM + attrname,
						subterm.trim()));
			}
			booleanQuery.add(phraseQuery, BooleanClause.Occur.SHOULD);
		}
		return booleanQuery;
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
	public static Query searchRelationByRelationAttributeExact(
			AttributeName an, String term) {

		PhraseQuery phraseQuery = new PhraseQuery();
		String fieldname = RELATTRIBUTE_FIELD + DELIM + an.getId();
		String[] split = LuceneEnv.stripText(term).split(" ");
		for (String subterm : split) {
			phraseQuery.add(new Term(fieldname, subterm.trim()));
		}
		return phraseQuery;

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
	 */
	public static Query searchRelationByRelationAttributeFuzzy(
			AttributeName an, String term, float minimumSimilarity) {

		BooleanQuery booleanQuery = new BooleanQuery();
		String fieldname = RELATTRIBUTE_FIELD + DELIM + an.getId();
		String[] split = LuceneEnv.stripText(term).split(" ");
		for (String subterm : split) {
			FuzzyQuery fuzzyQuerry = new FuzzyQuery(new Term(fieldname,
					subterm.trim()), minimumSimilarity);
			booleanQuery.add(fuzzyQuerry, BooleanClause.Occur.MUST);
		}
		return booleanQuery;
	}

}
