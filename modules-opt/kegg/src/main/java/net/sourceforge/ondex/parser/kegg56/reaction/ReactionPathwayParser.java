/*
 * Created on 19-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.reaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.parser.kegg56.Parser;
import net.sourceforge.ondex.parser.kegg56.data.Entry;
import net.sourceforge.ondex.parser.kegg56.data.Pathway;
import net.sourceforge.ondex.parser.kegg56.data.Reaction;
import net.sourceforge.ondex.parser.kegg56.sink.Concept;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptName;
import net.sourceforge.ondex.parser.kegg56.sink.Relation;
import net.sourceforge.ondex.parser.kegg56.util.DPLPersistantSet;
import net.sourceforge.ondex.parser.kegg56.util.Util;

import com.sleepycat.persist.EntityCursor;

/**
 * Parses reaction concepts from KEGG pathway maps.
 * 
 * @author taubertj
 */
public class ReactionPathwayParser {

	/**
	 * Processes all known pathways
	 * 
	 * @param pathways
	 *            cache which holds KGML pathways
	 * @param relationsCache
	 *            relation cache
	 * @throws MetaDataMissingException
	 * @throws InconsistencyException
	 */
	public void parseAndWrite(DPLPersistantSet<Pathway> pathways,
			DPLPersistantSet<Relation> relationsCache)
			throws MetaDataMissingException, InconsistencyException {

		final Pattern spaceSplit = Pattern.compile(" ");

		// write out concepts immediately
		Util util = Parser.getUtil();

		// reaction IDs already written
		Map<String, Reaction> writtenReactions = new HashMap<String, Reaction>();

		// iterate over all pathways
		EntityCursor<Pathway> cursor = pathways.getCursor();
		Iterator<Pathway> itPath = cursor.iterator();
		while (itPath.hasNext()) {

			// next pathway map
			Pathway pathway = itPath.next();
			for (Reaction reaction : pathway.getReactions().values()) {

				// represent KEGG reaction as concept prototype
				Concept concept_reaction = new Concept(reaction.getName()
						.toUpperCase(), MetaData.CV_KEGG, MetaData.CC_REACTION);

				// set description as the reversible flag
				concept_reaction.setDescription(reaction.getType());

				// concept name for reaction
				int i = 0;
				for (String name : reaction.getNames()) {
					ConceptName cn = new ConceptName(name, i == 0);
					concept_reaction.getConceptNames().add(cn);
					i++;
				}

				// any equations to be used as concept name
				if (reaction.getEquation() != null) {
					ConceptName cn = new ConceptName(reaction.getEquation(),
							i == 0);
					concept_reaction.getConceptNames().add(cn);
				}

				// add accessions to concept prototype
				ConceptAcc ca = new ConceptAcc(reaction.getName(),
						MetaData.CV_KEGG);
				ca.setAmbiguous(false);
				concept_reaction.getConceptAccs().add(ca);

				// add context to concept here
				concept_reaction.addContext(pathway.getId());

				// make sure reaction concepts are written
				if (!writtenReactions.containsKey(concept_reaction.getId())) {
					writtenReactions.put(concept_reaction.getId(), reaction);
					util.writeConcept(concept_reaction, false);
				}

				// member_of relation between pathway and reaction
				Relation m_isp = new Relation(concept_reaction.getId(),
						pathway.getId(), MetaData.RT_MEMBER_PART_OF);
				// check if relation has been already written
				if (relationsCache.contains(m_isp.pk)) {
					m_isp = relationsCache.get(m_isp.pk);
				}
				m_isp.addContext(pathway.getId());
				relationsCache.add(m_isp);

				// assign KO terms to reaction
				for (String koTerm : reaction.getKoTerms()) {

					koTerm = "KO:" + koTerm;

					// only create relations to existing concepts
					if (Parser.getUtil().getCw()
							.conceptParserIDIsWritten(koTerm)) {

						// create member_of relation between reaction and KO term
						Relation part_of = new Relation(
								concept_reaction.getId(), koTerm,
								MetaData.RT_MEMBER_PART_OF);
						if (relationsCache.contains(part_of.pk)) {
							part_of = relationsCache.get(part_of.pk);
						}
						part_of.addContext(pathway.getId());
						relationsCache.add(part_of);
					}
				}

				// assign EC terms to reaction
				for (String ecTerm : reaction.getECTerms()) {

					// make sure there is a concept for EC term
					if (!Parser.getUtil().getCw()
							.conceptParserIDIsWritten(ecTerm)) {

						// create new EC concept prototype
						Concept ec = new Concept(ecTerm, MetaData.CV_KEGG,
								MetaData.CC_EC);
						ec.setDescription("parsed ec from reaction file");

						// add concept name
						ConceptName eccn = new ConceptName(ec.getId(), false);
						if (ec.getConceptNames() == null
								|| ec.getConceptNames().size() == 0) {
							eccn.setPreferred(true);
						}
						ec.getConceptNames().add(eccn);

						// add EC number as non ambiguous accession
						ConceptAcc ecAcc = new ConceptAcc(ec.getId(),
								MetaData.CV_EC);
						ecAcc.setAmbiguous(false);
						ec.getConceptAccs().add(ecAcc);

						// write EC concept
						Parser.getUtil().writeConcept(ec);
					}

					// create catalysing class relation between reaction and EC term
					Relation cat_c = new Relation(concept_reaction.getId(),
							ecTerm, MetaData.RT_CATALYSEING_CLASS);
					if (relationsCache.contains(cat_c.pk)) {
						cat_c = relationsCache.get(cat_c.pk);
					}
					cat_c.addContext(pathway.getId());
					relationsCache.add(cat_c);
				}

				// cs_by relation for all substrates
				for (Entry substrate : reaction.getSubstrates()) {
					String[] substrates = spaceSplit.split(substrate.getName()
							.trim());
					for (String substrateName : substrates) {
						Relation cs_by = new Relation(substrateName.trim()
								.toUpperCase(), reaction.getName(),
								MetaData.RT_CONSUMED_BY);
						if (relationsCache.contains(cs_by.pk)) {
							cs_by = relationsCache.get(cs_by.pk);
						}
						cs_by.addContext(pathway.getId());
						relationsCache.add(cs_by);
					}
				}

				// pd_by relation for all products
				for (Entry product : reaction.getProducts()) {
					String[] products = spaceSplit.split(product.getName()
							.trim());
					for (String productName : products) {
						Relation pd_by = new Relation(productName.trim()
								.toUpperCase(), reaction.getName(),
								MetaData.RT_PRODUCED_BY);
						if (relationsCache.contains(pd_by.pk)) {
							pd_by = relationsCache.get(pd_by.pk);
						}
						pd_by.addContext(pathway.getId());
						relationsCache.add(pd_by);
					}
				}
			}
		}
		pathways.closeCursor(cursor);

		// iterate over all pathways again
		cursor = pathways.getCursor();
		itPath = cursor.iterator();
		while (itPath.hasNext()) {

			// next pathway map
			Pathway pathway = itPath.next();

			// go through all entries
			for (Entry entry : pathway.getEntries().values()) {

				// ca_by relation between an entry and a reaction
				if (entry.getReaction() != null) {
					String[] results = spaceSplit.split(entry.getReaction()
							.toUpperCase());
					for (String result : results) {

						// check if referred reaction is a valid code
						if (result.trim().replaceAll("[^0-9]", "").length() != 5) {
							System.err
									.println("Reaction "
											+ result
											+ " is not a valid 5 digit reaction code: ReactionPathwayParser");
							continue;
						}

						// reaction catalysed by multiple enzymes
						String[] concepts = spaceSplit.split(entry.getName()
								.toUpperCase());
						for (String concept : concepts) {

							// default _EN is for enzymes, however does not work
							// for KO instances
							String conceptID = concept + "_EN";
							if (!Parser.getUtil().getCw()
									.conceptParserIDIsWritten(conceptID)) {
								// this is to support single KO instances
								// (without _EN, _PR and _GE)
								conceptID = concept;
							}

							// make sure there is a concept for enzyme
							if (Parser.getUtil().getCw()
									.conceptParserIDIsWritten(conceptID)) {
								Relation ca_by = new Relation(result,
										conceptID, MetaData.RT_CATALYSED_BY);
								if (relationsCache.contains(ca_by.pk)) {
									ca_by = relationsCache.get(ca_by.pk);
								}
								ca_by.addContext(pathway.getId());
								relationsCache.add(ca_by);
							} else {
								System.err.println("Missing concept: "
										+ conceptID + " For Reaction");
							}
						}
					}
				}
			}
		}
		pathways.closeCursor(cursor);
		util.writeRelations(relationsCache);
	}
}
