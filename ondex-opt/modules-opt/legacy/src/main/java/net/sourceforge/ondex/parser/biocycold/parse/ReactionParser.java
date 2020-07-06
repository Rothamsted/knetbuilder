package net.sourceforge.ondex.parser.biocycold.parse;

import java.util.Iterator;

import net.sourceforge.ondex.parser.biocycold.objects.AbstractNode;
import net.sourceforge.ondex.parser.biocycold.objects.Compound;
import net.sourceforge.ondex.parser.biocycold.objects.ECNumber;
import net.sourceforge.ondex.parser.biocycold.objects.Enzyme;
import net.sourceforge.ondex.parser.biocycold.objects.Pathway;
import net.sourceforge.ondex.parser.biocycold.objects.Reaction;
import net.sourceforge.ondex.parser.biocycold.objects.SinkFactory;

/**
 * Parser for the reaction.* files(s)
 * 
 * @author peschr
 * 
 */
public class ReactionParser extends AbstractParser {

	private Reaction reaction;

	@Override
	public void distribute(String key, String value) throws Exception {
		if (key.equals("LEFT"))
			this.addLeft(value);
		else if (key.equals("RIGHT"))
			this.addRight(value);
		else if (key.equals("EC-NUMBER"))
			this.addECNumber(value);
		else if (key.equals("IN-PATHWAY"))
			this.addInPathway(value);
		else if (key.equals("BALANCE-STATE"))
			this.addState(value);
		else if (key.equals("SPONTANEOUS?"))
			this.setSpontaneous(value);
		else if (key.equals("DELTAG0"))
			this.setDeltaGo(value);
	}

	private void setDeltaGo(String value) {
		try {
			reaction.setDeltaGo(Float.parseFloat(value));
		} catch (NumberFormatException e) {
			System.err.println("Invalid DeltaGo ==> " + value);
		}
	}

	private void setSpontaneous(String value) {
		reaction.setIsSpontainious(value.trim().equalsIgnoreCase("T"));
	}

	private void addState(String value) {
		reaction.setBalancedState(value);
	}

	public void addInPathway(String value) throws Exception {
		try {
			Pathway path = (Pathway) SinkFactory.getInstance().create(
					Pathway.class, value);
			reaction.addInPathway(path);
		} catch (Exception e) {
			reaction.addInUnknown(SinkFactory.getInstance().findByUniqueId(
					value));
		}
	}

	public void addECNumber(String value) throws Exception {
		ECNumber ec = (ECNumber) SinkFactory.getInstance().create(
				ECNumber.class, value);
		if (reaction.getGotCatBy().size() > 0) {
			Iterator<Enzyme> i = reaction.getGotCatBy().iterator();
			while (i.hasNext()) {
				i.next().setEcNumber(ec);
			}
		}

		for (Pathway pathway : reaction.getInPathway()) {
			pathway.addECNumber(ec);
		}
	}

	/**
	 * 
	 * @param value
	 * @throws Exception
	 */
	public void addLeft(String value) throws Exception {
		try {
			AbstractNode node = SinkFactory.getInstance().findByUniqueId(value);
			reaction.addLeft(node);
		} catch (Exception e) {
			// If a link is not valid. Create a new Compound.
			AbstractNode node = SinkFactory.getInstance().create(
					Compound.class, value);
			reaction.addLeft(node);
		}
	}

	public void addRight(String value) throws Exception {
		try {
			AbstractNode node = SinkFactory.getInstance().findByUniqueId(value);
			reaction.addRight(node);
		} catch (Exception e) {
			// If a link is not valid. Create a new Compound.
			AbstractNode node = SinkFactory.getInstance().create(
					Compound.class, value);
			// ((Compound) node).setCommonName(value);
			reaction.addRight(node);
		}
	}

	public AbstractNode getNode() {
		return reaction;
	}

	public void start(String uniqueId) throws Exception {
		reaction = (Reaction) SinkFactory.getInstance().create(Reaction.class,
				uniqueId);
	}
}
