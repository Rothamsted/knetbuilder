/*
 * Created on 27-Apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.ondex.parser.kegg52.path;

import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.parser.kegg52.MetaData;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.data.Entry;
import net.sourceforge.ondex.parser.kegg52.data.Pathway;
import net.sourceforge.ondex.parser.kegg52.sink.Concept;
import net.sourceforge.ondex.parser.kegg52.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg52.sink.ConceptName;
import net.sourceforge.ondex.parser.kegg52.sink.Relation;
import net.sourceforge.ondex.parser.kegg52.util.DPLPersistantSet;
import net.sourceforge.ondex.parser.kegg52.util.TaxidMapping;
import net.sourceforge.ondex.parser.kegg52.util.Util;

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

    private HashSet<String> pathwaysNamesWritten = new HashSet<String>();
    private HashSet<String> MAPPathwaysWritten = new HashSet<String>();

    private ArrayList<Relation> relationsCache;
    private Util util;

    public PathwayMerger() {
        relationsCache = new ArrayList<Relation>();
        util = Parser.getUtil();
    }

    public void mergeAndWrite(DPLPersistantSet<Pathway> pathways) {

        EntityCursor<Pathway> cursor = pathways.getCursor();
        Iterator<Pathway> itPath = cursor.iterator(); //DOES NOT COMMIT
        while (itPath.hasNext()) {
            Pathway pathway = itPath.next();
            Concept concept = new Concept(pathway.getId(), MetaData.CV_KEGG, MetaData.CC_PATHWAY);
            concept.setSelfContext(true);
            if (TaxidMapping.getMapping().containsKey(pathway.getOrg()))
                concept.setTaxid(TaxidMapping.getMapping().get(pathway.getOrg()));

            ConceptAcc conceptAcc = new ConceptAcc(concept.getId(),
                    pathway.getOrg() + pathway.getNumber(),
                    concept.getElement_of());
            concept.getConceptAccs().add(conceptAcc);

            if (pathway.getImage() != null)
                concept.setDescription(pathway.getImage());
            if (pathway.getLink() != null)
                concept.setUrl(pathway.getLink());

            if (pathway.getTitle() != null) {
                ConceptName conceptName = new ConceptName(concept.getId(),
                        pathway.getTitle());
                conceptName.setPreferred(true);
                concept.getConceptNames().add(conceptName);
            }
            pathwaysNamesWritten.add(concept.getId().toUpperCase());
            util.writeConcept(concept);

            String mapID = ("PATH:MAP" + pathway.getId().replaceAll("[^0-9]", "")).toUpperCase();

            if (!MAPPathwaysWritten.contains(mapID)) {
                Concept mapconcept = new Concept(mapID, MetaData.CV_KEGG, MetaData.CC_PATHWAY);
                mapconcept.setSelfContext(true);
                MAPPathwaysWritten.add(mapID);
                mapconcept.setDescription("Derived MAP Pathway for " + pathway.getId());
                ConceptAcc mapconceptAcc = new ConceptAcc(concept.getId(),
                        mapID,
                        MetaData.CV_KEGG);
                mapconcept.getConceptAccs().add(mapconceptAcc);
                util.writeConcept(mapconcept);
            }
            Relation r = new Relation(concept.getId(), mapID, MetaData.RT_DERIVES_FROM);
            r.addContext(mapID);
            relationsCache.add(r);

            // these are map pathways referenced in the pathways
            for (String key : pathway.getEntries().keySet()) {
                Entry entry = pathway.getEntries().get(key);
                if (entry.getType().equals("map") && entry.getName().contains("map")) {
                    mapID = entry.getName().toUpperCase();
                    if (!MAPPathwaysWritten.contains(mapID)) {
                        Concept mapconcept = new Concept(mapID, MetaData.CV_KEGG, MetaData.CC_PATHWAY);
                        mapconcept.setSelfContext(true);
                        MAPPathwaysWritten.add(mapID);
                        mapconcept.setDescription("Abstract referenced MAP Pathway");
                        ConceptAcc mapconceptAcc = new ConceptAcc(concept.getId(),
                                mapID,
                                MetaData.CV_KEGG);
                        mapconcept.getConceptAccs().add(mapconceptAcc);
                        util.writeConcept(mapconcept);
                    }
                }
            }
        }
        pathways.closeCursor(cursor);

        util.writeRelations(relationsCache);

        System.out.println("Completed Path write and Map references");
        cursor = pathways.getCursor();
        itPath = cursor.iterator(); //DOES NOT COMMIT
        while (itPath.hasNext()) {
            Pathway pathway = itPath.next();
            constructHierarchy(pathway);
        }
        pathways.closeCursor(cursor);
        Parser.getUtil().writeRelations(relationsCache);
    }

    private void constructHierarchy(Pathway path) {

        Iterator<Entry> entIt = path.getEntries().values().iterator();
        while (entIt.hasNext()) {
            Entry entry = entIt.next();

            if (entry.getType().equalsIgnoreCase("map")) {
                String pathTo = entry.getName().toUpperCase().trim();

                if (pathTo == null || pathTo.length() == 0) {
                    continue; //empty name tag
                } else if (pathTo.equalsIgnoreCase(path.getId().toUpperCase()) || pathTo.contains("TITLE:")) {
                    continue; //check for self matches
                } else if (pathTo.contains("MAP")) {
                    continue; //do not include MAP pathways in the hierarchy
                }

                String relType = MetaData.RT_ADJACENT_TO;

                Relation r = new Relation(pathTo, path.getId().toUpperCase(), relType);
                relationsCache.add(r);

                if (!pathwaysNamesWritten.contains(pathTo.toUpperCase())) {
                    String name = entry.getGraphics().getName();
                    if (name == null || name.length() == 0) {
                        name = pathTo;
                    }

                    Concept concept = new Concept(pathTo, MetaData.CV_KEGG, MetaData.CC_PATHWAY);
                    concept.setSelfContext(true);

                    String org = TaxidMapping.getMapping().get(path.getOrg());
                    if (org != null)
                        concept.setTaxid(org);

                    ConceptAcc conceptAcc = new ConceptAcc(concept.getId(),
                            pathTo,
                            MetaData.CV_KEGG);
                    concept.getConceptAccs().add(conceptAcc);

                    concept.setDescription(name);

                    concept.setUrl(entry.getLink());

                    ConceptName conceptName = new ConceptName(concept.getId(), name);
                    conceptName.setPreferred(true);
                    concept.getConceptNames().add(conceptName);
                    pathwaysNamesWritten.add(concept.getId().toUpperCase());
                    util.writeConcept(concept);
                }

            }
        }
    }

    public void writeReferenceMap(String hierarchyFile, List<Pathway> pathways) {

        Iterator<Pathway> itPath = pathways.iterator();
        while (itPath.hasNext()) {
            Pathway pathway = itPath.next();
            Concept concept = new Concept(pathway.getId(), MetaData.CV_KEGG, MetaData.CC_PATHWAY);
            concept.setSelfContext(true);

            MAPPathwaysWritten.add(concept.getId());
            ConceptAcc conceptAcc = new ConceptAcc(concept.getId(),
                    pathway.getOrg() + pathway.getNumber(),
                    MetaData.CV_KEGG);
            concept.getConceptAccs().add(conceptAcc);

            if (pathway.getImage() != null)
                concept.setDescription("MAP Reference Pathway: " + pathway.getImage());
            if (pathway.getLink() != null)
                concept.setUrl(pathway.getLink());

            if (pathway.getTitle() != null) {
                ConceptName conceptName = new ConceptName(concept.getId(), pathway.getTitle());
                conceptName.setPreferred(true);
                concept.getConceptNames().add(conceptName);
            }
            util.writeConcept(concept);
        }

        itPath = pathways.iterator();
        while (itPath.hasNext()) {
            Pathway pathway = itPath.next();
            constructHierarchy(pathway);
        }

        if (hierarchyFile != null)
            parsePathwayHierarchy(hierarchyFile, pathways);

        Parser.getUtil().writeRelations(relationsCache);
    }


    private void parsePathwayHierarchy(String file, List<Pathway> pathways) {

        HashMap<String, ArrayList<String>> superToChild = new HashMap<String, ArrayList<String>>();

        String superCat = null;
        String currentChildCat = null;

        Pattern psup = Pattern.compile("^[0-9]+. ");
        Pattern pchild = Pattern.compile("^[0-9]+.[0-9]+");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while (br.ready()) {
                String line = br.readLine().trim();

                if (line.length() == 0) continue;
                if (psup.matcher(line).find()) {
                    superCat = line.substring(line.indexOf(" ")).trim();
                    currentChildCat = null;
                } else if (pchild.matcher(line).find()) {
                    currentChildCat = line.substring(line.indexOf(" ")).trim();
                    if (superCat != null) {
                        ArrayList<String> children = superToChild.get(superCat);
                        if (children == null) {
                            children = new ArrayList<String>();
                            superToChild.put(superCat, children);
                        }
                        children.add(currentChildCat);
                    }
                } else {

                    if (currentChildCat != null) {
                        ArrayList<String> children = superToChild.get(currentChildCat);
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

        Iterator<Pathway> itPath = pathways.iterator();
        while (itPath.hasNext()) {
            Pathway pathway = itPath.next();
            if (pathway.getTitle() != null) nameToId.put(pathway.getTitle().toUpperCase().trim(), pathway.getId());
        }

        Iterator<String> parentIt = superToChild.keySet().iterator();
        while (parentIt.hasNext()) {
            String parent = parentIt.next();
            ArrayList<String> children = superToChild.get(parent);
            Iterator<String> it = children.iterator();
            while (it.hasNext()) {
                String parentId = nameToId.get(parent.toUpperCase());
                String child = it.next();
                if (parentId != null) {

                    String childId = nameToId.get(child.toUpperCase());

                    if (childId != null) {
                        Relation r = new Relation(childId, parentId, MetaData.RT_MEMBER_PART_OF);
                        // parent node is context for all childs
                        r.addContext(parentId);
                        relationsCache.add(r);
                    }
                }
            }
        }
    }

}
