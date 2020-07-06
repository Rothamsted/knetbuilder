package net.sourceforge.ondex.mapping.blastbased;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.PluginErrorEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;

/**
 * Implements a mapping using BLAST similarity searches for sequence data.
 * 
 * @author taubertj
 * @version 28.03.2012
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Status(description = "Tested December 2013 (Artem Lysenko)", status = StatusType.STABLE)
public class Mapping extends ONDEXMapping implements ArgumentNames {

	private static int CPUS = Runtime.getRuntime().availableProcessors() > 1 ? Runtime
			.getRuntime().availableProcessors() : 2;

	/**
	 * Constructor
	 */
	public Mapping() {

	}

	@Override
	public void start() throws InvalidPluginArgumentException {

		// current arguments
		File blastDir = new File(
				(String) args.getUniqueValue(PATH_TO_BLAST_ARG));
		float evalue = 0.001F;
		if (args.getUniqueValue(EVALUE_ARG) != null) {
			evalue = (Float) args.getUniqueValue(EVALUE_ARG);
		}
		String seqAttr = "AA";
		if (args.getUniqueValue(SEQUENCE_ATTRIBUTE_ARG) != null) {
			seqAttr = (String) args.getUniqueValue(SEQUENCE_ATTRIBUTE_ARG);
		}
		String seqType = "AA";
		if (args.getUniqueValue(SEQUENCE_TYPE_ARG) != null) {
			seqType = (String) args.getUniqueValue(SEQUENCE_TYPE_ARG);
		}

		// check all preconditions
		boolean ready = true;

		File blastpExe = null;
		File blastnExe = null;
		File makeblastdbExe = null;
		if (!blastDir.exists()) {
			fireEventOccurred(new WrongParameterEvent(
					"BLAST executable directory does not exists.",
					getCurrentMethodName()));
			ready = false;
		} else {
			File[] files = blastDir.listFiles();
			for (File file : files) {
				if (file.getName().startsWith("blastp")) {
					blastpExe = file;
				} else if (file.getName().startsWith("blastn")) {
					blastnExe = file;
				} else if (file.getName().startsWith("makeblastdb")) {
					makeblastdbExe = file;
				}
			}
			if (blastpExe == null) {
				fireEventOccurred(new WrongParameterEvent(
						"No blastp executable found.", getCurrentMethodName()));
				ready = false;
			}
			if (blastnExe == null) {
				fireEventOccurred(new WrongParameterEvent(
						"No blastn executable found.", getCurrentMethodName()));
				ready = false;
			}
			if (makeblastdbExe == null) {
				fireEventOccurred(new WrongParameterEvent(
						"No makeblastdb executable found.",
						getCurrentMethodName()));
				ready = false;
			}
		}

		if (!seqType.equals("AA") && !seqType.equals("NA")) {
			fireEventOccurred(new WrongParameterEvent(
					"Only AA or NA allowed as sequence type.",
					getCurrentMethodName()));
			ready = false;
		}

		AttributeName an = graph.getMetaData().getAttributeName(seqAttr);
		if (an == null) {
			fireEventOccurred(new AttributeNameMissingEvent(
					"Given AttributeName for sequences not found. " + seqAttr,
					"[Mapping - start]"));
			ready = false;
		}

		RelationType h_s_s = graph.getMetaData().getRelationType(
				MetaData.relType);
		if (h_s_s == null) {
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.relType,
					getCurrentMethodName()));
			ready = false;
		}

		EvidenceType et = graph.getMetaData()
				.getEvidenceType(MetaData.evidence);
		if (et == null) {
			fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.evidence,
					getCurrentMethodName()));
			ready = false;
		}

		AttributeName hitAn = graph.getMetaData().getAttributeName(
				MetaData.evalueHit);
		if (hitAn == null) {
			fireEventOccurred(new AttributeNameMissingEvent(MetaData.evalueHit,
					getCurrentMethodName()));
			ready = false;
		}

		AttributeName attBitScore = graph.getMetaData().getAttributeName(
				MetaData.BLAST_BITSCORE);
		if (attBitScore == null) {
			fireEventOccurred(new AttributeNameMissingEvent(
					MetaData.BLAST_BITSCORE, getCurrentMethodName()));
			ready = false;
		}

		AttributeName attPSI = graph.getMetaData().getAttributeName(
				MetaData.BLAST_PSI);
		if (attPSI == null) {
			fireEventOccurred(new AttributeNameMissingEvent(MetaData.BLAST_PSI,
					getCurrentMethodName()));
			ready = false;
		}

		// proceed if ready
		if (ready) {

			String ondexDir = net.sourceforge.ondex.config.Config.ondexDir;

			File tempDir = new File(ondexDir + File.separator + "temp");
			if (!tempDir.exists()) {
				tempDir.mkdir();
			}
			File fasta = new File(tempDir.getAbsolutePath() + File.separator
					+ System.currentTimeMillis() + ".fasta");

			try {
				// FASTA output writer
				BufferedWriter writer = new BufferedWriter(
						new FileWriter(fasta));

				// get all sequences out in fasta file
				for (ONDEXConcept concept : graph
						.getConceptsOfAttributeName(an)) {
					// write local identifier
					writer.write(">lcl|" + concept.getId());
					writer.newLine();
					// write sequence
					Attribute attribute = concept.getAttribute(an);
					writer.write(attribute.getValue().toString()
							.replaceAll("\n", ""));
					writer.newLine();
				}

				// close writer
				writer.flush();
				writer.close();

				// makeblastdb
				String format = "prot";
				if (seqType.equals("NA"))
					format = "nucl";

				String[] cmd = { makeblastdbExe.getAbsolutePath(), "-dbtype",
						format, "-in", fasta.getName() };
				Process makeblastdb = Runtime.getRuntime().exec(cmd, null,
						tempDir);

				// parse STDOUT and STDERR
				StreamGobbler errorGobbler = new StreamGobbler(
						makeblastdb.getErrorStream(), "ERROR");
				StreamGobbler outputGobbler = new StreamGobbler(
						makeblastdb.getInputStream(), "OUTPUT");

				// kick them off
				errorGobbler.start();
				outputGobbler.start();

				// wait for makeblastdb
				int exitVal = makeblastdb.waitFor();
				System.out.println("Makeblastdb ExitValue: " + exitVal);

				// start BLAST
				File program = blastpExe;
				if (seqType.equals("NA"))
					program = blastnExe;

				cmd = new String[] { program.getAbsolutePath(), "-num_threads",
						(CPUS - 1) + "", "-outfmt",
						"6 qacc sacc pident evalue bitscore", "-evalue",
						Float.valueOf(evalue).toString(), "-db",
						fasta.getName(), "-query", fasta.getName(), "-out",
						fasta.getName() + ".out" };
				for (String s : cmd)
					System.out.print(s + " ");
				System.out.println();
				Process blast = Runtime.getRuntime().exec(cmd, null, tempDir);

				// parse STDOUT and STDERR
				BlastError stdout = new BlastError(blast.getInputStream());
				BlastError stderr = new BlastError(blast.getErrorStream());

				// kick them off
				stdout.start();
				stderr.start();

				// wait for BLAST to finish
				exitVal = blast.waitFor();
				System.out.println("BLAST ExitValue: " + exitVal);

				// parse in results from file
				File fileIn = new File(tempDir + File.separator
						+ fasta.getName() + ".out");
				BlastParser parser = new BlastParser(fileIn);
				parser.start();

				// wait for parser thread to finish
				synchronized (this) {
					while (!parser.isReady()) {
						this.wait(10);
					}
				}

				boolean withinCV = false;
				if (args.getUniqueValue(WITHIN_DATASOURCE_ARG) != null) {
					withinCV = (Boolean) args
							.getUniqueValue(WITHIN_DATASOURCE_ARG);
				}

				// iterate over results
				Hashtable<ONDEXConcept, List<Result>> mapping = new Hashtable<ONDEXConcept, List<Result>>();
				Iterator<Result> it = parser.getResult().iterator();
				while (it.hasNext()) {
					Result result = it.next();
					int sourceId = Integer
							.parseInt(result.getSourceAccession());
					int targetId = Integer
							.parseInt(result.getTargetAccession());
					// different concept ids
					if (sourceId != targetId) {
						ONDEXConcept source = graph.getConcept(sourceId);
						ONDEXConcept target = graph.getConcept(targetId);
						// different cvs
						if (withinCV
								|| !source.getElementOf().equals(
										target.getElementOf())) {
							// same concept class
							boolean allowed = false;
							if (source.getOfType().equals(target.getOfType())) {
								allowed = true;
							} else {
								if (this.getCCtoMapTo(graph, source.getOfType())
										.contains(target.getOfType())) {
									allowed = true;
								}
							}
							if (allowed) {
								if (!mapping.containsKey(source))
									mapping.put(source, new ArrayList<Result>());
								mapping.get(source).add(result);
							}
						}
					}
				}

				// create mappings
				for (ONDEXConcept from : mapping.keySet()) {

					Iterator<Result> results = mapping.get(from).iterator();
					while (results.hasNext()) {
						Result result = results.next();
						int targetId = Integer.parseInt(result
								.getTargetAccession());
						ONDEXConcept to = graph.getConcept(targetId);

						// check if relation already exists
						ONDEXRelation r = graph.getRelation(from, to, h_s_s);
						if (r == null)
							r = graph.getFactory().createRelation(from, to,
									h_s_s, et);

						// check if there is a previous score
						if (r.getAttribute(hitAn) != null) {
							Attribute attribute = r.getAttribute(hitAn);
							double old = (Double) attribute.getValue();
							// keep lowest E-value
							if (old > result.getValue())
								attribute.setValue(Double.valueOf(result
										.getValue()));
						} else {
							r.createAttribute(hitAn,
									Double.valueOf(result.getValue()), false);
						}
						if (r.getAttribute(attBitScore) != null) {
							Attribute attribute = r.getAttribute(attBitScore);
							double old = (Double) attribute.getValue();
							// keep highest Bitscore
							if (old < result.getBitScore())
								attribute.setValue(Double.valueOf(result
										.getBitScore()));
						} else {
							r.createAttribute(attBitScore,
									Double.valueOf(result.getBitScore()), false);
						}

						if (r.getAttribute(attPSI) != null) {
							Attribute attribute = r.getAttribute(attPSI);
							double old = (Double) attribute.getValue();
							// keep highest PSI
							if (old < result.getPercSequenceIdentity())
								attribute.setValue(Double.valueOf(result
										.getPercSequenceIdentity()));
						} else {
							r.createAttribute(attPSI, Double.valueOf(result
									.getPercSequenceIdentity()), false);
						}
					}
				}

			} catch (FileNotFoundException fnfe) {
				fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(),
						getCurrentMethodName()));
			} catch (IOException ioe) {
				fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
						getCurrentMethodName()));
			} catch (InterruptedException ie) {
				fireEventOccurred(new PluginErrorEvent(ie.getMessage(),
						getCurrentMethodName()));
			}
		}
	}

	/**
	 * Returns name of this mapping.
	 * 
	 * @return String
	 */
	public String getName() {
		return "BLAST based mapping";
	}

	/**
	 * Returns version of this mapping.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return "28.03.2012";
	}

	@Override
	public String getId() {
		return "blastbased";
	}

	/**
	 * Returns necessary arguments for this mapping. PATH_TO_BLAST_ARG is
	 * mandatory. EVALUE_ARG, SEQUENCE_ATTRIBUTE_ARG and SEQUENCE_TYPE_ARG are
	 * optional.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		StringMappingPairArgumentDefinition pairCC = new StringMappingPairArgumentDefinition(
				EQUIVALENT_CC_ARG, EQUIVALENT_CC_ARG_DESC, false, null, true);

		FileArgumentDefinition pathToBlast = new FileArgumentDefinition(
				PATH_TO_BLAST_ARG, PATH_TO_BLAST_ARG_DESC, true, true, true);

		RangeArgumentDefinition<Float> eValue = new RangeArgumentDefinition<Float>(
				EVALUE_ARG, EVALUE_ARG_DESC, false, 0.001F, 0F,
				Float.MAX_VALUE, Float.class);

		StringArgumentDefinition seqGDS = new StringArgumentDefinition(
				SEQUENCE_ATTRIBUTE_ARG, SEQUENCE_ATTRIBUTE_ARG_DESC, false,
				"AA", false);

		StringArgumentDefinition seqType = new StringArgumentDefinition(
				SEQUENCE_TYPE_ARG, SEQUENCE_TYPE_ARG_DESC, false, "AA", false);

		BooleanArgumentDefinition withinCV = new BooleanArgumentDefinition(
				WITHIN_DATASOURCE_ARG, WITHIN_DATASOURCE_ARG_DESC, false, false);

		return new ArgumentDefinition<?>[] { pairCC, pathToBlast, eValue,
				seqGDS, seqType, withinCV };
	}

	/**
	 * Does not require an indexed graph.
	 * 
	 * @return false
	 */
	public boolean requiresIndexedGraph() {
		return false;
	}

	/**
	 * Captures results of a BLAST run.
	 * 
	 * @author taubertj
	 */
	private class Result {

		private double eValue;

		private double bitScore;

		private double percSequenceIdentity;

		private String targetAccession;

		private String sourceAccession;

		public String getSourceAccession() {
			return sourceAccession;
		}

		public String getTargetAccession() {
			return targetAccession;
		}

		public double getValue() {
			return eValue;
		}

		public double getBitScore() {
			return bitScore;
		}

		public double getPercSequenceIdentity() {
			return percSequenceIdentity;
		}

		Result(String targetAccession, String sourceAccession, double psi,
				double eval, double bitscore) {
			this.bitScore = bitscore;
			this.eValue = eval;
			this.percSequenceIdentity = psi;
			this.targetAccession = targetAccession;
			this.sourceAccession = sourceAccession;
		}

		public String toString() {
			return this.getTargetAccession() + "-> "
					+ this.getSourceAccession() + "=" + this.getValue() + " "
					+ this.getBitScore();
		}

	}

	/**
	 * Thread for handling results returned by BLAST.
	 * 
	 * @author taubertj
	 */
	private class BlastParser extends Thread {

		File is;

		ArrayList<Result> resultList = new ArrayList<Result>();

		boolean ready = false;

		BlastParser(File is) {
			this.is = is;
		}

		public void run() {
			try {
				BufferedReader br = new BufferedReader(new FileReader(is));
				String line = null;
				while ((line = br.readLine()) != null) {
					// System.out.println(line);
					String[] split = line.split("\t");
					resultList.add(new Result(split[0].replace("lcl|", ""),
							split[1].replace("lcl|", ""), Double
									.parseDouble(split[2]), Double
									.parseDouble(split[3]), Double
									.parseDouble(split[4])));
				}
				ready = true;
				br.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public ArrayList<Result> getResult() {
			return new ArrayList<Result>(resultList);
		}

		public boolean isReady() {
			return ready;
		}
	}

	/**
	 * Captures STDERR of BLAST.
	 * 
	 * @author taubertj
	 */
	private class BlastError extends Thread {

		InputStream is;

		BlastError(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					System.err.println(line);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Textbook example of a stream globber.
	 * 
	 * @author taubertj
	 */
	private class StreamGobbler extends Thread {

		InputStream is;

		String type;

		StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					System.out.println(type + ">" + line);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
