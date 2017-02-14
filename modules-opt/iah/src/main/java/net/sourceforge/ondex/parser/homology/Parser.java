package net.sourceforge.ondex.parser.homology;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.apache.log4j.Level;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parser extends ONDEXParser
{


    private ConceptClass ccORF;
    private RelationType rtHom;
    private DataSource dataSourceSGD, dataSourceMips;
    private EvidenceType evBlast, evImpd;

    @Override
    public void start() throws Exception {

        ccORF = requireConceptClass("ORF");
        rtHom = requireRelationType("homologue");
        dataSourceSGD = requireDataSource("SGD");
        dataSourceMips = requireDataSource("MIPS");
        evBlast = requireEvidenceType("BLAST");
        evImpd = requireEvidenceType("IMPD");

        String infile = (String) getArguments().getUniqueValue(FileArgumentDefinition.INPUT_FILE);

        int indexOrfA = 0, indexOrfB = 3;

        try {
            BufferedReader br = new BufferedReader(new FileReader(infile));
            String line;
            int linenum = 0;
            while ((line = br.readLine()) != null) {
                linenum++;
                String[] cols = line.split("\t");
                if (cols.length == 6) {
                    String orfA = cols[indexOrfA];
                    String orfB = cols[indexOrfB];

                    ONDEXConcept cA = createConcept(orfA);
                    ONDEXConcept cB = createConcept(orfB);
                    graph.getFactory().createRelation(cA, cB, rtHom, evBlast);

                } else {
                    logInconsistency("Unexpected number of columns in line " + linenum);
                }
            }
        } catch (IOException e) {
            throw new ParsingFailedException(e);
        }
    }


    private ONDEXConcept createConcept(String orf) {
        ONDEXConcept c = graph.getFactory().createConcept(orf, dataSourceSGD, ccORF, evImpd);
        c.createConceptAccession(orf, dataSourceMips, false);
        return c;
    }


    private void logInconsistency(String message) {
        InconsistencyEvent e = new InconsistencyEvent(message, "");
        e.setLog4jLevel(Level.WARN);
        fireEventOccurred(e);
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
        };
    }

    @Override
    public String getId() {
        return "yeasthomology";
    }

    @Override
    public String getName() {
        return "Yeast homology parser";
    }

    @Override
    public String getVersion() {
        return "11.02.2010";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }


}
