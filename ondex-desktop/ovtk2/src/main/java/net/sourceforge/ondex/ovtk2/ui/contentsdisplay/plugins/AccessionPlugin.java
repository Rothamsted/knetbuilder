package net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin;
import net.sourceforge.ondex.validator.htmlaccessionlink.Condition;
import net.sourceforge.ondex.validator.htmlaccessionlink.Validator;

/**
 * Accessions display producer. It assembles infos about the concepts'
 * accessions and links them to the correspondig URL. To add more DataSource
 * links, just complete the HashMap in the Constructor.
 * 
 * @author Keywan
 */
public class AccessionPlugin extends AbstractContentDisplayPlugin {

	public static Map<String, String> cvToURL;
	public static Validator mapper;

	static {
		cvToURL = new HashMap<String, String>();
		cvToURL.put("CHEMBLTARGET", "https://www.ebi.ac.uk/chembldb/target/inspect/");
		cvToURL.put("CHEMBLASSAY", "https://www.ebi.ac.uk/chembldb/assay/inspect/");
		cvToURL.put("CHEMBL", "https://www.ebi.ac.uk/chembldb/compound/inspect/");
		cvToURL.put("GO", "http://www.ebi.ac.uk/QuickGO/GTerm?id=");
		cvToURL.put("UNIPROTKB", "http://beta.uniprot.org/uniprot/");
		cvToURL.put("EMBL", "http://www.ebi.ac.uk/cgi-bin/emblfetch?style=html&id=");
		cvToURL.put("IPRO", "http://www.ebi.ac.uk/interpro/IEntry?ac=");
		cvToURL.put("NLM", "http://www.ncbi.nlm.nih.gov/pubmed/");
		cvToURL.put("PubMed", "http://www.ncbi.nlm.nih.gov/pubmed/");
		cvToURL.put("TAIR", "http://www.arabidopsis.org/servlets/TairObject?type=locus&name=");
		cvToURL.put("EC", "http://www.expasy.org/enzyme/");
		cvToURL.put("PROSITE", "http://www.expasy.org/prosite/");
		cvToURL.put("NC_NP", "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&id=");
		cvToURL.put("NC_NM", "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?id=");
		cvToURL.put("NC_GE", "http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&term=");
		cvToURL.put("PFAM", "http://pfam.sanger.ac.uk/family?acc=");
		cvToURL.put("TX", "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=");
		cvToURL.put("INTACT", "http://www.ebi.ac.uk/intact/binary-search/faces/search.xhtml?query=");
		cvToURL.put("AC", "http://pmn.plantcyc.org/ARA/NEW-IMAGE?object=");
		cvToURL.put("SCOP", "http://scop.mrc-lmb.cam.ac.uk/scop/search.cgi?sid=");
		cvToURL.put("GENB", "http://www.ncbi.nlm.nih.gov/sites/entrez?db=nuccore&cmd=search&term=");
		cvToURL.put("PRINTS", "http://www.bioinf.manchester.ac.uk/cgi-bin/dbbrowser/PRINTS/DoPRINTS.pl?cmd_a=Display&fun_a=Text&qst_a=");
		cvToURL.put("PRODOM", "http://prodom.prabi.fr/prodom/current/cgi-bin/request.pl?question=DBEN&query=");
		cvToURL.put("MC", "http://metacyc.org/META/substring-search?type=NIL&object=");
		cvToURL.put("OMIM", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=OMIM&dopt=Detailed&tmpl=dispomimTemplate&list_uids=");
		cvToURL.put("REAC", "http://www.reactome.org/cgi-bin/eventbrowser?DB=gk_current&ID=");
		cvToURL.put("DOI", "http://dx.doi.org/");
		cvToURL.put("Poplar-JGI", "http://genome.jgi-psf.org/cgi-bin/dispGeneModel?db=Poptr1_1&id=");
		cvToURL.put("PoplarCyc", "http://pmn.plantcyc.org/POPLAR/NEW-IMAGE?object=");
	}

	/**
	 * Constructor. Initialises and fills the DataSource to URL HashMap.
	 * 
	 * @throws InvalidPluginArgumentException
	 */
	public AccessionPlugin(ONDEXGraph aog) throws InvalidPluginArgumentException {
		super(aog);

		File existingMapping = new File(Config.ondexDir + File.separator + "importdata" + File.separator + "htmlaccessionlink");

		if (existingMapping.exists()) {
			// singleton for mapper
			if (mapper == null) {
				mapper = new Validator();

				ONDEXPluginArguments varg = new ONDEXPluginArguments(mapper.getArgumentDefinitions());
				varg.setOption(FileArgumentDefinition.INPUT_DIR, existingMapping.getAbsolutePath());

				mapper.setArguments(varg);
				System.out.println("Using mapping file: " + existingMapping.getAbsolutePath());
			}
		}
	}

	/**
	 * Displays and links the accessions of concepts
	 * 
	 * @see AbstractContentDisplayPlugin#compileContent(ONDEXEntity);
	 */
	@Override
	public String compileContent(ONDEXEntity e) {
		StringBuffer b = new StringBuffer();
		if (e instanceof ONDEXConcept) {
			ONDEXConcept c = (ONDEXConcept) e;
			// group accessions by DataSource [DataSource -> Accession]
			Map<DataSource, List<ConceptAccession>> cvMap = getCVtoAccessions(c);
			writeAccessions(cvMap, c.getOfType(), b);
		}

		return b.toString();
	}

	/*
	 * Collects for a given concept its accessions grouped by DataSource
	 */
	private Map<DataSource, List<ConceptAccession>> getCVtoAccessions(ONDEXConcept c) {
		Map<DataSource, List<ConceptAccession>> cvMap = new HashMap<DataSource, List<ConceptAccession>>();
		for (ConceptAccession acc : c.getConceptAccessions()) {
			// add all accessions by data source
			DataSource cv = acc.getElementOf();
			if (!cvMap.containsKey(cv)) {
				List<ConceptAccession> ll = new LinkedList<ConceptAccession>();
				cvMap.put(cv, ll);
			}
			cvMap.get(cv).add(acc);
		}

		return cvMap;
	}

	/*
	 * Writes the collected accessions
	 */
	private void writeAccessions(Map<DataSource, List<ConceptAccession>> cvMap, ConceptClass cc, StringBuffer b) {
		b.append("<h2>Accessions</h2>");
		for (DataSource ds : cvMap.keySet()) {

			// use data source ID
			String dsID = ds.getId();
			b.append("<b>" + dsID + ":</b> ");

			// get URL for this type of accessions
			String url = cvToURL.get(dsID);
			if (mapper != null) {
				Condition cond = new Condition(dsID, cc.getId());
				String prefix = (String) mapper.validate(cond);
				if (prefix != null && prefix.length() > 0) {
					url = prefix;
				}
			}

			for (ConceptAccession acc : cvMap.get(ds)) {
				// ambiguous accessions are red
				if (acc.isAmbiguous())
					b.append("<font color=\"red\">");
				else
					b.append("<font color=\"black\">");

				// add in URL
				if (url != null) {
					b.append("<a href=" + url + "" + acc.getAccession() + ">" + acc.getAccession() + "</a>");
				} else {
					b.append(acc.getAccession());
				}

				// close current accession
				b.append("</font>");
				b.append("&nbsp");
			}
			b.append("<br />");
		}
	}

	/**
	 * get name of producer
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getName()
	 */
	@Override
	public String getName() {
		return "Accession";
	}

	/**
	 * get version of producer
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.contentsdisplay.AbstractContentDisplayPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return "23.05.2011";
	}

}
