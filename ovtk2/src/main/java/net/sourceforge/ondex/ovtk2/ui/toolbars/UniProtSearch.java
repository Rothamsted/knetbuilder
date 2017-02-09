package net.sourceforge.ondex.ovtk2.ui.toolbars;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.IdLabel;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;
import net.sourceforge.ondex.parser.uniprot.ArgumentNames;
import net.sourceforge.ondex.parser.uniprot.sink.Protein;
import net.sourceforge.ondex.parser.uniprot.transformer.Transformer;
import net.sourceforge.ondex.parser.uniprot.xml.ComponentParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.AccessionBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.CommentBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.DbReferenceBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.EntryStartParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.GeneBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.ProteinNameBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.PublicationBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.SequenceBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.component.TaxonomieBlockParser;
import net.sourceforge.ondex.parser.uniprot.xml.filter.FilterEnum;
import net.sourceforge.ondex.parser.uniprot.xml.filter.ValueFilter;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

import com.ctc.wstx.stax.WstxInputFactory;

/**
 * Search the graph for a search term
 * 
 * @author taubertj
 * 
 */
public class UniProtSearch implements Monitorable {

	/**
	 * Process is cancelled in-between
	 */
	private boolean cancelled = false;

	/**
	 * current Ondex graph
	 */
	private ONDEXGraph graph;

	/**
	 * match description part
	 */
	private Map<IdLabel, String> infos;

	/**
	 * all matches
	 */
	private Map<IdLabel, List<String>> matches;

	/**
	 * Current progress
	 */
	private int progress = 0;

	/**
	 * Max progress
	 */
	private int progressMax = 1;

	/**
	 * Search term
	 */
	private String search;

	/**
	 * Current progress message / state
	 */
	private String state = Monitorable.STATE_IDLE;

	/**
	 * Viewer which holds Ondex graph
	 */
	private OVTK2PropertiesAggregator viewer;

	/**
	 * Setup the search for a given viewer and search term.
	 * 
	 * @param viewer
	 * @param search
	 * @param searchMode
	 */
	public UniProtSearch(OVTK2PropertiesAggregator viewer, String search) {
		this.viewer = viewer;
		this.search = search;

		graph = viewer.getONDEXJUNGGraph();

		// keep track of matching parts
		matches = new Hashtable<IdLabel, List<String>>();

		// additional information per concept
		infos = new Hashtable<IdLabel, String>();
	}

	@Override
	public int getMaxProgress() {
		return progressMax;
	}

	@Override
	public int getMinProgress() {
		// always 0 in this case
		return 0;
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public Throwable getUncaughtException() {
		// there shouldn't be any exceptions
		return null;
	}

	@Override
	public boolean isAbortable() {
		return true;
	}

	@Override
	public boolean isIndeterminate() {
		return false;
	}

	private Vector<Vector<Object>> processMatches() {
		state = "Post-processing matches...";

		Vector<Vector<Object>> result = new Vector<Vector<Object>>();

		// add in matches
		for (IdLabel label : matches.keySet()) {
			Vector<Object> row = new Vector<Object>();
			row.add(label);
			StringBuffer buf = new StringBuffer();
			for (String match : matches.get(label)) {
				buf.append(match);
				buf.append(", ");
			}
			buf.delete(buf.length() - 2, buf.length() - 1);
			row.add(buf.toString());
			row.add(infos.get(label));
			result.add(row);
		}

		// last step, now finish
		progress++;
		state = Monitorable.STATE_TERMINAL;

		return result;
	}

	/**
	 * Straight forward in-memory search implementation.
	 * 
	 * @param viewer
	 *            viewer to search in
	 * @param search
	 *            <code>String</code> to search for
	 * @throws Exception
	 */
	public Vector<Vector<Object>> search() throws Exception {

		state = "Searching webservice...";

		progress = 0;

		progressMax = 1;

		// Populate XML parsing delegates
		HashMap<String, ComponentParser> delegates = new HashMap<String, ComponentParser>();
		HashMap<FilterEnum, ValueFilter> filter = new HashMap<FilterEnum, ValueFilter>();
		delegates.put("dbReference", new DbReferenceBlockParser(filter.get(FilterEnum.DatabaseReferenceFilter)));
		delegates.put("entry", new EntryStartParser());
		delegates.put("comment", new CommentBlockParser());
		delegates.put("sequence", new SequenceBlockParser());
		delegates.put("accession", new AccessionBlockParser(filter.get(FilterEnum.DatabaseReferenceFilter)));// .AccessionFilter)));
		delegates.put("organism", new TaxonomieBlockParser(filter.get(FilterEnum.TaxonomieFilter)));
		delegates.put("reference", new PublicationBlockParser());
		delegates.put("protein", new ProteinNameBlockParser());
		delegates.put("gene", new GeneBlockParser());

		// setup UniProt parser arguments and transformer
		ONDEXPluginArguments args = new ONDEXPluginArguments(new ArgumentDefinition<?>[] { new BooleanArgumentDefinition(ArgumentNames.HIDE_LARGE_SCALE_PUBLICATIONS_ARG, ArgumentNames.HIDE_LARGE_SCALE_PUBLICATIONS_ARG_DESC, false, true) });
		args.addOption(ArgumentNames.HIDE_LARGE_SCALE_PUBLICATIONS_ARG, Boolean.TRUE);
		Transformer transformer = new Transformer(graph, args, false, null);

		// for xml parsing
		WstxInputFactory factory = (WstxInputFactory) WstxInputFactory.newInstance();
		factory.configureForSpeed();

		Set<ONDEXConcept> created = new HashSet<ONDEXConcept>();

		// multiple UniProt IDs can be supplied separated by |
		for (String s : search.trim().split("\\|")) {

			// UniProt URL
			URL url = new URL("http://www.uniprot.org/uniprot/" + s.trim() + ".xml");

			InputStream stream = url.openStream();

			XMLStreamReader staxXmlReader = (XMLStreamReader) factory.createXMLStreamReader(stream);

			while (staxXmlReader.hasNext()) {
				if (cancelled) {
					break;
				}

				int event = staxXmlReader.next();
				// parse one protein information and store in sink objects
				if (event == XMLStreamConstants.START_ELEMENT) {
					String element = staxXmlReader.getLocalName();

					if (delegates.containsKey(element)) {
						ComponentParser parser = (ComponentParser) delegates.get(element);
						parser.parseElement(staxXmlReader);
					}
				}
				// transform one protein into Ondex datastructure
				else if (event == XMLStreamConstants.END_ELEMENT) {
					String element = staxXmlReader.getLocalName();
					if (element.equalsIgnoreCase("entry")) {

						// get new UniProt protein
						ONDEXConcept ac = transformer.transform(Protein.getInstance());
						created.add(ac);

						// track matching part
						List<String> match = new ArrayList<String>();
						match.add(s.trim());

						String name = String.valueOf(ac.getId());
						if (ac.getConceptName() != null)
							name = ac.getConceptName().getName();
						IdLabel label = new IdLabel(ac.getId(), name);

						matches.put(label, match);
						infos.put(label, ac.getOfType() + " [" + ac.getElementOf() + "]");

						// create a new instance
						Protein.getInstance(true, false);
					}
				}
			}
			staxXmlReader.close();
		}

		System.out.println("Added " + created.size() + " new concepts.");

		// make new concepts visible
		viewer.getONDEXJUNGGraph().setVisibility(created, true);
		for (ONDEXConcept c : created) {
			// make all relations visible
			viewer.getONDEXJUNGGraph().setVisibility(graph.getRelationsOfConcept(c), true);
		}

		// layout nodes on big circle
		LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(), null, created);

		if (viewer.getMetaGraph() != null)
			viewer.getMetaGraph().updateMetaData();

		return processMatches();
	}

	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
	}

}
