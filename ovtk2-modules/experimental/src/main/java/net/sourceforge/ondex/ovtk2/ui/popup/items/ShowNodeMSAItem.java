package net.sourceforge.ondex.ovtk2.ui.popup.items;

import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.io.AppletFormatAdapter;
import jalview.io.FastaFile;
import jalview.jbgui.GCutAndPasteTransfer;
import jalview.jbgui.GPCAPanel;
import jalview.jbgui.GStructureViewer;
import jalview.jbgui.GTreePanel;
import jalview.ws.jws1.Discoverer;

import java.awt.Component;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.io.IOException;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected node and all of the same DataSource
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ShowNodeMSAItem extends EntityMenuItem<ONDEXConcept> implements
		ContainerListener, InternalFrameListener {

	// align frame
	private AlignFrame al;

	@Override
	public boolean accepts() {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		// ConceptClass ccProtein =
		// graph.getMetaData().getConceptClass("Protein");
		AttributeName an = graph.getMetaData().getAttributeName("AA");
		if (an != null) {
			int i = 0;
			for (ONDEXConcept c : entities) {
				if (c.getAttribute(an) != null)
					i++;
				// at least two proteins with AA
				if (i > 1)
					return true;
			}
		}
		return false;
	}

	@Override
	protected void doAction() {
		AttributeName an = viewer.getONDEXJUNGGraph().getMetaData()
				.getAttributeName("AA");
		if (an != null) {

			StringBuffer allSeqs = new StringBuffer();
			for (ONDEXConcept c : entities) {
				Attribute attribute = c.getAttribute(an);
				if (attribute != null) {
					// FASTA like format
					allSeqs.append("> " + c.getId() + "|" + getName(c) + "\n");
					allSeqs.append(attribute.getValue().toString());
					allSeqs.append("\n");
				}
			}

			try {
				// prevents some errors with Desktop not present
				// System.setProperty("java.awt.headless", "true");

				// Log4j logging
				Cache.initLogger();

				// track new windows opening
				new Desktop();
				Desktop.desktop.addContainerListener(this);

				// Initialise web services
				Discoverer.doDiscovery();

				// input FASTA sequences
				FastaFile fasta = new FastaFile(allSeqs.toString(),
						AppletFormatAdapter.PASTE);

				// not really aligned yet
				Alignment align = new Alignment(fasta.getSeqsAsArray());

				// display frame
				al = new AlignFrame(align, AlignFrame.DEFAULT_WIDTH,
						AlignFrame.DEFAULT_HEIGHT);
				al.sortIDMenuItem_actionPerformed(null);

				// web services for alignment
				al.BuildWebServiceMenu();

				// set JalView version
				Cache.applicationProperties.setProperty("VERSION", "2.6");
				Cache.applicationProperties
						.setProperty("LATEST_VERSION", "2.6");

				// add to desktop
				RegisteredJInternalFrame registeredFrame = new RegisteredJInternalFrame(
						"JalView", "Multiple Sequence Alignment", "JalView",
						true, true, true, true);
				registeredFrame.setContentPane(al.getContentPane());
				registeredFrame.setJMenuBar(al.getJMenuBar());
				registeredFrame.setSize(al.getSize());
				registeredFrame.addInternalFrameListener(this);

				// add possibly interested listeners
				for (ComponentListener l : al.getComponentListeners())
					registeredFrame.addComponentListener(l);
				for (InternalFrameListener l : al.getInternalFrameListeners())
					registeredFrame.addInternalFrameListener(l);
				for (KeyListener l : al.getKeyListeners())
					registeredFrame.addKeyListener(l);

				OVTK2Desktop.getInstance().display(registeredFrame,
						Position.centered);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getName(ONDEXConcept ac) {
		String label = "";

		// get first preferred name as label
		for (ConceptName cn : ac.getConceptNames()) {
			if (cn.isPreferred()) {
				label = cn.getName();
				break;
			}
		}
		// next try annotation
		if (label == null || label.trim().length() == 0) {
			label = ac.getAnnotation();
		}
		// next try description
		if (label == null || label.trim().length() == 0) {
			label = ac.getDescription();
		}
		// next try pid
		if (label == null || label.trim().length() == 0) {
			label = ac.getPID();
		}
		// last resort to concept id
		if (label == null || label.trim().length() == 0) {
			label = String.valueOf(ac.getId());
		}

		return label;
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.LINK;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ShowNodeMSA";
	}

	@Override
	protected String getUndoPropertyName() {
		return "";
	}

	@Override
	public void componentAdded(ContainerEvent e) {
		OVTK2Desktop.getInstance().getDesktopPane().add(e.getChild());
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		OVTK2Desktop.getInstance().getDesktopPane().remove(e.getChild());
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		for (Component c : OVTK2Desktop.getInstance().getDesktopPane()
				.getComponents()) {
			if (c instanceof GCutAndPasteTransfer) {
				try {
					((GCutAndPasteTransfer) c).setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
			}

			else if (c instanceof GPCAPanel) {
				try {
					((GPCAPanel) c).setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
			}

			else if (c instanceof GStructureViewer) {
				try {
					((GStructureViewer) c).setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
			}

			else if (c instanceof GTreePanel) {
				try {
					((GTreePanel) c).setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
	}
}
