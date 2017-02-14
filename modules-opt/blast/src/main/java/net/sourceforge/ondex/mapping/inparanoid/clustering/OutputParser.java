package net.sourceforge.ondex.mapping.inparanoid.clustering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.ondex.core.ONDEXGraph;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

/*
 * Created on 29.05.2005
 *
 */

/**
 * Parses blast like output format. Checks for overlap and minimum bit-score.
 * 
 * @author Jan
 * @version 28.02.2008
 */
public class OutputParser extends Thread {

	// global vars
	private int cutoff = 30;

	private double overlap = 0.5;

	private Vector<OndexMatch> matches = new Vector<OndexMatch>();

	// query properties and states
	private String query = null;

	private String queryTaxid = null;

	private int lengthOfQuery = 0;

	private boolean startOfQuery = false;

	// match properties and states
	private String target = null;

	private String targetTaxid = null;

	private int lengthOfTarget = 0;

	private boolean inMatch = false;

	private double matchScore = 0;

	// private double matchEvalue = 0;
	private int matchIdentities = 0;

	private int matchPositives = 0;

	private int matchGaps = 0;

	private int matchLength = 0;

	// lines for more then one HSP
	private boolean inHSP = false;

	private boolean firstHSPQuery = false;

	private boolean firstHSPSbjct = false;

	private int n;

	private Map<Integer, Integer> qstart = LazyMap.decorate(
			new HashMap<Integer, Integer>(), new Factory<Integer>() {

				@Override
				public Integer create() {
					return Integer.valueOf(0);
				}
			});

	private Map<Integer, Integer> qend = LazyMap.decorate(
			new HashMap<Integer, Integer>(), new Factory<Integer>() {

				@Override
				public Integer create() {
					return Integer.valueOf(0);
				}
			});

	private Map<Integer, Integer> mstart = LazyMap.decorate(
			new HashMap<Integer, Integer>(), new Factory<Integer>() {

				@Override
				public Integer create() {
					return Integer.valueOf(0);
				}
			});

	private Map<Integer, Integer> mend = LazyMap.decorate(
			new HashMap<Integer, Integer>(), new Factory<Integer>() {

				@Override
				public Integer create() {
					return Integer.valueOf(0);
				}
			});

	private Map<Integer, Double> hspScore = LazyMap.decorate(
			new HashMap<Integer, Double>(), new Factory<Double>() {

				@Override
				public Double create() {
					return Double.valueOf(0);
				}
			});

	private Map<Integer, Integer> hspIdentities = LazyMap.decorate(
			new HashMap<Integer, Integer>(), new Factory<Integer>() {

				@Override
				public Integer create() {
					return Integer.valueOf(0);
				}
			});

	private Map<Integer, Integer> hspPositives = LazyMap.decorate(
			new HashMap<Integer, Integer>(), new Factory<Integer>() {

				@Override
				public Integer create() {
					return Integer.valueOf(0);
				}
			});

	private Map<Integer, Integer> hspGaps = LazyMap.decorate(
			new HashMap<Integer, Integer>(), new Factory<Integer>() {

				@Override
				public Integer create() {
					return Integer.valueOf(0);
				}
			});

	private Map<Integer, Integer> hspLength = LazyMap.decorate(
			new HashMap<Integer, Integer>(), new Factory<Integer>() {

				@Override
				public Integer create() {
					return Integer.valueOf(0);
				}
			});

	private InputStream in;

	private ONDEXGraph aog;

	/**
	 * Constructor for thread.
	 * 
	 * @param graph
	 *            AbstractONDEXGraph
	 * @param in
	 *            reader for blast like output file
	 * @param cutoff
	 *            bit score cutoff
	 * @param overlap
	 *            matching region should cover at least of longer seq
	 */
	public OutputParser(ONDEXGraph graph, InputStream in, int c, double o) {
		aog = graph;
		cutoff = c;
		overlap = o;
		this.in = in;
	}

	/**
	 * Returns list of OndexMatch parsed.
	 * 
	 * @return List<OndexMatch>
	 */
	public List<OndexMatch> getMatches() {
		return matches;
	}

	/**
	 * Parses blast like output of a given file
	 * 
	 */
	public void run() {

		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strline = br.readLine();
			while ((strline = br.readLine()) != null) {
				String line = strline.trim().replaceAll(",", "");

				if (line.startsWith("Query= ")) {
					// begin of new query
					startOfQuery = true;
					inMatch = false;

					// write up previous match
					if (target != null) {

						buildMatch();

						target = null;
					}

					// get query name
					query = line
							.substring(line.indexOf(" ") + 1, line.length());

					// split query name into 3 components
					String[] result = query.split("\\|");
					query = result[0]; // OndexID
					queryTaxid = result[3]; // TaxID
					// System.out.println("Query: " + query + " " + queryTaxid);
				}

				else if (startOfQuery) {
					if (line.startsWith("Length=")) {
						startOfQuery = false;

						// get length from line after query line
						line = line.substring(line.indexOf("=") + 1,
								line.length());
						lengthOfQuery = Integer.parseInt(line);
						// System.out.println("Length of query: " +
						// lengthOfQuery);
					}
				}

				else if (line.startsWith(">")) {

					// start a new match
					inMatch = true;
					inHSP = false;

					// write up previous match
					if (target != null) {

						buildMatch();

						target = null;

					}

					// get name of match
					target = line.substring(1, line.length()).trim();

					// split match name into 3 components
					String[] result = target.split("\\|");
					target = result[0]; // OndexID
					targetTaxid = result[3]; // TaxID
					// System.out.println("Target: " + target + " " +
					// targetTaxid);
				}

				else if (inMatch && line.indexOf("Length=") > -1) {
					// get length for match
					line = line.substring(line.indexOf("=") + 1, line.length());
					lengthOfTarget = Integer.parseInt(line);
					// System.out.println("Length of target: " +
					// lengthOfTarget);
				}

				else if (inMatch && line.indexOf("Score = ") > -1) {
					// get bit score
					String score = line.substring(line.indexOf("Score") + 8,
							line.indexOf("bits") - 1);
					matchScore = Double.parseDouble(score);
					// System.out.println("Score: " + matchScore);
				}

				else if (inMatch && line.indexOf("Identities = ") > -1) {

					// get identities
					matchIdentities = Integer.parseInt(line.substring(
							line.indexOf("=") + 2, line.indexOf("/")));
					int total = Integer.parseInt(line.substring(
							line.indexOf("/") + 1, line.indexOf("(") - 1));

					// get match length
					matchLength = total;
					if (line.indexOf("Positives = ") > -1) {
						line = line.substring(line.indexOf("Positives"),
								line.length());
						// get positives
						matchPositives = Integer.parseInt(line.substring(
								line.indexOf("=") + 2, line.indexOf("/")));

					}
					if (line.indexOf("Gaps = ") > -1) {
						line = line.substring(line.indexOf("Gaps"),
								line.length());
						// get caps
						matchGaps = Integer.parseInt(line.substring(
								line.indexOf("=") + 2, line.indexOf("/")));
					}

					inMatch = false;
					inHSP = true;
					firstHSPQuery = firstHSPSbjct = true;

					// clear previous stored values
					hspScore.clear();
					hspIdentities.clear();
					hspPositives.clear();
					hspGaps.clear();
					hspLength.clear();

					// set values for first HSP
					n = 0;
					hspScore.put(n, matchScore);
					hspIdentities.put(n, matchIdentities);
					hspPositives.put(n, matchPositives);
					hspGaps.put(n, matchGaps);
					hspLength.put(n, matchLength);
					// System.out.println(matchScore + " " + matchIdentities +
					// " "
					// + matchPositives + " " + matchGaps + " "
					// + matchLength);
				}

				// get start and end of query on first alignment
				else if (inHSP && firstHSPQuery && line.startsWith("Query: ")) {
					line = line.substring(line.indexOf(" ") + 1, line.length())
							.trim();
					qstart.put(
							n,
							Integer.parseInt(line.substring(0,
									line.indexOf(" "))));
					qend.put(
							n,
							Integer.parseInt(line.substring(
									line.lastIndexOf(" ") + 1, line.length())));
					firstHSPQuery = false;
				}

				// get start and end of match on first alignment
				else if (inHSP && firstHSPSbjct && line.startsWith("Sbjct: ")) {
					line = line.substring(line.indexOf(" ") + 1, line.length())
							.trim();
					mstart.put(
							n,
							Integer.parseInt(line.substring(0,
									line.indexOf(" "))));
					mend.put(
							n,
							Integer.parseInt(line.substring(
									line.lastIndexOf(" ") + 1, line.length())));
					firstHSPSbjct = false;
				}

				// get last end for query
				else if (inHSP && !firstHSPQuery && line.startsWith("Query: ")) {
					qend.put(
							n,
							Integer.parseInt(line.substring(
									line.lastIndexOf(" ") + 1, line.length())));
				}

				// get last end for match
				else if (inHSP && !firstHSPSbjct && line.startsWith("Sbjct: ")) {
					mend.put(
							n,
							Integer.parseInt(line.substring(
									line.lastIndexOf(" ") + 1, line.length())));
				}

				// this happens if we have more than one sequence reported
				else if (inHSP && line.indexOf("Score = ") > -1) {

					firstHSPQuery = firstHSPSbjct = true;

					n++; // next sequence number

					// get bit score
					String score = line.substring(line.indexOf("Score") + 8,
							line.indexOf("bits") - 1);
					hspScore.put(n, Double.parseDouble(score));

				}

				else if (inHSP && line.indexOf("Identities = ") > -1) {

					// get identities
					hspIdentities.put(n, Integer.parseInt(line.substring(
							line.indexOf("=") + 2, line.indexOf("/"))));

					// get match length
					hspLength.put(n, Integer.parseInt(line.substring(
							line.indexOf("/") + 1, line.indexOf("(") - 1)));

					if (line.indexOf("Positives = ") > -1) {
						line = line.substring(line.indexOf("Positives"),
								line.length());
						// get positives
						hspPositives.put(
								n,
								Integer.parseInt(line.substring(
										line.indexOf("=") + 2,
										line.indexOf("/"))));
					}

					if (line.indexOf("Gaps = ") > -1) {
						line = line.substring(line.indexOf("Gaps"),
								line.length());
						// get caps
						hspGaps.put(
								n,
								Integer.parseInt(line.substring(
										line.indexOf("=") + 2,
										line.indexOf("/"))));
					}
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// write up previous match
		if (target != null) {

			buildMatch();

			target = null;

		}

	}

	/**
	 * Validates a HSP for overlap with other HSPs.
	 * 
	 */
	private void validateHSP() {
		// init array which tell accept of hsp
		boolean[] accepted = new boolean[n + 1];
		for (int i = 0; i < n + 1; i++) {
			accepted[i] = true;
		}

		for (int i = 0; i < n + 1; i++) {
			// Decide if we want to accept this HSP
			// Check for overlap

			for (int c = 0; c < i; c++) {
				if (accepted[c]) {
					int qstart1, qend1, mstart1, mend1, qstart2, qend2, mstart2, mend2;
					// Which HSP is N-terminal?
					if (qstart.get(i) < qstart.get(c)) {
						qstart1 = qstart.get(i);
						qend1 = qend.get(i);
						mstart1 = mstart.get(i);
						mend1 = mend.get(i);
						qstart2 = qstart.get(c);
						qend2 = qend.get(c);
						mstart2 = mstart.get(c);
						mend2 = mend.get(c);
					} else {
						qstart2 = qstart.get(i);
						qend2 = qend.get(i);
						mstart2 = mstart.get(i);
						mend2 = mend.get(i);
						qstart1 = qstart.get(c);
						qend1 = qend.get(c);
						mstart1 = mstart.get(c);
						mend1 = mend.get(c);
					}

					int qlength1 = qend1 - qstart1;
					int qlength2 = qend2 - qstart2;
					int qlength = (qlength1 < qlength2) ? qlength1 : qlength2;

					int mlength1 = mend1 - mstart1;
					int mlength2 = mend2 - mstart2;
					int mlength = (mlength1 < mlength2) ? mlength1 : mlength2;

					int qoverlap = qstart2 - qend1;
					int moverlap = mstart2 - mend1;

					if ((qlength * qlength) == 0) { // Zero length - why?
						accepted[i] = false; // Discard this HSP
					}

					// 5% overlap is allowed:
					if (((double) qoverlap / (double) qlength < -0.05)
							|| ((double) moverlap / (double) mlength < -0.05)) {
						accepted[i] = false;
					}
				}
			}
		}

		matchScore = 0;
		matchLength = 0;
		matchIdentities = 0;
		matchPositives = 0;
		matchGaps = 0;

		for (int i = 0; i < n + 1; i++) {
			if (accepted[i]) {
				matchScore += hspScore.get(i);
				matchLength += hspLength.get(i);
				matchIdentities += hspIdentities.get(i);
				matchPositives += hspPositives.get(i);
				matchGaps += hspGaps.get(i);
			}
		}
	}

	/**
	 * Finally adds a match to the result list.
	 * 
	 */
	private void buildMatch() {
		if (n > 0)
			validateHSP();

		// adapt match length
		matchLength -= matchGaps;

		if (!(matchScore < cutoff)) {
			OndexMatch pair = new OndexMatch();
			pair.setQuery(aog.getConcept(Integer.parseInt(query)));
			pair.setTarget(aog.getConcept(Integer.parseInt(target)));
			pair.setQueryTaxId(queryTaxid);
			pair.setTargetTaxId(targetTaxid);
			pair.setScore(matchScore);
			// System.out.println("Evaluating pair: " + pair);
			// Ignore if matching area on query sequence covers
			// less than 50% of longer sequence.
			if (lengthOfQuery > lengthOfTarget) {
				if (!((double) matchLength / (double) lengthOfQuery < overlap))
					matches.add(pair);
			} else {
				if (!((double) matchLength / (double) lengthOfTarget < overlap))
					matches.add(pair);
			}
		}

		// reset all match variables
		matchScore = 0;
		matchLength = 0;
		matchGaps = 0;
		matchIdentities = 0;
		matchPositives = 0;
		lengthOfTarget = 0;
	}
}
