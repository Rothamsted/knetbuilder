package net.sourceforge.ondex.ovtk2.util;

import java.awt.Desktop;
import java.net.URL;

import javax.help.JHelpContentViewer;
import javax.help.plaf.basic.BasicContentViewerUI;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkEvent;

/**
 * A native UI for JHelpContentViewer using links to the native browser.
 * 
 * @author taubertj
 * @version 01/10/2012
 */
public class OVTKBasicNativeContentViewerUI extends BasicContentViewerUI {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 1863440993578899547L;

	/**
	 * Default constructor
	 * 
	 * @param arg0
	 */
	public OVTKBasicNativeContentViewerUI(JHelpContentViewer arg0) {
		super(arg0);
	}

	public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
		return new OVTKBasicNativeContentViewerUI((JHelpContentViewer) x);
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent he) {
		if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				URL u = he.getURL();
				if (u.getProtocol().equalsIgnoreCase("mailto") || u.getProtocol().equalsIgnoreCase("http") || u.getProtocol().equalsIgnoreCase("ftp")) {
					Desktop.getDesktop().browse(u.toURI());
					return;
				}
			} catch (Throwable t) {
			}
		}
		super.hyperlinkUpdate(he);
	}
}
