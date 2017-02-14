package net.sourceforge.ondex.parser.kegg53;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.*;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.kegg53.args.ArgumentNames;
import net.sourceforge.ondex.parser.kegg53.args.SpeciesArgumentDefinition;
import net.sourceforge.ondex.parser.kegg53.comp.CompParser;
import net.sourceforge.ondex.parser.kegg53.comp.CompPathwayMerger;
import net.sourceforge.ondex.parser.kegg53.data.*;
import net.sourceforge.ondex.parser.kegg53.enzyme.EnzymePathwayParser;
import net.sourceforge.ondex.parser.kegg53.gene.GeneFilesParser;
import net.sourceforge.ondex.parser.kegg53.gene.GenePathwayParser;
import net.sourceforge.ondex.parser.kegg53.gene.GenesPathwayParser;
import net.sourceforge.ondex.parser.kegg53.ko.KoParser;
import net.sourceforge.ondex.parser.kegg53.ko.KoPathwayMerger;
import net.sourceforge.ondex.parser.kegg53.ko.KoRelationMerger;
import net.sourceforge.ondex.parser.kegg53.path.PathwayMerger;
import net.sourceforge.ondex.parser.kegg53.reaction.ReactionLigandDBParser;
import net.sourceforge.ondex.parser.kegg53.reaction.ReactionPathwayParser;
import net.sourceforge.ondex.parser.kegg53.relation.RelationPathwayParser;
import net.sourceforge.ondex.parser.kegg53.sink.ConceptWriter;
import net.sourceforge.ondex.parser.kegg53.sink.RelationWriter;
import net.sourceforge.ondex.parser.kegg53.sink.SequenceWriter;
import net.sourceforge.ondex.parser.kegg53.util.BerkleyLocalEnvironment;
import net.sourceforge.ondex.parser.kegg53.util.DPLPersistantSet;
import net.sourceforge.ondex.parser.kegg53.util.Util;
import net.sourceforge.ondex.parser.kegg53.xml.XMLParser;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * @author taubertj, hindlem
 */
@Status(description = "Tested March 2010 (hindlem et al.)", status = StatusType.STABLE)
@DatabaseTarget(name = "KEGG", description = "The KEGG database", version = "Release 53.0, January 1, 2010", url = "http://www.genome.jp/kegg")
@DataURL(name = "KEGG databases",
        description = "KEGG genes, ligand, pathway, and brite databases. keggHierarchy file is optional and adds a shallow hierarchy of super pathways.",
        urls = {"ftp://ftp.genome.jp/pub/kegg/release/current/brite.tar.gz",
                "ftp://ftp.genome.jp/pub/kegg/release/current/kgml.tar.gz",
                "ftp://ftp.genome.jp/pub/kegg/release/current/pathway.tar.gz",
                "ftp://ftp.genome.jp/pub/kegg/release/current/ligand.tar.gz",
                "ftp://ftp.genome.jp/pub/kegg/release/current/genes.tar.gz",
                "ftp://ftp.genome.jp/pub/kegg/release/current/medicus.tar.gz",
                "https://ondex.svn.sourceforge.net/svnroot/ondex/trunk/ondex-parent/modules/kegg/data/importdata/kegg/keggHierarchy"})
@Authors(authors = {"Matthew Hindle", "Jan Taubert"}, emails = {"matthew_hindle at users.sourceforge.net", "jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Shaochih Kuo"}, emails = {"sckuo at users.sourceforge.net"})
public class Parser extends ONDEXParser implements ArgumentNames {
 
    public static final boolean DEBUG = true;

    protected static Util writerUtils;

    public static ConceptWriter getConceptWriter() {
        return writerUtils.getCw();
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

    private static boolean importAllSequences4Species = false;

    public static boolean isImportAllSequences4Species() {
        return importAllSequences4Species;
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

        for (String mergerGene : mergerHash.keySet()) {
            Set<Entry> mergerSet = mergerHash.get(mergerGene);
            if (parentHash.containsKey(mergerGene)) // if its in the parent add
                // to
                parentHash.get(mergerGene).addAll(mergerSet);
            else
                parentHash.put(mergerGene, mergerSet); // else create new gene

        }
        return parentHash;
    }

    /**
     * @param speciesNames can be unique common name, NCBI taxid, or kegg species code
     * @param genomeParser index of genome file
     * @return list of GenomeParser.Taxonomony
     */
    private static Set<GenomeParser.Taxonomony> parseTaxids(List<String> speciesNames, GenomeParser genomeParser) {
        Set<GenomeParser.Taxonomony> orgs = new HashSet<GenomeParser.Taxonomony>(15);

        for (String speciesName : speciesNames) {
            try {
                GenomeParser.Taxonomony taxid = genomeParser.getTaxonomony(Integer.parseInt(speciesName));
                if (taxid == null)
                    System.err.println("Unknown ncbi taxid " + speciesName + " in parseTaxids() of Parser ");
                else
                    orgs.add(taxid);
            } catch (NumberFormatException e) {
                GenomeParser.Taxonomony taxid = genomeParser.getTaxonomony(speciesName);
                if (taxid == null) {
                    taxid = genomeParser.getTaxonomonyByUniqueName(speciesName);
                    if (taxid == null)
                        System.err.println("Can not identify species name in kegg: " + speciesName);
                    else
                        orgs.add(taxid);
                } else {
                    orgs.add(taxid);
                }
            }

        }
        return orgs;
    }

    private boolean cleanup = true;


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
        for (Pathway pathway : cursor) {
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
        for (Relation r : pathway.getRelations()) {
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
        args.add(new SpeciesArgumentDefinition(SPECIES_ARG, SPECIES_ARG_DESC, true));
        args.add(new BooleanArgumentDefinition(IMPORT_SEQS_4_SPECIES_ARG,
                IMPORT_SEQS_4_SPECIES_ARG_DESC, true, false));
        args.add(new SpeciesArgumentDefinition(SPECIES_OTHO_ARG, SPECIES_OTHO_ARG_DESC, false));
        args.add(new BooleanArgumentDefinition(CLEANUP_ARG, CLEANUP_ARG_DESC,
                false, true));

        return args.toArray(new ArgumentDefinition[args.size()]);
    }

    public ONDEXPluginArguments getArguments() {
        return args;
    }

    public String getName() {
        return "KEGG parser, version 53";
    }

    public String getVersion() {
        return "09.10.2010";
    }

    @Override
    public String getId() {
        return "kegg53";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * constructs file locations and constructs species
     */
    private void setUpParameters() throws IOException, InvalidPluginArgumentException {

        Boolean seq = (Boolean) args.getUniqueValue(ArgumentNames.IMPORT_SEQS_4_SPECIES_ARG);
        if (seq != null && seq == true) {
            importAllSequences4Species = true;
        }

        if (args.getOptions().containsKey(ArgumentNames.CLEANUP_ARG)) {
            cleanup = ((Boolean) args.getUniqueValue(ArgumentNames.CLEANUP_ARG));
        }
    }

    public void start() throws Exception {

        setUpParameters();

        File inputDir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        FileRegistry fr = FileRegistry.getInstance(inputDir.getAbsolutePath());


        FileIndex genesResource = fr.getIndex(FileRegistry.DataResource.GENES);
        FileIndex kgmlResource = fr.getIndex(FileRegistry.DataResource.KGML);
        FileIndex pathwayResource = fr.getIndex(FileRegistry.DataResource.PATHWAY);
        FileIndex ligandResource = fr.getIndex(FileRegistry.DataResource.LIGAND);
        FileIndex briteResource = fr.getIndex(FileRegistry.DataResource.BRITE);
        FileIndex medicusResource = fr.getIndex(FileRegistry.DataResource.MEDICUS);

        GenomeParser genomeParser = new GenomeParser(genesResource.getFile("genome"));

        Set<GenomeParser.Taxonomony> orgs = parseTaxids((List<String>) args.getObjectValueList(ArgumentNames.SPECIES_ARG), genomeParser);

        Set<GenomeParser.Taxonomony> orthologOrgs = parseTaxids((List<String>) args.getObjectValueList(ArgumentNames.SPECIES_OTHO_ARG), genomeParser);

        writerUtils = new Util(graph, genomeParser);

        // This is independent of other processes
        if (DEBUG)
            System.out.println("KoParser");

        KoParser koParser = new KoParser(genesResource.getFile("ko"));

        // This is independent of other processes

        BerkleyLocalEnvironment env = new BerkleyLocalEnvironment(graph);

        final DPLPersistantSet<Pathway> pathwayCache = new DPLPersistantSet<Pathway>(
                env, Pathway.class);
        DPLPersistantSet<net.sourceforge.ondex.parser.kegg53.sink.Relation> relationsCache = new DPLPersistantSet<net.sourceforge.ondex.parser.kegg53.sink.Relation>(
                env, net.sourceforge.ondex.parser.kegg53.sink.Relation.class);
        DPLPersistantSet<net.sourceforge.ondex.parser.kegg53.sink.Sequence> sequenceCache = new DPLPersistantSet<net.sourceforge.ondex.parser.kegg53.sink.Sequence>(
                env, net.sourceforge.ondex.parser.kegg53.sink.Sequence.class);


        Set<String> species = getSpeciesToParse(orgs, genomeParser);
        System.out.println(species);
        Pattern kgmlRegex = getKGMLRegex(species);
        List<String> kgmlFiles = kgmlResource.getFileNames(kgmlRegex, true);
        

        ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);

        XMLParser xmlParser;

        /*
         * This needs updating to the latest version, whenever KEGG changes its DTD file.
         * There needs to be a better way for the future. 
         */
        URL url = new URL("http://www.genome.jp/kegg/xml/KGML_v0.7.1_.dtd");
        try {
            String file = File.createTempFile("KGML_v0.7.1_", ".dtd").getAbsolutePath();
            saveURL(url, file);
            xmlParser = new XMLParser(new File(file).toURI().toURL());
            //System.out.println(url+" saved to "+new File(file).toURI().toURL());
        } catch (IOException e) {
            xmlParser = new XMLParser();
            System.err.println("Unable to access " + url.toString() + " disabling validation: " + e.getMessage());
        }

        Set<Future<?>> futures = new HashSet<Future<?>>();

        for (String kgmlFile : kgmlFiles)
            futures.add(EXECUTOR.submit(
                    new KGMLJob(xmlParser, kgmlResource.getFile(kgmlFile))));

        int percentProgress = 0;
        int kgmlFilesCompleted = 0;
        for (Future<?> future : futures) {
            try {
                Pathway pathway = (Pathway) future.get();
                pathwayCache.add(pathway);
                kgmlFilesCompleted++;

                int percentComplete = Math.round(((float) kgmlFilesCompleted / (float) futures.size()) * 100f);

                if (percentComplete % 10 == 0 && percentComplete > percentProgress) {
                    percentProgress = percentComplete;
                    System.out.println(
                            "KGML pathways parsed " + percentComplete + " % (" + kgmlFilesCompleted + ")");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new Error(e);
            }
        }

        System.out.println(pathwayCache.size() + " organism pathways parsed");

        ReactionLigandDBParser reactionParser = new ReactionLigandDBParser();
        reactionParser.parse(ligandResource.getFile("reaction"));
        reactionParser.addReactionInfoToPathways(pathwayCache);

        List<String> kgmlMapFiles = kgmlResource.getFileNames(
                Pattern.compile("^ko[\\d]{5}\\.xml$"), true);

        System.out.println("Parsing Map pathways");

        ArrayList<Pathway> pathways = new ArrayList<Pathway>();

        for (String kgmlFile : kgmlMapFiles)
            futures.add(EXECUTOR.submit(
                    new KGMLJob(xmlParser, kgmlResource.getFile(kgmlFile))));

        for (Future<?> future : futures) {
            try {
                Pathway pathway = (Pathway) future.get();
                pathways.add(pathway);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new Error(e);
            }
        }

        System.out.println("Parsed KO " + pathways.size() + " pathways");

        EXECUTOR.shutdown();

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

        if (!cleanup && new File(inputDir.getAbsolutePath() + File.separator + "keggHierarchy").exists())
            pm.writeReferenceMap(inputDir.getAbsolutePath() + File.separator + "keggHierarchy",
                    pathways, genomeParser);

        pm.mergeAndWrite(pathwayCache, genomeParser);

        CompParser compParser = new CompParser(
                ligandResource.getFile("compound"),
                ligandResource.getFile("glycan"), 
                medicusResource.getFile("drug"));

        // out of memory during CompPathMerge at this point last all species
        if (DEBUG)
            System.out.println("CompPathwayMerger");
        new CompPathwayMerger().mergeAndWrite(pathwayCache, compParser.parse());

        if (DEBUG)
            System.out.println("KoPathwayMerger");

        KoPathwayMerger koPathwayMerger = new KoPathwayMerger(pathwayCache,
                koParser.getKoConceptToGenes(), koParser
                        .getKoNamesToKoConcept(), koParser
                        .getKoAccessionToKoConcept(), relationsCache);
        koPathwayMerger.merge(species);
        // koParser.finalise();

        if (DEBUG)
            System.out.println("GenePathwayParser");
        GenePathwayParser parseGene = new GenePathwayParser(pathwayCache);
        Map<String, Set<Entry>> gene2GeneEntries = parseGene.parse();

        if (DEBUG)
            System.out.println("mergeGenes");
        Map<String, Set<Entry>> merge = Parser.mergeGenes(gene2GeneEntries,
                koPathwayMerger.getGene2KoEntries());

        if (DEBUG)
            System.out.println("GeneFileParser");

        GeneFilesParser geneParser = new GeneFilesParser(
                genesResource,
                merge,
                sequenceCache,
                relationsCache,
                pathwayCache);

        geneParser.parseAndWrite(orgs, orthologOrgs, genomeParser);

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

        if (DEBUG)
            System.out.println("clean up Writers");

    }

    private void saveURL(URL url, String filename) throws IOException {
        URLConnection connection = url.openConnection();
        connection.connect();
        InputStreamReader ReadIn = new InputStreamReader(connection.getInputStream());
        BufferedReader BufData = new BufferedReader(ReadIn);
        FileWriter FWriter = new FileWriter(filename);
        BufferedWriter BWriter = new BufferedWriter(FWriter);
        String urlData = null;
        while ((urlData = BufData.readLine()) != null) {
            BWriter.write(urlData);
            BWriter.newLine();
        }
        BWriter.close();
    }

    /**
     * Constructs a regex that matches kgml files
     *
     * @param keggOrganisms list of kegg organisms to creat regex for (pre-processed will not accept "all" as a species)
     * @return
     */
    private Pattern getKGMLRegex(Set<String> keggOrganisms) {
        Set<String> regexs = new HashSet<String>();

        for (String keggOrganism : keggOrganisms) {
            regexs.add("^" + keggOrganism.toLowerCase() + "[\\d]{5}\\.xml$");
        }
        StringBuilder builder = new StringBuilder();
        for (String regex : regexs) {
            if (builder.length() == 0)
                builder.append("(" + regex + ")");
            else
                builder.append("|(" + regex + ")");
        }
        return Pattern.compile(builder.toString());
    }

    private Set<String> getSpeciesToParse(Set<GenomeParser.Taxonomony> orgs, GenomeParser genomeParser) {
        Set<String> organisms = new HashSet<String>();

        for (GenomeParser.Taxonomony org : orgs) {
            String keggId = org.getKeggId();
            if (keggId.equals("all")) {
                Set<GenomeParser.Taxonomony> allSpecies = new HashSet<GenomeParser.Taxonomony>();
                for (String species : genomeParser.getAllKeggSpecies()) {
                    if (species.equals("all"))
                        continue;
                    GenomeParser.Taxonomony taxon = genomeParser.getTaxonomony(species);
                    allSpecies.add(taxon);
                }
                return organisms;
            } else {
                organisms.add(org.getKeggId());
            }
        }
        return organisms;
    }

    /**
     * @author hindlem
     */
    class KGMLJob implements Callable<Pathway> {

        private XMLParser xmlParser;
        private InputStream fileStream;

        public KGMLJob(XMLParser xmlParser, InputStream fileStream) {
            this.xmlParser = xmlParser;
            this.fileStream = fileStream;
        }

        public Pathway call() throws IOException, SAXException {
            return xmlParser.parse(fileStream);
        }
    }

}
