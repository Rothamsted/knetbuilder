package net.sourceforge.ondex.parser.ahd;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.*;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.auxfunctions.TabArrayObject;
import net.sourceforge.ondex.tools.auxfunctions.TabDelimited;
import net.sourceforge.ondex.tools.auxfunctions.tuples.Pair;
import net.sourceforge.ondex.tools.auxfunctions.tuples.Triple;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This is a parser for the Arabidopsis Hormone Database
 * <p/>
 * The data for this is not public, it is a tab delimited dump from the authors.
 * <p/>
 * This is finished although the microarray information is commented out as it's not quite clear
 * what the microarray information adds in this case. *
 * <p/>
 * There is some representational issues here due to relations not being able to take multiple GDSes
 * and/or concepts not being able to have multiple relations between them with the same type.
 * <p/>
 * The data has been merged whenever needed and defaults to worse case scenario for the reliability
 * information...
 * <p/>
 * Further details in the code itself where this is done.
 * <p/>
 * There is a large amount of TabArrayObject resetting, this is for safety reasons.
 *
 * @author sckuo
 */

public class Parser extends ONDEXParser
{

    private EntityFactory ef;

    private static DataSource dataSourceAHD;
    private static DataSource dataSourceTair;
    private static DataSource dataSourcePubMed;

    private static EvidenceType etIMPD;
    private static EvidenceType etGI;
    private static EvidenceType etGOA;

    private static ConceptClass ccAHDPO;
    private static ConceptClass ccHormone;
    private static ConceptClass ccMutant;
    private static ConceptClass ccExp;
    private static ConceptClass ccGene;
    private static ConceptClass ccPublication;
    private static ConceptClass ccMicro;

    private static RelationType rtISA; // is a
    private static RelationType rtPP; // mutant participates in an experiment
    private static RelationType rtOP; // observed phenotype
    private static RelationType rtHasMutatedGene; // has mutated gene
    private static RelationType rtPub;
    private static RelationType rtTreat; // mutant (+) hormone = phenotype
    private static RelationType rtNotTreat; // mutant (-) hormone = phenotype
    private static RelationType rtAssoc; // mutant (?) hormone = phenotype

    private static RelationType rtContrib;

    private static AttributeName anTLConf;
    private static AttributeName anDominance;
    private static AttributeName anMut;
    private static AttributeName anEco;
    private static AttributeName anPlantType;
    private static AttributeName anDesc;
    private static AttributeName anSite;
    private static AttributeName anGOA;
    private static AttributeName anGeneRole;
    private static AttributeName anMicro;


    // declaring elements to hold the Phenotype Ontology
    HashMap<String, ONDEXConcept> first_level_elements;
    HashMap<Pair, ONDEXConcept> second_level_elements;
    HashMap<Triple, ONDEXConcept> third_level_elements;

    // declaring elements to hold plants
    HashMap<String, ONDEXConcept> plants;

    // declaring elements to hold hormones
    HashMap<String, ONDEXConcept> hormones;

    // declaring elements to hold genes
    HashMap<String, ONDEXConcept> genes;

    // declaring publications
    HashMap<String, ONDEXConcept> publications;

    // declaring Microarray experiments
    HashMap<String, ONDEXConcept> microarray;


    private void initaliseMetaData() throws AttributeNameMissingException, RelationTypeMissingException, ConceptClassMissingException, EvidenceTypeMissingException, DataSourceMissingException {
        dataSourceAHD = requireDataSource(MetaData.ahd_cv);
        dataSourceTair = requireDataSource(MetaData.tair_cv);
        dataSourcePubMed = requireDataSource(MetaData.pubmed_cv);

        etIMPD = requireEvidenceType(MetaData.evidence);
        etGOA = requireEvidenceType(MetaData.etGOA);
        etGI = requireEvidenceType(MetaData.etGI);

        ccAHDPO = requireConceptClass(MetaData.ahdpo);
        ccMicro = requireConceptClass(MetaData.microarrayexp);
        ccMutant = requireConceptClass(MetaData.mutant);
        ccHormone = requireConceptClass(MetaData.hormone);
        ccExp = requireConceptClass(MetaData.exp);
        ccGene = requireConceptClass(MetaData.gene);
        ccPublication = requireConceptClass(MetaData.publication);

        rtISA = requireRelationType(MetaData.rtISA);
        rtPP = requireRelationType(MetaData.rtPP);
        rtOP = requireRelationType(MetaData.rtOP);
        rtHasMutatedGene = requireRelationType(MetaData.rtHasMutatedGene);
        rtPub = requireRelationType(MetaData.rtPub);
        rtNotTreat = requireRelationType(MetaData.rtNotTreat);
        rtTreat = requireRelationType(MetaData.rtTreat);
        rtAssoc = requireRelationType(MetaData.rtAssoc);

        rtContrib = requireRelationType(MetaData.rtContrib);

        anTLConf = requireAttributeName(MetaData.antlconf);
        anDominance = requireAttributeName(MetaData.anDominance);
        anMut = requireAttributeName(MetaData.anMut);
        anEco = requireAttributeName(MetaData.anEco);
        anPlantType = requireAttributeName(MetaData.anPT);
        anDesc = requireAttributeName(MetaData.anDesc);
        anSite = requireAttributeName(MetaData.anSite);
        anGeneRole = requireAttributeName(MetaData.anGeneRole);
        anGOA = requireAttributeName(MetaData.anGOA);
        anMicro = requireAttributeName(MetaData.anMicro);

        /*
           *
           * Initalise place holders
           *
           */

        first_level_elements = new HashMap<String, ONDEXConcept>();
        second_level_elements = new HashMap<Pair, ONDEXConcept>();
        third_level_elements = new HashMap<Triple, ONDEXConcept>();

        plants = new HashMap<String, ONDEXConcept>();
        hormones = new HashMap<String, ONDEXConcept>();

        genes = new HashMap<String, ONDEXConcept>();
        publications = new HashMap<String, ONDEXConcept>();
        microarray = new HashMap<String, ONDEXConcept>();

    }

    /**
     * No arguments as yet.
     *
     * @return ArguementDefinition<?>[]
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR, FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    @Override
    public void start() throws Exception {

        initaliseMetaData();

        // Get entity factory
        ef = graph.getFactory();

        // holder for things to come
        TabArrayObject tao;

        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        // grab files folder
        String input_dir = dir.getAbsolutePath() + File.separator;


        // TODO:


        /*
           * Start of parsing of td_phenotype
           *
           */

        TabDelimited td_phenotype = new TabDelimited(input_dir + "phenotype.csv");

        // create wildtype phenotype
        ONDEXConcept wildtype = ef.createConcept("", dataSourceAHD, ccAHDPO, etIMPD);
        wildtype.createConceptName("wild type", true);

        while ((tao = td_phenotype.getNext()) != null) {

            // deal with the top level ontology elements
            String organ = (String) tao.getElement(2);
            ONDEXConcept first_level_element = null;

            if (organ.equals("") || organ == null) {

                System.out.println("Error");

            } else {

                if (first_level_elements.containsKey(organ)) {

                    first_level_element = first_level_elements.get(organ);

                } else {

                    first_level_element = ef.createConcept("", dataSourceAHD, ccAHDPO, etIMPD);
                    first_level_element.createConceptName(organ, true);
                    first_level_elements.put(organ, first_level_element);

                }

            }

            // deal with second level ontology elements
            String attribute = (String) tao.getElement(3);
            ONDEXConcept second_level_element = null;

            Pair oa = new Pair(organ, attribute);

            if (attribute.equals("") || attribute == null) {

                System.out.println("Error");

            } else {

                if (second_level_elements.containsKey(oa)) {

                    second_level_element = second_level_elements.get(oa);

                } else {

                    second_level_element = ef.createConcept("", dataSourceAHD, ccAHDPO, etIMPD);
                    second_level_element.createConceptName(attribute, true);
                    second_level_elements.put(oa, second_level_element);

                    // create relation between first and second level elements

                    ef.createRelation(second_level_element, first_level_element, rtISA, etIMPD);

                }

            }

            // deal with third level ontology elements
            String phenotype_desc = (String) tao.getElement(6);
            ONDEXConcept third_level_element = null;
            Triple oap = new Triple(organ, attribute, phenotype_desc);

            if (phenotype_desc.equals("") || phenotype_desc == null) {

                System.out.println("Error");

            } else {

                if (third_level_elements.containsKey(oap)) {

                    third_level_element = third_level_elements.get(oap);

                } else {

                    third_level_element = ef.createConcept("", dataSourceAHD, ccAHDPO, etIMPD);
                    third_level_element.createConceptName(phenotype_desc, true);
                    third_level_elements.put(oap, third_level_element);

                    // create relation between third and second level elements

                    ef.createRelation(third_level_element, second_level_element, rtISA, etIMPD);

                }

            }

            // get or creating hormones
            ONDEXConcept hormone = getHormone((String) tao.getElement(5));

            // get or creating plants
            ONDEXConcept plant = getPlant((String) tao.getElement(1));

            // create experiments
            ONDEXConcept experiment = ef.createConcept("", dataSourceAHD, ccExp, etIMPD);
            experiment.createConceptAccession((String) tao.getElement(0), dataSourceAHD, false);

            // linking experiments to phenotypes
            ef.createRelation(experiment, third_level_element, rtOP, etIMPD);

            // linking experiments to plants
            ef.createRelation(plant, experiment, rtPP, etIMPD);

            // linking experiments to hormone
            String has_hormone = (String) tao.getElement(4);
            if (has_hormone.equals("0")) {

                ef.createRelation(experiment, hormone, rtNotTreat, etIMPD);

            } else if (has_hormone.equals("1")) {

                ef.createRelation(experiment, hormone, rtTreat, etIMPD);

            } else if (has_hormone.equals("2")) {

                ef.createRelation(experiment, hormone, rtAssoc, etIMPD);

            } else {

                System.err.println("Error in phenotype - column 4");
                tao.debug();

            }


        }

        // reset tab array object
        tao = null;
        td_phenotype = null;


        /*
           * Start of parsing of td_plant_hormone
           *
           */

        TabDelimited td_plant_hormone = new TabDelimited(input_dir + "plant_hormone.csv");

        while ((tao = td_plant_hormone.getNext()) != null) {

            String plantName = (String) tao.getElement(1);
            String hormoneName = (String) tao.getElement(2);

            ONDEXConcept plant = getPlant(plantName);
            ONDEXConcept hormone = getHormone(hormoneName);

            ONDEXRelation r = ef.createRelation(plant, hormone, rtAssoc, etIMPD);

            if (tao.getElement(3).equals("yes")) {
                r.createAttribute(anTLConf, true, false);
            } else if (tao.getElement(3).equals("no")) {
                r.createAttribute(anTLConf, false, false);
            } else {

                System.err.println("Error in plant hormone - column 3");
                tao.debug();

            }

        }

        // reset tab array object
        tao = null;
        td_plant_hormone = null;

        /*
           * Start of parsing of td_plant_info
           *
           */

        TabDelimited td_plant_info = new TabDelimited(input_dir + "plant_info.csv");

        while ((tao = td_plant_info.getNext()) != null) {

            ONDEXConcept plant = getPlant((String) tao.getElement(0));

            String eco = (String) tao.getElement(1);
            if (!eco.equals("")) {
                plant.createAttribute(anEco, eco, false);
            }

            String mut = (String) tao.getElement(2);
            if (!mut.equals("")) {
                plant.createAttribute(anMut, mut, false);
            }


            String dom = (String) tao.getElement(3);
            if (!dom.equals("")) {
                plant.createAttribute(anDominance, dom, false);
            }

            String plantType = (String) tao.getElement(6);
            if (!plantType.equals("")) {
                plant.createAttribute(anPlantType, plantType, false);
            }

            String desc = (String) tao.getElement(5);
            if (!desc.equals("")) {
                plant.createAttribute(anDesc, desc, false);
            }

            String pmidsList = (String) tao.getElement(4);
            if (!pmidsList.equals("")) {

                if (pmidsList.contains(";")) {

                    String[] pmidsL = pmidsList.split(";");
                    for (String n : pmidsL) {

                        ONDEXConcept pub = getPub(n);
                        ef.createRelation(plant, pub, rtPub, etIMPD);

                    }

                } else {

                    ONDEXConcept pub = getPub(pmidsList);
                    ef.createRelation(plant, pub, rtPub, etIMPD);

                }

            }

        }

        // reset tab array object
        tao = null;
        td_plant_info = null;

        TabDelimited td_gene_hormone_plant = new TabDelimited(input_dir + "gene_hormone_plant.csv");

        while ((tao = td_gene_hormone_plant.getNext()) != null) {

            String geneAcc = (String) tao.getElement(1);
            ONDEXConcept g = getGene(geneAcc);

            // AHD names
            String geneName = (String) tao.getElement(2);

            if (!geneName.equals("")) {

                if (geneName.contains(",")) {

                    String[] gNames = geneName.split(",");
                    for (String n : gNames) {

                        if (g.getConceptName(n) == null) {
                            g.createConceptName(n, true);
                        }
                    }

                } else {

                    if (g.getConceptName(geneName) == null) {
                        g.createConceptName(geneName, true);
                    }

                }

            }

            // linking genes to plants
            String plantName = (String) tao.getElement(4);
            ONDEXConcept plant = getPlant(plantName);
            ONDEXRelation hasMutatedGene = graph.getRelation(plant, g, rtHasMutatedGene);
            if (graph.getRelation(plant, g, rtHasMutatedGene) == null) {
                hasMutatedGene = ef.createRelation(plant, g, rtHasMutatedGene, etIMPD);
            }


            // linking genes to hormones
            // TODO: Find better relation
            String hormoneName = (String) tao.getElement(3);
            ONDEXConcept hormone = getHormone(hormoneName);
            if (graph.getRelation(g, hormone, rtAssoc) == null) {
                ef.createRelation(g, hormone, rtAssoc, etIMPD);
            }

            // annotate with mutated site

            try {
                String site = (String) tao.getElement(5);

                if (!site.equals("")) {

                    if (!hasMutatedGene.getAttribute(anSite).getValue().equals(site)) {
                        hasMutatedGene.createAttribute(anSite, site, false);
                    }
                }
            } catch (Exception e) {

                // do nothing

            }


        }

        // reset tab array object
        tao = null;
        td_gene_hormone_plant = null;

        TabDelimited td_gene_hormone_info = new TabDelimited(input_dir + "gene_hormone_info.csv");

        while ((tao = td_gene_hormone_info.getNext()) != null) {

            String gAcc = (String) tao.getElement(1);
            String gName = (String) tao.getElement(2);

            ONDEXConcept gene = getGene(gAcc);

            if ((!gName.equals("")) && gene.getConceptName(gName) == null) {

                gene.createConceptName(gName, true);

            }

            String hormoneName = (String) tao.getElement(3);

            ONDEXConcept hormone = getHormone(hormoneName);

            ONDEXRelation geneToHormone = graph.getRelation(gene, hormone, rtAssoc);

            if (geneToHormone == null) {
                geneToHormone = ef.createRelation(gene, hormone, rtAssoc, etIMPD);
            }

            String hormoneRole = (String) tao.getElement(4);

            if (!hormoneRole.equals("-") && !hormoneRole.equals("")) {

                if (geneToHormone.getAttribute(anGeneRole) == null) {
                    geneToHormone.createAttribute(anGeneRole, hormoneRole, false);
                } else if (geneToHormone.getAttribute(anGeneRole).getValue().equals(hormoneRole)) {
                    // do nothing
                } else {
                    tao.debug(); // some kind of duplication going on...
                }
            }

            String geneDesc = (String) tao.getElement(5);

            // there is a problem here, some gene-hormone relationships have multiple valid descriptions...
            // but the Attribute system only supports one description

            if (!geneDesc.equals("")) {

                Attribute attribute = geneToHormone.getAttribute(anDesc);

                if (attribute == null) {

                    geneToHormone.createAttribute(anDesc, geneDesc, false);

                } else if (attribute.getValue().equals(geneDesc)) {

                    // do nothing

                } else {

                    //System.err.println("problem here " + (String) tao.getElement(1));
                    // appending the new description onto the old one
                    attribute.setValue(attribute.getValue() + " ; " + geneDesc);

                }
            }


            // still potentially same problems as above
            Attribute sureattribute = geneToHormone.getAttribute(anTLConf);
            String sure = (String) tao.getElement(7);
            if (sureattribute == null) {

                if (sure.equals("yes")) {

                    geneToHormone.createAttribute(anTLConf, true, false);

                } else if (sure.equals("no")) {

                    geneToHormone.createAttribute(anTLConf, false, false);

                } else {

                    System.err.println("Error in gene-hormone info - column 7");
                    tao.debug();

                }

            } else if (sureattribute.getValue().equals(sure)) {
                // do nothing
            } else {

                // default to no if there's a conflict
                sureattribute.setValue(false);

            }

            // still same problem as above
            // evidence issue, when 2 or more things have one from and one not from go
            // defaults to go if there's a clash
            Attribute goaattribute = geneToHormone.getAttribute(anGOA);
            String go = (String) tao.getElement(8);
            if (goaattribute == null) {

                if (go == null || go.isEmpty()) {

                    geneToHormone.addEvidenceType(etGI);

                } else if (go.equals("GO")) {

                    geneToHormone.addEvidenceType(etGOA);
                    //geneToHormone.createGDS(anGOA, "yes", false);

                } else if (go.equals("other")) {

                    geneToHormone.addEvidenceType(etIMPD);
                }

            } else if (goaattribute.getValue().equals(go)) {
                // do nothing
            } else {

                // default to go if there's a conflict
                goaattribute.setValue("GO");

            }

            // tying all publication information to the gene only, even though it's not quite true..
            // the publication should really be tied to the relations, but one cannot set up a relation
            // between a relation and a node
            String pimds = (String) tao.getElement(6);

            if (pimds.equals("")) {

            } else if (pimds.contains(";")) {

                String[] pids = pimds.split(";");

                for (String p : pids) {

                    ONDEXConcept pub = getPub(p);

                    if (graph.getRelation(gene, pub, rtPub) == null) {

                        ef.createRelation(gene, pub, rtPub, etIMPD);

                    }

                }

            } else {


                // get publication or create
                ONDEXConcept pub = getPub(pimds);

                // make relation if none already exists
                if (graph.getRelation(gene, pub, rtPub) == null) {

                    ef.createRelation(gene, pub, rtPub, etIMPD);

                }

            }

        }

        /*
           * Microarray stuff follows here
           *

          // reset tab array object
          tao = null;
          td_gene_hormone_info = null;

          TabDelimited td_gene_hormone_microarray = new TabDelimited(input_dir+"gene_hormone_microarray.csv");

          while ((tao = td_gene_hormone_microarray.getNext()) != null) {

              String row_id = (String) tao.getElement(0);
              String exp_id = (String) tao.getElement(1);
              String hormoneName = (String) tao.getElement(2);
              String locusName = (String) tao.getElement(3);
              String microArrayData = (String) tao.getElement(4);

              ONDEXConcept hormone = getHormone(hormoneName);
              ONDEXConcept gene = getGene(locusName);
              ONDEXConcept mexp = getExp(exp_id);

              ONDEXConcept c = ef.createConcept("", dataSourceAHD, ccExp, etIMPD);
              c.createConceptAccession(row_id, dataSourceAHD, false);

              ef.createRelation(c, mexp, rtContrib, etIMPD);
              ef.createRelation(c, hormone, rtAssoc, etIMPD);
              ef.createRelation(gene, c, rtPP, etIMPD);

              c.createGDS(anMicro, microArrayData, false);


          }

          // reset tab array object
          tao = null;
          td_gene_hormone_microarray = null;

          // microarray info
          // there is some stuff not parsed, but not sure about the value of them
          TabDelimited microarray_info = new TabDelimited(input_dir+"microarray_info.csv");

          while ((tao = microarray_info.getNext()) != null) {

              String exp_id = (String) tao.getElement(0);
              String plantName = (String) tao.getElement(2);
              String hormoneName = (String) tao.getElement(4);
              String pmid = ((String) tao.getElement(9)).substring(6); // trim the "PMID: "
              String url = (String) tao.getElement(11);



              ONDEXConcept mexp = getExp(exp_id);
              ONDEXConcept pub = getPub(pmid);
              ONDEXConcept hormone = getHormone(hormoneName);
              ONDEXConcept plant = getPlant(plantName);


              ef.createRelation(mexp, pub, rtPub, etIMPD);
              ef.createRelation(hormone, mexp, rtAssoc, etIMPD);
              ef.createRelation(plant, mexp, rtPP, etIMPD);

              mexp.createGDS(anMicro, url, false);




          }

          */

    }


    /**
     * Returns name of this parser.
     *
     * @return String
     */
    @Override
    public String getName() {
        return "Arabidopsis Hormone Database Parser";
    }

    /**
     * Returns the version of this parser.
     *
     * @return String
     */
    @Override
    public String getVersion() {
        return "2009-10-02";
    }

    /**
     * No validators required.
     *
     * @return String[]
     */
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /*
      * Fetches a plant from the hashMap or creates a new plant with that name
      *
      *
      */

    public ONDEXConcept getPlant(String a) {

        ONDEXConcept p = null;

        if (plants.containsKey(a)) {

            p = plants.get(a);

        } else {

            p = ef.createConcept("", dataSourceAHD, ccMutant, etIMPD);
            p.createConceptName(a, true);
            plants.put(a, p);

        }

        return p;

    }

    public ONDEXConcept getHormone(String a) {
    	
    	a = a.trim();

        ONDEXConcept h = null;

        if (hormones.containsKey(a)) {

            h = hormones.get(a);

        } else {

            h = ef.createConcept("", dataSourceAHD, ccHormone, etIMPD);
            h.createConceptName(a, true);
            hormones.put(a, h);

        }

        return h;

    }

    public ONDEXConcept getGene(String a) {

        ONDEXConcept g = null;
        String b = a;
        a = a.toLowerCase();

        if (genes.containsKey(a)) {

            g = genes.get(a);

        } else {

            g = ef.createConcept("", dataSourceAHD, ccGene, etIMPD);
            genes.put(a, g);


            Pattern pattern = Pattern.compile("AT\\dG\\d+");

            Matcher matcher = pattern.matcher(b);
            // AHD can contain both ATG accessions and gene names in the same column
            if (matcher.find()) {

                if (g.getConceptAccession(b, dataSourceTair) == null) {
                    g.createConceptAccession(b, dataSourceTair, true);
                }
            } else {

                if (g.getConceptName(b) == null) {
                    g.createConceptName(b, true);
                }
            }

        }

        return g;

    }

    public ONDEXConcept getPub(String a) {

        ONDEXConcept p = null;

        if (publications.containsKey(a)) {

            p = publications.get(a);

        } else {

            p = ef.createConcept("", dataSourceAHD, ccPublication, etIMPD);
            p.createConceptAccession(a, dataSourcePubMed, false);
            publications.put(a, p);

        }

        return p;

    }


    public ONDEXConcept getExp(String a) {

        ONDEXConcept exp = null;

        if (microarray.containsKey(a)) {

            exp = microarray.get(a);

        } else {

            exp = ef.createConcept("", dataSourceAHD, ccMicro, etIMPD);
            exp.createConceptName(a, true);
            microarray.put(a, exp);

        }

        return exp;

    }


    @Override
    public String getId() {
        return "ahd";
    }
}
