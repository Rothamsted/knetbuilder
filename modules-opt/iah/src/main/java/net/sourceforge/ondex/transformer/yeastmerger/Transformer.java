package net.sourceforge.ondex.transformer.yeastmerger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.tools.ConsoleProgressBar;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import org.apache.log4j.Level;

/**
 * Yeast merger
 * <p/>
 * This transformer has a lot of different responsibilities:
 * - Merge Polypeptides with equal MIPS accession
 * - Merge Proteins (upstream-downstream) with equal set of MIPS accessions
 * - Merge Genes with equals MIPS accession
 * - Create missing links between polypeptides and proteins
 * - Report inconsistencies in protein classification
 *
 * @author jweile
 */
public class Transformer extends ONDEXTransformer
{

    /*
      * used metadata.
      */
    private ConceptClass ccThing, ccPP, ccProtein, ccNC, ccRNA, ccGene, ccPG;
    private DataSource dataSourceMIPS, dataSourceUnknown;
    private RelationType rtPartOf;
    private AttributeName anSources;
    private EvidenceType etTrans, etAcc;

    /**
     * fetches all required metadata.
     *
     * @throws MetaDataMissingException if the required metadata
     *                                  cannot be found in the graph.
     */
    private void fetchMD() throws MetaDataMissingException {
        ccPP = requireConceptClass("Polypeptide");
        ccProtein = requireConceptClass("Protein");
        ccNC = requireConceptClass("NucleotideFeature");
        ccRNA = requireConceptClass("RNA");
        ccThing = requireConceptClass("Thing");
        ccGene = requireConceptClass("Gene");
        ccPG = requireConceptClass("Pseudogene");

        rtPartOf = requireRelationType("is_part_of");
//		rtEq = requireRelationType("equ");

        dataSourceMIPS = requireDataSource("MIPS");
        dataSourceUnknown = requireDataSource("unknown");

        anSources = requireAttributeName("DataSources");

        etTrans = requireEvidenceType("InferredByTransformation");
        etAcc = requireEvidenceType("ACC");
    }

    /*
      * mergers
      */
    private DataMerger<String> pidMerger, annoMerger, descMerger, sourceMerger;
    private DataListMerger<EvidenceType> etMerger;
    private DataListMerger<ConceptAccession> accMerger;
    private DataListMerger<ConceptName> nameMerger;
    private DataListMerger<Attribute> attributeMerger;
    private DataListMerger<ONDEXConcept> contextMerger;

    /**
     * Initializes the required mergers.
     */
    private void initMergers() {
        pidMerger = new DataMerger<String>(graph) {
            @Override
            public String extractData(ONDEXConcept c) {
                return c.getPID();
            }
        };
        annoMerger = new DataMerger<String>(graph) {
            @Override
            public String extractData(ONDEXConcept c) {
                return c.getAnnotation();
            }
        };
        descMerger = new DataMerger<String>(graph) {
            @Override
            public String extractData(ONDEXConcept c) {
                return c.getDescription();
            }
        };
        sourceMerger = new DataMerger<String>(graph) {
            @Override
            public String extractData(ONDEXConcept c) {
                return c.getElementOf().getFullname();
            }
        };
        etMerger = new DataListMerger<EvidenceType>(graph) {
            @Override
            public Set<EvidenceType> extractDataList(ONDEXConcept c) {
                return c.getEvidence();
            }
        };
        accMerger = new DataListMerger<ConceptAccession>(graph) {
            @Override
            public Set<ConceptAccession> extractDataList(
                    ONDEXConcept c) {
                return c.getConceptAccessions();
            }
        };
        nameMerger = new DataListMerger<ConceptName>(graph) {
            @Override
            public Set<ConceptName> extractDataList(ONDEXConcept c) {
                return c.getConceptNames();
            }
        };
        attributeMerger = new DataListMerger<Attribute>(graph) {
            @Override
            public Set<Attribute> extractDataList(
                    ONDEXConcept c) {
                return c.getAttributes();
            }
        };
        contextMerger = new DataListMerger<ONDEXConcept>(graph) {
            @Override
            public Set<ONDEXConcept> extractDataList(ONDEXConcept c) {
                return c.getTags();
            }
        };
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getArgumentDefinitions()
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{

        };
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getName()
     */
    @Override
    public String getName() {
        return "Yeast merger";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return "15.07.2009";
    }

    @Override
    public String getId() {
        return "yeastmerger";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresIndexedGraph()
     */
    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresValidators()
     */
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#start()
     */
    @Override
    public void start() throws Exception {
        fetchMD();
        initMergers();

        //merge nucleotide features
        mergeByAccession(ccNC, dataSourceMIPS, false, true, true);
        //merge polypeptides
        mergeByAccession(ccPP, dataSourceMIPS, false, false, false);
        //merge proteins
        mergeByAccession(ccProtein, dataSourceMIPS, true, true, false);
        //merge RNAs
        mergeByAccession(ccRNA, dataSourceMIPS, false, false, false);

        linkByAccession(ccPP, ccProtein, rtPartOf, dataSourceMIPS);
    }


    /**
     * Merges the concepts of <code>ConceptClass cc</cc> if they have the same set of
     * accessions from the namespace <code>cv</code>.
     *
     * @param cc                 the <code>ConceptClass</code> of which concepts shall be merged.
     * @param dataSource                 the namespace of the accession, according to which concepts are merged.
     * @param multipleAccAllowed whether a concept is allowed to have more than one accession
     *                           in the given namespace.
     */
    private void mergeByAccession(ConceptClass cc, DataSource dataSource, boolean completeMatch,
                                  boolean multipleAccAllowed, boolean missingAccAllowed) {

        HashMap<String, MergeList> index = index(cc, dataSource, completeMatch,
                multipleAccAllowed, missingAccAllowed);

        logInfo("Merging " + cc.getFullname() + "...");
        ConsoleProgressBar pb = new ConsoleProgressBar(index.keySet().size());
        StringBuilder inconsistencies = new StringBuilder();
        int count = 0;
        for (String accKey : index.keySet()) {
            MergeList list = index.get(accKey);
            if (list.size() > 1 && !list.hasBeenMerged()) {
                try {
                    merge(list, accKey);
                    list.markAsMerged();
                    count++;
                } catch (InconsistencyException e) {
                    inconsistencies.append(e.getMessage()).append("\n");
                }
            }
            pb.inc(1);
        }
        pb.complete();

        logInfo(count + " sets of " + cc.getFullname() + " have been merged.");

        if (inconsistencies.length() > 0) {
            logInconsistency(inconsistencies.toString());
        }
    }

    private void merge(List<Integer> list, String groupID) throws InconsistencyException {
        ConceptClass cc = determineCC(list, groupID);

        //merge PIDs
        String pid = pidMerger.mergeStrings(list);

        //create merge concept
        ONDEXConcept merged = graph.getFactory().createConcept(pid, dataSourceUnknown, cc, etTrans);

        //merge annotations
        String anno = annoMerger.mergeStrings(list);
        if (anno != null) {
            merged.setAnnotation(anno);
        }

        //merge descriptions
        String desc = descMerger.mergeStrings(list);
        if (desc != null) {
            merged.setDescription(desc);
        }

        //merge CVs
        Collection<String> cvs = sourceMerger.mergeData(list);
        merged.createAttribute(anSources, new ArrayList<String>(cvs), false);

        //merge evidences
        for (EvidenceType et : etMerger.mergeData(list)) {
            merged.addEvidenceType(et);
        }

        //merge accessions
        for (ConceptAccession acc : accMerger.mergeData(list)) {
            merged.createConceptAccession(acc.getAccession(), acc.getElementOf(), acc.isAmbiguous());
        }

        //merge names
        for (ConceptName name : nameMerger.mergeData(list)) {
            merged.createConceptName(name.getName(), name.isPreferred());
        }

        //merge gds
        for (Attribute attribute : attributeMerger.mergeData(list)) {
            merged.createAttribute(attribute.getOfType(), attribute.getValue(), false);
        }

        //merge context
        for (ONDEXConcept co : contextMerger.mergeData(list)) {
            merged.addTag(co);
        }

        //index relations
        HashMap<RelationWrapper, HashSet<Attribute>> rel2attribute
                = new HashMap<RelationWrapper, HashSet<Attribute>>();
        HashMap<RelationWrapper, HashSet<EvidenceType>> rel2ets
                = new HashMap<RelationWrapper, HashSet<EvidenceType>>();
        for (int cid : list) {
            ONDEXConcept c = graph.getConcept(cid);
            for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                RelationWrapper w = new RelationWrapper();
                w.type = r.getOfType().getId();

                if (r.getFromConcept().equals(c)) {
                    w.target = r.getToConcept().getId();
                    w.outward = true;
                } else {
                    w.target = r.getFromConcept().getId();
                    w.outward = false;
                }

                //register gdss
                HashSet<Attribute> gdsset = rel2attribute.get(w);
                if (gdsset == null) {
                    gdsset = new HashSet<Attribute>();
                    rel2attribute.put(w, gdsset);
                }
                for (Attribute attribute : r.getAttributes()) {
                    gdsset.add(attribute);
                }

                //register evidences
                HashSet<EvidenceType> etset = rel2ets.get(w);
                if (etset == null) {
                    etset = new HashSet<EvidenceType>();
                    rel2ets.put(w, etset);
                }
                for (EvidenceType et : r.getEvidence()) {
                    etset.add(et);
                }
            }
        }

        //merge relations
        for (RelationWrapper w : rel2attribute.keySet()) {
            ONDEXConcept from = w.outward ? merged : graph.getConcept(w.target);
            ONDEXConcept to = w.outward ? graph.getConcept(w.target) : merged;
            RelationType rt = graph.getMetaData().getRelationType(w.type);
            ONDEXRelation r;
            
            r = graph.getRelation(from, to, rt);
            if (r == null) {
                r = graph.getFactory().createRelation(from, to, rt, etTrans);
            }
            
            HashSet<EvidenceType> ets = rel2ets.get(w);
            if (ets != null) {
                for (EvidenceType et : ets) {
                    r.addEvidenceType(et);
                }
            }
            HashSet<Attribute> gdss = rel2attribute.get(w);
            if (gdss != null) {
                for (Attribute attribute : gdss) {
                    r.createAttribute(attribute.getOfType(), attribute.getValue(), false);
                }
            }
        }

        //delete originals
        for (int cid : list) {
            graph.deleteConcept(cid);
        }

    }

    /**
     * Links concepts of <code>ConceptClass fromClass</code> to
     * concepts of <code>ConceptClass toClass</code> with relations
     * of <code>RelationType rt</code> if they share accessions in
     * the namespace <code>DataSource accNameSpace</code>.
     * Depending on the relation type, the complete set of accessions
     * needs to match ("equ" mode), or the only accession in the
     * from-concept needs to match at least one of the accessions in
     * the to-concept ("is_part_of" mode).
     */
    private void linkByAccession(ConceptClass fromClass,
                                 ConceptClass toClass, RelationType rt, DataSource accNameSpace) {

        boolean eqMode = !rt.equals(rtPartOf);

        HashMap<String, MergeList> fromIndex = index(fromClass, accNameSpace, eqMode, eqMode, false);
        HashMap<String, MergeList> toIndex = index(toClass, accNameSpace, eqMode, true, false);

        logInfo("Linking " + fromClass.getFullname() + " to " + toClass.getFullname() + "...");

        for (String acc : fromIndex.keySet()) {
            List<Integer> fromCids = fromIndex.get(acc);
            check(eqMode || fromCids.size() == 1,
                    "More than one " + fromClass.getFullname() +
                            " for accession " + acc + " after merging!");

            List<Integer> toCids = toIndex.get(acc);
            if (toCids != null) {
                for (int fromCid : fromCids) {
                    ONDEXConcept fromC = graph.getConcept(fromCid);
                    for (int toCid : toCids) {
                        ONDEXConcept toC = graph.getConcept(toCid);

                        ONDEXRelation r = graph.getRelation(fromC, toC, rt);
                        if (r == null) {
                            graph.getFactory().createRelation(fromC, toC, rt, etAcc);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates an index of the concepts in <code>ConceptClass cc</code>
     * according to their accessions in the namespace <code>DataSource cv</code>.
     *
     * @param setMode            determines whether an entry is created for the whole
     *                           set of accessions in a concept or for every single accession in it.
     * @param multipleAccAllowed whether more than one accession is allowed
     *                           per concept.
     * @return a <code>HashMap</code> mapping <code>String</code>s representing
     *         accessions (or accession sets, if <code>setMode == true</code>) to an
     *         <code>List<Integer></code> containing all the integer IDs of the concepts
     *         that bear the concerned accession (or set thererof).
     */
    private HashMap<String, MergeList> index(ConceptClass cc, DataSource dataSource, boolean setMode, boolean multipleAccAllowed, boolean missingAccAllowed) {
        logInfo("Indexing " + cc.getFullname() + "...");
        HashMap<String, MergeList> index = new HashMap<String, MergeList>();

        for (int cid : conceptsOfCC(cc)) {
            ONDEXConcept c = graph.getConcept(cid);
            Set<String> accs = accessionOfNamespace(c, dataSource);

            check(multipleAccAllowed || accs.size() <= 1,
                    pretty(c) + " has more than one key accession: " + accs);
            check(missingAccAllowed || accs.size() > 0,
                    pretty(c) + " has no key accession!");

            if (accs.size() == 0) {
                continue;
            }

            if (setMode) {
                String accCat = cat(accs, "|");
                accs.clear();
                accs.add(accCat);
            }

            //search for existing list
            MergeList list = null;
            for (String acc : accs) {
                MergeList returnedList = index.get(acc);
                if (returnedList != null) {
                    if (list == null) {
                        list = returnedList;
                    } else if (!list.equals(returnedList)) {
                        //merge the mergelists :(
                        list.merge(returnedList);
                    }
                }
            }

            //if not found: create
            if (list == null) {
                list = new MergeList();
            }

            //register the concept
            list.add(cid);

            //register list for all accessions
            for (String acc : accs) {
                index.put(acc, list);
            }

        }

        return index;
    }

    /**
     * determines the most specific concept class for a list of concept ids.
     *
     * @param cids the concept ids.
     * @throws InconsistencyException
     */
    private ConceptClass determineCC(List<Integer> cids, String groupID) throws InconsistencyException {
        HashSet<ConceptClass> ccs = new HashSet<ConceptClass>();
        for (int cid : cids) {
            ONDEXConcept c = graph.getConcept(cid);
            ccs.add(c.getOfType());
        }
        if (ccs.size() == 1) {
            return ccs.iterator().next();
        }

        ConceptClass curr_deepest = ccThing;
        for (ConceptClass cc : ccs) {
            if ((curr_deepest.equals(ccGene) && cc.equals(ccPG)) || (curr_deepest.equals(ccPG) && cc.equals(ccGene))) {
                curr_deepest = ccGene;
            } else {
                curr_deepest = deeper(cc, curr_deepest);
            }
            if (curr_deepest == null) {
                throw new InconsistencyException("Inconsistent concept class " +
                        "assignment in merge group " + groupID + ":\n" + prettyCs(cids));
            }
        }

        return curr_deepest;
    }

    /**
     * determines the deeper of two given concept classes in terms
     * of hierarchy.<br>
     * Worst case runtime: O(d) where d = hierarchy depth
     *
     * @param cc1
     * @param cc2
     * @return the deeper of the two, or null if on different branches.
     */
    private ConceptClass deeper(ConceptClass cc1, ConceptClass cc2) {
        if (cc1.isAssignableFrom(cc2)) {
            return cc2;
        } else if (cc1.isAssignableTo(cc2)) {
            return cc1;
        } else {
            return null;
        }
    }

    /**
     * Concatenates all members of the collection <code>ss</code>
     * using the delimiter <code>delim</code>.
     * <h4>Example:</h4>
     * ss: {"1","2","3"}<br>
     * delim: "|"<br>
     * output: "1|2|3"<br>
     *
     * @param ss    the set of strings to concatenate
     * @param delim the delimter to use
     * @return a <code>String</code> containing the above concatenation.
     */
    private String cat(Collection<String> ss, String delim) {
        if (ss.size() == 0) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (String s : ss) {
            b.append(s + delim);
        }
        b.delete(b.length() - delim.length(), b.length());
        return b.toString();
    }

    /**
     * Returns a set of strings representing all accessions of the
     * <code>ONDEXConcept c</code> that are elements of the
     * <code>DataSource cv</code>.
     */
    private Set<String> accessionOfNamespace(ONDEXConcept c, DataSource dataSource) {
        Set<String> set = new TreeSet<String>();

        for (ConceptAccession acc : c.getConceptAccessions()) {
            if (acc.getElementOf().equals(dataSource)) {
                set.add(acc.getAccession());
            }
        }

        return set;
    }

    /**
     * returns an <code>Set<Integer></code> containing the IDs instances
     * of <code>ConceptClass cc</code>.
     *
     * @param cc The concerned <code>ConceptClass</code>.
     */
    private Set<Integer> conceptsOfCC(ConceptClass cc) {
        Set<Integer> set = new TreeSet<Integer>();

        for (ONDEXConcept c : graph.getConcepts()) {
            if (c.inheritedFrom(cc)) {
                set.add(c.getId());
            }
        }

        return set;
    }


    //####Output methods####

    /**
     * Checks whether <code>b</code> holds. If not it logs an an
     * inconsistency event with the message <code>string</code>.
     *
     * @param b      the condition to check on.
     * @param string the message to log in case <code>b</code>
     *               does not hold.
     */
    private void check(boolean b, String string) {
        if (!b) {
            logInconsistency(string);
        }
    }

    /**
     * Logs an inconsistency event with message <code>s</code>.
     *
     * @param s the message to log as an inconsistency.
     */
    private void logInconsistency(String s) {
        InconsistencyEvent e = new InconsistencyEvent("\n" + s, "");
        e.setLog4jLevel(Level.DEBUG);
        fireEventOccurred(e);
    }

    /**
     * Logs the information message in <code>s</code>.
     *
     * @param s The information to log.
     */
    private void logInfo(String s) {
        GeneralOutputEvent e = new GeneralOutputEvent("\n" + s, "");
        e.setLog4jLevel(Level.INFO);
        fireEventOccurred(e);
    }

    /**
     * Returns a nicely formatted string that describes the <code>ONDEXConcept c</code>.
     * If <code>c.getId() == 1</code> and <code>c.getPID().equals("lacZ")</code>, then
     * <code>pretty(c)</code> returns <code>Concept #1 ("lacZ")</code>.
     *
     * @param c The concerned <code>ONDEXConcept</code>.
     * @return a <code>String</code> containing output described above.
     */
    private String pretty(ONDEXConcept c) {
        return c.getOfType() + " #" + c.getId() + " (" + c.getPID() + ") -- source: " + c.getElementOf();
    }

    private String prettyCs(List<Integer> cids) {
        StringBuilder b = new StringBuilder();
        for (int cid : cids) {
            ONDEXConcept c = graph.getConcept(cid);
            b.append("  * " + pretty(c) + "\n");
        }
        return b.toString();
    }

    private class RelationWrapper {
        public String type;
        public int target;
        public boolean outward;

        public boolean equals(Object o) {
            if (o instanceof RelationWrapper) {
                RelationWrapper w = (RelationWrapper) o;
                if (w.type.equals(type) &&
                        w.target == target &&
                        w.outward == outward) {
                    return true;
                }
            }
            return false;
        }
    }

    @SuppressWarnings("serial")
    private static class MergeList extends ArrayList<Integer> {
        private boolean merged = false;

        public void markAsMerged() {
            merged = true;
        }

        public void merge(MergeList otherlist) {
            for (int element : otherlist) {
                if (!this.contains(element)) {
                    this.add(element);
                }
            }
        }

        public boolean hasBeenMerged() {
            return merged;
        }

    }
}
