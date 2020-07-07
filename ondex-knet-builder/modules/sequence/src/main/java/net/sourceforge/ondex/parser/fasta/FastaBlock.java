package net.sourceforge.ondex.parser.fasta;

/**
 * Class for storing fasta sequence and header.
 * 
 * @author hoekmanb
 */
public class FastaBlock {

	private StringBuffer sequence = new StringBuffer();

	private String header = new String();

	public void addSequence(String sequence) {
	    this.sequence.append(sequence);
	}

	public void setHeader(String header) {
	    this.header = removeGT(header);
	}

	public String getHeader() {
	    return header;
	}

	public String getSequence() {
	    return sequence.toString();
	}
	
	private String removeGT(String header) {
	    if(header.startsWith(">")){
		return header.substring(1);
	    }
	    return header;
	}
}
