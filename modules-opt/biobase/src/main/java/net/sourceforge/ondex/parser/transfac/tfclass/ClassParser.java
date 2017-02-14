package net.sourceforge.ondex.parser.transfac.tfclass;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.parser.transfac.AbstractTFParser;
import net.sourceforge.ondex.parser.transfac.ConceptWriter;

import java.io.IOException;

public class ClassParser extends AbstractTFParser {

    protected ClassParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    @Override
    protected void start() throws IOException {
        throw new RuntimeException("Not Implemented");
    }

}
