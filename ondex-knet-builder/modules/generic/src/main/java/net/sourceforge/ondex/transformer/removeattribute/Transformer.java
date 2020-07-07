package net.sourceforge.ondex.transformer.removeattribute;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * A simple transformer for removing Attribute's based on an attribute name
 * 
 * @author lysenkoa, hindlem
 */
@Authors(authors = { "Artem Lysenko", "Matthew Hindle", "Jan Taubert" }, emails = {
		"lysenkoa at users.sourceforge.net",
		"matthew_hindle at users.sourceforge.net" })
@Status(description = "Tested December 2013 (Artem Lysenko)", status = StatusType.STABLE)
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Transformer extends ONDEXTransformer implements ArgumentNames {

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] {
				new StringArgumentDefinition(ATTRIBUTE_NAME_ARG,
						ATTRIBUTE_NAME_ARG_DESC, false, null, true),
				new StringArgumentDefinition(DATASOURCE_ARG,
						DATASOURCE_ARG_DESC, false, null, false),
				new StringArgumentDefinition(CONCEPTCLASS_ARG,
						CONCEPTCLASS_ARG_DESC, false, null, false),
				new StringArgumentDefinition(ACCESSION_DATASOURCE_ARG,
						ACCESSION_DATASOURCE_ARG_DESC, false, null, true),
				new StringArgumentDefinition(RELATIONTYPE_ARG,
						RELATIONTYPE_ARG_DESC, false, null, true),
				new BooleanArgumentDefinition(EXCLUDE_ARG, EXCLUDE_ARG_DESC,
						false, true) };
	}

	public String getName() {
		return "Delete accessions and attributes from concepts or relations";
	}

	public String getVersion() {
		return "11/11/2011";
	}

	@Override
	public String getId() {
		return "removeattribute";
	}

	public boolean requiresIndexedGraph() {
		return false;
	}

	public void start() throws InvalidPluginArgumentException {

		// get logic from user arguments
		boolean removeMatches = (Boolean) args.getUniqueValue(EXCLUDE_ARG);

		// number of removed attributes or accessions
		int number = 0;

		// start with all relations from graph
		Set<ONDEXRelation> relations = BitSetFunctions.copy(graph
				.getRelations());

		// filter by relation type
		String relationTypeName = (String) args
				.getUniqueValue(RELATIONTYPE_ARG);
		if (relationTypeName != null) {
			RelationType relationTypeRestriction = graph.getMetaData()
					.getRelationType(relationTypeName);
			if (relationTypeRestriction == null) {
				this.fireEventOccurred(new WrongParameterEvent(relationTypeName
						+ " is not a valid RelationType",
						getCurrentMethodName()));
				throw new InvalidPluginArgumentException(relationTypeName
						+ " is not a valid RelationType");
			}
			relations.retainAll(graph
					.getRelationsOfRelationType(relationTypeRestriction));
		}

		// start with all concepts from graph
		Set<ONDEXConcept> concepts = BitSetFunctions.copy(graph.getConcepts());

		// filter by data source
		String dataSourceName = (String) args.getUniqueValue(DATASOURCE_ARG);
		if (dataSourceName != null) {
			DataSource dataSourceRestriction = graph.getMetaData()
					.getDataSource(dataSourceName);
			if (dataSourceRestriction == null) {
				this.fireEventOccurred(new WrongParameterEvent(dataSourceName
						+ " is not a valid DataSource", getCurrentMethodName()));
				throw new InvalidPluginArgumentException(dataSourceName
						+ " is not a valid DataSource");
			}
			concepts.retainAll(graph
					.getConceptsOfDataSource(dataSourceRestriction));
		}

		// restrict base to the given concept class
		String conceptClassName = (String) args
				.getUniqueValue(CONCEPTCLASS_ARG);
		if (conceptClassName != null) {
			ConceptClass conceptClassRestriction = graph.getMetaData()
					.getConceptClass(conceptClassName);
			if (conceptClassRestriction == null) {
				this.fireEventOccurred(new WrongParameterEvent(conceptClassName
						+ " is not a valid ConceptClass",
						getCurrentMethodName()));
				throw new InvalidPluginArgumentException(conceptClassName
						+ " is not a valid ConceptClass");
			}
			concepts.retainAll(graph
					.getConceptsOfConceptClass(conceptClassRestriction));
		}

		// remove Attributes from concepts and relations
		String[] attributeArguments = (String[]) args
				.getObjectValueArray(ATTRIBUTE_NAME_ARG);
		if (attributeArguments != null) {

			// add attribute names from arguments
			Set<AttributeName> attributeNames = new HashSet<AttributeName>();
			for (String attributeNameId : attributeArguments) {
				AttributeName attributeName = graph.getMetaData()
						.getAttributeName(attributeNameId);
				if (attributeName == null) {
					this.fireEventOccurred(new WrongParameterEvent(
							attributeNameId + " is not a valid AttributeName",
							getCurrentMethodName()));
					throw new InvalidPluginArgumentException(attributeNameId
							+ " is not a valid AttributeName");
				}
				attributeNames.add(attributeName);
			}

			// the final removing of Attribute on user constraints
			for (ONDEXConcept concept : concepts) {
				if (removeMatches) {
					// simply remove all user specified attribute names
					for (AttributeName an : attributeNames) {
						if (concept.deleteAttribute(an))
							number++;
					}
				} else if (attributeNames.size() > 0) {
					// build inverse to user specified attribute names
					List<AttributeName> attrNamesToRemove = new LinkedList<AttributeName>();
					for (Attribute attr : concept.getAttributes()) {
						if (!attributeNames.contains(attr.getOfType()))
							attrNamesToRemove.add(attr.getOfType());
					}
					// remove them from concept
					for (AttributeName an : attrNamesToRemove) {
						if (concept.deleteAttribute(an))
							number++;
					}
				}
			}

			// the final removing of Attribute on user constraints
			for (ONDEXRelation relation : relations) {
				if (removeMatches) {
					// simply remove all user specified attribute names
					for (AttributeName an : attributeNames) {
						if (relation.deleteAttribute(an))
							number++;
					}
				} else if (attributeNames.size() > 0) {
					// build inverse to user specified attribute names
					List<AttributeName> attrNamesToRemove = new LinkedList<AttributeName>();
					for (Attribute attr : relation.getAttributes()) {
						if (!attributeNames.contains(attr.getOfType()))
							attrNamesToRemove.add(attr.getOfType());
					}
					// remove them from relation
					for (AttributeName an : attrNamesToRemove) {
						if (relation.deleteAttribute(an))
							number++;
					}
				}
			}
		}

		// look for certain type of accession to be removed
		String[] accessionDataSourceArguments = (String[]) args
				.getObjectValueArray(ACCESSION_DATASOURCE_ARG);
		if (accessionDataSourceArguments != null) {

			// add data sources from arguments
			Set<DataSource> accessionDataSources = new HashSet<DataSource>();
			for (String accessionDataSourceName : accessionDataSourceArguments) {
				DataSource dataSource = graph.getMetaData().getDataSource(
						accessionDataSourceName);
				if (dataSource == null) {
					this.fireEventOccurred(new WrongParameterEvent(
							accessionDataSourceName
									+ " is not a valid DataSource",
							getCurrentMethodName()));
					throw new InvalidPluginArgumentException(
							accessionDataSourceName
									+ " is not a valid DataSource");
				}
				accessionDataSources.add(dataSource);
			}

			// the final removing of accessions on user constraints
			for (ONDEXConcept concept : concepts) {

				// list of all accessions to be removed on current concept
				List<ConceptAccession> accessionsToBeRemoved = new LinkedList<ConceptAccession>();
				for (ConceptAccession conceptAcc : concept
						.getConceptAccessions()) {
					if (removeMatches) {
						// remove accessions as specified by user
						if (accessionDataSources.contains(conceptAcc
								.getElementOf()))
							accessionsToBeRemoved.add(conceptAcc);
					} else if (accessionDataSources.size() > 0) {
						// remove all other accessions not specified by user
						if (!accessionDataSources.contains(conceptAcc
								.getElementOf()))
							accessionsToBeRemoved.add(conceptAcc);
					}
				}
				// finally remove all found concept accessions
				for (ConceptAccession temp : accessionsToBeRemoved) {
					if (concept.deleteConceptAccession(temp.getAccession(),
							temp.getElementOf()))
						number++;
				}
			}
		}

		// some general output
		System.err.println(number);
		this.fireEventOccurred(new GeneralOutputEvent(
				number
						+ " thingies successfuly removed from yer graph. Have a pleasant day!",
				getCurrentMethodName()));
	}

	/**
	 * Convenience method for outputing the current method name in a dynamic way
	 * 
	 * @return the calling method name
	 */
	public static String getCurrentMethodName() {
		Exception e = new Exception();
		StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
		String name = trace.getMethodName();
		String className = trace.getClassName();
		int line = trace.getLineNumber();
		return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
				+ "]";
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
