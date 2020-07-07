package net.sourceforge.ondex.parser.transfac.site;

import java.util.HashSet;

import net.sourceforge.ondex.parser.transfac.AbstractTFObject;


public class SiteObject extends AbstractTFObject {

	public SiteObject(String accession) {
		super(accession);
	}

	private String id;
	private String comment;
	
	private HashSet<String> bindingFactorAccessions = new HashSet<String>();
	private String seq;
	private String seq_type;
	private String elem_denom;

	private String def_of_first_position = "transcription start site";
	private Integer lastPosBindingSite;
	private Integer firstPosBindingSite;
	private String matrixAccession;
	
	private String situatedTo;

	public void setId(String id) {
		this.id = id;
	}

	public void setComments(String comment) {
		this.comment = comment;
	}

	public void addBindingFactorAccession(String string) {
		bindingFactorAccessions.add(string);
	}

	public void setSituatedTo(String genAcc) {
		this.situatedTo = genAcc;
	}
	
	public String getSituatedTo() {
		return this.situatedTo;
	}
	
	public void setSequence(String seq) {
		this.seq = seq;
	}

	public void setSequenceType(String seq_type) {
		this.seq_type = seq_type;
	}

	public void setElementDenomination(String elem_denom) {
		this.elem_denom = elem_denom;
	}

	public void setDefinitionOfFirstPosition(String def_of_first_position) {
		this.def_of_first_position = def_of_first_position;
	}

	public void setLastPosBindingSite(Integer lastPosBindingSite) {
		this.lastPosBindingSite = lastPosBindingSite;
	}

	public void setFirstPosBindingSite(Integer firstPosBindingSite) {
		this.firstPosBindingSite = firstPosBindingSite;
	}

	public void addMatrixAccession(String matrixAccession) {
		this.matrixAccession = matrixAccession;
	}

	public HashSet<String> getBindingFactorAccessions() {
		return bindingFactorAccessions;
	}

	public String getComment() {
		return comment;
	}

	public String getDef_of_first_position() {
		return def_of_first_position;
	}

	public String getElem_denom() {
		return elem_denom;
	}

	public Integer getFirstPosBindingSite() {
		return firstPosBindingSite;
	}

	public String getId() {
		return id;
	}

	public Integer getLastPosBindingSite() {
		return lastPosBindingSite;
	}

	public String getMatrixAccession() {
		return matrixAccession;
	}

	public String getSeq() {
		return seq;
	}

	public String getSeq_type() {
		return seq_type;
	}

}
