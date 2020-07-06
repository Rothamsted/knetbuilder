package net.sourceforge.ondex.parser.transfac.tfclass;

import java.io.IOException;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.parser.transfac.AbstractTFParser;
import net.sourceforge.ondex.parser.transfac.ConceptWriter;

public class ClassParser extends AbstractTFParser {

    protected ClassParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    @Override
    protected void start() throws IOException {
        throw new RuntimeException("Not Implemented");
    }

}
