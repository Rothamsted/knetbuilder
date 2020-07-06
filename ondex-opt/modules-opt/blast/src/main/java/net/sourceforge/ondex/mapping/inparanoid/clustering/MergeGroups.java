package net.sourceforge.ondex.mapping.inparanoid.clustering;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.mapping.inparanoid.Mapping;

/**
 * Merges groups of orthologs together according to the INPARANOID algorithm.
 * 
 * @author taubertj
 * 
 */
public class MergeGroups {

	private static boolean DEBUG = Mapping.DEBUG;

	private static boolean DEBUG_NOT_TRIGGERED = true;
	
	// current seq cutoff
	private int cutoff;

	// global list of orthologs
	private Vector<Ortholog> orthos;

	// index structure
	private Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>> taxidToMatches;

	public MergeGroups(int cutoff) {
		this.cutoff = cutoff;
	}

	/**
	 * Delete weaker group if (score(A2-B2) - score(A1-B1) > 50).
	 * 
	 * @param ortho1
	 *            Ortholog
	 */
	private void deleteWeaker(Ortholog ortho1) {

		Iterator<Ortholog> it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho2 = it.next();

			if (!ortho1.equals(ortho2) && ortho1.getMainA() != null
					&& ortho2.getMainA() != null) {
				if (ortho1.inA.contains(ortho2.getMainA())
						|| ortho1.inB.contains(ortho2.getMainB())) {
					if (Math.abs(ortho2.getScore() - ortho1.getScore()) > cutoff) {
						if (ortho2.getScore() > ortho1.getScore()) {
							if (DEBUG) {
								GeneralOutputEvent so = new GeneralOutputEvent(
										"delete weaker group ortho1="
												+ ortho1.getScore() + " < "
												+ ortho2.getScore(),
										"[MergeGroups - deleteWeaker]");
								Mapping.propgateEvent(so);
							}
							ortho1.setMainA(null);
							ortho1.setMainB(null);
							ortho1.inA.clear();
							ortho1.inB.clear();
						} else if (ortho1.getScore() > ortho2.getScore()) {
							if (DEBUG) {
								GeneralOutputEvent so = new GeneralOutputEvent(
										"delete weaker group ortho2="
												+ ortho2.getScore() + " < "
												+ ortho1.getScore(),
										"[MergeGroups - deleteWeaker]");
								Mapping.propgateEvent(so);
							}
							ortho2.setMainA(null);
							ortho2.setMainB(null);
							ortho2.inA.clear();
							ortho2.inB.clear();
						} else {
							GeneralOutputEvent so = new GeneralOutputEvent(
									"not deleting weaker, error: scores are the same!!!",
									"[MergeGroups - deleteWeaker]");
							Mapping.propgateEvent(so);
							so = new GeneralOutputEvent(ortho1.getMainA() + " "
									+ ortho1.getMainB() + " " + ortho1.getScore(),
									"[MergeGroups - deleteWeaker]");
							Mapping.propgateEvent(so);
							so = new GeneralOutputEvent(ortho2.getMainA() + " "
									+ ortho2.getMainB() + " " + ortho2.getScore(),
									"[MergeGroups - deleteWeaker]");
							Mapping.propgateEvent(so);
						}
					}
				}
			}
		}
	}

	/**
	 * Merges a given list of orthologs.
	 * 
	 * @param o
	 *            Vector<Ortholog>
	 * @param t
	 *            Hashtable
	 * @return Vector<Ortholog>
	 */
	public Vector<Ortholog> merge(
			Vector<Ortholog> o,
			Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>> t) {

		orthos = o;
		taxidToMatches = t;

		// first sort orthologs according to score
		Collections.sort(orthos);

		// iterate through all orthologs
		Iterator<Ortholog> it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho = it.next();

			mergeStrongerGroup(ortho);

			mergeEquallyGood(ortho);

			deleteWeaker(ortho);

			mergeHighConfidence(ortho);
		}

		// remove all deleted ortholog groups
		it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho = it.next();
			if (ortho.getMainA() == null) {
				it.remove();
			}
		}

		// resolve overlapping
		it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho = it.next();
			resolveOverlapping(ortho);
		}

		// remove main orthologs from inparalogs
		it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho = it.next();
			checkForMainOrthologsInInparalogs(ortho);
		}

		// get inparalog number for each taxid
		Map<String, Integer> inparas = new HashMap<String, Integer>();
		it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho = it.next();

			if (DEBUG) {
				GeneralOutputEvent so = new GeneralOutputEvent(ortho.getTaxidA()
						+ " " + ortho.getMainA().getConcept().getPID() + " - "
						+ ortho.getTaxidB() + " " + ortho.getMainB().getConcept().getPID()
						+ " = " + ortho.getScore(), "[MergeGroups - merge]");
				Mapping.propgateEvent(so);
				if (ortho.inA.size() > 0)
					System.out.println(ortho.inA);
				if (ortho.inB.size() > 0)
					System.out.println(ortho.inB);
			}

			if (!inparas.containsKey(ortho.getTaxidA()))
				inparas.put(ortho.getTaxidA(), 0);
			inparas.put(ortho.getTaxidA(), inparas.get(ortho.getTaxidA())
					+ ortho.inA.size());

			if (!inparas.containsKey(ortho.getTaxidB()))
				inparas.put(ortho.getTaxidB(), 0);
			inparas.put(ortho.getTaxidB(), inparas.get(ortho.getTaxidB())
					+ ortho.inB.size());
		}

		// output inparalog number
		Iterator<String> it2 = inparas.keySet().iterator();
		while (it2.hasNext()) {
			String taxId = it2.next();
			GeneralOutputEvent so = new GeneralOutputEvent("Inparalogs for "
					+ taxId + " = " + inparas.get(taxId),
					"[MergeGroups - merge]");
			Mapping.propgateEvent(so);
		}

		return orthos;
	}

	/**
	 * Divides inparalogs in overlapping areas according to distance to main
	 * ortholog.
	 * 
	 * @param ortho1
	 *            Ortholog
	 */
	private void resolveOverlapping(Ortholog ortho1) {

		Iterator<Ortholog> it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho2 = it.next();

			// got through each different combination
			if (!ortho1.equals(ortho2)) {

				// check inparalogs for mainA
				if (!Collections.disjoint(ortho1.inA, ortho2.inA)) {

					// get mapping inparalog id to confidence for ortho1
					Map<Inparalog, Double> orthoInA1 = new HashMap<Inparalog, Double>();
					Iterator<Inparalog> it2 = ortho1.inA.iterator();
					while (it2.hasNext()) {
						Inparalog inpara = it2.next();
						orthoInA1.put(inpara, inpara.getConfidence());
					}

					// get mapping inparalog id to confidence for ortho2
					Map<Inparalog, Double> orthoInA2 = new HashMap<Inparalog, Double>();
					it2 = ortho2.inA.iterator();
					while (it2.hasNext()) {
						Inparalog inpara = it2.next();
						orthoInA2.put(inpara, inpara.getConfidence());
					}

					// remove overlapping inparalog
					it2 = orthoInA1.keySet().iterator();
					while (it2.hasNext()) {
						Inparalog inpara = it2.next();
						if (orthoInA2.containsKey(inpara)) {
							double confidence1 = orthoInA1.get(inpara);
							double confidence2 = orthoInA2.get(inpara);
							if (confidence1 > confidence2) {
								ortho2.inA.remove(inpara);
							} else {
								ortho1.inA.remove(inpara);
							}
						}
					}
				}

				// check inparalogs from mainB
				if (!Collections.disjoint(ortho1.inB, ortho2.inB)) {

					// get mapping inparalog id to confidence for ortho1
					Map<Inparalog, Double> orthoInB1 = new HashMap<Inparalog, Double>();
					Iterator<Inparalog> it2 = ortho1.inB.iterator();
					while (it2.hasNext()) {
						Inparalog inpara = it2.next();
						orthoInB1.put(inpara, inpara.getConfidence());
					}

					// get mapping inparalog id to confidence for ortho2
					Map<Inparalog, Double> orthoInB2 = new HashMap<Inparalog, Double>();
					it2 = ortho2.inB.iterator();
					while (it2.hasNext()) {
						Inparalog inpara = it2.next();
						orthoInB2.put(inpara, inpara.getConfidence());
					}

					// remove overlapping inparalogs
					it2 = orthoInB1.keySet().iterator();
					while (it2.hasNext()) {
						Inparalog inpara = it2.next();
						if (orthoInB2.containsKey(inpara)) {
							double confidence1 = orthoInB1.get(inpara);
							double confidence2 = orthoInB2.get(inpara);
							if (confidence1 > confidence2) {
								ortho2.inB.remove(inpara);
							} else {
								ortho1.inB.remove(inpara);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Merge if two equally good best hits found.
	 * 
	 * @param ortho1
	 *            Ortholog
	 */
	private void mergeEquallyGood(Ortholog ortho1) {

		Iterator<Ortholog> it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho2 = it.next();

			if (!ortho1.equals(ortho2) && ortho1.getMainA() != null
					&& ortho2.getMainA() != null) {
				if (ortho1.getMainB().equals(ortho2.getMainB())
						&& ortho1.getScore() == ortho2.getScore()) {
					if (DEBUG) {
						GeneralOutputEvent so = new GeneralOutputEvent(
								"merging equally good mainB1=mainB2",
								"[MergeGroups - mergeEquallyGood]");
						Mapping.propgateEvent(so);
					}
					ortho1.inA.addAll(ortho2.inA);
					ortho1.inB.addAll(ortho2.inB);
					ortho1.inA.add(ortho2.getMainA());
					ortho1.inB.add(ortho2.getMainB());
					ortho2.setMainA(null);
					ortho2.setMainB(null);
					ortho2.inA.clear();
					ortho2.inB.clear();
					recalcConfidence(ortho1);
				} else if (ortho1.getMainA().equals(ortho2.getMainA())
						&& ortho1.getScore() == ortho2.getScore()) {
					if (DEBUG) {
						GeneralOutputEvent so = new GeneralOutputEvent(
								"merging equally good mainA1=mainA2",
								"[MergeGroups - mergeEquallyGood]");
						Mapping.propgateEvent(so);
					}
					ortho1.inA.addAll(ortho2.inA);
					ortho1.inB.addAll(ortho2.inB);
					ortho1.inA.add(ortho2.getMainA());
					ortho1.inB.add(ortho2.getMainB());
					ortho2.setMainA(null);
					ortho2.setMainB(null);
					ortho2.inA.clear();
					ortho2.inB.clear();
					recalcConfidence(ortho1);
				}
			}
		}
	}

	/**
	 * Merge if (score(A1-A2) < 0.5 * score(A1-B1)).
	 * 
	 * @param ortho1
	 *            Ortholog
	 */
	private void mergeHighConfidence(Ortholog ortho1) {

		Iterator<Ortholog> it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho2 = it.next();

			if (!ortho1.equals(ortho2) && ortho1.getMainA() != null
					&& ortho2.getMainA() != null) {
				if (ortho1.inA.contains(ortho2.getMainA())) {
					Iterator<Inparalog> it2 = ortho1.inA.iterator();
					Inparalog inpara = null;
					while (it2.hasNext()) {
						inpara = it2.next();
						if (inpara.equals(ortho2.getMainA()))
							break;
					}
					if (inpara.getConfidence() > 50.0) {
						if (DEBUG) {
							GeneralOutputEvent so = new GeneralOutputEvent(
									"merging high confidence mainA",
									"[MergeGroups - mergeHighConfidence]");
							Mapping.propgateEvent(so);
						}
						ortho1.inA.addAll(ortho2.inA);
						ortho1.inB.addAll(ortho2.inB);
						ortho1.inA.add(ortho2.getMainA());
						ortho1.inB.add(ortho2.getMainB());
						ortho2.setMainA(null);
						ortho2.setMainB(null);
						ortho2.inA.clear();
						ortho2.inB.clear();
						recalcConfidence(ortho1);
					}
				}
				if (ortho1.inB.contains(ortho2.getMainB())) {
					Iterator<Inparalog> it2 = ortho1.inB.iterator();
					Inparalog inpara = null;
					while (it2.hasNext()) {
						inpara = it2.next();
						if (inpara.equals(ortho2.getMainB()))
							break;
					}
					if (inpara.getConfidence() > 50.0) {
						if (DEBUG) {
							GeneralOutputEvent so = new GeneralOutputEvent(
									"merging high confidence mainB",
									"[MergeGroups - mergeHighConfidence]");
							Mapping.propgateEvent(so);
						}
						ortho1.inA.addAll(ortho2.inA);
						ortho1.inB.addAll(ortho2.inB);
						ortho1.inA.add(ortho2.getMainA());
						ortho1.inB.add(ortho2.getMainB());
						ortho2.setMainA(null);
						ortho2.setMainB(null);
						ortho2.inA.clear();
						ortho2.inB.clear();
						recalcConfidence(ortho1);
					}
				}
			}
		}
	}

	/**
	 * Merge groups if mainA2 and mainB2 are already clustered in a stronger
	 * group mainA1-mainB1.
	 * 
	 * @param ortho1
	 *            Ortholog
	 */
	private void mergeStrongerGroup(Ortholog ortho1) {

		Iterator<Ortholog> it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho2 = it.next();

			if (!ortho1.equals(ortho2)) {
				if (ortho1.inA.contains(ortho2.getMainA())
						&& ortho1.inB.contains(ortho2.getMainB())
						&& ortho1.getScore() > ortho2.getScore()) {
					if (DEBUG) {
						GeneralOutputEvent so = new GeneralOutputEvent(
								"merging stronger groups",
								"[MergeGroups - mergeStrongerGroup]");
						Mapping.propgateEvent(so);
					}
					ortho1.inA.addAll(ortho2.inA);
					ortho1.inB.addAll(ortho2.inB);
					ortho1.inA.add(ortho2.getMainA());
					ortho1.inB.add(ortho2.getMainB());
					ortho2.setMainA(null);
					ortho2.setMainB(null);
					ortho2.inA.clear();
					ortho2.inB.clear();
					recalcConfidence(ortho1);
				}
			}
		}
	}

	/**
	 * Recalculates confidence value for each inparalog.
	 * 
	 * @param ortho
	 *            Ortholog
	 */
	private void recalcConfidence(Ortholog ortho) {

		// get inparalogs for mainA
		Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>> taxidA = taxidToMatches
				.get(ortho.getTaxidA());
		Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>> taxidAA = taxidA
				.get(ortho.getTaxidA());

		// get all matches for ortho.getMainA().id
		Hashtable<ONDEXConcept, OndexMatch> matchesAA = taxidAA
				.get(ortho.getMainA().getConcept());
		OndexMatch aa = matchesAA.get(ortho.getMainA().getConcept());

		Iterator<Inparalog> it = ortho.inA.iterator();
		while (it.hasNext()) {
			Inparalog inpara = it.next();
			if (matchesAA.containsKey(inpara.getConcept())) {
				OndexMatch match = matchesAA.get(inpara.getConcept());
				inpara.setConfidence(100.0 * (match.getScore() - ortho.getScore())
						/ (aa.getScore() - ortho.getScore()));
			} else {
				// if there isnt a match between mainA and inparalog use cutoff
				inpara.setConfidence(100.0 * (cutoff - ortho.getScore())
						/ (aa.getScore() - ortho.getScore()));
			}
		}

		// get inparalogs for mainB
		Hashtable<ONDEXConcept, OndexMatch> matchesBB = null;
		Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>> taxidB = null;
		Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>> taxidBB = null;
		OndexMatch bb = null;
		
		try{
			taxidB = taxidToMatches.get(ortho.getTaxidB());
			taxidBB = taxidB.get(ortho.getTaxidB());
			// get all matches for ortho.getMainB().id
			matchesBB = taxidBB.get(ortho.getMainB().getConcept());
			bb = matchesBB.get(ortho.getMainB().getConcept());
		}
		catch(NullPointerException e){
			try{
				if(DEBUG_NOT_TRIGGERED){
					DEBUG_NOT_TRIGGERED = false;
					System.err.println("ortho.getMainB().concept  = "+ortho.getMainB().getConcept());
					System.err.println("matchesBB  = "+matchesBB);
					System.err.println("taxidBB  = "+taxidBB);
					System.err.println("taxidB  = "+taxidB);
					System.err.println("ortho.getTaxidB()  = "+ortho.getTaxidB());
					System.err.println("bb  = "+bb);	
					e.printStackTrace();	
				}

			}
			catch(NullPointerException e1){
				if(DEBUG_NOT_TRIGGERED){
					System.err.println("Unexpected null pointer exception!");
					e1.printStackTrace();	
				}
			}
			return;
		}
		if(matchesBB == null || bb == null){
			return;
		}

		it = ortho.inB.iterator();
		while (it.hasNext()) {
			Inparalog inpara = it.next();
			if (matchesBB.containsKey(inpara.getConcept())) {
				OndexMatch match = matchesBB.get(inpara.getConcept());
				inpara.setConfidence(100.0 * (match.getScore() - ortho.getScore())
						/ (bb.getScore() - ortho.getScore()));
			} else {
				// if there isnt a match between mainB and inparalog use cutoff
				inpara.setConfidence(100.0 * (cutoff - ortho.getScore())
						/ (bb.getScore() - ortho.getScore()));
			}
		}
	}

	/**
	 * Removes valid main orthologs from inparalogs.
	 * 
	 * @param ortho1
	 *            Ortholog
	 */
	private void checkForMainOrthologsInInparalogs(Ortholog ortho1) {

		Iterator<Ortholog> it = orthos.iterator();
		while (it.hasNext()) {
			Ortholog ortho2 = it.next();

			if (ortho2.inA.contains(ortho1.getMainA())) {
				if (DEBUG) {
					GeneralOutputEvent so = new GeneralOutputEvent(
							"remove main ortholog A " + ortho1.getMainA() + " from "
									+ ortho2.inA,
							"[MergeGroups - checkForMainOrthologsInInparalogs]");
					Mapping.propgateEvent(so);
				}
				ortho2.inA.remove(ortho1.getMainA());
			}

			if (ortho2.inB.contains(ortho1.getMainB())) {
				if (DEBUG) {
					GeneralOutputEvent so = new GeneralOutputEvent(
							"remove main ortholog B " + ortho1.getMainB() + " from "
									+ ortho2.inB,
							"[MergeGroups - checkForMainOrthologsInInparalogs]");
					Mapping.propgateEvent(so);
				}
				ortho2.inB.remove(ortho1.getMainB());
			}
		}
	}
}
