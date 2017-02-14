package net.sourceforge.ondex.parser.taxonomy;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.*;
import java.util.*;

/**
 * Parser for the NCBI Taxonomy database. Creates a is_a hierarchy of terms.
 * Uses names.dmp and nodes.dmp.
 *
 * @author taubertj
 * @version 28.05.2008
 */
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Parser extends ONDEXParser implements MetaData,
        ArgumentNames {

    private static final boolean DEBUG = true;

    /**
     * No validators required.
     *
     * @return null
     */
    @Override
    public String[] requiresValidators() {
        return null;
    }

    /**
     * One argument about the root node to start taxonomy tree is optional.
     *
     * @return new ArgumentDefinition[] { rootNode }
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        FileArgumentDefinition inputDir = new FileArgumentDefinition(
                FileArgumentDefinition.INPUT_DIR,
                "directory with taxonomy files", true, true, true, false);
        StringArgumentDefinition rootNode = new StringArgumentDefinition(
                ROOT_ARG, ROOT_ARG_DESC, false, "1", true);

        return new ArgumentDefinition[]{rootNode, inputDir};
    }

    @Override
    public String getName() {
        return "NCBI Taxonomy Parser";
    }

    @Override
    public String getVersion() {
        return "28.05.2008";
    }

    @Override
    public String getId() {
        return "taxonomy";
    }

    @Override
    public void start() throws InvalidPluginArgumentException {

        /**
         * check for data input directory and Files
         */


        File dir = new File((String) args
                .getUniqueValue(FileArgumentDefinition.INPUT_DIR));
        if (!dir.exists()) {
            fireEventOccurred(new DataFileMissingEvent(dir.getAbsolutePath(),
                    "[Parser - start]"));
            return;
        }

        // names.dmp
        File fileNames = new File(dir.getAbsolutePath() + File.separator
                + "names.dmp");

        // nodes.dmp
        File fileNodes = new File(dir.getAbsolutePath() + File.separator
                + "nodes.dmp");

        /**
         * check meta data
         */
        EvidenceType et = graph.getMetaData().getEvidenceType(ET);
        if (et == null) {
            fireEventOccurred(new EvidenceTypeMissingEvent(ET,
                    "[Parser - start]"));
            return;
        }

        ConceptClass cc = graph.getMetaData().getConceptClass(CC);
        if (cc == null) {
            fireEventOccurred(new ConceptClassMissingEvent(CC,
                    "[Parser - start]"));
            return;
        }

        DataSource dataSource = graph.getMetaData().getDataSource(CV);
        if (dataSource == null) {
            fireEventOccurred(new DataSourceMissingEvent(CV, "[Parser - start]"));
            return;
        }

        AttributeName taxid = graph.getMetaData().getAttributeName(TAXID);
        if (taxid == null) {
            fireEventOccurred(new AttributeNameMissingEvent(TAXID,
                    "[Parser - start]"));
            return;
        }

        AttributeName rank = graph.getMetaData().getAttributeName(RANK);
        if (rank == null) {
            fireEventOccurred(new AttributeNameMissingEvent(RANK,
                    "[Parser - start]"));
            return;
        }

        RelationType rtset = graph.getMetaData()
                .getRelationType(RTSET);
        if (rtset == null) {
            fireEventOccurred(new RelationTypeMissingEvent(RTSET,
                    "[Parser - start]"));
            return;
        }

        /**
         * check for given arguments about rootNode
         */
        List<String> rootNodes = new ArrayList<String>();
        if (args.getObjectValueArray(ROOT_ARG) != null) {
            for (String o : (String[]) args.getObjectValueArray(ROOT_ARG)) {
                rootNodes.add(o);
            }
        } else {
            rootNodes.add("1");
        }

        // taxonomy hierarchy from parent to child
        Map<String, List<String>> hierarchy = new HashMap<String, List<String>>();

        // create relations from nodes.dmp
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(fileNodes));

            // read file line by line
            while (reader.ready()) {
                String[] line = reader.readLine().split("\t|\t");

                // parent to child ordering
                if (!hierarchy.containsKey(line[2])) {
                    hierarchy.put(line[2], new ArrayList<String>());
                }
                // prevent self loop (root id)
                if (!line[2].equals(line[0]))
                    hierarchy.get(line[2]).add(line[0]);
            }
        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent(fileNodes
                    .getAbsolutePath(), "[Parser - start]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
                    "[Parser - start]"));
        }
        if (DEBUG)
            System.out.println("Finished hierarchy.");

        /**
         * this is our node filter
         */
        Set<String> nodesToVisit = new HashSet<String>();

        // iterate over parameters
        for (String rootNode : rootNodes) {

            // stores results of tree search
            List<String> founds = new ArrayList<String>();
            founds.add(rootNode);

            // perform tree search in hierarchy
            if (hierarchy.containsKey(rootNode)) {

                // enqueue all direct children of root node
                List<String> due = new ArrayList<String>();
                due.add(rootNode);
                due.addAll(hierarchy.get(rootNode));

                // here traversal steps
                int index = 1;
                while (due.size() - founds.size() > 0) {

                    // add child to processing list
                    String child = due.get(index);
                    founds.add(child);
                    index++;

                    // enqueue children, lets hope that taxonomy is not cyclic!
                    if (hierarchy.containsKey(child))
                        due.addAll(hierarchy.get(child));

                    if (DEBUG && founds.size() % 10000 == 0) {
                        System.out.println("Nodes found so far "
                                + founds.size());
                    }
                }
            }

            // add to global set
            nodesToVisit.addAll(founds);
            founds = null;
        }
        if (DEBUG)
            System.out.println("Finished search, found " + nodesToVisit.size()
                    + " hits.");

        // free up memory
        hierarchy = null;

        /**
         * now we parse all concepts with their names
         */

        // mapping of taxid to AbstactConcept
        Map<String, ONDEXConcept> concepts = new HashMap<String, ONDEXConcept>();

        // number of concepts so far
        int nb = 0;

        // create concepts from names.dmp
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(fileNames));

            // unique names
            Set<String> names = new HashSet<String>();

            // read file line by line
            while (reader.ready()) {
                String[] line = reader.readLine().split("\t|\t");

                // apply the filter
                if (nodesToVisit.contains(line[0])) {

                    // create a new ONDEXConcept for taxid and add to list
                    if (!concepts.containsKey(line[0])) {
                        ONDEXConcept c = graph.getFactory().createConcept(line[0], dataSource,
                                cc, et);
                        concepts.put(line[0], c);
                        nb++;

                        if (DEBUG && nb % 10000 == 0)
                            System.out.println("Concepts created so far " + nb);

                        // taxid as Attribute
                        c.createAttribute(taxid, line[0], false);

                        // first name is preferred
                        c.createConceptName(line[2], true);

                        // taxid also as accession, non-ambiguous
                        c.createConceptAccession(line[0], dataSource, false);

                        names.clear();
                        names.add(line[2]);
                    } else {
                        // add more synonyms
                        ONDEXConcept c = concepts.get(line[0]);
                        if (!names.contains(line[2])) {
                            c.createConceptName(line[2], false);
                            names.add(line[2]);
                        }
                    }
                }
            }

            // free up memory
            names = null;
        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent(fileNames
                    .getAbsolutePath(), "[Parser - start]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
                    "[Parser - start]"));
        }
        if (DEBUG)
            System.out.println("Finished names, created " + nb + " concepts.");

        // number of relations so far
        nb = 0;

        /**
         * A second time over the hierarchy to create relations
         */
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(fileNodes));

            // read file line by line
            while (reader.ready()) {
                String[] line = reader.readLine().split("\t|\t");

                // apply filter
                if (nodesToVisit.contains(line[0])
                        && nodesToVisit.contains(line[2])) {

                    // get concepts of relation
                    ONDEXConcept childConcept = concepts.get(line[0]);
                    ONDEXConcept parentConcept = concepts.get(line[2]);

                    // check for consistency
                    if (childConcept == null) {
                        fireEventOccurred(new InconsistencyEvent(
                                "Child concept with pid " + line[0]
                                        + " not found.", "[Parser - start]"));
                    } else if (parentConcept == null) {
                        fireEventOccurred(new InconsistencyEvent(
                                "Parent concept with pid " + line[2]
                                        + " not found.", "[Parser - start]"));
                    } else {
                        // create a new relation and add the RANK attribute
                        ONDEXRelation r = graph.getFactory().createRelation(
                                childConcept, parentConcept, rtset, et);
                        r.createAttribute(rank, line[4], false);
                        nb++;

                        if (DEBUG && nb % 10000 == 0)
                            System.out
                                    .println("Relations created so far " + nb);
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent(fileNodes
                    .getAbsolutePath(), "[Parser - start]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
                    "[Parser - start]"));
        }
        if (DEBUG)
            System.out.println("Finished nodes, created " + nb + " relations.");
    }
}
