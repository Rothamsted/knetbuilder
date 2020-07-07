package net.sourceforge.ondex.parser.oxl;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.export.oxl.RefManager;
import net.sourceforge.ondex.export.oxl.XMLTagNames;

/**
 * This class parses ONDEX Relation tags and stores them in a ONDEX graph. It
 * handeles XML ONDEX Relation tags.
 * 
 * @author sierenk
 */
public class RelationParser extends AbstractEntityParser {
	private RefManager<String, RelationType> rtRefManager;

	// contains mapping of old to new integer ids
	private Map<Integer, Integer> idMapping;

	// number of current relations parsed
	private int progress = 0;

	/**
	 * Creates a parser for Relation elements.
	 * 
	 * @param og
	 *            ONDEXGraph for storing parsed concepts.
	 * @param idOldNew
	 *            mapping of XML IDs to ONDEX IDs for Concepts.
	 */
	public RelationParser(final ONDEXGraph og, Map<Integer, Integer> idOldNew)
			throws JAXBException {
		super(og);
		this.idMapping = idOldNew;

		this.rtRefManager = new RefManager<String, RelationType>() {
			@Override
			protected RelationType resolveExternally(String id) {
				return og.getMetaData().getRelationType(id);
			}
		};
	}

	/**
	 * Returns progress of parsing.
	 * 
	 * @return number of relations parsed
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * Returns the name of this XMLComponentParser.
	 * 
	 * @return String
	 */
	public String getName() {
		return "RelationParser";
	}

	/**
	 * Parses ONDEX Relation tag.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @throws XMLStreamException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws JAXBException
	 * @throws InconsistencyException
	 */
	public void parse(XMLStreamReader xmlr) throws XMLStreamException,
			JAXBException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InconsistencyException {

		progress++;

		// read fromConcept
		xmlr.nextTag(); // fromConcept
		int fromConceptS = Integer.parseInt(xmlr.getElementText());
		int fromId = idMapping.get(fromConceptS);
		ONDEXConcept fromConcept = og.getConcept(fromId);
		if (fromConcept == null)
			throw new InconsistencyException(
					"Something went wrong getting fromConcept for XML ID "
							+ fromConceptS + " mapped to ONDEX ID " + fromId);

		// read toConcept
		xmlr.nextTag(); // toConcept
		int toConceptS = Integer.parseInt(xmlr.getElementText());
		int toId = idMapping.get(toConceptS);
		ONDEXConcept toConcept = og.getConcept(toId);
		if (toConcept == null)
			throw new InconsistencyException(
					"Something went wrong getting toConcept for XML ID "
							+ toConceptS + " mapped to ONDEX ID " + toId);

		// qualifier or ofTypeSet
		xmlr.nextTag();
		int eventType = xmlr.getEventType();

		ONDEXConcept qualifier = null;
		if (eventType == XMLStreamConstants.START_ELEMENT
				&& xmlr.getLocalName().equals(XMLTagNames.QUALIFIER)) {

			// read qualifier
			int qualifierS = Integer.parseInt(xmlr.getElementText());
			int qualId = idMapping.get(qualifierS); // get qualifier concept
			qualifier = og.getConcept(qualId);
			if (qualifier == null)
				throw new InconsistencyException(
						"Something went wrong getting qualifierConcept for XML ID "
								+ qualifierS + " mapped to ONDEX ID " + qualId);

			ONDEXEventHandler.getEventHandlerForSID(og.getSID())
					.fireEventOccurred(
							new GeneralOutputEvent("Qualifier concept "
									+ qualifier + " ignored.",
									"[RelationParser - parse]"));

			xmlr.nextTag(); // ofTypeSet
		}

		// read ofType Content
		RelationType rt = parseRelationTypeContent(xmlr).get();
		xmlr.nextTag(); // end tag ofTypeSet

		xmlr.nextTag(); // start evidences
		Collection<EvidenceType> evidences = parseEvidences(xmlr);
		xmlr.nextTag(); // end evidences

		if (evidences == null || evidences.size() == 0)
			throw new InconsistencyException(
					"An evidence type is missing in the XML at Relation "
							+ fromConcept.getPID() + " to "
							+ toConcept.getPID() + " ofType" + rt.getId()
							+ " can not continue with relation.");

		// create Relation
		ONDEXRelation r = og.createRelation(fromConcept, toConcept, rt,
				evidences);

		xmlr.nextTag(); // relgds

		parseAttributes(xmlr, r);

		xmlr.nextTag(); // end relgds

		eventType = xmlr.getEventType();

		// fill context
		if (eventType == XMLStreamConstants.START_ELEMENT
				&& xmlr.getLocalName().equals(XMLTagNames.CONTEXT)) {
			String[] list = xmlr.getElementText().split(",");
			for (String number : list) {
				r.addTag(og.getConcept(idMapping.get(Integer.parseInt(number))));
			}

			xmlr.nextTag(); // skip end tag
		}
		// fill context new way
		if (eventType == XMLStreamConstants.START_ELEMENT
				&& xmlr.getLocalName().equals(XMLTagNames.CONTEXTS)) {
			parseContext(xmlr, r);
		}
	}

	/**
	 * Adds context to a relation
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @param r
	 *            ONDEXRelation to add context to
	 * @throws XMLStreamException
	 */
	protected void parseContext(XMLStreamReader xmlr, ONDEXRelation r)
			throws XMLStreamException {
		int eventType = xmlr.getEventType();

		while (xmlr.hasNext() && eventType != XMLStreamConstants.END_ELEMENT) {

			if (eventType == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.CONTEXT)) {
				// example tags
				// <id>12</id>

				xmlr.nextTag(); // id
				int number = Integer.parseInt(xmlr.getElementText());

				// add context to data repository ...
				r.addTag(og.getConcept(idMapping.get(number)));

				xmlr.nextTag(); // skip end tag
			}
			eventType = xmlr.next();
		}
	}

	/**
	 * Parses a relation_type tag and returns a RelationType object.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @return RelationType
	 * @throws XMLStreamException
	 */
	protected RefManager.Ref<RelationType> parseRelationTypeContent(
			final XMLStreamReader xmlr) throws XMLStreamException {

		// for backward compatibility with 1.2
		if (xmlr.getName().getLocalPart().equals("ofTypeSet")) {
			// rtset id
			xmlr.nextTag();
			xmlr.getElementText();

			// rtset fullname
			xmlr.nextTag();
			xmlr.getElementText();

			// rtset description
			xmlr.nextTag();
			xmlr.getElementText();

			// skip to first relation type definition
			xmlr.nextTag();
			xmlr.nextTag();
		}

		xmlr.nextTag(); // name
		final String nameS = checkForSpace(xmlr.getElementText());
		final RefManager.Ref<RelationType> rtRef = rtRefManager.makeRef(nameS);

		if (xmlr.getLocalName().equals(XMLTagNames.ID_REF)) {
			// nothing else to do
		} else {

			xmlr.nextTag(); // fullname
			String fullname = xmlr.getElementText();

			xmlr.nextTag(); // description
			String descriptionS = xmlr.getElementText();

			xmlr.nextTag(); // inverseName
			String inverseNameS = xmlr.getElementText();

			xmlr.nextTag(); // isAntisymmetric

			String isAntisymmetricS = xmlr.getElementText();
			boolean isAntisymmetric = Boolean.parseBoolean(isAntisymmetricS);

			xmlr.nextTag(); // isReflexive

			String isReflexiveS = xmlr.getElementText();
			boolean isReflexive = Boolean.parseBoolean(isReflexiveS);

			xmlr.nextTag(); // isSymmetric

			String isSymmetricS = xmlr.getElementText();
			boolean isSymmetric = Boolean.parseBoolean(isSymmetricS);

			xmlr.nextTag(); // isTransitive

			String isTransitiveS = xmlr.getElementText();
			boolean isTransitive = Boolean.parseBoolean(isTransitiveS);

			xmlr.nextTag(); // specialisationOf or end tag reltype

			if (!og.getMetaData().checkRelationType(nameS)) {
				RelationType rt = og
						.getMetaData()
						.getFactory()
						.createRelationType(nameS, fullname, descriptionS,
								inverseNameS, isAntisymmetric, isReflexive,
								isSymmetric, isTransitive);
				// System.err.println("Resolving " + nameS);
				rtRefManager.resolve(nameS, rt);
			} else {
				boolean update = false;
				RelationType rt = og.getMetaData().getRelationType(nameS);
				if ((rt.getFullname() == null || rt.getFullname().equals(""))
						&& (fullname != null && fullname.trim().equals(""))) {
					rt.setFullname(fullname);
					update = true;
				}
				if ((rt.getDescription() == null || rt.getDescription().equals(
						""))
						&& (descriptionS != null && descriptionS.trim().equals(
								""))) {
					rt.setDescription(descriptionS);
					update = true;
				}
				if ((rt.getInverseName() == null || rt.getInverseName().equals(
						""))
						&& (inverseNameS != null && inverseNameS.trim().equals(
								""))) {
					rt.setInverseName(inverseNameS);
					update = true;
				}
				if (update) {
					rt.setAntisymmetric(isAntisymmetric);
					rt.setReflexive(isReflexive);
					rt.setSymmetric(isSymmetric);
					rt.setTransitiv(isTransitive);
				}

			}

			// start optional specialisationOf tag
			if (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.SPECIALISATIONOF)) {

				parseRelationTypeContent(xmlr)
						.setCompletionAction(
								new RefManager.OnCompletion<net.sourceforge.ondex.core.RelationType>() {
									@Override
									public void onCompletion(
											RelationType relationType) {
										// System.err.println("Responding to completion of "
										// + relationType +
										// " by setting specialisationOf on " +
										// nameS);
										rtRef.get().setSpecialisationOf(
												relationType);
									}
								});

				xmlr.nextTag(); // end tag specOf or end tag relation_type

			}
			// end optional
		}
		return rtRef;
	}

}
