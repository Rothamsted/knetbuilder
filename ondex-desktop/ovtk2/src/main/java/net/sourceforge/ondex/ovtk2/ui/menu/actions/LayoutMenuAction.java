package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.layout.OVTK2Layouter;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2LayoutOptions;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.ui.menu.CustomMouseListener;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2GraphMouse;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.ovtk2.util.VisualisationUtils;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

/**
 * Listens to action events specific to the layout menu.
 * 
 * @author taubertj
 * 
 */
public class LayoutMenuAction implements ActionListener, InternalFrameListener {

	// layout options of active graph
	private static OVTK2LayoutOptions options = null;

	/**
	 * Closes a internal frame if it is not already null.
	 * 
	 * @param frame
	 *            frame to close
	 */
	private static void close(RegisteredJInternalFrame frame) {
		if (frame != null) {
			try {
				frame.setClosed(true);
			} catch (PropertyVetoException pve) {
				ErrorDialog.show(pve);
			}
		}
	}

	/**
	 * Returns whether or not the options is currently visible.
	 * 
	 * @return is visible?
	 */
	public static boolean isOptionsShown() {
		return options != null;
	}

	/**
	 * Shows the options frame.
	 * 
	 * @param viewer
	 *            what viewer to show options for
	 */
	private static void showOptions(OVTK2Viewer viewer) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// check if there is already a options
		if (options == null) {
			options = new OVTK2LayoutOptions(viewer);
			options.addInternalFrameListener(desktop);
			desktop.display(options, Position.right);
		} else {
			options.setViewer(viewer);
			options.toFront();
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
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

		// handle Layouter calls
		if (cmd.startsWith("Menu.Layout.")) {
			if (viewer != null && !CustomMouseListener.pressed) {
				String className = Config.config.getProperty(cmd);

				// get new instance of layout
				int index = className.lastIndexOf(".");
				String name = className.substring(index + 1, className.length());

				try {
					System.err.println(viewer.getVisualizationViewer().getGraphLayout());
					if (viewer.getVisualizationViewer().getGraphLayout() instanceof OVTK2Layouter) {
						((OVTK2Layouter) viewer.getVisualizationViewer().getGraphLayout()).cleanUp();
					}

					final OVTK2Layouter layouter_new = OVTK2PluginLoader.getInstance().loadLayouter(name, viewer);

					if (layouter_new instanceof Monitorable) {
						// layout knows about its progress
						Monitorable p = (Monitorable) layouter_new;
						OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Running Layout...", p);
						Thread t = new Thread() {
							public void run() {
								VisualisationUtils.runLayout(layouter_new, viewer);
								if (options != null) {
									options.setLayouter(layouter_new);
								}
							}
						};
						// for coping with plug-in Attribute data types
						try {
							t.setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
						} catch (FileNotFoundException e) {
							ErrorDialog.show(e);
						}
						t.start();
					} else {
						// wrap into indefinite process
						IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
							public void task() {
								// for coping with plug-in Attribute data types
								try {
									Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
								} catch (FileNotFoundException e) {
									ErrorDialog.show(e);
								} catch (MalformedURLException e) {
									ErrorDialog.show(e);
								}

								VisualisationUtils.runLayout(layouter_new, viewer);
								if (options != null) {
									options.setLayouter(layouter_new);
								}
							}
						};

						// set layout
						OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Running Layout...", p);
						p.start();
					}

					// central handling of scaling
					OVTK2GraphMouse mouse = (OVTK2GraphMouse) viewer.getVisualizationViewer().getGraphMouse();
					if (name.equals("StaticLayout")) {
						// static layout should stay static
						mouse.setViewScaling(true);
					} else {
						// reset scaling control
						mouse.setViewScaling(false);
					}
				} catch (Exception e) {
					ErrorDialog.show(e);
				}
			}

		}

		// toggle options view
		else if (cmd.equals("options")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				if (selected) {
					showOptions(viewer);
				} else {
					close(options);
				}
			}
		}
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		if (e.getInternalFrame() instanceof OVTK2Viewer) {
			OVTK2Viewer viewer = (OVTK2Viewer) e.getInternalFrame();
			if (options != null && !options.getViewer().equals(viewer)) {
				options.setViewer(viewer);
			}
		}
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		if (e.getInternalFrame() instanceof OVTK2LayoutOptions) {
			desktop.locations.remove(e.getInternalFrame());
			options = null;
		} else if (e.getInternalFrame() instanceof OVTK2Viewer) {
			int count = 0;
			for (JInternalFrame jif : desktop.getDesktopPane().getAllFrames()) {
				if (jif instanceof OVTK2Viewer)
					count++;
			}
			// last OVTK2Viewer is closing
			if (count == 1) {
				close(options);
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
