package net.sourceforge.ondex.ovtk2.ui.toolbars;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.IdLabel;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;
import net.sourceforge.ondex.tools.chemical.ChEMBLWrapper;
import net.sourceforge.ondex.tools.data.ChemicalStructure;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

/**
 * Search the graph for a search term
 * 
 * @author taubertj
 * 
 */
public class ChemicalSearch implements Monitorable {

	/**
	 * Process is cancelled in-between
	 */
	private boolean cancelled = false;

	/**
	 * concept class restriction
	 */
	private ConceptClass conceptClass = null;

	/**
	 * context restriction
	 */
	private ONDEXConcept context = null;

	/**
	 * tanimoto cutoff for search
	 */
	private double cutoff = 0.7;

	/**
	 * data source restriction
	 */
	private DataSource dataSource = null;

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
	 * Search mode SMILES or InChI
	 */
	private String mode = null;

	/**
	 * format tanimoto score
	 */
	private NumberFormat nf;

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
	 * Query ChEMBL webservice
	 */
	private boolean useChEMBL = false;

	/**
	 * Viewer which holds Ondex graph
	 */
	private OVTK2PropertiesAggregator viewer;

	/**
	 * Setup the search for a given viewer and search term.
	 * 
	 * @param viewer
	 * @param search
	 * @param restrictConceptClass
	 * @param restrictDataSource
	 * @param restrictContext
	 * @param searchMode
	 * @param percentSimilarity
	 * @param useChEMBL
	 */
	public ChemicalSearch(OVTK2PropertiesAggregator viewer, String search, ConceptClass restrictConceptClass, DataSource restrictDataSource, ONDEXConcept restrictContext, String searchMode, int percentSimilarity, boolean useChEMBL) {
		this.viewer = viewer;
		this.search = search;
		this.conceptClass = restrictConceptClass;
		this.dataSource = restrictDataSource;
		this.context = restrictContext;
		this.mode = searchMode;
		this.cutoff = (double) percentSimilarity / 100.0;
		this.useChEMBL = useChEMBL;

		graph = viewer.getONDEXJUNGGraph();

		// keep track of matching parts
		matches = new Hashtable<IdLabel, List<String>>();

		// additional information per concept
		infos = new Hashtable<IdLabel, String>();

		// nice formatting of scores
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumIntegerDigits(1);
		nf.setMaximumIntegerDigits(1);
	}

	/**
	 * Parses compound concepts from given ChEMBL URL
	 * 
	 * @param url
	 * @param s
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private int getCompounds(URL url, String s) throws IOException, ParserConfigurationException, SAXException {

		Set<ONDEXConcept> created = new HashSet<ONDEXConcept>();

		// utility class providing XML parsing
		ChEMBLWrapper wrapper = new ChEMBLWrapper(graph);
		AttributeName anChemicalStructure = graph.getMetaData().getAttributeName("ChemicalStructure");

		// open http connection
		HttpURLConnection uc = (HttpURLConnection) url.openConnection();

		// check for response code
		int code = uc.getResponseCode();

		if (code != 200) {
			// in the event of error
			String response = uc.getResponseMessage();
			System.err.println("HTTP/1.x " + code + " " + response);
		} else {

			// get main content
			InputStream in = new BufferedInputStream(uc.getInputStream());

			// parse XML content
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in);
			doc.getDocumentElement().normalize();

			// get all compound elements
			NodeList nList = doc.getElementsByTagName("compound");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					ONDEXConcept ac = wrapper.parseCompound(eElement, null);
					created.add(ac);

					ChemicalStructure cs = (ChemicalStructure) ac.getAttribute(anChemicalStructure).getValue();

					// track matching part
					List<String> match = new ArrayList<String>();
					if (ChEMBLWrapper.getTagValue("similarity", eElement) != null) {
						double tanimoto = Double.valueOf(ChEMBLWrapper.getTagValue("similarity", eElement)) / 100.0;
						match.add(cs.getSMILES() + " [" + nf.format(tanimoto) + "]");
					} else {
						if (s != null)
							match.add(s);
						else
							match.add(cs.getSMILES());
					}

					String name = String.valueOf(ac.getId());
					if (ac.getConceptName() != null)
						name = ac.getConceptName().getName();
					IdLabel label = new IdLabel(ac.getId(), name);

					matches.put(label, match);
					infos.put(label, ac.getOfType() + " [" + ac.getElementOf() + "]");
				}
			}
			System.out.println("Created " + created.size() + " concepts");

			// make new concepts visible
			viewer.getONDEXJUNGGraph().setVisibility(created, true);

			// layout nodes on big circle
			LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(), null, created);

			if (viewer.getMetaGraph() != null)
				viewer.getMetaGraph().updateMetaData();
		}

		return created.size();
	}

	/**
	 * Parses target concepts from given ChEMBL URL
	 * 
	 * @param url
	 * @param s
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private int getTargets(URL url, String s) throws IOException, ParserConfigurationException, SAXException {

		Set<ONDEXConcept> created = new HashSet<ONDEXConcept>();

		// utility class providing XML parsing
		ChEMBLWrapper wrapper = new ChEMBLWrapper(graph);

		// open http connection
		HttpURLConnection uc = (HttpURLConnection) url.openConnection();

		// check for response code
		int code = uc.getResponseCode();

		if (code != 200) {
			// in the event of error
			String response = uc.getResponseMessage();
			System.err.println("HTTP/1.x " + code + " " + response);
		} else {

			// get main content
			InputStream in = new BufferedInputStream(uc.getInputStream());

			// parse XML content
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in);
			doc.getDocumentElement().normalize();

			// get all compound elements
			NodeList nList = doc.getElementsByTagName("target");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					ONDEXConcept ac = wrapper.parseTarget(eElement, null);
					created.add(ac);

					// track matching part
					List<String> match = new ArrayList<String>();
					if (s != null)
						match.add(s);

					String name = String.valueOf(ac.getId());
					if (ac.getConceptName() != null)
						name = ac.getConceptName().getName();
					IdLabel label = new IdLabel(ac.getId(), name);

					matches.put(label, match);
					infos.put(label, ac.getOfType() + " [" + ac.getElementOf() + "]");
				}
			}
			System.out.println("Created " + created.size() + " concepts");

			// make new concepts visible
			viewer.getONDEXJUNGGraph().setVisibility(created, true);

			// layout nodes on big circle
			LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(), null, created);

			if (viewer.getMetaGraph() != null)
				viewer.getMetaGraph().updateMetaData();
		}

		return created.size();
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
	 * @throws CDKException
	 */
	public Vector<Vector<Object>> search() throws Exception {

		// search for ChEMBL compounds by similarity
		if (useChEMBL && mode.equals(Config.language.get("ToolBar.Search.Mode.SMILES"))) {

			state = "Searching webservice...";

			progress = 0;

			progressMax = 1;

			// build query URL
			URL url = new URL("https://www.ebi.ac.uk/chemblws/compounds/similarity/" + search.trim() + "/" + Math.round(this.cutoff * 100));
			System.out.println(url);

			getCompounds(url, null);

			return processMatches();
		}

		// search for ChEMBL compound by StdInChiKey
		else if (mode.equals(Config.language.get("ToolBar.Search.Mode.InChIKey"))) {

			state = "Searching webservice...";

			progress = 0;

			progressMax = 1;

			for (String s : search.trim().split("\\|")) {

				// build query URL
				URL url = new URL("https://www.ebi.ac.uk/chemblws/compounds/stdinchikey/" + s.trim());
				System.out.println(url);

				getCompounds(url, s.trim());
			}

			return processMatches();
		}

		// search for ChEMBL compound by ID
		else if (mode.equals(Config.language.get("ToolBar.Search.Mode.ChEMBL"))) {

			state = "Searching webservice...";

			progress = 0;

			progressMax = 1;

			for (String s : search.trim().split("\\|")) {

				// build query URL
				URL url = new URL("https://www.ebi.ac.uk/chemblws/compounds/" + s.trim());
				System.out.println(url);

				int nb = getCompounds(url, s.trim());

				// no compounds found, try target next
				if (nb == 0) {
					url = new URL("https://www.ebi.ac.uk/chemblws/targets/" + s.trim());
					System.out.println(url);

					getTargets(url, s.trim());
				}
			}

			return processMatches();
		}
		else throw new IllegalArgumentException ( String.format ( 
			"Internal Error: invalid mode parameter \"%s\" sent to ChemicalSerch", mode 
		));
	}

	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
	}

}
