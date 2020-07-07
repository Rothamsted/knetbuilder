package net.sourceforge.ondex.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.NullValueEvent;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * Factory class to provide method stubs for concept or relation creation.
 * 
 * @author sckuo
 */
public final class EntityFactory
    implements Serializable {

	/**
	 * contained Ondex graph
	 */
	private final ONDEXGraph g;

	/**
	 * Constructor for actual implementation of ONDEXGraph.
	 * 
	 * @param g
	 */
	public EntityFactory(ONDEXGraph g) {
		this.g = g;
	}

	/**
	 * Creates a new ONDEXConcept with the given pid, DataSource and
	 * ConceptClass. Adds the new ONDEXConcept to the list of Concepts of this
	 * graph and returns it.
	 * 
	 * @param pid
	 *            PARSER ID of the new ONDEXConcept
	 * @param elementOf
	 *            DataSource of the new ONDEXConcept
	 * @param ofType
	 *            ConceptClass of the new ONDEXConcept
	 * @param evidence
	 *            evidence types
	 * @return new ONDEXConcept
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ONDEXConcept createConcept(String id, DataSource elementOf,
			ConceptClass ofType, Collection<EvidenceType> evidence)
			throws NullValueException, UnsupportedOperationException {
		return g.createConcept(id, "", "", elementOf, ofType, evidence);
	}

	/**
	 * Creates a new ONDEXConcept with the given pid, DataSource and
	 * ConceptClass. Adds the new ONDEXConcept to the list of Concepts of this
	 * graph and returns it.
	 * 
	 * @param pid
	 *            PARSER ID of the new ONDEXConcept
	 * @param elementOf
	 *            DataSource of the new ONDEXConcept
	 * @param ofType
	 *            ConceptClass of the new ONDEXConcept
	 * @param evidencetype
	 *            evidence type
	 * @return new ONDEXConcept
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ONDEXConcept createConcept(String id, DataSource elementOf,
			ConceptClass ofType, EvidenceType evidencetype)
			throws NullValueException, UnsupportedOperationException {
		if (evidencetype == null) {
			fireEventOccurred(new NullValueEvent(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptEvidenceTypeNull"),
					"[EntityFactory - createConcept]"));
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptEvidenceTypeNull"));
		} else {
			List<EvidenceType> v = new ArrayList<EvidenceType>();
			v.add(evidencetype);
			return g.createConcept(id, "", "", elementOf, ofType, v);
		}
	}

	/**
	 * Creates a new ONDEXConcept with the given pid, annotation, DataSource and
	 * ConceptClass. Adds the new ONDEXConcept to the list of Concepts of this
	 * graph and returns it.
	 * 
	 * @param pid
	 *            PARSER ID of the new ONDEXConcept
	 * @param annotation
	 *            relevant annotation of the new ONDEXConcept
	 * @param elementOf
	 *            DataSource of the new ONDEXConcept
	 * @param ofType
	 *            ConceptClass of the new ONDEXConcept
	 * @param evidence
	 *            evidence types
	 * @return new ONDEXConcept
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ONDEXConcept createConcept(String id, String annotation,
			DataSource elementOf, ConceptClass ofType,
			Collection<EvidenceType> evidence) throws NullValueException,
			UnsupportedOperationException {
		return g.createConcept(id, annotation, "", elementOf, ofType, evidence);
	}

	/**
	 * Creates a new ONDEXConcept with the given pid, DataSource and
	 * ConceptClass. Adds the new ONDEXConcept to the list of Concepts of this
	 * graph and returns it.
	 * 
	 * @param pid
	 *            PARSER ID of the new ONDEXConcept
	 * @param annotation
	 *            relevant annotation of the new ONDEXConcept
	 * @param elementOf
	 *            DataSource of the new ONDEXConcept
	 * @param ofType
	 *            ConceptClass of the new ONDEXConcept
	 * @param evidencetype
	 *            evidence type
	 * @return new ONDEXConcept
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ONDEXConcept createConcept(String id, String annotation,
			DataSource elementOf, ConceptClass ofType, EvidenceType evidencetype)
			throws NullValueException, UnsupportedOperationException {
		if (evidencetype == null) {
			fireEventOccurred(new NullValueEvent(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptEvidenceTypeNull"),
					"[EntityFactory - createConcept]"));
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptEvidenceTypeNull"));
		} else {
			List<EvidenceType> v = new ArrayList<EvidenceType>();
			v.add(evidencetype);
			return g.createConcept(id, annotation, "", elementOf, ofType, v);
		}
	}

	/**
	 * Creates a new ONDEXConcept with the given pid, DataSource and
	 * ConceptClass. Adds the new ONDEXConcept to the list of Concepts of this
	 * graph and returns it.
	 * 
	 * @param pid
	 *            PARSER ID of the new ONDEXConcept
	 * @param annotation
	 *            relevant annotation of the new ONDEXConcept
	 * @param description
	 *            other descriptions of the new ONDEXConcept
	 * @param elementOf
	 *            DataSource of the new ONDEXConcept
	 * @param ofType
	 *            ConceptClass of the new ONDEXConcept
	 * @param evidencetype
	 *            evidence type
	 * @return new ONDEXConcept
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ONDEXConcept createConcept(String id, String annotation,
			String description, DataSource elementOf, ConceptClass ofType,
			EvidenceType evidencetype) throws NullValueException,
			UnsupportedOperationException {
		if (evidencetype == null) {
			fireEventOccurred(new NullValueEvent(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptEvidenceTypeNull"),
					"[EntityFactory - createConcept]"));
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptEvidenceTypeNull"));
		} else {
			List<EvidenceType> v = new ArrayList<EvidenceType>();
			v.add(evidencetype);
			return g.createConcept(id, annotation, description, elementOf,
					ofType, v);
		}
	}

	/**
	 * Creates a new ONDEXRelation with the given fromConcept, toConcept, ofType
	 * and a collection of EvidenceTypes. Adds the new created ONDEXRelation to
	 * the list of Relations of this graph.
	 * 
	 * @param fromConcept
	 *            from ONDEXConcept for the new ONDEXRelation
	 * @param toConcept
	 *            to ONDEXConcept for the new ONDEXRelation
	 * @param ofType
	 *            RelationType for the new ONDEXRelation
	 * @param evidencetype
	 *            evidence type
	 * @return new ONDEXRelation
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ONDEXRelation createRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType,
			EvidenceType evidencetype) throws NullValueException,
			UnsupportedOperationException {
		if (evidencetype == null) {
			fireEventOccurred(new NullValueEvent(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationEvidenceTypeNull"),
					"[EntityFactory - createRelation]"));
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationEvidenceTypeNull"));
		} else {
			List<EvidenceType> v = new ArrayList<EvidenceType>(1);
			v.add(evidencetype);
			return g.createRelation(fromConcept, toConcept, ofType, v);
		}
	}

	/**
	 * Propagate event e to the right graph listeners.
	 * 
	 * @param e
	 *            EventType
	 */
	private void fireEventOccurred(EventType e) {
		ONDEXEventHandler.getEventHandlerForSID(g.getSID())
				.fireEventOccurred(e);
	}

}
