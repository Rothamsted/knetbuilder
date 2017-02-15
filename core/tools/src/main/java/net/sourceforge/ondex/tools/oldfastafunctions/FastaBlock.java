package net.sourceforge.ondex.tools.oldfastafunctions;

/**
 * Class for storing fasta sequence and header.
 *
 * @author hoekmanb
 */
@Deprecated
public class FastaBlock {

    private final StringBuilder sequence = new StringBuilder();

    private String header = "";

    /**
     * @param sequence the amino acid or nucleic acid sequence
     */
    public void addSequence(String sequence) {
        this.sequence.append(sequence);
    }

    /**
     * @param header the header line
     */
    public void setHeader(String header) {
        this.header = removeGT(header);
    }

    /**
     * @return the header line
     */
    public String getHeader() {
        return header;
    }

    /**
     * @return the amino acid or nucleic acid sequence
     */
    public String getSequence() {
        return sequence.toString();
    }

    /**
     * @param header the header to remove leading ">"
     * @return header without leading ">"
     */
    private String removeGT(String header) {
        if (header.charAt(0) == '>') {
            return header.substring(1);
	    }
	    return header;
	}
}
