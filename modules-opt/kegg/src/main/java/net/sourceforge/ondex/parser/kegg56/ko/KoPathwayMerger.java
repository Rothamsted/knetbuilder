/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.ko;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.parser.kegg56.Parser;
import net.sourceforge.ondex.parser.kegg56.data.Entry;
import net.sourceforge.ondex.parser.kegg56.data.Pathway;
import net.sourceforge.ondex.parser.kegg56.sink.Concept;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptName;
import net.sourceforge.ondex.parser.kegg56.sink.Relation;
import net.sourceforge.ondex.parser.kegg56.util.DPLPersistantSet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;

/**
 * @author taubertj
 */
public class KoPathwayMerger {

	private DPLPersistantSet<Pathway> pathways;
	private Map<Concept, Set<String>> koConceptToGenes;

	private Map<String, Set<Entry>> gene2KoEntries;
	private Map<String, Set<String>> ko2Genes;
	private DPLPersistantSet<Relation> relationsCache;
	private Map<String, Concept> koNamesToKoConcept;
	private Map<String, Concept> koAccessionToKoConcept;

	public KoPathwayMerger(DPLPersistantSet<Pathway> pathways,
			Map<Concept, Set<String>> koConceptToGenes,
			Map<String, Concept> koNamesToKoConcept,
			Map<String, Concept> koAccessionToKoConcept,
			DPLPersistantSet<Relation> relationsCache) {
		this.pathways = pathways;
		this.koConceptToGenes = koConceptToGenes;
		this.koNamesToKoConcept = koNamesToKoConcept;
		this.koAccessionToKoConcept = koAccessionToKoConcept;
		this.relationsCache = relationsCache;
	}

	/**
	 * @param organisms
	 *            list of KEGG organisms to parse
	 * @throws InconsistencyException
	 */
	public void merge(Set<String> organisms) throws MetaDataMissingException,
			InconsistencyException {

		final Pattern spaceSplit = Pattern.compile(" ");

		gene2KoEntries = new HashMap<String, Set<Entry>>(500);
		ko2Genes = new HashMap<String, Set<String>>(500);

		ArrayList<String> writtenIds = new ArrayList<String>(10000);

		EntityCursor<Pathway> cursor = pathways.getCursor();
		for (Pathway pathway : cursor) {
			// go through all entries and find KO concepts
			for (Entry entry : pathway.getEntries().values()) {
				if (entry.getType().equalsIgnoreCase("ortholog")) {
					String[] result = spaceSplit.split(entry.getName());
					for (String res : result) {

						res = res.toUpperCase();
						String koEqu = res;
						if (res.startsWith("KO:")) {
							koEqu = res.substring(3, res.length())
									.toUpperCase();
						}

						Concept conceptKO = koAccessionToKoConcept.get(koEqu);
						if (conceptKO == null) {
							conceptKO = koNamesToKoConcept.get(koEqu);
						}

						// get genes for KO concept
						Set<String> genes = null;
						if (conceptKO != null) {
							genes = koConceptToGenes.get(conceptKO);
						} else if (koEqu.length() > 0) {
							// try to search in all KO concept names
							conceptKO = searchFor(koEqu);
						}

						// if not found, construct abstract concept
						if (conceptKO == null && res.length() > 0) {
							conceptKO = new Concept(res, MetaData.CV_KEGG,
									MetaData.CC_KEGG_ORTHOLOG);
							conceptKO.setDescription("abstract ko concept");
						} else if (conceptKO == null) {
							continue;
						}

						// if it has an org prefix it is from a gene therefore:
						// add KO in front
						if (res.indexOf(":") != -1) {
							String org = res.substring(0, res.indexOf(":"));
							if (organisms.contains(org)) {
								conceptKO.setId("KO:" + conceptKO.getId());
							}
						}
						// if there is a link available
						if (entry.getLink() != null)
							conceptKO.setUrl(entry.getLink());

						// let entry know about its concepts
						entry.getConceptIDs().add(conceptKO.getId());
						if (!writtenIds.contains(conceptKO.getId())) {
							writtenIds.add(conceptKO.getId());
							Parser.getUtil().writeConcept(conceptKO, false);
						}

						// if there are genes for this KO concept
						if (genes != null) {

							// put all genes into ko2Genes mapping
							ko2Genes.put(conceptKO.getId().toUpperCase(), genes);

							for (String gene : genes) {
								Set<Entry> entries = gene2KoEntries.get(gene);
								if (!gene2KoEntries.containsKey(gene)) {
									entries = new HashSet<Entry>();
									gene2KoEntries.put(gene, entries);
								}

								// mapping geneId to set of KO entries
								entries.add(entry);
							}
						}
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

		Parser.getUtil().writeRelations(relationsCache);
	}

	public Map<String, Set<Entry>> getGene2KoEntries() {
		return gene2KoEntries;
	}

	public Map<String, Set<String>> getKo2Genes() {
		return ko2Genes;
	}

	private final Pattern colonSplit = Pattern.compile(":");

	/**
	 * searches for name in all concept names of all concepts
	 * 
	 * @param name
	 * @return
	 */
	private Concept searchFor(String name) {
		String search = name;
		String[] result = colonSplit.split(name);
		if (result.length > 1) {
			search = result[1];
		}

		// go through all KO concepts
		for (Concept concept : koAccessionToKoConcept.values()) {
			if (concept.getConceptNames() != null) {
				// go through all concept names
				for (ConceptName conceptName : concept.getConceptNames()) {
					if (conceptName.getName().equals(search)) {
						// clone KO concept with new id
						return concept.clone(name, MetaData.CV_KEGG,
								MetaData.CC_KEGG_ORTHOLOG);
					}
				}
			}
		}
		return null;
	}

}
