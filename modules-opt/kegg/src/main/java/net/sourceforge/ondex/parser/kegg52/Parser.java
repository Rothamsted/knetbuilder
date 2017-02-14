package net.sourceforge.ondex.parser.kegg52;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.*;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.kegg52.args.ArgumentNames;
import net.sourceforge.ondex.parser.kegg52.args.SpeciesArgumentDefinition;
import net.sourceforge.ondex.parser.kegg52.comp.CompParser;
import net.sourceforge.ondex.parser.kegg52.comp.CompPathwayMerger;
import net.sourceforge.ondex.parser.kegg52.data.*;
import net.sourceforge.ondex.parser.kegg52.enzyme.EnzymePathwayParser;
import net.sourceforge.ondex.parser.kegg52.gene.AbreviationsParser;
import net.sourceforge.ondex.parser.kegg52.gene.GeneFilesParser;
import net.sourceforge.ondex.parser.kegg52.gene.GenePathwayParser;
import net.sourceforge.ondex.parser.kegg52.genes.GenesPathwayParser;
import net.sourceforge.ondex.parser.kegg52.ko.KoParser;
import net.sourceforge.ondex.parser.kegg52.ko.KoPathwayMerger;
import net.sourceforge.ondex.parser.kegg52.ko.KoRelationMerger;
import net.sourceforge.ondex.parser.kegg52.path.PathwayMerger;
import net.sourceforge.ondex.parser.kegg52.reaction.ReactionPathwayParser;
import net.sourceforge.ondex.parser.kegg52.relation.RelationPathwayParser;
import net.sourceforge.ondex.parser.kegg52.sink.ConceptWriter;
import net.sourceforge.ondex.parser.kegg52.sink.RelationWriter;
import net.sourceforge.ondex.parser.kegg52.sink.SequenceWriter;
import net.sourceforge.ondex.parser.kegg52.util.*;
import net.sourceforge.ondex.parser.kegg52.xml.XMLParser;

import java.io.File;
import java.util.*;

/*
 * Created on 25-Apr-2005
 *
 */

/**
 * @author taubertj, hindlem
 */
@Status(description = "To be fully tested for March 10 Release", status = StatusType.STABLE)
@DatabaseTarget(name = "KEGG", description = "The KEGG database", version = "Release 52.1, December 1, 2009", url = "http://www.genome.jp/kegg")
@DataURL(name = "KEGG genes, ligand, pathway, and brite databases",
        description = "",
        urls = {"ftp://ftp.genome.jp/pub/kegg/release/archive/kegg/52/genes.tar.gz",
                "ftp://ftp.genome.jp/pub/kegg/release/archive/kegg/52/ligand.tar.gz",
                "ftp://ftp.genome.jp/pub/kegg/release/archive/kegg/52/pathway.tar.gz",
                "?/kgml.tar.gz",
                "?/brite.tar.gz",
                "https://ondex.svn.sourceforge.net/svnroot/ondex/trunk/ondex-parent/modules/kegg/data/importdata/kegg/keggHierarchy"})
@Authors(authors = {"Matthew Hindle", "Jan Taubert"}, emails = {"matthew_hindle at users.sourceforge.net", "jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Shaochih Kuo"}, emails = {"sckuo at users.sourceforge.net"})
public class Parser extends ONDEXParser implements ArgumentNames {

    public static final boolean DEBUG = false;

    private static boolean importAllSequences = false;

    private static Parser instance;

    public static Set<String> orgs;

    // these are fillers in a species pathway from other organisms
    public static boolean parseOrthologfillers = true;

    public static String pathToGenome;

    public static String pathToKegg;

    public static String pathToTaxonomy;

    protected static Util writerUtils;

    public static ConceptWriter getConceptWriter() {
        return writerUtils.getCw();
    }

    public static String getPathToKegg() {
        return pathToKegg;
    }

    public static String getPathToTaxonomy() {
        return pathToTaxonomy;
    }

    public static RelationWriter getRelationWriter() {
        return writerUtils.getRw();
    }

    public static SequenceWriter getSequenceWriter() {
        return writerUtils.getSw();
    }

    public static Util getUtil() {
        return writerUtils;
    }

    public static boolean isImportAllSequences() {
        return importAllSequences;
    }

    public static Map<String, Set<Entry>> mergeGenes(
            Map<String, Set<Entry>> t1, Map<String, Set<Entry>> t2) {

        Map<String, Set<Entry>> parentHash;
        Map<String, Set<Entry>> mergerHash;
        if (t1.size() >= t2.size()) {
            parentHash = t1;
            mergerHash = t2;
        } else {
            parentHash = t2;
            mergerHash = t1;
        }

        Iterator<String> itMergerGene = mergerHash.keySet().iterator();
        while (itMergerGene.hasNext()) {
            String mergerGene = itMergerGene.next();
            Set<Entry> mergerSet = mergerHash.get(mergerGene);
            if (parentHash.containsKey(mergerGene)) // if its in the parent add
                // to
                parentHash.get(mergerGene).addAll(mergerSet);
            else
                parentHash.put(mergerGene, mergerSet); // else create new gene

        }
        return parentHash;
    }

    private static Set<String> parseTaxids(List<String> taxids) {
        HashSet<String> orgs = new HashSet<String>(15);

        // construct a reverse mapping taxid to org
        HashMap<String, HashSet<String>> reverseMapping = new HashMap<String, HashSet<String>>();
        Iterator<String> itMap = TaxidMapping.getMapping().keySet().iterator();
        while (itMap.hasNext()) {
            String key = itMap.next();
            String value = TaxidMapping.getMapping().get(key);
            if (!reverseMapping.containsKey(value))
                reverseMapping.put(value, new HashSet<String>());
            HashSet<String> keys = reverseMapping.get(value);
            keys.add(key);
        }

        for (String taxId : taxids) {
            if (reverseMapping.containsKey(taxId)) {
                HashSet<String> keys = reverseMapping.get(taxId);
                Iterator<String> it = keys.iterator();
                while (it.hasNext())
                    orgs.add(it.next().toLowerCase());
            } else {
                orgs.add(taxId.toLowerCase());
            }
        }
        orgs.remove("map");// this is a reference map not a species
        return orgs;
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    private boolean cleanup = true;

    private Set<File> kgmlFilenamesToParse = new HashSet<File>();

    private String pathToCompound;

    private String pathToDBAbreviations;

    private String pathToDTD;

    private String pathToGeneAbreviations;

    private String pathToGenes;

    private String pathToGlycan;

    private String pathToKGML;

    private String pathToKO;

    private String pathToXML;

    /**
     * Clean all entries in a pathway which are not referenced in relation or
     * reaction.
     *
     * @param pathwayCache all pathways
     * @throws DatabaseException
     */
    private void cleanPathways(DPLPersistantSet<Pathway> pathwayCache)
            throws DatabaseException {
        EntityCursor<Pathway> cursor = pathwayCache.getCursor();

        // iterate over all pathways
        Iterator<Pathway> it = cursor.iterator();
        while (it.hasNext()) {
            Pathway pathway = it.next();
            // extract referenced entries
            Set<String> referenced = extractReferences(pathway);
            Set<String> remove = new HashSet<String>();
            // check every entry if referenced
            for (String key : pathway.getEntries().keySet()) {
                Entry entry = pathway.getEntries().get(key);
                if (!referenced.contains(entry.getName()))
                    remove.add(key);
                    // referenced genes in protein complex, do not remove
                else if (entry.getComponents().size() > 0) {
                    for (String id : entry.getComponents().keySet())
                        remove.remove(id);
                }
            }
            // remove all non referenced entries
            for (String key : remove) {
                pathway.getEntries().remove(key);
            }
            // update persistent pathway representation
            cursor.update(pathway);
        }
        // close database cursor
        cursor.close();
    }

    /**
     * Returns the set of all IDs which is really referenced in a relation.
     *
     * @param pathway current Pathway
     * @return set of all IDs
     */
    private Set<String> extractReferences(Pathway pathway) {
        Set<String> referenced = new HashSet<String>();

        // all participants of relations are referenced
        Iterator<Relation> it = pathway.getRelations().iterator();
        while (it.hasNext()) {
            Relation r = it.next();
            // get name of entry 1 from entry list
            referenced.add(pathway.getEntries().get(r.getEntry1().getId())
                    .getName());
            // get name of entry 2 from entry list
            referenced.add(pathway.getEntries().get(r.getEntry2().getId())
                    .getName());
            for (Subtype sub : r.getSubtype()) {
                String id = sub.getValue();
                Entry entry = pathway.getEntries().get(id);
                if (entry != null)
                    referenced.add(entry.getName());
            }
        }

        // cache gene marked with a reaction
        Map<String, Entry> map = new HashMap<String, Entry>();
        for (String key : pathway.getEntries().keySet()) {
            Entry entry = pathway.getEntries().get(key);
            if (entry.getReaction() != null)
                map.put(entry.getReaction(), entry);
        }

        // all members of reaction should be included
        for (String key : pathway.getReactions().keySet()) {
            Reaction r = pathway.getReactions().get(key);
            // include additionally genes marked with this reaction
            if (map.containsKey(r.getName())) {
                referenced.add(map.get(r.getName()).getName());
            }
            for (Entry entry : r.getProducts()) {
                // product compound
                referenced.add(entry.getName());
            }
            for (Entry entry : r.getSubstrates()) {
                // substrate compound
                referenced.add(entry.getName());
            }
        }
        return referenced;
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        ArrayList<ArgumentDefinition<?>> args = new ArrayList<ArgumentDefinition<?>>();

        // Guess the path to kegg species...this is a problem we need to know th
        // args before we can work out the list of species
        args.add(new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false));
        args.add(new SpeciesArgumentDefinition());
        args.add(new BooleanArgumentDefinition(IMPORTSEQS_ARG,
                SPECIES_ARG_DESC, true, false));
        args.add(new BooleanArgumentDefinition(PATHWAY_FILLERS_ARG,
                PATHWAY_FILLERS_ARG_DESC, true, false));
        args.add(new BooleanArgumentDefinition(CLEANUP_ARG, CLEANUP_ARG_DESC,
                false, true));

        return args.toArray(new ArgumentDefinition[args.size()]);
    }

    public ONDEXPluginArguments getArguments() {
        return args;
    }

    public Set<File> getKGMLFilenamesToBeParsed() {
        return kgmlFilenamesToParse;
    }

    private List<File> getFilenames(File root) {
        ArrayList<File> files = new ArrayList<File>();
        MyFileFilter myFileFilter = new MyFileFilter("xml");
        if (root.isDirectory()) {
            File[] childs = root.listFiles(myFileFilter);
            for (File child : childs) {
                if (child.isDirectory())
                    files.addAll(getFilenames(child));
                if (child.isFile()) {
                    files.add(child);
                }
            }
        }
        return files;
    }

    public String getName() {
        return new String("KEGG parser, version 52");
    }

    public String getPathToCompound() {
        return pathToCompound;
    }

    public String getPathToDBAbreviations() {
        return pathToDBAbreviations;
    }

    public String getPathToDTD() {
        return pathToDTD;
    }

    public String getPathToGeneAbreviations() {
        return pathToGeneAbreviations;
    }

    public String getPathToGenes() {
        return pathToGenes;
    }

    public String getPathToGlycan() {
        return pathToGlycan;
    }

    public String getPathToKO() {
        return pathToKO;
    }

    public String getPathToXML() {
        return pathToXML;
    }

    public String getVersion() {
        return new String("10.04.2006");
    }

    @Override
    public String getId() {
        return "kegg52";
    }

    @Override
    public String[] requiresValidators() {
        return new String[]{};
    }

    /**
     * constructs file locations and constructs species
     */
    private void setUpParameters() throws InvalidPluginArgumentException {

        parseOrthologfillers = (Boolean) args.getUniqueValue(PATHWAY_FILLERS_ARG);

        pathToKegg = (String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR);

        pathToKGML = pathToKegg + File.separator + "kgml";
        pathToDTD = pathToKGML + File.separator + "KGML.dtd";
        pathToKO = pathToKegg + File.separator + "genes" + File.separator
                + "ko";
        pathToCompound = pathToKegg + File.separator + "ligand"
                + File.separator + "compound";
        pathToGlycan = pathToKegg + File.separator + "ligand" + File.separator
                + "glycan";

        pathToGenes = pathToKegg + File.separator + "genes";
        pathToDBAbreviations = pathToGenes + File.separator + "databases-kegg";
        pathToGeneAbreviations = pathToGenes + File.separator
                + "databases-genes";
        pathToTaxonomy = pathToKegg + File.separator + "genes" + File.separator
                + "taxonomy";
        pathToGenome = pathToKegg + File.separator + "genes" + File.separator
                + "genome";
        // parse taxids from genome file
        TaxidMapping.createTaxidMapping();

        kgmlFilenamesToParse.clear();

        List<String> specArguments = (List<String>) args
                .getObjectValueList(ArgumentNames.SPECIES_ARG);

        if (specArguments.contains(SpeciesArgumentDefinition.ALL)) {
            pathToXML = pathToKGML;
            kgmlFilenamesToParse.addAll(getFilenames(new File(pathToXML)));
        } else {
            orgs = parseTaxids(specArguments);
            Iterator<String> it = orgs.iterator();
            while (it.hasNext()) {
                String org = it.next().toLowerCase();
                pathToXML = pathToKGML + File.separator + org;
                kgmlFilenamesToParse.addAll(getFilenames(new File(pathToXML)));
            }
        }

        Boolean seq = (Boolean) args.getUniqueValue(ArgumentNames.IMPORTSEQS_ARG);
        if (seq != null && seq == true) {
            importAllSequences = true;
        }

        if (args.getOptions().containsKey(ArgumentNames.CLEANUP_ARG)) {
            cleanup = (Boolean) args.getUniqueValue(ArgumentNames.CLEANUP_ARG);
        }
    }

    public void start() throws InvalidPluginArgumentException {
        setUpParameters();

        instance = this;

        writerUtils = new Util(graph);
        writerUtils.getQueue().setDEBUG(DEBUG);

        // This is independent of other processes
        if (DEBUG)
            System.out.println("KoParser");
        KoParser koParser = new KoParser(getPathToKO());
        koParser.parse();

        BerkleyLocalEnvironment env = new BerkleyLocalEnvironment(graph);

        final DPLPersistantSet<Pathway> pathwayCache = new DPLPersistantSet<Pathway>(
                env, Pathway.class);
        DPLPersistantSet<net.sourceforge.ondex.parser.kegg52.sink.Relation> relationsCache = new DPLPersistantSet<net.sourceforge.ondex.parser.kegg52.sink.Relation>(
                env, net.sourceforge.ondex.parser.kegg52.sink.Relation.class);
        DPLPersistantSet<net.sourceforge.ondex.parser.kegg52.sink.Sequence> sequenceCache = new DPLPersistantSet<net.sourceforge.ondex.parser.kegg52.sink.Sequence>(
                env, net.sourceforge.ondex.parser.kegg52.sink.Sequence.class);

        Iterator<File> itFiles = kgmlFilenamesToParse.iterator();
        System.out.println("KGML Files to parse " + kgmlFilenamesToParse.size());

        MultiThreadQueue queue = new MultiThreadQueue(1000, this.getClass()
                .getName()); // wait for 3 seconds for threads to finish
        final XMLParser xmlParser = new XMLParser(getPathToDTD());
        if (DEBUG)
            System.out.println(kgmlFilenamesToParse.size() + " Pathways to be parsed");

        while (itFiles.hasNext()) {
            final File file = itFiles.next();
            queue.addRunnable(new Runnable() {

                public void run() {
                    Pathway pathway = xmlParser.parse(file);
                    pathwayCache.add(pathway);
                    // TODO: this one here causes IllegalAccessException:
                    if (pathwayCache.size() % kgmlFilenamesToParse.size() == 50)
                        if (DEBUG)
                            System.out.println(pathwayCache.size()
                                    + " Pathways parsed");
                }
            });
        }

        queue.waitToFinish("Parser");
        queue.finalize();

        System.out.println("Parsing Map pathways");
        List<File> mapFiles = getFilenames(new File(pathToKGML + File.separator
                + "map"));
        ArrayList<Pathway> pathways = new ArrayList<Pathway>(mapFiles.size());

        Iterator<File> mapFileIt = mapFiles.iterator();
        while (mapFileIt.hasNext()) {
            pathways.add(xmlParser.parse(mapFileIt.next()));
        }

        if (DEBUG)
            System.out.println(pathwayCache.size()
                    + " Pathways parsed in Total");

        // clean-up all unconnected entries in pathways
        if (cleanup)
            try {
                cleanPathways(pathwayCache);
            } catch (DatabaseException e) {
                e.printStackTrace();
            }

        if (DEBUG)
            System.out.println("Write and merge");
        PathwayMerger pm = new PathwayMerger();

        if (!cleanup)
            pm.writeReferenceMap(pathToKegg + File.separator + "keggHierarchy",
                    pathways);
        pm.mergeAndWrite(pathwayCache);

        CompParser compParser = new CompParser(getPathToCompound(),
                getPathToGlycan());

        // out of memory during CompPathMerge at this point last all species
        if (DEBUG)
            System.out.println("CompPathwayMerger");
        new CompPathwayMerger().mergeAndWrite(pathwayCache, compParser.parse());
        compParser = null;

        if (DEBUG)
            System.out.println("KoPathwayMerger");

        KoPathwayMerger koPathwayMerger = new KoPathwayMerger(pathwayCache,
                koParser.getKoConceptToGenes(), koParser
                        .getKoNamesToKoConcept(), koParser
                        .getKoAccessionToKoConcept(), relationsCache);
        koPathwayMerger.merge();
        // koParser.finalise();

        if (DEBUG)
            System.out.println("GenePathwayParser");
        GenePathwayParser parseGene = new GenePathwayParser(pathwayCache);
        Map<String, Set<Entry>> gene2GeneEntries = parseGene.parse();
        parseGene = null;

        if (DEBUG)
            System.out.println("mergeGenes");
        Map<String, Set<Entry>> merge = Parser.mergeGenes(gene2GeneEntries,
                koPathwayMerger.getGene2KoEntries());
        gene2GeneEntries = null;

        if (DEBUG)
            System.out.println("GeneFileParser");
        GeneFilesParser geneParser = new GeneFilesParser(orgs,
                getPathToGenes(), merge, AbreviationsParser
                        .parse(getPathToGeneAbreviations()), sequenceCache,
                relationsCache, pathwayCache);

        geneParser.parseAndWrite();
        geneParser = null;
        merge = null;

        if (DEBUG)
            System.out.println("KoRelationMerger");
        new KoRelationMerger().mergeAndWrite(koPathwayMerger.getKo2Genes());

        if (DEBUG)
            System.out.println("EnzymePathwayParser");
        new EnzymePathwayParser().parseAndWrite(pathwayCache, relationsCache);

        if (DEBUG)
            System.out.println("GenesPathwayParser");
        GenesPathwayParser.parseAndWrite(pathwayCache, relationsCache);

        if (DEBUG)
            System.out.println("ReactionPathwayParser");
        ReactionPathwayParser.parseAndWrite(pathwayCache, relationsCache);

        if (DEBUG)
            System.out.println("RelationPathwayParser");
        new RelationPathwayParser().parseAndWrite(pathwayCache, relationsCache);

        relationsCache.finalize();
        sequenceCache.finalize();

        if (DEBUG)
            System.out.println("clean up Writers");
        writerUtils.cleanup();
        pathwayCache.finalize();
        env.finalize();
        env = null;

        kgmlFilenamesToParse.clear();
    }
}
