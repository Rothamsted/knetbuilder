package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.config.PluginID;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.filter.OVTK2FilterInternalFrameListener;
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
 * Listens to action events specific to the filter menu.
 * 
 * @author taubertj
 * 
 */
public class FilterMenuAction implements ActionListener, InternalFrameListener {

	// map of filter to viewer windows
	private static Map<OVTK2Viewer, List<JInternalFrame>> filters = new HashMap<OVTK2Viewer, List<JInternalFrame>>();

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

		// handle filter calls
		if (cmd.startsWith("Menu.Filter.")) {
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

							// get new instance of filter
							int index = className.lastIndexOf(".");
							String pack = className.substring(0, index);
							pack = pack.substring(pack.lastIndexOf(".") + 1, pack.length());
							PluginID plid = new PluginID(pack, className.substring(index + 1, className.length()));
							OVTK2Filter filter_new = OVTK2PluginLoader.getInstance().loadFilter(plid, viewer);

							// add as frame to desktop
							JInternalFrame filterFrame = new RegisteredJInternalFrame(filter_new.getName(), "Filter", filter_new.getName() + " - " + viewer.getTitle(), true, true, true, true);
							filterFrame.addInternalFrameListener(new OVTK2FilterInternalFrameListener(filter_new));
							filterFrame.setContentPane(filter_new);
							filterFrame.setVisible(true);
							filterFrame.pack();

							// handle closing of all filter windows
							filterFrame.addInternalFrameListener(new FilterMenuAction());
							if (!filters.containsKey(viewer))
								filters.put(viewer, new ArrayList<JInternalFrame>());
							filters.get(viewer).add(filterFrame);

							desktop.display(filterFrame, Position.leftTop);
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
		if (e.getInternalFrame().getContentPane() instanceof OVTK2Filter) {
			// remove filter from list associated to viewer
			Iterator<OVTK2Viewer> it = filters.keySet().iterator();
			while (it.hasNext()) {
				// get filter per viewer
				Iterator<JInternalFrame> it2 = filters.get(it.next()).iterator();
				while (it2.hasNext()) {
					// check if filter was closed
					if (it2.next().equals(e.getInternalFrame()))
						it2.remove();
				}
			}
		} else if (e.getInternalFrame() instanceof OVTK2Viewer) {
			// make all filter frames disappear
			if (filters.containsKey(e.getInternalFrame())) {
				for (JInternalFrame frame : filters.get(e.getInternalFrame()).toArray(new JInternalFrame[0])) {
					close(frame);
				}
				filters.remove(e.getInternalFrame());
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
