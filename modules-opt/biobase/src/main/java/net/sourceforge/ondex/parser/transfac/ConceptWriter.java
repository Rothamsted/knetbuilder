package net.sourceforge.ondex.parser.transfac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.transfac.factor.FactorObject;
import net.sourceforge.ondex.parser.transfac.factor.gene.GeneObject;
import net.sourceforge.ondex.parser.transfac.matrix.MatrixObject;
import net.sourceforge.ondex.parser.transfac.sink.Publication;
import net.sourceforge.ondex.parser.transfac.site.SiteObject;

/**
 * Provides the interface to the AbstractONDEXGraph for writing concepts and
 * relations.
 * 
 * @author taubertj
 * 
 */
public class ConceptWriter {

	// tf accession prepender
	private static String TFACC = "TF_";

	// current AbstractONDEXGraph
	private ONDEXGraph aog;

	// ONDEX metadata
	private DataSource dataSourceTF;
	private DataSource dataSourcePMID;
	private DataSource dataSourceTAIR;
	private EvidenceType evi;

	private AttributeName aaAttr;
	private AttributeName naAttr;
	private AttributeName tfbss;
	private AttributeName tfmAttr;
	private AttributeName urlAttr;
	private AttributeName taxAttr;
	private AttributeName authorAttr;

	private ConceptClass ccTF;
	private ConceptClass ccTFBS;
	private ConceptClass ccGene;
	private ConceptClass ccPWM;
	private ConceptClass ccPublication;
	private ConceptClass ccProtein;

	private RelationType en_by;
	private RelationType rg_by;
	private RelationType h_pwm;
	private RelationType pub_in;
	private RelationType it_wi;
	private RelationType bi_to;
	private RelationType si_to;
	private RelationType is_a;

	// mapping gene id to ONDEXConcept
	private HashMap<String, ONDEXConcept> genesWritten = new HashMap<String, ONDEXConcept>();

	// mapping matrix id to ONDEXConcept
	private HashMap<String, ONDEXConcept> matricesWritten = new HashMap<String, ONDEXConcept>();

	// mapping publications to ONDEXConcept
	private HashMap<String, ONDEXConcept> publicationsWritten = new HashMap<String, ONDEXConcept>();

	// mapping factor id to ONDEXConcept
	private HashMap<String, ONDEXConcept> factorsWritten = new HashMap<String, ONDEXConcept>();

	// mapping site id to ONDEXConcept
	private HashMap<String, ONDEXConcept> sitesWritten = new HashMap<String, ONDEXConcept>();

	// mapping site id to factors
	private HashMap<String, HashSet<String>> sitesToTransFacs = new HashMap<String, HashSet<String>>();

	// relations for factor interaction
	private HashMap<String, String> factorInteraction = new HashMap<String, String>();

	/**
	 * Constructor for a given AbstractONDEXGraph.
	 * 
	 * @param aog -
	 *            AbstractONDEXGraph
	 */
	protected ConceptWriter(ONDEXGraph aog) {
		this.aog = aog;

		// get all the ONDEX metadata straight

		String methodName = "ConceptWriter(AbstractONDEXGraph aog)";

		// CVs
		dataSourceTF = aog.getMetaData().getDataSource("TF");
		if (dataSourceTF == null)
			Parser.propagateEventOccurred(new DataSourceMissingEvent(
					"TransFac DataSource is null", methodName));

		dataSourcePMID = aog.getMetaData().getDataSource("NLM");
		if (dataSourcePMID == null)
			Parser.propagateEventOccurred(new DataSourceMissingEvent(
					"TransFac PMID is null", methodName));

		dataSourceTAIR = aog.getMetaData().getDataSource("TAIR");
		if (dataSourceTAIR == null)
			Parser.propagateEventOccurred(new DataSourceMissingEvent(
					"TransFac dataSourceTAIR is null", methodName));

		// EvidenceTypes
		evi = aog.getMetaData().getEvidenceType("IMPD");
		if (evi == null)
			Parser.propagateEventOccurred(new EvidenceTypeMissingEvent(
					"TransFac evi is null", methodName));

		// ConceptClasses
		ccTF = aog.getMetaData().getConceptClass("TF");
		if (ccTF == null)
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(
					"TransFac ccTF is null", methodName));

		ccTFBS = aog.getMetaData().getConceptClass("TFBS");
		if (ccTFBS == null)
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(
					"TransFac ccTF is null", methodName));

		ccGene = aog.getMetaData().getConceptClass("Gene");
		if (ccGene == null)
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(
					"TransFac ccGene is null", methodName));

		ccPWM = aog.getMetaData().getConceptClass("PWM");
		if (ccPWM == null)
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(
					"TransFac ccPWM is null", methodName));

		ccPublication = aog.getMetaData().getConceptClass("Publication");
		if (ccPublication == null)
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(
					"TransFac ccPublication is null", methodName));

		ccProtein = aog.getMetaData().getConceptClass("Protein");
		if (ccProtein == null)
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(
					"TransFac ccProtein is null", methodName));

		// AttributeNames
		taxAttr = aog.getMetaData().getAttributeName("TAXID");
		if (taxAttr == null)
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(
					"TransFac taxAttr is null", methodName));

		tfbss = aog.getMetaData().getAttributeName("TFBSS");
		if (tfbss == null)
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(
					"TransFac TFBSS is null", methodName));

		authorAttr = aog.getMetaData().getAttributeName("AUTHORS");
		if (authorAttr == null)
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(
					"TransFac aaAttr is null", methodName));

		aaAttr = aog.getMetaData().getAttributeName("AA");
		if (aaAttr == null)
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(
					"TransFac aaAttr is null", methodName));

		naAttr = aog.getMetaData().getAttributeName("NA");
		if (naAttr == null)
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(
					"TransFac naAttr is null", methodName));

		tfmAttr = aog.getMetaData().getAttributeName("TFM");
		if (tfmAttr == null)
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(
					"TransFac tfmAttr is null", methodName));

		urlAttr = aog.getMetaData().getAttributeName("URL");
		if (urlAttr == null)
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(
					"TransFac urlAttr is null", methodName));

		// RelationTypes
		en_by = aog.getMetaData().getRelationType("en_by");
		if (en_by == null)
			en_by = getRelationType("en_by");

		rg_by = aog.getMetaData().getRelationType("rg_by");
		if (rg_by == null)
			rg_by = getRelationType("rg_by");

		h_pwm = aog.getMetaData().getRelationType("h_pwm");
		if (h_pwm == null)
			h_pwm = getRelationType("h_pwm");

		it_wi = aog.getMetaData().getRelationType("it_wi");
		if (it_wi == null)
			it_wi = getRelationType("it_wi");

		pub_in = aog.getMetaData().getRelationType("pub_in");
		if (pub_in == null)
			pub_in = getRelationType("pub_in");

		bi_to = aog.getMetaData().getRelationType("bi_to");
		if (bi_to == null)
			bi_to = getRelationType("bi_to");

		si_to = aog.getMetaData().getRelationType("situated_to");
		if (si_to == null)
			si_to = getRelationType("situated_to");

		is_a = aog.getMetaData().getRelationType("is_a");
		if (is_a == null)
			is_a = getRelationType("is_a");

		if (en_by == null)
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					"RelationType en_by is null", methodName));
		if (rg_by == null)
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					"RelationType rg_by is null", methodName));
		if (h_pwm == null)
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					"RelationType h_pwm is null", methodName));
		if (it_wi == null)
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					"RelationType it_wi is null", methodName));
		if (pub_in == null)
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					"RelationType pub_in is null", methodName));
		if (bi_to == null)
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					"RelationType bi_to is null", methodName));
		if (si_to == null)
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					"RelationType si_to is null", methodName));
		if (is_a == null)
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					"RelationType is_a is null", methodName));
	}

	/**
	 * Create a RelationType, if missing in metadata.
	 * 
	 * @param name
	 *            Name of RelationType to be contained
	 * @return RelationType
	 */
	private RelationType getRelationType(String name) {
		RelationType rt = aog.getMetaData().getRelationType(name);
		if (rt != null) {
			return aog.getMetaData().getFactory().createRelationType(rt.getId(), rt);
		} else {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					"Missing RelationType: " + name,
					"getRelationType(String name)"));
		}
		return null;
	}

	/**
	 * Generates a transfac url to the transcription factor.
	 * 
	 * @param tf_id
	 * @return transfac url
	 */
	protected String makeTransfacURL(String tf_id) {
		return "http://www.biobase.de/cgi-bin/biobase/transfac/11.2/bin/getTFProf.cgi?"
				+ tf_id;
	}

	private static final Pattern notDNAChecker = Pattern
			.compile("[^A|T|G|C|U]");

	/**
	 * Write a given FactorObject to ONDEX.
	 * 
	 * @param f
	 *            FactorObject
	 */
	public void createFactor(FactorObject f) {

		String desc = "";
		if (f.getDescription() != null)
			desc = f.getDescription();

		// create concept for reaction
		ONDEXConcept conceptFactor = aog.getFactory().createConcept(TFACC
				+ f.getAccession(), desc, "", dataSourceTF, ccTF, evi);
		ONDEXConcept conceptProtein = aog.getFactory().createConcept(TFACC
				+ f.getAccession() + "_PR", desc, "", dataSourceTF, ccProtein, evi);
		factorsWritten.put(f.getAccession(), conceptFactor);

		// relation between factor and protein
		aog.getFactory().createRelation(conceptFactor, conceptProtein, is_a, evi);

		// create preffered name
		if (f.getName() != null) {
			conceptFactor.createConceptName(f.getName(), true);
			conceptProtein.createConceptName(f.getName(), true);
		}

		// add more concept names
		Iterator<String> itS = f.getSynonyms().iterator();
		while (itS.hasNext()) {
			String syn = itS.next();
			writeSynonym(syn, f.getName(), conceptFactor);
			writeSynonym(syn, f.getName(), conceptProtein);
		}

		// set taxid
		if (f.getSpecies() != null && f.getSpecies().length() > 0) {
			conceptFactor.createAttribute(taxAttr, f.getSpecies(), true);
			conceptProtein.createAttribute(taxAttr, f.getSpecies(), true);
		}

		// create a TransFac URL
		conceptFactor.createAttribute(urlAttr, makeTransfacURL(f
				.getAccession()), false);

		// add TF accessions
		if (f.getAccession() != null) {
			conceptFactor.createConceptAccession(f.getAccession(), dataSourceTF,
					false);
		}

		// add more TF concept accessions
		Iterator<String> itAA = f.getAccessionAlternatives();
		while (itAA.hasNext()) {
			String acc = itAA.next();
			conceptFactor.createConceptAccession(acc, dataSourceTF, false);
			// alternative accession for interaction relations
			factorsWritten.put(acc, conceptFactor);
		}

		// add accessions to other CVs
		Iterator<DBlink> itDBL = f.getDatabaseLinks();
		while (itDBL.hasNext()) {
			DBlink link = itDBL.next();
			if (link.getAcc() != null) {
				DataSource dataSource = aog.getMetaData().getDataSource(link.getCv());
				if (dataSource == null) {
					Parser.propagateEventOccurred(new DataSourceMissingEvent("DataSource "
							+ link.getCv() + " not detected",
							"createFactor(FactorObject f)"));
				} else {
					conceptFactor.createConceptAccession(link.getAcc(), dataSource,
							false);
					conceptProtein.createConceptAccession(link.getAcc(), dataSource,
							false);
				}
			}
		}

		// add sequence
		if (f.getSequence() != null) {
			boolean nonDNA = notDNAChecker.matcher(f.getSequence()).find();
			if (nonDNA) {
				conceptProtein.createAttribute(aaAttr, f.getSequence(),
						false);
			} else {
				conceptProtein.createAttribute(naAttr, f.getSequence(),
						false);
			}
		}

		// relation to encoding gene
		if (f.getEncodingGene() != null) {
			ONDEXConcept gene = genesWritten.get(f.getEncodingGene());
			if (gene != null) {
				aog.getFactory().createRelation(conceptProtein, gene, en_by, evi);
			} else {
				Parser.propagateEventOccurred(new DataFileErrorEvent(
						"Some encoding references are missing: "
								+ f.getEncodingGene(),
						"createFactor(FactorObject f)"));
			}
		}

		// relations for regulated genes
		Iterator<String> itRegulated = f.getRegulatedGenes();
		while (itRegulated.hasNext()) {
			String regulated = itRegulated.next();
			ONDEXConcept gene = genesWritten.get(regulated);
			if (gene != null) {
				aog.getFactory().createRelation(gene, conceptFactor,
						rg_by, evi);
			} else {
				Parser.propagateEventOccurred(new DataFileErrorEvent(
						"Some regulation references are missing: "
								+ regulated,
						"createFactor(FactorObject f)"));
			}
			
		}

		// relations for interacting factors
		Iterator<String> itFactors = f.getInteractingFactors().iterator();
		while (itFactors.hasNext()) {
			String factor = itFactors.next();
			factorInteraction.put(f.getAccession(), factor);
		}

		// h_pwm for pwms
		Iterator<String> itPWM = f.getMatrices();
		while (itPWM.hasNext()) {
			String has = itPWM.next();
			aog.getFactory().createRelation(conceptFactor, matricesWritten.get(has),
					h_pwm, evi);
		}

		// add associated publications
		Iterator<Publication> it = f.getPublications().iterator();
		while (it.hasNext()) {
			writePublicationAndRelation(it.next(), conceptFactor);
		}

		Iterator<String> bindSites = f.getBindingSites().iterator();
		while (bindSites.hasNext()) {
			String siteAccession = bindSites.next();

			if (sitesWritten.size() > 0) { // this indicates the sites has
											// already been written if this is
											// not true then the factor to site
											// relation will be created when/if
											// they are

				ONDEXConcept site = sitesWritten.get(siteAccession);

				if (site != null) {
					aog.getFactory().createRelation(conceptFactor, site, bi_to, evi);
				} else {
					Parser.propagateEventOccurred(new DataFileErrorEvent(
							"Unkown Site Accession:" + siteAccession
									+ " referenced in TF dat file for TF: "
									+ f.getAccession(),
							"createFactor(FactorObject f)"));
				}

				HashSet<String> tfs = sitesToTransFacs.get(siteAccession);
				if (tfs == null) {
					Parser
							.propagateEventOccurred(new DataFileErrorEvent(
									"Site Accession: "
											+ siteAccession
											+ " referenced in TF dat file but not in Site",
									"createFactor(FactorObject f)"));
				} else if (!tfs.contains(f.getAccession())) {
					Parser
							.propagateEventOccurred(new DataFileErrorEvent(
									"Site: "
											+ siteAccession
											+ " is referenced as binding to "
											+ f.getAccession()
											+ " in TF dat file but not in Site dat file ",
									"createFactor(FactorObject f)"));
				} else {
					tfs.remove(f.getAccession());
					if (tfs.size() == 0) {
						sitesToTransFacs.remove(tfs);
					}
				}

			} else { // relation should already be in place -- verify this
				HashSet<String> tfs = sitesToTransFacs.get(siteAccession);

				if (tfs == null) {
					tfs = new HashSet<String>();
					sitesToTransFacs.put(siteAccession, tfs);
				}
				tfs.add(f.getAccession());
			}
		}

	}

	/**
	 * Write a given GeneObject to ONDEX.
	 * 
	 * @param g
	 *            GeneObject
	 */
	public void createGene(GeneObject g) {

		// create concept for gene
		String desc = "";
		if (g.getDescription() != null)
			desc = g.getDescription();

		ONDEXConcept c = aog.getFactory().createConcept(TFACC + g.getAccession(),
				desc, "", dataSourceTF, ccGene, evi);
		genesWritten.put(g.getAccession(), c);

		// create preffered name
		if (g.getName() != null) {
			c.createConceptName(g.getName(), true);
		}

		// add more concept names
		Iterator<String> itS = g.getSynonyms().iterator();
		while (itS.hasNext()) {
			writeSynonym(itS.next(), g.getName(), c);
		}

		// set taxid as Attribute
		if (g.getSpecies() != null && g.getSpecies().length() > 0) {
			c.createAttribute(taxAttr, g.getSpecies(), true);
		}

		// add concept accession for TF
		if (g.getAccession() != null) {
			c.createConceptAccession(g.getAccession(), dataSourceTF, false);
		}

		// add more TF concept accessions
		Iterator<String> itAA = g.getAccessionAlternatives();
		while (itAA.hasNext()) {
			String acc = itAA.next();
			c.createConceptAccession(acc, dataSourceTF, false);
			// alternative accession for encoding relations
			genesWritten.put(acc, c);
		}

		// add accessions to other CVs
		Iterator<DBlink> itDBL = g.getDatabaseLinks();
		while (itDBL.hasNext()) {
			DBlink link = itDBL.next();
			if (link.getAcc() != null) {
				DataSource dataSource = aog.getMetaData().getDataSource(link.getCv());
				if (dataSource == null) {
					Parser.propagateEventOccurred(new DataSourceMissingEvent("DataSource "
							+ link.getCv() + " not detected",
							"createGene(GeneObject g)"));
				} else {
					c.createConceptAccession(link.getAcc(), dataSource, false);
				}
			}
		}

		// add associated publications
		Iterator<Publication> it = g.getPublications().iterator();
		while (it.hasNext()) {
			writePublicationAndRelation(it.next(), c);
		}

		if (factorsWritten.size() > 0) {
			g.getInteractingFactors();
		}
	}

	/**
	 * Write a given MatrixObject to ONDEX.
	 * 
	 * @param m
	 *            MatrixObject
	 */
	public void createMatrix(MatrixObject m) {

		String desc = "";
		if (m.getDescription() != null)
			desc = m.getDescription();

		// create concept for matrix
		ONDEXConcept c = aog.getFactory().createConcept(TFACC + m.getAccession(),
				desc, "", dataSourceTF, ccPWM, evi);
		matricesWritten.put(m.getAccession(), c);

		// create preffered name
		if (m.getName() != null) {
			c.createConceptName(m.getName(), true);
		}

		// add concept accession for TF
		if (m.getAccession() != null) {
			c.createConceptAccession(m.getAccession(), dataSourceTF, false);
		}

		// add more TF concept accessions
		Iterator<String> itAA = m.getAccessionAlternatives();
		while (itAA.hasNext()) {
			c.createConceptAccession(itAA.next(), dataSourceTF, false);
		}

		// create ConceptAttribute
		c.createAttribute(tfmAttr, m.getMatrix(), false);

		// add associated publications
		Iterator<Publication> it = m.getPublications().iterator();
		while (it.hasNext()) {
			writePublicationAndRelation(it.next(), c);
		}
	}

	/**
	 * You can run this before or after TF creation it will link both when they
	 * are both created
	 * 
	 * @param site
	 *            SiteObject
	 */
	public void createSite(SiteObject site) {

		String desc = null;
		if (site.getDescription() != null) {
			desc = site.getDescription();
		} else {
			desc = "";
		}
		ONDEXConcept c = aog.getFactory().createConcept(TFACC + site.getAccession(),
				desc, dataSourceTF, ccTFBS, evi);
		sitesWritten.put(site.getAccession(), c);
		c.createConceptAccession(site.getAccession(), dataSourceTF, false);

		if (site.getSeq() != null
				&& (site.getSeq_type() == null || site.getSeq_type()
						.equalsIgnoreCase("DNA"))) {
			c.createAttribute(tfbss, site.getSeq(), false);
		} else if (site.getSeq() != null) {
			ONDEXEventHandler.getEventHandlerForSID(aog.getSID()).fireEventOccurred(new DataFileErrorEvent(
							"Missing Seq Type for TF Binding Site: Seqence was not written",
							"createSite(SiteObject site)"));
		}

		// add more concept names
		Iterator<String> itS = site.getSynonyms().iterator();
		while (itS.hasNext()) {
			writeSynonym(itS.next(), site.getName(), c);
		}

		Iterator<String> itAA = site.getAccessionAlternatives();
		while (itAA.hasNext()) {
			c.createConceptAccession(itAA.next(), dataSourceTF, false);
		}

		// add accessions to other CVs
		Iterator<DBlink> itDBL = site.getDatabaseLinks();
		while (itDBL.hasNext()) {
			DBlink link = itDBL.next();
			if (link.getAcc() != null) {
				DataSource dataSource = aog.getMetaData().getDataSource(link.getCv());
				if (dataSource == null) {
					Parser.propagateEventOccurred(new DataSourceMissingEvent("DataSource "
							+ link.getCv() + " not detected",
							"createSite(SiteObject site)"));
				} else {
					c.createConceptAccession(link.getAcc(), dataSource, false);
				}
			}
		}

		// add associated publications
		Iterator<Publication> it = site.getPublications().iterator();
		while (it.hasNext()) {
			writePublicationAndRelation(it.next(), c);
		}

		// h_pwm for pwms
		String has = site.getMatrixAccession();
		if (has != null) {
			ONDEXConcept pwm = matricesWritten.get(has);
			if (pwm != null) {
				aog.getFactory().createRelation(c, pwm, h_pwm, evi);
			} else {
				Parser.propagateEventOccurred(new DataFileErrorEvent(
						"Site Accession: " + site.getAccession()
								+ " contains unknown matrix " + has,
						"createSite(SiteObject site)"));
			}
		}

		// situated next to a gene
		String situated = site.getSituatedTo();
		if (situated != null) {
			ONDEXConcept gene = genesWritten.get(situated);
			if (gene != null) {
				aog.getFactory().createRelation(c, gene, si_to, evi);
			} else {
				Parser.propagateEventOccurred(new DataFileErrorEvent(
						"Site Accession: " + site.getAccession()
								+ " contains unknown gene " + situated,
						"createSite(SiteObject site)"));
			}
		}

		Iterator<String> bindTFs = site.getBindingFactorAccessions().iterator();
		while (bindTFs.hasNext()) {
			String tfAccession = bindTFs.next();

			if (factorsWritten.size() > 0) { // this indicates factor have
												// already been written if this
												// is not true then the factor
												// to site relation will be
												// created when/if they are

				ONDEXConcept tf = factorsWritten.get(tfAccession);

				if (tf != null) {
					aog.getFactory().createRelation(tf, c, bi_to, evi);
				} else {
					Parser
							.propagateEventOccurred(new DataFileErrorEvent(
									"Unknown TranscriptionFactor: "
											+ tfAccession
											+ " accession referenced in Site dat file for Site: "
											+ site.getAccession()
											+ " but not recorded int TF dat file",
									"createSite(SiteObject site)"));
				}

				HashSet<String> tfs = sitesToTransFacs.get(site.getAccession());
				if (tfs == null) {
					Parser
							.propagateEventOccurred(new DataFileErrorEvent(
									"Site Accession: "
											+ site.getAccession()
											+ " referenced in Site dat file but not in TF dat file",
									"createSite(SiteObject site)"));
				} else if (!tfs.contains(tfAccession)) {
					Parser
							.propagateEventOccurred(new DataFileErrorEvent(
									"Site: "
											+ site.getAccession()
											+ " is referenced as binding to "
											+ tfAccession
											+ " in Site dat file but not in TF dat file ",
									"createSite(SiteObject site)"));
				} else {
					tfs.remove(tfAccession);
				}
				if (tfs.size() == 0) {
					sitesToTransFacs.remove(tfs);
				}

			} else { // we can't create the relation yet but we can store it
						// for later verification
				HashSet<String> tfs = sitesToTransFacs.get(site.getAccession());

				if (tfs == null) {
					tfs = new HashSet<String>();
					sitesToTransFacs.put(site.getAccession(), tfs);
				}
				tfs.add(tfAccession);
			}
		}
	}

	/**
	 * Call me after all TF and Sites have been created Validates that data file
	 * relations between the two are consistant and creates any missing
	 * relations
	 * 
	 * @param caller
	 */
	public void validateSiteToTF(String caller) {
		// if sitesToTransFacs is greater than 0 and factors have already been
		// written we have data inconsistancey in that relations are mentioned
		// in one file but not the other
		if (factorsWritten.size() > 0 && sitesWritten.size() > 0) {
			// both have been written ok to check
			if (sitesToTransFacs.size() > 0) {

				Iterator<String> siteIt = sitesToTransFacs.keySet().iterator();
				while (siteIt.hasNext()) {
					String siteAcc = siteIt.next();
					HashSet<String> tfs = sitesToTransFacs.get(siteAcc);

					Iterator<String> tfIt = tfs.iterator();
					while (tfIt.hasNext()) {
						String tfAccession = tfIt.next();
						Parser
								.propagateEventOccurred(new DataFileErrorEvent(
										"TF Accession: "
												+ tfAccession
												+ " to Site Accession: "
												+ siteAcc
												+ " not referenced in "
												+ caller
												+ " dat file but preseent in other i.e. Site/TF",
										"validateSiteToTF(String caller)"));
						ONDEXConcept tf = factorsWritten.get(tfAccession);
						ONDEXConcept sa = sitesWritten.get(siteAcc);

						if (tf != null) {
							aog.getFactory().createRelation(tf, sa, bi_to, evi);
						} else {
							System.err
									.println("ERROR:ERROR TF not written could not create relation during site redundent creation "
											+ tfAccession);
						}
					}
				}
				sitesToTransFacs.clear(); // found these no need for table
			}
		}
	}

	private HashSet<String> reportedMissingCVs = new HashSet<String>();

	private void writeSynonym(String synonym, String conceptName,
			ONDEXConcept c) {
		String check = (String) ValidatorRegistry.validators.get("cvregex").validate(synonym);

		if (check != null) {
			DataSource dataSource = aog.getMetaData().getDataSource(check);

			if (dataSource == null) {
				if (!reportedMissingCVs.contains(check)) {
					ONDEXEventHandler.getEventHandlerForSID(aog.getSID()).fireEventOccurred(new DataSourceMissingEvent("DataSource missing => "
							+ check, ""));
					reportedMissingCVs.add(check);
				}
			} else {
				if (c.getConceptAccession(synonym, dataSource) == null) {
					c.createConceptAccession(synonym.toUpperCase(), dataSource,
							false);
				}
			}
		} else {
			if (!synonym.equalsIgnoreCase(conceptName)) {
				c.createConceptName(synonym,false);
			}
		}
	}

	private void writePublicationAndRelation(Publication pub, ONDEXConcept c) {

		if (pub.getPmid() != null) {

			ONDEXConcept pubC = publicationsWritten.get(pub.getPmid());

			if (pubC == null) {
				pubC = aog.getFactory().createConcept(TFACC + pub.getPmid(), "Title: "
						+ pub.getTitle(), dataSourceTF, ccPublication, evi);
				if (pub.getPmid() != null && pub.getPmid().length() > 0)
					pubC.createConceptAccession(pub.getPmid(), dataSourcePMID,
									false);
				if (pub.getTitle() != null)
					pubC.createConceptName(pub.getTitle(), true);
				if (pub.getAuthors() != null)
					pubC.createAttribute(authorAttr, pub.getAuthors(),
									true);
				publicationsWritten.put(pub.getPmid(), pubC);
			}
			aog.getFactory().createRelation(c, pubC, pub_in, evi);
		} else {
			Parser
					.propagateEventOccurred(new DataFileErrorEvent(
							"PMID in Publication missing: " + pub.getPmid()
									+ " | source: " + pub.getSource()
									+ " | title: " + pub.getTitle()
									+ " | authors: " + pub.getAuthors(),
							"writePublicationAndRelation(Publication pub, ONDEXConcept c)"));
		}
	}

	protected void makeFactorInteraction() {

		Iterator<String> keys = factorInteraction.keySet().iterator();

		while (keys.hasNext()) {
			String key = keys.next();
			ONDEXConcept from = factorsWritten.get(key);
			ONDEXConcept to = factorsWritten.get(factorInteraction.get(key));
			if (to != null) {
				aog.getFactory().createRelation(from, to, it_wi, evi);
			} else {
				Parser.propagateEventOccurred(new DataFileErrorEvent(
						"Factor for interaction missing: from "
								+ from.getPID() + " to "
								+ factorInteraction.get(from) + " (missing).",
						"makeFactorInteraction()"));
			}
		}

	}

}
