/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.sink;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg56.GenomeParser;
import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.tools.data.ChemicalStructure;
import net.sourceforge.ondex.tools.data.Protein3dStructure;

/**
 * Class writes KEGG prototype concepts to the ONDEX graph
 * 
 * @author taubertj
 */
public class ConceptWriter {

	private static final Pattern colonPattern = Pattern.compile(":");

	private AttributeName anGraphical;

	private AttributeName anTaxid;

	private AttributeName anUbiquitous;

	private AttributeName anURL;

	private AttributeName anChemicalStructure;

	private AttributeName anProtein3dStructure;

	private HashMap<String, ConceptClass> ccs;

	private HashMap<String, DataSource> cvs;

	private EvidenceType evidenceType;

	// wrapped ONDEX graph
	private ONDEXGraph graph;

	private Set<String> unknownAccessions = new HashSet<String>();

	private Set<String> writtenConcepts = new HashSet<String>(100000);

	// conceptId
	private Map<String, Integer> writtenConceptTranslations = new HashMap<String, Integer>(
			100000);

	/**
	 * Sets ONDEX graph and initialises caches.
	 * 
	 * @param og
	 *            ONDEXGraph
	 */
	public ConceptWriter(ONDEXGraph og) {
		this.graph = og;
		cvs = new HashMap<String, DataSource>(200);
		ccs = new HashMap<String, ConceptClass>(200);
	}

	/**
	 * Checks if a concept id has been written already.
	 * 
	 * @param id
	 *            concept ID
	 * @return boolean
	 */
	public boolean conceptONDEXIDIsWritten(Integer id) {
		return writtenConceptTranslations.containsValue(id);
	}

	/**
	 * Checks if a KEGG internal id has been written already.
	 * 
	 * @param name
	 *            KEGG id
	 * @return boolean
	 */
	public boolean conceptParserIDIsWritten(String name) {
		return writtenConcepts.contains(name.toUpperCase());
	}

	/**
	 * Create a concept in ONDEX graph.
	 * 
	 * @param concept
	 *            KEGG concept prototype
	 * @param genomeParser
	 *            genome data for taxonomy
	 * @throws MetaDataMissingException
	 */
	public void createConcept(Concept concept, GenomeParser genomeParser,
			boolean speciesSpecific) throws MetaDataMissingException,
			InconsistencyException {

		// check concept is fine
		if (concept == null)
			throw new NullPointerException("Concept is null");
		if (concept.getId() == null)
			throw new NullPointerException("Concept ID is null");

		writtenConcepts.add(concept.getId().trim().toUpperCase());

		// assign taxonomy from genome file
		String[] result = colonPattern.split(concept.getId());
		if (result.length == 2 && speciesSpecific) {
			GenomeParser.Taxonomony taxonomy = genomeParser
					.getTaxonomony(result[0].toLowerCase());
			if (taxonomy != null) {
				concept.setTaxid(String.valueOf(taxonomy.getTaxNumber()));
			} else {
				// System.err.println(result[0] + " is unknown KEGG species");
				throw new InconsistencyException(result[0]
						+ " is unknown KEGG species");
			}
		}

		// set default evidence type
		if (evidenceType == null)
			evidenceType = graph.getMetaData().getEvidenceType(
					MetaData.EVIDENCE_IMPD);
		if (evidenceType == null)
			throw new EvidenceTypeMissingException(MetaData.EVIDENCE_IMPD);

		// ubiquitous attribute
		if (anUbiquitous == null)
			anUbiquitous = graph.getMetaData().getAttributeName(
					MetaData.ATTR_NAME_UBIQUITOUS);
		if (anUbiquitous == null)
			throw new AttributeNameMissingException(
					MetaData.ATTR_NAME_UBIQUITOUS);

		// taxonomy attribute
		if (anTaxid == null)
			anTaxid = graph.getMetaData().getAttributeName(
					MetaData.ATTR_NAME_TAXID);
		if (anTaxid == null)
			throw new AttributeNameMissingException(MetaData.ATTR_NAME_TAXID);

		// URL attribute
		if (anURL == null)
			anURL = graph.getMetaData()
					.getAttributeName(MetaData.ATTR_NAME_URL);
		if (anURL == null)
			throw new AttributeNameMissingException(MetaData.ATTR_NAME_URL);

		// ChemicalStruture attribute
		if (anChemicalStructure == null) {
			anChemicalStructure = graph.getMetaData().getAttributeName(
					MetaData.ATTR_NAME_CHEMICAL);
			// TODO: put into ondex_metadata.xml
			if (anChemicalStructure == null)
				anChemicalStructure = graph.getMetaData().getFactory()
						.createAttributeName(MetaData.ATTR_NAME_CHEMICAL,
								MetaData.ATTR_NAME_CHEMICAL,
								ChemicalStructure.class);
		}

		// Protein3dStruture attribute
		if (anProtein3dStructure == null) {
			anProtein3dStructure = graph.getMetaData().getAttributeName(
					MetaData.ATTR_NAME_PROTEIN3D);
			// TODO: put into ondex_metadata.xml
			if (anProtein3dStructure == null)
				anProtein3dStructure = graph.getMetaData().getFactory()
						.createAttributeName(MetaData.ATTR_NAME_PROTEIN3D,
								MetaData.ATTR_NAME_PROTEIN3D,
								Protein3dStructure.class);
		}

		// graphical location attribute
		if (anGraphical == null) {
			anGraphical = graph.getMetaData().getAttributeName(
					MetaData.ATTR_NAME_GRAPHICAL);
			if (anGraphical == null)
				throw new AttributeNameMissingException(
						MetaData.ATTR_NAME_GRAPHICAL);
		}

		// get data source from cache
		DataSource dataSource = cvs.get(concept.getElement_of());
		if (dataSource == null) {
			if (concept.getElement_of().trim().length() == 0)
				throw new DataSourceMissingException("empty cv " + concept.getId());

			dataSource = graph.getMetaData().getDataSource(concept.getElement_of());
			cvs.put(concept.getElement_of(), dataSource);
		}
		if (dataSource == null)
			throw new DataSourceMissingException(concept.getElement_of());

		// get concept class from cache
		ConceptClass cc = ccs.get(concept.getOf_type_fk());
		if (cc == null) {
			cc = graph.getMetaData().getConceptClass(concept.getOf_type_fk());
			ccs.put(concept.getOf_type_fk(), cc);
		}
		if (cc == null)
			throw new ConceptClassMissingException(concept.getOf_type_fk());

		// check description
		String description = "";
		if (concept.getDescription() != null)
			description = concept.getDescription();

		// create actual ONDEX concept
		ONDEXConcept ac = graph.getFactory().createConcept(
				concept.getId().toUpperCase(), description, dataSource, cc,
				evidenceType);

		// mapping between KEGG internal and concept ID
		writtenConceptTranslations.put(concept.getId().trim().toUpperCase(), ac
				.getId());
		// System.out.println("WRITTEN " + ac.getPID().toUpperCase() + " " +
		// concept.getId().toUpperCase().trim());

		// add context to written concept
		for (String context : concept.getContext()) {
			Integer existingId = writtenConceptTranslations.get(context
					.toUpperCase());
			if (existingId != null)
				ac.addTag(graph.getConcept(existingId));
			else {
				throw new InconsistencyException("|" + context.toUpperCase()
						+ "| Context not found");
			}
		}
		if (concept.isSelfContext()) {
			ac.addTag(ac);
		}

		// add graphical position attribute
		if (concept.getGraphical_x() != 0 && concept.getGraphical_y() != 0) {
			Point2D.Float p = new Point2D.Float(concept.getGraphical_x(),
					concept.getGraphical_y());
			ac.createAttribute(anGraphical, p, false);
		}

		// add taxonomy id attribute
		if (concept.getTaxid() != null && concept.getTaxid().length() > 0)
			ac.createAttribute(anTaxid, concept.getTaxid(), true);

		// add URL as attribute
		if (concept.getUrl() != null && concept.getUrl().length() > 0)
			ac.createAttribute(anURL, concept.getUrl(), false);

		// add ChemicalStructure if present
		if (concept.getMol() != null && concept.getMol().length() > 0) {
			ChemicalStructure cs = new ChemicalStructure();
			cs.setMOL(concept.getMol());
			ac.createAttribute(anChemicalStructure, cs, false);
		}

		// special case about PDB structure
		if (concept.getStructures() != null) {
			for (String acc : concept.getStructures()) {
				Protein3dStructure ps = new Protein3dStructure();
				ps.setAccessionNr(acc);
				// System.out.println(acc);
				ac.createAttribute(anProtein3dStructure, ps, false);
			}
		}

		// add concept names
		if (concept.getConceptNames() != null) {
			for (ConceptName conceptName : concept.getConceptNames()) {
				ac.createConceptName(conceptName.getName(), conceptName
						.isPreferred());
			}
		}

		// add KEGG id as concept accession
		String acc = concept.getId();
		if (acc.indexOf("_") > -1) {
			acc = acc.substring(0, acc.indexOf("_"));
		}
		ac.createConceptAccession(acc, cvs.get(MetaData.CV_KEGG), true);

		// add concept accessions
		if (concept.getConceptAccs() != null) {
			for (ConceptAcc conceptAcc : concept.getConceptAccs()) {
				if (!conceptAcc.getElement_of().equalsIgnoreCase("RN")) {

					// check if there is a mapping defined
					String metadatadb = MetaData.getMapping(conceptAcc
							.getElement_of());
					if (metadatadb == null) {
						metadatadb = conceptAcc.getElement_of().toUpperCase();
					}

					// get DataSource for KEGG database reference
					DataSource accDataSource = cvs.get(metadatadb);
					if (accDataSource == null) {
						if (metadatadb != null
								&& metadatadb.trim().length() > 0
								&& !unknownAccessions.contains(metadatadb)) {
							// this is a unknown database reference
							accDataSource = graph.getMetaData().getDataSource(metadatadb);
							if (accDataSource == null) {
								unknownAccessions.add(metadatadb);
								System.out
										.println("Unknown Accession database: "
												+ metadatadb);
								continue;
							}
						} else {
							continue;
						}
					}

					// build concept accession
					if (conceptAcc.getConcept_accession().length() > 0) {

						// set ambiguity here
						if (conceptAcc.getElement_of().equals(MetaData.CV_CAS))
							conceptAcc.setAmbiguous(false);
						else if (conceptAcc.getElement_of().equals(
								MetaData.CV_UNIPROT))
							conceptAcc.setAmbiguous(false);
						else if (conceptAcc.getElement_of().equals(
								MetaData.CV_EC)
								&& concept.getOf_type_fk().equals(
										MetaData.CV_EC))
							conceptAcc.setAmbiguous(false);
						else if (conceptAcc.getElement_of().equals(
								MetaData.CV_EC))
							conceptAcc.setAmbiguous(true);

						// create concept accession
						ac.createConceptAccession(conceptAcc
								.getConcept_accession().toUpperCase(), accDataSource,
								conceptAcc.isAmbiguous());
					}
				}
			}
		}
	}

	/**
	 * Returns concept id for an internal KEGG id.
	 * 
	 * @param internal
	 *            String
	 * @return Integer
	 * @throws InconsistencyException
	 */
	public Integer getWrittenConceptId(String internal)
			throws InconsistencyException {
		if (!writtenConceptTranslations.containsKey(internal.toUpperCase())) {
			throw new InconsistencyException(internal.toUpperCase()
					+ " not found.");
		}
		return writtenConceptTranslations.get(internal.toUpperCase());
	}
}
