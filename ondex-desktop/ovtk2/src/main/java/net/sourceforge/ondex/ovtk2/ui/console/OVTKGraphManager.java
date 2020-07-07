package net.sourceforge.ondex.ovtk2.ui.console;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDesktopPane;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.ResourceAccess;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;

/**
 * Controls graph synching and handles clean-up after the execution of the
 * commands
 * 
 * @author lysenkoa
 * 
 */
public class OVTKGraphManager implements ResourceAccess {
	private Map<ONDEXGraph, List<OVTK2PropertiesAggregator>> graphsToViewer = new HashMap<ONDEXGraph, List<OVTK2PropertiesAggregator>>();

	private Map<String, Object> graphsByName = new HashMap<String, Object>();

	public OVTKGraphManager(JDesktopPane desktopPane) {
		for (Component c : desktopPane.getComponents()) {
			if (c instanceof OVTK2PropertiesAggregator) {
				OVTK2PropertiesAggregator v = (OVTK2PropertiesAggregator) c;
				ONDEXGraph g = v.getONDEXJUNGGraph();
				graphsByName.put(g.getName(), g);
				List<OVTK2PropertiesAggregator> viewers = graphsToViewer.get(g);
				if (viewers == null) {
					viewers = new ArrayList<OVTK2PropertiesAggregator>();
					graphsToViewer.put(g, viewers);
				}
				viewers.add(v);
			}
		}
	}

	public Map<String, Object> getResources() {
		return graphsByName;
	}

	public void detachResource(String resourceName) {
		for (OVTK2PropertiesAggregator ov : graphsToViewer.get(graphsByName.get(resourceName))) {
			if (ov instanceof OVTK2Viewer)
				((OVTK2Viewer) ov).dispose();
		}
	}
}
