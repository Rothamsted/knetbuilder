package net.sourceforge.ondex.export.clusters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.IntegerRangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.export.ONDEXExport;

public class Export extends ONDEXExport implements ArgumentNames {

	@Override
	public String getId() {
		return "clusterexporter";
	}

	@Override
	public String getName() {
		return "OXL Cluster Exporter";
	}

	@Override
	public String getVersion() {
		return "05.09.2012";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		IntegerRangeArgumentDefinition minsizeArg = new IntegerRangeArgumentDefinition(
				MINSIZE_ARG, MINSIZE_ARG_DESC, true, 3, 1, Integer.MAX_VALUE);
		StringArgumentDefinition prefixArg = new StringArgumentDefinition(
				PREFIX_ARG, PREFIX_ARG_DESC, true, "", false);
		BooleanArgumentDefinition remainingArg = new BooleanArgumentDefinition(
				REMAINING_ARG, REMAINING_ARG_DESC, true, true);
		FileArgumentDefinition exportDir = new FileArgumentDefinition(
				FileArgumentDefinition.EXPORT_DIR,
				FileArgumentDefinition.EXPORT_DIR_DESC, true, false, true,
				false);
		return new ArgumentDefinition<?>[] { minsizeArg, prefixArg,
				remainingArg, exportDir };
	}

	@Override
	public void start() throws Exception {

		// get minimum cluster size argument
		int minsize = (Integer) args.getUniqueValue(MINSIZE_ARG);
		fireEventOccurred(new GeneralOutputEvent("Minimum cluster size: "
				+ minsize, getCurrentMethodName()));

		// get filename prefix argument
		String prefix = (String) args.getUniqueValue(PREFIX_ARG);
		fireEventOccurred(new GeneralOutputEvent("Filename prefix: " + prefix,
				getCurrentMethodName()));

		// get remaining argument
		boolean remaining = (Boolean) args.getUniqueValue(REMAINING_ARG);
		fireEventOccurred(new GeneralOutputEvent("Export remaining concepts: "
				+ remaining, getCurrentMethodName()));

		// get export directory argument
		String dir = (String) args
				.getUniqueValue(FileArgumentDefinition.EXPORT_DIR);
		fireEventOccurred(new GeneralOutputEvent("Exporting to directory: "
				+ dir, getCurrentMethodName()));

		// identify all clusters in graph
		final Map<ONDEXConcept, Set<ONDEXConcept>> clusters = identifyClusters();

		// sort clusters by size
		ONDEXConcept[] keys = clusters.keySet().toArray(new ONDEXConcept[0]);
		Arrays.sort(keys, new Comparator<ONDEXConcept>() {

			@Override
			public int compare(ONDEXConcept o1, ONDEXConcept o2) {
				return clusters.get(o2).size() - clusters.get(o1).size();
			}
		});

		// all concepts not in clusters of minimum size
		Set<ONDEXConcept> leftOver = new HashSet<ONDEXConcept>();

		// information file with concept names and accessions
		BufferedWriter infoWriter = new BufferedWriter(new FileWriter(dir
				+ File.separator + prefix + "_information.tab"));

		// write clusters in descending order of size
		int i = 1;
		for (ONDEXConcept key : keys) {
			if (clusters.get(key).size() >= minsize) {
				String filename = dir + File.separator + prefix + i + ".oxl";
				writeCluster(clusters.get(key), filename);
				writeInfo(clusters.get(key), filename, infoWriter);
				i++;
			} else if (remaining) {
				// add all remaining concepts
				leftOver.addAll(clusters.get(key));
			}
		}

		// write remaining concepts
		if (remaining) {
			String filename = dir + File.separator + prefix + "_rest.oxl";
			writeCluster(leftOver, filename);
			writeInfo(leftOver, filename, infoWriter);
		}

		infoWriter.close();
	}

	/**
	 * Writes concept names and accessions for all concepts in given cluster to
	 * cluster information file.
	 * 
	 * @param cluster
	 *            set of concepts to write
	 * @param filename
	 *            file name containing cluster
	 * @param infoWriter
	 *            output writer
	 */
	private void writeInfo(Set<ONDEXConcept> cluster, String filename,
			BufferedWriter infoWriter) throws Exception {
		// write concept names and accessions
		for (ONDEXConcept c : cluster) {

			// first column file name
			infoWriter.write(filename);

			// second column all concept names
			Set<String> names = new HashSet<String>();
			for (ConceptName cn : c.getConceptNames()) {
				names.add(cn.getName());
			}
			String[] sorted = names.toArray(new String[names.size()]);
			Arrays.sort(sorted);
			infoWriter.write("\t\"");
			for (int i = 0; i < sorted.length; i++) {
				infoWriter.write(sorted[i]);
				if (i < sorted.length - 1)
					infoWriter.write(", ");
			}
			infoWriter.write("\"");

			// third column all concept accessions
			infoWriter.write("\t\"");
			ConceptAccession[] cas = c.getConceptAccessions().toArray(
					new ConceptAccession[0]);
			Arrays.sort(cas, new Comparator<ConceptAccession>() {

				@Override
				public int compare(ConceptAccession o1, ConceptAccession o2) {
					if (o1.getElementOf().equals(o2.getElementOf()))
						return o1.getAccession().compareTo(o2.getAccession());
					return o1.getElementOf().getId()
							.compareTo(o2.getElementOf().getId());
				}
			});
			for (int i = 0; i < cas.length; i++) {
				infoWriter.write(cas[i].getAccession());
				infoWriter.write(" (" + cas[i].getElementOf().getId() + ")");
				if (i < cas.length - 1)
					infoWriter.write(", ");
			}
			infoWriter.write("\"\n");
		}
	}

	/**
	 * Writes given concepts as a subgraph to OXL
	 * 
	 * @param concepts
	 *            set of concepts to write
	 * @param filename
	 *            file name for output
	 */
	private void writeCluster(Set<ONDEXConcept> concepts, String filename)
			throws Exception {

		// new OXL exporter
		net.sourceforge.ondex.export.oxl.Export export = new net.sourceforge.ondex.export.oxl.Export();
		export.setLegacyMode(true);

		// set exporter arguments
		ONDEXPluginArguments pluginArgs = new ONDEXPluginArguments(
				export.getArgumentDefinitions());
		pluginArgs.setOption(FileArgumentDefinition.EXPORT_FILE, filename);
		export.setArguments(pluginArgs);

		// set concepts to export
		export.setConcepts(concepts);

		// set relations to export
		Set<ONDEXRelation> relations = new HashSet<ONDEXRelation>();
		for (ONDEXConcept c : concepts) {
			relations.addAll(graph.getRelationsOfConcept(c));
		}
		export.setRelations(relations);

		// start export
		export.start();
	}

	/**
	 * Extract all clusters from graph using breadth first search. Arbitrary
	 * chosen root concept.
	 * 
	 * @return map of root concept to all cluster members
	 */
	private Map<ONDEXConcept, Set<ONDEXConcept>> identifyClusters() {

		Map<ONDEXConcept, Set<ONDEXConcept>> clusters = new HashMap<ONDEXConcept, Set<ONDEXConcept>>();

		// concepts already assigned to a cluster
		Set<ONDEXConcept> seen = new HashSet<ONDEXConcept>();

		// process all concepts in graph
		for (ONDEXConcept c : graph.getConcepts()) {
			if (!seen.contains(c)) {
				// start a new cluster
				clusters.put(c, new HashSet<ONDEXConcept>());

				// perform a breadth-first search
				LinkedList<ONDEXConcept> stack = new LinkedList<ONDEXConcept>();
				stack.add(c);
				while (!stack.isEmpty()) {

					// pop top element of stack
					ONDEXConcept top = stack.pop();
					clusters.get(c).add(top);
					seen.add(top);

					// get all relations of this concept
					for (ONDEXRelation r : graph.getRelationsOfConcept(top)) {
						if (r.getFromConcept().equals(top)) {

							// get to concept
							ONDEXConcept to = r.getToConcept();
							if (!seen.contains(to)) {
								// queue up to concept
								stack.add(to);
							}
						}
						// ignore self-loops
						else if (r.getToConcept().equals(top)) {

							// get from concept
							ONDEXConcept from = r.getFromConcept();
							if (!seen.contains(from)) {
								// queue up from concept
								stack.add(from);
							}
						}
					}
				}
			}
		}

		return clusters;
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

}
