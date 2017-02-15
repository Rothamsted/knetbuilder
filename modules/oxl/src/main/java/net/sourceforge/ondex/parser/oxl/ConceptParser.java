package net.sourceforge.ondex.parser.oxl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.export.oxl.RefManager;
import net.sourceforge.ondex.export.oxl.XMLTagNames;

/**
 * This class parses XML ONDEX Concepts and stores them in a ONDEX graph. It
 * handles XML ONDEX Concept tags.
 * 
 * @author sierenk
 * @author Matthew Pocock
 */
public class ConceptParser extends AbstractEntityParser {

	// associated data sources
	private RefManager<String, DataSource> dataSourceRefManager;

	// associated concept classes
	private RefManager<String, ConceptClass> conceptClassRefManager;

	// contains mapping of old to new integer ids
	private Map<Integer, Integer> idMapping;

	// mapping concept id to context list of ids
	private Map<Integer, Set<Integer>> context;

	// number of current concept parsed
	private int progress = 0;

	/**
	 * Creates a parser for Concept elements.
	 * 
	 * @param og
	 *            ONDEXGraph for storing parsed concepts.
	 * @param idOldNew
	 *            mapping of imported to created IDs
	 * @param context
	 *            context for concept id
	 */
	public ConceptParser(final ONDEXGraph og, Map<Integer, Integer> idOldNew,
			Map<Integer, Set<Integer>> context) throws JAXBException {
		super(og);
		this.idMapping = idOldNew;
		this.context = context;

		dataSourceRefManager = new RefManager<String, DataSource>() {
			@Override
			protected DataSource resolveExternally(String name) {
				return og.getMetaData().getDataSource(name);
			}
		};

		conceptClassRefManager = new RefManager<String, ConceptClass>() {
			@Override
			protected ConceptClass resolveExternally(String id) {
				return og.getMetaData().getConceptClass(id);
			}
		};
	}

	/**
	 * Returns progress of parsing.
	 * 
	 * @return number of concepts parsed
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * Returns name of XMLComponentParser.
	 * 
	 * @return String
	 */
	public String getName() {
		return "ConceptParser";
	}

	/**
	 * Parses ONDEX Concept tags.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws InconsistencyException
	 */
	public void parse(XMLStreamReader xmlr) throws XMLStreamException,
			JAXBException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InconsistencyException {

		progress++;

		xmlr.nextTag(); // concept id
		Integer id = Integer.valueOf(xmlr.getElementText());

		xmlr.nextTag(); // concept pid
		String pid = xmlr.getElementText();

		xmlr.nextTag(); // conncept annotation
		String annotation = xmlr.getElementText();

		xmlr.nextTag(); // concept description
		String description = xmlr.getElementText();

		// elementOf (DataSource)
		DataSource dataSource = parseDataSource(xmlr).get();

		// ofType (CC)
		ConceptClass cc = parseConceptClass(xmlr).get();

		xmlr.nextTag(); // evidences
		Collection<EvidenceType> evidences = parseEvidences(xmlr);

		if (evidences == null)
			throw new InconsistencyException(
					"An evidence type is missing in the XML at Concept " + pid
							+ " can not continue with concept.");

		// create the concept
		ONDEXConcept c = og.createConcept(pid, annotation, description,
				dataSource, cc, evidences);
		idMapping.put(id, c.getId());

		xmlr.nextTag(); // conames
		parseConceptNames(xmlr, c);

		xmlr.nextTag(); // coaccessions

		parseConceptAccessions(xmlr, c);

		xmlr.nextTag(); // cogds start

		parseAttributes(xmlr, c);

		xmlr.nextTag(); // concept end or start context

		int eventType = xmlr.getEventType();

		// fill context old way
		if (eventType == XMLStreamConstants.START_ELEMENT
				&& xmlr.getLocalName().equals(XMLTagNames.CONTEXT)) {
			String[] list = xmlr.getElementText().split(",");
			for (String number : list) {
				if (!context.containsKey(id))
					context.put(id, new HashSet<Integer>());
				context.get(id).add(Integer.valueOf(number));
			}

			xmlr.nextTag(); // skip end tag
		}

		// fill context new way
		if (eventType == XMLStreamConstants.START_ELEMENT
				&& xmlr.getLocalName().equals(XMLTagNames.CONTEXTS)) {
			parseContext(xmlr, id);
		}
	}

	/**
	 * Fills global map with context per concept
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @param id
	 *            current concept id
	 * @throws XMLStreamException
	 */
	protected void parseContext(XMLStreamReader xmlr, Integer id)
			throws XMLStreamException {
		int eventType = xmlr.getEventType();

		while (xmlr.hasNext() && eventType != XMLStreamConstants.END_ELEMENT) {

			if (eventType == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.CONTEXT)) {
				// example tags
				// <id>12</id>

				xmlr.nextTag(); // id
				Integer number = Integer.valueOf(xmlr.getElementText());

				// add context to data cache ...
				if (!context.containsKey(id))
					context.put(id, new HashSet<Integer>());
				context.get(id).add(number);

				xmlr.nextTag(); // skip end tag
			}
			eventType = xmlr.next();
		}
	}

	/**
	 * Parses Control Vocabulary Content. Returns the parsed DataSource or the
	 * matching existing DataSource.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @return DataSource
	 * @throws XMLStreamException
	 */
	protected RefManager.Ref<DataSource> parseDataSourceContent(
			XMLStreamReader xmlr) throws XMLStreamException {

		// parse DataSource content
		xmlr.nextTag(); // name
		String cvname = checkForSpace(xmlr.getElementText());
		final RefManager.Ref<DataSource> dataSourceRef = dataSourceRefManager
				.makeRef(cvname);

		if (xmlr.getLocalName().equals(XMLTagNames.ID_REF)) {
			// nothing
		} else {
			// create DataSource on ONDEXGraph (if it does not exist)
			if (!og.getMetaData().checkDataSource(cvname)) {
				xmlr.nextTag(); // fullname
				String fullname = xmlr.getElementText();

				xmlr.nextTag(); // description
				String cvdescription = xmlr.getElementText();

				DataSource dataSource = og.getMetaData().createDataSource(
						cvname, fullname, cvdescription);
				dataSourceRefManager.resolve(cvname, dataSource);
			} else {
				while (xmlr.hasNext()) {
					int eventType = xmlr.next();

					if (eventType == XMLStreamConstants.END_ELEMENT
							&& xmlr.getLocalName().equals(
									XMLTagNames.DESCRIPTION)) {
						break;
					}
				}
			}
		}
		return dataSourceRef;
	}

	/**
	 * Parses Control Vocabulary. Returns the parsed DataSource or the matching
	 * existing DataSource.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @return DataSource
	 * @throws XMLStreamException
	 */
	protected RefManager.Ref<DataSource> parseDataSource(XMLStreamReader xmlr)
			throws XMLStreamException {

		xmlr.nextTag();// start elementOf or cv tag

		// parse elementOf content
		RefManager.Ref<DataSource> dataSourceRef = parseDataSourceContent(xmlr);

		xmlr.nextTag();// end elementOf or cv tag

		return dataSourceRef;

	}

	/**
	 * Parses a sequence of concept names which belong to a given concept.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @param c
	 *            AbstractConcept
	 * @throws XMLStreamException
	 */
	protected void parseConceptNames(XMLStreamReader xmlr, ONDEXConcept c)
			throws XMLStreamException {

		int eventType = xmlr.getEventType();

		while (xmlr.hasNext() && eventType != XMLStreamConstants.END_ELEMENT) {

			if (eventType == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.CONCEPTNAME)) {
				// example tags
				// <name>Christmas-tree</name>
				// <isPreferred>true</isPreferred>

				xmlr.nextTag(); // name
				String cname = xmlr.getElementText();

				xmlr.nextTag(); // isPreferred

				String isPreferredS = xmlr.getElementText();
				boolean isPreferred = Boolean.parseBoolean(isPreferredS);

				// add concept names to data repository ...
				c.createConceptName(cname, isPreferred);

				xmlr.nextTag(); // skip end tag
			}
			eventType = xmlr.next();
		}
	}

	/**
	 * Parses a sequence of concept accession numbers which belong to a given
	 * concept.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @param c
	 *            AbstractConcept
	 * @throws XMLStreamException
	 */
	protected void parseConceptAccessions(XMLStreamReader xmlr,
			final ONDEXConcept c) throws XMLStreamException {

		int eventType = xmlr.getEventType();

		while (xmlr.hasNext() && eventType != XMLStreamConstants.END_ELEMENT) {

			if (eventType == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.CONCEPTACCESSION)) {

				xmlr.nextTag(); // accession
				final String accession = xmlr.getElementText();

				xmlr.nextTag(); // elementOf

				RefManager.Ref<DataSource> dataSource = parseDataSourceContent(xmlr);

				xmlr.nextTag(); // end elementOf

				xmlr.nextTag(); // ambiguous
				String ambiguousS = xmlr.getElementText();
				final boolean ambiguous = Boolean.parseBoolean(ambiguousS);

				// add concept accessions to data repository ...
				dataSource
						.setCompletionAction(new RefManager.OnCompletion<net.sourceforge.ondex.core.DataSource>() {
							@Override
							public void onCompletion(DataSource dataSource) {
								c.createConceptAccession(accession, dataSource,
										ambiguous);
							}
						});

				xmlr.nextTag(); // skip end tag
			}
			eventType = xmlr.next();
		}
	}

	/**
	 * Parses ConceptClass tag.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @return ConceptClass
	 * @throws XMLStreamException
	 */
	protected RefManager.Ref<ConceptClass> parseConceptClass(
			XMLStreamReader xmlr) throws XMLStreamException {

		xmlr.nextTag(); // ofType (CC)
		return parseConceptClassContent(xmlr);
	}

	/**
	 * Parses the content an ofType/conceptclass tag.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @return ConceptClass
	 * @throws XMLStreamException
	 */
	protected RefManager.Ref<ConceptClass> parseConceptClassContent(
			XMLStreamReader xmlr) throws XMLStreamException {

		// parse ofType/cc content

		xmlr.nextTag(); // name
		final String ccname = xmlr.getElementText();
		final RefManager.Ref<ConceptClass> ccRef = conceptClassRefManager
				.makeRef(ccname);

		if (xmlr.getLocalName().equals(XMLTagNames.ID_REF)) {
			// nothing else to do
		} else {
			xmlr.nextTag(); // fullname
			String fullname = xmlr.getElementText();

			xmlr.nextTag(); // description
			String ccdescription = xmlr.getElementText();

			// if cc does not exist, create Concept Class
			if (!og.getMetaData().checkConceptClass(ccname)) {
				conceptClassRefManager.resolve(
						ccname,
						og.getMetaData()
								.getFactory()
								.createConceptClass(ccname, fullname,
										ccdescription));
			} else { // skip everything else
				// do nothing
			}

			xmlr.nextTag(); // end ofType OR start specialisationOf

			// optional specialisationOf tag
			int eventType = xmlr.getEventType();
			if (eventType == XMLStreamConstants.START_ELEMENT
					&& xmlr.getLocalName().equals(XMLTagNames.SPECIALISATIONOF)) {
				RefManager.Ref<ConceptClass> specOf = parseConceptClassContent(xmlr);
				specOf.setCompletionAction(new RefManager.OnCompletion<net.sourceforge.ondex.core.ConceptClass>() {
					@Override
					public void onCompletion(ConceptClass parent) {
						ccRef.get().setSpecialisationOf(parent);
					}
				});
			}
		}

		return ccRef;
	}

	/**
	 * Called after parser run finished.
	 * 
	 * @param aog
	 *            ONDEXGraph
	 * @param idOldNew
	 *            Map<Integer, Integer>
	 * @param context
	 *            Map<Integer, Set<Integer>>
	 * @throws InconsistencyException
	 */
	public static void syncContext(ONDEXGraph aog,
			Map<Integer, Integer> idOldNew, Map<Integer, Set<Integer>> context)
			throws InconsistencyException {
		// update the context for previous parsed concepts
		for (Integer key : context.keySet()) {
			ONDEXConcept c = aog.getConcept(idOldNew.get(key));
			for (Integer id : context.get(key)) {
				ONDEXConcept co = aog.getConcept(idOldNew.get(id));
				if (co != null)
					c.addTag(co);
				else
					throw new InconsistencyException(
							"A tag concept for concept " + c.getId()
									+ " is missing.");
			}
		}
	}

}
