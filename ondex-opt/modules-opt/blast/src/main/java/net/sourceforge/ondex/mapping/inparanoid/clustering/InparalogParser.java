package net.sourceforge.ondex.mapping.inparanoid.clustering;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.mapping.inparanoid.Mapping;

/**
 * Adds inparalogs to the given ortholog groups.
 * 
 * @author taubertj
 * 
 */
public class InparalogParser {


	/**
	 * Constructor 
	 */
	public InparalogParser() {
	}

	/**
	 * Parses inparalogs from a given index structure and adds them to the
	 * corresponding ortholog groups.
	 * 
	 * @param orthologs
	 *            Hashtable
	 * @param taxidToMatches
	 *            Hashtable
	 * @return Vector<Ortholog>
	 */
	public Vector<Ortholog> parse(
			Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>>> orthologs,
			Hashtable<String, Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>>> taxidToMatches) {

		// init return vector
		Vector<Ortholog> orthos = new Vector<Ortholog>();

		// go through all taxids for A
		Iterator<String> it = orthologs.keySet().iterator();
		while (it.hasNext()) {
			String taxidA = it.next();

			// get hashtable for taxidA
			Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>>> orthosTaxidA = orthologs
					.get(taxidA);
			Iterator<String> it2 = orthosTaxidA.keySet().iterator();
			while (it2.hasNext()) {
				String taxidB = it2.next();

				// get hashtable for taxidB
				Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, Ortholog>> orthosTaxidAB = orthosTaxidA
						.get(taxidB);
				Iterator<ONDEXConcept> it3 = orthosTaxidAB.keySet()
						.iterator();
				while (it3.hasNext()) {
					ONDEXConcept orthoA = it3.next();

					// get hashtable for orthoA
					Hashtable<ONDEXConcept, Ortholog> hitsOrthoA = orthosTaxidAB
							.get(orthoA);
					Iterator<ONDEXConcept> it4 = hitsOrthoA.keySet()
							.iterator();
					while (it4.hasNext()) {
						ONDEXConcept orthoB = it4.next();

						// build an ortholog group
						Ortholog ortho = hitsOrthoA.get(orthoB);
						orthos.add(ortho);
					}
				}
			}
		}

		for (int i = 0; i < orthos.size(); i++) {
			Ortholog ortho = orthos.get(i);

			// get inparalogs for mainA
			Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>> taxidA = taxidToMatches
					.get(ortho.getTaxidA());
			Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>> taxidAA = taxidA
					.get(ortho.getTaxidA());
			if (taxidAA.containsKey(ortho.getMainA().getConcept())) {

				// get all matches for ortho.mainA.id
				Hashtable<ONDEXConcept, OndexMatch> matchesAA = taxidAA
						.get(ortho.getMainA().getConcept());
				OndexMatch aa = matchesAA.get(ortho.getMainA().getConcept());

				if (aa != null) {
					Enumeration<OndexMatch> enu = matchesAA.elements();
					while (enu.hasMoreElements()) {
						OndexMatch match = enu.nextElement();

						// score of inparalog greater then mainScore
						if (match.getScore() > ortho.getScore()
								&& !match.getTarget().equals(ortho.getMainA().getConcept())) {
							Inparalog inpara = new Inparalog(match.getTarget(),
									match.getTargetTaxId());
							inpara.setConfidence(100.0
									* (match.getScore() - ortho.getScore())
									/ (aa.getScore() - ortho.getScore()));
							ortho.inA.add(inpara);
						}
					}
				} else {
					GeneralOutputEvent so = new GeneralOutputEvent(
							"missing selfmatch for " + ortho.getTaxidA() + " "
									+ ortho.getMainA().getConcept().getPID(),
							"[InparalogParser - parse]");
					Mapping.propgateEvent(so);
					Enumeration<OndexMatch> enu = matchesAA.elements();
					while (enu.hasMoreElements()) {
						OndexMatch match = enu.nextElement();
						so = new GeneralOutputEvent(match.getQuery().getPID() + " "
								+ match.getTarget().getPID(),
								"[InparalogParser - parse]");
						Mapping.propgateEvent(so);
					}
				}
			}

			// get inparalogs for mainB
			Hashtable<String, Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>>> taxidB = taxidToMatches
					.get(ortho.getTaxidB());
			Hashtable<ONDEXConcept, Hashtable<ONDEXConcept, OndexMatch>> taxidBB = taxidB
					.get(ortho.getTaxidB());

			if (taxidBB.containsKey(ortho.getMainB().getConcept())) {

				// get all matches for ortho.mainB.id
				Hashtable<ONDEXConcept, OndexMatch> matchesBB = taxidBB
						.get(ortho.getMainB().getConcept());
				OndexMatch bb = matchesBB.get(ortho.getMainB().getConcept());

				if (bb != null) {
					Enumeration<OndexMatch> enu = matchesBB.elements();
					while (enu.hasMoreElements()) {
						OndexMatch match = enu.nextElement();

						// score of inparalog greater then mainScore
						if (match.getScore() > ortho.getScore()
								&& !match.getTarget().equals(ortho.getMainB().getConcept())) {
							Inparalog inpara = new Inparalog(match.getTarget(),
									match.getTargetTaxId());
							inpara.setConfidence(100.0
									* (match.getScore() - ortho.getScore())
									/ (bb.getScore() - ortho.getScore()));
							ortho.inB.add(inpara);
						}
					}
				} else {
					GeneralOutputEvent so = new GeneralOutputEvent(
							"missing selfmatch for " + ortho.getTaxidB() + " "
									+ ortho.getMainB().getConcept().getPID(),
							"[InparalogParser - parse]");
					Mapping.propgateEvent(so);
				}
			}
		}

		return orthos;
	}
}
