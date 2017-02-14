package net.sourceforge.ondex.transformer.sharedgi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.IntegerRangeArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.PluginErrorEvent;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.tools.ConsoleProgressBar;
import net.sourceforge.ondex.tools.MetaDataLookup;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;
import org.apache.log4j.Level;

/**
 * Shared GI transformer
 *
 * @author Jochen Weile, M.Sc.
 */
public class Transformer extends ONDEXTransformer
{

    //##### META DATA ######
    private ConceptClass ccGI, ccPI, ccGene, ccPub;
    private RelationType rtPartAct, rtPartPass, rtGI, rtShare;
    private EvidenceType etTrans;
    private AttributeName atLinks;
    private DataSource dataSourceMips;

    private void initMetaData() throws MetaDataMissingException {
        ccGI = requireConceptClass("GenInt");
        ccPI = requireConceptClass("PhysInt");
        ccGene = requireConceptClass("Gene");
        ccPub = requireConceptClass("Publication");
        rtPartAct = requireRelationType("part_act");
        rtPartPass = requireRelationType("part_pass");
        rtGI = requireRelationType("gi");
        etTrans = requireEvidenceType("InferredByTransformation");
        RelationType rtR = requireRelationType("r");
        rtShare = graph.getMetaData().createRelationType("shareGI", "shared_genetic_interaction_with", "", "shares_genetic_interaction_with", false, false, true, false, rtR);
        AttributeName atGDS = requireAttributeName("Attribute");
        atLinks = graph.getMetaData().createAttributeName("links", "links", "number of links", null, Integer.class, atGDS);
        dataSourceMips = requireDataSource("MIPS");
    }


    //##### ARGUMENTS #####

    public static final String TRANSL_ARG = "TranslationFile";
    public static final String TRANSL_ARG_DESC = "Path to Translation File";
    public static final String THRESH_ARG = "Threshold";
    public static final String THRESH_ARG_DESC = "Minimal number of shared interactions " +
            "required for edge creation";
    public static final String OUT_ARG = "OutputFile";
    public static final String OUT_ARG_DESC = "Path to output file";

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(TRANSL_ARG, TRANSL_ARG_DESC, true, true, false),
                new IntegerRangeArgumentDefinition(THRESH_ARG, THRESH_ARG_DESC, true, 2, 1, Integer.MAX_VALUE),
                new FileArgumentDefinition(OUT_ARG, OUT_ARG_DESC, true, false, false)
        };
    }


    //##### FIELDS #####

    private MetaDataLookup<RelationType> rtLookup;

    private int threshold;

    private String outfile;

    private void initFields() throws Exception {
        ONDEXGraphMetaData md = graph.getMetaData();
        File transl = new File((String) args.getUniqueValue(TRANSL_ARG));
        rtLookup = new MetaDataLookup<RelationType>(transl, md, RelationType.class);

        threshold = (Integer) args.getUniqueValue(THRESH_ARG);

        outfile = (String) args.getUniqueValue(OUT_ARG);
    }


    //##### METHODS #####

    @Override
    public void start() throws Exception {
        initMetaData();
        initFields();
        transformInteractions();
        deleteConcepts();
        inferSharedInteractions();
        writeOutput();
    }

    private void transformInteractions() {
        log("Transforming Interactions...");

        ConsoleProgressBar pb = new ConsoleProgressBar(graph.getConcepts().size());

        for (ONDEXConcept c : graph.getConcepts()) {
            pb.inc(1);
            if (!c.inheritedFrom(ccGI)) {
                continue;
            }
            RelationType rt = rtLookup.get(c.getOfType().getId());
            if (rt == null) {
                continue;
            }
            ONDEXConcept from = null, to = null;
            for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                if (r.inheritedFrom(rtPartAct)) {
                    from = r.getFromConcept();
                    if (to != null) {
                        break;
                    }
                } else if (r.inheritedFrom(rtPartPass)) {
                    to = r.getFromConcept();//yes that's actually correct!
                    if (from != null) {
                        break;
                    }
                }
            }
            if (from == null || to == null) {
                continue;
            }
            graph.getFactory().createRelation(from, to, rt, etTrans);
        }
        pb.complete();
    }

    private void deleteConcepts() {
        log("Deleting Interaction concepts...");

        long mem = Runtime.getRuntime().freeMemory();

        ConsoleProgressBar pb = new ConsoleProgressBar(graph.getConcepts().size());

        for (ONDEXConcept c : graph.getConcepts()) {
            pb.inc(1);
            if (c.inheritedFrom(ccGI) || c.inheritedFrom(ccPI) || c.inheritedFrom(ccPub)) {
                graph.deleteConcept(c.getId());
            }
        }

        System.gc();
        pb.complete();
        mem = Runtime.getRuntime().freeMemory() - mem;
        log("Gained " + mem + " bytes");

    }

    private void inferSharedInteractions() {
        Map<PairSet, Integer> links = LazyMap.decorate(new HashMap<PairSet, Integer>(), new Factory<Integer>() {
			@Override
			public Integer create() {
				return Integer.valueOf(0);
			}});

        log("Counting shared interactions...");

        ConsoleProgressBar pb = new ConsoleProgressBar(graph.getConcepts().size());

        for (ONDEXConcept c : graph.getConcepts()) {
            pb.inc(1);
            if (!c.inheritedFrom(ccGene)) {
                continue;
            }
            List<Integer> neighbours = new ArrayList<Integer>();
            for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                if (!r.inheritedFrom(rtGI)) {
                    continue;
                }
                ONDEXConcept other = r.getFromConcept().equals(c) ? r.getToConcept() : r.getFromConcept();
                if (!other.inheritedFrom(ccGene)) {
                    continue;
                }
                neighbours.add(other.getId());
            }
            for (int i = 0; i < neighbours.size(); i++) {
                int id_i = neighbours.get(i);
                for (int j = i + 1; j < neighbours.size(); j++) {
                    int id_j = neighbours.get(j);
                    PairSet p = new PairSet(id_i, id_j);
                    links.put(p, links.get(p) + 1);
                }
            }
        }
        pb.complete();

        runStats(links);

        log("Linking share partners...");
        pb = new ConsoleProgressBar(links.size());

        for (PairSet pair : links.keySet()) {
            pb.inc(1);
            int ln = links.get(pair);
            if (ln >= threshold) {
                ONDEXConcept from = graph.getConcept(pair.a);
                ONDEXConcept to = graph.getConcept(pair.b);
//				if (!interact(from.getId(), to.getId())) {
                ONDEXRelation r = graph.getFactory().createRelation(from, to, rtShare, etTrans);
                r.createAttribute(atLinks, ln, false);
//				}
            }
        }
        pb.complete();
    }

    private void runStats(Map<PairSet, Integer> links) {
        log("Running statistical analysis...");
        System.out.println("Number of sharing partners: " + links.size());

        int num_bins = 800;

        int maxVal = max(links.values());
        int[] bins = new int[num_bins + 1];
        int[] bins2 = new int[num_bins + 1];

        ConsoleProgressBar pb = new ConsoleProgressBar(links.size());

        Plotter plotter = new Plotter(1000, 1500, 1000, 1500);

        for (PairSet pair : links.keySet()) {
            pb.inc(1);

            int value = links.get(pair);

            int index = value * num_bins / maxVal;
            bins[index]++;

            if (interact(pair.a, pair.b)) {
                bins2[index]++;
            }

            int deg_a = getDegree(pair.a);
            int deg_b = getDegree(pair.b);
            plotter.plot(value, deg_a);
            plotter.plot(value, deg_b);

        }
        pb.complete();


        System.out.println("#shared\tfreq\tfreq_int");
        for (int i = 0; i < num_bins; i++) {
            System.out.println((i * maxVal / num_bins) + "\t" + bins[i] + "\t" + bins2[i]);
        }

        plotter.exportImage("/home/jweile/scatterplot.png");
    }


    private int getDegree(int cid) {
        ONDEXConcept c = graph.getConcept(cid);
        int count = 0;
        for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
            if (r.inheritedFrom(rtGI)) {
                count++;
            }
        }
        return count;
    }


    private int max(Collection<Integer> values) {
        int max = Integer.MIN_VALUE;
        for (int i : values) if (i > max) max = i;
        return max;
    }


    private void writeOutput() {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(outfile));
            w.write("#shared_interactions\tgene1\tgene1_name\tgene2\tgene2_name\tknown_interaction\n");
            for (ONDEXRelation r : graph.getRelationsOfRelationType(rtShare)) {
                String fromName = getMips(r.getFromConcept());
                ConceptName fromGname = r.getFromConcept().getConceptName();
                String fromGnameS = fromGname != null ? fromGname.getName() : "";
                String toName = getMips(r.getToConcept());
                ConceptName toGname = r.getToConcept().getConceptName();
                String toGnameS = toGname != null ? toGname.getName() : "";
                boolean intact = interact(r.getFromConcept().getId(), r.getToConcept().getId());
                Attribute linksAttribute = r.getAttribute(atLinks);
                if (fromName != null && toName != null && linksAttribute != null) {
                    w.write(linksAttribute.getValue() + "\t" + fromName + "\t" + fromGnameS + "\t" + toName + "\t" + toGnameS + "\t" + intact + "\n");
                }
            }
            w.close();
        } catch (IOException e) {
            logError("Output file could not be written: " + outfile);
        }
    }

    private String getMips(ONDEXConcept fromConcept) {
        for (ConceptAccession acc : fromConcept.getConceptAccessions()) {
            if (acc.getElementOf().equals(dataSourceMips)) {
                return acc.getAccession();
            }
        }
        return null;
    }


    //##### HELPER METHODS #####

    private void log(String s) {
        GeneralOutputEvent e = new GeneralOutputEvent("\n" + s, "");
        e.setLog4jLevel(Level.INFO);
        fireEventOccurred(e);
    }

    private void logError(String s) {
        PluginErrorEvent e = new PluginErrorEvent("\n" + s, "");
        e.setLog4jLevel(Level.ERROR);
        fireEventOccurred(e);
    }

    private boolean interact(int a, int b) {
        ONDEXConcept ca = graph.getConcept(a);
        for (ONDEXRelation r : graph.getRelationsOfConcept(ca)) {
            int from = r.getFromConcept().getId();
            int to = r.getToConcept().getId();
            if ((from == a && to == b) || (from == b && to == a)) {
                if (r.inheritedFrom(rtGI)) {
                    return true;
                }
            }
        }
        return false;
    }

    //##### HELPER CLASSES #####

    private class PairSet {
        public int a, b;

        public PairSet(int x, int y) {
            if (x > y) {
                a = y;
                b = x;
            } else {
                a = x;
                b = y;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PairSet) {
                PairSet o = (PairSet) obj;
                if (o.a == a && o.b == b) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return a * 300000 + b;
        }
    }


//##### OLIGATORY STUFF #####

    @Override
    public String getName() {
        return "Shared GI transformer";
    }

    @Override
    public String getVersion() {
        return "11.08.2009";
    }

    @Override
    public String getId() {
        return "sharedgi";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }
}
