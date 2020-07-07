package net.sourceforge.ondex.ovtk2.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EntityFactory;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

/**
 * Class that wraps an ONDEXGraph into a JUNG graph representation.
 * 
 * @author taubertj
 * @author Matthew Pocock
 */
public class ONDEXJUNGGraph extends JUNGGraphAdapter {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -4153642887688523779L;

	/**
	 * wrapped ONDEXGraph
	 */
	private final ONDEXGraph og;

	/**
	 * for saving graph annotations
	 */
	protected final Map<String, String> annotations = new HashMap<String, String>();

	/**
	 * Sets wrapped ONDEXGraph.
	 * 
	 * @param graph
	 *            wrapped ONDEXGraph
	 */
	public ONDEXJUNGGraph(ONDEXGraph graph) {
		this.og = graph;
	}

	/**
	 * @return the annotations
	 */
	public Map<String, String> getAnnotations() {
		return annotations;
	}

	/**
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXAssociable#getSID()
	 */
	public long getSID() {
		return og.getSID();
	}

	/**
	 * @param pid
	 * @param annotation
	 * @param description
	 * @param elementOf
	 * @param ofType
	 * @param evidence
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#createConcept(java.lang.String,
	 *      java.lang.String, java.lang.String,
	 *      net.sourceforge.ondex.core.DataSource,
	 *      net.sourceforge.ondex.core.ConceptClass, java.util.Collection)
	 */
	public ONDEXConcept createConcept(String pid, String annotation, String description, DataSource elementOf, ConceptClass ofType, Collection<EvidenceType> evidence) {
		return og.createConcept(pid, annotation, description, elementOf, ofType, evidence);
	}

	/**
	 * @param fromConcept
	 * @param toConcept
	 * @param ofType
	 * @param evidence
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#createRelation(net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.RelationType, java.util.Collection)
	 */
	public ONDEXRelation createRelation(ONDEXConcept fromConcept, ONDEXConcept toConcept, RelationType ofType, Collection<EvidenceType> evidence) {
		return og.createRelation(fromConcept, toConcept, ofType, evidence);
	}

	/**
	 * @param id
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#deleteConcept(int)
	 */
	public boolean deleteConcept(int id) {
		return og.deleteConcept(id);
	}

	/**
	 * @param id
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#deleteRelation(int)
	 */
	public boolean deleteRelation(int id) {
		return og.deleteRelation(id);
	}

	/**
	 * @param fromConcept
	 * @param toConcept
	 * @param ofType
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#deleteRelation(net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.RelationType)
	 */
	public boolean deleteRelation(ONDEXConcept fromConcept, ONDEXConcept toConcept, RelationType ofType) {
		return og.deleteRelation(fromConcept, toConcept, ofType);
	}

	/**
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getAllTags()
	 */
	public Set<ONDEXConcept> getAllTags() {
		return og.getAllTags();
	}

	/**
	 * @param id
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConcept(int)
	 */
	public ONDEXConcept getConcept(int id) {
		return og.getConcept(id);
	}

	/**
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConcepts()
	 */
	public Set<ONDEXConcept> getConcepts() {
		return og.getConcepts();
	}

	/**
	 * @param an
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfAttributeName(net.sourceforge.ondex.core.AttributeName)
	 */
	public Set<ONDEXConcept> getConceptsOfAttributeName(AttributeName an) {
		return og.getConceptsOfAttributeName(an);
	}

	/**
	 * @param cc
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfConceptClass(net.sourceforge.ondex.core.ConceptClass)
	 */
	public Set<ONDEXConcept> getConceptsOfConceptClass(ConceptClass cc) {
		return og.getConceptsOfConceptClass(cc);
	}

	/**
	 * @param dataSource
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfDataSource(net.sourceforge.ondex.core.DataSource)
	 */
	public Set<ONDEXConcept> getConceptsOfDataSource(DataSource dataSource) {
		return og.getConceptsOfDataSource(dataSource);
	}

	/**
	 * @param et
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfEvidenceType(net.sourceforge.ondex.core.EvidenceType)
	 */
	public Set<ONDEXConcept> getConceptsOfEvidenceType(EvidenceType et) {
		return og.getConceptsOfEvidenceType(et);
	}

	/**
	 * @param ac
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfTag(net.sourceforge.ondex.core.ONDEXConcept)
	 */
	public Set<ONDEXConcept> getConceptsOfTag(ONDEXConcept ac) {
		return og.getConceptsOfTag(ac);
	}

	/**
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getFactory()
	 */
	public EntityFactory getFactory() {
		return og.getFactory();
	}

	/**
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getMetaData()
	 */
	public ONDEXGraphMetaData getMetaData() {
		return og.getMetaData();
	}

	/**
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getName()
	 */
	public String getName() {
		return og.getName();
	}

	/**
	 * @param id
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelation(int)
	 */
	public ONDEXRelation getRelation(int id) {
		return og.getRelation(id);
	}

	/**
	 * @param fromConcept
	 * @param toConcept
	 * @param ofType
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelation(net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.RelationType)
	 */
	public ONDEXRelation getRelation(ONDEXConcept fromConcept, ONDEXConcept toConcept, RelationType ofType) {
		return og.getRelation(fromConcept, toConcept, ofType);
	}

	/**
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelations()
	 */
	public Set<ONDEXRelation> getRelations() {
		return og.getRelations();
	}

	/**
	 * @param an
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfAttributeName(net.sourceforge.ondex.core.AttributeName)
	 */
	public Set<ONDEXRelation> getRelationsOfAttributeName(AttributeName an) {
		return og.getRelationsOfAttributeName(an);
	}

	/**
	 * @param concept
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfConcept(net.sourceforge.ondex.core.ONDEXConcept)
	 */
	public Set<ONDEXRelation> getRelationsOfConcept(ONDEXConcept concept) {
		return og.getRelationsOfConcept(concept);
	}

	/**
	 * @param cc
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfConceptClass(net.sourceforge.ondex.core.ConceptClass)
	 */
	public Set<ONDEXRelation> getRelationsOfConceptClass(ConceptClass cc) {
		return og.getRelationsOfConceptClass(cc);
	}

	/**
	 * @param dataSource
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfDataSource(net.sourceforge.ondex.core.DataSource)
	 */
	public Set<ONDEXRelation> getRelationsOfDataSource(DataSource dataSource) {
		return og.getRelationsOfDataSource(dataSource);
	}

	/**
	 * @param et
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfEvidenceType(net.sourceforge.ondex.core.EvidenceType)
	 */
	public Set<ONDEXRelation> getRelationsOfEvidenceType(EvidenceType et) {
		return og.getRelationsOfEvidenceType(et);
	}

	/**
	 * @param rt
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfRelationType(net.sourceforge.ondex.core.RelationType)
	 */
	public Set<ONDEXRelation> getRelationsOfRelationType(RelationType rt) {
		return og.getRelationsOfRelationType(rt);
	}

	/**
	 * @param ac
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfTag(net.sourceforge.ondex.core.ONDEXConcept)
	 */
	public Set<ONDEXRelation> getRelationsOfTag(ONDEXConcept ac) {
		return og.getRelationsOfTag(ac);
	}

	/**
	 * @return
	 * @see net.sourceforge.ondex.core.ONDEXGraph#isReadOnly()
	 */
	public boolean isReadOnly() {
		return og.isReadOnly();
	}

}
