package net.sourceforge.ondex.mapping.inparanoid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.StreamGobbler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.PluginErrorEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.mapping.inparanoid.clustering.Inparalog;
import net.sourceforge.ondex.mapping.inparanoid.clustering.InparalogParser;
import net.sourceforge.ondex.mapping.inparanoid.clustering.MergeGroups;
import net.sourceforge.ondex.mapping.inparanoid.clustering.OndexMatch;
import net.sourceforge.ondex.mapping.inparanoid.clustering.Ortholog;
import net.sourceforge.ondex.mapping.inparanoid.clustering.OrthologParser;
import net.sourceforge.ondex.mapping.inparanoid.clustering.OutputParser;

/**
 * Implements the INPARANOID algorithm as a mapping method for the ONDEX system.
 * 
 * @author taubertj
 * @version 29.03.2012
 */
@Status(description = "Tested March 2010 (Artem Lysenko) Works for the tutorial example, fails in almost all other cases", status = StatusType.STABLE)
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
public class Mapping extends ONDEXMapping implements ArgumentNames, MetaData {

	private static int CPUS = Runtime.getRuntime().availableProcessors() > 1 ? Runtime
			.getRuntime().availableProcessors() : 2;

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

	// global DEBUG flag
	public static boolean DEBUG = false;

	// current instance of this mapping
	private static Mapping instance;

	/**
	 * Constructor
	 */
	public Mapping() {
	}

	/**
	 * Returns name of this mapping.
	 * 
	 * @return String
	 */
	public String getName() {
		return new String("Inparanoid");
	}

	/**
	 * Returns version of this mapping.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return new String("29.03.2012");
	}

	@Override
	public String getId() {
		return "inparanoid";
	}

	/**
	 * Returns necessary arguments for this mapping. PATH_TO_BLAST_ARG is
	 * mandatory. EVALUE_ARG, SEQUENCE_ATTRIBUTE_ARG and SEQUENCE_TYPE_ARG are
	 * optional.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		FileArgumentDefinition pathToBlast = new FileArgumentDefinition(
				PATH_TO_BLAST_ARG, PATH_TO_BLAST_ARG_DESC, true, true, true);

		RangeArgumentDefinition<Float> eValue = new RangeArgumentDefinition<Float>(
				EVALUE_ARG, EVALUE_ARG_DESC, false, 0.001F, 0F,
				Float.MAX_VALUE, Float.class);

		StringArgumentDefinition seqGDS = new StringArgumentDefinition(
				SEQUENCE_ATTRIBUTE_ARG, SEQUENCE_ATTRIBUTE_ARG_DESC, false,
				atAA, false);

		StringArgumentDefinition seqType = new StringArgumentDefinition(
				SEQUENCE_TYPE_ARG, SEQUENCE_TYPE_ARG_DESC, false, atAA, false);

		RangeArgumentDefinition<Integer> cutOff = new RangeArgumentDefinition<Integer>(
				CUTOFF_ARG, CUTOFF_ARG_DESC, false, 30, 0, Integer.MAX_VALUE,
				Integer.class);

		RangeArgumentDefinition<Float> overlap = new RangeArgumentDefinition<Float>(
				OVERLAP_ARG, OVERLAP_ARG_DESC, false, 0.5F, 0F, 1F, Float.class);

		return new ArgumentDefinition<?>[] { pathToBlast, eValue, seqGDS,
				seqType, cutOff, overlap };
	}

	/**
	 * No IndexONDEXGraph is required.
	 * 
	 * @return false
	 */
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public void start() throws Exception {

		// set current instance
		instance = this;

		// current arguments
		File blastDir = new File(
				(String) args.getUniqueValue(PATH_TO_BLAST_ARG));
		float evalue = 0.001F;
		if (args.getUniqueValue(EVALUE_ARG) != null) {
			evalue = (Float) args.getUniqueValue(EVALUE_ARG);
		}
		System.err.println("Evalue parsed: " + evalue);
		String seqAttr = atAA;
		if (args.getUniqueValue(SEQUENCE_ATTRIBUTE_ARG) != null) {
			seqAttr = (String) args.getUniqueValue(SEQUENCE_ATTRIBUTE_ARG);
		}
		String seqType = atAA;
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
		if (!seqType.equals(atAA) && !seqType.equals(atNA)) {
			fireEventOccurred(new WrongParameterEvent("Only " + atAA + " or "
					+ atNA + " allowed as sequence type.",
					getCurrentMethodName()));
			ready = false;
		}

		AttributeName anSeq = graph.getMetaData().getAttributeName(seqAttr);
		if (anSeq == null) {
			fireEventOccurred(new AttributeNameMissingEvent(
					"Given AttributeName for sequences not found. " + seqAttr,
					getCurrentMethodName()));
			ready = false;
		}

		AttributeName anTaxid = graph.getMetaData().getAttributeName(atTaxid);
		if (anTaxid == null) {
			fireEventOccurred(new AttributeNameMissingEvent(atTaxid,
					getCurrentMethodName()));
			ready = false;
		}

		AttributeName attributeConf = graph.getMetaData().getAttributeName(
				atCONF);
		if (attributeConf == null) {
			fireEventOccurred(new AttributeNameMissingEvent(atCONF,
					getCurrentMethodName()));
			ready = false;
		}

		AttributeName attributeBitscore = graph.getMetaData().getAttributeName(
				atBITSCORE);
		if (attributeBitscore == null) {
			fireEventOccurred(new AttributeNameMissingEvent(atBITSCORE,
					getCurrentMethodName()));
			ready = false;
		}

		RelationType relationTypeOrtho = graph.getMetaData().getRelationType(
				rtsOrtolog);
		if (relationTypeOrtho == null) {
			fireEventOccurred(new RelationTypeMissingEvent(rtsOrtolog,
					getCurrentMethodName()));
			ready = false;
		}

		RelationType relationTypeIpara = graph.getMetaData().getRelationType(
				rtsPara);
		if (relationTypeIpara == null) {
			fireEventOccurred(new RelationTypeMissingEvent(rtsPara,
					getCurrentMethodName()));
			ready = false;
		}

		EvidenceType evidencetype = graph.getMetaData().getEvidenceType(
				etINPARANOID);
		if (evidencetype == null) {
			fireEventOccurred(new EvidenceTypeMissingEvent(etINPARANOID,
					getCurrentMethodName()));
			ready = false;
		}

		// get mapping specific parameters
		Integer cutoff = (Integer) args.getUniqueValue(CUTOFF_ARG);

		Float overlap = (Float) args.getUniqueValue(OVERLAP_ARG);

		// vector for ondex matches
		List<OndexMatch> vector = new Vector<OndexMatch>();

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
				int i = 0;
				for (ONDEXConcept ac : graph.getConceptsOfAttributeName(anSeq)) {
					DataSource dataSource = ac.getElementOf();
					ConceptClass cc = ac.getOfType();
					// parse out taxid
					String taxid = "-1";
					Attribute attributeTaxid = ac.getAttribute(anTaxid);
					if (attributeTaxid != null) {
						String value = (String) attributeTaxid.getValue();
						value = value.trim();
						if (value.length() > 0)
							taxid = value;
					}
					writer.write("> " + ac.getId() + "|" + dataSource.getId()
							+ "|" + cc.getId() + "|" + taxid + "|"
							+ ac.getPID() + "\n");
					// write sequence
					Attribute attributeSeq = ac.getAttribute(anSeq);
					writer.write(attributeSeq.getValue().toString() + "\n");
					i++;
				}
				fireEventOccurred(new GeneralOutputEvent("Written " + i
						+ " sequences.", getCurrentMethodName()));

				// close writer
				writer.flush();
				writer.close();

				// makeblastdb
				String format = "prot";
				if (seqType.equals(atNA))
					format = "nucl";

				String[] cmd = { makeblastdbExe.getAbsolutePath(), "-dbtype",
						format, "-in", fasta.getName() };
				Process makeblastdb = Runtime.getRuntime().exec(cmd, null,
						tempDir);

				// parse STDOUT and STDERR
				StreamGobbler errorGobbler = new StreamGobbler(
						makeblastdb.getErrorStream(), System.err);
				StreamGobbler outputGobbler = new StreamGobbler(
						makeblastdb.getInputStream(), System.out);

				// kick them off
				errorGobbler.start();
				outputGobbler.start();

				// wait for makeblastdb
				int exitVal = makeblastdb.waitFor();
				System.out.println("Makeblastdb ExitValue: " + exitVal);

				// start BLAST
				File program = blastpExe;
				if (seqType.equals(atNA))
					program = blastnExe;

				// compose BLAST command, no -m8 option currently
				File output = new File(fasta.getAbsolutePath() + "_results");
				cmd = new String[] { program.getAbsolutePath(), "-num_threads",
						(CPUS - 1) + "", "-evalue",
						Float.valueOf(evalue).toString(), "-db",
						fasta.getName(), "-query", fasta.getName(), "-out",
						output.getName() };
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

				// parse BLAST output
				System.err.println("Using output file:"
						+ output.getAbsolutePath());
				OutputParser parserThread = new OutputParser(graph,
						new FileInputStream(output), cutoff, overlap);
				parserThread.start();

				// finish reading whatever's left in the buffers
				parserThread.join();

				if (exitVal != 0)
					fireEventOccurred(new PluginErrorEvent("Process " + cmd
							+ " returned non-zero value:" + exitVal + "; "
							+ "Process error:\n", getCurrentMethodName()));
				else
					vector = parserThread.getMatches();

			} catch (InterruptedException ie) {
				fireEventOccurred(new PluginErrorEvent(ie.getMessage(),
						getCurrentMethodName()));
			}
		}

		// index according to taxids and concepts
		Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>> taxidToMatches = new Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>>();

		// sort matches according to taxid of query, match and query id
		Iterator<OndexMatch> it = vector.iterator();
		while (it.hasNext()) {

			OndexMatch match = it.next();

			// first index by query taxid
			if (!taxidToMatches.containsKey(match.getQueryTaxId()))
				taxidToMatches
						.put(match.getQueryTaxId(),
								new Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>());
			Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>> queryTaxids = taxidToMatches
					.get(match.getQueryTaxId());

			// second index by target taxid
			if (!queryTaxids.containsKey(match.getTargetTaxId()))
				queryTaxids
						.put(match.getTargetTaxId(),
								new Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>());
			Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>> targetTaxids = queryTaxids
					.get(match.getTargetTaxId());

			// third index by query concept
			if (!targetTaxids.containsKey(match.getQuery()))
				targetTaxids.put(match.getQuery(),
						new Hashtable<ONDEXConcept, OndexMatch>());
			Hashtable<ONDEXConcept, OndexMatch> matchQuery = targetTaxids
					.get(match.getQuery());

			// forth index by target concept
			matchQuery.put(match.getTarget(), match);
		}

		// some verbose output
		GeneralOutputEvent so = new GeneralOutputEvent("Taxids found: "
				+ taxidToMatches.keySet().size(), getCurrentMethodName());
		fireEventOccurred(so);

		// parse orthologs
		OrthologParser oparser = new OrthologParser(cutoff);
		Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>> orthologs = oparser
				.parse(taxidToMatches);

		// parse inparalogs
		InparalogParser iparser = new InparalogParser();
		Vector<Ortholog> orthos = iparser.parse(orthologs, taxidToMatches);

		// merge groups of orthologs
		MergeGroups mgroups = new MergeGroups(cutoff);
		orthos = mgroups.merge(orthos, taxidToMatches);

		// some verbose output
		so = new GeneralOutputEvent("Ortholog groups: " + orthos.size(),
				getCurrentMethodName());
		fireEventOccurred(so);

		// create relations for orthologs and inparalogs
		Iterator<Ortholog> it2 = orthos.iterator();
		while (it2.hasNext()) {

			// get next ortholog group and its orthologs
			Ortholog ortho = it2.next();
			ONDEXConcept mainA = ortho.getMainA().getConcept();
			ONDEXConcept mainB = ortho.getMainB().getConcept();

			// create relation between main orthologs in both directions
			ONDEXRelation r = graph.getFactory().createRelation(mainA, mainB,
					relationTypeOrtho, evidencetype);
			r.createAttribute(attributeBitscore, new Double(ortho.getScore()),
					false);
			r = graph.getFactory().createRelation(mainB, mainA,
					relationTypeOrtho, evidencetype);
			r.createAttribute(attributeBitscore, new Double(ortho.getScore()),
					false);

			// create relations for inparalogs for mainA
			Iterator<Inparalog> it3 = ortho.inA.iterator();
			while (it3.hasNext()) {
				Inparalog ipara = it3.next();
				ONDEXConcept c = ipara.getConcept();
				if (graph.getRelation(c, mainA, relationTypeIpara) == null) {
					r = graph.getFactory().createRelation(c, mainA,
							relationTypeIpara, evidencetype);
					r.createAttribute(attributeConf,
							new Double(ipara.getConfidence()), false);
				}
			}

			// create relations for inparalogs for mainB
			it3 = ortho.inB.iterator();
			while (it3.hasNext()) {
				Inparalog ipara = it3.next();
				ONDEXConcept c = ipara.getConcept();
				if (graph.getRelation(c, mainB, relationTypeIpara) == null) {
					r = graph.getFactory().createRelation(c, mainB,
							relationTypeIpara, evidencetype);
					r.createAttribute(attributeConf,
							new Double(ipara.getConfidence()), false);
				}
			}
		}
	}

	// /**
	// * To capture BLAST err output stream.
	// *
	// * @author taubertj
	// *
	// */
	// private class StreamReaderThread extends Thread {
	// StringBuffer mOut;
	//
	// InputStreamReader mIn;
	//
	// public StreamReaderThread(InputStream in, StringBuffer out) {
	// mOut = out;
	// mIn = new InputStreamReader(in);
	// }
	//
	// public void run() {
	// int ch;
	// try {
	// while (-1 != (ch = mIn.read()))
	// mOut.append((char) ch);
	// } catch (Exception e) {
	// mOut.append("\nRead error:" + e.getMessage());
	// }
	// }
	// }

	/**
	 * Convenience method for outputing the current method name in a dynamic way
	 * 
	 * @return the calling method name
	 */
	public static String getCurrentMethodName() {
		Exception e = new Exception();
		StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
		String name = trace.getMethodName();
		String className = trace.getClassName();
		int line = trace.getLineNumber();
		return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
				+ "]";
	}

	/**
	 * Propagate sub events to current instance of mapping.
	 * 
	 * @param et
	 *            EventType
	 */
	public static void propgateEvent(EventType et) {
		if (instance != null)
			instance.fireEventOccurred(et);
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
