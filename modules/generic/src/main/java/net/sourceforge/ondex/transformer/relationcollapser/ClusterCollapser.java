package net.sourceforge.ondex.transformer.relationcollapser;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.exception.type.InconsistencyException;

/**
 * Collapses clusters of concepts and relations. Transfers attributes and other
 * properties according to arguments set.
 * 
 * @author hindlem, taubertj
 */
public class ClusterCollapser {

	/**
	 * String manipulation
	 */
	private final Pattern colonPattern = Pattern.compile(":");
	private final Comparator<String> defaultStringComparator;

	/**
	 * Track new meta data
	 */
	private final Map<String, ConceptClass> newConceptClasses;
	private final Map<String, DataSource> newDataSources;

	/**
	 * Properties copying flags
	 */
	private final boolean cloneAttributes;
	private final boolean copyTagRefs;

	/**
	 * Properties for copying names
	 */
	private final DataSource prefNamesDataSource;

	/**
	 * Initialises string comparator and internal data structures.
	 * 
	 * @param cloneAttributes
	 * @param copyTagRefs
	 * @param prefNamesDataSource
	 */
	public ClusterCollapser(boolean cloneAttributes, boolean copyTagRefs,
			DataSource prefNamesDataSource) {

		this.defaultStringComparator = new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		};

		this.newConceptClasses = new HashMap<String, ConceptClass>();
		this.newDataSources = new HashMap<String, DataSource>();

		this.cloneAttributes = cloneAttributes;
		this.copyTagRefs = copyTagRefs;
		this.prefNamesDataSource = prefNamesDataSource;
	}

	/**
	 * The main method for collapsing clusters
	 * 
	 * @param graph
	 *            the graph containing the cluster
	 * @param cluster
	 *            the ids of the relations that form this cluster
	 * @return the new super concept
	 * @throws InconsistencyException
	 */
	public ONDEXConcept collapseRelationCluster(ONDEXGraph graph,
			Set<ONDEXRelation> cluster) throws InconsistencyException {

		if (cluster.size() == 0) {
			return null; // nothing to do
		}

		// collect all concepts of cluster
		Set<ONDEXConcept> clusterConcepts = new HashSet<ONDEXConcept>();
		for (ONDEXRelation collapseRelation : cluster) {

			ONDEXConcept fromConcept = collapseRelation.getFromConcept();
			ONDEXConcept toConcept = collapseRelation.getToConcept();

			if (fromConcept.equals(toConcept)) { // ignore self relations
				graph.deleteRelation(collapseRelation.getId());
				continue;
			}

			clusterConcepts.add(fromConcept);
			clusterConcepts.add(toConcept);
		}

		// all of the cluster was self relations, therefore collapsing on this
		// cluster is pointless
		if (clusterConcepts.size() == 0)
			return null;

		// collapse the now isolated equivalent clusters to a core node
		return collapseConceptCluster(graph, clusterConcepts);
	}

	/**
	 * Collapses a group of concepts to one super concept
	 * 
	 * @param graph
	 *            the graph to collapse on
	 * @param clusterConcepts
	 *            concepts to collapse
	 * @return the new super concept
	 * @throws InconsistencyException
	 */
	public ONDEXConcept collapseConceptCluster(ONDEXGraph graph,
			Set<ONDEXConcept> clusterConcepts) throws InconsistencyException {

		TreeSet<String> sortedConceptClassConcat = new TreeSet<String>(
				defaultStringComparator);
		TreeSet<String> sortedDataSourceConcat = new TreeSet<String>(
				defaultStringComparator);
		TreeSet<String> sortedPIDsConcat = new TreeSet<String>(
				defaultStringComparator);

		Set<String> descriptions = new HashSet<String>();
		Set<String> annotations = new HashSet<String>();
		Map<String, ConceptName> conceptNames = new HashMap<String, ConceptName>();
		Set<ConceptAccession> conceptAccessions = new HashSet<ConceptAccession>();
		Set<EvidenceType> conceptEvidences = new HashSet<EvidenceType>();

		// index all attributes from collapsed concepts
		Map<AttributeName, Set<Attribute>> attributesMap = null;
		if (cloneAttributes)
			attributesMap = new HashMap<AttributeName, Set<Attribute>>();

		// concepts which have these concepts as tag
		Set<ONDEXConcept> tagRefsConcepts = null;
		if (copyTagRefs)
			tagRefsConcepts = new HashSet<ONDEXConcept>();

		// relations which have these concepts as tag
		Set<ONDEXRelation> tagRefsRelations = null;
		if (copyTagRefs)
			tagRefsRelations = new HashSet<ONDEXRelation>();

		// collect all concept tags
		Set<ONDEXConcept> conceptTags = new HashSet<ONDEXConcept>();

		// System.out.println("Collapsing concept cluster "+clusterConcepts.size());

		// process each concept of cluster
		for (ONDEXConcept concept : clusterConcepts) {

			// record concept classes encountered in cluster
			String[] conceptClasses = colonPattern.split(concept.getOfType()
					.getId());
			for (String ccName : conceptClasses) {
				sortedConceptClassConcat.add(ccName);
			}

			// record data sources encountered in cluster
			String[] dataSources = colonPattern.split(concept.getElementOf()
					.getId());
			for (String dsName : dataSources) {
				sortedDataSourceConcat.add(dsName);
			}

			// collect other concept properties
			sortedPIDsConcat.add(concept.getPID());
			descriptions.add(concept.getDescription());
			annotations.add(concept.getAnnotation());

			// additional concept properties
			for (ConceptName cn : concept.getConceptNames()) {
				String name = cn.getName();
				// check for a data source restriction for preferred names
				if (prefNamesDataSource != null) {
					if (!concept.getElementOf().equals(prefNamesDataSource))
						cn.setPreferred(false);
				}
				// add name to map of names
				if (conceptNames.containsKey(name)) {
					ConceptName oldCN = conceptNames.get(name);
					// a preferred concept name overrides
					if (cn.isPreferred())
						oldCN.setPreferred(true);
				} else {
					conceptNames.put(name, cn);
				}
			}
			conceptAccessions.addAll(concept.getConceptAccessions());
			conceptEvidences.addAll(concept.getEvidence());
			conceptTags.addAll(concept.getTags());

			if (copyTagRefs) {
				// get concepts which have this concept as a tag
				tagRefsConcepts.addAll(graph.getConceptsOfTag(concept));

				// get relations which have this concept as a tag
				tagRefsRelations.addAll(graph.getRelationsOfTag(concept));
			}

			if (cloneAttributes) {
				// get all attributes from concept
				fillAttributeIndex(attributesMap, concept.getAttributes());
			}
		}

		// System.out.println("Concept Class concat "+sortedConceptClassConcat.size());
		ConceptClass newConceptClass = null;
		if (sortedConceptClassConcat.size() == 1) {

			// simplest case when all concepts in cluster share same concept
			// class, reuse this concept class then
			newConceptClass = graph.getMetaData().getConceptClass(
					sortedConceptClassConcat.first());
		} else {

			// concatenate all found concept class IDs
			StringBuilder newCCName = new StringBuilder();
			boolean first = true;
			for (String ccName : sortedConceptClassConcat) {
				if (!first)
					newCCName.append(':');
				newCCName.append(ccName);
				first = false;
			}

			// check if new concept class has been created already
			String ccName = newCCName.toString();
			newConceptClass = newConceptClasses.get(ccName);
			if (newConceptClass == null) {
				// may have been created on a previous collapse
				newConceptClass = graph.getMetaData().getConceptClass(ccName);
				if (newConceptClass == null) {
					newConceptClass = graph
							.getMetaData()
							.getFactory()
							.createConceptClass(ccName, ccName,
									"Collapsed Concept Class Set");
				}
				newConceptClasses.put(ccName, newConceptClass);
			}
		}

		// System.out.println("DataSource concat "+sortedCVConcat.size());
		DataSource newDataSource;
		if (sortedDataSourceConcat.size() == 1) {

			// simplest case when all concepts in cluster share same data
			// source, reuse this data source then
			newDataSource = graph.getMetaData().getDataSource(
					sortedDataSourceConcat.first());
		} else {

			// concatenate all found data source IDs
			StringBuilder newDSName = new StringBuilder();
			boolean first = true;
			for (String dsName : sortedDataSourceConcat) {
				if (!first)
					newDSName.append(':');
				newDSName.append(dsName);
				first = false;
			}

			// check if new data source has been created already
			String dsName = newDSName.toString();
			newDataSource = newDataSources.get(dsName);
			if (newDataSource == null) {
				// may have been created on a previous collapse
				newDataSource = graph.getMetaData().getDataSource(dsName);
				if (newDataSource == null) {
					newDataSource = graph.getMetaData().createDataSource(
							dsName, dsName, "Collapsed Data Source Set");
				}
				newDataSources.put(dsName, newDataSource);
			}
		}

		// System.out.println("Create new Concept ");
		ONDEXConcept newConcept = graph.createConcept(
				toString(sortedPIDsConcat), toString(annotations),
				toString(descriptions), newDataSource, newConceptClass,
				conceptEvidences);

		// System.out.println("Adding context ");
		for (ONDEXConcept tag : conceptTags) {
			if (clusterConcepts.contains(tag)) {
				// concept is now tag of itself
				newConcept.addTag(newConcept);
			} else {
				// tag still does exist
				newConcept.addTag(tag);
			}
		}

		if (cloneAttributes) {
			// System.out.println("Clone concept attributes");
			copyConceptAttributes(graph, newConcept, attributesMap);
		}

		if (copyTagRefs) {

			// go through all concepts which previously had some cluster concept
			// in their tag list
			for (ONDEXConcept concept : tagRefsConcepts) {
				// sanity check, cluster concepts get deleted anyway
				if (!clusterConcepts.contains(concept)) {
					concept.addTag(newConcept);
				}
			}

			// go through all relations which previously had some cluster
			// concept in their tag list
			for (ONDEXRelation relation : tagRefsRelations) {
				relation.addTag(newConcept);
			}
		}

		// System.out.println("create concept names ");
		for (ConceptName cn : conceptNames.values()) {
			newConcept.createConceptName(cn.getName(), cn.isPreferred());
		}

		// System.out.println("create concept accessions ");
		for (ConceptAccession acc : conceptAccessions) {
			newConcept.createConceptAccession(acc.getAccession(),
					acc.getElementOf(), acc.isAmbiguous());
		}

		Set<ONDEXConcept> conceptsTodelete = new HashSet<ONDEXConcept>(
				clusterConcepts.size());

		// System.out.println("copy relations");
		for (ONDEXConcept concept : clusterConcepts) {
			conceptsTodelete.add(concept);

			// get all relations of current cluster concept
			for (ONDEXRelation relation : graph.getRelationsOfConcept(concept)
					.toArray(new ONDEXRelation[0])) {
				ONDEXConcept from = relation.getFromConcept();
				ONDEXConcept to = relation.getToConcept();

				// initialise here
				ONDEXConcept newFrom = from;
				ONDEXConcept newTo = to;

				// check it is an external concept and assign new from and to's
				if (from.getId() == to.getId()) {
					// this was a self loop and should be copied as a self loop
					if (clusterConcepts.contains(from)) {
						newFrom = newConcept;
						newTo = newConcept;
					}
				}

				// from concept is part of cluster
				else if (from.getId() == concept.getId()) {
					newFrom = newConcept;
					newTo = to;
					// to concept is also part of cluster
					if (clusterConcepts.contains(to)) {
						graph.deleteRelation(relation.getId());
						continue; // this is a lost internal cluster relation
					}
				}

				// to concept is part of cluster
				else if (to.getId() == concept.getId()) {
					newFrom = from;
					newTo = newConcept;
					// from concept is also part of cluster
					if (clusterConcepts.contains(from)) {
						graph.deleteRelation(relation.getId());
						continue; // this is a lost internal cluster relation
					}
				}

				// System.out.println("get new relation");
				ONDEXRelation newRelation = graph.getRelation(newFrom, newTo,
						relation.getOfType());

				// create relation if did not exist
				if (newRelation == null) {
					newRelation = graph.createRelation(newFrom, newTo,
							relation.getOfType(), relation.getEvidence());
				}

				// System.out.println("add evidence type");
				addEvidenceTypeToRelation(newRelation, relation.getEvidence());

				// copy across tags
				for (ONDEXConcept tag : relation.getTags()) {
					newRelation.addTag(tag);
				}

				if (cloneAttributes) {
					// System.out.println("copy relation gds");
					copyRelationAttributes(graph, relation, newRelation);
				}

				graph.deleteRelation(relation.getId());
			}
		}

		// clean up
		for (ONDEXConcept concept : conceptsTodelete) {
			graph.deleteConcept(concept.getId());
		}

		// System.out.println("Collapsed concept");
		return newConcept;
	}

	/**
	 * Copies attributes collected from old concepts to the new one
	 * 
	 * @param graph
	 * @param newConcept
	 * @param attributesMap
	 * @throws InconsistencyException
	 */
	private void copyConceptAttributes(ONDEXGraph graph,
			ONDEXConcept newConcept,
			Map<AttributeName, Set<Attribute>> attributesMap)
			throws InconsistencyException {

		// all possible attribute names
		for (AttributeName attName : attributesMap.keySet()) {

			// get set of attributes for attribute name
			Set<Attribute> attributes = attributesMap.get(attName);

			// simplest case, there is only one attribute for this name
			if (attributes.size() == 1) {

				// just copy single attribute over
				Attribute newAttribute = attributes.iterator().next();
				newConcept.createAttribute(attName, newAttribute.getValue(),
						newAttribute.isDoIndex());

			} else {

				// copy across all Attributes
				for (Attribute oldAtt : attributes) {

					// get old attribute name
					AttributeName oldAttName = oldAtt.getOfType();

					// check if attribute for this name already exists
					Attribute existingAtt = newConcept.getAttribute(oldAttName);

					if (existingAtt == null) {

						// if Attribute does not exist create it
						newConcept.createAttribute(oldAttName,
								oldAtt.getValue(), oldAtt.isDoIndex());

					} else if (oldAtt.getValue() instanceof Collection
							&& existingAtt.getValue() instanceof Collection) {

						// old attribute and be cast to collection
						Collection<?> oldCol = (Collection<?>) oldAtt
								.getValue();

						// existing attribute can be cast to collection
						Collection<?> existingCol = (Collection<?>) existingAtt
								.getValue();

						// new set merges both collections
						Set<Object> set = new HashSet<Object>(oldCol);
						set.addAll(existingCol);

						// needed for Berkeley to know something has been added
						existingAtt.setValue(set);

					} else if (!(oldAtt.getValue() instanceof Collection && existingAtt
							.getValue() instanceof Collection)) {

						// search for a free attribute name
						AttributeName newAttName = oldAttName;
						do {
							newAttName = getNewAttributeName(graph, newAttName);
						} while (newConcept.getAttribute(newAttName) != null);

						// add old value as a separate attribute
						newConcept.createAttribute(newAttName,
								oldAtt.getValue(), oldAtt.isDoIndex());

					} else
						// that means something is wrong with the
						// attribute name, should be either both collections or
						// none of them, inconsistency in data found!
						throw new InconsistencyException(
								"Trying to merge a collection attribute with a non collection attribute: "
										+ oldAtt.getOfType().getId());
				}
			}
		}
	}

	/**
	 * Copies all attributes from oldRelation to newRelation.
	 * 
	 * @param graph
	 * @param oldRelation
	 * @param newRelation
	 * @throws InconsistencyException
	 */
	private void copyRelationAttributes(ONDEXGraph graph,
			ONDEXRelation oldRelation, ONDEXRelation newRelation)
			throws InconsistencyException {

		// copy across all Attributes
		for (Attribute oldAtt : oldRelation.getAttributes()) {

			// get old attribute name
			AttributeName oldAttName = oldAtt.getOfType();

			// check if attribute for this name already exists
			Attribute existingAtt = newRelation.getAttribute(oldAttName);

			if (existingAtt == null) {

				// if Attribute does not exist create it
				newRelation.createAttribute(oldAttName, oldAtt.getValue(),
						oldAtt.isDoIndex());

			} else if (oldAtt.getValue() instanceof Collection
					&& existingAtt.getValue() instanceof Collection) {

				// old attribute and be cast to collection
				Collection<?> oldCol = (Collection<?>) oldAtt.getValue();

				// existing attribute can be cast to collection
				Collection<?> existingCol = (Collection<?>) existingAtt
						.getValue();

				// new set merges both collections
				Set<Object> set = new HashSet<Object>(oldCol);
				set.addAll(existingCol);

				// needed for Berkeley to know something has been added
				existingAtt.setValue(set);

			} else if (!(oldAtt.getValue() instanceof Collection && existingAtt
					.getValue() instanceof Collection)) {

				// search for a free attribute name
				AttributeName newAttName = oldAttName;
				do {
					newAttName = getNewAttributeName(graph, newAttName);
				} while (newRelation.getAttribute(newAttName) != null);

				// add old value as a separate attribute
				newRelation.createAttribute(newAttName, oldAtt.getValue(),
						oldAtt.isDoIndex());

			} else
				// that means something is wrong with the
				// attribute name, should be either both collections or
				// none of them, inconsistency in data found!
				throw new InconsistencyException(
						"Trying to merge a collection attribute with a non collection attribute: "
								+ oldAtt.getOfType().getId());
		}
	}

	/**
	 * Indexes all given attributes by their attribute name and add them to the
	 * set of attributes.
	 * 
	 * @param attributesMap
	 * @param attributes
	 */
	private void fillAttributeIndex(
			Map<AttributeName, Set<Attribute>> attributesMap,
			Set<Attribute> attributes) {

		// iterate over every attribute
		for (Attribute attribute : attributes) {

			// add attribute to set indexed by attribute name
			AttributeName attName = attribute.getOfType();
			if (!attributesMap.containsKey(attName))
				attributesMap.put(attName, new HashSet<Attribute>());
			attributesMap.get(attName).add(attribute);
		}
	}

	/**
	 * Adds the evidence types to the target relation where they do not already
	 * exist
	 * 
	 * @param newRelation
	 *            the new relations
	 * @param evi
	 *            the new evidence types
	 */
	private void addEvidenceTypeToRelation(ONDEXRelation newRelation,
			Set<EvidenceType> evi) {
		if (evi.size() == 0) {
			return;
		}
		Set<EvidenceType> eviExisting = newRelation.getEvidence();
		for (EvidenceType et : evi) {
			if (!eviExisting.contains(et)) {
				newRelation.addEvidenceType(et);
			}
		}
	}

	private String toString(Set<String> pids) {
		StringBuffer sb = new StringBuffer();
		for (String pid : pids) {
			if (sb.length() > 0)
				sb.append(';');
			sb.append(pid);
		}
		return sb.toString();
	}

	/**
	 * Creates a new Attribute by incrementing _n
	 * 
	 * @param graph
	 *            the graph to create new MetaData on
	 * @param att
	 *            AttributeName to increment
	 * @return a incremented AttributeName
	 */
	private AttributeName getNewAttributeName(ONDEXGraph graph,
			AttributeName att) {
		ONDEXGraphMetaData ondexMetaData = graph.getMetaData();

		// System.out.println(att.getId());
		// work out original id and highest integer
		String attId = att.getId();
		int i = 0;
		int index = attId.lastIndexOf('_');
		if (index > -1) {
			String id = att.getId();
			attId = id.substring(0, index);
			try {
				i = Integer.parseInt(id.substring(index + 1, id.length()));
			} catch (NumberFormatException e) {
				// can't find number there may be stray :
			}
		}

		// increment count by one
		i++;

		// create a new attribute name
		String name = attId + '_' + i;
		AttributeName attNew = ondexMetaData.getAttributeName(name);
		if (attNew == null)
			attNew = ondexMetaData.createAttributeName(name, att.getFullname(),
					att.getDescription(), att.getUnit(), att.getDataType(),
					att.getSpecialisationOf());
		return attNew;
	}

}
