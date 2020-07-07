package net.sourceforge.ondex.mapping.inparanoid.clustering;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.mapping.inparanoid.Mapping;

/**
 * Parses ortholog groups of proteins out of a given index structure.
 * 
 * @author taubertj
 * 
 */
public class OrthologParser {

	private static boolean DEBUG = Mapping.DEBUG;

	// cutoff used for filtering seqalign
	private int cutoff;


	/**
	 * Inits cutoff to use.
	 * 
	 * @param cutoff
	 *            int
	 */
	public OrthologParser(int cutoff) {
		this.cutoff = cutoff;
	}

	/**
	 * Returns putative ortholog groups.
	 * 
	 * @param taxidToMatches
	 *            Hashtable
	 * @return Hashtable
	 */
	public Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>> parse(
			Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>> taxidToMatches) {

		// new return index structure
		Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Vector<OndexMatch>>>> taxidsToQuery = new Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Vector<OndexMatch>>>>();

		// make matches symetric
		fillUpMissingMatches(taxidToMatches);

		// for all query taxids
		Enumeration<String> enuQueryTaxid = taxidToMatches.keys();
		while (enuQueryTaxid.hasMoreElements()) {
			String queryTaxid = enuQueryTaxid.nextElement();

			// for all target taxids
			Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>> queryTaxids = taxidToMatches
					.get(queryTaxid);
			Enumeration<String> enuTargetTaxid = queryTaxids.keys();
			while (enuTargetTaxid.hasMoreElements()) {
				String targetTaxid = enuTargetTaxid.nextElement();

				// use only different combinations
				if (!queryTaxid.equals(targetTaxid)) {
					Vector<OndexMatch> matches = new Vector<OndexMatch>();

					// get all query ids within taxid combination
					Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>> targetTaxids = queryTaxids
							.get(targetTaxid);
					Enumeration<ONDEXConcept> enuQuery = targetTaxids.keys();
					while (enuQuery.hasMoreElements()) {
						ONDEXConcept query = enuQuery.nextElement();

						// get all target ids within taxid combination
						Hashtable<ONDEXConcept, OndexMatch> matchQuery = targetTaxids
								.get(query);
						Enumeration<ONDEXConcept> enuTarget = matchQuery
								.keys();
						while (enuTarget.hasMoreElements()) {
							ONDEXConcept target = enuTarget.nextElement();

							// get match and add to list of matches
							matches.add(matchQuery.get(target));
						}
					}

					if (DEBUG) {
						GeneralOutputEvent so = new GeneralOutputEvent(
								"Matches between " + queryTaxid + " and "
										+ targetTaxid + " found: "
										+ matches.size(),
								"[OrthologParser - parse]");
						Mapping.propgateEvent(so);
					}

					// get average match
					Vector<OndexMatch> averages = mergeAverage(matches,
							taxidToMatches);

					// find best hit for each query
					Hashtable<ONDEXConcept, Vector<OndexMatch>> queryToMatches = findBestHits(averages);
					if (DEBUG) {
						GeneralOutputEvent so = new GeneralOutputEvent(
								"Different queries in " + queryTaxid
										+ " found: " + queryToMatches.size(),
								"[OrthologParser - parse]");
						Mapping.propgateEvent(so);
					}

					// add to taxid specific list
					if (!taxidsToQuery.containsKey(queryTaxid))
						taxidsToQuery
								.put(
										queryTaxid,
										new Hashtable<String, Hashtable<ONDEXConcept, Vector<OndexMatch>>>());
					Hashtable<String, Hashtable<ONDEXConcept, Vector<OndexMatch>>> queryTaxidMatches = taxidsToQuery
							.get(queryTaxid);
					queryTaxidMatches.put(targetTaxid, queryToMatches);

				}
			}
		}

		// make Orthologs
		Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>> orthologs = makeOrthologs(taxidsToQuery);

		return orthologs;
	}

	/**
	 * Fills pairs of matches up, so match exists in both taxid combinations.
	 * 
	 * @param taxidToMatches
	 *            Hashtable
	 */
	private void fillUpMissingMatches(
			Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>> taxidToMatches) {

		// for all query taxids
		Enumeration<String> enuQueryTaxid = taxidToMatches.keys();
		while (enuQueryTaxid.hasMoreElements()) {
			String queryTaxid = enuQueryTaxid.nextElement();

			// for all target taxids
			Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>> queryTaxids = taxidToMatches
					.get(queryTaxid);
			Enumeration<String> enuTargetTaxid = queryTaxids.keys();
			while (enuTargetTaxid.hasMoreElements()) {
				String targetTaxid = enuTargetTaxid.nextElement();

				// use only different combinations
				if (!queryTaxid.equals(targetTaxid)) {
					Vector<OndexMatch> matches = new Vector<OndexMatch>();

					// get all query concepts within taxid combination
					Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>> targetTaxids = queryTaxids
							.get(targetTaxid);
					Enumeration<ONDEXConcept> enuQuery = targetTaxids.keys();
					while (enuQuery.hasMoreElements()) {
						ONDEXConcept query = enuQuery.nextElement();

						// get all target concepts within taxid combination
						Hashtable<ONDEXConcept, OndexMatch> matchQuery = targetTaxids
								.get(query);
						Enumeration<ONDEXConcept> enuTarget = matchQuery
								.keys();
						while (enuTarget.hasMoreElements()) {
							ONDEXConcept target = enuTarget.nextElement();

							// get match and add to list of matches
							matches.add(matchQuery.get(target));
						}
					}

					// fill up all missing matches with default cutoff
					int fillUps = checkForMissingMatches(matches,
							taxidToMatches);

					if (DEBUG) {
						GeneralOutputEvent so = new GeneralOutputEvent(
								"FillUps between " + targetTaxid + " and "
										+ queryTaxid + " found: " + fillUps,
								"[OrthologParser - fillUpMissingMatches]");
						Mapping.propgateEvent(so);
					}
				}
			}
		}
	}

	/**
	 * Make new match with opposite taxid combination if missing
	 * 
	 * @param matches
	 *            Vector<OndexMatch>
	 * @param taxidToMatches
	 *            Hashtable
	 * @return int
	 */
	private int checkForMissingMatches(
			Vector<OndexMatch> matches,
			Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>> taxidToMatches) {

		int fillUps = 0;

		// go through each match
		for (int i = 0; i < matches.size(); i++) {
			OndexMatch match = matches.get(i);

			// look up taxids in reversed order
			if (!taxidToMatches.containsKey(match.getTargetTaxId()))
				taxidToMatches
						.put(
								match.getTargetTaxId(),
								new Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>());
			Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>> taxidTarget = taxidToMatches
					.get(match.getTargetTaxId());

			if (!taxidTarget.containsKey(match.getQueryTaxId()))
				taxidTarget
						.put(
								match.getQueryTaxId(),
								new Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>());
			Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>> taxidQuery = taxidTarget
					.get(match.getQueryTaxId());

			// look up with reversed order of target, query id
			if (!taxidQuery.containsKey(match.getTarget()))
				taxidQuery.put(match.getTarget(),
						new Hashtable<ONDEXConcept, OndexMatch>());
			Hashtable<ONDEXConcept, OndexMatch> matchTarget = taxidQuery
					.get(match.getTarget());

			if (!matchTarget.containsKey(match.getQuery())) {
				// construct complement to match
				OndexMatch newMatch = new OndexMatch();
				newMatch.setQuery(match.getTarget());
				newMatch.setQueryTaxId(match.getTargetTaxId());
				newMatch.setTarget(match.getQuery());
				newMatch.setTargetTaxId(match.getQueryTaxId());
				newMatch.setScore(cutoff);
				fillUps++;
				// add newMatch to global list
				matchTarget.put(match.getQuery(), newMatch);
			}
		}

		return fillUps;
	}

	/**
	 * Merges scoreAB and scoreBA to an average.
	 * 
	 * @param matches
	 *            Vector<OndexMatch>
	 * @param taxidToMatches
	 *            Hashtable
	 * @return Vector<OndexMatch>
	 */
	private Vector<OndexMatch> mergeAverage(
			Vector<OndexMatch> matches,
			Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>> taxidToMatches) {

		Vector<OndexMatch> averages = new Vector<OndexMatch>();

		// for each match
		for (int i = 0; i < matches.size(); i++) {
			OndexMatch first = matches.get(i);

			// look up taxids in reversed order
			Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>> taxidTarget = taxidToMatches
					.get(first.getTargetTaxId());
			Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>> taxidQuery = taxidTarget
					.get(first.getQueryTaxId());

			// look up with reversed order of target, query id
			Hashtable<ONDEXConcept, OndexMatch> matchTarget = taxidQuery
					.get(first.getTarget());
			OndexMatch second = matchTarget.get(first.getQuery());

			int average = (int) ((first.getScore() + second.getScore()) / 2.0 + 0.5);

			// make combination of first and second
			OndexMatch merged = new OndexMatch();
			merged.setQuery(first.getQuery());
			merged.setQueryTaxId(first.getQueryTaxId());
			merged.setTarget(first.getTarget());
			merged.setTargetTaxId(first.getTargetTaxId());
			merged.setScore(average);
			averages.add(merged);
		}

		return averages;
	}

	/**
	 * Finds the best hit for each query.
	 * 
	 * @param averages
	 *            Vector<OndexMatch>
	 * @return Hashtable
	 */
	private Hashtable<ONDEXConcept, Vector<OndexMatch>> findBestHits(
			Vector<OndexMatch> averages) {

		Hashtable<ONDEXConcept, Vector<OndexMatch>> queryToMatches = new Hashtable<ONDEXConcept, Vector<OndexMatch>>();

		// get matches for each query
		for (int i = 0; i < averages.size(); i++) {
			OndexMatch match = averages.get(i);
			if (!queryToMatches.containsKey(match.getQuery()))
				queryToMatches.put(match.getQuery(), new Vector<OndexMatch>());
			Vector<OndexMatch> matches = queryToMatches.get(match.getQuery());
			matches.add(match);
		}

		// for each query sort matches of query
		Enumeration<ONDEXConcept> enu = queryToMatches.keys();
		while (enu.hasMoreElements()) {
			ONDEXConcept query = enu.nextElement();
			Vector<OndexMatch> matches = queryToMatches.get(query);
			Collections.sort(matches);
			/*
			 * System.out.print(query+": "); for (int i=0; i < matches.size();
			 * i++) { Match match = (Match) matches.get(i);
			 * System.out.print(match.getTarget()+"="+match.getScore()+" "); }
			 * System.out.println();
			 */
		}

		return queryToMatches;
	}

	/**
	 * Makes ortholog groups from best hits.
	 * 
	 * @param taxidsToQuery
	 *            Hashtable
	 * @return Hashtable
	 */
	private Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>> makeOrthologs(
			Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Vector<OndexMatch>>>> taxidsToQuery) {

		Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>> orthologs = new Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>>();

		int count = 0;

		// get taxid combinations
		Enumeration<String> enuQueryTaxid = taxidsToQuery.keys();
		while (enuQueryTaxid.hasMoreElements()) {
			String queryTaxid = enuQueryTaxid.nextElement();

			Hashtable<String, Hashtable<ONDEXConcept, Vector<OndexMatch>>> queryTaxidMatches = taxidsToQuery
					.get(queryTaxid);
			Enumeration<String> enuTargetTaxid = queryTaxidMatches.keys();
			while (enuTargetTaxid.hasMoreElements()) {
				String targetTaxid = enuTargetTaxid.nextElement();

				// get queries for each taxid combination
				Hashtable<ONDEXConcept, Vector<OndexMatch>> targetTaxidMatches = queryTaxidMatches
						.get(targetTaxid);
				Enumeration<ONDEXConcept> enuQuery = targetTaxidMatches
						.keys();
				while (enuQuery.hasMoreElements()) {
					ONDEXConcept query = enuQuery.nextElement();

					Vector<OndexMatch> firstMatches = targetTaxidMatches
							.get(query);
					OndexMatch firstBestHit = firstMatches.get(0);

					// get matches for first best hit
					Hashtable<String, Hashtable<ONDEXConcept, Vector<OndexMatch>>> secondTargetTaxid = taxidsToQuery
							.get(firstBestHit.getTargetTaxId());
					Hashtable<ONDEXConcept, Vector<OndexMatch>> secondQueryTaxid = secondTargetTaxid
							.get(firstBestHit.getQueryTaxId());
					Vector<OndexMatch> secondMatches = secondQueryTaxid
							.get(firstBestHit.getTarget());

					OndexMatch secondBestHit = secondMatches.get(0);

					// check if the two best hits matches each other
					if (firstBestHit.getQuery().equals(secondBestHit.getTarget())) {
						if (buildOrtholog(orthologs, firstBestHit,
								secondBestHit))
							count++;
					}
				}
			}
		}

		if (DEBUG)
			System.out.println("Total of possible orthologs: " + count);

		return orthologs;
	}

	/**
	 * Build a new pair of orthologs.
	 * 
	 * @param orthologs
	 *            Hashtable
	 * @param firstBestHit
	 *            OndexMatch
	 * @param secondBestHit
	 *            OndexMatch
	 * @return boolean
	 */
	private boolean buildOrtholog(
			Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>> orthologs,
			OndexMatch firstBestHit, OndexMatch secondBestHit) {

		// check if reverse still exists as ortholog
		if (!orthologs.containsKey(secondBestHit.getQueryTaxId()))
			orthologs
					.put(
							secondBestHit.getQueryTaxId(),
							new Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>());
		Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>> checkQueryTaxid = orthologs
				.get(secondBestHit.getQueryTaxId());

		if (!checkQueryTaxid.containsKey(secondBestHit.getTargetTaxId()))
			checkQueryTaxid
					.put(
							secondBestHit.getTargetTaxId(),
							new Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>());
		Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>> checkTargetTaxid = checkQueryTaxid
				.get(secondBestHit.getTargetTaxId());

		if (!checkTargetTaxid.containsKey(secondBestHit.getQuery()))
			checkTargetTaxid.put(secondBestHit.getQuery(),
					new Hashtable<ONDEXConcept, Ortholog>());
		Hashtable<ONDEXConcept, Ortholog> checkQuery = checkTargetTaxid
				.get(secondBestHit.getQuery());

		// if reverse not yet exists
		if (!checkQuery.containsKey(secondBestHit.getTarget())) {

			// make new Ortholog from firstBestHit
			Ortholog ortho = new Ortholog();
			ortho.setScore(firstBestHit.getScore());

			// main ortholog A
			Inparalog mainA = new Inparalog(firstBestHit.getQuery(),
					firstBestHit.getQueryTaxId());
			mainA.setConfidence(100.0);
			ortho.setMainA(mainA);
			ortho.setTaxidA(firstBestHit.getQueryTaxId());

			// main ortholog B
			Inparalog mainB = new Inparalog(firstBestHit.getTarget(),
					firstBestHit.getTargetTaxId());
			mainB.setConfidence(100.0);
			ortho.setMainB(mainB);
			ortho.setTaxidB(firstBestHit.getTargetTaxId());

			// add to orthologs
			if (!orthologs.containsKey(firstBestHit.getQueryTaxId()))
				orthologs
						.put(
								firstBestHit.getQueryTaxId(),
								new Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>());
			Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>> putQueryTaxid = orthologs
					.get(firstBestHit.getQueryTaxId());

			if (!putQueryTaxid.containsKey(firstBestHit.getTargetTaxId()))
				putQueryTaxid
						.put(
								firstBestHit.getTargetTaxId(),
								new Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>());
			Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>> putTargetTaxid = putQueryTaxid
					.get(firstBestHit.getTargetTaxId());

			if (!putTargetTaxid.containsKey(firstBestHit.getQuery()))
				putTargetTaxid.put(firstBestHit.getQuery(),
						new Hashtable<ONDEXConcept, Ortholog>());
			Hashtable<ONDEXConcept, Ortholog> putQuery = putTargetTaxid
					.get(firstBestHit.getQuery());
			putQuery.put(firstBestHit.getTarget(), ortho);

			return true;
		}

		return false;
	}
}
