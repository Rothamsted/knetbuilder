package net.sourceforge.ondex.parser.grassius;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import static net.sourceforge.ondex.parser.grassius.ArgumentNames.*;
import static net.sourceforge.ondex.parser.grassius.MetaData.*;

/**
 * GRASSIUS: a platform for comparative regulatory genomics across the grasses.
 * Yilmaz A, Nishiyama MY Jr, Fuentes BG, Souza GM, Janies D, Gray J, Grotewold E.
 * Plant Physiol. 2009 Jan;149(1):171-80. Epub 2008 Nov 5.
 *
 * @author hindlem
 */
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Parser extends ONDEXParser
{

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new RangeArgumentDefinition<Integer>(SPECIES_ARG, SPECIES_ARG_DESC, true, 0, 0, Integer.MAX_VALUE, Integer.class),
                new FileArgumentDefinition(NA_FILE_ARG, NA_FILE_ARG_DESC, true, true, false, false),
                new FileArgumentDefinition(AA_FILE_ARG, AA_FILE_ARG_DESC, true, true, false, false),
        };
    }

    @Override
    public String getName() {
        return "grassius parser";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "grassius";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        String gene_file = (String) args.getUniqueValue(NA_FILE_ARG);
        String protein_file = (String) args.getUniqueValue(AA_FILE_ARG);

        Integer species = (Integer) args.getUniqueValue(SPECIES_ARG);

        ConceptClass gene_cc = graph.getMetaData().getConceptClass(GENE);
        if (gene_cc == null)
            throw new ConceptClassMissingException(GENE);

        ConceptClass protein_cc = graph.getMetaData().getConceptClass(PROTEIN);
        if (protein_cc == null)
            throw new ConceptClassMissingException(PROTEIN);

        ConceptClass tf_fam_cc = graph.getMetaData().getConceptClass(TF_FAMILY);
        if (tf_fam_cc == null)
            throw new ConceptClassMissingException(TF_FAMILY);

        ConceptClass tf_cc = graph.getMetaData().getConceptClass(CC_TRANS_FACTOR);
        if (tf_cc == null)
            throw new ConceptClassMissingException(CC_TRANS_FACTOR);

        EvidenceType evi = graph.getMetaData().getEvidenceType(ET_IMPD);
        if (evi == null)
            throw new EvidenceTypeMissingException(ET_IMPD);

        DataSource grassiusDataSource = graph.getMetaData().getDataSource(GRASSIUS_CV);
        if (grassiusDataSource == null) {
            throw new DataSourceMissingException(GRASSIUS_CV);
        }

        AttributeName na_att = graph.getMetaData().getAttributeName(ATT_NA);
        if (na_att == null) {
            throw new AttributeNameMissingException(ATT_NA);
        }

        AttributeName aa_att = graph.getMetaData().getAttributeName(ATT_AA);
        if (aa_att == null) {
            throw new AttributeNameMissingException(ATT_AA);
        }

        AttributeName taxid_att = graph.getMetaData().getAttributeName(ATT_TAXID);
        if (taxid_att == null) {
            throw new AttributeNameMissingException(ATT_TAXID);
        }

        RelationType en_by = graph.getMetaData().getRelationType(EN_BY_RT);
        if (en_by == null) {
            throw new RelationTypeMissingException(EN_BY_RT);
        }

        RelationType is_a = graph.getMetaData().getRelationType(IS_A_RT);
        if (is_a == null) {
            throw new RelationTypeMissingException(IS_A_RT);
        }

        RelationType is_part = graph.getMetaData().getRelationType(M_ISP_RT);
        if (is_part == null) {
            throw new RelationTypeMissingException(M_ISP_RT);
        }

        String header = null;

        Map<String, Integer> geneNameToConceptId = new HashMap<String, Integer>();
        Map<String, Integer> tfFamilyToConceptId = new HashMap<String, Integer>();

        BufferedReader bis = new BufferedReader(new FileReader(gene_file));
        while (bis.ready()) {
            String line = bis.readLine();
            if (line.startsWith(">"))
                header = line;
            else {
                String[] values = header.substring(1).split("\\s*\\|\\s*");
                String accession = values[0].trim();
                String tfFamily = values[1].trim();

                ONDEXConcept gene_concept = graph.getFactory().createConcept(
                        accession,
                        "Of transcription factor family " + tfFamily,
                        grassiusDataSource,
                        gene_cc,
                        evi);
                gene_concept.createAttribute(
                        na_att,
                        line.trim(),
                        false);
                gene_concept.createAttribute(
                        taxid_att,
                        String.valueOf(species),
                        false);
                parseAccessions(species, accession, gene_concept, false);

                geneNameToConceptId.put(accession, gene_concept.getId());
            }
        }
        bis.close();

        bis = new BufferedReader(new FileReader(protein_file));
        while (bis.ready()) {
            String line = bis.readLine();
            if (line.startsWith(">"))
                header = line;
            else {
                String[] values = header.substring(1).split("\\s*\\|\\s*");
                String accession = values[0].trim();
                String tfFamily = values[1].trim();

                ONDEXConcept protein_concept = graph.getFactory().createConcept(
                        accession,
                        "Of transcription factor family " + tfFamily,
                        grassiusDataSource,
                        protein_cc,
                        evi);
                protein_concept.createAttribute(
                        aa_att,
                        line.trim(),
                        false);
                protein_concept.createAttribute(
                        taxid_att,
                        String.valueOf(species),
                        false);
                parseAccessions(species, accession, protein_concept, false);

                ONDEXConcept tf_concept = graph.getFactory().createConcept(
                        accession,
                        "Of transcription factor family " + tfFamily,
                        grassiusDataSource,
                        tf_cc,
                        evi);
                tf_concept.createAttribute(
                        taxid_att,
                        String.valueOf(species),
                        false);
                parseAccessions(species, accession, tf_concept, false);

                graph.getFactory().createRelation(protein_concept, tf_concept,
                        is_a, evi);

                ONDEXConcept gene_concept = graph.getConcept(geneNameToConceptId.get(accession));

                graph.getFactory().createRelation(protein_concept, gene_concept,
                        en_by, evi);

                ONDEXConcept familyConcept = null;

                Integer id = tfFamilyToConceptId.get(tfFamily);
                if (id == null) {
                    familyConcept = graph.getFactory().createConcept(
                            tfFamily, grassiusDataSource, tf_fam_cc, evi);
                    familyConcept.createConceptName(tfFamily, true);

                } else {
                    familyConcept = graph.getConcept(id);
                }

                graph.getFactory().createRelation(protein_concept, familyConcept,
                        is_part, evi);
                graph.getFactory().createRelation(gene_concept, familyConcept,
                        is_part, evi);
                graph.getFactory().createRelation(tf_concept, familyConcept,
                        is_part, evi);
            }
        }
        bis.close();


    }

    private DataSource tigrDataSource = null;
    private DataSource maizeDataSource = null;
    private DataSource sugerCaneDataSource = null;
    private DataSource sorgumDataSource = null;

    private void parseAccessions(int taxid, String accession, ONDEXConcept concept, boolean ambiguous) {

        int spliceDot = accession.indexOf(".");
        if (spliceDot > -1) {
            String locus = accession.substring(0, spliceDot);
            parseAccessions(taxid, locus, concept, true);
        }

        //rice
        if (taxid == 39946 || taxid == 39947 || taxid == 4530) {
            if (tigrDataSource == null) {
                tigrDataSource = graph.getMetaData().getDataSource(CV_TIGR);
            }
            concept.createConceptAccession(accession, tigrDataSource, ambiguous);

        } else if (taxid == 4577 || taxid == 112001 || taxid == 381124
                || taxid == 334825 || taxid == 4579 || taxid == 76912) { //rice
            if (maizeDataSource == null) {
                maizeDataSource = graph.getMetaData().getDataSource(MAIZE_CV);
            }
            concept.createConceptAccession(accession, maizeDataSource, ambiguous);
        } else if (taxid == 4547 || taxid == 128810) { //sugarcane Saccharum_officinarum 4547
            if (sugerCaneDataSource == null) {
                sugerCaneDataSource = graph.getMetaData().getDataSource(SUGERCANE_CV);
            }
            concept.createConceptAccession(accession, sugerCaneDataSource, ambiguous);
        } else if (taxid == 4557 || taxid == 4558) { //sorghum  Sorghum bicolor 4558
            if (sorgumDataSource == null) {
                sorgumDataSource = graph.getMetaData().getDataSource(SORGUM_CV);
            }
            concept.createConceptAccession(accession, sorgumDataSource, ambiguous);
        } else {
            System.err.println("unknown accession for species code: " + taxid);
        }

    }


}
