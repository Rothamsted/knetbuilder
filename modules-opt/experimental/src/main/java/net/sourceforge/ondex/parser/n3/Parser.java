package net.sourceforge.ondex.parser.n3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.parser.ONDEXParser;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Parser extends ONDEXParser
{

	private final static String ccArg = "ConceptClassPredicate";

	private final static String ccDesc = "A pair of ConceptClass id and N3 predicate which should be assigned to this ConceptClass, a ConceptClass usually has more than one predicate.";

	private final static String rtArg = "RelationTypePredicate";

	private final static String rtDesc = "A pair of RelationType id and N3 predicate which should be treated as a RelationType, there can only be one predicate per RelationType.";

	private final static String nameArg = "ConceptNamePredicate";

	private final static String nameDesc = "Which predicates should be treated as ConceptName (with / without preferred flag) on a Concept, this can be multiple.";

	private final static String caArg = "ConceptAccessionPredicate";

	private final static String caDesc = "A pair of id of the DataSource of the ConceptAccession and the N3 predicate to be used as a ConceptAccession on a Concept.";

	private final static String anConceptArg = "ConceptAttributeNamePredicate";

	private final static String anConceptDesc = "A pair of id of the AttributeName of the Attribute and the SPAQRL query (concept id, Attribute value) to be used on Concepts.";

	private final static String anRelationArg = "RelationAttributeNamePredicate";

	private final static String anRelationDesc = "A pair of id of the AttributeName of the Attribute and the SPARQL query (fromConcept id, ofType URI, toConcept id, Attribute value) to be used on Relations.";

	private final static String cvArg = "DataSource";

	private final static String cvDesc = "The id of the DataSource (DataSource) created concepts should belong to.";

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		FileArgumentDefinition inFile = new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE,
				FileArgumentDefinition.INPUT_FILE_DESC, true, true, false,
				false);
		StringMappingPairArgumentDefinition cc = new StringMappingPairArgumentDefinition(
				ccArg, ccDesc, true, null, true);
		StringMappingPairArgumentDefinition rt = new StringMappingPairArgumentDefinition(
				rtArg, rtDesc, false, null, true);
		StringMappingPairArgumentDefinition name = new StringMappingPairArgumentDefinition(
				nameArg, nameDesc, false, null, true);
		StringMappingPairArgumentDefinition acc = new StringMappingPairArgumentDefinition(
				caArg, caDesc, false, null, true);
		StringMappingPairArgumentDefinition anC = new StringMappingPairArgumentDefinition(
				anConceptArg, anConceptDesc, false, null, true);
		StringMappingPairArgumentDefinition anR = new StringMappingPairArgumentDefinition(
				anRelationArg, anRelationDesc, false, null, true);
		StringArgumentDefinition cv = new StringArgumentDefinition(cvArg,
				cvDesc, false, "UC", false);
		return new ArgumentDefinition<?>[] { inFile, cc, rt, name, acc, anC,
				anR, cv };
	}

	@Override
	public String getId() {
		return "n3";
	}

	@Override
	public String getName() {
		return "Simple RDF N3 parser";
	}

	@Override
	public String getVersion() {
		return "24/03/2010";
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	// concepts indexed by their respective URI
	Map<String, ONDEXConcept> concepts = new HashMap<String, ONDEXConcept>();

	// relations indexed by their combined URI
	Map<String, ONDEXRelation> relations = new HashMap<String, ONDEXRelation>();

	@Override
	public void start() throws Exception {

		// get Evidence type from meta data
		EvidenceType evidence = graph.getMetaData().getEvidenceType("IMPD");
		if (evidence == null)
			throw new EvidenceTypeMissingException("IMPD");

		// get DataSource from meta data
		String cvId = (String) args.getUniqueValue(cvArg);
		DataSource dataSource = graph.getMetaData().getDataSource(cvId);
		if (dataSource == null)
			throw new DataSourceMissingException(cvId);

		// what predicates constitute a concept class
		Map<ConceptClass, Set<String>> ccPredicates = new HashMap<ConceptClass, Set<String>>();

		// parse list of expected concept classes
		for (Object o : args.getObjectValueArray(ccArg)) {
			String[] pair = o.toString().split(",");
			String ccId = pair[0];
			ConceptClass cc = graph.getMetaData().getConceptClass(ccId);
			if (cc == null)
				throw new ConceptClassMissingException(ccId);
			if (!ccPredicates.containsKey(cc))
				ccPredicates.put(cc, new HashSet<String>());
			ccPredicates.get(cc).add(pair[1]);
		}

		// which predicates should be used as predicates
		Map<RelationType, String> rtPredicate = new HashMap<RelationType, String>();

		// parse list of relation types from predicates
		for (Object o : args.getObjectValueArray(rtArg)) {
			String[] pair = o.toString().split(",");
			String rtId = pair[0];
			RelationType rt = graph.getMetaData().getRelationType(rtId);
			if (rt == null)
				throw new RelationTypeMissingException(rtId);
			rtPredicate.put(rt, pair[1]);
		}

		// which predicates to be used as concept names and preferred
		Map<String, Boolean> namePredicates = new HashMap<String, Boolean>();

		// list of predicates for concept names
		for (Object o : args.getObjectValueArray(nameArg)) {
			String[] pair = o.toString().split(",");
			String predicate = pair[1];
			namePredicates.put(predicate, Boolean.parseBoolean(pair[0]));
		}

		// paths leading to the value associated with an attribute name
		Map<AttributeName, String> anRelationPredicates = new HashMap<AttributeName, String>();

		// parse list of attribute names from predicates
		for (Object o : args.getObjectValueArray(anRelationArg)) {
			String[] pair = o.toString().split(",");
			String anId = pair[0];
			AttributeName an = graph.getMetaData().getAttributeName(anId);
			if (an == null)
				throw new AttributeNameMissingException(anId);
			anRelationPredicates.put(an, pair[1]);
		}

		// paths leading to the value associated with an attribute name
		Map<AttributeName, String> anConceptPredicates = new HashMap<AttributeName, String>();

		// parse list of attribute names from predicates
		for (Object o : args.getObjectValueArray(anConceptArg)) {
			String[] pair = o.toString().split(",");
			String anId = pair[0];
			AttributeName an = graph.getMetaData().getAttributeName(anId);
			if (an == null)
				throw new AttributeNameMissingException(anId);
			anConceptPredicates.put(an, pair[1]);
		}

		File file = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
		// open reader for current RDF
		BufferedReader reader = new BufferedReader(new FileReader(file));

		// create an empty model
		Model model = ModelFactory.createDefaultModel();
		RDFReader rdfreader = model.getReader("N3");
		rdfreader.read(model, reader, "");

		// first get skeleton concepts
		for (ConceptClass key : ccPredicates.keySet()) {
			parseConceptsPerConceptClass(model, key, ccPredicates.get(key), dataSource,
					evidence);
		}

		// second get skeleton relations
		for (RelationType key : rtPredicate.keySet()) {
			parseRelationsPerRelationType(model, key, rtPredicate.get(key),
					evidence);
		}

		// add concept names to concepts
		for (String key : namePredicates.keySet()) {
			parseConceptNames(model, key, namePredicates.get(key));
		}

		// add Attribute to concepts
		for (AttributeName an : anConceptPredicates.keySet()) {
			parseAttributeNamesConcepts(model, an, anConceptPredicates.get(an));
		}

		// add Attribute to relations
		for (AttributeName an : anRelationPredicates.keySet()) {
			parseAttributeNamesRelations(model, an, anRelationPredicates
					.get(an));
		}

	}

	/**
	 * Associates statements as Attribute to relations.
	 * 
	 * @param model
	 *            JENA model to search
	 * @param an
	 *            AttributeName to use
	 * @param path
	 *            Path describing how to reach Attribute value
	 */
	private void parseAttributeNamesRelations(Model model,
			AttributeName attrname, String path) {

		// Create a new query
		Query query = QueryFactory.create(path, Syntax.syntaxARQ);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Process query results
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			Iterator<String> varNames = qs.varNames();
			Resource fromResource = qs.getResource(varNames.next());
			String fromUri = fromResource.getURI();
			if (fromUri == null) {
				fromUri = fromResource.getId().toString();
				System.err.println("fromUri is null in query solution " + qs
						+ ". Using AnonId " + fromUri + " instead.");
			}
			Literal type = qs.getLiteral(varNames.next());
			Resource toResource = qs.getResource(varNames.next());
			String toUri = toResource.getURI();
			if (toUri == null) {
				toUri = toResource.getId().toString();
				System.err.println("toUri is null in query solution " + qs
						+ ". Using AnonId " + toUri + " instead.");
			}
			String combined = fromUri + "\t" + type.getString() + "\t" + toUri;
			ONDEXRelation relation = relations.get(combined);
			if (relation == null) {
				System.err.println("relation not found for key " + combined);
				continue;
			}
			Literal valueLiteral = qs.getLiteral(varNames.next());
			Object value = valueLiteral.getValue();
			if (attrname.getDataType().isAssignableFrom(value.getClass())) {
				relation.createAttribute(attrname, value, false);
			} else {
				System.err.println("Trying to put wrong literal "
						+ valueLiteral + " for AttributeName "
						+ attrname.getDataType() + " compared to value "
						+ value.getClass());
			}
		}

		// Important - free up resources used running the query
		qe.close();
	}

	/**
	 * Associates statements as Attribute to concepts.
	 * 
	 * @param model
	 *            JENA model to search
	 * @param attrname
	 *            AttributeName to use
	 * @param path
	 *            Path describing how to reach Attribute value
	 */
	private void parseAttributeNamesConcepts(Model model,
			AttributeName attrname, String path) {

		// Create a new query
		Query query = QueryFactory.create(path, Syntax.syntaxARQ);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Process query results
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			Iterator<String> varNames = qs.varNames();
			Resource conceptResource = qs.getResource(varNames.next());
			String uri = conceptResource.getURI();
			if (uri == null) {
				uri = conceptResource.getId().toString();
				System.err.println("concept uri is null in query solution "
						+ qs + ". Using AnonId " + uri + " instead.");
			}
			ONDEXConcept concept = concepts.get(uri);
			if (concept == null) {
				System.err.println("concept not found in query solution " + qs);
				continue;
			}
			Literal valueLiteral = qs.getLiteral(varNames.next());
			Object value = valueLiteral.getValue();
			if (attrname.getDataType().isAssignableFrom(value.getClass())) {
				concept.createAttribute(attrname, value, false);
			} else {
				System.err.println("Trying to put wrong literal "
						+ valueLiteral + " for AttributeName "
						+ attrname.getDataType() + " compared to value "
						+ value.getClass());
			}
		}

		// Important - free up resources used running the query
		qe.close();
	}

	/**
	 * Searches the model for predicates to be used as concept names.
	 * 
	 * @param model
	 *            JENA model to search
	 * @param uri
	 *            URI of the property constituting a concept name
	 * @param preferred
	 *            whether or not this concept name is preferred
	 */
	private void parseConceptNames(Model model, String uri, boolean preferred) {

		Property p = model.getProperty(uri);
		StmtIterator it = model.listStatements(null, p, (Resource) null);
		while (it.hasNext()) {
			Statement st = it.next();
			String fromUri = st.getSubject().getURI();
			if (fromUri == null) {
				fromUri = st.getSubject().getId().toString();
				System.err.println("fromUri is null in statement " + st
						+ ". Using AnonId " + fromUri + " instead.");
			}
			ONDEXConcept concept = concepts.get(fromUri);
			if (concept == null) {
				System.err.println("concept not found in statement " + st);
				continue;
			}
			// a concept name predicate should be a literal
			concept.createConceptName(st.getLiteral().getString(), preferred);
		}
	}

	/**
	 * Searches the model for statements with a predicate to be used as a
	 * relation type and constructs a relation.
	 * 
	 * @param model
	 *            JENA model to search
	 * @param ofType
	 *            RelationType for relations
	 * @param uri
	 *            URI of the property constituting a relation
	 * @param evidence
	 *            EvidenceType for relations
	 */
	private void parseRelationsPerRelationType(Model model,
			RelationType ofType, String uri, EvidenceType evidence) {

		Property p = model.getProperty(uri);
		StmtIterator it = model.listStatements(null, p, (Resource) null);
		while (it.hasNext()) {
			Statement st = it.next();
			String fromUri = st.getSubject().getURI();
			if (fromUri == null) {
				fromUri = st.getSubject().getId().toString();
				System.err.println("fromUri is null in statement " + st
						+ ". Using AnonId " + fromUri + " instead.");
			}
			String toUri = st.getResource().getURI();
			if (toUri == null) {
				toUri = st.getResource().getId().toString();
				System.err.println("toUri is null in statement " + st
						+ ". Using AnonId " + toUri + " instead.");
			}
			ONDEXConcept fromConcept = concepts.get(fromUri);
			if (fromConcept == null) {
				System.err.println("from concept not found in statement " + st);
				continue;
			}
			ONDEXConcept toConcept = concepts.get(toUri);
			if (toConcept == null) {
				System.err.println("to concept not found in statement " + st);
				continue;
			}
			ONDEXRelation r = graph.getFactory().createRelation(fromConcept,
					toConcept, ofType, evidence);
			relations.put(fromUri + "\t" + p.getURI() + "\t" + toUri, r);
		}
	}

	/**
	 * Searches the model for statements satisfying given predicate conditions
	 * and constructs an empty concept then.
	 * 
	 * @param model
	 *            JENA model to search
	 * @param ofType
	 *            ConceptClass for concepts
	 * @param predicates
	 *            predicates indicating ConceptClass
	 * @param elementOf
	 *            DataSource for concepts
	 * @param evidence
	 *            EvidenceType for concepts
	 */
	private void parseConceptsPerConceptClass(Model model, ConceptClass ofType,
			Set<String> predicates, DataSource elementOf, EvidenceType evidence) {

		Set<Resource> uniques = null;

		// first identify all resources which satisfy all predicates
		for (String uri : predicates) {
			Property p = model.getProperty(uri);
			if (uniques == null)
				uniques = model.listResourcesWithProperty(p).toSet();
			else
				uniques.retainAll(model.listResourcesWithProperty(p).toSet());
		}

		// create a concept for each resource
		for (Resource r : uniques) {
			String uri = r.getURI();
			if (uri != null) {
				ONDEXConcept c = graph.getFactory().createConcept(uri,
						elementOf, ofType, evidence);
				concepts.put(uri, c);
			} else {
				uri = r.getId().toString();
				System.err.println("No URI for Resource " + r
						+ ". Using AnonId " + uri + " instead.");
				ONDEXConcept c = graph.getFactory().createConcept(uri,
						elementOf, ofType, evidence);
				concepts.put(uri, c);
			}
		}
	}

}
