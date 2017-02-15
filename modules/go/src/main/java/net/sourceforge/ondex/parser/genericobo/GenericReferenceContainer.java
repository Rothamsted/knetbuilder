package net.sourceforge.ondex.parser.genericobo;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * 
 * @author hoekmanb
 *
 */
public class GenericReferenceContainer extends ReferenceContainer {

	public GenericReferenceContainer(ONDEXGraph graph) {
		super(graph);
	}

	@Override
	public boolean writeXRef() {
		return false;
	}

	@Override
	public void analyseXRef() {
		// Do nothing?
	}
}
