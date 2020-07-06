package net.sourceforge.ondex.ovtk2.ui.console.functions;

import java.util.BitSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.unconnected.Filter;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

public class PoplarFunctions {

	/**
	 * Creates a report for Poplar proteins and their BLAST and HMM hits.
	 * including Intersection, Union etc.
	 */
	public static String poplarReport(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXGraphMetaData md = graph.getMetaData();

		ConceptClass ccProtein = md.getConceptClass("Protein");
		ConceptClass ccProtDomain = md.getConceptClass("ProtDomain");
		DataSource dataSourceUniprot = md.getDataSource("UNIPROTKB");
		DataSource dataSourcePoplar1 = md.getDataSource("PHYTOZOME-POPLAR");
		DataSource dataSourcePoplar2 = md.getDataSource("PlnTFDB:PHYTOZOME-POPLAR");

		RelationType rtSimSeq = md.getRelationType("h_s_s");
		RelationType rtMISP = md.getRelationType("member_of");
		Set<ONDEXConcept> view1 = graph.getConceptsOfConceptClass(ccProtein);
		Set<ONDEXConcept> view2 = graph.getConceptsOfDataSource(dataSourceUniprot);
		Set<ONDEXConcept> poplarProts = BitSetFunctions.andNot(view1, view2);

		// get h_s_s relations and their from concepts which are Poplar_JGI
		// proteins
		BitSet sbs = new BitSet();
		for (ONDEXRelation rel : graph.getRelationsOfRelationType(rtSimSeq)) {
			if ((rel.getFromConcept().getElementOf().equals(dataSourcePoplar1) || rel.getFromConcept().getElementOf().equals(dataSourcePoplar2)) && rel.getToConcept().getElementOf().equals(dataSourceUniprot)) {
				int id = rel.getFromConcept().getId();
				sbs.set(id);
			}
		}
		Set<ONDEXConcept> blastSet = BitSetFunctions.create(graph, ONDEXConcept.class, sbs);

		// get m_isp relations (hmm) and their from concepts which are
		// Poplar_JGI proteins
		BitSet sbs2 = new BitSet();
		for (ONDEXRelation rel : graph.getRelationsOfRelationType(rtMISP)) {
			if (rel.getToConcept().getOfType().equals(ccProtDomain)) {
				int id = rel.getFromConcept().getId();
				sbs2.set(id);
			}
		}
		Set<ONDEXConcept> hmmSet = BitSetFunctions.create(graph, ONDEXConcept.class, sbs2);

		int protSize = poplarProts.size();
		int blastSize = blastSet.size();
		int hmmSize = hmmSet.size();
		int orSize = BitSetFunctions.or(blastSet, hmmSet).size();
		int andSize = BitSetFunctions.and(blastSet, hmmSet).size();
		int blastNOThmm = BitSetFunctions.andNot(blastSet, hmmSet).size();
		int hmmNotblast = BitSetFunctions.andNot(hmmSet, blastSet).size();
		int noHitsSize = protSize - orSize;

		return "\nPoplar proteins in total: " + protSize + "\n" + "Proteins with NO hits: " + noHitsSize + "\n" + "Proteins with BLAST hits:" + blastSize + "\n" + "Proteins with HMM hits:" + hmmSize + "\n" + "BLAST OR HMM: " + orSize + "\n" + "BLAST AND HMM: " + andSize + "\n" + "BLAST NOT HMM: " + blastNOThmm + "\n" + "HMM NOT BLAST: " + hmmNotblast + "\n";
	}

	public static String removePoplarUniProt(OVTK2PropertiesAggregator viewer) throws InvalidPluginArgumentException {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXGraphMetaData md = graph.getMetaData();
		ConceptClass ccProt = md.getConceptClass("Protein");
		DataSource dataSourceUniProt = md.getDataSource("UNIPROTKB");
		RelationType rtSimSeq = md.getRelationType("h_s_s");

		Set<ONDEXConcept> v1 = graph.getConceptsOfConceptClass(ccProt);
		Set<ONDEXConcept> v2 = graph.getConceptsOfDataSource(dataSourceUniProt);
		Set<ONDEXConcept> protSet = BitSetFunctions.and(v1, v2);

		// delete UniProt proteins with no hits from Poplar
		int numRemovedProteins = 0;
		for (ONDEXConcept prot : protSet) {
			boolean hasHits = false;
			Set<ONDEXRelation> relSet = graph.getRelationsOfConcept(prot);
			for (ONDEXRelation rel : relSet) {
				if (rel.getOfType().equals(rtSimSeq) && !rel.getFromConcept().getElementOf().equals(dataSourceUniProt)) {
					hasHits = true;
					break;
				}
			}
			if (!hasHits) {
				graph.deleteConcept(prot.getId());
				numRemovedProteins++;
			}
		}

		Filter filter = new Filter();

		// set invisible unconnected Publication and EC concepts
		ONDEXPluginArguments fa = new ONDEXPluginArguments(filter.getArgumentDefinitions());
		fa.addOption("ConceptClassRestriction", "Publication");
		fa.addOption("ConceptClassRestriction", "EC");

		filter.setArguments(fa);
		filter.setONDEXGraph(graph);
		filter.start();

		Set<ONDEXConcept> cs = BitSetFunctions.copy(graph.getConcepts());
		cs.removeAll(filter.getVisibleConcepts());

		int numRemovedPublicationandEC = cs.size();
		graph.setVisibility(cs, false);

		return "\nRemoved UniProt proteins: " + numRemovedProteins + "\n" + "Invisible Publication and EC: " + numRemovedPublicationandEC + "\n";

	}

}
