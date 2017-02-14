package net.sourceforge.ondex.parser.arabidopsis_hormone_db;

import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.util.ArrayKey;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.*;

/**
 * @author lysenkoa
 */
@Status(description = "Untested formally", status = StatusType.EXPERIMENTAL)
@DatabaseTarget(name = "Arabidopsis Hormone Database (AHD)",
        description = "A systematic and comprehensive view of morphological phenotypes regulated by plant hormones.",
        version = "",
        url = "http://ahd.cbi.pku.edu.cn")
public class Parser extends ONDEXParser implements MetaData {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                        FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    @Override
    public String getName() {
        return "Arabidopsis Hormone Database parser";
    }

    @Override
    public String getVersion() {
        return "v1.0";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    private DataSource ahd;
    private DataSource pmid;
    private RelationType pub_in;
    private RelationType is_p;
    private RelationType induces;
    private RelationType subjected_to;
    private RelationType r;
    private EvidenceType evidence;
    private ConceptClass mutant;
    private ConceptClass treatment;
    private ConceptClass publication;
    private ConceptClass annotation;
    private ConceptClass phenotype;
    private ConceptClass hormone;
    private ConceptClass protein;
    private ConceptClass experiment;

    private AttributeName mutType;
    private AttributeName eco;
    private AttributeName mutagenesis;
    private AttributeName dominance;
    private AttributeName comment;
    private AttributeName description;
    private AttributeName micro;
    private AttributeName annoSource;
    private AttributeName ecoCont;
    private AttributeName ecoTest;
    private AttributeName cell;
    private AttributeName age;
    private AttributeName treatments;
    private AttributeName hormoneTreat;
    private AttributeName hormoneConc;
    private boolean createHormoneContext = true;
    private final Lookup lookup = new Lookup();


    @Override
    public void start() throws Exception {
        ahd = createDataSource(graph, AHD);
        pmid = createDataSource(graph, PMID);
        pub_in = createRT(graph, pub_in_);
        evidence = createEvidence(graph, IMPD);
        is_p = createRT(graph, is_p_);
        r = createRT(graph, r_);
        induces = createRT(graph, induces_);
        subjected_to = createRT(graph, subjected_to_);
        mutant = createCC(graph, Mutant);
        hormone = createCC(graph, Hormone);
        protein = createCC(graph, Protein);
        experiment = createCC(graph, Experiment);
        annotation = createCC(graph, Annoation);
        phenotype = createCC(graph, Phenotype);
        treatment = createCC(graph, Treatment);
        publication = createCC(graph, Publication);

        annoSource = createAttName(graph, Annoation_source, String.class);
        ecoCont = createAttName(graph, Control_ecotype, String.class);
        hormoneTreat = createAttName(graph, Hormone_treatment, String.class);
        hormoneConc = createAttName(graph, Hormone_concepntaration, String.class);
        ecoTest = createAttName(graph, Test_ecotype, String.class);
        cell = createAttName(graph, Cell_type, String.class);
        age = createAttName(graph, Age, String.class);
        treatments = createAttName(graph, Treatments, String.class);
        micro = createAttName(graph, Microarray_data, String.class);
        mutType = createAttName(graph, Mutant_Transgenic_plant, String.class);
        description = createAttName(graph, Description, String.class);
        eco = createAttName(graph, Ecotype, String.class);
        mutagenesis = createAttName(graph, Mutagenesis_type, String.class);
        dominance = createAttName(graph, Dominant_Recessive_Semi_dominant, String.class);
        comment = createAttName(graph, Comment, String.class);


        Map<String, FileParser> parserDelegates = new HashMap<String, FileParser>();
        parserDelegates.put("gene_hormone_info", new FileParser() {
            public void parseLine(String[] data) throws FileNotFoundException, IOException {
                ONDEXConcept prot = lookup.lookupProtein(data[1], data[2]);
                ONDEXConcept h = lookup.lookupHormone(data[3]);
                ONDEXRelation rel = createRealtion(prot, h, r);
                ONDEXConcept anno = graph.getFactory().createConcept(data[4], ahd, annotation, evidence);
                if (!data[4].equals("-")) {
                    anno.createConceptName(data[4], true);
                }
                if (data[5].length() != 0) {
                    anno.createAttribute(description, data[5], false);
                }
                anno.createAttribute(micro, data[7], false);

                String z = "Mutant";
                if (data.length == 9) {
                    z = data[8];

                }
                anno.createAttribute(annoSource, z, false);
                rel.addTag(anno);

                String[] ids = data[6].split(";");
                for (String id1 : ids) {
                    id1 = id1.trim();
                    if (id1 == null || id1.length() == 0)
                        continue;
                    ONDEXConcept pub = lookup.lookupPublication(id1);
                    graph.getFactory().createRelation(anno, pub, pub_in, evidence);
                    createHormoneContext(data[3], pub);
                }
                createHormoneContext(data[3], prot, rel, anno);
            }
        });
        parserDelegates.put("gene_hormone_microarray", new FileParser() {
            public void parseLine(String[] data) throws FileNotFoundException, IOException {
                ONDEXConcept exp = lookup.lookupExperiment(data[1]);
                ONDEXConcept prot = lookup.lookupProtein(data[3], null);
                String num = data[4];
                List<String> names = new ArrayList<String>();
                List<Double> measures = new ArrayList<Double>();
                if (num.contains("||")) {
                    String[] data1 = num.split("\\|\\|");
                    for (String d : data1) {
                        measures.add(getDouble(d.substring(0, d.indexOf("("))));
                        String name = d.substring(d.indexOf("(") + 1, d.length() - 1);
                        names.add(data[1] + "_" + name);
                    }
                } else {
                    names.add(data[1] + "_Experiment");
                    if (num.contains("(")) {
                        measures.add(getDouble(num.substring(0, num.indexOf("("))));
                    } else {
                        measures.add(getDouble(num));
                    }
                }
                for (int i = 0; i < measures.size(); i++) {
                    Integer index = lookup.getIndex(prot, names.get(i));
                    AttributeName att = null;
                    if (index > 0) {
                        att = createAttName(graph, names.get(i) + "[" + index + "]", Double.class);
                    } else {
                        att = createAttName(graph, names.get(i), Double.class);
                    }
                    prot.createAttribute(att, measures.get(i), false);
                }
                prot.addTag(exp);
                createHormoneContext(data[2], exp, prot);
            }
        });
        parserDelegates.put("gene_hormone_plant", new FileParser() {
            public void parseLine(String[] data) throws FileNotFoundException, IOException {
                ONDEXConcept protein = lookup.lookupProtein(data[1], data[2]);
                ONDEXConcept mut = lookup.lookupMutatant(data[4]);
                if (data.length > 5 && mut.getAttribute(description) == null) {
                    mut.createAttribute(description, data[5], false);
                }
                ONDEXRelation rr = createRealtion(protein, mut, r);
                createHormoneContext(data[3], rr, protein, mut);
            }
        });
        parserDelegates.put("microarray_info", new FileParser() {
            public void parseLine(String[] data) throws FileNotFoundException, IOException {
                ONDEXConcept exp = lookup.lookupExperiment(data[0]);
                if (exp.getAttributes().size() == 0) {
                    exp.createAttribute(ecoCont, data[1], false);
                    exp.createAttribute(ecoTest, data[2], false);
                    exp.createAttribute(cell, data[3], false);
                    exp.createAttribute(age, data[5], false);
                    exp.createAttribute(treatments, data[8], false);
                    exp.createAttribute(description, data[7], false);
                    exp.createAttribute(hormoneTreat, data[4], false);
                    exp.createAttribute(hormoneConc, data[6], false);
                }
                createHormoneContext(data[4], exp);
            }

        });

        parserDelegates.put("phenotype", new FileParser() {
            private ONDEXConcept pheno0;

            @Override
            public void parse(File file) throws IOException {
                pheno0 = graph.getFactory().createConcept("Phenotype", ahd, phenotype, evidence);
                pheno0.createConceptName("Phenotype", true);
                super.parse(file);
            }

            public void parseLine(String[] data) throws FileNotFoundException, IOException {
                ONDEXConcept pheno1 = lookup.lookupPhenotype(data[2]);
                ONDEXRelation r0 = createRealtion(pheno1, pheno0, is_p);
                String id = data[3];
                if (data[3].equalsIgnoreCase("other")) {
                    id = data[2] + " other responce";
                }
                ONDEXConcept pheno2 = lookup.lookupPhenotype(id, data[2]);
                ONDEXRelation r1 = createRealtion(pheno2, pheno1, is_p);
                ONDEXConcept pheno3 = lookup.lookupPhenotype(data[6], id, data[2]);
                ONDEXRelation r2 = createRealtion(pheno3, pheno2, is_p);
                ONDEXConcept mutant = lookup.lookupMutatant(data[1]);
                ONDEXConcept treat = lookup.lookupTreatment(data[4], data[5]);
                ONDEXRelation r3 = createRealtion(mutant, treat, subjected_to);
                ONDEXRelation r4 = createRealtion(treat, pheno3, induces);
                ONDEXConcept h = lookup.lookupHormone(data[5]);
                ONDEXRelation r5 = createRealtion(treat, h, r);
                createHormoneContext(data[5], pheno1, pheno2, r0, r1, r2, r3, r4, r5, pheno3, mutant, treat);
            }
        });
        /*parserDelegates.put("plant_hormone", new  FileParser(){
              public void parseLine(String[] data) throws FileNotFoundException,	IOException {

              }
          });*/
        parserDelegates.put("plant_info", new FileParser() {
            public void parseLine(String[] data) throws FileNotFoundException, IOException {
                ONDEXConcept c = lookup.lookupMutatant(data[0]);
                if (data[1].length() != 0)
                    c.createAttribute(eco, data[1], false);
                if (data[2].length() != 0)
                    c.createAttribute(mutagenesis, data[2], false);
                if (data[3].length() != 0)
                    c.createAttribute(dominance, data[3], false);
                if (data[5].length() != 0)
                    c.createAttribute(comment, data[5], false);
                if (data[6].length() != 0)
                    c.createAttribute(mutType, data[6], false);
                String[] ids = data[5].split(";");
                for (String id : ids) {
                    if (id.length() == 0)
                        continue;
                    ONDEXConcept pub = lookup.lookupPublication(id);
                    graph.getFactory().createRelation(c, pub, pub_in, evidence);
                }
            }
        });

        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        for (File f : dir.listFiles()) {
            String simple = f.getName().split("\\.")[0];
            FileParser fp = parserDelegates.get(simple);
            if (fp != null) {
                fp.parse(f);
            }
        }
    }

    public void createHormoneContext(String hormoneName, ONDEXEntity... cs) {
        if (createHormoneContext) {
            for (ONDEXEntity c : cs) {
                ONDEXConcept h = lookup.lookupHormone(hormoneName);
                c.addTag(h);
            }
        }
    }


    public static Double getDouble(String num) {
        try {
            Double result = Double.valueOf(num);
            return result;
        }
        catch (NumberFormatException e) {
            System.err.println("Warning could not parse >" + num + "< as number.");
            return Double.NaN;
        }
    }

    public ONDEXRelation createRealtion(ONDEXConcept from, ONDEXConcept to, RelationType rt) {
        ONDEXRelation result = graph.getRelation(from, to, rt);
        if (result == null) {
            result = graph.getFactory().createRelation(from, to, rt, evidence);
        }
        return result;
    }

    private class Lookup {
        private Map<String, ONDEXConcept> pubIndex = new HashMap<String, ONDEXConcept>();
        private Map<String, ONDEXConcept> mutatnIndex = new HashMap<String, ONDEXConcept>();
        private Map<String, ONDEXConcept> treatmentIndex = new HashMap<String, ONDEXConcept>();
        private Map<String, ONDEXConcept> hormoneIndex = new HashMap<String, ONDEXConcept>();
        private Map<String, ONDEXConcept> proteinIndex = new HashMap<String, ONDEXConcept>();
        private Map<String, ONDEXConcept> experimentIndex = new HashMap<String, ONDEXConcept>();
        private Map<ArrayKey<Object>, Integer> expIndex = new HashMap<ArrayKey<Object>, Integer>();
        private Map<ArrayKey<String>, ONDEXConcept> phenotypeIndex = new HashMap<ArrayKey<String>, ONDEXConcept>();
        private final Map<String, String> treatmentCode = new HashMap<String, String>();

        public Lookup() {
            treatmentCode.put("0", "- ");
            treatmentCode.put("1", "+ ");
            treatmentCode.put("2", "Other, involving ");
        }

        public Integer getIndex(ONDEXConcept c, String att) {
            ArrayKey<Object> key = new ArrayKey<Object>(new Object[]{c.getId(), att});
            Integer count = expIndex.get(key);
            if (count == null) {
                count = 0;
            } else {
                count++;
            }
            expIndex.put(key, count);
            return count;
        }

        public ONDEXConcept lookupPublication(String id) {
            Holder h = lookupConcept(pubIndex, publication, id);
            ONDEXConcept c = h.concept;
            if (h.isNew)
                c.createConceptAccession(id, pmid, false);
            return c;
        }

        public ONDEXConcept lookupExperiment(String id) {
            Holder h = lookupConcept(experimentIndex, experiment, id);
            ONDEXConcept c = h.concept;
            if (h.isNew)
                c.createConceptName(id, true);
            return c;
        }

        public ONDEXConcept lookupMutatant(String id) {
            Holder h = lookupConcept(mutatnIndex, mutant, id);
            ONDEXConcept c = h.concept;
            if (h.isNew)
                c.createConceptName(id, true);
            return c;
        }

        public ONDEXConcept lookupPhenotype(String... ids) {
            ArrayKey<String> key = new ArrayKey<String>(ids);
            Holder h = lookupConcept(phenotypeIndex, phenotype, key);
            ONDEXConcept c = h.concept;
            if (h.isNew)
                c.createConceptName(ids[0], true);
            return c;
        }

        public ONDEXConcept lookupHormone(String id) {
            Holder h = lookupConcept(hormoneIndex, hormone, id);
            ONDEXConcept c = h.concept;
            if (h.isNew)
                c.createConceptName(id, true);
            return c;
        }

        public ONDEXConcept lookupProtein(String id, String name) {
            String id1 = id.toUpperCase();
            Holder h = lookupConcept(proteinIndex, protein, id1);
            ONDEXConcept c = h.concept;
            if (h.isNew) {
                if (c.getConceptNames().size() == 0 && name != null && name.length() != 0) {
                    c.createConceptName(name, true);
                }
            }
            return c;
        }

        public ONDEXConcept lookupTreatment(String id, String hormone) {
            String trueId = treatmentCode.get(id);
            Holder h = lookupConcept(treatmentIndex, treatment, trueId + " - " + hormone);
            ONDEXConcept c = h.concept;
            if (h.isNew)
                c.createConceptName(trueId, true);
            return c;
        }

        @SuppressWarnings("unchecked")
        private Holder lookupConcept(Map index, ConceptClass cc, Object o) {
            ONDEXConcept c = (ONDEXConcept) index.get(o);
            if (c == null) {
                if (o instanceof String) {
                    String pid = (String) o;
                    c = graph.getFactory().createConcept(pid, ahd, cc, evidence);
                    index.put(pid, c);
                } else if (o instanceof ArrayKey) {
                    ArrayKey key = (ArrayKey) o;
                    c = graph.getFactory().createConcept((String) key.getArray()[0], ahd, cc, evidence);
                    index.put(key, c);
                }
                return new Holder(c, true);
            }
            return new Holder(c, false);
        }
    }

    private class Holder {
        private final ONDEXConcept concept;
        private final boolean isNew;

        public Holder(ONDEXConcept c, boolean isNew) {
            this.concept = c;
            this.isNew = isNew;
        }
    }

    private interface FileParserInterface {
        public void parseLine(String[] data) throws FileNotFoundException, IOException;
    }

    private abstract class FileParser implements FileParserInterface {

        protected void parse(File file) throws IOException {
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\t");
                for (int i = 0; i < data.length; i++) {
                    if (data[i] != null) {
                        data[i] = data[i].trim();
                    }
                }
                parseLine(data);
            }
            br.close();
        }
    }

    @Override
    public String getId() {
        return "arabidopsis_hormone_db";
    }
}
