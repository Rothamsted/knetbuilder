package net.sourceforge.ondex.parser.tigrricefasta;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.apache.log4j.Level;

import java.util.List;


/**
 * FASTA FILE parser
 *
 * @author berendh
 */
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
public class Parser extends ONDEXParser
{

    public String getName() {
        return new String("TIGER_RICE_FASTA_FILE_PARSER");
    }

    public String getVersion() {
        return new String("17.05.2007");
    }

    @Override
    public String getId() {
        return "tigrricefasta";
    }

    public void start() throws InvalidPluginArgumentException {

        GeneralOutputEvent so = new GeneralOutputEvent("Starting Tigr Rice Fasta File parsing...", getCurrentMethodName());
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);

        List<String> files = (List<String>) args.getObjectValueList(ArgumentNames.IMPORTFILES_ARG);

        ParseGenome parseGenome = new ParseGenome();
        parseGenome.parse(files.toArray(new String[files.size()]), graph);

        so = new GeneralOutputEvent("Finished Tigr Rice Fasta File parsing...", getCurrentMethodName());
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new FileArgumentDefinition(ArgumentNames.IMPORTFILES_ARG, ArgumentNames.IMPORTFILES_ARG, true, true, false, true)
        };
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * Convenience method for outputting the current method name in a dynamic way
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

}