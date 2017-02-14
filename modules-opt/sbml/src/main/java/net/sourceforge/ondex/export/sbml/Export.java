package net.sourceforge.ondex.export.sbml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.CompressResultsArguementDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * Exporter for the SBML format level 2.1
 * 
 * @date 13.06.07
 */
public class Export extends ONDEXExport implements ArgumentNames {

	public static final String COMPARTMENT = "compartment";

	public static final String REACTION = "reaction";

	public static final String SPECIES = "species";

	public static final String LISTOFCOMPARTMENTS = "listOfCompartments";

	public static final String LISTOFREACTIONS = "listOfReactions";

	public static final String LISTOFSPECIES = "listOfSpecies";

	public static final String LISTOFREACTANTS = "listOfReactants";

	public static final String LISTOFPRODUCTS = "listOfProducts";

	public static final String LISTOFMODIFIERS = "listOfModifiers";

	public static final String SPECIESREFERENCE = "speciesReference";

	public static final String MODIFIERSPECIESREFERENCE = "modifierSpeciesReference";

	public static final String SBOTERM = "sboTerm";

	private static final String SBML_NS = "http://www.sbml.org/sbml/level2/version3";

	private static final String W3_NS = "http://www.w3.org/1999/xhtml";

	private Set<String> conceptClassToReaction = new HashSet<String>();

	private Set<String> conceptClassToAnnotation = new HashSet<String>();

	private Map<String, String> conceptClassToSBO = new HashMap<String, String>();

	private Set<String> relationTypeToRegArc = new HashSet<String>();

	private Set<String> relationTypeReverseLogic = new HashSet<String>();

	private Set<String> relationTypeToAnnotation = new HashSet<String>();

	private Set<ONDEXConcept> sourceConcepts;

	private Set<ONDEXRelation> sourceRelations;

	private Set<ONDEXRelation> processedRelations = new HashSet<ONDEXRelation>();

	private Set<ONDEXConcept> conceptsToReactions = new HashSet<ONDEXConcept>();

	private boolean proc_subset = false;

	// private static final String CSML_NS =
	// "http://www.csml.org/csml/version1";
	// private static final String CSML_NS_PF = "csml";

	// private static final String RDF_NS_PF = "rdf";
	// private static final String RDF_NS =
	// "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	private void buildDocument(final XMLStreamWriter xmlw, final ONDEXGraph og) throws XMLStreamException, NullValueException, AccessDeniedException {
		xmlw.writeStartDocument("UTF-8", "1.0");
		xmlw.writeStartElement("sbml");
		xmlw.writeDefaultNamespace(SBML_NS); // this will be xmlns prefix
		// xmlw.writeNamespace(CSML_NS_PF, CSML_NS);
		xmlw.writeAttribute("level", "2");
		xmlw.writeAttribute("version", "3");

		writeNotes(xmlw);
		writeModel(xmlw);

		xmlw.writeEndElement(); // ondex
		xmlw.writeEndDocument();
	}

	@SuppressWarnings("unused")
	private void buildDocument(XMLStreamWriter xmlw, Set<ONDEXConcept> concepts, Set<ONDEXRelation> relations) throws XMLStreamException, NullValueException, AccessDeniedException {
		sourceConcepts = concepts;
		sourceRelations = relations;
		proc_subset = true;
		buildDocument(xmlw, graph);
	}

	@SuppressWarnings("unused")
	private void buildMetaDataDocument(XMLStreamWriter xmlw, ONDEXGraphMetaData md) {
		System.err.println("MetaData Export not supported for class: " + this.getClass().toString());
	}

	/**
	 * Method to generate species name in SBML used for an ONDEXConcept.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @return species name of ONDEXConcept
	 */
	private String generateName(ONDEXConcept concept) {
		String name = "";

		// check for concept names first
		Set<ConceptName> names = concept.getConceptNames();
		if (names.size() > 0) {

			// check for preferred concept name
			ConceptName cn = concept.getConceptName();
			if (cn != null)
				name = concept.getConceptName().getName();
			else {

				// select longest concept name otherwise
				for (ConceptName cName : names) {
					String temp = cName.getName();
					if (name.length() > temp.length())
						name = temp;
				}
			}
		} else {
			// no concept names found
			if (concept.getPID() != null)
				name = concept.getPID();
			else if (concept.getAnnotation() != null)
				name = concept.getAnnotation();
			else if (concept.getDescription() != null)
				name = concept.getDescription();
			else
				// last resort
				name = concept.getOfType().getFullname();
		}
		return name;
	}

	/**
	 * @return Returns arguments for the exporter
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] { new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE, "SBML export file", true, false, false, false), new FileArgumentDefinition(CONFIG_FILE_ARG, CONFIG_FILE_ARG_DESC, false, true, false),
				new CompressResultsArguementDefinition(EXPORT_AS_ZIP_FILE, EXPORT_AS_ZIP_FILE_DESC, false, false) };
	}

	/**
	 * Returns a String array with concept IDs concatenated with "m".
	 * 
	 * @param concepts
	 *            list of ONDEXConcept
	 * @return list of String concept IDs
	 */
	private String[] getIds(Set<ONDEXConcept> concepts) {
		Set<String> ids = new HashSet<String>();
		Iterator<ONDEXConcept> it = concepts.iterator();
		while (it.hasNext())
			ids.add("m" + it.next().getId());
		return ids.toArray(new String[ids.size()]);
	}

	/**
	 * Counts how many relations which are modifiers exist for a given concept.
	 * 
	 * @param c
	 *            ONDEXConcept with relations
	 * @return number of modifier relations
	 */
	private int getModifierRelationsCout(ONDEXConcept c) {
		Set<ONDEXRelation> output = new HashSet<ONDEXRelation>();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (!proc_subset || sourceRelations.contains(r)) {
				String relationType = r.getOfType().getFullname();
				if (relationType == null)
					relationType = r.getOfType().getId();

				String targetRelationType = getTarget(r).getOfType().getFullname();
				if (targetRelationType == null)
					targetRelationType = getTarget(r).getOfType().getId();

				if (relationTypeToRegArc.contains(relationType) && !relationTypeToAnnotation.contains(relationType) && !getTarget(r).equals(c) && !conceptClassToReaction.contains(targetRelationType)) {
					output.add(r);
				}
			}
		}
		return output.size();
	}

	/**
	 * Which concepts are taking part in modifier relations for a given concept.
	 * 
	 * @param c
	 *            ONDEXConcept with relations
	 * @return modifier concepts
	 */
	private Set<ONDEXConcept> getModifiers(ONDEXConcept c) {
		Set<ONDEXConcept> output = new HashSet<ONDEXConcept>();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (!proc_subset || sourceRelations.contains(r)) {
				String relationType = r.getOfType().getFullname();
				if (relationType == null)
					relationType = r.getOfType().getId();

				if (relationTypeToRegArc.contains(relationType) && !getSource(r).equals(c)) {
					output.add(getSource(r));
				}
			}
		}
		return output;
	}

	/**
	 * @return Returns exporter name
	 */
	@Override
	public String getName() {
		return "SBML";
	}

	/**
	 * @return SBML level
	 */
	public String getSBMLLevel() {
		return "2";
	}

	@Override
	public String getId() {
		return "sbml";
	}

	/**
	 * @return SBML version
	 */
	public String getSBMLVersion() {
		return "1";
	}

	/**
	 * For a given relation returns the source concept, observes the reverse
	 * logic flag.
	 * 
	 * @param r
	 *            ONDEXRelation
	 * @return source concept
	 */
	private ONDEXConcept getSource(ONDEXRelation r) {
		String relationType = r.getOfType().getFullname();
		if (relationType == null)
			relationType = r.getOfType().getId();

		if (relationTypeReverseLogic.contains(relationType)) {
			return r.getToConcept();
		}
		return r.getFromConcept();
	}

	/**
	 * For a given concept returns all connecting source concepts, usually the
	 * reactions.
	 * 
	 * @param c
	 *            ONDEXConcept
	 * @return connecting source concepts
	 */
	private Set<ONDEXConcept> getSources(ONDEXConcept c) {
		Set<ONDEXConcept> output = new HashSet<ONDEXConcept>();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (!proc_subset || sourceRelations.contains(r)) {
				String relationType = r.getOfType().getFullname();
				if (relationType == null)
					relationType = r.getOfType().getId();

				if (!relationTypeToRegArc.contains(relationType) && !relationTypeToAnnotation.contains(relationType) && !getSource(r).equals(c)) {
					output.add(getSource(r));
				}
			}
		}
		return output;
	}

	/**
	 * For a given relation returns the target concept, observes the reverse
	 * logic flag.
	 * 
	 * @param r
	 *            ONDEXRelation
	 * @return target concept
	 */
	private ONDEXConcept getTarget(ONDEXRelation r) {
		String relationType = r.getOfType().getFullname();
		if (relationType == null)
			relationType = r.getOfType().getId();

		if (relationTypeReverseLogic.contains(relationType)) {
			return r.getFromConcept();
		}
		return r.getToConcept();
	}

	/**
	 * For a given concept returns all connecting target concepts, usually the
	 * reactions.
	 * 
	 * @param c
	 *            ONDEXConcept
	 * @return connecting target concepts
	 */
	private Set<ONDEXConcept> getTargets(ONDEXConcept c) throws NullValueException, AccessDeniedException {
		Set<ONDEXConcept> output = new HashSet<ONDEXConcept>();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (!proc_subset || sourceRelations.contains(r)) {
				String relationType = r.getOfType().getFullname();
				if (relationType == null)
					relationType = r.getOfType().getId();

				if (!relationTypeToRegArc.contains(relationType) && !relationTypeToAnnotation.contains(relationType) && !getTarget(r).equals(c)) {
					output.add(getTarget(r));
				}
			}
		}
		return output;
	}

	/**
	 * @return Exporter producer version
	 */
	@Override
	public String getVersion() {
		return "17.10.08";
	}

	/**
	 * Writes out the list of reactions for ONDEX entries.
	 * 
	 * @param xmlw
	 *            XML stream
	 * @throws XMLStreamException
	 * @throws NullValueException
	 * @throws AccessDeniedException
	 */
	private void listOfReactions(XMLStreamWriter xmlw) throws XMLStreamException, NullValueException, AccessDeniedException {

		// every species or reaction needs an ID
		int id_num = 1;
		int dummy_species_id = 1;
		int dummy_source_id = 1;

		xmlw.writeStartElement(LISTOFREACTIONS);

		// write reactions from concepts
		for (ONDEXConcept c : conceptsToReactions) {

			// get sources of this reaction
			Set<ONDEXConcept> sources = getSources(c);
			String[] sourceIds = getIds(sources);
			if (sourceIds.length < 1)
				sourceIds = new String[] { "d" + dummy_species_id++ };

			// get targets of this reaction
			Set<ONDEXConcept> targets = getTargets(c);
			String[] targetIds = getIds(targets);
			if (targetIds.length < 1)
				targetIds = new String[] { "d" + dummy_species_id++ };

			// get modifiers if exist
			Set<ONDEXConcept> modifiers = getModifiers(c);
			String[] modifierIds = getIds(modifiers);

			// mark current reaction concept as processed
			processedRelations.addAll(graph.getRelationsOfConcept(c));

			// write reaction to SBML
			String[][] lists = new String[][] { sourceIds, targetIds, modifierIds };
			writeReaction(xmlw, "r" + (id_num++), generateName(c), lists);
		}

		// check if we are running only on a subset
		Set<ONDEXRelation> relations;
		if (!proc_subset) {
			relations = graph.getRelations();
		} else {
			relations = sourceRelations;
		}

		// write reactions from relations
		for (ONDEXRelation r : relations) {

			// decide which kind of relation
			String relationType = r.getOfType().getFullname();
			if (relationType == null)
				relationType = r.getOfType().getId();

			if (relationTypeToRegArc.contains(relationType) && !processedRelations.contains(r)) {

				// QUESTION: Why do we need a dummy source here?
				String[][] lists = new String[][] { new String[] { "s" + dummy_source_id++ }, new String[] { "m" + getTarget(r).getId() }, new String[] { "m" + getSource(r).getId() } };
				writeReaction(xmlw, "r" + (id_num++), relationType, lists);
			}

			else if (!relationTypeToAnnotation.contains(relationType) && !processedRelations.contains(r)) {

				// simplest reaction with source and target, no modifier
				String[][] lists = new String[][] { new String[] { "m" + getSource(r).getId() }, new String[] { "m" + getTarget(r).getId() }, new String[] {} };
				writeReaction(xmlw, "r" + (id_num++), relationType, lists);
			}
		}

		xmlw.writeEndElement();// reactions
	}

	/**
	 * Write out the list of species for ONDEX entries.
	 * 
	 * @param xmlw
	 *            XML stream
	 * @throws XMLStreamException
	 * @throws NullValueException
	 * @throws AccessDeniedException
	 */
	private void listOfSpecies(XMLStreamWriter xmlw) throws XMLStreamException, NullValueException, AccessDeniedException {

		// every species needs an ID
		int dummy_species_id = 1;
		int dummy_source_id = 1;

		xmlw.writeStartElement(LISTOFSPECIES);

		// check if we are running only on a subset
		Set<ONDEXConcept> concepts;
		if (!proc_subset) {
			concepts = graph.getConcepts();
		} else {
			concepts = sourceConcepts;
		}

		// write species from concepts
		for (ONDEXConcept c : concepts) {

			String conceptClass = c.getOfType().getFullname();
			if (conceptClass == null)
				conceptClass = c.getOfType().getId();

			String sbo = null;
			if (conceptClassToSBO.containsKey(conceptClass))
				sbo = conceptClassToSBO.get(conceptClass);

			// check for potential modifiers
			for (int i = 0; i < getModifierRelationsCout(c); i++)
				writeSpecies(xmlw, "Input", "s" + (dummy_source_id++), sbo);

			// check if it is a species or reaction

			if (conceptClassToReaction.contains(conceptClass)) {

				// here is a reaction without any reactant, create a dummy
				if (getSources(c).size() < 1)
					writeSpecies(xmlw, "Unknown reactant", "d" + (dummy_species_id++), sbo);

				// here is a reaction without any product, create a dummy
				if (getTargets(c).size() < 1)
					writeSpecies(xmlw, "Unknown product", "d" + (dummy_species_id++), sbo);

				// mark this concept as a reaction
				conceptsToReactions.add(c);

			}

			else if (!conceptClassToAnnotation.contains(conceptClass)) {

				// finally it is a real species
				writeSpecies(xmlw, generateName(c), "m" + c.getId(), sbo);
			}
		}

		xmlw.writeEndElement();// listOfSpecies
	}

	/**
	 * @return true if requires indexing flase otherwise
	 */
	public boolean requiresIndexedGraph() {
		return false;
	}

	/**
	 * Starts the export process.
	 */
	@Override
	public void start() throws NullValueException, AccessDeniedException, InvalidPluginArgumentException {

		// get the XML factory and file to write to
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		File file = new File((String) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));

		// is optional
		if (args.getOptions().containsKey(CONFIG_FILE_ARG)) {
			ConfigReader config = new ConfigReader((String) args.getUniqueValue(CONFIG_FILE_ARG));
			conceptClassToReaction = config.getConceptClassToReaction();
			conceptClassToAnnotation = config.getConceptClassToAnnotation();
			relationTypeToRegArc = config.getRelationTypeToRegArc();
			relationTypeReverseLogic = config.getRelationTypeReverseLogic();
			relationTypeToAnnotation = config.getRelationTypeToAnnotation();
			conceptClassToSBO = config.getConceptClassToSBO();
		}

		// compress SBML?
		Boolean packed = ((Boolean) args.getUniqueValue(EXPORT_AS_ZIP_FILE));
		if (packed == null) {
			packed = false;
		}

		try {
			OutputStream outStream = null;
			if (packed) {
				// use zip compression
				String zipname = file.getAbsolutePath() + ".gz";
				outStream = new GZIPOutputStream(new FileOutputStream(zipname));
			} else {
				// output file writer
				outStream = new FileOutputStream(file);
			}

			XMLStreamWriter xmlStreamWriter = new IndentingXMLStreamWriter(xmlof.createXMLStreamWriter(outStream, "UTF-8"));
			// XMLStreamWriter xmlStreamWriter = (XMLStreamWriter)
			// xmlw.createXMLStreamWriter(outStream, "UTF-8");

			// start document build here
			buildDocument(xmlStreamWriter, graph);

			xmlStreamWriter.flush();
			xmlStreamWriter.close();

			outStream.flush();
			outStream.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the default compartment to SBML
	 * 
	 * @param xmlw
	 *            XML stream
	 * @throws XMLStreamException
	 */
	private void writeCompartments(XMLStreamWriter xmlw) throws XMLStreamException {

		// list contains only one element
		xmlw.writeStartElement(LISTOFCOMPARTMENTS);
		xmlw.writeStartElement(COMPARTMENT);
		xmlw.writeAttribute("id", "default");
		xmlw.writeAttribute("size", "1");
		xmlw.writeEndElement();// compartment
		xmlw.writeEndElement();// compartments
	}

	/**
	 * Writes model information to the SBML file.
	 * 
	 * @param xmlw
	 *            XML stream
	 * @throws XMLStreamException
	 * @throws NullValueException
	 * @throws AccessDeniedException
	 */
	private void writeModel(XMLStreamWriter xmlw) throws XMLStreamException, NullValueException, AccessDeniedException {

		// info for model
		xmlw.writeStartElement("model");
		xmlw.writeAttribute("id", "ONDEX_Export");
		xmlw.writeAttribute("name", "ONDEX Export");

		// only one default compartment
		writeCompartments(xmlw);

		// write all species first
		listOfSpecies(xmlw);

		// write all reactions next
		listOfReactions(xmlw);

		xmlw.writeEndElement();// model
	}

	/**
	 * Writes the header notes to the SBML file.
	 * 
	 * @param xmlw
	 *            XML stream
	 * @throws XMLStreamException
	 */
	private void writeNotes(XMLStreamWriter xmlw) throws XMLStreamException {

		xmlw.writeStartElement("notes");

		xmlw.writeStartElement("body");
		xmlw.writeDefaultNamespace(W3_NS);
		xmlw.writeAttribute("lang", "en");

		// some provenance info here
		xmlw.writeStartElement("p");
		xmlw.writeCharacters("Written as part of an ONDEX (url http://ondex.org/) export");

		xmlw.writeEndElement();// p
		xmlw.writeEndElement();// body
		xmlw.writeEndElement();// notes
	}

	/**
	 * Writes the XML syntax of a reaction in SBML.
	 * 
	 * @param xmlw
	 *            XML stream
	 * @param id
	 *            current reaction id
	 * @param name
	 *            current reaction name
	 * @param lists
	 *            [0] array of reactants [1] array of products [2] array of
	 *            possible modifiers
	 * @throws XMLStreamException
	 */
	private void writeReaction(XMLStreamWriter xmlw, String id, String name, String[][] lists) throws XMLStreamException {

		// reaction specific info
		xmlw.writeStartElement(REACTION);
		xmlw.writeAttribute("id", id);
		xmlw.writeAttribute("name", name);
		xmlw.writeAttribute("reversible", "false");

		// write reactants
		writeSpeciesRefs(xmlw, LISTOFREACTANTS, lists[0]);

		// write products
		writeSpeciesRefs(xmlw, LISTOFPRODUCTS, lists[1]);

		// write possible modifiers
		if (lists[2].length > 0)
			writeSpeciesRefs(xmlw, LISTOFMODIFIERS, lists[2]);

		// write default kinetic element
		xmlw.writeStartElement("kineticLaw");
		xmlw.writeStartElement("math");
		xmlw.writeAttribute("xmlns", "http://www.w3.org/1998/Math/MathML");
		xmlw.writeStartElement("cn");
		xmlw.writeAttribute("type", "integer");
		xmlw.writeCharacters("1");

		xmlw.writeEndElement();// cn
		xmlw.writeEndElement();// math
		xmlw.writeEndElement();// kLaw
		xmlw.writeEndElement();// reaction
	}

	/**
	 * Writes the XML syntax of a species in SBML.
	 * 
	 * @param xmlw
	 *            XML stream
	 * @param name
	 *            current species name
	 * @param id
	 *            current species id
	 * @throws XMLStreamException
	 */
	private void writeSpecies(XMLStreamWriter xmlw, String name, String id, String sbo) throws XMLStreamException {

		// species info
		xmlw.writeStartElement(SPECIES);
		xmlw.writeAttribute("id", id);
		xmlw.writeAttribute("name", name);
		xmlw.writeAttribute("compartment", "default");
		if (sbo != null)
			xmlw.writeAttribute(SBOTERM, sbo);
		xmlw.writeAttribute("initialAmount", "0");
		xmlw.writeAttribute("boundaryCondition", "true");
		xmlw.writeEndElement();// species
	}

	/**
	 * Writes a list of species references in XML SBML syntax.
	 * 
	 * @param xmlw
	 *            XML stream
	 * @param name
	 *            name of references list, e.g. LISTOFREACTANTS, LISTOFPRODUCTS
	 *            or LISTOFMODIFIERS
	 * @param list
	 *            the actual list of species IDs
	 * @throws XMLStreamException
	 */
	private void writeSpeciesRefs(XMLStreamWriter xmlw, String name, String[] list) throws XMLStreamException {
		xmlw.writeStartElement(name);

		// decide what type it is
		String elName;
		if (name.equals(LISTOFMODIFIERS)) {
			elName = MODIFIERSPECIESREFERENCE;
		} else {
			elName = SPECIESREFERENCE;
		}

		// write references
		for (String s : list) {
			xmlw.writeStartElement(elName);
			xmlw.writeAttribute("species", s);
			xmlw.writeEndElement();
		}
		xmlw.writeEndElement();
	}

	/**
	 * @return a list of required validators as string tags
	 */
	@Override
	public String[] requiresValidators() {
		return new String[0];
	}
}
