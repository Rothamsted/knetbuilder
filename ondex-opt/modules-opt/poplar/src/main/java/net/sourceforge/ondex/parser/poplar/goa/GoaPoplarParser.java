package net.sourceforge.ondex.parser.poplar.goa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.parser.poplar.MetaData;
import net.sourceforge.ondex.parser.poplar.Parser;
import net.sourceforge.ondex.parser.poplar.Registry;
import net.sourceforge.ondex.workflow.InvalidPluginArgumentException;
import net.sourceforge.ondex.workflow.ONDEXPluginArguments;

/**
 * Parser for the Poplar GO Annotation file.
 * <p/>
 * Download from:
 * ftp://ftp.jgi-psf.org/pub/JGI_data/Poplar/annotation/v1.1/functional/Poptr1_1.goinfo.tab.gz
 * <p/>
 * Creates Protein --> MolFunc, BioProc, CelComp concepts
 *
 * @author keywan
 */
public class GoaPoplarParser {

    private static final boolean DEBUG = false;

    private DataSource dataSourceJGI;
    private DataSource dataSourceGO;

    private ConceptClass ccProtein;
    private ConceptClass ccMolFunc;
    private ConceptClass ccBioProc;
    private ConceptClass ccCelComp;

    private RelationType rtFunc;
    private RelationType rtProc;
    private RelationType rtComp;

    private EvidenceType etIMPD;

    private AttributeName anTaxID;

    private HashMap<String, Integer> parsedGoIDs;

    private Registry poplarReg;


    /**
     * Initialisation of parameters
     */
    public GoaPoplarParser(Registry poplarReg) {
        this.poplarReg = poplarReg;
        parsedGoIDs = new HashMap<String, Integer>();
    }


    /**
     * Parses every line into a Protein to GO relation
     */
    public void start(ONDEXGraph graph, ONDEXPluginArguments pa, Parser parser) throws InvalidPluginArgumentException {
        GeneralOutputEvent goe = new GeneralOutputEvent("Parsing GO Annotations...", "[Parser - start()]");
        parser.fireEventOccurred(goe);

        //required MetaData
        try {
            dataSourceGO = parser.requireDataSource(MetaData.CV_GO);
            dataSourceJGI = parser.requireDataSource(MetaData.CV_PHYTOZOME);
            ccProtein = parser.requireConceptClass(MetaData.CC_PROTEIN);
            ccMolFunc = parser.requireConceptClass(MetaData.CC_MolFunc);
            ccBioProc = parser.requireConceptClass(MetaData.CC_BioProc);
            ccCelComp = parser.requireConceptClass(MetaData.CC_CelComp);
            rtFunc = parser.requireRelationType(MetaData.RT_hasFunction);
            rtProc = parser.requireRelationType(MetaData.RT_hasParticipant);
            rtComp = parser.requireRelationType(MetaData.RT_locatedIn);
            etIMPD = parser.requireEvidenceType(MetaData.ET_IMPD);
            anTaxID = parser.requireAttributeName(MetaData.AN_TAXID);
        } catch (DataSourceMissingException e) {
            e.printStackTrace();
        } catch (ConceptClassMissingException e) {
            e.printStackTrace();
        } catch (RelationTypeMissingException e) {
            e.printStackTrace();
        } catch (EvidenceTypeMissingException e) {
            e.printStackTrace();
        } catch (AttributeNameMissingException e) {
            e.printStackTrace();
        }

        File inputDir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        String filename = inputDir.getAbsolutePath() + File.separator + "Poptr1_1.goinfo.tab.gz";

        BufferedReader input;

        try {
            if (filename.endsWith(".gz")) {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(filename));
                input = new BufferedReader(new InputStreamReader(gzip));
            } else {
                input = new BufferedReader(new FileReader(filename));
            }

            String inputLine = input.readLine();
            inputLine = input.readLine(); //skip header line
            int count = 0;
            while (inputLine != null) {

                if (++count == 1000 && DEBUG) break;

                String[] col = inputLine.split("\t");
                String protID = col[0];
//				String goID = col[1];
                String goName = col[2];
                String goType = col[3];
                String goAcc = col[4];

                if (!poplarReg.containsProtein(protID)) {
                    ONDEXConcept p = graph.getFactory().createConcept(protID, dataSourceJGI, ccProtein, etIMPD);
                    p.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
                    poplarReg.addProtein(protID, p.getId());
                }
                ONDEXConcept protein = graph.getConcept(poplarReg.getProtein(protID));

                if (!parsedGoIDs.containsKey(goAcc)) {
                    ONDEXConcept g;
                    if (goType.equals("molecular_function"))
                        g = graph.getFactory().createConcept(goAcc, dataSourceJGI, ccMolFunc, etIMPD);
                    else if (goType.equals("biological_process"))
                        g = graph.getFactory().createConcept(goAcc, dataSourceJGI, ccBioProc, etIMPD);
                    else
                        g = graph.getFactory().createConcept(goAcc, dataSourceJGI, ccCelComp, etIMPD);

                    g.createConceptAccession(goAcc, dataSourceGO, false);
                    g.createConceptName(goName, true);
                    parsedGoIDs.put(goAcc, g.getId());
                }
                ONDEXConcept go = graph.getConcept(parsedGoIDs.get(goAcc));

                if (goType.equals("molecular_function"))
                    graph.getFactory().createRelation(protein, go, rtFunc, etIMPD);
                else if (goType.equals("biological_process"))
                    graph.getFactory().createRelation(protein, go, rtProc, etIMPD);
                else
                    graph.getFactory().createRelation(protein, go, rtComp, etIMPD);

                inputLine = input.readLine();
            }

            input.close();

        } catch (FileNotFoundException fnfe) {
            parser.fireEventOccurred(new DataFileMissingEvent(fnfe
                    .getMessage(), "[Parser - start()]"));
        } catch (IOException ioe) {
            parser.fireEventOccurred(new DataFileErrorEvent(ioe
                    .getMessage(), "[Parser - start()]"));
        }
    }
}
