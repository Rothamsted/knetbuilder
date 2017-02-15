package net.sourceforge.ondex.parser.generif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.ONDEXParser;

import org.apache.log4j.Level;

/**
 * Parser for the GeneRIF file.
 * <p/>
 * Download from: ftp://ftp.ncbi.nih.gov/gene/GeneRIF/generifs_basic.gz
 * <p/>
 * Creates Genes --> Publications and assigns the GeneRIFs to the relations
 * 
 * @author keywan
 */
public class Parser extends ONDEXParser
{

	private DataSource dataSourceGeneRIF;
	private DataSource dataSourceNCBI;
	private DataSource dataSourceNLM;
	private ConceptClass ccGene;
	private ConceptClass ccPublication;
	private EvidenceType etIMPD;
	private AttributeName anTaxID;
	private AttributeName anEvidence;
	private RelationType rtPubIn;

	private HashMap<String, Integer> parsedPubIDs;
	private HashMap<String, Integer> parsedGeneIDs;
	private List<String> taxids;

	/**
	 * Initialisation of parameters
	 */
	public Parser() {
		parsedPubIDs = new HashMap<String, Integer>();
		parsedGeneIDs = new HashMap<String, Integer>();
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition taxId = new StringArgumentDefinition(
				ArgumentNames.TAXID_ARG, ArgumentNames.TAXID_ARG_DESC, false,
				null, true);
		FileArgumentDefinition inputDir = new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_DIR,
				"directory with generif files", true, true, true, false);
		return new ArgumentDefinition<?>[] { taxId, inputDir };
	}

	public String getName() {
		return "GeneRIF Parser";
	}

	public String getVersion() {
		return "03.10.2008";
	}

	@Override
	public String getId() {
		return "generif";
	}

	/**
	 * Parses every line into a Gene to Publication relation Considers only
	 * given TaxIDs, if TaxID argument is used
	 */
	public void start() throws InvalidPluginArgumentException, FileNotFoundException, IOException {
		GeneralOutputEvent goe = new GeneralOutputEvent(
				"Start GeneRIF parsing...", "[Parser - start()]");
		goe.setLog4jLevel(Level.INFO);
		fireEventOccurred(goe);

		initMetaData(graph);

		taxids = (List<String>) args.getObjectValueList(ArgumentNames.TAXID_ARG);

		File dir = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
		String infilesDir = dir.getAbsolutePath();
		if (!infilesDir.endsWith(File.separator))
			infilesDir = infilesDir + File.separator;

		String filename = infilesDir + "generifs_basic.gz";

		BufferedReader input;

		try {
			if (filename.endsWith(".gz")) {
				GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(
						filename));
				input = new BufferedReader(new InputStreamReader(gzip));
			} else {
				input = new BufferedReader(new FileReader(filename));
			}

			String inputLine = input.readLine();

			while (inputLine != null) {

				String[] col = inputLine.split("\t");
				String taxID = col[0];
				String geneID = col[1];
				// third column can be a list of publications
				String[] pubmedIDs = col[2].split(",");
				// String timestamp = col[3];
				String geneRIF = col[4];

				if (taxids != null && !taxids.contains(taxID)) {
					inputLine = input.readLine();
					continue;
				}

				if (!parsedGeneIDs.containsKey(geneID)) {
					ONDEXConcept g = graph.getFactory().createConcept(geneID,
							dataSourceGeneRIF, ccGene, etIMPD);
					g.createConceptAccession(geneID, dataSourceNCBI, false);
					g.createAttribute(anTaxID, taxID, false);
					parsedGeneIDs.put(geneID, g.getId());
				}
				ONDEXConcept gene = graph.getConcept(parsedGeneIDs.get(geneID));

				for (String pubmedID : pubmedIDs) {
					if (!parsedPubIDs.containsKey(pubmedID)) {
						ONDEXConcept pub = graph.getFactory().createConcept(
								pubmedID, dataSourceGeneRIF, ccPublication, etIMPD);
						pub.createConceptAccession(pubmedID, dataSourceNLM, false);
						parsedPubIDs.put(pubmedID, pub.getId());
					}
					ONDEXConcept publication = graph.getConcept(parsedPubIDs
							.get(pubmedID));

					if (graph.getRelation(gene, publication, rtPubIn) == null) {
						// create new relation with GeneRIF
						ONDEXRelation rel = graph.getFactory().createRelation(
								gene, publication, rtPubIn, etIMPD);
						rel.createAttribute(anEvidence, geneRIF, false);
					} else {
						// add new GeneRIF if relations already exists
						ONDEXRelation rel = graph.getRelation(gene,
								publication, rtPubIn);
						String oldGDS = (String) rel.getAttribute(anEvidence)
								.getValue();
						rel.getAttribute(anEvidence).setValue(
								oldGDS + ", " + geneRIF);

					}
				}

				inputLine = input.readLine();
			}

			input.close();
			goe = new GeneralOutputEvent("Successfully read file.",
					"[Parser - start()]");
			goe.setLog4jLevel(Level.INFO);
			fireEventOccurred(goe);

		} catch (FileNotFoundException fnfe) {
			fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(),
					"[Parser - start()]"));
            throw fnfe;
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[Parser - start()]"));
            throw ioe;
		}
	}

	private void initMetaData(ONDEXGraph graph) {
		// check DataSource
		dataSourceNCBI = graph.getMetaData().getDataSource(MetaData.CV_NCBI);
		if (dataSourceNCBI == null) {
			this.fireEventOccurred(new DataSourceMissingEvent(MetaData.CV_NCBI,
					"[Parser - initMetaData]"));
		}
		dataSourceNLM = graph.getMetaData().getDataSource(MetaData.CV_NLM);
		if (dataSourceNLM == null) {
			this.fireEventOccurred(new DataSourceMissingEvent(MetaData.CV_NLM,
					"[Parser - initMetaData]"));
		}
		dataSourceGeneRIF = graph.getMetaData().getDataSource(MetaData.CV_GENERIF);
		if (dataSourceGeneRIF == null) {
			this.fireEventOccurred(new DataSourceMissingEvent(MetaData.CV_GENERIF,
					"[Parser - initMetaData]"));
		}
		// check CC
		ccGene = graph.getMetaData().getConceptClass(MetaData.CC_GENE);
		if (ccGene == null) {
			this.fireEventOccurred(new ConceptClassMissingEvent(
					MetaData.CC_GENE, "[Parser - initMetaData]"));
		}
		ccPublication = graph.getMetaData().getConceptClass(
				MetaData.CC_PUBLICATION);
		if (ccPublication == null) {
			this.fireEventOccurred(new ConceptClassMissingEvent(
					MetaData.CC_PUBLICATION, "[Parser - initMetaData]"));
		}
		// check RelationType
		rtPubIn = graph.getMetaData().getRelationType(MetaData.RT_PUBLISHED_IN);
		if (rtPubIn == null) {
			fireEventOccurred(new RelationTypeMissingEvent(
					MetaData.RT_PUBLISHED_IN, "[Parser - initMetaData]"));
		}
		// check ET
		etIMPD = graph.getMetaData().getEvidenceType(MetaData.ET);
		if (etIMPD == null) {
			this.fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.ET,
					"[Parser - initMetaData]"));
		}

		// check AttributeNames
		anTaxID = graph.getMetaData().getAttributeName(MetaData.AN_TAXID);
		if (anTaxID == null) {
			fireEventOccurred(new AttributeNameMissingEvent(MetaData.AN_TAXID,
					"[Parser - initMetaData]"));
		}

		anEvidence = graph.getMetaData().getAttributeName(MetaData.AN_EVIDENCE);
		if (anEvidence == null) {
			fireEventOccurred(new AttributeNameMissingEvent(
					MetaData.AN_EVIDENCE, "[Parser - initMetaData]"));
		}
	}
}
