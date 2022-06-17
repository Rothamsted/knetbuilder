package net.sourceforge.ondex.parser.transfac.tfclass;

import java.io.IOException;

import net.sourceforge.ondex.parser.transfac.AbstractTFParser;
import net.sourceforge.ondex.parser.transfac.ConceptWriter;
import net.sourceforge.ondex.workflow.InvalidPluginArgumentException;
import net.sourceforge.ondex.workflow.ONDEXPluginArguments;

public class ClassParser extends AbstractTFParser {

    protected ClassParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    @Override
    protected void start() throws IOException {
        throw new RuntimeException("Not Implemented");
    }

}
