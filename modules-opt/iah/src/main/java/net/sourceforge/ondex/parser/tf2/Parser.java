package net.sourceforge.ondex.parser.tf2;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the Balaji-Madan Babu transcription factor network.
 * http://www.mrc-lmb.cam.ac.uk/genomes/madanm/tfcomb/
 *
 * @author jweile
 */
public class Parser extends ONDEXParser
{

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
        };
    }

    @Override
    public String getName() {
        return "Balaji-Madan Babu TF network parser";
    }

    @Override
    public String getVersion() {
        return "26.06.2009";
    }

    @Override
    public String getId() {
        return "tf2";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        ConceptClass ccTF = requireConceptClass("TF"),
                ccGene = requireConceptClass("Gene"),
                ccPP = requireConceptClass("Polypeptide");
        RelationType rtRgBy = requireRelationType("rg_by"),
                rtPartOf = requireRelationType("is_part_of"),
                rtEnBy = requireRelationType("en_by");
        EvidenceType ev = requireEvidenceType("IMPD");
        DataSource dataSourceUnknown = requireDataSource("NCBI"), dataSourceMips = requireDataSource("MIPS");

        HashMap<String, ONDEXConcept> tfConcepts = new HashMap<String, ONDEXConcept>();
        HashMap<String, ONDEXConcept> geneConcepts = new HashMap<String, ONDEXConcept>();
        HashMap<String, ONDEXConcept> ppConcepts = new HashMap<String, ONDEXConcept>();

        BufferedReader br = new BufferedReader(
                new FileReader((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE)));
        String l, tf, target;
        String[] val;
        while ((l = br.readLine()) != null) {
            val = l.split("\t");
            if (val.length != 2) continue;
            tf = correct(val[0]);
            target = correct(val[1]);

            //create TF Protein concept if non-existent
            ONDEXConcept tfConcept = tfConcepts.get(tf);
            if (tfConcept == null) {
                tfConcept = graph.getFactory().createConcept(tf, dataSourceUnknown, ccTF, ev);
                tfConcept.createConceptAccession(tf, dataSourceMips, false);
                tfConcepts.put(tf, tfConcept);
            }

            //create TF PP concept if non-existent
            ONDEXConcept tfPPConcept = ppConcepts.get(tf);
            if (tfPPConcept == null) {
                tfPPConcept = graph.getFactory().createConcept(tf, dataSourceUnknown, ccPP, ev);
                tfPPConcept.createConceptAccession(tf, dataSourceMips, false);
                ppConcepts.put(tf, tfPPConcept);
            }

            //link Polypeptide to TF
            ONDEXRelation ppPartOfTF = graph.getRelation(tfPPConcept, tfConcept, rtPartOf);
            if (ppPartOfTF == null) {
                ppPartOfTF = graph.getFactory().createRelation(tfPPConcept, tfConcept, rtPartOf, ev);
            }

            //create target Gene concept if non-existent
            ONDEXConcept geneConcept = geneConcepts.get(target);
            if (geneConcept == null) {
                geneConcept = graph.getFactory().createConcept(target, dataSourceUnknown, ccGene, ev);
                geneConcept.createConceptAccession(target, dataSourceMips, false);
                geneConcepts.put(target, geneConcept);
            }

            //create target PP concept if non-existent
            ONDEXConcept targetPPConcept = ppConcepts.get(target);
            if (targetPPConcept == null) {
                targetPPConcept = graph.getFactory().createConcept(target, dataSourceUnknown, ccPP, ev);
                targetPPConcept.createConceptAccession(target, dataSourceMips, false);
                ppConcepts.put(target, targetPPConcept);
            }

            //link target Polypeptide to target Gene
            ONDEXRelation targetPPEnByGene = graph.getRelation(targetPPConcept, geneConcept, rtEnBy);
            if (targetPPEnByGene == null) {
                targetPPEnByGene = graph.getFactory().createRelation(targetPPConcept, geneConcept, rtEnBy, ev);
            }

            //link TF to target
            graph.getFactory().createRelation(geneConcept, tfConcept, rtRgBy, ev);

        }

        br.close();
    }

    /**
     * Converts the Y-name into SGD format if necessary
     *
     * @return sgd compatible y-name
     */
    private String correct(String inY) {
        Pattern p = Pattern.compile("(Y\\w{2}\\d{3}[WC])((\\w{1}))");
        Matcher m = p.matcher(inY);
        if (m.find() && m.groupCount() == 3) {
            String outY = m.group(1) + "-" + m.group(2);
            return outY;
        } else {
            return inY;
        }
    }

}
