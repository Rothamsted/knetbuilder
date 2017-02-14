/*
 * Created on 27-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.path;

import com.sleepycat.persist.EntityCursor;

import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg56.GenomeParser;
import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.parser.kegg56.Parser;
import net.sourceforge.ondex.parser.kegg56.data.Entry;
import net.sourceforge.ondex.parser.kegg56.data.Pathway;
import net.sourceforge.ondex.parser.kegg56.sink.Concept;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptName;
import net.sourceforge.ondex.parser.kegg56.sink.Relation;
import net.sourceforge.ondex.parser.kegg56.util.DPLPersistantSet;
import net.sourceforge.ondex.parser.kegg56.util.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author taubertj
 */
public class PathwayMerger {

	private Set<String> pathwaysNamesWritten = new HashSet<String>();
	private Set<String> koPathwaysWritten = new HashSet<String>();

	private ArrayList<Relation> relationsCache;
	private Util util;

	public PathwayMerger() {
		relationsCache = new ArrayList<Relation>();
		util = Parser.getUtil();
	}

	public void mergeAndWrite(DPLPersistantSet<Pathway> pathways,
			GenomeParser genomeParser) throws MetaDataMissingException, InconsistencyException {

		EntityCursor<Pathway> cursor = pathways.getCursor();
		Iterator<Pathway> itPath = cursor.iterator(); // DOES NOT COMMIT
		while (itPath.hasNext()) {
			Pathway pathway = itPath.next();
			Concept concept = new Concept(pathway.getId(), MetaData.CV_KEGG,
					MetaData.CC_PATHWAY);
			concept.setSelfContext(true);

			GenomeParser.Taxonomony taxonomy = genomeParser
					.getTaxonomony(pathway.getOrg());
			if (taxonomy != null) {
				concept.setTaxid(String.valueOf(taxonomy.getTaxNumber()));
			} else {
				System.err.println(pathway.getOrg()
						+ " is unknown kegg species");
			}

			ConceptAcc conceptAcc = new ConceptAcc(pathway.getOrg()
					+ pathway.getNumber(), concept.getElement_of());
			concept.getConceptAccs().add(conceptAcc);

			if (pathway.getImage() != null)
				concept.setDescription(pathway.getImage());
			if (pathway.getLink() != null)
				concept.setUrl(pathway.getLink());

			if (pathway.getTitle() != null) {
				ConceptName conceptName = new ConceptName(pathway.getTitle(),
						true);
				concept.getConceptNames().add(conceptName);
			}
			pathwaysNamesWritten.add(concept.getId().toUpperCase());
			util.writeConcept(concept, false);

			// these are map pathways referenced in the pathways
			for (String key : pathway.getEntries().keySet()) {
				Entry entry = pathway.getEntries().get(key);
				if (entry.getType().equals("map")
						&& entry.getName().contains("map")) {
					String koID = entry.getName().toUpperCase();
					if (!koPathwaysWritten.contains(koID)) {
						Concept mapconcept = new Concept(koID,
								MetaData.CV_KEGG, MetaData.CC_PATHWAY);
						mapconcept.setSelfContext(true);
						koPathwaysWritten.add(koID);
						mapconcept
								.setDescription("Abstract referenced KO Pathway");
						ConceptAcc mapconceptAcc = new ConceptAcc(koID,
								MetaData.CV_KEGG);
						mapconcept.getConceptAccs().add(mapconceptAcc);
						util.writeConcept(mapconcept, false);
					}
				}
			}
		}
		pathways.closeCursor(cursor);

		util.writeRelations(relationsCache);

		System.out.println("Completed Path write and Map references");
		cursor = pathways.getCursor();
		itPath = cursor.iterator(); // DOES NOT COMMIT
		while (itPath.hasNext()) {
			Pathway pathway = itPath.next();
			constructHierarchy(pathway, genomeParser);
		}
		pathways.closeCursor(cursor);
		Parser.getUtil().writeRelations(relationsCache);
	}

	private void constructHierarchy(Pathway path, GenomeParser genomeParser)
			throws MetaDataMissingException, InconsistencyException {

		for (Entry entry : path.getEntries().values()) {
			if (entry.getType().equalsIgnoreCase("map")) {
				String pathTo = entry.getName().toUpperCase().trim();

				if (pathTo == null || pathTo.length() == 0) {
					continue; // empty name tag
				} else if (pathTo.equalsIgnoreCase(path.getId().toUpperCase())
						|| pathTo.contains("TITLE:")) {
					continue; // check for self matches
				}

				String relType = MetaData.RT_ADJACENT_TO;

				Relation r = new Relation(pathTo, path.getId().toUpperCase(),
						relType);
				relationsCache.add(r);

				if (!pathwaysNamesWritten.contains(pathTo.toUpperCase())) {
					String name = entry.getGraphics().getName();
					if (name == null || name.length() == 0) {
						name = pathTo;
					}

					Concept concept = new Concept(pathTo, MetaData.CV_KEGG,
							MetaData.CC_PATHWAY);
					concept.setSelfContext(true);

					GenomeParser.Taxonomony taxonomy = genomeParser
							.getTaxonomony(path.getOrg());
					if (taxonomy != null) {
						concept.setTaxid(String
								.valueOf(taxonomy.getTaxNumber()));
					} else {
						System.err.println(path.getOrg()
								+ " is unknown kegg species");
					}

					ConceptAcc conceptAcc = new ConceptAcc(pathTo,
							MetaData.CV_KEGG);
					concept.getConceptAccs().add(conceptAcc);

					concept.setDescription(name);

					concept.setUrl(entry.getLink());

					ConceptName conceptName = new ConceptName(name, true);
					concept.getConceptNames().add(conceptName);
					pathwaysNamesWritten.add(concept.getId().toUpperCase());
					util.writeConcept(concept, false);
				}

			}
		}
	}

	/**
	 * @param hierarchyFile
	 * @param pathways
	 * @param genomeParser
	 * @throws InconsistencyException 
	 */
	public void writeReferenceMap(String hierarchyFile, List<Pathway> pathways,
			GenomeParser genomeParser) throws MetaDataMissingException, InconsistencyException {

		Iterator<Pathway> itPath = pathways.iterator();
		while (itPath.hasNext()) {
			Pathway pathway = itPath.next();
			Concept concept = new Concept(pathway.getId(), MetaData.CV_KEGG,
					MetaData.CC_PATHWAY);
			concept.setSelfContext(true);

			koPathwaysWritten.add(concept.getId());
			ConceptAcc conceptAcc = new ConceptAcc(pathway.getOrg()
					+ pathway.getNumber(), MetaData.CV_KEGG);
			concept.getConceptAccs().add(conceptAcc);

			if (pathway.getImage() != null)
				concept.setDescription("MAP Reference Pathway: "
						+ pathway.getImage());
			if (pathway.getLink() != null)
				concept.setUrl(pathway.getLink());

			if (pathway.getTitle() != null) {
				ConceptName conceptName = new ConceptName(pathway.getTitle(),
						true);
				concept.getConceptNames().add(conceptName);
			}
			util.writeConcept(concept, false);
		}

		itPath = pathways.iterator();
		while (itPath.hasNext()) {
			Pathway pathway = itPath.next();
			constructHierarchy(pathway, genomeParser);
		}

		if (hierarchyFile != null)
			parsePathwayHierarchy(hierarchyFile, pathways);

		Parser.getUtil().writeRelations(relationsCache);
	}

	private void parsePathwayHierarchy(String file, List<Pathway> pathways) {

		HashMap<String, List<String>> superToChild = new HashMap<String, List<String>>();

		String superCat = null;
		String currentChildCat = null;

		Pattern psup = Pattern.compile("^[0-9]+. ");
		Pattern pchild = Pattern.compile("^[0-9]+.[0-9]+");

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while (br.ready()) {
				String line = br.readLine().trim();

				if (line.length() == 0)
					continue;
				if (psup.matcher(line).find()) {
					superCat = line.substring(line.indexOf(" ")).trim();
					currentChildCat = null;
				} else if (pchild.matcher(line).find()) {
					currentChildCat = line.substring(line.indexOf(" ")).trim();
					if (superCat != null) {
						List<String> children = superToChild.get(superCat);
						if (children == null) {
							children = new ArrayList<String>();
							superToChild.put(superCat, children);
						}
						children.add(currentChildCat);
					}
				} else {

					if (currentChildCat != null) {
						List<String> children = superToChild
								.get(currentChildCat);
						if (children == null) {
							children = new ArrayList<String>();
							superToChild.put(currentChildCat, children);
						}
						children.add(line.trim());
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		HashMap<String, String> nameToId = new HashMap<String, String>();

		for (Pathway pathway : pathways) {
			if (pathway.getTitle() != null)
				nameToId.put(pathway.getTitle().toUpperCase().trim(), pathway
						.getId());
		}

		for (String parent : superToChild.keySet()) {
			List<String> children = superToChild.get(parent);
			for (String child : children) {
				String parentId = nameToId.get(parent.toUpperCase());
				if (parentId != null) {
					String childId = nameToId.get(child.toUpperCase());

					if (childId != null) {
						Relation r = new Relation(childId, parentId,
								MetaData.RT_MEMBER_PART_OF);
						// parent node is context for all childs
						r.addContext(parentId);
						relationsCache.add(r);
					}
				}
			}
		}
	}

}
