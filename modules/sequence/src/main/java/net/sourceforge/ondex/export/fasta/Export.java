package net.sourceforge.ondex.export.fasta;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.*;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.tools.tab.exporter.Label;
import net.sourceforge.ondex.tools.tab.exporter.extractors.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * A rough and ready fasta exporter....under development
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Export extends ONDEXExport implements ArgumentNames {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition st = new StringArgumentDefinition(
                SEQUENCE_TYPE_ARG, SEQUENCE_TYPE_ARG_DESC, true, "NA", false);
        RangeArgumentDefinition<Integer> cid = new RangeArgumentDefinition<Integer>(
                CONCEPTS_ARG, CONCEPTS_ARG_DESC, true, null, 0,
                Integer.MAX_VALUE, Integer.class);
        StringArgumentDefinition ha = new StringArgumentDefinition(
                HEADER_FIELDS_ARG, HEADER_FIELDS_ARG_DESC, true, "NA", true);
        BooleanArgumentDefinition ttid = new BooleanArgumentDefinition(
                TRANSLATE_TAXID_ARG, TRANSLATE_TAXID_ARG_DESC, false, false);
        BooleanArgumentDefinition iv = new BooleanArgumentDefinition(
                INCLUDE_VARIENTS_ARG, INCLUDE_VARIENTS_ARG_DESC, false, false);
        new BooleanArgumentDefinition(ZIP_FILE_ARG, ZIP_FILE_ARG_DESC, false,
                true);
        FileArgumentDefinition exportFile = new FileArgumentDefinition(
                FileArgumentDefinition.EXPORT_FILE, "FASTA export file",
                true, false, false, false);
        return new ArgumentDefinition<?>[]{st, cid, ha, ttid, iv, exportFile};
    }

    @Override
    public String getName() {
        return "fasta exporter";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "fasta";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[] {"scientificspeciesname"};
    }

    @Override
    public void start() throws Exception {
        int seqWritten = 0;

        AttributeName at = graph.getMetaData().getAttributeName(
                (String) args.getUniqueValue(SEQUENCE_TYPE_ARG));

        Set<AttributeName> attributesOfSequenceType = new HashSet<AttributeName>();
        attributesOfSequenceType.add(at);
        if ((Boolean) args.getUniqueValue(INCLUDE_VARIENTS_ARG)) {
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                AttributeName next = graph.getMetaData().getAttributeName(
                        at.getId() + ':' + i);
                if (next == null) {
                    break;
                } else {
                    System.out.println("Found " + at.getId() + ':' + i);
                    attributesOfSequenceType.add(next);
                }
            }
        }

        BufferedWriter bw;
        File exportfile = new File((String) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));
        String file = exportfile.getAbsolutePath();
        if ((Boolean) args.getUniqueValue(ZIP_FILE_ARG)) {
            if (!file.endsWith(".gz")) {
                file = file + ".gz";
            }
            bw = new BufferedWriter(new OutputStreamWriter(
                    new GZIPOutputStream(new FileOutputStream(file), 512 * 2)));
        } else {
            bw = new BufferedWriter(new FileWriter(exportfile.getAbsolutePath()));
        }

        Boolean translate = (Boolean) args.getUniqueValue(TRANSLATE_TAXID_ARG);

        List<AttributeExtractor> headerAttributes = new ArrayList<AttributeExtractor>();
        for (Object argument : args.getObjectValueList(CONCEPTS_ARG)) {
            headerAttributes.add(parseHeaderArgument((String) argument,
                    translate));
        }

        List<?> concepts = args.getObjectValueList(CONCEPTS_ARG);

        for (Object conceptId : concepts) {
            ONDEXConcept concept = graph.getConcept((Integer) conceptId);
            if (concept == null) {
                System.err.println((Integer) conceptId
                        + " is not a valid conceptID");
                continue;
            }

            StringBuffer sb = new StringBuffer();

            int n = 1;
            for (AttributeExtractor attHeader : headerAttributes) {
                sb.append(attHeader.getValue(concept));
                if (n != headerAttributes.size())
                    sb.append('|');
                n++;
            }

            for (AttributeName aSeq : attributesOfSequenceType) {

                Attribute attribute = concept.getAttribute(aSeq);
                if (attribute == null) {
                    continue;
                }

                String seq = ((String) attribute.getValue()).toUpperCase().trim();

                if (seq == null || seq.length() == 0) {
                    System.err.println("empty seq");
                    continue;
                }

                bw.write(sb.toString());
                bw.newLine();

                String stringToWrite = null;

                if (stringToWrite == null || stringToWrite.length() == 0) {
                    System.err.println("nout to write");
                    continue;
                }

                for (int i = 0; i < stringToWrite.length(); i = i + 70) {
                    int end = i + 70;
                    if (end > stringToWrite.length() - 1) {
                        end = stringToWrite.length() - 1;
                    }
                    String toWrite = stringToWrite.substring(i, end);
                    if (toWrite.length() > 0) {
                        bw.write(toWrite);
                        bw.newLine();
                    }
                }
                seqWritten++;
            }
        }
        bw.flush();
        bw.close();

        System.out.println(seqWritten + " sequences written");

    }

    /**
     * @param argument
     * @param translateTaxId
     * @return
     */
    private AttributeExtractor parseHeaderArgument(String argument,
                                                   boolean translateTaxId) {
        String[] processed = argument.split(":");
        String value = processed[0].trim().toLowerCase();

        if (value.equals("name")) {
            return new NameAttributeExtractor();
        } else if (value.equals("accession")) {
            return new AccessionAttributeExtractor(processed[1], false);
        } else if (value.equals("gds")) {
            return new GDSAttributeExtractor(graph.getMetaData()
                    .getAttributeName(processed[1]), translateTaxId);
        } else if (value.equals("context")) {
            Label userDefined = Label.NAME;
            if (processed.length == 3) {
                userDefined = Label.translate(processed[2]);
            }

            return new ContextExtractor(processed[1], userDefined);
        } else if (value.equals("evidence")) {
            if (processed.length == 2) {
                return new DefinedEvidenceAttributeExtractor(graph
                        .getMetaData().getEvidenceType(processed[1]));
            } else {
                return new EvidenceAttributeExtractor();
            }
        } else if (value.equals("class")) {
            return new TypeAttributeExtractor();
        } else if (value.equals("pid")) {
            return new PidAttributeExtractor();
        } else if (value.equals("description")) {
            return new DescriptionAttributeExtractor();
        } else if (value.equals("annotation")) {
            return new AnnotationAttributeExtractor();
        } else if (value.equals("cv")) {
            return new CVAttributeExtractor();
        } else {
            System.err.println(value + " is an unknown field (ignored)");
            return null;
        }
    }
}
