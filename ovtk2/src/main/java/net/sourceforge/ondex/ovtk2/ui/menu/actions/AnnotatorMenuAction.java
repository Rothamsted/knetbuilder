package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2AnnotatorInternalFrameListener;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.config.PluginID;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.ui.menu.CustomMouseListener;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;

/**
 * Listens to action events specific to the annotator menu.
 * 
 * @author taubertj
 */
public class AnnotatorMenuAction implements ActionListener, InternalFrameListener {

	// map of annotator to viewer windows
	private static Map<OVTK2Viewer, List<JInternalFrame>> annotators = new HashMap<OVTK2Viewer, List<JInternalFrame>>();

	/**
	 * Closes a internal frame if it is not already null.
	 * 
	 * @param frame
	 *            frame to close
	 */
	private static void close(JInternalFrame frame) {
		if (frame != null) {
			try {
				frame.setClosed(true);
			} catch (PropertyVetoException pve) {
				ErrorDialog.show(pve);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		final OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();
		final OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();

		// for coping with plug-in Attribute data types
		try {
			Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
		} catch (FileNotFoundException e) {
			ErrorDialog.show(e);
		} catch (MalformedURLException e) {
			ErrorDialog.show(e);
		}

		// special placement for analysis tools, e.g. poplar
		if (cmd.startsWith("Menu.Analysis.")) {
			String className = Config.config.getProperty(cmd);
			Constructor<?> constr;
			try {
				constr = Class.forName(className).getConstructor(new Class<?>[] { OVTK2Viewer.class });

				JInternalFrame frame = (JInternalFrame) constr.newInstance(viewer);
				desktop.display(frame, Position.rightBottom);
			} catch (Exception e) {
				ErrorDialog.show(e);
			}
		}

		// handle annotator calls
		else if (cmd.startsWith("Menu.Annotator.")) {
			if (viewer != null && !CustomMouseListener.pressed) {
				final String className = Config.config.getProperty(cmd);

				// wrap into indefinite process
				IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
					public void task() {
						try {
							// for coping with plug-in Attribute data types
							try {
								Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
							} catch (FileNotFoundException e) {
								ErrorDialog.show(e);
							}

							// get new instance of annotator
							int index = className.lastIndexOf(".");
							String pack = className.substring(0, index);
							pack = pack.substring(pack.lastIndexOf(".") + 1, pack.length());
							PluginID plid = new PluginID(pack, className.substring(index + 1, className.length()));
							OVTK2Annotator annotator_new = OVTK2PluginLoader.getInstance().loadAnnotator(plid, viewer);

							// add as frame to desktop
							JInternalFrame annotatorFrame = new RegisteredJInternalFrame(annotator_new.getName(), "Annotator", annotator_new.getName() + " - " + viewer.getTitle(), true, true, false, true);
							annotatorFrame.addInternalFrameListener(new OVTK2AnnotatorInternalFrameListener(annotator_new));
							annotatorFrame.setContentPane(annotator_new);
							annotatorFrame.setVisible(true);
							annotatorFrame.pack();

							// handle closing of all annotator windows
							annotatorFrame.addInternalFrameListener(new AnnotatorMenuAction());
							if (!annotators.containsKey(viewer))
								annotators.put(viewer, new ArrayList<JInternalFrame>());
							annotators.get(viewer).add(annotatorFrame);

							desktop.display(annotatorFrame, Position.leftTop);
						} catch (Exception e) {
							ErrorDialog.show(e);
						}
					}
				};
				p.start();
				OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Working...", p);
			}
		}
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		if (e.getInternalFrame().getContentPane() instanceof OVTK2Annotator) {
			// remove annotator from list associated to viewer
			Iterator<OVTK2Viewer> it = annotators.keySet().iterator();
			while (it.hasNext()) {
				// get annotators per viewer
				Iterator<JInternalFrame> it2 = annotators.get(it.next()).iterator();
				while (it2.hasNext()) {
					// check if annotator was closed
					if (it2.next().equals(e.getInternalFrame()))
						it2.remove();
				}
			}
		} else if (e.getInternalFrame() instanceof OVTK2Viewer) {
			// make all annotator frames disappear
			if (annotators.containsKey(e.getInternalFrame())) {
				for (JInternalFrame frame : annotators.get(e.getInternalFrame()).toArray(new JInternalFrame[annotators.size()])) {
					close(frame);
				}
				annotators.remove(e.getInternalFrame());
			}
		}
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {

	}
}
