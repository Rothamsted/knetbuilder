package net.sourceforge.ondex.mapping.sequence2pfam.method;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * Provides basic implementations of the <code>IMethod<code> interface
 * @author peschr
 *
 */
public abstract class AbstractMethod implements IMethod{
	private String programDir ;
	private String evalue;
	private String pfamPfad ;
	private String tmpDir ;
	protected ONDEXGraph graph;
	private String bitscore;
	private String hmmThreshold;
	
	private AttributeName seqAtt;
	private ConceptClass conceptType;
	
	
	public AttributeName getSeqAtt() {
		return seqAtt;
	}

	public ConceptClass getConceptType() {
		return conceptType;
	}

	public String getProgramDir() {
		return programDir;
	}

	public String getEvalue() {
		return evalue;
	}

	public String getPfamPfad() {
		return pfamPfad;
	}

	public String getTmpDir() {
		return tmpDir;
	}
	
	public String getBitscore() {
		return bitscore;
	}
	/**
	 * Sets variables
	 * @param programDir - specifed where the program is located
	 * @param pfamPfad - path of the pfam database
	 * @param tmpDir - path for the temporary fasta styled database
	 * @param evalue - defines the e-value
	 */
	public AbstractMethod(String programDir, String pfamPfad, String tmpDir,
			String evalue, String bitscore, String hmmThreshold,
			ONDEXGraph graph, AttributeName seqAtt, ConceptClass conceptType) {
		super();
		this.graph = graph;
		this.programDir = programDir;
		this.pfamPfad = pfamPfad;
		this.tmpDir = tmpDir;
		this.evalue = evalue;
		this.bitscore = bitscore;
		this.hmmThreshold = hmmThreshold;
		this.seqAtt = seqAtt;
		this.conceptType = conceptType;
	}

	public String getHmmThreshold() {
		return hmmThreshold;
	}



}
