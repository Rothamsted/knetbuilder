package net.sourceforge.ondex.tools.oldfastafunctions;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * 
 * @author berendh
 *
 */
@Deprecated
public abstract class WriteFastaFile {
    
    public abstract void parseFastaBlock(ONDEXGraph graph,FastaBlock fasta);

}
