package net.sourceforge.ondex.parser.tair;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.tair.genome.ParseGenome;
import net.sourceforge.ondex.parser.tair.protein.ParseProtein;
import net.sourceforge.ondex.parser.tair.publication.ParseAnnotation;
import org.apache.log4j.Level;

import java.io.File;


/**
 * a subset of the TAIR10 database parsing genes, proteins, domains
 *
 * @author berendh
 */
@Status(description = "Parses Arabidopsis CDS and proteins, to be used with TAIR release 2010. Tested August 2013 (Artem Lysenko)", status = StatusType.STABLE)
@DatabaseTarget(name = "TAIR",
        description = "The Arabidopsis Information Resource (TAIR) maintains a database of genetic and molecular biology data for the model higher plant Arabidopsis thaliana ",
        version = "10",
        url = "http://www.arabidopsis.org")
@DataURL(name = "TAIR ftp",
        description = "ftp.arabidopsis.org",
        urls = {"ftp://ftp.arabidopsis.org/Genes/TAIR10_genome_release/*",
                "ftp://ftp.arabidopsis.org/Proteins/Domain",
                "ftp://ftp.arabidopsis.org/Proteins/Id conversions",
                "ftp://ftp.arabidopsis.org/Sequences/blast_datasets/TAIR10_blastsets/TAIR10_pep_20101207",
                "ftp://ftp.arabidopsis.org/Sequences/blast_datasets/TAIR10_blastsets/TAIR10_cdna_20101207",
                "ftp://ftp.arabidopsis.org/home/tair/Genes/TAIR10_genome_release/TAIR10_locushistory.txt"})
public class Parser extends ONDEXParser
{

    public final static boolean DEBUG = false;

    public static String TAIRX = "TAIR10";

    public static final String BLASTSETSSUBDIR = TAIRX + "_blastsets";

    public String getName() {
        return new String("TAIR");
    }

    public String getVersion() {
        return new String("04.06.2008");
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new BooleanArgumentDefinition(ArgumentNames.ANNOTATION, ArgumentNames.ANNOTATION_DESC, false, false),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                        FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    public void start() throws InvalidPluginArgumentException {

        GeneralOutputEvent so = new GeneralOutputEvent("Started TAIR Database Parser...", getCurrentMethodName());
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);

        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        //Get Dir for the tair files:
        String inFilesDir = dir.getAbsolutePath() + File.separator;

        //Parse The Genome
        ParseGenome genome = new ParseGenome();
        genome.parseGenome(inFilesDir, graph);

        boolean parseAnnotation = (Boolean) args.getUniqueValue(ArgumentNames.ANNOTATION);

        if (parseAnnotation) {
            //parse protein domain information
            ParseProtein protDomain = new ParseProtein();
            protDomain.parse(inFilesDir, graph, genome.getProteinsMap());

            //parse publications
            ParseAnnotation genePubs = new ParseAnnotation();
            genePubs.parse(inFilesDir, graph, genome.getProteinsMap());
        }

        //Parse ... (other things)

        so = new GeneralOutputEvent("Finished TAIR Database Parser...", getCurrentMethodName());
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);
    }

    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

    @Override
    public String[] requiresValidators() {
        // TODO Auto-generated method stub
        return new String[]{};
    }

    @Override
    public String getId() {
        return "tair";
    }
}