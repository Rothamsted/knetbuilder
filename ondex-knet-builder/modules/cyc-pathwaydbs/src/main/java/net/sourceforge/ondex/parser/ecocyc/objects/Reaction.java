package net.sourceforge.ondex.parser.ecocyc.objects;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a reaction
 * 
 * @author peschr
 * 
 */
public class Reaction extends AbstractNode {

	private Set<Enzyme> gotCatBy = new HashSet<Enzyme>();

	private Set<Pathway> inPathway = new HashSet<Pathway>();

	private Set<AbstractNode> inUnknown = new HashSet<AbstractNode>();

	private Set<AbstractNode> left = new HashSet<AbstractNode>();

	private Set<AbstractNode> right = new HashSet<AbstractNode>();

	private String balancedState = null;

	private Boolean isSpontainious = null;

	private Float deltaGo = null;

	public void addInPathway(Pathway pathway) {
		inPathway.add(pathway);
	}

	public void addInUnknown(AbstractNode unknown) {
		inUnknown.add(unknown);
	}

	public void addRight(AbstractNode rightNode) {
		if (!right.contains(rightNode))
			right.add(rightNode);
	}

	public void addLeft(AbstractNode leftNode) {
		if (!left.contains(leftNode))
			left.add(leftNode);
	}

	/**
	 * For m_isp relation to a pathway.
	 * 
	 * @return Set of Pathway
	 */
	public Set<Pathway> getInPathway() {
		return inPathway;
	}

	/**
	 * For m_isp relation to something else.
	 * 
	 * @return Set of AbstractNode of unknown type
	 */
	public Set<AbstractNode> getInUnknown() {
		return inUnknown;
	}

	public Set<AbstractNode> getLeft() {
		return left;
	}

	public Set<AbstractNode> getRight() {
		return right;
	}

	public Set<Enzyme> getGotCatBy() {
		return gotCatBy;
	}

	/**
	 * Adds the EC number to the related enzyme.
	 * 
	 * @param gotCatBy
	 */
	public void addGetsCatBy(Enzyme getsCatBy) {
		this.gotCatBy.add(getsCatBy);
	}

	/**
	 * 
	 * @param balancedState
	 *            the state as in balanced or unbalanced
	 */
	public void setBalancedState(String balancedState) {
		this.balancedState = balancedState;
	}

	/**
	 * 
	 * @return the state as in balanced or unbalanced
	 */
	public String getBalancedState() {
		return balancedState;
	}

	public Boolean getIsSpontainious() {
		return isSpontainious;
	}

	public void setIsSpontainious(Boolean isSpontainious) {
		this.isSpontainious = isSpontainious;
	}

	public void setDeltaGo(float deltaGo) {
		this.deltaGo = deltaGo;
	}

	public Float getDeltaGo() {
		return deltaGo;
	}
}