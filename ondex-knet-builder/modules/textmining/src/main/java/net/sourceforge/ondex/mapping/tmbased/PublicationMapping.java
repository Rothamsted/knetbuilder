package net.sourceforge.ondex.mapping.tmbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This Class represents a single publication along with all matching concepts
 * (hits) to it. The hits are grouped together according to their Concept
 * Classes.
 * 
 * @author keywan
 * 
 */
public class PublicationMapping {

	// publication ID
	private Integer pubID;
	// contains hits to this publication grouped by CC
	private HashMap<String, ArrayList<Hit>> hits;

	/**
	 * Standard constructor
	 * 
	 * @param pubID
	 *            Publication ID
	 */
	public PublicationMapping(Integer pubID) {
		this.pubID = pubID;
		this.hits = new HashMap<String, ArrayList<Hit>>();
	}

	/**
	 * Add a hit to the publication
	 * 
	 * @param cc
	 *            CC of the Hit
	 * @param hit
	 *            Hit concept
	 */
	public void addHit(String cc, Hit hit) {
		if (!this.hits.containsKey(cc)) {
			this.hits.put(cc, new ArrayList<Hit>());
		}
		this.hits.get(cc).add(hit);
	}

	/**
	 * Remove hit of type CC from this publication
	 * 
	 * @param cc
	 *            CC of the Hit
	 * @param hit
	 *            Hit that should be removed
	 */
	public void removeHit(String cc, Hit hit) {
		this.hits.get(cc).remove(hit);
	}

	/**
	 * Remove set of hits of type CC from this publication
	 * 
	 * @param cc
	 *            CC of the Hit
	 * @param hits
	 *            A Set of hits that should be removed
	 */
	public void removeHits(String cc, HashSet<Hit> hits) {
		if (!this.hits.containsKey(cc)) {
			return;
		}

		Iterator<Hit> hitIt = hits.iterator();
		while (hitIt.hasNext()) {
			Hit hit = hitIt.next();
			removeHit(cc, hit);
		}
	}

	/**
	 * Get all hits of given Concept Class
	 * 
	 * @param cc
	 *            ConceptClass
	 * @return all Hits of CC
	 */
	public ArrayList<Hit> getHitsForCC(String cc) {
		return this.hits.get(cc);
	}

	/**
	 * Get a single Hit of Concept Class
	 * 
	 * @param cc
	 *            ConceptClass
	 * @param hitID
	 *            Hit ID
	 * @return Hit from given CC and with ID
	 */
	public Hit getHit(String cc, Integer hitID) {
		if (!this.hits.containsKey(cc)) {
			return null;
		}
		Iterator<Hit> hitIt = this.hits.get(cc).iterator();
		while (hitIt.hasNext()) {
			Hit hit = hitIt.next();
			if (hit.getHitConID().equals(hitID)) {
				return hit;
			}
		}
		return null;
	}

	/**
	 * Check if publication contains given Hit
	 * 
	 * @param cc
	 *            ConceptClass
	 * @param hitID
	 *            Hit ID
	 * @return
	 */
	public boolean containsHit(String cc, Integer hitID) {
		if (!this.hits.containsKey(cc)) {
			return false;
		}
		Iterator<Hit> hitIt = this.hits.get(cc).iterator();
		while (hitIt.hasNext()) {
			Hit hit = hitIt.next();
			if (hit.getHitConID().equals(hitID)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get all hits of every CC
	 * 
	 * @return HashMap containing CC as key, and arraylist of Hits as value
	 */
	public HashMap<String, ArrayList<Hit>> getHits() {
		return hits;
	}

	/**
	 * get Publication ID
	 * 
	 * @return
	 */
	public Integer getPublicationID() {
		return pubID;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("----------------------\n");
		buf.append("PUB: " + this.getPublicationID() + "\n");
		buf.append("----------------------\n");

		HashMap<String, ArrayList<Hit>> results = this.getHits();

		for (String cc : results.keySet()) {
			Iterator<Hit> hitIt = results.get(cc).iterator();
			buf.append(cc + ": ");
			while (hitIt.hasNext()) {
				Hit hit = hitIt.next();
				buf.append(hit.getHitConID() + " [" + hit.getScore() + "] ["
						+ hit.getNumberOfEvidence() + "] ["
						+ hit.getNumberOfOccurrence() + "]\n");

				Iterator<Occurrence> occIt = hit.getOccurrence().iterator();
				while (occIt.hasNext()) {
					Occurrence occ = occIt.next();
					buf.append(occ.getQuery() + " [" + occ.getScore() + "] ");
				}
				buf.append("\n");
				Iterator<String> eviIt = hit.getEvidence().iterator();
				while (eviIt.hasNext()) {
					String evi = eviIt.next();
					buf.append(evi + "\n");
				}
			}
		}

		return buf.toString();
	}

}
