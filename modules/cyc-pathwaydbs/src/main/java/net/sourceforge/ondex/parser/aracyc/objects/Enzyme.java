package net.sourceforge.ondex.parser.aracyc.objects;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents an enzyme
 * @author peschr
 * 
 */
public class Enzyme extends AbstractNode {
	public Enzyme() {
		isEnzymeComplex = false;
	}

	private ArrayList<AbstractNode> activator = new ArrayList<AbstractNode>();
	private ArrayList<AbstractNode> inhibitor = new ArrayList<AbstractNode>();
	private ArrayList<AbstractNode> cofactors = new ArrayList<AbstractNode>();
	private Reaction catBy;
	private ECNumber ecNumber;

	private boolean isEnzymeComplex;
	public ArrayList<Protein> is_a = new ArrayList<Protein>();

	public ArrayList<Protein> getIs_a() {
		return is_a;
	}

	/**
	 * Adds a Protein to an Enzyme. If the Enzyme is a Enzyme-Complex the method
	 * returns true, else false
	 * 
	 * @param is_a
	 * @return
	 */
	public boolean addIs_a(Protein is_a) {
		if (this.is_a.size() > 0 && isEnzymeComplex == false) {
			isEnzymeComplex = true;
			Iterator<Protein> proteins = this.is_a.iterator();
			while (proteins.hasNext()) {
				proteins.next().addIsMemberOf(this);
			}
		}

		this.is_a.add(is_a);
		return isEnzymeComplex;
	}

	public boolean isEnzymeComplex() {
		return isEnzymeComplex;
	}

	public void addActivator(AbstractNode node) {
		activator.add(node);

	}

	public void addInhibitor(AbstractNode node) {
		inhibitor.add(node);
	}

	public ArrayList<AbstractNode> getActivator() {
		return activator;
	}

	public ArrayList<AbstractNode> getInhibitor() {
		return inhibitor;
	}

	public Reaction getCatBy() {
		return catBy;
	}

	public void setCatBy(Reaction catBy) {
		this.catBy = catBy;
	}

	public ECNumber getEcNumber() {
		return ecNumber;
	}

	public void setEcNumber(ECNumber ecNumber) {
		this.ecNumber = ecNumber;
	}

	public void addCofactor(AbstractNode node) {
		cofactors.add(node);
	}

}
