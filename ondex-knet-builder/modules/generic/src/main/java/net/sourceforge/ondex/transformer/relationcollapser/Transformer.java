package net.sourceforge.ondex.transformer.relationcollapser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.relationneighbours.DepthInsensitiveRTValidator;
import net.sourceforge.ondex.algorithm.relationneighbours.RelationNeighboursSearch;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * A equivalence collapser NB: If you do not specify a relation type it will
 * collapse all known relation types (this is an unusual thing to want to do)
 * When collapsing concepts with GDSs of the same AttributeName but different
 * Attribute values then
 * 
 * @author hindlem
 */
@Status(description = "Tested December 2013 (Artem Lysenko)", status = StatusType.STABLE)
@Authors(authors = { "Matthew Hindle" }, emails = { "matthew_hindle at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Transformer extends ONDEXTransformer implements ArgumentNames {

	Pattern commaPat = Pattern.compile(",");

	/**
	 * starts the ball rolling
	 */
	public void start() throws InvalidPluginArgumentException,
			InconsistencyException {

		// get arguments
		String[] validRelationTypes = (String[]) args
				.getObjectValueArray(RELATION_TYPE_ARG);

		String[] restrictConceptClasses = (String[]) args
				.getObjectValueArray(CONCEPTCLASS_RESTRICTION_ARG);

		String[] restrictDataSources = (String[]) args
				.getObjectValueArray(DATASOURCE_RESTRICTION_ARG);

		boolean cloneGDSs = (Boolean) args.getUniqueValue(CLONE_ATTRIBUTES_ARG);

		fireEventOccurred(new GeneralOutputEvent("Clone Attribute Concept? = "
				+ cloneGDSs, getCurrentMethodName()));

		boolean copyTagRefs = (Boolean) args
				.getUniqueValue(COPY_TAG_REFERENCES_ARG);

		// get data source for copying preferred names
		DataSource prefNamesDataSource = null;
		String prefNamesDataSourceId = (String) args
				.getUniqueValue(DATASOURCE_PREFNAMES_ARG);
		if (prefNamesDataSourceId.trim().length() > 0) {
			prefNamesDataSource = graph.getMetaData().getDataSource(
					prefNamesDataSourceId);
			if (prefNamesDataSource == null)
				fireEventOccurred(new WrongParameterEvent(prefNamesDataSourceId
						+ " is not a valid DataSource", getCurrentMethodName()));
		}

		fireEventOccurred(new GeneralOutputEvent(
				"Copy Tag References across? = " + copyTagRefs,
				getCurrentMethodName()));

		ClusterCollapser clc = new ClusterCollapser(cloneGDSs, copyTagRefs,
				prefNamesDataSource);

		RelationNeighboursSearch ns = new RelationNeighboursSearch(graph);

		DepthInsensitiveRTValidator validator = new DepthInsensitiveRTValidator();

		// add concept class restriction pairs
		if (restrictConceptClasses != null && restrictConceptClasses.length > 0) {
			for (String s : restrictConceptClasses) {
				String pair = s.trim();
				String[] values = commaPat.split(pair);

				if (values.length != 2) {
					fireEventOccurred(new WrongParameterEvent(
							"Invalid Format for ConceptClass pair " + pair,
							getCurrentMethodName()));
				}

				// retrieve concept classes from meta data
				ConceptClass fromConceptClass = graph.getMetaData()
						.getConceptClass(values[0]);
				ConceptClass toConceptClass = graph.getMetaData()
						.getConceptClass(values[1]);

				// check concept classes exist
				if (fromConceptClass != null && toConceptClass != null) {
					validator.addConceptClassPair(fromConceptClass,
							toConceptClass);
					validator.addConceptClassPair(toConceptClass,
							fromConceptClass);
					fireEventOccurred(new GeneralOutputEvent(
							"Added ConceptClass restriction for "
									+ fromConceptClass.getId() + " ==> "
									+ toConceptClass.getId(),
							getCurrentMethodName()));
				} else {
					if (fromConceptClass == null)
						fireEventOccurred(new WrongParameterEvent(values[0]
								+ " is not a valid ConceptClass (From)",
								getCurrentMethodName()));
					if (toConceptClass == null)
						fireEventOccurred(new WrongParameterEvent(values[1]
								+ " is not a valid ConceptClass (To)",
								getCurrentMethodName()));
				}
			}
		}

		// add data source restriction pairs
		if (restrictDataSources != null && restrictDataSources.length > 0) {
			for (String s : restrictDataSources) {
				String pair = s.trim();
				String[] values = commaPat.split(pair);

				if (values.length != 2) {
					fireEventOccurred(new WrongParameterEvent(
							"Invalid Format for DataSource pair " + pair,
							getCurrentMethodName()));
				}

				// retrieve data sources from meta data
				DataSource fromDataSource = graph.getMetaData().getDataSource(
						values[0]);
				DataSource toDataSource = graph.getMetaData().getDataSource(
						values[1]);

				// check data sources exist
				if (fromDataSource != null && toDataSource != null) {
					validator.addDataSourcePair(fromDataSource, toDataSource);
					validator.addDataSourcePair(toDataSource, fromDataSource);
					fireEventOccurred(new GeneralOutputEvent(
							"Added DataSource restriction for "
									+ fromDataSource.getId() + " ==> "
									+ toDataSource.getId(),
							getCurrentMethodName()));
				} else {
					if (fromDataSource == null)
						fireEventOccurred(new WrongParameterEvent(values[0]
								+ " is not a valid DataSource (From)",
								getCurrentMethodName()));
					if (toDataSource == null)
						fireEventOccurred(new WrongParameterEvent(values[1]
								+ " is not a valid DataSource (To)",
								getCurrentMethodName()));
				}
			}
		}

		// add relation type restrictions
		Set<RelationType> relationTypes = new HashSet<RelationType>();
		if (validRelationTypes != null && validRelationTypes.length > 0) {

			// get relation types from arguments
			for (String s : validRelationTypes) {
				RelationType relationType = graph.getMetaData()
						.getRelationType(s.trim());
				if (relationType != null) {
					relationTypes.add(relationType);
					validator.addRelationType(relationType);
					fireEventOccurred(new GeneralOutputEvent(
							"Added Relation Type restriction for ",
							getCurrentMethodName()));
				} else {
					fireEventOccurred(new WrongParameterEvent(s
							+ " is not a valid Relation Type",
							getCurrentMethodName()));
				}
			}

			// this is dangerous and will collapse everything unless they have
			// added concept restrictions so warn (this is useful for testing
			// however)
			if (relationTypes.size() == 0) {
				fireEventOccurred(new GeneralOutputEvent(
						"!Warning No Relation Types specified in relation collapser (this will collapse all known relation types)",
						getCurrentMethodName()));
			}

			ns.setValidator(validator);

			// all valid relations
			Set<ONDEXRelation> relations = new HashSet<ONDEXRelation>();

			// add all relations of the valid relation types
			for (RelationType relationType : relationTypes) {
				// big bug was here: it has to be OR instead of AND
				relations
						.addAll(graph.getRelationsOfRelationType(relationType));
			}

			if (relations.size() == 0) {
				fireEventOccurred(new GeneralOutputEvent(
						"No valid relations in scope of specified relations and/or relation types: THEREFORE NO RESULTS",
						getCurrentMethodName()));
				return;
			}

			fireEventOccurred(new GeneralOutputEvent(
					"Locating clusters and collapsing", getCurrentMethodName()));

			NumberFormat formatter = new DecimalFormat("000.00");

			double numRel = relations.size();
			double relcol = 0;

			// we research every time as relations in the graph change as they
			// are deleted and new relations reassigned to replacement concepts
			for (ONDEXRelation relation : relations) {

				// calculate current percentage
				double percent = relcol / numRel * 100d;
				if (percent % 5d == 0) {
					System.out.println(formatter.format(percent)
							+ "% of relations collapsed ");
				}
				relcol++;

				// it is deleted already
				if (relation == null)
					continue;

				// is not a valid starting concept
				if (!validator.isValidRelationAtDepth(relation, 0,
						relation.getFromConcept())) {
					continue;
				}

				// search neighbours of concept
				ns.search(relation.getFromConcept());

				// get relations of concept
				Set<ONDEXRelation> foundEquRs = ns.getFoundRelations();
				if (foundEquRs.size() == 0) {
					// condition where lone equ relation has the same from and
					// to concept
					graph.deleteRelation(relation.getId());
					continue;
					// condition where the seed equ is excluded by relation
					// neigh arguments and relation from ==
					// to
				}
				clc.collapseRelationCluster(graph, foundEquRs);
			}
		}

		fireEventOccurred(new GeneralOutputEvent("Completed collapsing ",
				getCurrentMethodName()));
		ns.shutdown();
	}

	public String getName() {
		return "Relation Collapser";
	}

	public String getVersion() {
		return "19.09.2011";
	}

	@Override
	public String getId() {
		return "relationcollapser";
	}

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new StringArgumentDefinition(RELATION_TYPE_ARG,
						RELATION_TYPE_ARG_DESC, true, null, true),
				new StringMappingPairArgumentDefinition(
						CONCEPTCLASS_RESTRICTION_ARG,
						CONCEPTCLASS_RESTRICTION_ARG_DESC, false, null, true),
				new StringMappingPairArgumentDefinition(
						DATASOURCE_RESTRICTION_ARG,
						DATASOURCE_RESTRICTION_ARG_DESC, false, null, true),
				new BooleanArgumentDefinition(CLONE_ATTRIBUTES_ARG,
						CLONE_ATTRIBUTES_ARG_DESC, false, true),
				new BooleanArgumentDefinition(COPY_TAG_REFERENCES_ARG,
						COPY_TAG_REFERENCES_ARG_DESC, false, false),
				new StringArgumentDefinition(DATASOURCE_PREFNAMES_ARG,
						DATASOURCE_PREFNAMES_ARG_DESC, false, "", false) };

	}

	public boolean requiresIndexedGraph() {
		return false;
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
