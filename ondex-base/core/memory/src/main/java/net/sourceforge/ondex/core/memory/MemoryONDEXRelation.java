package net.sourceforge.ondex.core.memory;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.AbstractRelation;
import net.sourceforge.ondex.event.type.DuplicatedEntryEvent;

/**
 * This class represents a pure memory based implementation of AbstractRelation.
 * It uses standard JAVA datatypes.
 * 
 * @author taubertj
 * @author Matthew Pocock
 */
public class MemoryONDEXRelation extends AbstractRelation implements
		Comparable<MemoryONDEXRelation> {

	// serial version id
	private static final long serialVersionUID = 1L;

	// relation attribute associated with this relation
	private final BidiMap<AttributeName, Attribute> attributes;

	/**
	 * parent graph
	 */
	protected transient MemoryONDEXGraph graph;

	/**
	 * Constructor which fills all fields of this class.
	 * 
	 * @param sid
	 *            unique id
	 * @param graph
	 *            MemoryONDEXGraph
	 * @param id
	 *            Integer
	 * @param fromConcept
	 *            startpoint
	 * @param toConcept
	 *            endpoint
	 * @param ofType
	 *            specifies Relation Type
	 */
	protected MemoryONDEXRelation(long sid, MemoryONDEXGraph graph, int id,
			ONDEXConcept fromConcept, ONDEXConcept toConcept,
			RelationType ofType) {
		super(sid, id, fromConcept, toConcept, ofType);
		this.graph = graph;

		// initialise new relation specific data structures
		attributes = new DualHashBidiMap<AttributeName, Attribute>();
	}

	@Override
	public int compareTo(MemoryONDEXRelation o) {
		return (getId() < o.getId() ? -1 : (getId() == o.getId() ? 0 : 1));
	}

	@Override
	protected boolean dropEvidenceType(EvidenceType evidenceType) {
		// evidence types are held centrally by the graph
		graph.relationToEvidence.get(this).remove(evidenceType);
		Set<ONDEXRelation> set = graph.evidenceTypeToRelations
				.get(evidenceType);
		if (set != null)
			return set.remove(this);

		// nothing found
		return false;
	}

	@Override
	protected boolean dropTag(ONDEXConcept concept) {
		return graph.relationToTags.get(this).remove(concept)
				&& graph.tagToRelations.get(concept).remove(this);
	}

	@Override
	protected boolean removeRelationAttribute(AttributeName attributeName) {
		Set<ONDEXRelation> existingAttribute = graph.attributeNameToRelations
				.get(attributeName);
		if (existingAttribute != null) {
			existingAttribute.remove(this);
		}
		return attributes.remove(attributeName) != null;
	}

	@Override
	protected Set<EvidenceType> retrieveEvidenceTypeAll() {
		// will be wrapped as UnmodifiableSet in base
		return graph.relationToEvidence.get(this);
	}

	@Override
	protected Attribute retrieveRelationAttribute(AttributeName attributeName) {
		return attributes.get(attributeName);
	}

	@Override
	protected Set<Attribute> retrieveRelationAttributeAll() {
		// will be wrapped as UnmodifiableSet in base
		return attributes.values();
	}

	@Override
	protected Set<ONDEXConcept> retrieveTagAll() {
		// will be wrapped as UnmodifiableSet in base
		return graph.relationToTags.get(this);
	}

	@Override
	protected void saveEvidenceType(EvidenceType evidenceType) 
	{
		graph.evidenceTypeToRelations
		.computeIfAbsent ( evidenceType, et -> new HashSet<> () )
		.add ( this );
		
		graph.relationToEvidence.get(this).add(evidenceType);
	}

	@Override
	protected void saveTag(ONDEXConcept tag) 
	{
		// associate tag with this relation
		graph.tagToRelations
		.computeIfAbsent ( tag, tg -> new HashSet<> () )
		.add ( this );
		
		// associate this relation with tag
		graph.relationToTags
		.computeIfAbsent ( this, r -> new HashSet<> () )
		.add ( tag );
	}

	@Override
	protected Attribute storeRelationAttribute(Attribute attribute) {
		AttributeName an = attribute.getOfType();

		// check if attribute already exists
		Attribute existing = attributes.put(an, attribute);

		// complain about duplicates
		if (existing != null) {
			graph.fireEventOccurred(new DuplicatedEntryEvent(Config.properties
					.getProperty("memory.Relation.DuplicatedRelationAttribute")
					+ attribute.getOfType().getId(),
					"[Relation - storeRelationAttribute]"));
		}

		// store it in central index
		graph.attributeNameToRelations
		.computeIfAbsent ( an, _an -> new HashSet<> () )
		.add ( this );
		
		return attribute;
	}

}
