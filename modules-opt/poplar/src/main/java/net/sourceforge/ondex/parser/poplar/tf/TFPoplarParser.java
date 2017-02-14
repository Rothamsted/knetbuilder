package net.sourceforge.ondex.parser.poplar.tf;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.parser.poplar.MetaData;
import net.sourceforge.ondex.parser.poplar.Parser;
import net.sourceforge.ondex.parser.poplar.Registry;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class TFPoplarParser {

    private Registry poplarReg;

    private ConceptClass ccTF;
    private DataSource dataSourceDPTF, dataSourcePopJGI;
    private RelationType is_a;
    private EvidenceType etIMPD;

    public TFPoplarParser(Registry poplarReg) {
        this.poplarReg = poplarReg;
    }

    public void start(ONDEXGraph graph, ONDEXPluginArguments pa, Parser parser) throws InvalidPluginArgumentException {
        GeneralOutputEvent goe = new GeneralOutputEvent(
                "Parsing Transcription Factor Data...", "[Parser - start()]");
        parser.fireEventOccurred(goe);

        try {
            ccTF = parser.requireConceptClass(MetaData.CC_TF);
            dataSourceDPTF = parser.requireDataSource(MetaData.CV_DPTF);
            dataSourcePopJGI = parser.requireDataSource(MetaData.CV_PHYTOZOME);
            is_a = parser.requireRelationType(MetaData.RT_ISA);
            etIMPD = parser.requireEvidenceType(MetaData.ET_IMPD);
        } catch (ConceptClassMissingException e) {
            e.printStackTrace();
        } catch (DataSourceMissingException e) {
            e.printStackTrace();
        } catch (RelationTypeMissingException e) {
            e.printStackTrace();
        } catch (EvidenceTypeMissingException e) {
            e.printStackTrace();
        }

        File inputDir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        String filename = inputDir.getAbsolutePath() + File.separator + "Poplar.TF.pep";

        BufferedReader input;

        try {
            if (filename.endsWith(".gz")) {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(
                        filename));
                input = new BufferedReader(new InputStreamReader(gzip));
            } else {
                input = new BufferedReader(new FileReader(filename));
            }

            String inputLine = input.readLine();
            int numTF = 0;
            while (inputLine != null) {

                if (inputLine.startsWith(">")) {
                    String[] col = inputLine.split("\\|");
                    String accDPTF = col[0].substring(1);
                    String accJGI = col[2];
                    ONDEXConcept protein = graph.getConcept(poplarReg.getProtein(accJGI));
                    ONDEXConcept tf = graph.getFactory().createConcept(accDPTF, dataSourceDPTF, ccTF, etIMPD);
                    tf.createConceptAccession(accDPTF, dataSourceDPTF, false);
                    tf.createConceptAccession(accJGI, dataSourcePopJGI, false);
                    graph.getFactory().createRelation(protein, tf, is_a, etIMPD);
                    numTF++;
                }
                inputLine = input.readLine();
            }
            input.close();
            System.out.println(numTF + " TFs were parsed from DPTF.");
        } catch (FileNotFoundException fnfe) {
            parser.fireEventOccurred(new DataFileMissingEvent(
                    fnfe.getMessage(), "[Parser - start()]"));
        } catch (IOException ioe) {
            parser.fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
                    "[Parser - start()]"));
        }
    }

}
