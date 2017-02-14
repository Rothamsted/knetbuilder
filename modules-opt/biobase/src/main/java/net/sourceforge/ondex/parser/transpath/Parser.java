package net.sourceforge.ondex.parser.transpath;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses the Transpath database from BioBase.
 *
 * @author taubertj
 */
@Status(description = "Untested formally", status = StatusType.EXPERIMENTAL)
@DatabaseTarget(name = "BIOBASE TRANSPATH",
        description = "A manually curated database of pathways",
        version = "",
        url = "http://www.biobase-international.com")
public class Parser extends ONDEXParser
{

    // keep a static reference for event propagation
    private static Parser instance;

    public Parser() {
        instance = this;
    }

    public String getName() {
        return new String("Transpath parser");
    }

    public String getVersion() {
        return new String("26.03.2007");
    }

    @Override
    public String getId() {
        return "transpath";
    }


    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new BooleanArgumentDefinition(ArgumentNames.PARSE_MOLECULES_ARG, ArgumentNames.PARSE_MOLECULES_ARG, false, true),
                new BooleanArgumentDefinition(ArgumentNames.PARSE_PATHWAYS_ARG, ArgumentNames.PARSE_PATHWAYS_ARG, false, true),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                        FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    public void start() throws InvalidPluginArgumentException {

        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        GeneralOutputEvent so = new GeneralOutputEvent(
                "Using transpath input directory " + dir.getAbsolutePath(), "setONDEXGraph(AbstractONDEXGraph graph)");
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);

        Boolean parseMolecules = (Boolean) args.getUniqueValue(ArgumentNames.PARSE_MOLECULES_ARG);
        Boolean parsePathways = (Boolean) args.getUniqueValue(ArgumentNames.PARSE_PATHWAYS_ARG);

        if (parsePathways) { //dependencies
            parseMolecules = true;
        }

        // call all parsers
        try {
            ConceptWriter conceptParser = new ConceptWriter(graph);

            GeneParser gp = new GeneParser(conceptParser, args);
            gp.start();
            gp = null;

            if (parseMolecules) {
                MoleculeParser mp = new MoleculeParser(conceptParser, args);
                mp.start();
                mp = null;
            }

            if (parsePathways) {
                ReactionParser rp = new ReactionParser(conceptParser, args);
                rp.start();
                rp = null;
            }

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fireEventOccurred(new DataFileErrorEvent(fnfe.getMessage(), "setONDEXGraph(AbstractONDEXGraph graph)"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(), "setONDEXGraph(AbstractONDEXGraph graph)"));
        }

        so = new GeneralOutputEvent("Transpath parsing finished!", "setONDEXGraph(AbstractONDEXGraph graph)");
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"taxonomy"};
    }

}
