package net.sourceforge.ondex.tools.functions;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

import java.util.*;

/**
 * Extended library of methods for mundane graph manipulation tasks
 * 
 * @author lysenkoa
 */
public class GraphElementManipulation {
	private GraphElementManipulation() {
	}

	public static void rightJoin(ONDEXGraph graph, ONDEXConcept source,
			ONDEXConcept target) {
	}

	/**
	 * @param graph
	 *            - graph
	 * @param ids
	 *            - concept classes to compound
	 * @return - new concept class
	 * @author hindlem
	 *         <p/>
	 *         The method for creation of compound concept classes used during
	 *         the collapsing of entities
	 */
	public static ConceptClass getCompoundConceptClass(ONDEXGraph graph,
			Collection<ConceptClass> ids) {
		final TreeSet<String> sortedConceptClassConcat = new TreeSet<String>();
		ConceptClass newCc = null;
		for (ConceptClass id : ids) {
			String[] compoundIds = id.getId().split(":");
			if (compoundIds.length > 1) {
				Collections.addAll(sortedConceptClassConcat, compoundIds);
			} else {
				sortedConceptClassConcat.add(id.getId());
			}
		}

		if (sortedConceptClassConcat.size() == 0)
			throw new RuntimeException("The set supplied must not be empty");
		if (sortedConceptClassConcat.size() > 1) {
			StringBuilder newCCName = new StringBuilder();
			Iterator<String> ccit = sortedConceptClassConcat.iterator();
			boolean first = true;
			while (ccit.hasNext()) {
				String ccName = ccit.next();
				if (!first)
					newCCName.append(':');
				newCCName.append(ccName);
				first = false;
			}

			String ccName = newCCName.toString();
			newCc = graph.getMetaData().getConceptClass(ccName);
			if (newCc == null) {
				newCc = graph
						.getMetaData()
						.getFactory()
						.createConceptClass(ccName, ccName,
								"Collapsed Concept Class Set");
			}
		} else {
			String ccName = sortedConceptClassConcat.iterator().next();
			newCc = graph.getMetaData().getConceptClass(ccName);
			if (newCc == null) {
				newCc = graph
						.getMetaData()
						.getFactory()
						.createConceptClass(ccName, ccName,
								"Collapsed Concept Class Set");
			}
		}
		return newCc;
	}

	/**
	 * @param graph
	 *            - graph
	 * @param ids
	 *            - dataSources to compound
	 * @return - new dataSource
	 * @author hindlem
	 *         <p/>
	 *         The method for creation of compound dataSources used during the
	 *         collapsing of entities
	 */
	public static DataSource getCompoundDataSource(ONDEXGraph graph,
			Collection<DataSource> ids) {
		final TreeSet<String> sortedDataSourceConcat = new TreeSet<String>();
		DataSource newDataSource = null;
		for (DataSource id : ids) {
			String[] compoundIds = id.getId().split(":");
			if (compoundIds.length > 1) {
				Collections.addAll(sortedDataSourceConcat, compoundIds);
			} else {
				sortedDataSourceConcat.add(id.getId());
			}

		}

		if (sortedDataSourceConcat.size() == 0)
			throw new RuntimeException("The set supplied must not be empty");
		if (sortedDataSourceConcat.size() > 1) {
			StringBuilder newDataSourceName = new StringBuilder();
			Iterator<String> ccit = sortedDataSourceConcat.iterator();
			boolean first = true;
			while (ccit.hasNext()) {
				String dataSourceName = ccit.next();
				if (!first)
					newDataSourceName.append(':');
				newDataSourceName.append(dataSourceName);
				first = false;
			}

			String dataSourceId = newDataSourceName.toString();
			newDataSource = graph.getMetaData().getDataSource(dataSourceId);
			if (newDataSource == null) {
				newDataSource = graph
						.getMetaData()
						.getFactory()
						.createDataSource(dataSourceId, dataSourceId,
								"Collapsed DataSource Set");
			}
		} else {
			String dataSourceId = sortedDataSourceConcat.iterator().next();
			newDataSource = graph.getMetaData().getDataSource(dataSourceId);
			if (newDataSource == null) {
				newDataSource = graph
						.getMetaData()
						.getFactory()
						.createDataSource(dataSourceId, dataSourceId,
								"Collapsed DataSource Set");
			}
		}
		return newDataSource;
	}

	/**
	 * Clones the gds attribute form source concept to target concept. Will
	 * create the numbered attribute name, if the target already has such
	 * attribute name.
	 * 
	 * @param graph
	 *            - graph for the concepts
	 * @param source
	 *            - source of the gds to be copied
	 * @param target
	 *            - target concept, where the the equivalent value will be
	 *            created
	 * @param anOriginal
	 *            - origianl attribute name to copy
	 */
	public static final void cloneAttributeWithAttName(ONDEXGraph graph,
			ONDEXConcept source, ONDEXConcept target, AttributeName anOriginal) {
		AttributeName an = anOriginal;
		ONDEXGraphMetaData meta = graph.getMetaData();
		if (target.getAttribute(anOriginal) != null) {
			int counter = 1;
			while (target.getAttribute(an) != null) {
				String id = anOriginal.getId() + ":" + counter;
				an = meta.getAttributeName(id);
				if (an == null) {
					an = meta.getFactory().createAttributeName(id,
							anOriginal.getFullname(), anOriginal.getDataType(),
							anOriginal.getSpecialisationOf());
					break;
				}
				counter++;
			}
		}
		target.createAttribute(an, source.getAttribute(anOriginal).getValue(),
				source.getAttribute(anOriginal).isDoIndex());
	}

	/**
	 * Clones the gds attribute form source relation to target relation. Will
	 * create the numbered attribute name, if the target already has such
	 * attribute name.
	 * 
	 * @param graph
	 *            - graph for the relation
	 * @param source
	 *            - source of the gds to be copied
	 * @param target
	 *            - target relation, where the the equivalent value will be
	 *            created
	 * @param anOriginal
	 *            - origianl attribute name to copy
	 */
	public static final void cloneAttributeWithAttName(ONDEXGraph graph,
			ONDEXRelation source, ONDEXRelation target, AttributeName anOriginal) {
		AttributeName an = anOriginal;
		ONDEXGraphMetaData meta = graph.getMetaData();
		if (target.getAttribute(anOriginal) != null) {
			int counter = 1;
			while (target.getAttribute(an) != null) {
				String id = anOriginal.getId() + ":" + counter;
				an = meta.getAttributeName(id);
				if (an == null) {
					an = meta.getFactory().createAttributeName(id,
							anOriginal.getFullname(), anOriginal.getDataType(),
							anOriginal.getSpecialisationOf());
					break;
				}
				counter++;
			}
		}
		target.createAttribute(an, source.getAttribute(anOriginal).getValue(),
				source.getAttribute(anOriginal).isDoIndex());
	}

	/**
	 * Creates a one-to-many collapsed concept by creating collapsed concept of
	 * a source to all targets. Source concept is NOT deleted by this method.
	 * 
	 * @param graph
	 * @param source
	 * @param targets
	 */
	public static final Set<ONDEXConcept> oneToManyCollapse(ONDEXGraph graph,
			ONDEXConcept source, Collection<ONDEXConcept> targets) {
		boolean createNewTarget = false;
		Set<ONDEXConcept> result = new HashSet<ONDEXConcept>();
		System.err.println("Targets number:" + targets.size());
		for (ONDEXConcept target : targets) {
			System.err.println(target.getOfType());
			ConceptClass newClass = target.getOfType();
			Set<ConceptClass> set = new HashSet<ConceptClass>();
			set.add(newClass);
			set.add(source.getOfType());
			if (set.size() > 1) {
				createNewTarget = true;
				newClass = getCompoundConceptClass(graph, set);
			}

			DataSource newDataSource = target.getElementOf();
			Set<DataSource> allDataSources = new HashSet<DataSource>();
			allDataSources.add(newDataSource);
			allDataSources.add(source.getElementOf());
			if (allDataSources.size() > 1) {
				createNewTarget = true;
				newDataSource = getCompoundDataSource(graph, allDataSources);
			}

			ONDEXConcept realTarget = target;
			if (createNewTarget) {
				realTarget = copyConcept(graph, target, newClass, newDataSource);
				copyRelations(graph, target, realTarget);
				graph.deleteConcept(target.getId());
			}
			copyConceptData(graph, source, realTarget);
			copyRelations(graph, source, realTarget);
			result.add(realTarget);
		}
		return result;
	}

	/**
	 * Creates a one-to-one collapsed concept from the two concepts provided as
	 * argument. The original concepts are NOT deleted by this method
	 * 
	 * @param graph
	 *            - graph
	 * @param source
	 *            - concept one
	 * @param target
	 *            - concept two
	 * @return collapsed concept
	 */
	public static final ONDEXConcept oneToOneCollapse(ONDEXGraph graph,
			ONDEXConcept source, ONDEXConcept target) {
		boolean createNewTarget = false;
		Set<ONDEXConcept> result = new HashSet<ONDEXConcept>();
		ConceptClass newClass = target.getOfType();
		Set<ConceptClass> set = new HashSet<ConceptClass>();
		set.add(newClass);
		set.add(source.getOfType());
		if (set.size() > 1) {
			createNewTarget = true;
			newClass = getCompoundConceptClass(graph, set);
		}

		DataSource newDataSource = target.getElementOf();
		Set<DataSource> allDataSources = new HashSet<DataSource>();
		allDataSources.add(newDataSource);
		allDataSources.add(source.getElementOf());
		if (allDataSources.size() > 1) {
			createNewTarget = true;
			newDataSource = getCompoundDataSource(graph, allDataSources);
		}

		ONDEXConcept realTarget = target;
		if (createNewTarget) {
			realTarget = copyConcept(graph, target, newClass, newDataSource);
			copyRelations(graph, target, realTarget);
		}
		copyConceptData(graph, source, realTarget);
		copyRelations(graph, source, realTarget);
		result.add(realTarget);

		return realTarget;
	}

	/**
	 * Creates a copy of a concept and provides an opportunity to select a new
	 * concept class and new DataSource.
	 * 
	 * @param graph
	 *            - graph
	 * @param c
	 *            - original concept that will be copied
	 * @param newCC
	 *            - new concept class that the copy will have
	 * @param newDataSource
	 *            - new DataSource that the copy will have
	 * @return concept that was created
	 */
	public static ONDEXConcept copyConcept(ONDEXGraph graph, ONDEXConcept c,
			ConceptClass newCC, DataSource newDataSource) {
		EvidenceType der = ControledVocabularyHelper.createEvidence(graph,
				"Derived_copy");
		ONDEXConcept nc = graph.getFactory().createConcept(c.getPID(),
				newDataSource, newCC, der);
		copyConceptData(graph, c, nc);
		nc.removeEvidenceType(der);
		return nc;
	}

	/**
	 * Copies evidence, accessions, gdss, names and conctexts from the source
	 * concept to the target concept
	 * 
	 * @param graph
	 * @param copySourceConcept
	 * @param copyTargetConcept
	 */
	public static void copyConceptData(ONDEXGraph graph,
			ONDEXConcept copySourceConcept, ONDEXConcept copyTargetConcept) {
		for (EvidenceType et : copySourceConcept.getEvidence()) {
			copyTargetConcept.addEvidenceType(et);
		}

		for (ConceptAccession acc : copySourceConcept.getConceptAccessions()) {
			copyTargetConcept.createConceptAccession(acc.getAccession(),
					acc.getElementOf(), true);
		}

		for (ConceptName name : copySourceConcept.getConceptNames()) {
			copyTargetConcept.createConceptName(name.getName(),
					name.isPreferred());
		}

		for (Attribute attribute : copySourceConcept.getAttributes()) {
			copyTargetConcept.createAttribute(attribute.getOfType(),
					attribute.getValue(), attribute.isDoIndex());
		}

		if (copySourceConcept.getAnnotation() != null)
			copyTargetConcept.setAnnotation(copySourceConcept.getAnnotation());

		if (copySourceConcept.getDescription() != null)
			copyTargetConcept
					.setDescription(copySourceConcept.getDescription());

		for (ONDEXConcept c : copySourceConcept.getTags())
			copyTargetConcept.addTag(c);
	}

	/**
	 * Method to copy a concept
	 * 
	 * @param graph
	 *            - graph to copy concept to
	 * @param c
	 *            concept to copy
	 * @return a copy of a concept created
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 * @throws EmptyStringException
	 */
	public static ONDEXConcept copyConcept(ONDEXGraph graph, ONDEXConcept c)
			throws NullValueException, AccessDeniedException,
			EmptyStringException {
		return copyConcept(graph, c, c.getOfType(), c.getElementOf());
	}

	/**
	 * Copies all of the attributes from one concept to another (only if they do
	 * not already exist at target)
	 * 
	 * @param from
	 *            - concept to copy from
	 * @param to
	 *            - concept to copy to
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 * @throws EmptyStringException
	 */
	public static void copyConceptAttributes(ONDEXConcept from, ONDEXConcept to)
			throws NullValueException, AccessDeniedException,
			EmptyStringException {

		for (EvidenceType et : from.getEvidence())
			addNewEvidence(to, et);

		for (ConceptAccession ca : from.getConceptAccessions())
			addNewAccession(to, ca);

		for (ConceptName cn : from.getConceptNames())
			addNewName(to, cn);

		for (Attribute attribute : from.getAttributes())
			addNewAttribute(to, attribute);

		if (from.getAnnotation() != null && to.getDescription() == null)
			to.setAnnotation(from.getAnnotation());

		if (from.getDescription() != null && to.getDescription() == null)
			to.setDescription(from.getDescription());
	}

	/**
	 * Copies all of the attributes from one relation to another (only if they
	 * do not already exist at target)
	 * 
	 * @param source
	 *            - relation to copy from
	 * @param target
	 *            - relation to copy to
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 */
	public static void copyRelationAttributes(ONDEXRelation source,
			ONDEXRelation target) throws NullValueException,
			AccessDeniedException {
		for (EvidenceType et : source.getEvidence())
			addNewEvidence(target, et);

		for (Attribute attribute : source.getAttributes())
			addNewAttribute(target, attribute);
	}

	/**
	 * Merges concept toMerge with concept toKeep. All relations and attributes
	 * are copied across.
	 * 
	 * @param graph
	 *            - graph
	 * @param toKeep
	 *            - concept that will be retained and added to
	 * @param toMerge
	 *            - concept that will be merged
	 * @return merged concept
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 * @throws EmptyStringException
	 */
	public static ONDEXConcept mergeConcepts(ONDEXGraph graph,
			ONDEXConcept toKeep, ONDEXConcept toMerge)
			throws NullValueException, AccessDeniedException,
			EmptyStringException {
		for (ONDEXRelation r : graph.getRelationsOfConcept(toMerge)) {
			changeRelationVertex(graph, toMerge, toKeep, r);
		}

		copyConceptAttributes(toMerge, toKeep);
		graph.deleteConcept(toMerge.getId());
		return toKeep;
	}

	/**
	 * Creates an accession on concept only if it does not already exists
	 * 
	 * @param target
	 *            - relation
	 * @param newAcc
	 *            - attribute to create
	 * @throws AccessDeniedException
	 * @throws EmptyStringException
	 * @throws NullValueException
	 */
	public static void addNewAccession(ONDEXConcept target,
			ConceptAccession newAcc) throws NullValueException,
			EmptyStringException, AccessDeniedException {
		if (target.getConceptAccession(newAcc.getAccession(),
				newAcc.getElementOf()) == null)
			target.createConceptAccession(newAcc.getAccession(),
					newAcc.getElementOf(), true);
	}

	/**
	 * Creates an evidence type on concept only if it does not already exists
	 * 
	 * @param target
	 *            - relation
	 * @param newEv
	 *            - attribute to create
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 */
	public static void addNewEvidence(ONDEXConcept target, EvidenceType newEv)
			throws NullValueException, AccessDeniedException {
		if (!target.getEvidence().contains(newEv))
			target.addEvidenceType(newEv);
	}

	/**
	 * Creates an evidence type on relation only if it does not already exists
	 * 
	 * @param target
	 *            - relation
	 * @param newEv
	 *            - attribute to create
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 */
	public static void addNewEvidence(ONDEXRelation target, EvidenceType newEv)
			throws NullValueException, AccessDeniedException {
		if (!target.getEvidence().contains(newEv))
			target.addEvidenceType(newEv);
	}

	/**
	 * Creates a gds on concept only if it does not already exists
	 * 
	 * @param target
	 *            - relation
	 * @param newAttribute
	 *            - attribute to create
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 */
	public static void addNewAttribute(ONDEXConcept target,
			Attribute newAttribute) throws NullValueException,
			AccessDeniedException {
		if (target.getAttribute(newAttribute.getOfType()) == null)
			target.createAttribute(newAttribute.getOfType(),
					newAttribute.getValue(), newAttribute.isDoIndex());
	}

	/**
	 * Creates a gds on relation only if it does not already exists
	 * 
	 * @param target
	 *            - relation
	 * @param newAttribute
	 *            - attribute to create
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 */
	public static void addNewAttribute(ONDEXRelation target,
			Attribute newAttribute) throws NullValueException,
			AccessDeniedException {
		if (target.getAttribute(newAttribute.getOfType()) == null)
			target.createAttribute(newAttribute.getOfType(),
					newAttribute.getValue(), newAttribute.isDoIndex());
	}

	/**
	 * Creates a name on concept only if it does not already exists
	 * 
	 * @param target
	 *            - relation
	 * @param newName
	 *            - attribute to create
	 * @throws AccessDeniedException
	 * @throws EmptyStringException
	 * @throws NullValueException
	 */
	public static void addNewName(ONDEXConcept target, ConceptName newName)
			throws NullValueException, EmptyStringException,
			AccessDeniedException {
		if (target.getConceptName(newName.getName()) == null)
			target.createConceptName(newName.getName(), false);
	}

	/**
	 * Creates a new concept that has a specified concept class and is the same
	 * as the original one in all respects.
	 * 
	 * @param c
	 *            - source concept
	 * @param newClass
	 *            - concept class for the new concept
	 * @param graph
	 *            - graph
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 * @throws EmptyStringException
	 */
	public static void castConcept(ONDEXConcept c, ConceptClass newClass,
			ONDEXGraph graph) throws NullValueException, AccessDeniedException,
			EmptyStringException {
		Collection<EvidenceType> evidence = new ArrayList<EvidenceType>();
		for (EvidenceType et : c.getEvidence())
			evidence.add(et);

		ONDEXConcept nc = graph.getFactory().createConcept(c.getPID(),
				c.getElementOf(), newClass, evidence);

		for (ConceptAccession acc : c.getConceptAccessions()) {
			nc.createConceptAccession(acc.getAccession(), acc.getElementOf(),
					true);
		}

		for (ConceptName name : c.getConceptNames()) {
			nc.createConceptName(name.getName(), name.isPreferred());
		}

		for (Attribute attribute : c.getAttributes()) {
			nc.createAttribute(attribute.getOfType(), attribute.getValue(),
					attribute.isDoIndex());
		}

		if (c.getAnnotation() != null)
			nc.setAnnotation(c.getAnnotation());

		if (c.getDescription() != null)
			nc.setDescription(c.getDescription());

		for (ONDEXConcept co : c.getTags())
			nc.addTag(co);

		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			ONDEXRelation nr = null;

			evidence = new ArrayList<EvidenceType>();
			for (EvidenceType et : r.getEvidence())
				evidence.add(et);

			if (r.getFromConcept().equals(c) && r.getToConcept().equals(c)) {
				nr = graph.createRelation(nc, nc, r.getOfType(), evidence);
			} else {
				if (r.getFromConcept().equals(c)) {
					nr = graph.createRelation(nc, r.getToConcept(),
							r.getOfType(), evidence);
				} else {
					nr = graph.createRelation(r.getFromConcept(), nc,
							r.getOfType(), evidence);
				}
			}

			for (ONDEXConcept co : r.getTags())
				nr.addTag(co);

			for (Attribute rattribute : r.getAttributes()) {
				nr.createAttribute(rattribute.getOfType(),
						rattribute.getValue(), rattribute.isDoIndex());
			}
		}
		graph.deleteConcept(c.getId());
	}

	/**
	 * Changes the type set of the relation from the one specified to the new
	 * one
	 * 
	 * @param graph
	 *            - graph where the relation is
	 * @param r
	 *            - relation to change type of
	 * @param newTypeSet
	 *            - new type set
	 * @param switchDirection
	 * @return true if the relation was successfully create, false if not. The
	 *         creation will fail if there already is a relation of the new type
	 *         & same direction between the source and target concepts
	 */
	public static boolean castRelation(ONDEXGraph graph, ONDEXRelation r,
			RelationType newTypeSet, boolean switchDirection) {
		ONDEXRelation nr = null;
		ONDEXConcept source = r.getFromConcept();
		ONDEXConcept target = r.getToConcept();
		ONDEXRelation test = null;
		if (!switchDirection) {
			test = graph.getRelation(source, target, newTypeSet);
		} else {
			test = graph.getRelation(target, source, newTypeSet);
		}
		if (test != null) {
			return false;
		}

		Collection<EvidenceType> evidence = new ArrayList<EvidenceType>();

		for (EvidenceType et : r.getEvidence()) {
			evidence.add(et);
		}

		EvidenceType first = evidence.iterator().next();
		evidence.remove(first);

		if (!switchDirection) { // todo: I swapped this from
								// "switchDirection = false" as it looked like a
								// typo
			nr = graph.getFactory().createRelation(source, target, newTypeSet,
					first);
		} else {
			nr = graph.getFactory().createRelation(target, source, newTypeSet,
					first);
		}

		for (EvidenceType et : evidence) {
			nr.addEvidenceType(et);
		}

		for (ONDEXConcept co : r.getTags())
			nr.addTag(co);

		for (Attribute rattribute : r.getAttributes()) {
			nr.createAttribute(rattribute.getOfType(), rattribute.getValue(),
					rattribute.isDoIndex());
		}

		graph.deleteRelation(r.getId());
		return true;
	}

	public static void changeAttributeValue(ONDEXEntity e, AttributeName n,
			Object value) {
		if (e instanceof ONDEXConcept) {
			ONDEXConcept c = (ONDEXConcept) e;
			boolean doIndex = c.getAttribute(n).isDoIndex();
			c.deleteAttribute(n);
			c.createAttribute(n, value, doIndex);
		}
		if (e instanceof ONDEXRelation) {
			ONDEXRelation r = (ONDEXRelation) e;
			boolean doIndex = r.getAttribute(n).isDoIndex();
			r.deleteAttribute(n);
			r.createAttribute(n, value, doIndex);
		} else {
			throw new IllegalArgumentException(
					"This method only works with Ondex concepts and relations.");
		}
	}

	/**
	 * This method will create a copy of a relation and change its either its
	 * source or target vertex, and will create a copy and delete the original
	 * 
	 * @param graph
	 *            - graph to which the relation belongs
	 * @param original
	 *            - original concept associated with the relation (source or
	 *            target)
	 * @param copyTarget
	 *            - concept that will replace the original one
	 * @param r
	 *            - relation to copy
	 * @throws NullValueException
	 * @throws AccessDeniedException
	 */
	public static ONDEXRelation changeRelationVertex(ONDEXGraph graph,
			ONDEXConcept original, ONDEXConcept copyTarget, ONDEXRelation r)
			throws NullValueException, AccessDeniedException {
		ONDEXRelation nr = null;
		Collection<EvidenceType> evidence = r.getEvidence();
		Collection<ONDEXConcept> tags = new HashSet<ONDEXConcept>(r.getTags());
		Collection<Attribute> rgdss = r.getAttributes();
		if (r.getFromConcept().equals(original)
				&& r.getToConcept().equals(original)) {
			nr = graph.getRelation(copyTarget, copyTarget, r.getOfType());
			if (nr == null)
				nr = graph.createRelation(copyTarget, copyTarget,
						r.getOfType(), evidence);
		} else {
			if (r.getFromConcept().equals(original)) {
				nr = graph.getRelation(copyTarget, r.getToConcept(),
						r.getOfType());
				if (nr == null)
					nr = graph.createRelation(copyTarget, r.getToConcept(),
							r.getOfType(), evidence);
			} else {
				nr = graph.getRelation(r.getFromConcept(), copyTarget,
						r.getOfType());
				if (nr == null)
					nr = graph.createRelation(r.getFromConcept(), copyTarget,
							r.getOfType(), evidence);
			}
		}

		for (ONDEXConcept tag : tags) {
			if (!nr.getTags().contains(tag))
				nr.addTag(tag);
		}

		for (Attribute rattribute : rgdss) {
			if (nr.getAttribute(rattribute.getOfType()) == null)
				nr.createAttribute(rattribute.getOfType(),
						rattribute.getValue(), rattribute.isDoIndex());
		}
		return nr;
	}

	/**
	 * Makes concept into an attribute with the value of concept name on all of
	 * the concepts connected to it via relations of specified type set. Use
	 * only with non-directional relation types.
	 * 
	 * @param c
	 *            - defining concept
	 * @param attName
	 *            - name to use for the attribute
	 * @param graph
	 *            - graph
	 * @param rtss
	 *            - valid set of relation type sets
	 * @throws EmptyStringException
	 * @throws NullValueException
	 */
	public static void conceptToAttribute(ONDEXConcept c, String attName,
			ONDEXGraph graph, RelationType... rtss) throws NullValueException,
			EmptyStringException {
		ONDEXGraphMetaData data = graph.getMetaData();
		AttributeName an = data.getAttributeName(attName);
		if (an == null)
			an = data.getFactory().createAttributeName(attName, String.class);
		Set<RelationType> ts = new HashSet<RelationType>(Arrays.asList(rtss));
		List<ONDEXConcept> cluster = new ArrayList<ONDEXConcept>();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (ts.contains(r.getOfType())) {
				if (c.equals(r.getFromConcept()) && c.equals(r.getToConcept()))
					continue;
				if (c.equals(r.getFromConcept()))
					cluster.add(r.getToConcept());
				else if (c.equals(r.getToConcept()))
					cluster.add(r.getFromConcept());
			}
		}
		String value = c.getConceptName().getName();
		for (ONDEXConcept d : cluster) {
			d.createAttribute(an, value, false);
		}
		graph.deleteConcept(c.getId());
	}

	/**
	 * Swaps hub concept for a cluster where every concept is connected to every
	 * other concept by relation of specified type set. Use only with
	 * non-directional relation types
	 * 
	 * @param c
	 *            - hub concept
	 * @param clusterRel
	 *            - relation type set to use in the cluster
	 * @param graph
	 *            - graph
	 * @param rtss
	 *            - relation type set() that define the cluster
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 */
	public static void conceptToCluster(ONDEXConcept c,
			RelationType clusterRel, ONDEXGraph graph, RelationType... rtss)
			throws NullValueException, AccessDeniedException {
		Set<RelationType> ts = new HashSet<RelationType>(Arrays.asList(rtss));
		List<ONDEXConcept> cluster = new ArrayList<ONDEXConcept>();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (ts.contains(r.getOfType())) {
				if (c.equals(r.getFromConcept()) && c.equals(r.getToConcept()))
					continue;
				if (c.equals(r.getFromConcept()))
					cluster.add(r.getToConcept());
				else if (c.equals(r.getToConcept()))
					cluster.add(r.getFromConcept());
			}
		}

		Collection<EvidenceType> evidence = new ArrayList<EvidenceType>();
		for (EvidenceType et : c.getEvidence())
			evidence.add(et);

		for (int i = 0; i < cluster.size(); i++) {
			for (int j = i + 1; j < cluster.size(); j++) {
				graph.createRelation(cluster.get(i), cluster.get(j),
						clusterRel, evidence);
			}
		}
		graph.deleteConcept(c.getId());
	}

	/**
	 * This method will create a copy of the original relation between the
	 * concepts specified.
	 * 
	 * @param graph
	 *            - graph where this things live
	 * @param from
	 *            - source concept of the copied relation
	 * @param to
	 *            - target concept of the copied relation
	 * @param r
	 *            - relation that will be copied
	 */
	public static void copyRelation(ONDEXGraph graph, ONDEXConcept from,
			ONDEXConcept to, ONDEXRelation r) {
		ONDEXRelation nr = null;
		Collection<EvidenceType> evidence = r.getEvidence();
		Collection<ONDEXConcept> tags = new HashSet<ONDEXConcept>(r.getTags());
		Collection<Attribute> rgdss = r.getAttributes();

		nr = graph.createRelation(from, to, r.getOfType(), evidence);

		for (ONDEXConcept tag : tags)
			nr.addTag(tag);

		for (Attribute rattribute : rgdss) {
			cloneAttributeWithAttName(graph, r, nr, rattribute.getOfType());
		}
	}

	public static void copyRelations(ONDEXGraph graph, ONDEXConcept copySource,
			ONDEXConcept copyTarget) {
		for (ONDEXRelation r : graph.getRelationsOfConcept(copySource)) {
			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();
			if (from.equals(copySource)) {
				from = copyTarget;
			} else {
				to = copyTarget;
			}
			copyRelation(graph, from, to, r);
		}
	}
}
