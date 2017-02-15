package net.sourceforge.ondex.parser.affymetrix;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.parser.fasta.FastaBlock;
import net.sourceforge.ondex.parser.fasta.ReadFastaFiles;
import net.sourceforge.ondex.parser.fasta.WriteFastaFile;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser to parse the Affymetrix targetsequence and consensus files
 *
 * @author hindlem
 */
public class AffyParser {

    private ONDEXPluginArguments pa;

    private AttributeName taxIdAttr;
    private AttributeName naAttr;
    private AttributeName affyChipAttr;
    private AttributeName typeAttr;
    private DataSource dbEST;
    private DataSource dataSourceAFFY;
    private DataSource dataSourceTAIR;
    private ConceptClass ccTARGET;
    private ConceptClass consensusCC;
    private EvidenceType etIMPD;
    private String taxId;

    private RelationType derives_from;

    public AffyParser(ONDEXPluginArguments pa) {

        this.pa = pa;
    }

    public void setONDEXGraph(ONDEXGraph graph) throws Exception {

        String methodName = "setONDEXGraph(AbstractONDEXGraph graph)";

        typeAttr = graph.getMetaData().getAttributeName(
                MetaData.PROBE_SET_TYPE_att);
        if (typeAttr == null) {
            AttributeNameMissingEvent ge = new AttributeNameMissingEvent(
                    "Missing: " + MetaData.PROBE_SET_TYPE_att, methodName);
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                    .fireEventOccurred(ge);
            typeAttr = graph.getMetaData().getFactory().createAttributeName(
                    MetaData.PROBE_SET_TYPE_att, MetaData.PROBE_SET_TYPE_att,
                    String.class);
        }

        if (naAttr == null) {
            naAttr = graph.getMetaData().getAttributeName(MetaData.nucleicAcid);
            if (naAttr == null) {
                AttributeNameMissingEvent ge = new AttributeNameMissingEvent(
                        "Missing: " + MetaData.nucleicAcid, methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);
            }
        }

        if (etIMPD == null) {
            etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
            if (etIMPD == null) {
                EvidenceTypeMissingEvent ge = new EvidenceTypeMissingEvent(
                        "Could not find EvidenceType: " + MetaData.IMPD,
                        methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);
            }
        }

        if (taxIdAttr == null) {
            taxIdAttr = graph.getMetaData().getAttributeName(MetaData.taxID);
            if (taxIdAttr == null) {
                AttributeNameMissingEvent ge = new AttributeNameMissingEvent(
                        "Missing: " + MetaData.taxID, methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);
            }
        }

        if (affyChipAttr == null) {
            affyChipAttr = graph.getMetaData().getAttributeName(
                    MetaData.affyChip);
            if (affyChipAttr == null) {
                AttributeNameMissingEvent ge = new AttributeNameMissingEvent(
                        "Missing: " + MetaData.affyChip, methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);
            }
        }

        if (dbEST == null) {
            dbEST = graph.getMetaData().getDataSource(MetaData.cvdbEST);
            if (dbEST == null) {
                DataSourceMissingEvent ge = new DataSourceMissingEvent("Missing: "
                        + MetaData.cvdbEST, methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);
            }
        }

        if (dataSourceAFFY == null) {
            dataSourceAFFY = graph.getMetaData().getDataSource(MetaData.cvAFFY);
            if (dataSourceAFFY == null) {
                DataSourceMissingEvent ge = new DataSourceMissingEvent("Missing: "
                        + MetaData.cvAFFY, methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);
            }
        }

        if (dataSourceTAIR == null) {
            dataSourceTAIR = graph.getMetaData().getDataSource(MetaData.cvTAIR);
            if (dataSourceTAIR == null) {
                DataSourceMissingEvent ge = new DataSourceMissingEvent("Missing: "
                        + MetaData.cvTAIR, methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);
            }
        }

        if (ccTARGET == null) {
            ccTARGET = graph.getMetaData().getConceptClass(MetaData.ccTARGET);
            if (ccTARGET == null) {
                ConceptClassMissingEvent ge = new ConceptClassMissingEvent(
                        "Missing: " + MetaData.ccTARGET, methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);
            }
        }

        if (consensusCC == null) {
            consensusCC = graph.getMetaData().getConceptClass(
                    MetaData.ccCONSENSUS);
            if (consensusCC == null) {
                consensusCC = graph.getMetaData().getFactory()
                        .createConceptClass(MetaData.ccCONSENSUS);
                ConceptClassMissingEvent ge = new ConceptClassMissingEvent(
                        "Missing: " + MetaData.ccCONSENSUS, methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);
            }
        }

        if (derives_from == null) {
            derives_from = graph.getMetaData().getRelationType(
                    MetaData.derives_from);
            if (derives_from == null) {
                RelationTypeMissingEvent ge = new RelationTypeMissingEvent(
                        "Missing: " + MetaData.derives_from, methodName);
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                        .fireEventOccurred(ge);

            }
        }

        GeneralOutputEvent so = new GeneralOutputEvent(
                "Starting Affy Fasta File parsing...", methodName);
        so.setLog4jLevel(Level.INFO);
        ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                .fireEventOccurred(so);

        File dir = new File((String) pa
                .getUniqueValue(FileArgumentDefinition.INPUT_DIR));
        String inFilesDir = dir.getAbsolutePath();

        Map<String, File> consensusFiles = extractFileOfEnding(inFilesDir,
                ".consensus");
        Map<String, File> targetFiles = extractFileOfEnding(inFilesDir,
                ".target");

        for (String fileType : targetFiles.keySet()) {

            File targetFile = targetFiles.get(fileType);
            File consensusFile = consensusFiles.get(fileType);

            WriteFastaFileAffy writeTargetFastaFileAffy = new WriteFastaFileAffy(
                    ccTARGET);
            if (targetFile != null) {
                try {
                    ReadFastaFiles.parseFastaFile(graph, targetFile,
                            writeTargetFastaFileAffy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            WriteFastaFileAffy writeConsensusFastaFileAffy = new WriteFastaFileAffy(
                    consensusCC);

            try {
                ReadFastaFiles.parseFastaFile(graph, consensusFile,
                        writeConsensusFastaFileAffy);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, Integer> targetSequences = writeTargetFastaFileAffy
                    .getWrittenSequences();
            Map<String, Integer> consensusSequences = writeConsensusFastaFileAffy
                    .getWrittenSequences();

            for (String target : targetSequences.keySet()) {
                Integer ondexId = targetSequences.get(target);
                Integer consensusId = consensusSequences.get(target);

                if (ondexId != null && consensusId != null) {
                    graph.getFactory().createRelation(
                            graph.getConcept(consensusId),
                            graph.getConcept(ondexId), derives_from, etIMPD);
                }
            }
        }

        so = new GeneralOutputEvent("Finished Affy Fasta File parsing...",
                methodName);
        so.setLog4jLevel(Level.INFO);
        ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
                .fireEventOccurred(so);
    }

    /**
     * @param inFilesDir
     * @param string
     * @return
     * @throws Exception
     */
    private Map<String, File> extractFileOfEnding(String inFilesDir,
                                                  final String string) throws Exception {
        Map<String, File> map = new HashMap<String, File>();

        String[] files = new File(inFilesDir).list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(string);
            }
        });

        for (String file : files) {
            System.out.println("Found " + file + " of type "
                    + file.split("\\.")[0]);
            if (map.containsKey(file.split("\\.")[0])) {
                System.err.println("duplicate file types "
                        + file.split("\\.")[0] + " of the same " + string
                        + " type");
            }
            map.put(file.split("\\.")[0], new File(inFilesDir + File.separator
                    + file));
        }

        return map;
    }

    /**
     *
     */
    private class WriteFastaFileAffy extends WriteFastaFile {

        private Map<String, Integer> writtenSequences = new HashMap<String, Integer>();

        private ConceptClass cc;

        public WriteFastaFileAffy(ConceptClass cc) {
            this.cc = cc;
        }

        public Map<String, Integer> getWrittenSequences() {
            return writtenSequences;
        }

        @Override
        public void parseFastaBlock(ONDEXGraph graph, FastaBlock fasta)
                throws WriteFastaFile.FormatFileException {

            String header = fasta.getHeader();
            String[] fields = header.split(";");

            HashMap<String, String[]> ids = new HashMap<String, String[]>();

            for (String idField : fields) {
                String[] bipart = idField.split(":");
                if (bipart.length > 1) {
                    ids.put(bipart[0].replaceAll("_", "").trim().toLowerCase(),
                            bipart);
                }
            }
            String[] targets = ids.get("target");

            if (targets == null)
                targets = ids.get("consensus");

            if (targets == null)
                targets = ids.get("exemplar");

            if (targets == null) {
                System.err
                        .println("No accession found can not parse this FASTA seq HEADER="
                                + header);
                for (String id : ids.keySet()) {
                    System.err.println("FASTA seq HEADER components " + id
                            + " " + ids.get(id));
                }
                return;
            }

            String firstAccession = targets[targets.length - 1].trim(); // last
            // item
            // is
            // accession
            if (firstAccession.length() == 0) {
                throw new WriteFastaFile.FormatFileException(
                        "first accession not found in " + header);
            }

            String description = fields[fields.length - 1].trim();
            if (description.length() == 0)
                description = header;

            ONDEXConcept ac = graph.getFactory().createConcept(firstAccession,
                    description, dataSourceAFFY, cc, etIMPD);

            writtenSequences.put(firstAccession, ac.getId());

            if (ac != null) {

                ac.createConceptAccession(firstAccession, dataSourceAFFY, false);

                if (targets.length > 2) {
                    ac.createAttribute(affyChipAttr, targets[1], false);
                } else {
                    System.out.println(affyChipAttr.getId() + " not found in "
                            + header);
                }

                String[] gbs = ids.get("gb");
                if (gbs != null && gbs.length > 1) {
                    for (int i = 1; i < gbs.length; i++) {
                        ac.createConceptAccession(gbs[i], dbEST, true);
                    }
                }
                String[] affxs = ids.get("affx");
                if (affxs != null && affxs.length > 1) {
                    for (int i = 1; i < affxs.length; i++) {
                        ac.createConceptAccession(affxs[i], dataSourceAFFY, false);
                    }
                }

                String[] ats = ids.get("at");
                if (affxs != null && ats.length > 1) {
                    for (int i = 1; i < ats.length; i++) {
                        if (ats[i].startsWith("at")) {
                            ac.createConceptAccession(ats[i], dataSourceTAIR, true);
                        }
                    }
                }

                if (firstAccession.toLowerCase().contains("_s_")) {
                    ac.createAttribute(typeAttr, "s", false);
                    ac
                            .setDescription("A probe sets with common probes among multiple transcripts from different genes.");
                } else if (firstAccession.toLowerCase().contains("_x_")) {
                    ac.createAttribute(typeAttr, "x", false);
                    ac
                            .setDescription("A probe sets where it was not possible to select either a unique probe set or a probe set with identical probes among multiple transcripts. Rules for cross-hybridization were dropped in order to design the _x probe sets. These probe sets share some probes identically with two or more sequences and, therefore, these probe sets may cross-hybridize in an unpredictable manner.");
                } else if (firstAccession.toLowerCase().contains("_a_")) {
                    ac.createAttribute(typeAttr, "a", false);
                    ac
                            .setDescription("A probe sets that recognizes alternative transcripts from the same gene (a subset of the _s probe sets as described under HG-U133 Set).");
                }

                ac.createAttribute(naAttr, fasta.getSequence(), false);
            } else {
                System.err.println("header not parsed succesfully: (" + header
                        + ")");
            }
        }
    }

}
