package net.sourceforge.ondex.core.searchable;

import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * Allows the returning of scores on Graph hits
 * 
 * @author hindlem, taubertj
 * 
 * @param <E>
 *            the type of ONDEX entity
 */
public class ScoredHits<E extends ONDEXEntity> {

	/**
	 * All found ONDEXEntites
	 */
	private final Set<E> ondexHits;

	/**
	 * Score per ONDEXEntity
	 */
	private final Map<Integer, Float> scoresForHits;

	/**
	 * Sets internal data structures.
	 * 
	 * @param ondexHits
	 *            Set<E>
	 * @param scoresForHits
	 *            Map<Integer, Float>
	 */
	public ScoredHits(Set<E> ondexHits, Map<Integer, Float> scoresForHits) {
		this.ondexHits = ondexHits;
		this.scoresForHits = scoresForHits;
	}

	/**
	 * Returns hits for the specified query
	 * 
	 * @return Set<E>
	 */
	public Set<E> getOndexHits() {
		return ondexHits;
	}

	/**
	 * Retrieve the score of a given ONDEXEntity
	 * 
	 * @param entity
	 *            a ONDEX graph entity (e.g ONDEXConcept)
	 * @return the score for this entity
	 */
	public float getScoreOnEntity(E entity) {
		int id;
		if (entity instanceof ONDEXConcept) {
			id = entity.getId();
		} else if (entity instanceof ONDEXRelation) {
			id = entity.getId();
		} else {
			throw new RuntimeException(entity.getClass()
					+ " is incompatible with these Hits.");
		}
		if (scoresForHits.containsKey(id))
			return scoresForHits.get(id);
		return -1; // problem if not in scores?
	}

	/**
	 * Returns scores for the hit of the specified query (ONDEX id -> score).
	 * 
	 * @return Map<Integer, Float>
	 */
	public Map<Integer, Float> getScoresForHits() {
		return scoresForHits;
	}
}
