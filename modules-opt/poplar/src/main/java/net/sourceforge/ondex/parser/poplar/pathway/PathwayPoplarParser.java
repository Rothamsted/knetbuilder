package net.sourceforge.ondex.parser.poplar.pathway;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.*;
import net.sourceforge.ondex.parser.poplar.MetaData;
import net.sourceforge.ondex.parser.poplar.Parser;
import net.sourceforge.ondex.parser.poplar.Registry;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * Parser for the Poplar EC/Pathway annotation file.
 * <p/>
 * Download from:
 * ftp://ftp.jgi-psf.org/pub/JGI_data/Poplar/annotation/v1.1/functional/Poptr1_1.ecpathwayinfo.tab.gz
 * <p/>
 * Creates Protein --> Enzyme --> Path concepts
 *
 * @author keywan
 */
public class PathwayPoplarParser {

    private static final boolean DEBUG = false;

    private DataSource dataSourceJGI;
    private DataSource dataSourceEC;
    private ConceptClass ccProtein;
    private ConceptClass ccEnzyme;
    private ConceptClass ccPATHWAY;
    private RelationType rtCATC;
    private RelationType rtMISP;
    private EvidenceType etIMPD;
    private AttributeName anTaxID;

    private HashMap<String, Integer> parsedEcIDs;
    private HashMap<String, Integer> parsedKeggIDs;

    private Registry poplarReg;


    /**
     * Initialisation of parameters
     */
    public PathwayPoplarParser(Registry poplarReg) {
        this.poplarReg = poplarReg;

        parsedEcIDs = new HashMap<String, Integer>();
        parsedKeggIDs = new HashMap<String, Integer>();
    }


    /**
     * Parses every line into a Protein to Enzyme to Pathway relation
     */
    public void start(ONDEXGraph graph, ONDEXPluginArguments pa, Parser parser) throws InvalidPluginArgumentException {
        GeneralOutputEvent goe = new GeneralOutputEvent("Parsing EC annotations and KEGG Pathways...", "[Parser - start()]");
        parser.fireEventOccurred(goe);

        //required MetaData
        try {
            dataSourceEC = parser.requireDataSource(MetaData.CV_EC);
            dataSourceJGI = parser.requireDataSource(MetaData.CV_PHYTOZOME);
            ccProtein = parser.requireConceptClass(MetaData.CC_PROTEIN);
            ccEnzyme = parser.requireConceptClass(MetaData.CC_ENZYME);
            ccPATHWAY = parser.requireConceptClass(MetaData.CC_PATHWAY);
            rtCATC = parser.requireRelationType(MetaData.RT_CATC);
            rtMISP = parser.requireRelationType(MetaData.RT_MISP);
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

        String filename = inputDir.getAbsolutePath() + File.separator + "Poptr1_1.ecpathwayinfo.tab.gz";

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

                if (DEBUG && ++count == 1000) break;

                String[] col = inputLine.split("\t");
                String protID = col[0];
                String ecID = col[1];
                String definition = col[2];
                String catalyticActivity = col[3];
//				String coFactors = col[4];
//				String associatedDiseases = col[5];
                String pathwayKegg = col[6];
                String pathwayClass = col[7];
                String pathwayType = col[8];

                if (!poplarReg.containsProtein(protID)) {
                    ONDEXConcept p = graph.getFactory().createConcept(protID, dataSourceJGI, ccProtein, etIMPD);
                    p.createAttribute(anTaxID, Parser.POPLAR_TAX_ID, false);
                    poplarReg.addProtein(protID, p.getId());
                }
                ONDEXConcept protein = graph.getConcept(poplarReg.getProtein(protID));

                if (!parsedEcIDs.containsKey(ecID)) {
                    ONDEXConcept g = graph.getFactory().createConcept(ecID, dataSourceJGI, ccEnzyme, etIMPD);
                    if (!catalyticActivity.equals("\\N"))
                        g.setDescription(catalyticActivity);
                    g.createConceptAccession(ecID, dataSourceEC, false);
                    g.createConceptName(definition, true);
                    parsedEcIDs.put(ecID, g.getId());
                }
                ONDEXConcept enzyme = graph.getConcept(parsedEcIDs.get(ecID));

                ONDEXConcept pathway = null;
                if (!pathwayKegg.equals("\\N")) {
                    if (!parsedKeggIDs.containsKey(pathwayKegg)) {
                        ONDEXConcept g = graph.getFactory().createConcept(pathwayKegg, dataSourceJGI, ccPATHWAY, etIMPD);
                        g.createConceptName(pathwayKegg, true);
                        if (!pathwayType.equals("\\N"))
                            g.setAnnotation(pathwayType);
                        if (!pathwayClass.equals("\\N"))
                            g.setDescription(pathwayClass);
                        parsedKeggIDs.put(pathwayKegg, g.getId());
                    }
                    pathway = graph.getConcept(parsedKeggIDs.get(pathwayKegg));

                    if (graph.getRelation(enzyme, pathway, rtMISP) == null) {
                        graph.getFactory().createRelation(enzyme, pathway, rtMISP, etIMPD);
                    }

                }

                if (graph.getRelation(protein, enzyme, rtCATC) == null) {
                    graph.getFactory().createRelation(protein, enzyme, rtCATC, etIMPD);
                }

                //add pathway context to everything
                if (pathway != null) {
                    ONDEXRelation relEnzyme = graph.getRelation(enzyme, pathway, rtMISP);
                    ONDEXRelation relProtein = graph.getRelation(protein, enzyme, rtCATC);
                    if (!pathway.getTags().contains(pathway))
                        pathway.addTag(pathway);
                    if (!relEnzyme.getTags().contains(pathway))
                        relEnzyme.addTag(pathway);
                    if (!enzyme.getTags().contains(pathway))
                        enzyme.addTag(pathway);
                    if (!protein.getTags().contains(pathway))
                        protein.addTag(pathway);
                    if (!relProtein.getTags().contains(pathway))
                        relProtein.addTag(pathway);
                }

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
