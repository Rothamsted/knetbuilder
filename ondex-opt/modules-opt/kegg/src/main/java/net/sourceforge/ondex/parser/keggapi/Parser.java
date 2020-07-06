package net.sourceforge.ondex.parser.keggapi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.PathwayElement;
import keggapi.PathwayElementRelation;
import keggapi.Subtype;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.parser.ONDEXParser;

/**
 * Parses the KEGG database by querying via the keggapi.
 * 
 * @author taubertj
 * 
 */
public class Parser extends ONDEXParser implements ArgumentNames {

	// concept class for Compound
	ConceptClass ccComp = null;

	// concept class for EC number
	ConceptClass ccEC = null;

	// concept class for Enzyme
	ConceptClass ccEnzyme = null;

	// concept class for Gene
	ConceptClass ccGene = null;

	// concept class for KEGG orthologs
	ConceptClass ccKO = null;

	// concept class for Pathway
	ConceptClass ccPath = null;

	// concept class for Reaction
	ConceptClass ccReaction = null;

	// concept class for all other things
	ConceptClass ccThing = null;

	// data source for EC
	DataSource dsEC = null;

	// data source for KEGG
	DataSource dsKEGG = null;

	// cache EC concepts by their name
	Map<String, ONDEXConcept> ecCache = new HashMap<String, ONDEXConcept>();

	// cache created concepts by unique ID in map
	Map<Integer, ONDEXConcept> elementCache = new HashMap<Integer, ONDEXConcept>();

	// cache enzyme concepts by their name
	Map<String, ONDEXConcept> enzymeCache = new HashMap<String, ONDEXConcept>();

	// new evidence type
	EvidenceType etKEGGAPI = null;

	// relation type between enzyme and reaction
	RelationType rtCatalysiedBy = null;

	// relation type between gene and EC
	RelationType rtCatClass = null;

	// relation type between reaction and compound
	RelationType rtConsumedBy = null;

	// relation type between reaction and pathway
	RelationType rtMemberOf = null;

	// relation type between reaction and compound
	RelationType rtProducedBy = null;

	// KEGG API server port
	KEGGPortType serv = null;

	/**
	 * Adds accessions derived from the names of the pathway element to the
	 * ONDEXConcept
	 * 
	 * @param c
	 *            ONDEXConcept
	 * @param element
	 *            PathwayElement
	 */
	private void addAccessions(ONDEXConcept c, PathwayElement element) {
		String[] names = element.getNames();
		for (int i = 0; i < names.length; i++) {
			c.createConceptAccession(names[i], dsKEGG, false);
		}
	}

	/**
	 * Adds names from the pathway element to the ONDEXConcept
	 * 
	 * @param c
	 *            ONDEXConcept
	 * @param element
	 *            PathwayElement
	 */
	private void addNames(ONDEXConcept c, PathwayElement element) {
		// add concept names, arbitrarily set first one to preferred
		String[] names = element.getNames();
		for (int i = 0; i < names.length; i++) {
			c.createConceptName(names[i], i == 0);
		}
	}

	/**
	 * Creates an ONDEXConcept compound for a given PathwayElement
	 * 
	 * @param element
	 *            PathwayElement
	 * @param path
	 *            ONDEXConcept
	 * @return ONDEXConcept
	 */
	private ONDEXConcept createCompoundForElement(PathwayElement element,
			ONDEXConcept path) {

		if (!element.getType().equals("compound")) {
			fireEventOccurred(new InconsistencyEvent(
					"Element is not of type compound but of "
							+ element.getType(), getCurrentMethodName()));
		}

		// create concept for compound
		ONDEXConcept compound = graph.getFactory().createConcept(
				String.valueOf(element.getElement_id()), dsKEGG, ccComp,
				etKEGGAPI);
		compound.addTag(path);
		addNames(compound, element);
		addAccessions(compound, element);

		return compound;
	}

	/**
	 * Creates an ONDEXConcept reaction for a given PathwayElement
	 * 
	 * @param element
	 *            PathwayElement
	 * @param path
	 *            ONDEXConcept
	 * @return ONDEXConcept
	 * @throws RemoteException
	 */
	private ONDEXConcept createReactionForElement(PathwayElement element,
			ONDEXConcept path) throws RemoteException {

		// turns a gene element into a reaction place holder
		if (element.getType().equals("gene")) {
			// dummy reaction concept
			ONDEXConcept reaction = graph.getFactory().createConcept(
					String.valueOf(element.getElement_id()), dsKEGG,
					ccReaction, etKEGGAPI);
			reaction.addTag(path);

			// member_of relation between reaction and pathway
			ONDEXRelation member_of = graph.getFactory().createRelation(
					reaction, path, rtMemberOf, etKEGGAPI);
			member_of.addTag(path);

			// there might be more than one enzyme involved here
			for (String name : element.getNames()) {

				// link all same enzyme concepts together
				ONDEXConcept enzyme;
				if (!enzymeCache.containsKey(name)) {
					// create separate enzyme concepts
					enzyme = graph.getFactory().createConcept(name, dsKEGG,
							ccEnzyme, etKEGGAPI);
					enzyme.createConceptName(name, true);
					enzyme.createConceptAccession(name, dsKEGG, false);
					enzyme.addTag(path);
					enzymeCache.put(name, enzyme);

					// create one EC concept per enzyme name
					for (String enzymeName : serv.get_enzymes_by_gene(name)) {

						// link all same EC concepts together
						ONDEXConcept ec;
						if (!ecCache.containsKey(enzymeName)) {
							// EC concept, e.g. ec:1.2.1.1
							ec = graph.getFactory().createConcept(enzymeName,
									dsKEGG, ccEC, etKEGGAPI);
							ec.createConceptAccession(enzymeName, dsKEGG, false);
							ec.createConceptAccession(
									enzymeName.replaceFirst("ec:", ""), dsEC,
									false);
							ec.addTag(path);
							ecCache.put(enzymeName, ec);
						} else
							ec = ecCache.get(enzymeName);

						// relation cat_c between
						ONDEXRelation cat_c = graph.getFactory()
								.createRelation(enzyme, ec, rtCatClass,
										etKEGGAPI);
						cat_c.addTag(path);
					}
				} else
					enzyme = enzymeCache.get(name);

				// link enzymes to reaction
				ONDEXRelation ca_by = graph.getFactory().createRelation(
						reaction, enzyme, rtCatalysiedBy, etKEGGAPI);
				ca_by.addTag(path);
			}

			return reaction;
		}

		// turns a pathway map reference into a concept
		else if (element.getType().equals("map")) {
			// links over to another pathway map
			ONDEXConcept map = graph.getFactory().createConcept(
					String.valueOf(element.getElement_id()), dsKEGG, ccPath,
					etKEGGAPI);
			map.addTag(path);
			addNames(map, element);
			addAccessions(map, element);
			return map;
		}

		else {
			System.out.println("Missing element type: " + element.getType());
		}

		return null;
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new StringArgumentDefinition(ORG_ARG, ORG_ARG_DESC, true, null,
						true),
				new StringArgumentDefinition(MAP_ARG, MAP_ARG_DESC, true, null,
						true) };
	}

	@Override
	public String getId() {
		return "keggapi";
	}

	@Override
	public String getName() {
		return "KEGG API extractor";
	}

	@Override
	public String getVersion() {
		return "20.09.2011";
	}

	/**
	 * Initialises all globally used MetaData
	 */
	private void initMetaData() {

		// data source for all KEGG elements
		dsKEGG = graph.getMetaData().getDataSource("KEGG");
		if (dsKEGG == null)
			dsKEGG = graph.getMetaData().getFactory().createDataSource("KEGG");

		// data source for EC accessions
		dsEC = graph.getMetaData().getDataSource("EC");
		if (dsEC == null)
			dsEC = graph.getMetaData().getFactory().createDataSource("EC");

		// possibly a new evidence type
		etKEGGAPI = graph.getMetaData().getEvidenceType("KEGGAPI");
		if (etKEGGAPI == null)
			etKEGGAPI = graph.getMetaData().createEvidenceType("KEGGAPI",
					"KEGG API", "Imported via KEGG API.");

		// concept classes used to distinguish different KEGG queries
		ccPath = graph.getMetaData().getConceptClass("Path");
		if (ccPath == null)
			ccPath = graph.getMetaData().getFactory()
					.createConceptClass("Path");

		ccReaction = graph.getMetaData().getConceptClass("Reaction");
		if (ccReaction == null)
			ccReaction = graph.getMetaData().getFactory()
					.createConceptClass("Reaction");

		ccComp = graph.getMetaData().getConceptClass("Comp");
		if (ccComp == null)
			ccComp = graph.getMetaData().getFactory()
					.createConceptClass("Comp");

		ccEnzyme = graph.getMetaData().getConceptClass("Enzyme");
		if (ccEnzyme == null)
			ccEnzyme = graph.getMetaData().getFactory()
					.createConceptClass("Enzyme");

		ccGene = graph.getMetaData().getConceptClass("Gene");
		if (ccGene == null)
			ccGene = graph.getMetaData().getFactory()
					.createConceptClass("Gene");

		ccKO = graph.getMetaData().getConceptClass("KO");
		if (ccKO == null)
			ccKO = graph.getMetaData().getFactory().createConceptClass("KO");

		ccThing = graph.getMetaData().getConceptClass("Thing");
		if (ccThing == null)
			ccThing = graph.getMetaData().getFactory()
					.createConceptClass("Thing");

		ccEC = graph.getMetaData().getConceptClass("EC");
		if (ccEC == null)
			ccEC = graph.getMetaData().getFactory().createConceptClass("EC");

		// relation types used to connect different KEGG entities
		rtMemberOf = graph.getMetaData().getRelationType("member_of");
		if (rtMemberOf == null)
			rtMemberOf = graph.getMetaData().getFactory()
					.createRelationType("member_of");

		rtConsumedBy = graph.getMetaData().getRelationType("cs_by");
		if (rtConsumedBy == null)
			rtConsumedBy = graph.getMetaData().getFactory()
					.createRelationType("cs_by");

		rtProducedBy = graph.getMetaData().getRelationType("pd_by");
		if (rtProducedBy == null)
			rtProducedBy = graph.getMetaData().getFactory()
					.createRelationType("pd_by");

		rtCatClass = graph.getMetaData().getRelationType("cat_c");
		if (rtCatClass == null)
			rtCatClass = graph.getMetaData().getFactory()
					.createRelationType("cat_c");

		rtCatalysiedBy = graph.getMetaData().getRelationType("ca_by");
		if (rtCatalysiedBy == null)
			rtCatalysiedBy = graph.getMetaData().getFactory()
					.createRelationType("ca_by");
	}

	/**
	 * Retrieves all elements and their relations of a given pathway.
	 * 
	 * @param pathway_id
	 *            ID of KEGG pathway
	 * @throws RemoteException
	 */
	private void parsePathwayMap(String pathway_id) throws RemoteException {

		// always clear cache as IDs only unique per map
		elementCache.clear();

		// empty for new map to come
		ecCache.clear();

		// empty for new map to come
		enzymeCache.clear();

		// elements of pathway indexed by their ID
		Map<Integer, PathwayElement> elements = new HashMap<Integer, PathwayElement>();

		// index all pathway elements by ID
		for (PathwayElement element : serv.get_elements_by_pathway(pathway_id)) {
			elements.put(element.getElement_id(), element);
		}

		// sort relationship by their type
		Map<String, List<PathwayElementRelation>> relations = new HashMap<String, List<PathwayElementRelation>>();

		// index all relationships by their type
		for (PathwayElementRelation rel : serv
				.get_element_relations_by_pathway(pathway_id)) {
			String type = rel.getType();
			if (!relations.containsKey(type))
				relations.put(type, new ArrayList<PathwayElementRelation>());
			relations.get(type).add(rel);
		}

		fireEventOccurred(new GeneralOutputEvent("Found relation types in map "
				+ pathway_id + ": " + relations.keySet(),
				getCurrentMethodName()));

		// this is the pathway concept to be added as tag
		ONDEXConcept path = graph.getFactory().createConcept(pathway_id,
				dsKEGG, ccPath, etKEGGAPI);
		path.addTag(path); // self-tag
		path.createConceptName(pathway_id, true);
		path.createConceptAccession(pathway_id, dsKEGG, false);

		// process each relation type
		for (String type : relations.keySet()) {

			if (type.equals("ECrel")) {
				// process ECrel and assign tags
				processECrel(relations.get("ECrel"), elements, path);
			}

			else if (type.equals("maplink")) {
				// process maplink and assign tags
				processMaplink(relations.get("maplink"), elements, path);
			}

			else {
				fireEventOccurred(new InconsistencyEvent(
						"Remaining relation type for map " + pathway_id + ": "
								+ type, getCurrentMethodName()));
			}
		}
	}

	/**
	 * Creates ECrel relations between pathway entities.
	 * 
	 * @param list
	 *            List<PathwayElementRelation>
	 * @param elements
	 *            Map<Integer, PathwayElement>
	 * @param path
	 *            ONDEXConcept
	 * @return List<ONDEXRelation>
	 * @throws RemoteException
	 */
	private void processECrel(List<PathwayElementRelation> list,
			Map<Integer, PathwayElement> elements, ONDEXConcept path)
			throws RemoteException {

		// process all relations
		for (PathwayElementRelation rel : list) {

			int element_id1 = rel.getElement_id1();
			int element_id2 = rel.getElement_id2();

			// find concept for element 1
			if (!elements.containsKey(element_id1)) {
				fireEventOccurred(new InconsistencyEvent(
						"PathwayElementRelation " + element_id1 + " => "
								+ element_id2 + " (" + rel.getType()
								+ ") referred non-existing element 1.",
						getCurrentMethodName()));
				continue;
			}
			// check if concept is pre-cached
			if (!elementCache.containsKey(element_id1))
				elementCache.put(
						element_id1,
						createReactionForElement(elements.get(element_id1),
								path));
			ONDEXConcept concept1 = elementCache.get(element_id1);

			// find concept for element 2
			if (!elements.containsKey(element_id2)) {
				fireEventOccurred(new InconsistencyEvent(
						"PathwayElementRelation " + element_id1 + " => "
								+ element_id2 + " (" + rel.getType()
								+ ") referred non-existing element 2.",
						getCurrentMethodName()));
				continue;
			}
			// check if concept is pre-cached
			if (!elementCache.containsKey(element_id2))
				elementCache.put(
						element_id2,
						createReactionForElement(elements.get(element_id2),
								path));
			ONDEXConcept concept2 = elementCache.get(element_id2);

			// decide relationship based on sub type
			for (Subtype sub : rel.getSubtypes()) {
				if (sub.getRelation().equals("compound")) {
					int element_id = sub.getElement_id();

					// find concept for sub element
					if (!elements.containsKey(element_id)) {
						fireEventOccurred(new InconsistencyEvent(
								"PathwayElementRelation "
										+ element_id1
										+ " => "
										+ element_id2
										+ " ("
										+ rel.getType()
										+ ") referred non-existing sub-element: "
										+ element_id, getCurrentMethodName()));
						continue;
					}
					// check if concept is pre-cached
					if (!elementCache.containsKey(element_id))
						elementCache.put(
								element_id,
								createCompoundForElement(
										elements.get(element_id), path));
					ONDEXConcept compound = elementCache.get(element_id);

					// compound is produced by the first enzyme
					ONDEXRelation pd_by = graph.getFactory().createRelation(
							compound, concept1, rtProducedBy, etKEGGAPI);
					pd_by.addTag(path);

					// compound is consumed by the second enzyme
					ONDEXRelation cs_by = graph.getFactory().createRelation(
							compound, concept2, rtConsumedBy, etKEGGAPI);
					cs_by.addTag(path);
				} else {
					fireEventOccurred(new InconsistencyEvent(
							"Unknown subtype: " + sub.getRelation(),
							getCurrentMethodName()));
				}
			}
		}
	}

	/**
	 * Creates maplink relations between pathway entities.
	 * 
	 * @param list
	 *            List<PathwayElementRelation>
	 * @param elements
	 *            Map<Integer, PathwayElement>
	 * @param path
	 *            ONDEXConcept
	 * @return List<ONDEXRelation>
	 * @throws RemoteException
	 */
	private void processMaplink(List<PathwayElementRelation> list,
			Map<Integer, PathwayElement> elements, ONDEXConcept path)
			throws RemoteException {

		// process all relations
		for (PathwayElementRelation rel : list) {

			int element_id1 = rel.getElement_id1();
			int element_id2 = rel.getElement_id2();

			// find concept for element 1
			if (!elements.containsKey(element_id1)) {
				fireEventOccurred(new InconsistencyEvent(
						"PathwayElementRelation " + element_id1 + " => "
								+ element_id2 + " (" + rel.getType()
								+ ") referred non-existing element 1.",
						getCurrentMethodName()));
				continue;
			}
			// check if concept is pre-cached
			if (!elementCache.containsKey(element_id1))
				elementCache.put(
						element_id1,
						createReactionForElement(elements.get(element_id1),
								path));
			ONDEXConcept concept1 = elementCache.get(element_id1);

			// find concept for element 2
			if (!elements.containsKey(element_id2)) {
				fireEventOccurred(new InconsistencyEvent(
						"PathwayElementRelation " + element_id1 + " => "
								+ element_id2 + " (" + rel.getType()
								+ ") referred non-existing element 2.",
						getCurrentMethodName()));
				continue;
			}
			// check if concept is pre-cached
			if (!elementCache.containsKey(element_id2))
				elementCache.put(
						element_id2,
						createReactionForElement(elements.get(element_id2),
								path));
			ONDEXConcept concept2 = elementCache.get(element_id2);

			// decide relationship based on sub type
			for (Subtype sub : rel.getSubtypes()) {
				if (sub.getRelation().equals("compound")) {
					int element_id = sub.getElement_id();

					// find concept for sub element
					if (!elements.containsKey(element_id)) {
						fireEventOccurred(new InconsistencyEvent(
								"PathwayElementRelation "
										+ element_id1
										+ " => "
										+ element_id2
										+ " ("
										+ rel.getType()
										+ ") referred non-existing sub-element: "
										+ element_id, getCurrentMethodName()));
						continue;
					}
					// check if concept is pre-cached
					if (!elementCache.containsKey(element_id))
						elementCache.put(
								element_id,
								createCompoundForElement(
										elements.get(element_id), path));
					ONDEXConcept compound = elementCache.get(element_id);

					// compound is produced by the first enzyme
					ONDEXRelation pd_by = graph.getFactory().createRelation(
							compound, concept1, rtProducedBy, etKEGGAPI);
					pd_by.addTag(path);

					// compound is consumed by the second enzyme
					ONDEXRelation cs_by = graph.getFactory().createRelation(
							compound, concept2, rtConsumedBy, etKEGGAPI);
					cs_by.addTag(path);
				} else {
					fireEventOccurred(new InconsistencyEvent(
							"Unknown subtype: " + sub.getRelation(),
							getCurrentMethodName()));
				}
			}
		}
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {

		// construct all organism map combinations
		List<String> maps = new ArrayList<String>();
		for (String org : args.getObjectValueList(ORG_ARG, String.class)) {
			for (String map : args.getObjectValueList(MAP_ARG, String.class)) {
				maps.add("path:" + org + map);
			}
		}

		fireEventOccurred(new GeneralOutputEvent("Parsing maps: " + maps,
				"[Parser - start]"));

		// try to connect to KEGG
		KEGGLocator locator = new KEGGLocator();
		try {
			serv = locator.getKEGGPort();
		} catch (ServiceException e) {
			e.printStackTrace();
		}

		// exit here if no connection
		if (serv == null)
			return;

		// get all required meta data together
		initMetaData();

		// process all specified pathway maps
		for (String pathway_id : maps) {
			// just in case...
			pathway_id = pathway_id.toLowerCase();
			parsePathwayMap(pathway_id);
		}
	}

}
