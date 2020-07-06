package net.sourceforge.ondex.parser.affymetrix;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.parser.ONDEXParser;

/**
 * looks for files of the form
 * <p/>
 * *.consensus and *.target.fasta (only one each of these in a dir and a mapping
 * will be made between them)
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Parser extends ONDEXParser
{

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new FileArgumentDefinition(
                FileArgumentDefinition.INPUT_DIR,
                "directory with *.consensus and *.target.fasta files", true,
                true, true, false)};
    }

    @Override
    public String getName() {
        return "affymetrix parser";
    }

    @Override
    public String getVersion() {
        return "pre-alpha";
    }

    @Override
    public String getId() {
        return "affymetrix";
    }

    @Override
    public String[] requiresValidators() {
        return null;
    }

    @Override
    public void start() throws Exception {
        AffyParser parser = new AffyParser(args);
        parser.setONDEXGraph(graph);
    }

}
