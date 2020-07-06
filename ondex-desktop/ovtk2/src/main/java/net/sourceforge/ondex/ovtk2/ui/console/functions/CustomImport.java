package net.sourceforge.ondex.ovtk2.ui.console.functions;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createAttName;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createDataSource;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createEvidence;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createRT;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

public class CustomImport {
	/**
	 * A script to parse the data file form Miriam Gifford.
	 * 
	 * @param file
	 */
	public static final void parseData(String file, OVTK2PropertiesAggregator viewer) {

		ONDEXGraph graph = viewer.getONDEXJUNGGraph();

		try {
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			ConceptClass protein = createCC(graph, "Protein");
			ConceptClass conversion = createCC(graph, "Conversion");
			ConceptClass mirna = createCC(graph, "miRNA");
			ConceptClass comp = createCC(graph, "Comp");
			RelationType it_wi = createRT(graph, "it_wi");
			RelationType r = createRT(graph, "r");
			RelationType re_by = createRT(graph, "re_by");
			RelationType ac_by = createRT(graph, "ac_by");
			RelationType rg_by = createRT(graph, "rg_by");
			RelationType cat_by = createRT(graph, "cat_by");
			RelationType cs_by = createRT(graph, "cs_by");
			RelationType pd_by = createRT(graph, "pd_by");
			RelationType transported_by = createRT(graph, "transported_by");
			RelationType coex = createRT(graph, "coex");
			AttributeName correlation = createAttName(graph, "Correlation", Double.class);
			AttributeName type = createAttName(graph, "Type of conversion", String.class);
			AttributeName interologSource = createAttName(graph, "Introlog source", String.class);
			DataSource tair = createDataSource(graph, "TAIR");
			DataSource transfac = createDataSource(graph, "TRANSFAC");
			DataSource agris = createDataSource(graph, "AGRIS");
			DataSource bind = createDataSource(graph, "BIND");
			DataSource kegg = createDataSource(graph, "KEGG");
			DataSource uc = createDataSource(graph, "UC");
			EvidenceType orthology = createEvidence(graph, "Orthology");
			EvidenceType impd = createEvidence(graph, "IMPD");
			EvidenceType carr = createEvidence(graph, "Jim Carrington's group predictions");
			EvidenceType geneways = createEvidence(graph, "GeneWays analysis of literature");
			EvidenceType manual = createEvidence(graph, "manual curation");
			Set<String> metabolism = new HashSet<String>(Arrays.asList(new String[] { "Irc", "Irv", "Irx", "Itp" }));
			Set<String> interaction = new HashSet<String>(Arrays.asList(new String[] { "scpp", "cepp", "dmpp", "interolog:pp", "bind", "transfac:pp" }));
			Set<String> regulation = new HashSet<String>(Arrays.asList(new String[] { "regulog:pp", "suppress", "transfac:tfx", "agris:Activation", "agris:Repression" }));
			Map<String, String> translation = new HashMap<String, String>();
			translation.put("scpp", "yeast");
			translation.put("cepp", "worm");
			translation.put("dmpp", "fly");
			translation.put("interolog:pp", "unspecified");
			Map<String, Map<String, ONDEXConcept>> conversionTypes = new HashMap<String, Map<String, ONDEXConcept>>();
			conversionTypes.put("Irc", new HashMap<String, ONDEXConcept>());
			conversionTypes.put("Irv", new HashMap<String, ONDEXConcept>());
			conversionTypes.put("Irx", new HashMap<String, ONDEXConcept>());
			Map<String, String> conversionTranslation = new HashMap<String, String>();
			conversionTranslation.put("Irc", "irreversible");
			conversionTranslation.put("Irv", "reversible");
			conversionTranslation.put("Irx", "direction unknown");
			Map<String, ONDEXConcept> metabolites = new HashMap<String, ONDEXConcept>();
			Map<String, ONDEXConcept> miRNA = new HashMap<String, ONDEXConcept>();
			Map<String, ONDEXConcept> proteins = new HashMap<String, ONDEXConcept>();
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				if (metabolism.contains(data[1])) {
					if (data[1].equals("Itp")) {
						String compundData = null;
						String acc = null;
						if (isNotAtg(data[0])) {
							compundData = data[0];
							acc = data[2];
						} else if (isNotAtg(data[2])) {
							compundData = data[2];
							acc = data[0];
						} else {
							System.err.println("Transporter parsing error");
							continue;
						}
						ONDEXConcept p = proteins.get(acc);
						if (p == null) {
							p = graph.getFactory().createConcept(acc, kegg, protein, impd);
							if (isNotAtg(acc)) {
								p.createConceptName(acc.trim().toUpperCase(), true);
							} else {
								p.createConceptAccession(acc.trim().toUpperCase(), tair, true);
							}
							proteins.put(acc, p);
						}
						ONDEXConcept m = metabolites.get(compundData);
						if (m == null) {
							m = graph.getFactory().createConcept(compundData, kegg, comp, impd);
							m.createConceptName(compundData, true);
							metabolites.put(compundData, m);
						}
						graph.getFactory().createRelation(m, p, transported_by, manual);
					} else {
						Map<String, ONDEXConcept> map = conversionTypes.get(data[1]);

						if (isNotAtg(data[0])) {
							ONDEXConcept conv = map.get(data[2]);
							boolean createRealtion = false;
							if (conv == null) {
								conv = graph.getFactory().createConcept("", kegg, conversion, impd);
								conv.createAttribute(type, conversionTranslation.get(data[1]), false);
								map.put(data[2], conv);
								createRealtion = true;
							}
							ONDEXConcept p = proteins.get(data[2]);
							if (p == null) {
								p = graph.getFactory().createConcept(data[2], kegg, protein, impd);
								if (isNotAtg(data[2])) {
									p.createConceptName(data[2].trim().toUpperCase(), true);
								} else {
									p.createConceptAccession(data[2].trim().toUpperCase(), tair, true);
								}
								proteins.put(data[2], p);
							}
							if (createRealtion)
								createRealtion(graph, p, conv, cat_by, impd);
							ONDEXConcept m = metabolites.get(data[0]);
							if (m == null) {
								m = graph.getFactory().createConcept(data[0], kegg, comp, impd);
								m.createConceptName(data[0].trim().toUpperCase(), true);
								metabolites.put(data[0], m);
							}
							if (data[1].equals("Irc")) {
								createRealtion(graph, m, conv, cs_by, impd);
							} else if (data[1].equals("Irv")) {
								createRealtion(graph, m, conv, cs_by, impd);
								createRealtion(graph, conv, m, pd_by, impd);
							} else if (data[1].equals("Irx")) {
								createRealtion(graph, m, conv, r, impd);
							}
						} else if (isNotAtg(data[2])) {
							ONDEXConcept conv = map.get(data[0]);
							boolean createRealtion = false;
							if (conv == null) {
								conv = graph.getFactory().createConcept("", kegg, conversion, impd);
								conv.createAttribute(type, conversionTranslation.get(data[1]), false);
								map.put(data[0], conv);
								createRealtion = true;
							}
							ONDEXConcept p = proteins.get(data[0]);
							if (p == null) {
								p = graph.getFactory().createConcept(data[0], kegg, protein, impd);
								if (isNotAtg(data[0])) {
									p.createConceptName(data[0].trim().toUpperCase(), true);
								} else {
									p.createConceptAccession(data[0].trim().toUpperCase(), tair, true);
								}
								proteins.put(data[0], p);
							}
							if (createRealtion) {
								createRealtion(graph, p, conv, cat_by, impd);
							}

							ONDEXConcept m = metabolites.get(data[2]);
							if (m == null) {
								m = graph.getFactory().createConcept(data[2], kegg, comp, impd);
								m.createConceptName(data[2].trim().toUpperCase(), true);
								metabolites.put(data[2], m);
							}
							if (data[1].equals("Irc")) {
								createRealtion(graph, m, conv, pd_by, impd);
							} else if (data[1].equals("Irv")) {
								createRealtion(graph, m, conv, cs_by, impd);
								createRealtion(graph, m, conv, pd_by, impd);
							} else if (data[1].equals("Irx")) {
								createRealtion(graph, m, conv, r, impd);
							}
						} else {
							System.err.println("Unexpected input");
						}
					}
				} else if (interaction.contains(data[1])) {

					DataSource dataSource = uc;
					if (data[1].equals("bind")) {
						dataSource = bind;
					} else if (data[1].equals("transfac:pp")) {
						dataSource = transfac;
					}
					ONDEXConcept p = proteins.get(data[2]);
					if (p == null) {
						p = graph.getFactory().createConcept(data[2], dataSource, protein, impd);
						if (isNotAtg(data[2])) {
							p.createConceptName(data[2].trim().toUpperCase(), true);
						} else {
							p.createConceptAccession(data[2].trim().toUpperCase(), tair, true);
						}
						proteins.put(data[2], p);
					}
					ONDEXConcept p1 = proteins.get(data[0]);
					if (p1 == null) {
						p1 = graph.getFactory().createConcept(data[0], dataSource, protein, impd);
						if (isNotAtg(data[0])) {
							p1.createConceptName(data[0].trim().toUpperCase(), true);
						} else {
							p1.createConceptAccession(data[0].trim().toUpperCase(), tair, true);
						}
						proteins.put(data[0], p1);
					}
					if (data[1].equals("bind") || data[1].equals("transfac:pp")) {
						createRealtion(graph, p, p1, it_wi, impd);
					} else {
						ONDEXRelation r1 = createRealtion(graph, p, p1, it_wi, orthology);
						r1.createAttribute(interologSource, translation.get(data[1]), false);
					}
				} else if (regulation.contains(data[1])) {
					DataSource dataSource = uc;
					if (data[1].equals("transfac:tfx")) {
						dataSource = transfac;
					} else if (data[1].startsWith("agris:")) {
						dataSource = agris;
					}
					ONDEXConcept p = proteins.get(data[2]);
					if (p == null) {
						p = graph.getFactory().createConcept(data[2], dataSource, protein, impd);
						if (isNotAtg(data[2])) {
							p.createConceptName(data[2].trim().toUpperCase(), true);
						} else {
							p.createConceptAccession(data[2].trim().toUpperCase(), tair, true);
						}
						proteins.put(data[2], p);
					}
					ONDEXConcept p1 = proteins.get(data[0]);
					if (p1 == null) {
						p1 = graph.getFactory().createConcept(data[0], dataSource, protein, impd);
						if (isNotAtg(data[0])) {
							p1.createConceptName(data[0].trim().toUpperCase(), true);
						} else {
							p1.createConceptAccession(data[0].trim().toUpperCase(), tair, true);
						}
						proteins.put(data[0], p1);
					}
					if (data[1].equals("regulog:pp")) {
						createRealtion(graph, p, p1, rg_by, orthology);
					} else if (data[1].equals("suppress")) {
						createRealtion(graph, p, p1, re_by, geneways);
					} else if (data[1].equals("transfac:tfx")) {
						createRealtion(graph, p, p1, rg_by, impd);
					} else if (data[1].equals("agris:Activation")) {
						createRealtion(graph, p, p1, ac_by, impd);
					} else if (data[1].equals("agris:Repression")) {
						createRealtion(graph, p, p1, re_by, impd);
					}
				} else if (data[1].startsWith("reg")) {
					ONDEXConcept p = proteins.get(data[2]);
					if (p == null) {
						p = graph.getFactory().createConcept(data[2], uc, protein, impd);
						if (isNotAtg(data[2])) {
							p.createConceptName(data[2].trim().toUpperCase(), true);
						} else {
							p.createConceptAccession(data[2].trim().toUpperCase(), tair, true);
						}
						proteins.put(data[2], p);
					}
					ONDEXConcept p1 = proteins.get(data[0]);
					if (p1 == null) {
						p1 = graph.getFactory().createConcept(data[0], uc, protein, impd);
						if (isNotAtg(data[0])) {
							p1.createConceptName(data[0].trim().toUpperCase(), true);
						} else {
							p1.createConceptAccession(data[0].trim().toUpperCase(), tair, true);
						}
						proteins.put(data[0], p1);
					}
					ONDEXRelation r1 = createRealtion(graph, p, p1, coex, impd);

					try {
						Double value = Double.valueOf(data[1].replaceFirst("reg", ""));
						r1.createAttribute(correlation, value, false);
					} catch (NumberFormatException e) {
						System.err.println("Bad correaltion value:" + data[1]);
					}

				} else if (data[1].equals("mrr")) {
					if (isNotAtg(data[0])) {
						ONDEXConcept p = proteins.get(data[2]);
						if (p == null) {
							p = graph.getFactory().createConcept(data[2], uc, protein, carr);
							if (isNotAtg(data[2])) {
								p.createConceptName(data[2].trim().toUpperCase(), true);
							} else {
								p.createConceptAccession(data[2].trim().toUpperCase(), tair, true);
							}
							proteins.put(data[2], p);
						}
						ONDEXConcept m = miRNA.get(data[0]);
						if (m == null) {
							m = graph.getFactory().createConcept(data[0], uc, mirna, carr);
							m.createConceptAccession(data[0].trim().toUpperCase(), uc, true);
							m.createConceptName(data[0].trim(), true);
							miRNA.put(data[0], m);
						}
						createRealtion(graph, p, m, rg_by, carr);
					} else if (isNotAtg(data[2])) {
						ONDEXConcept p = proteins.get(data[0]);
						if (p == null) {
							p = graph.getFactory().createConcept(data[0], uc, protein, carr);
							if (isNotAtg(data[0])) {
								p.createConceptName(data[0].trim().toUpperCase(), true);
							} else {
								p.createConceptAccession(data[0].trim().toUpperCase(), tair, true);
							}
							proteins.put(data[0], p);
						}
						ONDEXConcept m = miRNA.get(data[2]);
						if (m == null) {
							m = graph.getFactory().createConcept(data[2], uc, mirna, carr);
							m.createConceptAccession(data[2].trim().toUpperCase(), uc, true);
							m.createConceptName(data[2].trim(), true);
							miRNA.put(data[2], m);
						}
						createRealtion(graph, p, m, rg_by, carr);
					} else {
						System.err.println("miRNA parsing error.");
						continue;
					}

				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ONDEXRelation createRealtion(ONDEXGraph graph, ONDEXConcept from, ONDEXConcept to, RelationType ofType, EvidenceType ev) {
		ONDEXRelation result = graph.getRelation(from, to, ofType);
		if (result == null) {
			result = graph.getFactory().createRelation(from, to, ofType, ev);
		}
		return result;
	}

	private static boolean isNotAtg(String s) {
		if (s.startsWith("At") && s.charAt(3) == 'g')
			return false;
		return true;
	}
}
