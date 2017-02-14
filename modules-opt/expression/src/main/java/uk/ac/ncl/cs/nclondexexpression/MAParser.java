/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nclondexexpression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.apache.log4j.Logger;

/**
 *
 * @author jweile
 */
public class MAParser extends ONDEXParser {

    private Logger logger = Logger.getLogger(this.getClass());

    private ConceptClass ccGene;
    private EvidenceType etImpd;
    private AttributeName anExp;
    private DataSource cvProvenance;

    public static final String PROVENANCE_ARG="Provenance";
    public static final String PROVENANCE_ARG_DESC="Provenance identifier of the dataset";

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[] {
            new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                    FileArgumentDefinition.INPUT_FILE_DESC,
                    true, true, false, false),
            new StringArgumentDefinition(PROVENANCE_ARG, PROVENANCE_ARG_DESC,
                    true, "unknown", false),
        };
    }

    @Override
    public void start() throws Exception {

        //setup metadata
        ccGene = requireConceptClass("Gene");
        etImpd = requireEvidenceType("IMPD");
        cvProvenance = requireDataSource((String)getArguments().getUniqueValue(PROVENANCE_ARG));

        //create expression map attribute name
        anExp = graph.getMetaData().getFactory()
                .createAttributeName("EXPMAP",
                "Gene expression data map",
                "Map linking expression data points to expression values",
                Map.class);


        //get file
        File inputFile = new File((String) getArguments().getUniqueValue(FileArgumentDefinition.INPUT_FILE));

        //read file
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(inputFile));

            //process file header
            String header = reader.readLine();
            String[] headerCols = null;
            if (header != null && header.length() > 0) {
                headerCols = header.split("\t");
                //complain if not enough columns
                if (header.length() < 2) {
                    throw new ParsingFailedException("Incorrect file format in file "+
                            inputFile.getAbsolutePath());
                }
            //complain if header is empty
            } else {
                throw new ParsingFailedException("Empty file header in file "+
                        inputFile.getAbsolutePath());
            }

            //determine namespace of gene name from first header column
            DataSource geneNamespace = graph.getMetaData().getDataSource(headerCols[0]);

            //start reading the contents
            String line; int lineNum = 0;
            while ((line = reader.readLine()) != null) {

                lineNum++;
                if (line.length() == 0) {
                    continue;
                }
                String[] cols = line.split("\t");

                //complain if line has incorrect number of fields
                if (cols.length != headerCols.length) {
                    logger.warn("Skipping line #"+lineNum+ "; Incorrect number of fields.");
                }

                String geneName = cols[0];

                //read expression values
                try {

                    Map<String,Double> values = new HashMap<String,Double>();
                    //starting from the second column, transfer values to map
                    for (int i = 1 ; i < cols.length; i++) {
                        double val = Double.parseDouble(cols[i]);
                        values.put(headerCols[i], val);
                    }

                    //create gene concept
                    ONDEXConcept geneConcept = graph.getFactory().createConcept(geneName, cvProvenance, ccGene, etImpd);
                    geneConcept.createConceptAccession(geneName, geneNamespace, false);
                    geneConcept.createConceptName(geneName, false);

                    //attach expression map
                    geneConcept.createAttribute(anExp, values, false);

                //complain if non-double values occurred
                } catch (NumberFormatException nfe) {
                    logger.warn("Skipping line #"+lineNum+ "; Non-numeric values detected.");
                }
            }

        } catch (IOException ioe) {
            throw new ParsingFailedException("Unable to read file "+inputFile.getAbsolutePath());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Unable to close file handle "+inputFile.getAbsolutePath(), e);
            }
        }
    }

    @Override
    public String getId() {
        return "ncl_ma";
    }

    @Override
    public String getName() {
        return "Newcastle Ondex Microarray data parser";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
    
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

}
