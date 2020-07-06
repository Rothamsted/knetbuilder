package net.sourceforge.ondex.export.clusterinfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * Exports concepts per connected component (cluster) with tab-separated
 * information about concepts as user defines.
 * 
 * @author taubertj
 * 
 */
public class Export extends ONDEXExport implements ArgumentNames {

	@Override
	public String getId() {
		return "clusterinfo";
	}

	@Override
	public String getName() {
		return "Cluster information export";
	}

	@Override
	public String getVersion() {
		return "30.04.2011";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] {
				new StringArgumentDefinition(ATTRIBUTE_NAMES,
						ATTRIBUTE_NAMES_DESC, false, null, true),
				new StringArgumentDefinition(ACCESSION_DATASOURCES,
						ACCESSION_DATASOURCES_DESC, false, null, true),
				new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE,
						"Data file to export", true, false, false, false) };
	}

	@Override
	public void start() throws Exception {

		// parse all attribute names
		List<AttributeName> ans = new ArrayList<AttributeName>();
		for (String id : args.getObjectValueList(ATTRIBUTE_NAMES, String.class)) {
			AttributeName an = graph.getMetaData().getAttributeName(id);
			if (an == null)
				fireEventOccurred(new WrongParameterEvent(
						"AttributeName with id " + id + " not found",
						"[Export - start]"));
			else {
				fireEventOccurred(new GeneralOutputEvent(
						"Adding AttributeName " + an, "[Export - start]"));
				ans.add(an);
			}
		}
		fireEventOccurred(new GeneralOutputEvent("Using AttributeNames " + ans,
				"[Export - start]"));

		// parse all data sources
		List<DataSource> dss = new ArrayList<DataSource>();
		for (String id : args.getObjectValueList(ACCESSION_DATASOURCES,
				String.class)) {
			DataSource ds = graph.getMetaData().getDataSource(id);
			if (ds == null)
				fireEventOccurred(new WrongParameterEvent("DataSource with id "
						+ id + " not found", "[Export - start]"));
			else {
				fireEventOccurred(new GeneralOutputEvent("Adding DataSource "
						+ ds, "[Export - start]"));
				dss.add(ds);
			}
		}
		fireEventOccurred(new GeneralOutputEvent("Using DataSources " + dss,
				"[Export - start]"));

		// all connected components in graph
		List<Set<ONDEXConcept>> clusters = findClusters();
		fireEventOccurred(new GeneralOutputEvent("Found " + clusters.size()
				+ " clusters.", "[Export - start]"));

		// export file
		File file = new File(
				(String) args
						.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		// convert list of clusters into array
		Set<?>[] sortedBySize = clusters.toArray(new Set[0]);

		// sort clusters by their size
		Arrays.sort(sortedBySize, new Comparator<Set<?>>() {

			@Override
			public int compare(Set<?> o1, Set<?> o2) {
				return o1.size() - o2.size();
			}
		});

		// count cluster sizes
		Map<Integer, Integer> clusterSizes = new HashMap<Integer, Integer>();
		for (Set<?> set : sortedBySize) {
			if (!clusterSizes.containsKey(set.size()))
				clusterSizes.put(set.size(), 1);
			else
				clusterSizes.put(set.size(), clusterSizes.get(set.size()) + 1);
		}

		// write some statistics
		writer.write("Statistics:\n");
		writer.write("Number of clusters\t" + sortedBySize.length + "\n");
		Integer[] keys = clusterSizes.keySet().toArray(new Integer[0]);
		Arrays.sort(keys);
		for (Integer i : keys) {
			writer.write("Clusters of size\t" + i + "\t" + clusterSizes.get(i)
					+ "\n");
		}
		writer.write("\n");

		// write header
		writer.write("NAME\t");
		for (DataSource ds : dss) {
			writer.write(ds + "\t");
		}
		for (AttributeName an : ans) {
			writer.write(an + "\t");
		}
		writer.write("\n\n");

		// output every cluster
		for (Set<?> set : sortedBySize) {

			// sorted by data source of concepts
			ONDEXConcept[] sortedByDataSource = set
					.toArray(new ONDEXConcept[0]);
			Arrays.sort(sortedByDataSource, new Comparator<ONDEXConcept>() {

				@Override
				public int compare(ONDEXConcept o1, ONDEXConcept o2) {
					return o2.getElementOf().compareTo(o1.getElementOf());
				}
			});

			// one concept per line
			for (ONDEXConcept c : sortedByDataSource) {

				// first column is preferred name
				ConceptName cn = c.getConceptName();
				if (cn != null)
					writer.write(cn.getName() + "\t");
				else
					writer.write(c.getId() + "\t");

				// write accessions
				for (DataSource ds : dss) {
					// collect all accessions of current data source
					List<String> accs = new ArrayList<String>();
					for (ConceptAccession ca : c.getConceptAccessions()) {
						if (ca.getElementOf().equals(ds)) {
							accs.add(ca.getAccession());
						}
					}
					// write as toString representation
					for (int i = 0; i < accs.size(); i++) {
						writer.write(accs.get(i));
						if (accs.size() > 1 && i < accs.size() - 1)
							writer.write(",");
					}
					writer.write("\t");
				}

				// write attributes
				for (AttributeName an : ans) {
					// check if attribute exists
					Attribute attr = c.getAttribute(an);
					if (attr != null) {
						// write as toString representation
						writer.write(attr.getValue().toString());
					}
					writer.write("\t");
				}

				// next concept on new line
				writer.write("\n");
			}

			// new line between each cluster
			writer.write("\n");
		}

		// flush output file
		writer.flush();
		writer.close();
	}

	private List<Set<ONDEXConcept>> findClusters() {

		// contains all found connected components
		List<Set<ONDEXConcept>> clusters = new ArrayList<Set<ONDEXConcept>>();

		// all concept which already have been assigned to a cluster
		Set<ONDEXConcept> visited = new HashSet<ONDEXConcept>();

		// process all concepts in graph
		for (ONDEXConcept c : graph.getConcepts()) {

			// only look at concepts not yet in a cluster
			if (!visited.contains(c)) {

				// new current cluster
				Set<ONDEXConcept> cluster = new HashSet<ONDEXConcept>();
				clusters.add(cluster);

				// breadth first search for all neighbours of c
				Queue<ONDEXConcept> stack = new LinkedList<ONDEXConcept>();
				stack.add(c); // add root

				// perform search
				while (!stack.isEmpty()) {

					// add current concept to cluster and mark as visited
					ONDEXConcept current = stack.poll();
					cluster.add(current);
					visited.add(current);

					// process all relations of concept
					for (ONDEXRelation r : graph.getRelationsOfConcept(current)) {

						// ignore self-loops
						if (r.getFromConcept().equals(r.getToConcept()))
							continue;

						if (r.getFromConcept().equals(current)) {
							// TO concept is neighbour
							ONDEXConcept neighbour = r.getToConcept();
							// continue search from here
							if (!visited.contains(neighbour))
								stack.add(neighbour);
						}

						else {
							// FROM concept is neighbour
							ONDEXConcept neighbour = r.getFromConcept();
							// continue search from here
							if (!visited.contains(neighbour))
								stack.add(neighbour);
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
