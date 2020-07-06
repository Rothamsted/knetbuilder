package net.sourceforge.ondex.ovtk2.ui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Catches hyperlink events and sorts them into browser and OVTK2 specific ones.
 * OVTK2 ones get redirected as actions to OVTK2Desktop.
 * 
 * @author taubertj
 * @version 19.05.2008
 */
public class OVTK2HyperlinkListener implements HyperlinkListener {

	/**
	 * Private instance to referrer action events to
	 */
	private OVTK2Desktop desktop = null;

	/**
	 * Sets the OVTK2Desktop to referrer action events to.
	 * 
	 * @param desktop
	 *            OVTK2Desktop to referrer to
	 */
	public OVTK2HyperlinkListener(OVTK2Desktop desktop) {
		this.desktop = desktop;
	}

	@Override
	public void hyperlinkUpdate(final HyperlinkEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (e.getDescription().startsWith("ovtk2://")) {
						// create event for OVTK2Desktop
						ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, e.getDescription().substring(8));
						desktop.actionPerformed(ae);
					} else if (e.getDescription().equals("close")) {
						// do nothing
					} else {
						Desktop desktop = null;
						// Before more Desktop API is used, first check
						// whether the API is supported by this particular
						// virtual machine (VM) on this particular host.
						if (Desktop.isDesktopSupported()) {
							desktop = Desktop.getDesktop();

							// open href in browser
							try {
								desktop.browse(new URI(e.getDescription()));
							} catch (IOException ioe) {
								ioe.printStackTrace();
							} catch (URISyntaxException use) {
								use.printStackTrace();
							}
						} else {
							JOptionPane.showInputDialog(desktop, "Hyperlinks not supported by OS.");
						}
					}
				}
			}
		});
	}
}
