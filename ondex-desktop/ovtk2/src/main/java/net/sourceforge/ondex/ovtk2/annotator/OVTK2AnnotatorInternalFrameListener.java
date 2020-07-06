package net.sourceforge.ondex.ovtk2.annotator;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.ovtk2.annotator.colorcategory.ColorCategoryAnnotator;
import net.sourceforge.ondex.ovtk2.annotator.scaleconcept.ScaleConceptAnnotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.AppearanceMenuAction;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

/**
 * provides clean-up method for annotator displayed in an internal frame
 * 
 * @author hindlem
 * 
 */
public class OVTK2AnnotatorInternalFrameListener implements InternalFrameListener {

	private OVTK2Annotator annotator;

	/**
	 * construct
	 * 
	 * @param annotator
	 *            the OVTK2Annotator associated with this frame
	 */
	public OVTK2AnnotatorInternalFrameListener(OVTK2Annotator annotator) {
		this.annotator = annotator;
	}

	public void internalFrameActivated(InternalFrameEvent e) {
	}

	public void internalFrameClosed(InternalFrameEvent e) {
	}

	public void internalFrameClosing(InternalFrameEvent e) {
		// if the graph frame still exists
		if (annotator.viewer != null && annotator.viewer.isVisible()) {

			OVTK2PropertiesAggregator viewer = annotator.viewer;

			// hack to close possible histogram frame
			if (annotator instanceof ScaleConceptAnnotator) {
				ScaleConceptAnnotator scale = (ScaleConceptAnnotator) annotator;
				if (scale.histogram != null) {
					try {
						scale.histogram.setClosed(true);
					} catch (PropertyVetoException e1) {
						ErrorDialog.show(e1);
					}
				}
			}

			// hack to close possible legend frame
			if (annotator instanceof ColorCategoryAnnotator) {
				ColorCategoryAnnotator cat = (ColorCategoryAnnotator) annotator;
				if (cat.legend != null) {
					try {
						cat.legend.setClosed(true);
					} catch (PropertyVetoException e1) {
						ErrorDialog.show(e1);
					}
				}
			}

			// if annotator hasnt been used, dont ask
			if (!annotator.hasBeenUsed())
				return;

			// dont ask when Close All
			if (viewer.isDestroy())
				return;

			Object[] options = { Config.language.getProperty("Annotator.Save.Changes.Keep"), Config.language.getProperty("Annotator.Save.Changes.Discard") };
			int option = JOptionPane.showOptionDialog(OVTK2Desktop.getInstance().getDesktopPane(), Config.language.getProperty("Annotator.Save.Changes.Text"), Config.language.getProperty("Annotator.Save.Changes.Title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

			switch (option) {
			case JOptionPane.YES_OPTION:
				// do nothing
				break;
			case JOptionPane.NO_OPTION:
				// reset to default values
				JCheckBoxMenuItem item = new JCheckBoxMenuItem();
				item.setSelected(false);
				OVTK2Desktop desktop = OVTK2Desktop.getInstance();
				desktop.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, AppearanceMenuAction.NODECOLOR));
				desktop.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, AppearanceMenuAction.EDGECOLOR));
				desktop.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, AppearanceMenuAction.NODESHAPE));
				desktop.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, AppearanceMenuAction.EDGESIZE));

				// reset icon transformer, might have been set by annotator
				viewer.getVisualizationViewer().getRenderContext().setVertexIconTransformer(null);

				// cleanup in title
				String name = viewer.getTitle();
				name = name.replaceAll(" \\(.+\\)$", "");
				viewer.setTitle(name);
				if (viewer instanceof OVTK2Viewer)
					((OVTK2Viewer) viewer).updateUI();
				break;
			}
		}
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
	}

	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	public void internalFrameIconified(InternalFrameEvent e) {
	}

	public void internalFrameOpened(InternalFrameEvent e) {
	}

}
