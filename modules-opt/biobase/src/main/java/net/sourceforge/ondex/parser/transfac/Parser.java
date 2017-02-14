package net.sourceforge.ondex.parser.transfac;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.transfac.factor.FactorParser;
import net.sourceforge.ondex.parser.transfac.factor.gene.GeneParser;
import net.sourceforge.ondex.parser.transfac.matrix.MatrixParser;
import net.sourceforge.ondex.parser.transfac.site.SiteParser;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses the Transfac database from BioBase.
 *
 * @author taubertj
 */
@Status(description = "Untested formally", status = StatusType.EXPERIMENTAL)
@DatabaseTarget(name = "BIOBASE TRANSFAC",
        description = "A manually curated database of transcription factors",
        version = "",
        url = "http://www.biobase-international.com/index.php?id=transfac")
public class Parser extends ONDEXParser
{

    private static Parser instance;

    @Override
    public String getName() {
        return new String("Transfac parser");
    }

    @Override
    public String getVersion() {
        return new String("28.03.2007");
    }

    @Override
    public String getId() {
        return "transfac";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                        FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    public void start() throws InvalidPluginArgumentException {
        instance = this;

        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        GeneralOutputEvent so = new GeneralOutputEvent(
                "Using transfac input directory " + dir.getAbsolutePath(), "");
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);

        try {
            ConceptWriter cw = new ConceptWriter(graph);

            GeneParser gp = new GeneParser(cw, args);
            gp.start();
            gp = null;

            MatrixParser mp = new MatrixParser(cw, args);
            mp.start();
            mp = null;

            FactorParser fp = new FactorParser(cw, args);
            fp.start();
            fp = null;

            SiteParser sp = new SiteParser(cw, args);
            sp.start();
            sp = null;

            cw.makeFactorInteraction();

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fireEventOccurred(new DataFileMissingEvent(fnfe.getMessage(), "[Parser - start]"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(), "[Parser - start]"));
        }

        so = new GeneralOutputEvent("Transfac parsing finished!", "[Parser - start]");
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);

    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"cvregex", "taxonomy"};
    }

}
