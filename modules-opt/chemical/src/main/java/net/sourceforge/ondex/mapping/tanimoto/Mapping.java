package net.sourceforge.ondex.mapping.tanimoto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.tools.data.ChemicalStructure;

/**
 * Chemical similarity mapping based on tanimoto scores
 * 
 * @author taubertj
 *
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Status(status = StatusType.STABLE, description = "Tested October 2012 (Jan Taubert)")
public class Mapping extends ONDEXMapping implements ArgumentNames, MetaData {

	/**
	 * Textbook example of a stream globber.
	 * 
	 * @author taubertj
	 */
	private class StreamGobbler extends Thread {

		InputStream is;

		StringBuffer sb = null;

		String type;

		boolean verbose = true;

		StreamGobbler(InputStream is, String type) {
			this(is, type, true, null);
		}

		StreamGobbler(InputStream is, String type, boolean verbose,
				StringBuffer sb) {
			this.is = is;
			this.type = type;
			this.verbose = verbose;
			this.sb = sb;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (verbose)
						System.out.println(type + ">" + line);
					if (sb != null)
						sb.append(line + "\n");
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * In debug mode will write results into a file
	 */
	public static final boolean DEBUG = false;

	float cutoff = 0.8F;

	String options = "";

	/**
	 * Counts total SMILES written
	 */
	private int total = 0;

	boolean withinCV = false;

	/**
	 * Build fast search index
	 * 
	 * @param tempDir
	 * @param smilesFile
	 * @param executable
	 * @return
	 * @throws Exception
	 */
	private File buildIndex(File tempDir, File smilesFile, File executable)
			throws Exception {

		// build index: babel mysmiles.smi -ofs
		String[] cmd = { executable.getAbsolutePath(), smilesFile.getName(),
				"-ofs", options };
		Process babel = Runtime.getRuntime().exec(cmd, null, tempDir);

		// parse STDOUT and STDERR
		StreamGobbler errorGobbler = new StreamGobbler(babel.getErrorStream(),
				"ERROR");
		StreamGobbler outputGobbler = new StreamGobbler(babel.getInputStream(),
				"OUTPUT");

		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		// wait for babel
		int exitVal = babel.waitFor();
		System.out.println("Babel ExitValue: " + exitVal);

		// check index was build
		File indexFile = new File(smilesFile.getAbsolutePath().replace(".smi",
				".fs"));
		if (!indexFile.exists()) {
			fireEventOccurred(new InconsistencyEvent("No index "
					+ indexFile.getAbsolutePath() + " was build.",
					getCurrentMethodName()));
			return null;
		}
		indexFile.deleteOnExit();

		return indexFile;
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		FileArgumentDefinition pathToExec = new FileArgumentDefinition(
				PATH_TO_EXEC_ARG, PATH_TO_EXEC_ARG_DESC, true, true, false);

		RangeArgumentDefinition<Float> cutoff = new RangeArgumentDefinition<Float>(
				CUTOFF_ARG, CUTOFF_ARG_DESC, false, 0.7F, 0F, 1F, Float.class);

		BooleanArgumentDefinition withinCV = new BooleanArgumentDefinition(
				WITHIN_DATASOURCE_ARG, WITHIN_DATASOURCE_ARG_DESC, false, false);

		StringArgumentDefinition options = new StringArgumentDefinition(
				EXEC_OPTIONS_ARG, EXEC_OPTIONS_ARG_DESC, false, "", false);

		return new ArgumentDefinition<?>[] { pathToExec, cutoff, withinCV,
				options };
	}

	@Override
	public String getId() {
		return "tanimoto";
	}

	@Override
	public String getName() {
		return "Chemical Similarity Search";
	}

	@Override
	public String getVersion() {
		return "02.08.2012";
	}

	/**
	 * Process each result
	 * 
	 * @param results
	 * @throws Exception
	 */
	private void processResults(List<String> results) throws Exception {

		// initialise meta data
		EvidenceType evidencetype = graph.getMetaData().getEvidenceType(ET);
		if (evidencetype == null)
			evidencetype = graph.getMetaData().getFactory()
					.createEvidenceType(ET);
		AttributeName conf = graph.getMetaData().getAttributeName(ATTR_CONF);
		RelationType relationtype = graph.getMetaData().getRelationType(RT);

		for (String s : results) {
			// ignore empty OpenBabel lines, might be a bug
			// TODO: find OpenBabel bug about ionised compounds
			if (s.trim().length() == 0)
				continue;

			String[] lines = s.split("\n");

			// the first line is always the query
			int fromId = Integer.parseInt(lines[0].split(" ")[0]);
			ONDEXConcept from = graph.getConcept(fromId);
			if (from == null) {
				fireEventOccurred(new InconsistencyEvent("From concept for ID "
						+ fromId + " not found.", getCurrentMethodName()));
				return;
			}
			DataSource dsFrom = from.getElementOf();

			for (int i = 1; i < lines.length; i++) {

				// tanimoto score is space separated
				String[] line = lines[i].split(" ");
				int toId = Integer.parseInt(line[0]);
				ONDEXConcept to = graph.getConcept(toId);
				if (to == null) {
					fireEventOccurred(new InconsistencyEvent(
							"To concept for ID " + toId + " not found.",
							getCurrentMethodName()));
					return;
				}
				DataSource dsTo = to.getElementOf();

				// only map between different data sources
				if (withinCV || !dsFrom.equals(dsTo)) {
					// create relation if not exit
					ONDEXRelation r = graph.getRelation(from, to, relationtype);
					if (r == null)
						r = graph.getFactory().createRelation(from, to,
								relationtype, evidencetype);
					else
						r.addEvidenceType(evidencetype);

					// add score
					Double score = Double.valueOf(line[1]);
					r.createAttribute(conf, score, false);
				}
			}
		}
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	/**
	 * Runs OpenBabel for similarity search
	 * 
	 * @param s
	 * @param tempDir
	 * @param smilesFile
	 * @param indexFile
	 * @param executable
	 * @param counter
	 * @return
	 * @throws Exception
	 */
	private String runBabel(String s, File tempDir, File smilesFile,
			File indexFile, File executable, int counter) throws Exception {

		String[] split = s.split(" ");

		// write query into temp file
		File queryFile = new File(indexFile.getAbsolutePath().replace(".fs",
				"_query" + split[1] + ".smi"));
		BufferedWriter writer = new BufferedWriter(new FileWriter(queryFile));
		writer.write(s);
		writer.flush();
		writer.close();

		// search using index file: babel mysmiles.fs -Squery.smi -otxt -at0.7
		String[] cmd = { executable.getAbsolutePath(), indexFile.getName(),
				"-S" + queryFile.getName(), "-otxt", options, "-aat" + cutoff };
		Process babel = Runtime.getRuntime().exec(cmd, null, tempDir);

		// parse STDOUT and STDERR
		StreamGobbler errorGobbler = new StreamGobbler(babel.getErrorStream(),
				"ERROR", false, null);
		StringBuffer results = new StringBuffer();
		StreamGobbler outputGobbler = new StreamGobbler(babel.getInputStream(),
				"OUTPUT", false, results);

		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		// wait for babel
		int exitVal = babel.waitFor();
		if (exitVal != 0)
			System.out.println("Babel ExitValue: " + exitVal);

		// remove temporary file
		queryFile.delete();

		int percentage = (int) (((double) counter / (double) total) * 100.0);
		if (percentage % 10 == 0) {
			System.out.println("Finished " + percentage + "% (" + counter + "/"
					+ total + ")");
		}

		if (DEBUG) {
			// file with all match lines for debugging
			BufferedWriter debugOut = new BufferedWriter(new FileWriter(
					indexFile.getAbsolutePath() + ".debug", true));
			debugOut.write("> " + split[1]);
			debugOut.write(results.toString());
			debugOut.flush();
			debugOut.close();
		}

		return results.toString().trim();
	}

	@Override
	public void start() throws Exception {
		// current arguments
		final File executable = new File(
				(String) args.getUniqueValue(PATH_TO_EXEC_ARG));
		if (!executable.exists()) {
			fireEventOccurred(new WrongParameterEvent("Executable "
					+ executable.getAbsolutePath() + "does not exists.",
					getCurrentMethodName()));
			return;
		}

		if (args.getUniqueValue(CUTOFF_ARG) != null) {
			cutoff = (Float) args.getUniqueValue(CUTOFF_ARG);
		}

		if (args.getUniqueValue(WITHIN_DATASOURCE_ARG) != null) {
			withinCV = (Boolean) args.getUniqueValue(WITHIN_DATASOURCE_ARG);
		}

		if (args.getUniqueValue(EXEC_OPTIONS_ARG) != null) {
			options = (String) args.getUniqueValue(EXEC_OPTIONS_ARG);
		}

		// ChemicalStruture attribute
		AttributeName anChemicalStructure = graph.getMetaData()
				.getAttributeName(ATTR_CHEMICAL_STRUCTURE);
		if (anChemicalStructure == null)
			anChemicalStructure = graph
					.getMetaData()
					.getFactory()
					.createAttributeName(ATTR_CHEMICAL_STRUCTURE,
							ATTR_CHEMICAL_STRUCTURE, ChemicalStructure.class);

		// check if there is data to process
		if (graph.getConceptsOfAttributeName(anChemicalStructure).size() == 0) {
			fireEventOccurred(new InconsistencyEvent(
					"No concepts with attribute" + anChemicalStructure.getId()
							+ " found.", getCurrentMethodName()));
			return;
		}

		// get temporary directory
		String ondexDir = net.sourceforge.ondex.config.Config.ondexDir;
		final File tempDir = new File(ondexDir + File.separator + "temp");
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}

		// write all SMILES from graph
		final File smilesFile = writeSMILES(tempDir, anChemicalStructure);

		// build fast search index
		final File indexFile = buildIndex(tempDir, smilesFile, executable);
		if (indexFile == null)
			return;

		// capture results
		List<String> results = new ArrayList<String>();

		// need to process every single SMILE again
		BufferedReader reader = new BufferedReader(new FileReader(smilesFile));
		int i = 0;
		while (reader.ready()) {

			// ignore empty lines
			final String s = reader.readLine();
			if (s.trim().length() == 0)
				continue;

			// run babel
			i++;
			results.add(runBabel(s, tempDir, smilesFile, indexFile, executable,
					i));
		}
		reader.close();

		processResults(results);
	}

	/**
	 * Write all SMILES from graph into file
	 * 
	 * @param tempDir
	 * @param an
	 * @return
	 * @throws Exception
	 */
	private File writeSMILES(File tempDir, AttributeName an) throws Exception {

		File smilesFile = new File(tempDir.getAbsolutePath() + File.separator
				+ System.currentTimeMillis() + ".smi");
		if (!DEBUG)
			smilesFile.deleteOnExit();

		// text-file output writer
		BufferedWriter writer = new BufferedWriter(new FileWriter(smilesFile));

		// get all smiles into file
		for (ONDEXConcept c : graph.getConceptsOfAttributeName(an)) {
			Attribute attribute = c.getAttribute(an);

			ChemicalStructure cs = (ChemicalStructure) attribute.getValue();
			String s = cs.getSMILES();
			if (s != null) {
				writer.write(s.trim() + " " + c.getId() + "\n");
				total++;
			}
		}

		// close writer
		writer.flush();
		writer.close();

		return smilesFile;
	}

}
