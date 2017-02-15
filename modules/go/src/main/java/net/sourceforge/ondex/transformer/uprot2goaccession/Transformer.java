package net.sourceforge.ondex.transformer.uprot2goaccession;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.annotationquality.GOTreeParser;
import net.sourceforge.ondex.algorithm.annotationquality.GoaIndexer;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Reads the uniprot accessions from the concept, looks up their associated
 * GOAs and adds them.
 *
 * @author Jochen Weile, B.Sc.
 */
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements ArgumentNames {

    //####FIELDS####

    /**
     * Set of taxids selected by user.
     */
    private HashSet<Integer> taxids;

    /**
     * whether to do a quality analysis.
     */
    private boolean qualityAnalysis;

    /**
     * location of input files.
     */
    private String goLocation, goaLocation;

    /**
     * debug flag.
     */
    private final boolean DEBUG = false;

    /**
     * metatdata taxid.
     */
    private AttributeName an_taxid;

    /**
     * metadata uniprot
     */
    private DataSource dataSource_uniprot;

    /**
     * metadata go.
     */
    private DataSource dataSource_go;

    /**
     * counts how many new accessions were written.
     */
    private int result = 0;

    /**
     * contains database ids to parse from goa file.
     */
    private HashSet<String> dbids = new HashSet<String>();


    //####METHODS####

    /**
     * @see net.sourceforge.ondex.transformer.ONDEXTransformer#getArgumentDefinitions()
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(TAXID_ARG, TAXID_ARG_DESC, true, null, true),
                new BooleanArgumentDefinition(ANALYZE_QUALITY_ARG, ANALYZE_QUALITY_ARG_DESC, false, false),
                new StringArgumentDefinition(GO_FILE_ARG, GO_FILE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(GOA_FILE_ARG, GOA_FILE_ARG_DESC, true, null, false),
                new StringArgumentDefinition(DBID_ARG, DBID_ARG_DESC, true, null, true),
        };
    }

    /**
     * @see net.sourceforge.ondex.transformer.ONDEXTransformer#getName()
     */
    @Override
    public String getName() {
        return "Uniprot2GOA Accession Transformer";
    }

    /**
     * @see net.sourceforge.ondex.transformer.ONDEXTransformer#getVersion()
     */
    @Override
    public String getVersion() {
        return "02.16.2008";
    }

    @Override
    public String getId() {
        return "uprot2goaccession";
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

        for (int taxid : taxids) {

            if (qualityAnalysis)
                qualityAnalysis("before", taxid);

            for (String dbid : dbids) {
                postAnnotationForTaxid(taxid, dbid);
            }

            if (qualityAnalysis)
                qualityAnalysis("after", taxid);

        }
        fireEventOccurred(new GeneralOutputEvent("Transformer successful. " + result + " new accessions written.", getName()));
    }

    /**
     * fetches all required metadata.
     */
    private void fetchMetaData() {
        an_taxid = graph.getMetaData().getAttributeName("TAXID");
        if (an_taxid == null)
            fireEventOccurred(new AttributeNameMissingEvent("Missing attribute name: TAXID", getName()));
        dataSource_uniprot = graph.getMetaData().getDataSource("UNIPROTKB");
        if (dataSource_uniprot == null)
            fireEventOccurred(new DataSourceMissingEvent("Missing DataSource: UNIPROTKB", getName()));
        dataSource_go = graph.getMetaData().getDataSource("GO");
        if (dataSource_go == null)
            fireEventOccurred(new DataSourceMissingEvent("Missing DataSource: GO", getName()));
    }

    /**
     * fetches and checks all user arguments.
     */
    private void fetchArguments() throws InvalidPluginArgumentException {
        taxids = new HashSet<Integer>();
        List<String> taxidlist = (List<String>) args.getObjectValueList(TAXID_ARG);
        for (String t : taxidlist) {
            try {
                int taxid = Integer.parseInt(t);
                taxids.add(taxid);
            } catch (NumberFormatException nfe) {
                fireEventOccurred(new WrongParameterEvent(t + " is not a valid taxonomy id.", getName()));
            }
        }

        qualityAnalysis = (Boolean) args.getUniqueValue(ANALYZE_QUALITY_ARG);

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

        file = args.getUniqueValue(GOA_FILE_ARG);
        if (file instanceof String) {
            String fileDummy = (String) file;
            File fileTester = new File(fileDummy);
            if (fileTester.isAbsolute()) {
                if (fileTester.exists())
                    goaLocation = fileTester.getAbsolutePath();
                else
                    fireEventOccurred(new DataFileMissingEvent("File " + fileDummy + " does not exist.", getName()));
            } else {
                if (fileDummy.startsWith(sep))
                    fileTester = new File(net.sourceforge.ondex.config.Config.ondexDir + fileDummy);
                else
                    fileTester = new File(net.sourceforge.ondex.config.Config.ondexDir + sep + fileDummy);

                if (fileTester.exists())
                    goaLocation = fileTester.getAbsolutePath();
                else
                    fireEventOccurred(new DataFileMissingEvent("File " + fileDummy + " does not exist.", getName()));
            }
        }

        Object[] dbidArr = args.getObjectValueArray(DBID_ARG);
        for (Object o : dbidArr) {
            String dbid = (String) o;
            dbids.add(dbid);
        }
    }

    /**
     * runs the actual uniprot2goa postannotation for one taxid.
     *
     * @param taxid
     */
    private void postAnnotationForTaxid(int taxid, String dbid) {

        try {
            GOTreeParser p = new GOTreeParser(goLocation);
            p.parseOboFile();
            GoaIndexer g = new GoaIndexer(p);
            g.parseFileByTaxon(goaLocation, taxid, dbid);

            for (ONDEXConcept c : graph.getConceptsOfAttributeName(an_taxid)) {
                String curr_taxidStr = (String) c.getAttribute(an_taxid).getValue();
                int curr_taxid = Integer.parseInt(curr_taxidStr);
                if (curr_taxid == taxid) {                           //correct concepts
                    HashSet<Integer> goas = new HashSet<Integer>();
                    HashSet<Integer> goas_old = new HashSet<Integer>();
                    for (ConceptAccession acc : c.getConceptAccessions()) {
                        if (acc.getElementOf().equals(dataSource_uniprot)) {//correct accessions
                            String uniprotID = acc.getAccession();
                            Iterator<Integer> goa_it = g.getGoaIteratorForProtID(uniprotID);
                            while (goa_it.hasNext()) {               //goas
                                int goa = goa_it.next();
                                goas.add(goa);
                            }
                        } else if (acc.getElementOf().equals(dataSource_go)) {//correct accessions
                            String go_acc = acc.getAccession();
                            try {
                                int go_acc_int = Integer.parseInt(go_acc.split(":")[1]);
                                goas_old.add(go_acc_int);
                            } catch (NumberFormatException nfe) {
                                System.err.println("GO acc couldn't be parsed!");
                            }
                        }
                    }
                    goas.removeAll(goas_old);
                    for (Integer goa : goas) {
                        String goStr = buildGoStr(goa);
                        c.createConceptAccession(goStr, dataSource_go, false);
                    }
                    result += goas.size();
                    if (DEBUG) System.out.println(goas.size() + " new accessions for concept " + c.getId());
                }
            }
        } catch (IOException ioe) {
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(), getName()));
        }
    }

    /**
     * builds a valid go id from an integer
     *
     * @param go the integer representing the id
     * @return the valid id
     */
    private String buildGoStr(int go) {
        String goBase = go + "";
        int zeros = 7 - goBase.length();
        char[] zeroBuffer = new char[zeros];
        for (int i = 0; i < zeros; i++)
            zeroBuffer[i] = '0';
        String goStr = "GO:" + new String(zeroBuffer) + goBase;
        return goStr;
    }

    /**
     * analyzes the quality of the current annotation of the concepts with the given
     * taxid and writes it to a file, tagged with the given string.
     *
     * @param file  an extension tag for the output file.
     * @param taxid the taxid.
     */
    private void qualityAnalysis(String file, int taxid) {
        String ondexdir = net.sourceforge.ondex.config.Config.ondexDir;

        String sep = File.separator;

        try {
            GOTreeParser p = new GOTreeParser(goLocation);
            p.parseOboFile();
            GoaIndexer g = new GoaIndexer(p);

            g.analyzeGraphAccByTaxon(graph, taxid);

            int[] histo = new int[15];
            for (int i = 0; i < histo.length; i++)
                histo[i] = 0;
            for (String uni : g.getAllUsedProtIDs()) {
                Iterator<Integer> it = g.getGoaIteratorForProtID(uni);
                double max = Double.NEGATIVE_INFINITY;
                while (it.hasNext()) {
                    double d = g.getInformationContent(it.next());
                    if (d > max) max = d;
                }
                int i = (int) max;
                histo[i]++;
            }

            BufferedWriter w = new BufferedWriter(new FileWriter(ondexdir + sep + taxid + "_stats_" + file + ".txt"));
            if (DEBUG) System.out.println(taxid + " " + file + ":");
            for (int i = 0; i < histo.length; i++) {
                w.write(i + "\t" + histo[i] + "\n");
                if (DEBUG) System.out.println(i + "\t" + histo[i]);
            }
            w.close();
        } catch (IOException ioe) {

        }
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
