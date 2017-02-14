package net.sourceforge.ondex.parser.yeastgenome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.ConsoleProgressBar;
import net.sourceforge.ondex.tools.MetaDataLookup;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;
import org.apache.log4j.Level;

public class Parser extends ONDEXParser
{


    //##### ARGUMENTS #####
    public static final String MD_FILE_ARG = "TranslationFile";
    public static final String MD_FILE_ARG_DESC = "Absolute path to the metadata translation file.";
    public static final String PARSE_DUBIOUS_ARG = "ParseDubious";
    public static final String PARSE_DUBIOUS_ARG_DESC = "Whether or not to parse dubious ORF entries.";
    public static final String RNA_ARG = "CreateRNAConcepts";
    public static final String RNA_ARG_DESC = "Whether or not RNA concepts should be created by the parser";
    public static final String REL_LOC_ARG = "InferRelativeLocations";
    public static final String REL_LOC_ARG_DESC = "Whether or not relative location " +
            "relations should be created between feature concepts.";

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
                new FileArgumentDefinition(MD_FILE_ARG, MD_FILE_ARG_DESC, true, true, false),
                new BooleanArgumentDefinition(RNA_ARG, RNA_ARG_DESC, false, false),
                new BooleanArgumentDefinition(REL_LOC_ARG, REL_LOC_ARG_DESC, false, false),
                new BooleanArgumentDefinition(PARSE_DUBIOUS_ARG, PARSE_DUBIOUS_ARG_DESC, false, false)
        };
    }

    //##### FIELDS #####

    private boolean createRNAConcepts, inferRelativeLocations, parseDubious;

    private MetaDataLookup<ConceptClass> ccLookup;
    private MetaDataLookup<EvidenceType> etLookup;

    private void initFields() throws PluginConfigurationException {
        File lookupfile = new File((String) args.getUniqueValue(MD_FILE_ARG));
        try {
            ccLookup = new MetaDataLookup<ConceptClass>(lookupfile, graph.getMetaData(), ConceptClass.class);
            etLookup = new MetaDataLookup<EvidenceType>(lookupfile, graph.getMetaData(), EvidenceType.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new PluginConfigurationException("Could not load metadata file. Message: " + e.getMessage());
        }

        Boolean b = (Boolean) args.getUniqueValue(RNA_ARG);
        if (b != null) {
            createRNAConcepts = b;
        } else {
            createRNAConcepts = false;
        }

        b = (Boolean) args.getUniqueValue(REL_LOC_ARG);
        if (b != null) {
            inferRelativeLocations = b;
        } else {
            inferRelativeLocations = false;
        }

        b = (Boolean) args.getUniqueValue(PARSE_DUBIOUS_ARG);
        if (b != null) {
            parseDubious = b;
        } else {
            parseDubious = false;
        }
    }

    private Map<String, Integer> mips2cid;
    private List<Integer> cdss = new ArrayList<Integer>();
    private List<Integer> rnaGenes = new ArrayList<Integer>();
    private List<Integer> featureCids = new ArrayList<Integer>();


    private HashMap<String, Set<IntPair>> introns = new HashMap<String, Set<IntPair>>();

    //##### METADATA #####

    private DataSource dataSourceSGD, dataSourceMIPS;
    private EvidenceType etIMPD, etSilent;
    private AttributeName atChrom, atStart, atStop, atStrand, atInStr;
    private ConceptClass ccRnaGene, ccNCRNA, ccGene, ccPP, ccMRNA,
            ccRRNA, ccRRNAGene, ccTRNA, ccTRNAGene,
            ccSNRNA, ccSNRNAGene, ccSNORNA, ccSNORNAGene;
    private RelationType rtHasParent, rtEnBy, rtTrscr, rtTrlt, rtLocIn, rt5pa, rtUpStream, rt5pOverlap, rt5pNeighbour;

    private void fetchMetaData() throws MetaDataMissingException {
        dataSourceSGD = requireDataSource("SGD");
        dataSourceMIPS = requireDataSource("MIPS");
        etIMPD = requireEvidenceType("IMPD");
        etSilent = requireEvidenceType("SGD_Silenced");
        atChrom = requireAttributeName("Chromosome");
        atStart = requireAttributeName("BEGIN");
        atStop = requireAttributeName("END");
        atStrand = requireAttributeName("STR");
        atInStr = requireAttributeName("IntronStructure");
        ccGene = requireConceptClass("Gene");
        ccRnaGene = requireConceptClass("NcRNAGene");
        ccNCRNA = requireConceptClass("ncRNA");
        ccPP = requireConceptClass("Polypeptide");
        ccMRNA = requireConceptClass("MRNA");
        ccRRNA = requireConceptClass("RRNA");
        ccRRNAGene = requireConceptClass("RRNAGene");
        ccTRNA = requireConceptClass("TRNA");
        ccTRNAGene = requireConceptClass("TRNAGene");
        ccSNRNA = requireConceptClass("SNRNA");
        ccSNRNAGene = requireConceptClass("SNRNAGene");
        ccSNORNA = requireConceptClass("SNORNA");
        ccSNORNAGene = requireConceptClass("SNORNAGene");
        rtHasParent = requireRelationType("subfeature_of");
        rtEnBy = requireRelationType("en_by");
        rtTrscr = requireRelationType("transcribes_to");
        rtTrlt = requireRelationType("translates_to");
        rtLocIn = requireRelationType("located_in");
        rt5pa = requireRelationType("5pAdjacent");
        rtUpStream = requireRelationType("upstream");
        rt5pOverlap = requireRelationType("5pOverlap");
        rt5pNeighbour = requireRelationType("5pNeighbour");
    }


    //##### MAIN METHODS #####


    @Override
    public void start() throws Exception {
        //prepare
        fetchMetaData();
        initFields();

        //main part

        FeatureParser fp = new FeatureParser();
        fp.parseFeatures();
        fp.createConcepts();
        fp.connectParents();
        fp = null;
        createEntityConcepts();
        if (inferRelativeLocations) {
            inferRelativeLocations();
        }
    }


    private String compileIntronStructure(String mips) {
        if (mips == null) {
            return null;
        }

        Set<IntPair> intronSet = introns.get(mips);
        if (intronSet == null) {
            return null;
        }

        StringBuilder b = new StringBuilder();
        for (IntPair i : intronSet) {
            b.append(i.a + "-" + i.b + ",");
        }

        if (b.length() == 0) {
            return null;
        }

        b.deleteCharAt(b.length() - 1);
        return b.toString();
    }


    private void createEntityConcepts() {
        log("Creating polypeptide concepts...");
        ConsoleProgressBar pb = new ConsoleProgressBar(cdss.size());

        for (int cid : cdss) {
            ONDEXConcept gene = graph.getConcept(cid);

            //skip silent genes
            if (gene.getEvidence().contains(etSilent)) {
                continue;
            }

            //create polypeptide
            ONDEXConcept pp = graph.getFactory().createConcept("pp" + cid, dataSourceSGD, ccPP, etIMPD);

            String mips = getMips(gene);
            if (mips == null || mips.equals("")) {
                logInconsistency("Gene concept missing MIPS accession: " + gene.getPID());
                continue;
            }
            pp.createConceptAccession(mips, dataSourceMIPS, false);

            if (gene.getConceptName() != null) {
                pp.createConceptName(gene.getConceptName().getName(), true);
            }

            graph.getFactory().createRelation(pp, gene, rtEnBy, etIMPD);

            //compile intron structure
            String introns = compileIntronStructure(mips);
            if (introns != null) {
                gene.createAttribute(atInStr, introns, false);
            }

            if (createRNAConcepts) {
                ONDEXConcept mrna = graph.getFactory().createConcept("mrna" + cid, dataSourceSGD, ccMRNA, etIMPD);
                mrna.createConceptAccession(mips, dataSourceMIPS, false);
                if (gene.getConceptName() != null) {
                    mrna.createConceptName(gene.getConceptName().getName(), true);
                }

                graph.getFactory().createRelation(gene, mrna, rtTrscr, etIMPD);
                graph.getFactory().createRelation(mrna, pp, rtTrlt, etIMPD);
            }
            pb.inc(1);
        }

        pb.complete();

        if (createRNAConcepts) {
            log("Creating RNA concepts...");
            pb = new ConsoleProgressBar(rnaGenes.size());
            for (int cid : rnaGenes) {
                ONDEXConcept rnagene = graph.getConcept(cid);
                String mips = getMips(rnagene);
                ONDEXConcept rna = null;
                if (rnagene.inheritedFrom(ccTRNAGene)) {
                    rna = graph.getFactory().createConcept("trna" + cid, dataSourceSGD, ccTRNA, etIMPD);
                } else if (rnagene.inheritedFrom(ccRRNAGene)) {
                    rna = graph.getFactory().createConcept("rrna" + cid, dataSourceSGD, ccRRNA, etIMPD);
                } else if (rnagene.inheritedFrom(ccSNRNAGene)) {
                    rna = graph.getFactory().createConcept("snrna" + cid, dataSourceSGD, ccSNRNA, etIMPD);
                } else if (rnagene.inheritedFrom(ccSNORNAGene)) {
                    rna = graph.getFactory().createConcept("snorna" + cid, dataSourceSGD, ccSNORNA, etIMPD);
                } else {
                    rna = graph.getFactory().createConcept("ncrna" + cid, dataSourceSGD, ccNCRNA, etIMPD);
                }
                rna.createConceptAccession(mips, dataSourceMIPS, false);

                graph.getFactory().createRelation(rnagene, rna, rtTrscr, etIMPD);

                pb.inc(1);
            }

            pb.complete();
        }
    }


    private void inferRelativeLocations() {
        log("Inferring relative locations...");
        ConsoleProgressBar pb = new ConsoleProgressBar(featureCids.size());


        for (int cid_i : featureCids) {
            ONDEXConcept fi = graph.getConcept(cid_i);

            Integer chrom_i = (Integer) getAttribute(fi, atChrom);
            if (chrom_i == null) {
                logInconsistency("CDS " + fi + " has no chromosome attribute");
                continue;
            }

            //W is normal, C is reverse!
            Character strand_i = (Character) getAttribute(fi, atStrand);
            if (strand_i == null) {
//				logInconsistency("CDS "+fi+" has no strand attribute");
                continue;
            }

            Integer start_i = (Integer) getAttribute(fi, atStart);
            if (start_i == null) {
                logInconsistency("CDS " + fi + " has no start attribute");
                continue;
            }

            Integer stop_i = (Integer) getAttribute(fi, atStop);
            if (stop_i == null) {
                logInconsistency("CDS " + fi + " has no start attribute");
                continue;
            }

            Set<Integer> has5pOverlap = new HashSet<Integer>();
            Set<Integer> has3pOverlap = new HashSet<Integer>();

            int minDistUp = Integer.MAX_VALUE;
            int minDistUpCid = -1;

            for (int cid_j : featureCids) {
                if (cid_j == cid_i) {
                    continue;
                }
                ONDEXConcept fj = graph.getConcept(cid_j);

                Integer chrom_j = (Integer) getAttribute(fj, atChrom);
                if (chrom_j == null) {
                    continue;
                }

                //W is normal, C is reverse!
                Character strand_j = (Character) getAttribute(fj, atStrand);
                if (strand_j == null) {
                    continue;
                }

                Integer start_j = (Integer) getAttribute(fj, atStart);
                if (start_j == null) {
                    continue;
                }

                Integer stop_j = (Integer) getAttribute(fj, atStop);
                if (stop_j == null) {
                    continue;
                }

                if (chrom_i.equals(chrom_j) && strand_i.equals(strand_j)) {

                    if ((strand_i == 'W' && (start_i < start_j && stop_j < stop_i)) ||
                            strand_i == 'C' && (stop_i < stop_j && start_j < stop_i)) {
                        //j inside i
                        graph.getFactory().createRelation(fj, fi, rtLocIn, etIMPD);
                    } else if (strand_i == 'W' && (stop_j < start_i)) {
                        //j upstream of i in W direction
                        int dist = start_i - stop_j;
                        if (dist < minDistUp) {
                            minDistUp = dist;
                            minDistUpCid = cid_j;
                        }
                    } else if (strand_i == 'C' && (start_i < stop_j)) {
                        //j upstream of i in C direction
                        int dist = stop_j - start_i;
                        if (dist < minDistUp) {
                            minDistUp = dist;
                            minDistUpCid = cid_j;
                        }
                    } else if ((strand_i == 'W' && (start_j < start_i && start_i < stop_j && stop_j < stop_i)) ||
                            strand_i == 'C' && (stop_i < stop_j && stop_j < start_i && start_i < start_j)) {
                        //j overlaps i's 5' end
                        if (graph.getRelation(fj, fi, rt5pOverlap) == null) {
                            graph.getFactory().createRelation(fj, fi, rt5pOverlap, etIMPD);
                            has5pOverlap.add(cid_i);
                            has3pOverlap.add(cid_j);
                        }
                    }
                }
            }

            if (minDistUpCid > -1) {
                ONDEXConcept upC = graph.getConcept(minDistUpCid);
                if (minDistUp < 2) {
                    if (!has5pOverlap.contains(cid_i) && !has3pOverlap.contains(minDistUpCid) && (graph.getRelation(upC, fi, rt5pa) == null)) {
                        graph.getFactory().createRelation(upC, fi, rt5pa, etIMPD);
                    }
                } else {
                    if (!has5pOverlap.contains(cid_i) && !has3pOverlap.contains(minDistUpCid) && (graph.getRelation(upC, fi, rtUpStream) == null)) {
                        graph.getFactory().createRelation(upC, fi, rtUpStream, etIMPD);
                    }
                }
            }
            pb.inc(1);
        }

        pb.complete();

        testConsistency();
    }

    //##### HELPER METHODS #####

    private void testConsistency() {
        for (int cid : featureCids) {
            ONDEXConcept c = graph.getConcept(cid);

            boolean upFound = false;
            boolean downFound = false;

            for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                if (r.inheritedFrom(rt5pNeighbour)) {
                    if (r.getToConcept().equals(c)) {
                        //has 5p neighbour
                        if (upFound) {
                            logInconsistency(c + "has more than one upstream neighbour");
                        } else {
                            upFound = true;
                        }
                    } else {
                        //has 3p neighbour
                        if (downFound) {
                            logInconsistency(c + " has more than one downstream neighbour");
                        } else {
                            downFound = true;
                        }
                    }
                }
            }

        }
    }


    private String getMips(ONDEXConcept gene) {
        for (ConceptAccession acc : gene.getConceptAccessions()) {
            if (acc.getElementOf().equals(dataSourceMIPS)) {
                return acc.getAccession();
            }
        }
        return null;
    }

    private Object getAttribute(ONDEXConcept c, AttributeName an) {
        Attribute attribute = c.getAttribute(an);
        if (attribute == null) {
            return null;
        } else {
            return attribute.getValue();
        }
    }

    private class Feature {
        private Set<String> sgdIDs = new TreeSet<String>();
        private ConceptClass cc;
        private Set<EvidenceType> evidence = new HashSet<EvidenceType>();
        private String mips;
        private String preferredName;
        private Set<String> aliases = new TreeSet<String>();
        private String parentMips;
        private Integer chromosome;
        private Integer start, stop;
        private Character strand;
        private String desc;

        public void fill(String sgdId, ConceptClass cc, String[] qual,
                         String mips, String name, String[] aliases,
                         String parentMips, String[] sgdId_sec, String chrom_num,
                         String start, String stop, String strand, String desc) {

            if (this.cc == null || cc.isAssignableTo(this.cc)) {
                this.cc = cc;
            }

            for (String q : qual) {
                EvidenceType et = etLookup.get(q);
                if (et != null) {
                    evidence.add(et);
                }
            }

            if (this.mips == null && mips != null && !mips.trim().equals("")) {
                this.mips = mips;
            }

            if (this.preferredName == null && name != null && !name.trim().equals("")) {
                this.preferredName = name;
            }

            for (String a : aliases) {
                if (a != null && !a.equals("")) {
                    this.aliases.add(a);
                }
            }

            if (this.parentMips == null && parentMips != null && !parentMips.trim().equals("")) {
                this.parentMips = parentMips;
            }

            for (String sgd : sgdId_sec) {
                if (sgd != null && !sgd.equals("")) {
                    sgdIDs.add(sgd);
                }
            }
            if (sgdId != null && !sgdId.equals("")) {
                sgdIDs.add(sgdId);
            }

            try {
                chromosome = Integer.parseInt(chrom_num);
            } catch (NumberFormatException nfe) {
                chromosome = null;
            }

            try {
                this.start = Integer.parseInt(start);
            } catch (NumberFormatException nfe) {
                start = null;
            }

            try {
                this.stop = Integer.parseInt(stop);
            } catch (NumberFormatException nfe) {
                stop = null;
            }

            if (strand.trim().equals("W")) {
                this.strand = 'W';
            } else if (strand.trim().equals("C")) {
                this.strand = 'C';
            } else {
                this.strand = null;
            }

            if (this.desc == null && desc != null && !desc.trim().equals("")) {
                this.desc = desc;
            }

        }

        public String getParent() {
            return parentMips;
        }

        public String getMIPS() {
            return mips;
        }


        public ONDEXConcept toConcept() {
            String id = "Feat:" + chromosome + ":" + start + ":" + stop + ":" + strand;
            ONDEXConcept c = graph.getFactory().createConcept(id, dataSourceSGD, cc, etIMPD);
            for (String sgd : sgdIDs) {
                c.createConceptAccession(sgd, dataSourceSGD, true);
            }
            if (mips != null && !mips.equals("")) {
                c.createConceptAccession(mips, dataSourceMIPS, false);
            }
            for (EvidenceType e : evidence) {
                c.addEvidenceType(e);
            }
            if (preferredName != null && !preferredName.equals("")) {
                c.createConceptName(preferredName, true);
            }
            for (String alias : aliases) {
                c.createConceptName(alias, false);
            }
            if (chromosome != null) {
                c.createAttribute(atChrom, chromosome, false);
            }
            if (start != null) {
                c.createAttribute(atStart, start, false);
            }
            if (stop != null) {
                c.createAttribute(atStop, stop, false);
            }
            if (strand != null) {
                c.createAttribute(atStrand, strand, false);
            }
            if (desc != null) {
                c.setDescription(desc);
            }
            return c;
        }

    }

    private class IntPair {
        public int a, b;

        public IntPair(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    @Override
    public String getName() {
        return "Yeast genome parser";
    }

    @Override
    public String getVersion() {
        return "30.07.2009";
    }

    @Override
    public String getId() {
        return "yeastgenome";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    private void log(String s) {
        GeneralOutputEvent e = new GeneralOutputEvent("\n" + s, "");
        e.setLog4jLevel(Level.INFO);
        fireEventOccurred(e);
    }

    private void logInconsistency(String s) {
        InconsistencyEvent e = new InconsistencyEvent("\n" + s, "");
        e.setLog4jLevel(Level.DEBUG);
        fireEventOccurred(e);
    }

    private class FeatureParser {
        private HashMap<String, Feature> features = new HashMap<String, Feature>();

        private Map<String, Integer> fid2cid;

        private Feature getFeature(String id) {
            Feature f = features.get(id);
            if (f == null) {
                f = new Feature();
                features.put(id, f);
            }
            return f;
        }

        private void parseFeatures() throws PluginConfigurationException {
            log("Reading file...");
            try {
                BufferedReader br = new BufferedReader(new FileReader((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE)));
                String line;
                int lineNum = 0;
                while ((line = br.readLine()) != null) {
                    lineNum++;
                    String[] cols = line.split("\t");
                    if (cols.length < 16) {
                        String[] tmp = new String[16];
                        for (int i = 0; i < 16; i++) tmp[i] = "";
                        System.arraycopy(cols, 0, tmp, 0, cols.length);
                        cols = tmp;
                    } else if (cols.length > 16) {
                        logInconsistency("Line " + lineNum + " has unexpected number of columns: " + cols.length);
                        continue;
                    }

                    String sgdId = cols[0],
                            type = cols[1];
                    String[] qual = cols[2].split("\\|");
                    String mips = cols[3],
                            name = cols[4];
                    String[] aliases = cols[5].split("\\|");
                    String parentMips = cols[6];
                    String[] sgdId_sec = cols[7].split("\\|");
                    String chrom_num = cols[8],
                            start = cols[9],
                            stop = cols[10],
                            strand = cols[11],
                            desc = cols[15];

                    ConceptClass cc = ccLookup.get(type);

                    //capture introns
                    if (type.equals("intron") || type.equals("five_prime_UTR_intron")) {
                        try {
                            int begin = Integer.parseInt(start);
                            int end = Integer.parseInt(stop);

                            Set<IntPair> intronSet = introns.get(parentMips);
                            if (intronSet == null) {
                                intronSet = new HashSet<IntPair>();
                                introns.put(parentMips, intronSet);
                            }
                            intronSet.add(new IntPair(begin, end));

                        } catch (NumberFormatException nfe) {
                            //ignore
                        }
                    }

                    //skip unsupported feature type
                    if (cc == null) {
                        continue;
                    }

                    //skip dubious genes
                    if (!parseDubious) {
                        boolean dub = false;
                        for (String q : qual) {
                            if (q.equals("Dubious")) {
                                dub = true;
                                break;
                            }
                        }
                        if (dub == true) {
                            continue;
                        }
                    }

                    String featID = chrom_num + ":" + start + ":" + stop + ":" + strand;

                    Feature f = getFeature(featID);
                    f.fill(sgdId, cc, qual, mips,
                            name, aliases, parentMips,
                            sgdId_sec, chrom_num, start,
                            stop, strand, desc);

                    if (lineNum % 500 == 499) {
                        System.out.print(".");
                    }
                }
                System.out.println();
            } catch (IOException ioe) {
                throw new PluginConfigurationException("Cannot read file " + args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
            }
        }

        private void createConcepts() {
            log("Creating feature concepts...");

            fid2cid = LazyMap.decorate(new HashMap<String, Integer>(), new Factory<Integer>(){
				@Override
				public Integer create() {
					return Integer.valueOf(-1);
				}});
            mips2cid = LazyMap.decorate(new HashMap<String, Integer>(), new Factory<Integer>(){
				@Override
				public Integer create() {
					return Integer.valueOf(-1);
				}});

            ConsoleProgressBar pb = new ConsoleProgressBar(features.keySet().size());

            for (String fid : features.keySet()) {
                Feature f = features.get(fid);
                ONDEXConcept c = f.toConcept();
                featureCids.add(c.getId());
                fid2cid.put(fid, c.getId());
                if (f.getMIPS() != null && !f.getMIPS().equals("")) {
                    mips2cid.put(f.getMIPS(), c.getId());
                }
                if (c.inheritedFrom(ccGene)) {
                    cdss.add(c.getId());
                } else if (createRNAConcepts && c.inheritedFrom(ccRnaGene)) {
                    rnaGenes.add(c.getId());
                }
                pb.inc(1);
            }

            pb.complete();

        }

        private void connectParents() {
            log("Creating subfeature relations...");

            ConsoleProgressBar pb = new ConsoleProgressBar(features.keySet().size());

            for (String fid : features.keySet()) {
                Feature f = features.get(fid);
                String parentMips = f.getParent();
                if (parentMips == null) {
                    continue;
                }
                ONDEXConcept child = graph.getConcept(fid2cid.get(fid));
                int parentCid = mips2cid.get(parentMips);
                if (parentCid > -1) {
                    ONDEXConcept parent = graph.getConcept(parentCid);
                    graph.getFactory().createRelation(child, parent, rtHasParent, etIMPD);
                }
                pb.inc(1);
            }

            pb.complete();
        }


    }

}
