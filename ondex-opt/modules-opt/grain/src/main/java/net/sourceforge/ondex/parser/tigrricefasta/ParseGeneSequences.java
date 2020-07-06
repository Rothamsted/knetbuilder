package net.sourceforge.ondex.parser.tigrricefasta;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.tools.oldfastafunctions.FastaBlock;
import net.sourceforge.ondex.tools.oldfastafunctions.ReadFastaFiles;
import net.sourceforge.ondex.tools.oldfastafunctions.WriteFastaFile;

/**
 * FASTA FILE parser for genome sequences
 *
 * @author berendh
 */
public class ParseGeneSequences {

    public Map<String, ONDEXConcept> parse(ONDEXGraph graph, String fileName) {

        WriteFastaFile writeFastaFileTigr = new WriteFastaFileTigr();

        try {
            ReadFastaFiles.parseFastaFile(graph, fileName, writeFastaFileTigr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ((WriteFastaFileTigr) writeFastaFileTigr).getConceptList();
    }

    private class WriteFastaFileTigr extends WriteFastaFile {

        private Map<String, ONDEXConcept> parsedConcepts = new HashMap<String, ONDEXConcept>();
        private ConceptClass cc;
        private DataSource elementOf;
        private EvidenceType etIMPD;
        private AttributeName taxIdAttr;
        private AttributeName naAttr;

        @Override
        public void parseFastaBlock(ONDEXGraph graph, FastaBlock fasta) {

            String header = fasta.getHeader();
            String[] fields = header.split("\\|");

            String firstAccession = fields[0].trim();
            String secondAccession = fields[1].trim();
            String annot = fields[2].trim();

            //			accession contains version number, remove:
            firstAccession = ParseGenome.chompVersion(firstAccession);

            if (cc == null) cc = graph.getMetaData().getConceptClass(MetaData.gene);
            if (elementOf == null) elementOf = graph.getMetaData().getDataSource(MetaData.tigr);
            if (etIMPD == null) etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
            if (taxIdAttr == null) taxIdAttr = graph.getMetaData().getAttributeName(MetaData.taxID);
            if (naAttr == null) naAttr = graph.getMetaData().getAttributeName(MetaData.nucleicAcids);

            ONDEXConcept ac = graph.getFactory().createConcept(fields[0].trim(), annot, elementOf, cc, etIMPD);

            parsedConcepts.put(fields[0].trim(), ac);

            if (ac != null) {
                ac.createConceptAccession(firstAccession, elementOf, false);
                ac.createConceptAccession(secondAccession, elementOf, false);

                ac.createAttribute(taxIdAttr, MetaData.taxIDValue, true);
                ac.createAttribute(naAttr, fasta.getSequence(), false);
            } else {
                System.out.println("header not parsed succesfully: (" + header + ")");
            }
        }

        public Map<String, ONDEXConcept> getConceptList() {
            return parsedConcepts;
        }
    }
}