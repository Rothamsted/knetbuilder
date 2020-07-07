package net.sourceforge.ondex.parser.uniprotsequence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.event.type.ParsingErrorEvent;
import net.sourceforge.ondex.parser.ONDEXParser;

/**
 * Retrieves AA sequences from UniPort RESTful server for every relevant
 * accession found
 * 
 * @author taubertj
 * 
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Status(status = StatusType.STABLE, description = "Tested December (Jacek Grzebyta)")
public class Parser extends ONDEXParser implements MetaData {

	/**
	 * Sequence attribute
	 */
	private AttributeName an;

	@Override
	public String getId() {
		return "uniprotsequence";
	}

	@Override
	public String getName() {
		return "UniProt RESTful Sequences";
	}

	@Override
	public String getVersion() {
		return "22.05.2012";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[0];
	}

	@Override
	public void start() throws Exception {

		// check concept class
		ConceptClass cc = graph.getMetaData().getConceptClass(CC_Protein);
		if (cc == null) {
			fireEventOccurred(new InconsistencyEvent(
					"Graph does not contain protein concepts. Not continuing.",
					getCurrentMethodName()));
			return;
		}

		// check data source
		DataSource uniProtKB = graph.getMetaData().getDataSource(CV_UniProt);
		if (uniProtKB == null) {
			fireEventOccurred(new InconsistencyEvent(
					"Graph does not contain entities marked with UNIPROTKB. Not continuing.",
					getCurrentMethodName()));
			return;
		}

		// could be non-existing, then create new
		an = graph.getMetaData().getAttributeName(ATR_SEQUENCE);
		if (an == null) {
			graph.getMetaData().getFactory()
					.createAttributeName(ATR_SEQUENCE, String.class);
		}

		// very naively iterate over all concepts and their accessions
		int found = 0;
		int added = 0;
		fireEventOccurred(new GeneralOutputEvent(
				"Checking for UniProtKB accession on "
						+ graph.getConceptsOfConceptClass(cc).size()
						+ " concepts.", getCurrentMethodName()));
		int i = 0;
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(cc)) {
			for (ConceptAccession ac : c.getConceptAccessions()) {

				// found relevant accession
				if (ac.getElementOf().equals(uniProtKB)) {
					found++;
					// only add if no sequence yet
					if (c.getAttribute(an) == null) {
						addSequence(c, ac.getAccession());
						added++;
					}
				}
			}
			i++;
			if (i % 1000 == 0) {
				fireEventOccurred(new GeneralOutputEvent("Finished " + i
						+ " concepts.", getCurrentMethodName()));
			}
		}

		// just some output at the end
		fireEventOccurred(new GeneralOutputEvent(
				"A total of " + found + " accessions processed and " + added
						+ " attributes created.", getCurrentMethodName()));
	}

	/**
	 * Get FASTA from RESTful service
	 * 
	 * @param c
	 *            ONDEXConcept
	 * @param accession
	 *            UniProt accession
	 */
	private void addSequence(ONDEXConcept c, String accession) throws Exception {
		// construct RESTful URL
		URL url = new URL("http://www.uniprot.org/uniprot/" + accession
				+ ".fasta");

		// open http connection
		HttpURLConnection uc = (HttpURLConnection) url.openConnection();

		// check for response code
		int code = uc.getResponseCode();

		if (code != 200) {
			// in the event of error
			String response = uc.getResponseMessage();
			fireEventOccurred(new ParsingErrorEvent("HTTP/1.x " + code + " "
					+ response, getCurrentMethodName()));
		} else {

			// get main content
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					uc.getInputStream()));

			// parse FASTA
			StringBuffer sb = new StringBuffer();
			while (reader.ready()) {
				String line = reader.readLine();

				// ignoring header information
				if (!line.startsWith(">")) {
					sb.append(line);
				}
			}

			// create sequence on concept
			c.createAttribute(an, sb.toString(), false);
		}
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

}
