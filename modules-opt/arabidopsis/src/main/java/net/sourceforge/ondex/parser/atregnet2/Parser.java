package net.sourceforge.ondex.parser.atregnet2;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Parses AtRegNet files
 * <p/>
 * requires GeneInfo.tbln, reg_net.tbl, families_data.tbl
 *
 * @author lysenkoa, hindlem
 * @version 24.05.2008
 */
@Status(description = "Tested August 2013 (Artem Lysenko)", status = StatusType.STABLE)
public class Parser extends ONDEXParser implements MetaData {

    private RelationType rt_regulated_by;
    private RelationType rt_repressed_by;
    private RelationType rt_activated_by;
    private RelationType rt_m_isp;

    private ConceptClass cc_protein;
    private ConceptClass cc_gene;
    private ConceptClass cc_tf;
    private ConceptClass cc_tf_fam;

    private RelationType rt_is_a;

    private DataSource dataSource;
    private DataSource tairdataSource;
    private DataSource genbdataSource;
    private DataSource proiddataSource;

    private EvidenceType eviType;

    private AttributeName taxidAn;
    private AttributeName pmid;
    private AttributeName bev;
    private RelationType rt_en_by;
    private AttributeName aa;
    //private AttributeName na;


    /**
     * No validators required.
     *
     * @return String[]
     */
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * Returns long list of ArgumentDefinitions to facilitate parsing of tabular
     * file.
     *
     * @return ArguementDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                        FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    /**
     * Returns name of this parser.
     *
     * @return name of parser
     */
    public String getName() {
        return "AtRegNet Parser (Simple version)";
    }

    /**
     * Returns version of this parser.
     *
     * @return version of parser
     */
    public String getVersion() {
        return "24.05.2008";
    }

    /**
     * Does the actual parsing process.
     */
    public void start() throws InvalidPluginArgumentException {
        // get user defined meta data
        rt_regulated_by = graph.getMetaData().getRelationType(regulated_by);
        rt_repressed_by = graph.getMetaData().getRelationType(repressed_by);
        rt_activated_by = graph.getMetaData().getRelationType(activated_by);
        rt_m_isp = graph.getMetaData().getRelationType(M_ISP_RT);

        cc_protein = graph.getMetaData().getConceptClass(CC_PROTEIN);
        cc_gene = graph.getMetaData().getConceptClass(CC_GENE);
        cc_tf = graph.getMetaData().getConceptClass(CC_TF);
        cc_tf_fam = graph.getMetaData().getConceptClass(CC_ProteinFamily);

        rt_is_a = graph.getMetaData().getRelationType(is_a);
        rt_en_by = graph.getMetaData().getRelationType(en_by);

        dataSource = graph.getMetaData().getDataSource(at_cv);
        tairdataSource = graph.getMetaData().getDataSource(tair_cv);
        proiddataSource = graph.getMetaData().getDataSource(proID_cv);
        genbdataSource = graph.getMetaData().getDataSource(genb_cv);
        eviType = graph.getMetaData().getEvidenceType(evidence);

        taxidAn = graph.getMetaData().getAttributeName(taxidAttr);
        pmid = graph.getMetaData().getAttributeName(ATT_PMID);
        bev = graph.getMetaData().getAttributeName(ATT_BEV);
        aa = graph.getMetaData().getAttributeName(ATT_AA);
        //na = graph.getMetaData().getAttributeName(ATT_NA);

        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));


        Map<String, ONDEXConcept> genes = new Hashtable<String, ONDEXConcept>();
        Map<String, ONDEXConcept> proteins = new Hashtable<String, ONDEXConcept>();
        Map<String, ONDEXConcept> tfs = new Hashtable<String, ONDEXConcept>();
        Map<String, ONDEXConcept> tfFamilyToConceptId = new HashMap<String, ONDEXConcept>();

        Map<String, String> geneNameToFamilyName = new HashMap<String, String>();


        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir + File.separator + "families_data.tbl"));
            while (reader.ready()) {
                String current = reader.readLine();
                String[] line = current.split("\t");
                String familyName = line[0];
                String geneName = line[1];
                geneNameToFamilyName.put(geneName, familyName);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(dir + File.separator + "GeneInfo.tbl"));
            while (reader.ready()) {
                String current = reader.readLine();
                String[] line = current.split("\t");
                String geneName = line[0];

                int index = geneName.indexOf('.');
                if (index > -1) {
                    geneName = geneName.substring(0, index);
                }

                ONDEXConcept familyConcept = null;
                String familyName = geneNameToFamilyName.get(geneName);
                if (familyName != null) {
                    familyConcept = tfFamilyToConceptId.get(familyName);
                    if (familyConcept == null) {
                        familyConcept = graph.getFactory().createConcept(
                                familyName, dataSource, cc_tf_fam, eviType);
                        tfFamilyToConceptId.put(familyName, familyConcept);
                        familyConcept.createConceptName(familyName, true);
                    }
                }


                ONDEXConcept tf = tfs.get(geneName.toLowerCase());
                if (tf == null) {
                    tf = createTranscriptionFactor(geneName,
                            new String[]{line[0], line[1]},
                            line[3]);
                    tfs.put(geneName.toLowerCase(), tf);
                }

                if (familyConcept != null)
                    graph.getFactory().createRelation(tf, familyConcept,
                            rt_m_isp, eviType);

                ONDEXConcept prot = proteins.get(geneName.toLowerCase());
                if (prot == null) {
                    prot = createProtein(geneName,
                            new String[]{line[0], line[1]},
                            line[3]);
                    proteins.put(geneName.toLowerCase(), prot);
                }

                graph.getFactory().createRelation(tf, prot,
                        rt_is_a, eviType);
                if (familyConcept != null)
                    graph.getFactory().createRelation(prot, familyConcept,
                            rt_m_isp, eviType);

                ONDEXConcept gene = genes.get(geneName.toLowerCase());
                if (gene == null) {
                    gene = createGene(geneName,
                            new String[]{line[0], line[1]},
                            line[3]);
                    genes.put(geneName.toLowerCase(), gene);
                }

                graph.getFactory().createRelation(prot, gene,
                        rt_en_by, eviType);
                if (familyConcept != null)
                    graph.getFactory().createRelation(gene, familyConcept,
                            rt_m_isp, eviType);
            }

            reader.close();

            reader = new BufferedReader(new FileReader(dir + File.separator + "reg_net.tbl"));
            int i = 1;
            while (reader.ready()) {
                String current = reader.readLine();
                String[] line = current.split("\t");
                String fromId = line[1].trim();
                String toId = line[4].trim();

                String tfFamily = line[2];

                ONDEXConcept familyConcept = tfFamilyToConceptId.get(tfFamily);
                if (familyConcept == null) {
                    familyConcept = graph.getFactory().createConcept(
                            tfFamily, dataSource, cc_tf_fam, eviType);
                    tfFamilyToConceptId.put(tfFamily, familyConcept);
                    familyConcept.createConceptName(tfFamily, true);
                }

                String additionalIdsFrom[] = fromId.split("/");
                if (additionalIdsFrom.length > 1) fromId = additionalIdsFrom[0];
                String additionalIdsTo[] = fromId.split("/");
                if (additionalIdsTo.length > 1) toId = additionalIdsTo[0];

                ONDEXConcept from = tfs.get(fromId.toLowerCase());
                if (from == null) {
                    from = createTranscriptionFactor(fromId,
                            additionalIdsFrom,
                            line[0]);
                    tfs.put(fromId.toLowerCase(), from);

                }
                graph.getFactory().createRelation(from, familyConcept,
                        rt_m_isp, eviType);

                ONDEXConcept tf_prot = proteins.get(fromId.toLowerCase());
                if (tf_prot == null) {
                    tf_prot = createProtein(fromId,
                            additionalIdsFrom,
                            line[0]);
                    proteins.put(fromId.toLowerCase(), tf_prot);
                }

                graph.getFactory().createRelation(tf_prot, familyConcept,
                        rt_m_isp, eviType);
                graph.getFactory().createRelation(tf_prot, from,
                        rt_is_a, eviType);

                ONDEXConcept tf_gene = genes.get(fromId.toLowerCase());
                if (tf_gene == null) {
                    tf_gene = createGene(fromId,
                            additionalIdsFrom,
                            line[0]);
                    genes.put(fromId.toLowerCase(), tf_gene);
                }
                graph.getFactory().createRelation(tf_gene, familyConcept,
                        rt_m_isp, eviType);

                graph.getFactory().createRelation(tf_prot, tf_gene,
                        rt_en_by, eviType);


                ONDEXConcept to = genes.get(toId.toLowerCase());
                if (to == null) {
                    to = createGene(fromId,
                            additionalIdsFrom,
                            line[0]);
                    genes.put(toId.toLowerCase(), to);
                }
                ONDEXRelation relation = null;
                if (line[9].equals("Activation")) {
                    relation = graph.getFactory().createRelation(from, to, rt_activated_by, eviType);
                } else if (line[9].equals("Repression")) {
                    relation = graph.getFactory().createRelation(from, to, rt_repressed_by, eviType);
                } else if (line[9].equals("Unknown")) {
                    relation = graph.getFactory().createRelation(from, to, rt_regulated_by, eviType);
                } else {
                    System.err.println("Row: " + i + " Bad data: " + line[9]);
                }

                if (line.length >= 13) relation.createAttribute(pmid, line[12], true);
                if (line.length >= 11) relation.createAttribute(bev, line[10], false);
            }
            i++;

            reader.close();

            reader = new BufferedReader(new FileReader(dir + File.separator + "families_pep.tbl"));
            String geneName = null;
            String sequence = null;
            while (reader.ready()) {
                String current = reader.readLine();
                String[] line = current.split("\t");

                if (line.length == 2) {
                    if (geneName != null && sequence != null) {
                        ONDEXConcept proteinConcept = proteins.get(geneName.toLowerCase());
                        if (proteinConcept != null) {
                            proteinConcept.createAttribute(aa, sequence, false);
                        }
                    }


                    geneName = line[0].trim();
                    sequence = line[1].trim();
                } else {
                    sequence = sequence + line[0].trim();
                }

            }
            geneName = null;
            sequence = null;

            reader.close();

        } catch (FileNotFoundException fnfe) {
            fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(), "[Parser - start]"));
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(), "[Parser - start]"));
        }


    }

    private ONDEXConcept createTranscriptionFactor(
            String tfid,
            String[] additionalIdsFrom,
            String conceptName
    ) {
        ONDEXConcept tf = graph.getFactory().createConcept(tfid, dataSource, cc_tf, eviType);
        createAccessions(additionalIdsFrom, tf);
        if (conceptName != null && !conceptName.equalsIgnoreCase("na")) {
            tf.createConceptName(conceptName.trim(), true);
        }
        tf.createAttribute(taxidAn, String.valueOf(3702), false);
        return tf;
    }

    private ONDEXConcept createProtein(
            String proteinid,
            String[] additionalIdsFrom,
            String conceptName
    ) {
        ONDEXConcept tf_prot = graph.getFactory()
                .createConcept(proteinid, dataSource, cc_protein, eviType);
        createAccessions(additionalIdsFrom, tf_prot);
        if (conceptName != null && !conceptName.equalsIgnoreCase("na")) {
            tf_prot.createConceptName(conceptName.trim(), true);
        }
        tf_prot.createAttribute(taxidAn, String.valueOf(3702), false);
        return tf_prot;
    }

    private ONDEXConcept createGene(String geneid,
                                    String[] additionalIdsFrom,
                                    String conceptName) {
        ONDEXConcept gene = graph.getFactory()
                .createConcept(geneid, dataSource, cc_gene, eviType);
        createAccessions(additionalIdsFrom, gene);
        if (conceptName != null && !conceptName.equalsIgnoreCase("na")) {
            gene.createConceptName(conceptName.trim(), true);
        }
        gene.createAttribute(taxidAn, String.valueOf(3702), false);
        return gene;
    }

    private Pattern atgPattern = Pattern.compile("AT[0-9]G[0-9]+([.][0-9]+)?", Pattern.CASE_INSENSITIVE);
    private Pattern proidPattern = Pattern.compile("[A-Z][A-Z][A-Z][0-9]+([.][0-9]+)?", Pattern.CASE_INSENSITIVE);
    private Pattern uniprotPattern = Pattern.compile("[A-Z][0-9]+", Pattern.CASE_INSENSITIVE);

    private void createAccessions(String[] additionalIdsFrom, ONDEXConcept concept) {
        for (String id : additionalIdsFrom) {
            id.replaceAll(";supported", "");
            id.replaceAll(";", "");

            if (atgPattern.matcher(id.toUpperCase()).matches()) {
                boolean ambiguous = true;
                if (id.contains(".")) {
                    ambiguous = false;
                }
                concept.createConceptAccession(id.toUpperCase(), tairdataSource, ambiguous);

                int spliceDot = id.indexOf('.');
                if (spliceDot > -1) {
                    String locus = id.substring(0, spliceDot);
                    concept.createConceptAccession(locus, tairdataSource, true);
                }
            } else if (proidPattern.matcher(id.toUpperCase()).matches()) {
                concept.createConceptAccession(id.toUpperCase(), proiddataSource, true);
            } else if (uniprotPattern.matcher(id.toUpperCase()).matches()) {
                concept.createConceptAccession(id.toUpperCase(), genbdataSource, true);
            }
        }
    }

    @Override
    public String getId() {
        return "atregnet2";
    }
}
