package net.sourceforge.ondex.transformer.goinformationcontent;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.annotationquality.GOTreeParser;
import net.sourceforge.ondex.algorithm.annotationquality.GoaIndexer;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.io.File;
import java.io.IOException;

/**
 * Iterates over the GO concepts of an Ondex graph, calculates its
 * information content (IC), and adds a Attribute attribute to each GO concept.
 *
 * @author Keywan
 */
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements ArgumentNames {

    /**
     * Set of taxids selected by user.
     */
    private Integer taxid;

    /**
     * location of input files.
     */
    private String goLocation;

    /**
     * counts how many new accessions were written.
     */
    private int result = 0;

    /**
     * metadata
     */
    private AttributeName anIC;
    private AttributeName anTaxid;
    private DataSource dataSourceGo;

    public final static String BioProc = "BioProc";
    public final static String MolFunc = "MolFunc";
    public final static String CelComp = "CelComp";


    /**
     * @see net.sourceforge.ondex.transformer.ONDEXTransformer#getArgumentDefinitions()
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(TAXID_ARG, TAXID_ARG_DESC, false, "-1", false),
                new StringArgumentDefinition(GO_FILE_ARG, GO_FILE_ARG_DESC, true, null, false),
        };
    }

    /**
     * @see net.sourceforge.ondex.transformer.ONDEXTransformer#getName()
     */
    @Override
    public String getName() {
        return "Annotate Information Content";
    }

    /**
     * @see net.sourceforge.ondex.transformer.ONDEXTransformer#getVersion()
     */
    @Override
    public String getVersion() {
        return "11.09.2009";
    }

    @Override
    public String getId() {
        return "goinformationcontent";
    }

    /**
     * @see net.sourceforge.ondex.transformer.ONDEXTransformer#requiresIndexedGraph()
     */
    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#start()
     */
    @Override
    public void start() throws InvalidPluginArgumentException {
        fetchArguments();
        fetchMetaData();

        fireEventOccurred(new GeneralOutputEvent("Start IC analysis for taxid: " + taxid, getName()));

        informationContentAnalysis(taxid);

        fireEventOccurred(new GeneralOutputEvent("Transformer successful. " + result + " new accessions written.", getName()));
    }


    /**
     * fetches all required metadata.
     */
    private void fetchMetaData() {
        anTaxid = graph.getMetaData().getAttributeName("TAXID");
        if (anTaxid == null)
            fireEventOccurred(new AttributeNameMissingEvent("Missing attribute name: TAXID", getName()));
        dataSourceGo = graph.getMetaData().getDataSource("GO");
        if (dataSourceGo == null)
            fireEventOccurred(new DataSourceMissingEvent("Missing DataSource: GO", getName()));
        anIC = graph.getMetaData().getAttributeName("IC");
        if (anIC == null) {
            graph.getMetaData().createAttributeName("IC", "Information Content", "IC for GO", null, Double.class, null);
            anIC = graph.getMetaData().getAttributeName("IC");
//TODO			fireEventOccurred(new AttributeNameMissingEvent("MISSING AN: IC", getName()));
        }
    }

    /**
     * fetches and checks all user arguments.
     */
    private void fetchArguments() throws InvalidPluginArgumentException {

        if (args.getObjectValueList(TAXID_ARG) != null) {
            String s = (String) args.getUniqueValue(TAXID_ARG);
            taxid = Integer.parseInt(s);
        }

        String sep = File.separator;

        Object file = args.getUniqueValue(GO_FILE_ARG);
        if (file instanceof String) {
            String fileDummy = (String) file;
            File fileTester = new File(fileDummy);
            if (fileTester.isAbsolute()) {
                if (fileTester.exists())
                    goLocation = fileTester.getAbsolutePath();
                else
                    fireEventOccurred(new DataFileMissingEvent("File " + fileDummy + " does not exist.", getName()));
            } else {
                if (fileDummy.startsWith(sep))
                    fileTester = new File(net.sourceforge.ondex.config.Config.ondexDir + fileDummy);
                else
                    fileTester = new File(net.sourceforge.ondex.config.Config.ondexDir + sep + fileDummy);

                if (fileTester.exists())
                    goLocation = fileTester.getAbsolutePath();
                else
                    fireEventOccurred(new DataFileMissingEvent("File " + fileDummy + " does not exist.", getName()));
            }
        }


    }


    /**
     * analyzes the quality of the current annotation of the concepts with the given
     * taxid and writes it to a file, tagged with the given string.
     *
     * @param taxid the taxid.
     */
    private void informationContentAnalysis(int taxid) {


        try {
            GOTreeParser p = new GOTreeParser(goLocation);
            p.parseOboFile();
            GoaIndexer g = new GoaIndexer(p);

            // if no taxid specified it will use proteins from all
            // taxa (taxid: -1) to calculate the IC statistics
            g.analyzeGraphByTaxon(graph, taxid);

            System.out.println("Number of proteins: " + g.getAllUsedProtIDs().size());
            System.out.println("Number of GO terms: " + g.getAllUsedGoTerms().size());

            for (ONDEXConcept c : graph.getConcepts()) {
                String cc = c.getOfType().getId();
                //if that is also of the correct type
                if (cc.equals(BioProc) || cc.equals(MolFunc) || cc.equals(CelComp)) {
                    //iterate over its accessions
                    for (ConceptAccession acc : c.getConceptAccessions()) {
                        //if the accession is of DataSource GO
                        if (acc.getElementOf().equals(dataSourceGo)) {
                            String goID = acc.getAccession();
                            int goID_int = Integer.parseInt(goID.split(":")[1]);
                            Double ic = g.getInformationContent(goID_int);
                            // ignore GO terms that were not used as an annotation
                            if (ic.equals(Double.NaN)) {
                                continue;
                            }
                            if (c.getAttribute(anIC) == null) {
                                c.createAttribute(anIC, ic, false);
                                result++;
                                break;
                            } else {
                                System.out.println("Warning: IC Attribute exists already for " + goID);
                            }
                        }
                    }
                }

            }


        } catch (IOException ioe) {

        }
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
