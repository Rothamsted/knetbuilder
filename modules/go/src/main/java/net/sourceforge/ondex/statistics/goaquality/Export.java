package net.sourceforge.ondex.statistics.goaquality;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.annotationquality.GOTreeParser;
import net.sourceforge.ondex.algorithm.annotationquality.GoTerm;
import net.sourceforge.ondex.algorithm.annotationquality.GoaIndexer;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.export.ONDEXExport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;


/**
 * Performs a quality analysis on the GO annotations of the underlying graph
 *
 * @author Jochen Weile, B.Sc.
 */
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
@Status(description = "Set to DISCONTINUED 4 May 2010 due to Writer not set via Arguements. (Christian)", status = StatusType.DISCONTINUED)
public class Export extends ONDEXExport implements ArgumentNames {

    //####FIELDS####

    /**
     * Set of taxids selected by user.
     */
    private HashSet<Integer> taxids;

    /**
     * location of input files.
     */
    private String goLocation;

    /**
     * debug flag.
     */
    private final boolean DEBUG = false;

    /**
     * metadata taxid.
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
     * output writer.
     */
    private BufferedWriter w;


    //####METHODS####

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(TAXID_ARG, TAXID_ARG_DESC, true, null, true),
                new StringArgumentDefinition(GO_FILE_ARG, GO_FILE_ARG_DESC, true, null, false)
        };
    }

    /**
     * @see net.sourceforge.ondex.statistics.ONDEXStatistics#getName()
     */
    @Override
    public String getName() {
        return "GO Annotation quality assessment tool";
    }

    /**
     * @see net.sourceforge.ondex.statistics.ONDEXStatistics#getVersion()
     */
    @Override
    public String getVersion() {
        return "09.10.2008";
    }

    @Override
    public String getId() {
        return "goaquality";
    }

    /**
     * @see net.sourceforge.ondex.statistics.ONDEXStatistics#requiresIndexedGraph()
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
            qualityAnalysis(taxid);
        }

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
        List<String> taxidlist = (List<String>) getArguments().getObjectValueList(TAXID_ARG);
        for (String t : taxidlist) {
            try {
                int taxid = Integer.parseInt(t);
                taxids.add(taxid);
            } catch (NumberFormatException nfe) {
                fireEventOccurred(new WrongParameterEvent(t + " is not a valid taxonomy id.", getName()));
            }
        }

        String sep = File.separator;

        String file = (String) getArguments().getUniqueValue(GO_FILE_ARG);

        String fileDummy = file;
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


    /**
     * analyzes the quality of the current annotation of the concepts with the given
     * taxid and writes it to a file, tagged with the given string.
     *
     * @param taxid the taxid.
     */
    private void qualityAnalysis(int taxid) {
        String ondexdir = net.sourceforge.ondex.config.Config.ondexDir;

        String sep = File.separator;

        try {
            GOTreeParser p = new GOTreeParser(goLocation);
            p.parseOboFile();
            GoaIndexer g = new GoaIndexer(p);

            g.analyzeGraphByTaxon(graph, taxid);

            int length = 15;
            int[] histoBP = new int[length];
            int[] histoMF = new int[length];
            int[] histoCC = new int[length];
            for (int i = 0; i < length; i++) {
                histoBP[i] = 0;
                histoMF[i] = 0;
                histoCC[i] = 0;
            }
            for (String uni : g.getAllUsedProtIDs()) {
                Iterator<Integer> it = g.getGoaIteratorForProtID(uni);
                double maxBP = Double.NEGATIVE_INFINITY;
                double maxMF = Double.NEGATIVE_INFINITY;
                double maxCC = Double.NEGATIVE_INFINITY;
                while (it.hasNext()) {
                    int goID = it.next();
                    int namespace = p.getNamespaceOfTerm(goID);
                    double d = g.getInformationContent(goID);
                    if ((namespace == GoTerm.DOMAIN_BIOLOGICAL_PROCESS) && (d > maxBP)) maxBP = d;
                    else if ((namespace == GoTerm.DOMAIN_MOLECULAR_FUNCTION) && (d > maxMF)) maxMF = d;
                    else if ((namespace == GoTerm.DOMAIN_CELLULAR_COMPONENT) && (d > maxCC)) maxCC = d;
                }
                if (maxBP > Double.NEGATIVE_INFINITY)
                    histoBP[(int) maxBP]++;
                if (maxMF > Double.NEGATIVE_INFINITY)
                    histoMF[(int) maxMF]++;
                if (maxCC > Double.NEGATIVE_INFINITY)
                    histoCC[(int) maxCC]++;
            }

            int num_genes = g.getAllUsedProtIDs().size();

            if (DEBUG) System.out.println(taxid + ":");
            w = new BufferedWriter(new FileWriter(ondexdir + sep + taxid + "_stats" + ".txt"));

            output("IC\tBP\tMF\tCC");
            for (int i = 0; i < histoBP.length; i++) {
                output(i + "\t" + histoBP[i] + "\t" + histoMF[i] + "\t" + histoCC[i]);
            }
            output("\nnumber of annotated concepts: " + num_genes);

            output("\n\nrelative values:\n");
            output("IC\tBP\tMF\tCC");
            for (int i = 0; i < histoBP.length; i++) {
                output(i + "\t" + (((double) histoBP[i]) / ((double) num_genes)) + "\t"
                        + (((double) histoMF[i]) / ((double) num_genes)) + "\t"
                        + (((double) histoCC[i]) / ((double) num_genes)));
            }

            w.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void output(String s) {
        if (DEBUG) System.out.println(s);
        try {
            w.write(s + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresValidators()
     */
    public String[] requiresValidators() {
        return new String[0];
    }
}
