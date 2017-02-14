/*
 * Created on 19-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.relation;

import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg53.MetaData;
import net.sourceforge.ondex.parser.kegg53.Parser;
import net.sourceforge.ondex.parser.kegg53.data.Entry;
import net.sourceforge.ondex.parser.kegg53.data.Pathway;
import net.sourceforge.ondex.parser.kegg53.data.Subtype;
import net.sourceforge.ondex.parser.kegg53.sink.ConceptWriter;
import net.sourceforge.ondex.parser.kegg53.sink.Relation;
import net.sourceforge.ondex.parser.kegg53.util.DPLPersistantSet;
import net.sourceforge.ondex.parser.kegg53.util.Util;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;

/**
 * @author taubertj
 */ 
public class RelationPathwayParser {

	private static String stripEnding(String s) {
		if (s.indexOf("_GE") > -1 || s.indexOf("_PR") > -1
				|| s.indexOf("_EN") > -1) {
			return (s.substring(0, s.length() - 3)).toUpperCase();
		}
		return s;
	}

	private String currentPathway = null;

	private ConceptWriter cw;

	private DPLPersistantSet<Relation> relationsCache;

	private Util util;

	public void parseAndWrite(DPLPersistantSet<Pathway> pathways,
			DPLPersistantSet<Relation> relationsCache)
			throws MetaDataMissingException {

		this.relationsCache = relationsCache;
		this.cw = Parser.getConceptWriter();
		this.util = Parser.getUtil();

		EntityCursor<Pathway> cursor = pathways.getCursor();
		for (Pathway pathway : cursor) {
			currentPathway = pathway.getId();
			for (net.sourceforge.ondex.parser.kegg53.data.Relation r : pathway
					.getRelations()) {
				Entry entry1 = r.getEntry1();
				Entry entry2 = r.getEntry2();

				if (r.getType().equalsIgnoreCase("ECrel")) {
					// enzyme-enzyme relation, indicating two enzymes catalyzing
					// successive reaction steps
					for (Subtype subtype : r.getSubtype()) {
						if (subtype.getName().equalsIgnoreCase("compound")) {
							// ignore
						} else if (subtype.getName().equalsIgnoreCase(
								"hidden compound")) {
							// ignore
						} else if (subtype.getName().equalsIgnoreCase(
								"indirect effect"))
							processIndirect_byECrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"activation"))
							processActivated_byECrel(entry1, entry2);
						else
							System.out.println("ECrel " + subtype.getName()
									+ " as a subtype has been ignored");
					}

				} else if (r.getType().equalsIgnoreCase("GErel")) {
					// gene expression interaction, indicating relation of
					// transcription factor and target gene product
					for (Subtype subtype : r.getSubtype()) {
						if (subtype.getName().equalsIgnoreCase("expression"))
							processExpressed_byGErel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"repression"))
							processRepressed_byGErel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"indirect effect"))
							processIndirect_byGErel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"missing interaction"))
							processInteraction_MissingGErel(entry1, entry2);
						else
							System.out.println("GErel " + subtype.getName()
									+ " as a subtype has been ignored");
					}

				} else if (r.getType().equalsIgnoreCase("PCrel")) {
					// protein-compound interaction
					if (r.getSubtype().size() == 0) {
						// protein compound interaction there is no subtype
						processInteracts_withPCrel(entry1, entry2);
					} else {
						// get subtype of relation
						for (Subtype subtype : r.getSubtype()) {
							if (subtype.getName().equalsIgnoreCase("compound")) {
								// ignore
							} else if (subtype.getName().equalsIgnoreCase(
									"activation"))
								processActivated_byPCrel(entry1, entry2);
							else if (subtype.getName().equalsIgnoreCase(
									"inhibition"))
								processInhibited_byPCrel(entry1, entry2);
							else if (subtype.getName().equalsIgnoreCase(
									"missing interaction"))
								processInteraction_MissingPCrel(entry1, entry2);
							else if (subtype.getName().equalsIgnoreCase(
									"binding/association"))
								processBinds_toPCrel(entry1, entry2);
							else if (subtype.getName().equalsIgnoreCase(
									"phosphorylation"))
								processPhosphorylated_byPCrel(entry1, entry2);
							else if (subtype.getName().equalsIgnoreCase(
									"indirect effect"))
								processIndirect_byPCrel(entry1, entry2);
							else if (subtype.getName().equalsIgnoreCase(
									"dissociation"))
								processDissociated_fromPCrel(entry1, entry2);
							else if (subtype.getName().equalsIgnoreCase(
									"state change"))
								processStatechange_fromPCrel(entry1, entry2);
							else
								System.out.println("PCrel " + subtype.getName()
										+ " as a subtype has been ignored");
						}
					}

				} else if (r.getType().equalsIgnoreCase("PPrel")) {
					// protein-protein interaction, such as binding and
					// modification
					for (Subtype subtype : r.getSubtype()) {
						if (subtype.getName().equalsIgnoreCase("compound")) {
							// ignore
						} else if (subtype.getName().equalsIgnoreCase(
								"activation"))
							processActivated_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"expression"))
							processExpressed_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"inhibition"))
							processInhibited_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"missing interaction"))
							processInteraction_MissingPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"phosphorylation"))
							processPhosphorylated_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"dephosphorylation"))
							processDephosphorylated_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"indirect effect"))
							processIndirect_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"dissociation"))
							processDissociated_fromPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"binding/association"))
							processBinds_toPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"methylation"))
							processMethylated_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"glycosylation"))
							processGlycosylated_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"demethylation"))
							processDemethylated_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"ubiquitination")
								|| subtype.getName().equals("ubiquination"))
							processUbiquinated_byPPrel(entry1, entry2);
						else if (subtype.getName().equalsIgnoreCase(
								"state change")
								|| subtype.getName().equals("state"))
							processStatechange_fromPPrel(entry1, entry2);
						else
							System.out.println("PPrel " + subtype.getName()
									+ " as a subtype has been ignored");
					}
				} else if (r.getType().equalsIgnoreCase("maplink")) {
					for (Subtype subtype : r.getSubtype()) {
						// maplink between map and enzyme
						if (subtype.getName().equalsIgnoreCase("compound")) {
							// ignore
						} else
							System.out.println("maplink " + subtype.getName()
									+ " as a subtype has been ignored");
					}
				}
			}
			try {
				cursor.update(pathway);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		pathways.closeCursor(cursor);

		System.out.println("Writing relations");
		util.writeRelations(relationsCache);

		cw = null;
		util = null;
	}

	private void processActivated_byECrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be an enzyme
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_EN";
			if (!cw.conceptParserIDIsWritten(fromID))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_EN";
					if (!cw.conceptParserIDIsWritten(toID))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// ac_by between enzyme and compound
						Relation ac_by = new Relation(fromID, toID,
								MetaData.RT_ACTIVATED_BY);
						ac_by.setFrom_element_of(MetaData.CV_KEGG);
						ac_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(ac_by.pk)) {
							ac_by = relationsCache.get(ac_by.pk);
						}
						ac_by.addContext(currentPathway);
						relationsCache.add(ac_by);
					} else {
						System.out.println(toID
								+ " not found (to). processActivated_byECrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processActivated_byECrel");
			}
		}
	}

	private void processActivated_byPCrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// ac_by between protein and compound
						Relation ac_by = new Relation(fromID, toID,
								MetaData.RT_ACTIVATED_BY);
						ac_by.setFrom_element_of(MetaData.CV_KEGG);
						ac_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(ac_by.pk)) {
							ac_by = relationsCache.get(ac_by.pk);
						}
						ac_by.addContext(currentPathway);
						relationsCache.add(ac_by);
					} else {
						System.out.println(toID
								+ " not found (to). processActivated_byPCrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processActivated_byPCrel");
			}
		}
	}

	private void processActivated_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// ac_by between two proteins
						Relation ac_by = new Relation(fromID, toID,
								MetaData.RT_ACTIVATED_BY);
						ac_by.setFrom_element_of(MetaData.CV_KEGG);
						ac_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(ac_by.pk)) {
							ac_by = relationsCache.get(ac_by.pk);
						}
						ac_by.addContext(currentPathway);
						relationsCache.add(ac_by);
					} else {
						System.out.println(toID
								+ " not found (to). processActivated_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processActivated_byPPrel");
			}
		}
	}

	private void processBinds_toPCrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// bi_to between protein and compound
						Relation bi_to = new Relation(fromID, toID,
								MetaData.RT_BINDS_TO);
						bi_to.setFrom_element_of(MetaData.CV_KEGG);
						bi_to.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(bi_to.pk)) {
							bi_to = relationsCache.get(bi_to.pk);
						}
						bi_to.addContext(currentPathway);
						relationsCache.add(bi_to);
					} else {
						System.out.println(toID
								+ " not found (to). processBinds_toPCrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processBinds_toPCrel");
			}
		}
	}

	private void processBinds_toPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// bi_to between two proteins
						Relation bi_to = new Relation(fromID, toID,
								MetaData.RT_BINDS_TO);
						bi_to.setFrom_element_of(MetaData.CV_KEGG);
						bi_to.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(bi_to.pk)) {
							bi_to = relationsCache.get(bi_to.pk);
						}
						bi_to.addContext(currentPathway);
						relationsCache.add(bi_to);
					} else {
						System.out.println(toID
								+ " not found (to). processBinds_toPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processBinds_toPPrel");
			}
		}
	}

	private void processDemethylated_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// dm_by between two proteins
						Relation dm_by = new Relation(fromID, toID,
								MetaData.RT_DEMETHYLATED_BY);
						dm_by.setFrom_element_of(MetaData.CV_KEGG);
						dm_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(dm_by.pk)) {
							dm_by = relationsCache.get(dm_by.pk);
						}
						dm_by.addContext(currentPathway);
						relationsCache.add(dm_by);
					} else {
						System.out
								.println(toID
										+ " not found (to). processDemethylated_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processDemethylated_byPPrel");
			}
		}
	}

	private void processDephosphorylated_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// de_by between two proteins
						Relation de_by = new Relation(fromID, toID,
								MetaData.RT_DEPHOSPHORYLATED_BY);
						de_by.setFrom_element_of(MetaData.CV_KEGG);
						de_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(de_by.pk)) {
							de_by = relationsCache.get(de_by.pk);
						}
						de_by.addContext(currentPathway);
						relationsCache.add(de_by);
					} else {
						System.out
								.println(toID
										+ " not found (to). processDephosphorylated_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processDephosphorylated_byPPrel");
			}
		}
	}

	private void processDissociated_fromPCrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// di_fr between protein and compound
						Relation di_fr = new Relation(fromID, toID,
								MetaData.RT_DISSOCIATED_FROM);
						di_fr.setFrom_element_of(MetaData.CV_KEGG);
						di_fr.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(di_fr.pk)) {
							di_fr = relationsCache.get(di_fr.pk);
						}
						di_fr.addContext(currentPathway);
						relationsCache.add(di_fr);
					} else {
						System.out
								.println(toID
										+ " not found (to). processDissociated_fromPCrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processDissociated_fromPCrel");
			}
		}
	}

	private void processDissociated_fromPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// di_fr between two proteins
						Relation di_fr = new Relation(fromID, toID,
								MetaData.RT_DISSOCIATED_FROM);
						di_fr.setFrom_element_of(MetaData.CV_KEGG);
						di_fr.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(di_fr.pk)) {
							di_fr = relationsCache.get(di_fr.pk);
						}
						di_fr.addContext(currentPathway);
						relationsCache.add(di_fr);
					} else {
						System.out
								.println(toID
										+ " not found (to). processDissociated_fromPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processDissociated_fromPPrel");
			}
		}
	}

	private void processExpressed_byGErel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a gene
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_GE";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// ex_by between gene and protein
						Relation ex_by = new Relation(fromID, toID,
								MetaData.RT_EXPRESSED_BY);
						ex_by.setFrom_element_of(MetaData.CV_KEGG);
						ex_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(ex_by.pk)) {
							ex_by = relationsCache.get(ex_by.pk);
						}
						ex_by.addContext(currentPathway);
						relationsCache.add(ex_by);
					} else {
						System.out.println(toID
								+ " not found (to). processExpressed_byGErel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processExpressed_byGErel");
			}
		}
	}

	private void processExpressed_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// ex_by between two proteins
						Relation ex_by = new Relation(fromID, toID,
								MetaData.RT_EXPRESSED_BY);
						ex_by.setFrom_element_of(MetaData.CV_KEGG);
						ex_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(ex_by.pk)) {
							ex_by = relationsCache.get(ex_by.pk);
						}
						ex_by.addContext(currentPathway);
						relationsCache.add(ex_by);
					} else {
						System.out.println(toID
								+ " not found (to). processExpressed_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processExpressed_byPPrel");
			}
		}
	}

	private void processGlycosylated_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// ac_by between two proteins
						Relation gl_by = new Relation(fromID, toID,
								MetaData.RT_GLYCOSYLATED_BY);
						gl_by.setFrom_element_of(MetaData.CV_KEGG);
						gl_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(gl_by.pk)) {
							gl_by = relationsCache.get(gl_by.pk);
						}
						gl_by.addContext(currentPathway);
						relationsCache.add(gl_by);
					} else {
						System.out
								.println(toID
										+ " not found (to). processGlycosylated_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processGlycosylated_byPPrel");
			}
		}
	}

	private void processIndirect_byGErel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a gene
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_GE";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// id_by between two proteins
						Relation id_by = new Relation(fromID, toID,
								MetaData.RT_INDIRECTLY_EFFECTED_BY);
						id_by.setFrom_element_of(MetaData.CV_KEGG);
						id_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(id_by.pk)) {
							id_by = relationsCache.get(id_by.pk);
						}
						id_by.addContext(currentPathway);
						relationsCache.add(id_by);
					} else {
						System.out.println(toID
								+ " not found (to). processIndirect_byGErel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processIndirect_byGErel");
			}
		}
	}

	private void processIndirect_byECrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a enzyme
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_EN";
			if (!cw.conceptParserIDIsWritten(fromID))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_EN";
					if (!cw.conceptParserIDIsWritten(toID))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// id_by between enzyme and compound
						Relation id_by = new Relation(fromID, toID,
								MetaData.RT_INDIRECTLY_EFFECTED_BY);
						id_by.setFrom_element_of(MetaData.CV_KEGG);
						id_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(id_by.pk)) {
							id_by = relationsCache.get(id_by.pk);
						}
						id_by.addContext(currentPathway);
						relationsCache.add(id_by);
					} else {
						System.out.println(toID
								+ " not found (to). processIndirect_byECrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processIndirect_byECrel");
			}
		}
	}

	private void processIndirect_byPCrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// id_by between protein and compound
						Relation id_by = new Relation(fromID, toID,
								MetaData.RT_INDIRECTLY_EFFECTED_BY);
						id_by.setFrom_element_of(MetaData.CV_KEGG);
						id_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(id_by.pk)) {
							id_by = relationsCache.get(id_by.pk);
						}
						id_by.addContext(currentPathway);
						relationsCache.add(id_by);
					} else {
						System.out.println(toID
								+ " not found (to). processIndirect_byPCrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processIndirect_byPCrel");
			}
		}
	}

	private void processIndirect_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// id_by between two proteins
						Relation id_by = new Relation(fromID, toID,
								MetaData.RT_INDIRECTLY_EFFECTED_BY);
						id_by.setFrom_element_of(MetaData.CV_KEGG);
						id_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(id_by.pk)) {
							id_by = relationsCache.get(id_by.pk);
						}
						id_by.addContext(currentPathway);
						relationsCache.add(id_by);
					} else {
						System.out.println(toID
								+ " not found (to). processIndirect_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processIndirect_byPPrel");
			}
		}
	}

	private void processInhibited_byPCrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// in_by between protein and compound
						Relation in_by = new Relation(fromID, toID,
								MetaData.RT_INHIBITED_BY);
						in_by.setFrom_element_of(MetaData.CV_KEGG);
						in_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(in_by.pk)) {
							in_by = relationsCache.get(in_by.pk);
						}
						in_by.addContext(currentPathway);
						relationsCache.add(in_by);
					} else {
						System.out.println(toID
								+ " not found (to). processInhibited_byPCrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processInhibited_byPCrel");
			}
		}
	}

	private void processInhibited_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// in_by between two proteins
						Relation in_by = new Relation(fromID, toID,
								MetaData.RT_INHIBITED_BY);
						in_by.setFrom_element_of(MetaData.CV_KEGG);
						in_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(in_by.pk)) {
							in_by = relationsCache.get(in_by.pk);
						}
						in_by.addContext(currentPathway);
						relationsCache.add(in_by);
					} else {
						System.out.println(toID
								+ " not found (to). processInhibited_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processInhibited_byPPrel");
			}
		}
	}

	private void processInteraction_MissingGErel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a gene
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_GE";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// it_mi between gene and protein
						Relation it_mi = new Relation(fromID, toID,
								MetaData.RT_MISSING_INTERACTION);
						it_mi.setFrom_element_of(MetaData.CV_KEGG);
						it_mi.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(it_mi.pk)) {
							it_mi = relationsCache.get(it_mi.pk);
						}
						it_mi.addContext(currentPathway);
						relationsCache.add(it_mi);
					} else {
						System.out
								.println(toID
										+ " not found (to). processInteraction_MissingGErel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processInteraction_MissingGErel");
			}
		}
	}

	private void processInteraction_MissingPCrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// it_mi between protein and compound
						Relation it_mi = new Relation(fromID, toID,
								MetaData.RT_MISSING_INTERACTION);
						it_mi.setFrom_element_of(MetaData.CV_KEGG);
						it_mi.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(it_mi.pk)) {
							it_mi = relationsCache.get(it_mi.pk);
						}
						it_mi.addContext(currentPathway);
						relationsCache.add(it_mi);
					} else {
						System.out
								.println(toID
										+ " not found (to). processInteraction_MissingPCrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processInteraction_MissingPCrel");
			}
		}
	}

	private void processInteraction_MissingPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// it_mi between two proteins
						Relation it_mi = new Relation(fromID, toID,
								MetaData.RT_MISSING_INTERACTION);
						it_mi.setFrom_element_of(MetaData.CV_KEGG);
						it_mi.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(it_mi.pk)) {
							it_mi = relationsCache.get(it_mi.pk);
						}
						it_mi.addContext(currentPathway);
						relationsCache.add(it_mi);
					} else {
						System.out
								.println(toID
										+ " not found (to). processInteraction_MissingPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processInteraction_MissingPPrel");
			}
		}
	}

	private void processInteracts_withPCrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// it_wi between protein and compound
						Relation it_wi = new Relation(fromID, toID,
								MetaData.RT_INTERACTS_WITH);
						it_wi.setFrom_element_of(MetaData.CV_KEGG);
						it_wi.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(it_wi.pk)) {
							it_wi = relationsCache.get(it_wi.pk);
						}
						it_wi.addContext(currentPathway);
						relationsCache.add(it_wi);
					} else {
						System.out
								.println(toID
										+ " not found (to). processInteracts_withPCrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processInteracts_withPCrel");
			}
		}
	}

	private void processMethylated_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// me_by between two proteins
						Relation me_by = new Relation(fromID, toID,
								MetaData.RT_METHYLATED_BY);
						me_by.setFrom_element_of(MetaData.CV_KEGG);
						me_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(me_by.pk)) {
							me_by = relationsCache.get(me_by.pk);
						}
						me_by.addContext(currentPathway);
						relationsCache.add(me_by);
					} else {
						System.out.println(toID
								+ " not found (to). processMethylated_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processMethylated_byPPrel");
			}
		}
	}

	private void processPhosphorylated_byPCrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// ph_by between protein and compound
						Relation ph_by = new Relation(fromID, toID,
								MetaData.RT_PHOSPHORYLATED_BY);
						ph_by.setFrom_element_of(MetaData.CV_KEGG);
						ph_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(ph_by.pk)) {
							ph_by = relationsCache.get(ph_by.pk);
						}
						ph_by.addContext(currentPathway);
						relationsCache.add(ph_by);
					} else {
						System.out
								.println(toID
										+ " not found (to). processPhosphorylated_byPCrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processPhosphorylated_byPCrel");
			}
		}
	}

	private void processPhosphorylated_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// ph_by between two proteins
						Relation ph_by = new Relation(fromID, toID,
								MetaData.RT_PHOSPHORYLATED_BY);
						ph_by.setFrom_element_of(MetaData.CV_KEGG);
						ph_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(ph_by.pk)) {
							ph_by = relationsCache.get(ph_by.pk);
						}
						ph_by.addContext(currentPathway);
						relationsCache.add(ph_by);
					} else {
						System.out
								.println(toID
										+ " not found (to). processPhosphorylated_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processPhosphorylated_byPPrel");
			}
		}
	}

	private void processRepressed_byGErel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a gene
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_GE";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// re_by between gene and protein
						Relation re_by = new Relation(fromID, toID,
								MetaData.RT_REPRESSED_BY);
						re_by.setFrom_element_of(MetaData.CV_KEGG);
						re_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(re_by.pk)) {
							re_by = relationsCache.get(re_by.pk);
						}
						re_by.addContext(currentPathway);
						relationsCache.add(re_by);
					} else {
						System.out.println(toID
								+ " not found (to). processRepressed_byGErel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processRepressed_byGErel");
			}
		}
	}

	private void processStatechange_fromPCrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a compound
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// st_fr between protein and compound
						Relation st_fr = new Relation(fromID, toID,
								MetaData.RT_STATE_CHANGED_FROM);
						st_fr.setFrom_element_of(MetaData.CV_KEGG);
						st_fr.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(st_fr.pk)) {
							st_fr = relationsCache.get(st_fr.pk);
						}
						st_fr.addContext(currentPathway);
						relationsCache.add(st_fr);
					} else {
						System.out
								.println(toID
										+ " not found (to). processStatechange_fromPCrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processStatechange_fromPCrel");
			}
		}
	}

	private void processStatechange_fromPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// st_fr between two proteins
						Relation st_fr = new Relation(fromID, toID,
								MetaData.RT_STATE_CHANGED_FROM);
						st_fr.setFrom_element_of(MetaData.CV_KEGG);
						st_fr.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(st_fr.pk)) {
							st_fr = relationsCache.get(st_fr.pk);
						}
						st_fr.addContext(currentPathway);
						relationsCache.add(st_fr);
					} else {
						System.out
								.println(toID
										+ " not found (to). processStatechange_fromPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processStatechange_fromPPrel");
			}
		}
	}

	private void processUbiquinated_byPPrel(Entry entry1, Entry entry2) {

		// go through all concepts of entry1
		for (String entry1ID : entry1.getConceptIDs()) {

			// entry1 is supposed to be a protein
			String fromID = entry1ID.trim().toUpperCase();
			if (entry1.getType().equals("gene"))
				fromID = stripEnding(fromID) + "_PR";
			if (cw.conceptParserIDIsWritten(fromID)) {

				// go through all concepts of entry2
				for (String entry2ID : entry2.getConceptIDs()) {

					// entry2 is supposed to be a protein
					String toID = entry2ID.trim().toUpperCase();
					if (entry2.getType().equals("gene"))
						toID = stripEnding(toID) + "_PR";
					if (cw.conceptParserIDIsWritten(toID)) {

						// ub_by between two proteins
						Relation ub_by = new Relation(fromID, toID,
								MetaData.RT_UBIQUINATED_BY);
						ub_by.setFrom_element_of(MetaData.CV_KEGG);
						ub_by.setTo_element_of(MetaData.CV_KEGG);
						if (relationsCache.contains(ub_by.pk)) {
							ub_by = relationsCache.get(ub_by.pk);
						}
						ub_by.addContext(currentPathway);
						relationsCache.add(ub_by);
					} else {
						System.out
								.println(toID
										+ " not found (to). processUbiquinated_byPPrel");
					}
				}
			} else {
				System.out.println(fromID
						+ " not found (from). processUbiquinated_byPPrel");
			}
		}
	}
}
