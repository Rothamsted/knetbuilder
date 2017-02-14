/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.sgdgenes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.ParsingErrorEvent;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.apache.log4j.Level;
import static uk.ac.ncl.cs.sgdgenes.Column.*;

/**
 *
 * @author jweile
 */
public class SgdGeneParser extends ONDEXParser {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[] {
            new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                    FileArgumentDefinition.INPUT_FILE_DESC,
                    true, true, false, false),
        };
    }

    private ConceptClass ccGene;
    private DataSource cvSGD, cvMIPS;
    private EvidenceType etImpd;

    private void initMetadata() throws MetaDataMissingException {
        ccGene = requireConceptClass("Gene");
        cvSGD = requireDataSource("SGD");
        cvMIPS = requireDataSource("MIPS");
        etImpd = requireEvidenceType("IMPD");
    }

    @Override
    public void start() throws Exception {

        initMetadata();

        File infile = new File((String)getArguments().getUniqueValue(FileArgumentDefinition.INPUT_FILE));

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(infile));

            String line; int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;

                String[] cols = line.split("\t");

                if (cols.length != 7) {
                    logError("Incorrect number of columns in line "+lineNum);
                }

                //create concept
                ONDEXConcept geneConcept = graph.getFactory().createConcept(cols[GENE_NAME.index()], cvSGD, ccGene, etImpd);
                //set preferred name
                geneConcept.createConceptName(cols[GENE_NAME.index()], true);
                //set other names
                if (cols[ALIASES.index()] != null &&  cols[ALIASES.index()].length() > 0) {
                    String[] aliases = cols[ALIASES.index()].split("\\|");
                    for (String alias : aliases) {
                        geneConcept.createConceptName(alias, false);
                    }
                }
                //set description
                if (cols[DESCRIPTION.index()] != null && cols[DESCRIPTION.index()].length() > 0) {
                    geneConcept.setDescription(cols[DESCRIPTION.index()]);
                }
                //set orf name (= mips accesssion)
                if (cols[ORF_NAME.index()] != null && cols[ORF_NAME.index()].length() > 0) {
                    geneConcept.createConceptAccession(cols[ORF_NAME.index()], cvMIPS, false);
                }
                //set sgd accession
                if (cols[SGD_ID.index()] != null && cols[SGD_ID.index()].length() > 0) {
                    geneConcept.createConceptAccession(cols[SGD_ID.index()], cvSGD, false);
                }

            }
        } catch (IOException ioe) {
            throw new ParsingFailedException("Unable to read file "+infile);
        } finally {
            try {
                reader.close();
            } catch (IOException ioe) {
                logError("Unable to close file handle "+infile);
            }
        }
    }

    @Override
    public String getId() {
        return "ncl_sgd_gene";
    }

    @Override
    public String getName() {
        return "Newcastle University SGD Gene parser";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    private void logError(String message) {
        ParsingErrorEvent e = new ParsingErrorEvent(message, "");
        e.setLog4jLevel(Level.ERROR);
        fireEventOccurred(e);
    }

}
