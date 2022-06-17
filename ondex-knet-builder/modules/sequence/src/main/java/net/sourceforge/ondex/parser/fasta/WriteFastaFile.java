package net.sourceforge.ondex.parser.fasta;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.workflow.InvalidPluginArgumentException;

public abstract class WriteFastaFile {

    public abstract void parseFastaBlock(ONDEXGraph graph, FastaBlock fasta) throws FormatFileException, InvalidPluginArgumentException;

    public static class FormatFileException extends Throwable {
        public FormatFileException(String s) {
            super(s);
        }
    }

}
