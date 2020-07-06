package net.sourceforge.ondex.ovtk2.ui.console;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.GraphAdaptor;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils;

/**
 * Part of transient reference - do not modify
 * 
 * @author lysenkoa
 * 
 */
public class LoadGraphAdaptor implements GraphAdaptor {

	@Override
	public void passGraph(ONDEXGraph graph) {
		DesktopUtils.displayGraphOnDesktop(graph);
	}
}
