package net.sourceforge.ondex.parser.go;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.go.data.OboConcept;
import net.sourceforge.ondex.parser.go.data.OboImporter;
import net.sourceforge.ondex.parser.go.data.StringMod;

import org.apache.log4j.Level;

/**
 * GO Ontology Parser for the file: "gene_ontology_edit.obo" in OBO v1.2 format
 * from http://www.geneontology.org/ontology/gene_ontology_edit.obo
 * <p/>
 * latest tested version: 06/2011
 * 
 * @author rwinnenb, keywan
 */
@Status(description = "GO OBO Parser. Works with the filtered ontology; cross-products, inter-ontology links, and has_part relationships removed. Tested Septemder 2013 (Artem Lysenko)", status = StatusType.STABLE)
@Authors(authors = { "Keywan Hassani-Pak" }, emails = { "keywan at users.sourceforge.net" })
@DatabaseTarget(name = "GO", description = "A parser for the Gene Ontology.", version = "OBO 1.2", url = "http://www.geneontology.org/")
@DataURL(name = "GO", description = "GO Filtered ontology; cross-products, inter-ontology links, and has_part relationships removed", urls = { "http://geneontology.org/ontology/obo_format_1_2/gene_ontology.1_2.obo" })
public class Parser extends ONDEXParser {

	// counts how many times activity accorred in GO termName
	private int activity_chopped = 0;

	// ondex meta data
	private DataSource dataSourceGO, dataSourceEC, dataSourceTC, dataSourceAC,
			dataSourceMC, dataSourceRESID, dataSourceUME, dataSourceUMR,
			dataSourceUMP, dataSourceREAC, dataSourceWIKI, dataSourceKEGG,
			dataSourcePO, dataSourceRHEA;

	private EvidenceType et;

	private ConceptClass ccMolFunc, ccBioProc, ccCelComp;

	// define here the abbrev. for the relations to be used
	private Map<String, RelationType> relation_name = new TreeMap<String, RelationType>();

	// contains mapping concering context
	private Map<ONDEXEntity, Set<ONDEXConcept>> contextMap = new Hashtable<ONDEXEntity, Set<ONDEXConcept>>();

	private Boolean isGOSLIM;

	/**
	 * Returns name of this parser.
	 * 
	 * @return String
	 */
	public String getName() {
		return new String("GO");
	}

	/**
	 * Returns version of this parser.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return new String("08.01.2008");
	}

	@Override
	public String getId() {
		return "go";
	}

	/**
	 * Just one ArgumentDefinition for obsolete parsing.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new BooleanArgumentDefinition(ArgumentNames.OBSOLETES_ARG,
						ArgumentNames.OBSOLETES_ARG_DESC, false, false),
				new BooleanArgumentDefinition(ArgumentNames.IS_GOSLIM_ARG,
						ArgumentNames.IS_GOSLIM_ARG_DESC, false, false),
				new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
						FileArgumentDefinition.INPUT_FILE_DESC, true, true,
						false, false), };
	}

	/**
	 * Sets AbstractONDEXGraph and starts actual processing.
	 */
	public void start() throws InvalidPluginArgumentException {

		isGOSLIM = (Boolean) args.getUniqueValue(ArgumentNames.IS_GOSLIM_ARG);

		GeneralOutputEvent goe = new GeneralOutputEvent(
				"Starting GO parsing...", getCurrentMethodName());
		goe.setLog4jLevel(Level.INFO);
		fireEventOccurred(goe);

		/** GENERIC PART FOR OBO FILES */

		OboImporter imp = new OboImporter();

		File go_obo_file = new File(
				(String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

		goe = new GeneralOutputEvent("Using GO flatfile " + go_obo_file,
				getCurrentMethodName());
		goe.setLog4jLevel(Level.INFO);
		fireEventOccurred(goe);

		// contains OBO parsing results
		List<OboConcept> results = null;

		try {
			results = imp.getFileContent(go_obo_file);
		} catch (FileNotFoundException fnfe) {
			fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(),
					getCurrentMethodName()));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					getCurrentMethodName()));
		}

		// get ONDEX metadata for GO database
		boolean ready = getMetaData(graph);

		// meta data correct
		if (ready) {

			// parse obsoletes
			Boolean getObsoletes = (Boolean) args
					.getUniqueValue(ArgumentNames.OBSOLETES_ARG);
			if (getObsoletes == null)
				getObsoletes = false;

			if (!getObsoletes) {
				goe = new GeneralOutputEvent("Ignoring obsolete terms.",
						getCurrentMethodName());
				goe.setLog4jLevel(Level.INFO);
				fireEventOccurred(goe);
			} else {
				goe = new GeneralOutputEvent("Also parsing obsolete terms.",
						getCurrentMethodName());
				goe.setLog4jLevel(Level.INFO);
				fireEventOccurred(goe);
			}

			Map<String, ONDEXConcept> createdConcepts = createConcepts(graph,
					results, getObsoletes);

			createRelations(graph, results, createdConcepts);

			createContext(graph, results, createdConcepts, getObsoletes);

			goe = new GeneralOutputEvent("Chopped of \"activity\" "
					+ this.activity_chopped + " times.",
					getCurrentMethodName());
			goe.setLog4jLevel(Level.INFO);
			fireEventOccurred(goe);

			goe = new GeneralOutputEvent("GO parsing finished!",
					getCurrentMethodName());
			goe.setLog4jLevel(Level.INFO);
			fireEventOccurred(goe);
		}
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	/**
	 * Setup ONDEX meta data, returns success.
	 * 
	 * @param graph
	 *            AbstractONDEXGraph
	 * @return boolean
	 */
	private boolean getMetaData(ONDEXGraph graph) {

		// success?
		boolean ready = true;

		String goCV = MetaData.cvGO;

		if (isGOSLIM) {
			goCV = MetaData.cvGOSLIM;
		}

		// DataSource: GO
		dataSourceGO = graph.getMetaData().getDataSource(goCV);
		if (dataSourceGO == null) {
			fireEventOccurred(new DataSourceMissingEvent(goCV,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: EC
		dataSourceEC = graph.getMetaData().getDataSource(MetaData.cvEC);
		if (dataSourceEC == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvEC,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: TC
		dataSourceTC = graph.getMetaData().getDataSource(MetaData.cvTC);
		if (dataSourceTC == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvTC,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: AC
		dataSourceAC = graph.getMetaData().getDataSource(MetaData.cvAC);
		if (dataSourceAC == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvAC,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: MC
		dataSourceMC = graph.getMetaData().getDataSource(MetaData.cvMC);
		if (dataSourceMC == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvMC,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: RESID
		dataSourceRESID = graph.getMetaData().getDataSource(MetaData.cvRESID);
		if (dataSourceRESID == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvRESID,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: UM-E
		dataSourceUME = graph.getMetaData().getDataSource(MetaData.cvUME);
		if (dataSourceUME == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvUME,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: UM-R
		dataSourceUMR = graph.getMetaData().getDataSource(MetaData.cvUMR);
		if (dataSourceUMR == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvUMR,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: UM-P
		dataSourceUMP = graph.getMetaData().getDataSource(MetaData.cvUMP);
		if (dataSourceUMP == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvUMP,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: REAC
		dataSourceREAC = graph.getMetaData().getDataSource(MetaData.cvREAC);
		if (dataSourceREAC == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvREAC,
					getCurrentMethodName()));
			ready = false;
		}

		// DataSource: WIKIPEDIA
		dataSourceWIKI = graph.getMetaData().getDataSource(MetaData.cvWIKI);
		if (dataSourceWIKI == null) {
			fireEventOccurred(new DataSourceMissingEvent(MetaData.cvWIKI,
					getCurrentMethodName()));
			ready = false;
		}

		if (graph.getMetaData().checkDataSource(MetaData.DATASOURCE_KEGG))
			dataSourceKEGG = graph.getMetaData().getDataSource(
					MetaData.DATASOURCE_KEGG);

		if (graph.getMetaData().checkDataSource(MetaData.DATASOURCE_PO))
			dataSourcePO = graph.getMetaData().getDataSource(
					MetaData.DATASOURCE_PO);

		if (graph.getMetaData().checkDataSource(MetaData.DATASOURCE_RHEA))
			dataSourceRHEA = graph.getMetaData().getDataSource(
					MetaData.DATASOURCE_RHEA);

		// ET: IMPD
		et = graph.getMetaData().getEvidenceType(MetaData.IMPD);
		if (et == null) {
			fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.IMPD,
					getCurrentMethodName()));
			ready = false;
		}

		// CC: MolFunc
		ccMolFunc = graph.getMetaData().getConceptClass(MetaData.MolFunc);
		if (ccMolFunc == null) {
			fireEventOccurred(new ConceptClassMissingEvent(MetaData.MolFunc,
					getCurrentMethodName()));
			ready = false;
		}

		// CC: BioProc
		ccBioProc = graph.getMetaData().getConceptClass(MetaData.BioProc);
		if (ccBioProc == null) {
			fireEventOccurred(new ConceptClassMissingEvent(MetaData.BioProc,
					getCurrentMethodName()));
			ready = false;
		}

		// CC: CelComp
		ccCelComp = graph.getMetaData().getConceptClass(MetaData.CelComp);
		if (ccCelComp == null) {
			fireEventOccurred(new ConceptClassMissingEvent(MetaData.CelComp,
					getCurrentMethodName()));
			ready = false;
		}

		// RTset: is_a
		RelationType is_a = graph.getMetaData().getRelationType(MetaData.is_a);
		if (is_a == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.is_a,
					getCurrentMethodName()));
			ready = false;
		} else
			relation_name.put("is_a", is_a); // defined for convenience

		// RTset: is_p
		RelationType is_p = graph.getMetaData().getRelationType(MetaData.is_p);
		if (is_p == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.is_p,
					getCurrentMethodName()));
			ready = false;
		} else
			relation_name.put("part_of", is_p);

		// RTset: has_part
		RelationType has_part = graph.getMetaData().getRelationType(
				MetaData.has_part);
		if (has_part == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.has_part,
					getCurrentMethodName()));
			ready = false;
		} else
			relation_name.put("has_part", has_part);

		// RTset: occurs_in
		RelationType occurs_in = graph.getMetaData().getRelationType(
				MetaData.occurs_in);
		if (occurs_in == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.occurs_in,
					getCurrentMethodName()));
			ready = false;
		} else
			relation_name.put("occurs_in", occurs_in);

		// RTset: results_in
		RelationType results_in = graph.getMetaData().getRelationType(
				MetaData.results_in);
		if (results_in == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.results_in,
					getCurrentMethodName()));
			ready = false;
		} else
			relation_name.put("results_in", results_in);

		// RTset: replaced_by
		RelationType rp_by = graph.getMetaData().getRelationType(
				MetaData.rtRp_by);
		if (rp_by == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.rtRp_by,
					getCurrentMethodName()));
			ready = false;
		} else
			relation_name.put("rp_by", rp_by);

		// RTset: rg_by
		RelationType rg_by = graph.getMetaData().getRelationType(
				MetaData.rtRegulates);
		if (rg_by == null) {
			fireEventOccurred(new RelationTypeMissingEvent(
					MetaData.rtRegulates, getCurrentMethodName()));
			ready = false;
		} else {
			relation_name.put("regulates", rg_by);
		}

		RelationType pos_reg = graph.getMetaData().getRelationType(
				MetaData.rtPos_reg);
		if (pos_reg == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.rtPos_reg,
					getCurrentMethodName()));
			ready = false;
		} else {
			relation_name.put("positively_regulates", pos_reg);
		}

		RelationType neg_reg = graph.getMetaData().getRelationType(
				MetaData.rtPos_reg);
		if (neg_reg == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.rtNeg_reg,
					getCurrentMethodName()));
			ready = false;
		} else {
			relation_name.put("negatively_regulates", neg_reg);
		}

		return ready;
	}

	/**
	 * Creates ONDEXConcepts from OboConcepts and returns a Map of OboConcept id
	 * to ONDEXConcept.
	 * 
	 * @param graph
	 *            AbstractONDEXGraph
	 * @param results
	 *            List<OboConcept>
	 * @param getObsoletes
	 *            boolean
	 * @return Map<String, ONDEXConcept>
	 */
	private Map<String, ONDEXConcept> createConcepts(ONDEXGraph graph,
			List<OboConcept> results, boolean getObsoletes) {

		ONDEXConcept concept = null;
		ConceptAccession accession = null;
		DataSource dataSourceAccession = null;

		// Set of unrecognized (not in metadata) data sources (xref)
		HashSet<String> createdDataSources = new HashSet<String>();

		// go through the results ArrayList and create Concepts and
		// Relations
		Hashtable<String, ONDEXConcept> createdConcepts = new Hashtable<String, ONDEXConcept>();
		for (OboConcept obo : results) {

			// check for obsolete Obo concepts
			if (!obo.isObsolete() || getObsoletes) {

				String id = obo.getId();
				// definition field in GO flat file
				String annotation = obo.getDefinition();

				if (obo.getNamespace().equalsIgnoreCase("molecular_function")) { // create
					// create ONDEXConcept
					concept = graph.getFactory().createConcept(id, annotation,
							dataSourceGO, ccMolFunc, et);
					createdConcepts.put(id, concept);
				} else if (obo.getNamespace().equalsIgnoreCase(
						"biological_process")) {
					// create ONDEXConcept
					concept = graph.getFactory().createConcept(id, annotation,
							dataSourceGO, ccBioProc, et);
					createdConcepts.put(id, concept);
				} else if (obo.getNamespace().equalsIgnoreCase(
						"cellular_component")) {
					// create ONDEXConcept
					concept = graph.getFactory().createConcept(id, annotation,
							dataSourceGO, ccCelComp, et);
					createdConcepts.put(id, concept);
				} else {
					fireEventOccurred(new InconsistencyEvent(
							"Namespace missing for GO object (" + obo.getId()
									+ ")", getCurrentMethodName()));
				}

				// create GO ConceptAccession
				accession = concept.createConceptAccession(id, dataSourceGO,
						false);

				// go through list of alternative GO ConceptAccessions
				for (int j = 0; j < obo.getAlt_ids().size(); j++) {
					accession = concept.createConceptAccession(obo.getAlt_ids()
							.get(j), dataSourceGO, true);
				}

				// go through list of ConceptAccessions from other data sources
				for (int j = 0; j < obo.getRefs().size(); j++) {
					boolean ambiguous = false;
					boolean write = false;
					String xref = obo.getRefs().get(j);
					String xref_name = "";

					if (xref.startsWith("EC:")) {
						ambiguous = false;
						dataSourceAccession = dataSourceEC;
						write = true;
						xref_name = xref.split(":")[1].trim();
						xref_name = StringMod.fillEC(xref_name);
					} else if (xref.startsWith("TC:")) {
						ambiguous = false;
						dataSourceAccession = dataSourceTC;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("AraCyc:")) {
						ambiguous = false;
						dataSourceAccession = dataSourceAC;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("MetaCyc:")) {
						ambiguous = false;
						dataSourceAccession = dataSourceMC;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("RESID:")) {
						ambiguous = true;
						dataSourceAccession = dataSourceRESID;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("UM-BBD_enzymeID:")) {
						ambiguous = false;
						dataSourceAccession = dataSourceUME;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("UM-BBD_pathwayID:")) {
						ambiguous = false;
						dataSourceAccession = dataSourceUMP;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("UM-BBD_reactionID:")) {
						ambiguous = false;
						dataSourceAccession = dataSourceUMP;
						write = true;
						xref_name = xref.split(":")[1];
					} else if (xref.startsWith("Reactome:")) {
						ambiguous = false;
						dataSourceAccession = dataSourceREAC;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("Wikipedia:")) {
						ambiguous = true;
						dataSourceAccession = dataSourceWIKI;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("KEGG:")) {
						ambiguous = true;
						dataSourceAccession = dataSourceKEGG;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("PO:")) {
						ambiguous = true;
						dataSourceAccession = dataSourcePO;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else if (xref.startsWith("RHEA:")) {
						ambiguous = true;
						dataSourceAccession = dataSourceRHEA;
						write = true;
						xref_name = xref.split(":")[1].trim();
					} else {
						ambiguous = true;
						String dataSource = xref.split(":")[0];
						xref_name = xref.split(":")[1].trim();
						write = true;

						if (!graph.getMetaData().checkDataSource(dataSource))
							graph.getMetaData().createDataSource(dataSource,
									dataSource, "");

						dataSourceAccession = graph.getMetaData()
								.getDataSource(dataSource);

						createdDataSources.add(dataSource);

						// fireEventOccurred(new DataSourceMissingEvent(
						// "Database \""
						// + xref
						// +
						// "\" referenced from xref field in GO file unknown!",
						// getCurrentMethodName()));
					}

					if (write) {
						if (xref_name.indexOf(" ") > -1)
							xref_name = xref_name.substring(0, xref_name.indexOf(" "));
						accession = concept.createConceptAccession(xref_name,
								dataSourceAccession, ambiguous);
					}
				}

				// create preferred ConceptName
				String concept_name = obo.getName();

				// get rid of activity (fillword, EC does not have this word for
				// their enzyme classes)
				concept_name = StringMod.removeUnwanted(concept_name.trim());
				if (!concept_name.equals(concept_name = StringMod
						.removeActivity(concept_name))) {
					activity_chopped++;
				}
				concept.createConceptName(concept_name, true);

				// represents already written syns - prevents duplication
				// accross syn types
				Set<String> writtenSyns = new HashSet<String>();
				writtenSyns.add(concept_name);

				for (String synType : obo.getSynonyms().keySet()) {
					// write exact syns first these are the most important
					if (synType.equalsIgnoreCase(OboConcept.exactSynonym)) {
						for (String s : obo.getSynonyms().get(synType)) {
							String trim = StringMod.removeUnwanted(s.trim());
							String syn = StringMod.removeActivity(trim);

							// get rid of activity (fillword, EC does not have
							// this word for their enzyme classes)
							if (!trim.equals(syn)) {
								activity_chopped++;
							}

							if (!writtenSyns.contains(syn)
									&& !syn.equalsIgnoreCase(concept_name)
									&& syn.length() > 0) {
								// pref exact
								concept.createConceptName(syn, true);
								writtenSyns.add(syn);
							}
						}

					} else {
						for (String s : obo.getSynonyms().get(synType)) {
							String trim = StringMod.removeUnwanted(s.trim());
							String syn = StringMod.removeActivity(trim);

							// get rid of activity (fillword, EC does not have
							// this word for their enzyme classes)
							if (!trim.equals(syn)) {
								activity_chopped++;
							}

							if (!syn.equalsIgnoreCase(concept_name)
									&& syn.length() > 0
									&& !writtenSyns.contains(syn)) {
								// NB there was no need to do 2nd part for
								// exactSyns as each type of syns is a set
								concept.createConceptName(syn, false);
								writtenSyns.add(syn);
							}
						}
					}
				}
			}
		}

		if (createdDataSources.size() > 0) {
			GeneralOutputEvent goe = new GeneralOutputEvent(
					"Data Sources (Xrefs) that are not in the Ondex MetaData: "
							+ createdDataSources.toString(),
					getCurrentMethodName());
			fireEventOccurred(goe);
		}

		return createdConcepts;
	}

	/**
	 * Create ONDEXRelations from the relations tags of OboConcepts. Returns the
	 * set of valid taxonomy id mappings for sensu.
	 * 
	 * @param graph
	 *            AbstractONDEXGraph
	 * @param results
	 *            List<OboConcept>
	 * @param createdConcepts
	 *            Map<String, ONDEXConcept>
	 */
	private void createRelations(ONDEXGraph graph, List<OboConcept> results,
			Map<String, ONDEXConcept> createdConcepts) {

		ONDEXConcept fromConcept = null;
		ONDEXConcept toConcept = null;

		// create ONDEXRelations
		RelationType rtset = null;

		for (OboConcept obo : results) {
			if (createdConcepts.containsKey(obo.getId())) {

				// get fromConcept from ID
				fromConcept = createdConcepts.get(obo.getId());

				for (int j = 0; j < obo.getRelations().size(); j++) {
					String[] relation = obo.getRelations().get(j);

					// get current RelationType
					String rel_type_orig = relation[0];
					rtset = relation_name.get(rel_type_orig);

					if (rtset == null) {
						fireEventOccurred(new RelationTypeMissingEvent(
								"Relation Typset for \"" + rel_type_orig
										+ "\" missing!",
								"[Parser - createRelations]"));
					} else {

						// get toConcept from ID
						String to_id = relation[1];
						toConcept = createdConcepts.get(to_id);

						if (toConcept == null) {
							fireEventOccurred(new InconsistencyEvent(
									"Missing Concept ID: " + to_id,
									"[Parser - createRelations]"));
						} else {
							graph.getFactory().createRelation(fromConcept,
									toConcept, rtset, et);
						}
					}
				}

				if (obo.getReplacement() != null) {
					rtset = relation_name.get(MetaData.rtRp_by);

					if (rtset == null) {
						fireEventOccurred(new RelationTypeMissingEvent(
								"Relation Typset for \"" + MetaData.rtRp_by
										+ "\" missing!",
								"[Parser - createRelations]"));
					} else {

						toConcept = createdConcepts.get(obo.getReplacement());

						if (toConcept == null) {
							fireEventOccurred(new InconsistencyEvent(
									"Missing Concept ID: "
											+ obo.getReplacement(),
									"[Parser - createRelations]"));
						} else {
							graph.getFactory().createRelation(fromConcept,
									toConcept, rtset, et);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates context from top-level GO terms.
	 * 
	 * @param graph
	 *            AbstractONDEXGraph
	 * @param results
	 *            List<OboConcept>
	 * @param createdConcepts
	 *            Map<String, ONDEXConcept>
	 * @param getObsoletes
	 *            boolean
	 */
	private void createContext(ONDEXGraph graph, List<OboConcept> results,
			Map<String, ONDEXConcept> createdConcepts, boolean getObsoletes) {

		// index oboconcepts by id
		Map<String, OboConcept> oboconcepts = new Hashtable<String, OboConcept>();
		for (OboConcept obo : results) {
			if (!obo.isObsolete() || getObsoletes) {
				oboconcepts.put(obo.getId(), obo);
			}
		}

		// get toplevel
		Set<String> toplevel = new HashSet<String>();
		ONDEXConcept concept = null;
		Iterator<String> it = oboconcepts.keySet().iterator();
		while (it.hasNext()) {
			String id = it.next();
			OboConcept obo = oboconcepts.get(id);
			concept = createdConcepts.get(id);
			if (!obo.isObsolete() && obo.getRelations().size() == 0) {
				toplevel.add(id);
				// toplevel concept is also context of itself
				concept.addTag(concept);
			}
		}

		// get sublevel
		Set<String> sublevel = new HashSet<String>();
		it = oboconcepts.keySet().iterator();
		while (it.hasNext()) {
			String id = it.next();
			OboConcept obo = oboconcepts.get(id);
			// more specific term
			ONDEXConcept fromConcept = createdConcepts.get(id);
			if (obo.getRelations().size() == 1) {
				Iterator<String[]> itr = obo.getRelations().iterator();
				String[] rel = itr.next();
				if (toplevel.contains(rel[1])) {
					sublevel.add(id);
					// more general term
					ONDEXConcept toConcept = createdConcepts.get(rel[1]);
					fromConcept.addTag(toConcept);
					fromConcept.addTag(fromConcept);
					ONDEXRelation r = graph.getRelation(fromConcept, toConcept,
							relation_name.get(rel[0]));
					r.addTag(toConcept);
				}
			}
		}

		// recurse for context assignment
		it = oboconcepts.keySet().iterator();
		while (it.hasNext()) {
			String id = it.next();
			recurseForContext(graph, sublevel, createdConcepts, oboconcepts,
					new HashSet<ONDEXEntity>(), id);
		}

		for (ONDEXEntity ondexEntity : contextMap.keySet()) {
			ONDEXEntity v = ondexEntity;
			for (ONDEXConcept ondexConcept : contextMap.get(v)) {
				v.addTag(ondexConcept);
			}
		}
	}

	/**
	 * Recurses to assign context to sub level concepts.
	 * 
	 * @param graph
	 *            AbstractONDEXGraph
	 * @param contextlevel
	 *            Set<String>
	 * @param createdConcepts
	 *            Map<String, ONDEXConcept>
	 * @param oboconcepts
	 *            Map<String, OboConcept>
	 * @param visited
	 *            HashSet<Viewable>
	 * @param current
	 *            String
	 */
	private void recurseForContext(ONDEXGraph graph, Set<String> contextlevel,
			Map<String, ONDEXConcept> createdConcepts,
			Map<String, OboConcept> oboconcepts, HashSet<ONDEXEntity> visited,
			String current) {

		// check if contextlevel element is reached
		boolean atContextLevel = false;
		OboConcept obo = oboconcepts.get(current);
		Vector<String[]> rels = new Vector<String[]>(obo.getRelations());
		if (obo.getReplacement() != null)
			rels.add(new String[] { "rp_by", obo.getReplacement() });
		Iterator<String[]> itr = rels.iterator();
		for (String[] rel1 : rels) {
			String[] rel = rel1;
			if (contextlevel.contains(rel[1])) {
				atContextLevel = true;
				ONDEXConcept context = createdConcepts.get(rel[1]);
				ONDEXConcept fromConcept = createdConcepts.get(current);
				ONDEXRelation r = graph.getRelation(fromConcept, context,
						relation_name.get(rel[0]));
				visited.add(r);
				visited.add(fromConcept);
				for (ONDEXEntity v : visited) {
					if (!contextMap.containsKey(v)) {
						contextMap.put(v, new HashSet<ONDEXConcept>());
					}
					contextMap.get(v).add(context);
				}
			}
		}

		// not recursion base
		if (!atContextLevel) {
			ONDEXConcept fromConcept = createdConcepts.get(current);
			visited.add(fromConcept);
			itr = rels.iterator();
			while (itr.hasNext()) {
				String[] rel = itr.next();
				ONDEXConcept toConcept = createdConcepts.get(rel[1]);
				visited.add(toConcept);
				ONDEXRelation r = graph.getRelation(fromConcept, toConcept,
						relation_name.get(rel[0]));
				visited.add(r);
				// recursion step
				recurseForContext(graph, contextlevel, createdConcepts,
						oboconcepts, visited, rel[1]);
			}
		}
	}
}