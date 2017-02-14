package net.sourceforge.ondex.parser.plntfdb;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.exception.type.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createAttName;


/**
 * PlnTFDB (2.0) is a public database arising from efforts to identify and catalogue all Plant genes involved in transcriptional control.
 * http://plntfdb.bio.uni-potsdam.de/v2.0/
 *
 * @author hindlem
 */
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Parser extends ONDEXParser implements ArgumentNames, MetaData {

    private ConceptClass ccTF;
    private ConceptClass ccProtein;
    private ConceptClass tf_fam_cc;

    private DataSource dataSourcePLNTFDB;

    private AttributeName taxAttr;
    private AttributeName aaAttr;

    private EvidenceType evi;

    private RelationType is_a;
    private RelationType en_by;
    private RelationType is_part;

    @Override
    public String[] requiresValidators() {
        return new String[]{"taxonomy"};
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(
                        FileArgumentDefinition.INPUT_DIR,
                        "directory with plntfdb files", true, true, true, false)
        };
    }

    @Override
    public String getName() {
        return "PlnTFDB (2.0) parser";
    }

    @Override
    public String getVersion() {
        return "30 June 2008";
    }

    @Override
    public String getId() {
        return "plntfdb";
    }

    @Override
    public void start() throws DataSourceMissingException, ConceptClassMissingException, RelationTypeMissingException, AttributeNameMissingException, EvidenceTypeMissingException, InvalidPluginArgumentException {
        Set<String> missingIds = new HashSet<String>();
        File path = new File((String) args
                .getUniqueValue(FileArgumentDefinition.INPUT_DIR));
        if (path == null || !path.exists()) {
            fireEventOccurred(new DataFileMissingEvent("The directory is missing :" + path.getAbsolutePath(), getCurrentMethodName()));
            return;
        }
        AttributeName is_tf = createAttName(graph, tfAttr, String.class);
        Pattern tabSplit = Pattern.compile("\t");

        File tfFile = null;
        File pepFile = null;
        //File pfamFile = null;

        String[] files = path.list();
        for (String file : files) {
            File fileReal = new File(path.getAbsolutePath() + File.separator + file);

            if (file.startsWith("PlnTFDB_tf")) {
                tfFile = fileReal;
            } else if (file.startsWith("PlnTFDB_PEP")) {
                pepFile = fileReal;
            }// else if (file.startsWith("PlnTFDB_PFAM_hits")) {
            //	pfamFile = fileReal;
            //}
        }

        HashMap<String, Integer> tfToTFConcept = new HashMap<String, Integer>();
        HashMap<String, Integer> tfToProtConcept = new HashMap<String, Integer>();

        initMetaData();
        Map<String, Integer> tfFamilyToConceptId = new HashMap<String, Integer>();

        if (tfFile != null) {

            try {
                BufferedReader bir = new BufferedReader(new FileReader(tfFile));
                while (bir.ready()) {
                    String line = bir.readLine();
                    String[] values = tabSplit.split(line);
                    String species = values[0].trim();

                    if (species.length() == 0 || species.equalsIgnoreCase("species")) {
                        continue; //its the header or a blank line
                    }

                    int taxId = Integer.parseInt((String) ValidatorRegistry.validators.get("taxonomy").validate(species));

                    String geneName = values[1].trim();
                    String tfFamily = values[2].trim();

                    String uniqueName = ((species + "_" + geneName).replaceAll(" ", "_")).toLowerCase();

                    ONDEXConcept familyConcept = null;

                    Integer id = tfFamilyToConceptId.get(tfFamily);
                    if (id == null) {
                        familyConcept = graph.getFactory().createConcept(
                                tfFamily, dataSourcePLNTFDB, tf_fam_cc, evi);
                        tfFamilyToConceptId.put(tfFamily, familyConcept.getId());
                        familyConcept.createConceptName(tfFamily, true);
                    } else {
                        familyConcept = graph.getConcept(id);
                    }

                    if (!tfToTFConcept.containsKey(uniqueName)) { //ignore duplicates
                        ONDEXConcept tf = graph.getFactory().createConcept(geneName, "Is part of transcription factor family " + tfFamily, dataSourcePLNTFDB, ccTF, evi);
                        tf.createAttribute(is_tf, "Yes", false);
                        tf.createAttribute(taxAttr, String.valueOf(taxId), false);
                        parseAccessions(taxId, geneName, tf, false);

                        tfToTFConcept.put(uniqueName, tf.getId());

                        ONDEXConcept protein = graph.getFactory().createConcept(geneName, dataSourcePLNTFDB, ccProtein, evi);
                        protein.createAttribute(taxAttr, String.valueOf(taxId), false);

                        parseAccessions(taxId, geneName, protein, false);

                        tfToProtConcept.put(uniqueName, protein.getId());

                        graph.getFactory().createRelation(protein, tf, is_a, evi);

                        graph.getFactory().createRelation(protein, familyConcept,
                                is_part, evi);
                        graph.getFactory().createRelation(tf, familyConcept,
                                is_part, evi);
                    }
                }
                bir.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            fireEventOccurred(new DataFileMissingEvent("Transcription factor file not found in :" + path.getAbsolutePath(), getCurrentMethodName()));
            return;
        }

        if (pepFile != null) {
            try {
                String uniqueId = null;
                StringBuilder peptideSeq = new StringBuilder();

                BufferedReader bir = new BufferedReader(new FileReader(pepFile));
                while (bir.ready()) {
                    String line = bir.readLine();
                    if (line.startsWith(">")) {
                        if (uniqueId != null && peptideSeq.length() > 0) {
                            Integer proteinId = tfToProtConcept.get(uniqueId.toLowerCase());
                            if (proteinId == null) {
                                missingIds.add(uniqueId.toLowerCase());
                                continue;
                            }

                            ONDEXConcept protein = graph.getConcept(proteinId);
                            protein.createAttribute(aaAttr, peptideSeq.toString(), false);
                        }
                        uniqueId = line.substring(1).trim();
                        peptideSeq.setLength(0);
                    } else {
                        peptideSeq.append(line.trim());
                    }
                }
                bir.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            fireEventOccurred(new DataFileMissingEvent("TF Peptide file not found in :" + path.getAbsolutePath(), getCurrentMethodName()));

        }
        System.err.println("Missing ids: " + missingIds);

    }

    private void initMetaData() throws ConceptClassMissingException, RelationTypeMissingException, AttributeNameMissingException, EvidenceTypeMissingException, DataSourceMissingException {
        // ConceptClasses
        ccTF = graph.getMetaData().getConceptClass(CC_TRANS_FACTOR);
        if (ccTF == null)
            throw new ConceptClassMissingException(CC_TRANS_FACTOR);

        ccProtein = graph.getMetaData().getConceptClass(CC_PROTEIN);
        if (ccProtein == null)
            throw new ConceptClassMissingException(CC_PROTEIN);

        tf_fam_cc = graph.getMetaData().getConceptClass(MetaData.CC_ProteinFamily);
        if (tf_fam_cc == null)
            throw new ConceptClassMissingException(MetaData.CC_ProteinFamily);

        dataSourcePLNTFDB = graph.getMetaData().getDataSource(CV_PLNTFDB);
        if (dataSourcePLNTFDB == null)
            throw new DataSourceMissingException(CV_PLNTFDB);

        // AttributeNames
        taxAttr = graph.getMetaData().getAttributeName(ATT_TAXID);
        if (taxAttr == null)
            throw new AttributeNameMissingException(ATT_TAXID);

        evi = graph.getMetaData().getEvidenceType(ET_IMPD);
        if (evi == null)
            throw new EvidenceTypeMissingException(ET_IMPD);

        aaAttr = graph.getMetaData().getAttributeName(ATT_AA);
        if (aaAttr == null)
            throw new AttributeNameMissingException(ATT_AA);

        en_by = graph.getMetaData().getRelationType(EN_BY_RT);
        if (en_by == null)
            throw new RelationTypeMissingException(EN_BY_RT);

        is_a = graph.getMetaData().getRelationType(IS_A_RT);
        if (is_a == null)
            throw new RelationTypeMissingException(IS_A_RT);

        is_part = graph.getMetaData().getRelationType(M_ISP_RT);
        if (is_part == null)
            throw new RelationTypeMissingException(M_ISP_RT);

    }

    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

    private DataSource tairDataSource = null;
    private DataSource tigrDataSource = null;
    private DataSource maizeDataSource = null;
    private DataSource sugerCaneDataSource = null;
    private DataSource sorgumDataSource = null;
    private DataSource poplar_jgiDataSource = null;
    private DataSource moss_jgiDataSource = null;
    private DataSource chlam_re_jgi = null;
    private DataSource green_algea_jgiDataSource = null;
    private DataSource cyan_mer_DataSource = null;

    private void parseAccessions(int taxid, String accession, ONDEXConcept concept, boolean ambiguous) throws DataSourceMissingException {

        int spliceDot = accession.indexOf(".");
        if (spliceDot > -1) {
            String locus = accession.substring(0, spliceDot);
            parseAccessions(taxid, locus, concept, true);
        }

        if (taxid == 3702) {
            if (tairDataSource == null)
                tairDataSource = graph.getMetaData().getDataSource(CV_TAIR);
            if (tairDataSource == null) throw new DataSourceMissingException(CV_TAIR);

            concept.createConceptAccession(accession, tairDataSource, ambiguous);
        } else if (taxid == 39946 || taxid == 39947 || taxid == 450) {
            if (tigrDataSource == null)
                tigrDataSource = graph.getMetaData().getDataSource(CV_TIGR);
            if (tigrDataSource == null) throw new DataSourceMissingException(CV_TIGR);

            concept.createConceptAccession(accession, tigrDataSource, ambiguous);
        } else if (taxid == 4577 || taxid == 112001 || taxid == 381124
                || taxid == 334825 || taxid == 4579 || taxid == 76912) { //rice
            if (maizeDataSource == null)
                maizeDataSource = graph.getMetaData().getDataSource(MAIZE_CV);
            if (maizeDataSource == null) throw new DataSourceMissingException(MAIZE_CV);

            concept.createConceptAccession(accession, maizeDataSource, ambiguous);
        } else if (taxid == 4547 || taxid == 128810) { //sugarcane Saccharum_officinarum 4547
            if (sugerCaneDataSource == null)
                sugerCaneDataSource = graph.getMetaData().getDataSource(SUGERCANE_CV);
            if (sugerCaneDataSource == null) throw new DataSourceMissingException(SUGERCANE_CV);

            concept.createConceptAccession(accession, sugerCaneDataSource, ambiguous);
        } else if (taxid == 4557 || taxid == 4558) { //sorghum  Sorghum bicolor 4558
            if (sorgumDataSource == null)
                sorgumDataSource = graph.getMetaData().getDataSource(SORGUM_CV);
            if (sorgumDataSource == null) throw new DataSourceMissingException(SORGUM_CV);

            concept.createConceptAccession(accession, sorgumDataSource, ambiguous);
        } else if (taxid == 45157) { //Cyanidioschyzon merolae
            if (cyan_mer_DataSource == null)
                cyan_mer_DataSource = graph.getMetaData().getDataSource(CY_MER_CV);
            if (cyan_mer_DataSource == null) throw new DataSourceMissingException(CY_MER_CV);

            concept.createConceptAccession(accession, cyan_mer_DataSource, ambiguous);
        } else if (taxid == 3694) { //Populus trichocarpa
            if (poplar_jgiDataSource == null)
                poplar_jgiDataSource = graph.getMetaData().getDataSource(POPLAR_CV);
            if (poplar_jgiDataSource == null) throw new DataSourceMissingException(POPLAR_CV);

            concept.createConceptAccession(accession, poplar_jgiDataSource, ambiguous);
        } else if (taxid == 3218) { //Physcomitrella patens
            if (moss_jgiDataSource == null)
                moss_jgiDataSource = graph.getMetaData().getDataSource(MOSS_CV);
            if (moss_jgiDataSource == null) throw new DataSourceMissingException(MOSS_CV);

            concept.createConceptAccession(accession, moss_jgiDataSource, ambiguous);
        } else if (taxid == 70448) { //Ostreococcus tauri
            if (green_algea_jgiDataSource == null)
                green_algea_jgiDataSource = graph.getMetaData().getDataSource(OS_TAU_ALGAE_CV);
            if (green_algea_jgiDataSource == null) throw new DataSourceMissingException(OS_TAU_ALGAE_CV);

            concept.createConceptAccession(accession, green_algea_jgiDataSource, ambiguous);
        } else if (taxid == 3055) { //Chlamydomonas_reinhardtii
            if (chlam_re_jgi == null)
                chlam_re_jgi = graph.getMetaData().getDataSource(CHLAM_CV);
            if (chlam_re_jgi == null) throw new DataSourceMissingException(CHLAM_CV);

            concept.createConceptAccession(accession, chlam_re_jgi, ambiguous);
        } else {
            System.err.println("unknown accession for species code: " + taxid);
        }
    }

}
