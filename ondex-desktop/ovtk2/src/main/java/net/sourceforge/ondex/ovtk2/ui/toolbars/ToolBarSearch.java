package net.sourceforge.ondex.ovtk2.ui.toolbars;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.IdLabel;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

/**
 * Search the graph for a search term
 * 
 * @author taubertj
 * 
 */
public class ToolBarSearch implements Monitorable {

	/**
	 * Emulates behaviour of MatchResult for normal String match.
	 * 
	 * @author taubertj
	 */
	private class Result implements MatchResult {

		private final String query;

		private final String target;

		public Result(String query, String target) {
			this.query = query;
			this.target = target;
		}

		@Override
		public int end() {
			return target.indexOf(query) + query.length();
		}

		@Override
		public int end(int group) {
			if (group == 0)
				return end();
			return 0;
		}

		@Override
		public String group() {
			return target.substring(start(), end());
		}

		@Override
		public String group(int group) {
			if (group == 0)
				return group();
			return null;
		}

		@Override
		public int groupCount() {
			return 1;
		}

		@Override
		public int start() {
			return target.indexOf(query);
		}

		@Override
		public int start(int group) {
			if (group == 0)
				return start();
			return 0;
		}

	}

	/**
	 * Current progress
	 */
	private int progress = 0;

	/**
	 * Max progress
	 */
	private int progressMax = 1;

	/**
	 * Current progress message / state
	 */
	private String state = Monitorable.STATE_IDLE;

	/**
	 * Viewer which holds Ondex graph
	 */
	private OVTK2PropertiesAggregator viewer;

	/**
	 * Search term
	 */
	private String search;

	/**
	 * Process is cancelled in-between
	 */
	private boolean cancelled = false;

	/**
	 * regex search option
	 */
	private boolean isRegex = false;

	/**
	 * case sensitive search option
	 */
	private boolean isCaseSensitive = false;

	/**
	 * concept class restriction
	 */
	private ConceptClass conceptClass = null;

	/**
	 * data source restriction
	 */
	private DataSource dataSource = null;

	/**
	 * context restriction
	 */
	private ONDEXConcept context = null;

	/**
	 * Setup the search for a given viewer and search term.
	 * 
	 * @param viewer
	 * @param search
	 * @param isRegex
	 * @param isCaseSensitive
	 * @param restrictConceptClass
	 * @param restrictDataSource
	 * @param restrictContext
	 */
	public ToolBarSearch(OVTK2PropertiesAggregator viewer, String search, boolean isRegex, boolean isCaseSensitive, ConceptClass restrictConceptClass, DataSource restrictDataSource, ONDEXConcept restrictContext) {
		this.viewer = viewer;
		this.search = search;
		this.isRegex = isRegex;
		this.isCaseSensitive = isCaseSensitive;
		this.conceptClass = restrictConceptClass;
		this.dataSource = restrictDataSource;
		this.context = restrictContext;
	}

	/**
	 * Highlights the matching parts in a target string.
	 * 
	 * @param mr
	 *            MatchResults with positions
	 * @param target
	 *            target String to modify
	 * @return modified String
	 */
	private String formatMatch(MatchResult mr, String target) {
		StringBuffer result = new StringBuffer();
		result.append("<html>");
		int lastEnd = 0;
		// this is a hack for MatchResult as it returns 0 as group count:
		// Group zero denotes the entire pattern by convention. It is not
		// included in this count.
		int count = mr.groupCount();
		if (count == 0)
			count = 1;
		for (int group = 0; group < count; group++) {
			try {
				result.append(target.substring(lastEnd, mr.start(group)));
				result.append("<b>");
				result.append(target.substring(mr.start(group), mr.end(group)));
				result.append("</b>");
				lastEnd = mr.end(group);
				// reached end, append rest of target
				if (group == count - 1) {
					result.append(target.substring(mr.end(group), target.length()));
				}
			} catch (IndexOutOfBoundsException e) {
				throw (IndexOutOfBoundsException) new IndexOutOfBoundsException("Problem formatting match with result: " + mr + " on target: " + target).initCause(e);
			}
		}
		result.append("</html>");
		return result.toString();
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

	/**
	 * Tests if there is a match of this query within the search string (looks
	 * if case sensitive or not)
	 * 
	 * @param p
	 *            the pattern (if null then literal match .contains is used)
	 * @param query
	 *            the query to match
	 * @param target
	 *            the target to match to
	 * @return is there a match
	 */
	private MatchResult isMatching(Pattern p, String query, String target) {
		if (p != null) {
			// Matcher m = p.matcher(target); //.matches requires the matching
			// of the whole word this is too restrictive
			Matcher m = p.matcher(target);
			boolean found = m.find();
			if (found) {
				return m.toMatchResult();
			} else {
				return null;
			}
		} else {
			String q = query;
			String t = target;
			if (!isCaseSensitive) {
				q = q.toUpperCase();
				t = t.toUpperCase();
			}
			int index = t.indexOf(q);
			// NOTE: hack to fix bizarre race condition on OS X
			if (index > -1 && index < t.length()) {
				return new Result(q, t);
			} else {
				return null;
			}
		}
	}

	/**
	 * Straight forward in-memory search implementation.
	 * 
	 * @param viewer
	 *            viewer to search in
	 * @param search
	 *            <code>String</code> to search for
	 */
	public Vector<Vector<Object>> search() {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();

		Pattern p = null;
		if (isRegex) { // case insensitive pattern matching
			p = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
		}
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();

		// keep track of matching parts
		Map<IdLabel, List<String>> matches = new Hashtable<IdLabel, List<String>>();

		// additional information per concept
		Map<IdLabel, String> infos = new Hashtable<IdLabel, String>();

		// view containing restrictions
		Set<ONDEXConcept> view = BitSetFunctions.copy(graph.getConcepts());

		// concept class restriction
		if (conceptClass != null)
			view.retainAll(graph.getConceptsOfConceptClass(conceptClass));

		// data source restriction
		if (dataSource != null)
			view.retainAll(graph.getConceptsOfDataSource(dataSource));

		// context restriction
		if (context != null)
			view.retainAll(graph.getConceptsOfTag(context));

		// update progress bar
		progressMax = view.size() + 1;

		state = "Search in concepts...";

		// iterate over all concepts
		for (ONDEXConcept ac : view) {

			// search was cancelled
			if (cancelled)
				break;

			// track matching part
			List<String> match = new ArrayList<String>();

			// search concept PID
			String pid = ac.getPID();
			MatchResult mr = isMatching(p, search, pid);
			if (mr != null)
				match.add(formatMatch(mr, pid) + " [Parser ID]");

			// search in annotation
			String anno = ac.getAnnotation();
			mr = isMatching(p, search, anno);
			if (mr != null)
				match.add(formatMatch(mr, anno) + " [Annotation]");

			// search in description
			String desc = ac.getDescription();
			mr = isMatching(p, search, desc);
			if (mr != null)
				match.add(formatMatch(mr, desc) + " [Description]");

			// search in concept names
			for (ConceptName con : ac.getConceptNames()) {
				String cn = con.getName();
				mr = isMatching(p, search, cn);
				if (mr != null)
					match.add(formatMatch(mr, cn) + " [ConceptName]");
			}

			// search in concept accessions
			for (ConceptAccession ca : ac.getConceptAccessions()) {
				String accession = ca.getAccession();
				String cv = ca.getElementOf().getId();

				mr = isMatching(p, search, accession);
				if (mr != null)
					match.add(formatMatch(mr, accession) + " (" + cv + ") [ConceptAccession]");
			}

			// search in Attribute
			for (Attribute attribute : ac.getAttributes()) {
				Class<?> c = attribute.getOfType().getDataType();
				if (String.class.isAssignableFrom(c) || Number.class.isAssignableFrom(c)) {
					mr = isMatching(p, search, attribute.getValue().toString());
					if (mr != null)
						match.add(formatMatch(mr, attribute.getValue().toString()) + " (" + attribute.getOfType().getId() + ") [ConceptAttribute]");
				}
			}

			// something was found
			if (match.size() > 0) {
				// indexing key
				String name = String.valueOf(ac.getId());
				if (ac.getConceptName() != null)
					name = ac.getConceptName().getName();
				IdLabel label = new IdLabel(ac.getId(), name);
				matches.put(label, match);
				infos.put(label, ac.getOfType() + " [" + ac.getElementOf() + "]");
			}

			// update progress bar
			progress++;
		}

		state = "Post-processing matches...";

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

	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
	}

}
