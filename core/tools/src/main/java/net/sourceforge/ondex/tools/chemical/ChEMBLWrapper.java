package net.sourceforge.ondex.tools.chemical;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.tools.MetaDataUtil;
import net.sourceforge.ondex.tools.data.ChemicalStructure;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides XML parsing functions to extract Ondex concepts from ChEMBL web
 * service calls
 * 
 * @author taubertj
 * 
 */
public class ChEMBLWrapper {

	/**
	 * Queries XML element list for given tag. Returns null if not found.
	 * 
	 * @param sTag
	 * @param eElement
	 * @return
	 */
	public static String getTagValue(String sTag, Element eElement) {
		if (eElement.getElementsByTagName(sTag) == null
				|| eElement.getElementsByTagName(sTag).item(0) == null)
			return null;

		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

	/**
	 * Attributes for compound
	 */
	private AttributeName anChemicalStructure, anInChiKey, anMass, anKnownDrug,
			anMedChemFriendly, anPassesRuleOfThree, anSpecies, anOrg, anType;

	/**
	 * ConceptClass for compound, target etc
	 */
	private ConceptClass ccComp, ccTarget;

	/**
	 * Of DataSource ChEMBL and related
	 */
	private DataSource dsCHEMBL, dsTARGET, dsUNIPROTKB;

	/**
	 * Standard evidence type
	 */
	private EvidenceType evidencetype;

	/**
	 * wrapped Ondex graph
	 */
	private ONDEXGraph graph;

	/**
	 * Sets graph to work with and initialises meta data
	 * 
	 * @param graph
	 *            ONDEXGraph
	 */
	public ChEMBLWrapper(ONDEXGraph graph) {
		this.graph = graph;
		initMetaData();
	}

	/**
	 * All meta data associated with a ChEMBL compound concept
	 */
	private void initMetaData() {

		// used to safely fetch meta data, i.e. create if not exist
		MetaDataUtil mdu = new MetaDataUtil(graph.getMetaData(), null);

		// attribute names
		anChemicalStructure = mdu.safeFetchAttributeName("ChemicalStructure",
				ChemicalStructure.class);
		anInChiKey = mdu.safeFetchAttributeName("InChIKey", String.class);
		anMass = mdu.safeFetchAttributeName("Mass", Double.class);
		anKnownDrug = mdu.safeFetchAttributeName("KnownDrug", Boolean.class);
		anMedChemFriendly = mdu.safeFetchAttributeName("MedChemFriendly",
				Boolean.class);
		anPassesRuleOfThree = mdu.safeFetchAttributeName("PassesRuleOfThree",
				Boolean.class);
		anSpecies = mdu.safeFetchAttributeName("Species", String.class);
		anOrg = mdu.safeFetchAttributeName("Organism", String.class);
		anType = mdu.safeFetchAttributeName("Type", String.class);

		// concept classes
		ConceptClass ccThing = graph.getMetaData().getConceptClass("Thing");
		if (ccThing == null)
			ccThing = graph.getMetaData().getFactory()
					.createConceptClass("Thing");
		ccComp = mdu.safeFetchConceptClass("Comp", "Compound", ccThing);
		ccTarget = mdu.safeFetchConceptClass("Target", "Target", ccThing);

		// data sources
		dsCHEMBL = mdu.safeFetchDataSource("CHEMBL");
		dsTARGET = mdu.safeFetchDataSource("CHEMBLTARGET");
		dsUNIPROTKB = mdu.safeFetchDataSource("UNIPROTKB");

		// evidence types
		evidencetype = mdu.safeFetchEvidenceType("IMPD");
	}

	/**
	 * Extract Ondex compound concept from ChEMBL XML element.
	 * 
	 * @param eElement
	 * @param c
	 *            create a new Ondex concept if null
	 * @return ONDEXConcept
	 */
	public ONDEXConcept parseCompound(Element eElement, ONDEXConcept c) {

		// unique ChEMBL identifier
		String key = getTagValue("chemblId", eElement);
		if (c == null)
			c = graph.getFactory().createConcept(key, dsCHEMBL, ccComp,
					evidencetype);
		c.createConceptAccession(key, dsCHEMBL, false);

		// get concept name
		String preferredCompoundName = getTagValue("preferredCompoundName",
				eElement);
		if (preferredCompoundName != null)
			c.createConceptName(preferredCompoundName, true);

		// set formular as annotation
		String molecularFormula = getTagValue("molecularFormula", eElement);
		if (molecularFormula != null)
			c.setAnnotation(molecularFormula);

		// add chemical structure to concept
		String smiles = getTagValue("smiles", eElement);
		if (smiles != null) {
			ChemicalStructure cs = new ChemicalStructure();
			cs.setSMILES(smiles);
			c.createAttribute(anChemicalStructure, cs, false);
		}

		// add InChiKey
		String stdInChiKey = getTagValue("stdInChiKey", eElement);
		if (stdInChiKey != null)
			c.createAttribute(anInChiKey, stdInChiKey, false);

		// get molecular weight
		String molecularWeight = getTagValue("molecularWeight", eElement);
		if (molecularWeight != null)
			c.createAttribute(anMass, Double.valueOf(molecularWeight), false);

		// add KnownDrug attribute
		String knownDrug = getTagValue("knownDrug", eElement);
		if ("Yes".equals(knownDrug))
			c.createAttribute(anKnownDrug, Boolean.TRUE, false);
		else if ("No".equals(knownDrug))
			c.createAttribute(anKnownDrug, Boolean.FALSE, false);
		else if (knownDrug != null)
			System.out.println("Unknown value for knownDrug: " + knownDrug);

		// add MedChemFriendly attribute
		String medChemFriendly = getTagValue("medChemFriendly", eElement);
		if ("Yes".equals(medChemFriendly))
			c.createAttribute(anMedChemFriendly, Boolean.TRUE, false);
		else if ("No".equals(medChemFriendly))
			c.createAttribute(anMedChemFriendly, Boolean.FALSE, false);
		else if (medChemFriendly != null)
			System.out.println("Unknow value for medChemFriendly: "
					+ medChemFriendly);

		// add PassesRuleOfThree attribute
		String passesRuleOfThree = getTagValue("passesRuleOfThree", eElement);
		if ("Yes".equals(passesRuleOfThree))
			c.createAttribute(anPassesRuleOfThree, Boolean.TRUE, false);
		else if ("No".equals(passesRuleOfThree))
			c.createAttribute(anPassesRuleOfThree, Boolean.FALSE, false);
		else if (passesRuleOfThree != null)
			System.out.println("Unknown value of passesRuleOfThree: "
					+ passesRuleOfThree);

		// get species
		String species = getTagValue("species", eElement);
		if (species != null)
			c.createAttribute(anSpecies, species, false);

		return c;
	}

	/**
	 * Extract Ondex target concept from ChEMBL XML element.
	 * 
	 * @param eElement
	 * @param existing
	 *            create a new Ondex concept if null
	 * @return ONDEXConcept
	 */
	public ONDEXConcept parseTarget(Element eElement, ONDEXConcept existing) {

		ONDEXConcept c = existing;

		// new target concept
		String key = getTagValue("chemblId", eElement);
		String desc = getTagValue("description", eElement);
		if (desc == null || desc.equals("Unspecified"))
			desc = "";
		if (c == null)
			c = graph.getFactory().createConcept(key, desc, dsCHEMBL, ccTarget,
					evidencetype);
		else
			c.setDescription(desc);

		// add concept names
		String preferredName = getTagValue("preferredName", eElement);
		if (preferredName != null)
			c.createConceptName(preferredName, true);

		if (getTagValue("synonyms", eElement) != null) {
			String[] synonyms = getTagValue("synonyms", eElement).split(";");
			for (String s : synonyms) {
				if (!s.equals("Unspecified") && s.trim().length() > 0)
					c.createConceptName(s.trim(), false);
			}
		}

		if (getTagValue("geneNames", eElement) != null) {
			String[] geneNames = getTagValue("geneNames", eElement).split(";");
			for (String s : geneNames) {
				if (!s.equals("Unspecified") && s.trim().length() > 0)
					c.createConceptName(s.trim(), false);
			}
		}

		// add ChEMBL ID as accession
		if (existing == null)
			c.createConceptAccession(key, dsTARGET, false);

		String protAcc = getTagValue("proteinAccession", eElement);
		if (protAcc != null && !protAcc.equals("Unspecified")
				&& protAcc.trim().length() > 0)
			c.createConceptAccession(protAcc.trim(), dsUNIPROTKB, false);

		// add type attribute
		String type = getTagValue("targetType", eElement);
		if (type != null)
			c.createAttribute(anType, type, false);

		// add organism attribute
		String organism = getTagValue("organism", eElement);
		if (organism != null)
			c.createAttribute(anOrg, organism, false);

		return c;
	}

}
