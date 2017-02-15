package net.sourceforge.ondex.parser.metacyc.objects;

import java.util.ArrayList;
/**
 * Represents a compound
 * @author peschr
 *
 */
public class Compound extends AbstractNode{
	private String molweight;
	private String systematicName;
	private String smiles;
	
	public String getMolweight() {
		return molweight;
	}
	public void setMolweight(String molweight) {
		this.molweight = molweight;
	}
	public String getSmiles() {
		return smiles;
	}
	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}
	public String getSystematicName() {
		return systematicName;
	}
	public void setSystematicName(String systematicName) {
		if ( super.getSynonym().contains(systematicName) )
			super.getSynonym().remove(systematicName);
		this.systematicName = systematicName;
	}
	
	private ArrayList<AbstractNode> cofactorOf = new ArrayList<AbstractNode>() ;
	public ArrayList<AbstractNode> getCofactorOf() {
		return cofactorOf;
	}
	public void addCofactorOf(AbstractNode node){
		this.cofactorOf.add(node);
	}
}
