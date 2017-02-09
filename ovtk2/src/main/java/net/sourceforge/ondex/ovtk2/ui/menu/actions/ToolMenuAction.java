package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.Component;
import java.awt.Dimension;
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

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.core.util.GraphAdaptor;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.ui.console.LoadGraphAdaptor;
import net.sourceforge.ondex.ovtk2.ui.console.OVTKScriptingInitialiser;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConceptClassColor;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConceptClassShape;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogDataSourceColor;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogRelationTypeColor;
import net.sourceforge.ondex.ovtk2.ui.menu.JInternalFrameSelector;
import net.sourceforge.ondex.ovtk2.ui.popup.custom.PopupItemEditPanel;
import net.sourceforge.ondex.ovtk2.ui.popup.custom.itemeditor.ItemEditor;
import net.sourceforge.ondex.ovtk2.ui.stats.StatsFrame;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.LauncherFrame;
import net.sourceforge.ondex.ovtk2.util.PluginUtils;
import net.sourceforge.ondex.scripting.OutputPrinter;

/**
 * Listens to action events specific to the tool menu.
 * 
 * @author taubertj
 */
public class ToolMenuAction implements ActionListener, InternalFrameListener {

	// map of statistics to viewer windows
	private static Map<OVTK2Viewer, List<StatsFrame>> stats = new HashMap<OVTK2Viewer, List<StatsFrame>>();

	private JInternalFrame console = null;

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
	 * Adds command line window to main frame.
	 * 
	 * @param desktop
	 *            OVTK2Desktop to host command line
	 */
	private void initCommandLine(OVTK2Desktop desktop) {

		if (console == null || console.isClosed()) {
			console = new RegisteredJInternalFrame(Config.language.getProperty("Menu.Tools.Console"), "Tools", Config.language.getProperty("Menu.Tools.Console"), true, true, true, true);
			console.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			console.setSize(600, 100);
			try {
				OutputPrinter c = OVTKScriptingInitialiser.getCommandLine();
				JScrollPane scrollingArea = new JScrollPane((Component) c);
				scrollingArea.setMinimumSize(new Dimension(0, 35));
				console.add(scrollingArea);
			} catch (RuntimeException e) {
				ErrorDialog.show(e);
			}
			desktop.display(console, Position.centered);
			console.setVisible(true);
		} else {
			console.setVisible(true);
			console.toFront();
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();
		OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();

		// for coping with plug-in Attribute data types
		try {
			Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
		} catch (FileNotFoundException e) {
			ErrorDialog.show(e);
		} catch (MalformedURLException e) {
			ErrorDialog.show(e);
		}

		// show launcher
		if (cmd.equals("launcher")) {
			Thread thread = new Thread() {
				public void run() {
					try {
						PluginUtils.initPluginRegistry();
						Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass("net.sourceforge.ondex.workflow2.gui.WorkflowTool");
						Constructor<?> ct = cls.getConstructor(GraphAdaptor.class);
						JFrame workflowTool = (JFrame) ct.newInstance(new LoadGraphAdaptor());
						new LauncherFrame(workflowTool, JInternalFrameSelector.NONE, "Ondex Integrator");
						workflowTool.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
						ErrorDialog.show(true, new PluginUtils.MissingPluginException("Launcher plugin is not installed or could not be initialized", e), Thread.currentThread());
					} catch (ExceptionInInitializerError e) {
						ErrorDialog.show(true, new PluginUtils.MissingPluginException("Launcher plugin is not installed "), Thread.currentThread());
					}
				}
			};
			try {
				// for coping with plug-in Attribute data types
				try {
					thread.setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
				} catch (FileNotFoundException e) {
					ErrorDialog.show(e);
				}
				thread.start();
			} catch (Exception e) {
				ErrorDialog.show(true, new PluginUtils.MissingPluginException("Launcher plugin is not installed or could not be initialized", e), Thread.currentThread());
			}
		}

		// show stats window
		else if (cmd.equals("stats")) {
			if (viewer != null) {
				StatsFrame frame = new StatsFrame(viewer);

				// handle closing of all stats windows
				frame.addInternalFrameListener(this);
				if (!stats.containsKey(viewer))
					stats.put(viewer, new ArrayList<StatsFrame>());
				stats.get(viewer).add(frame);
				desktop.display(frame, Position.centered);
			}
		}

		// settings for mapping rtset to color
		if (cmd.equals("rtcolor")) {
			DialogRelationTypeColor dialog = new DialogRelationTypeColor();
			desktop.display(dialog, Position.centered);
		}

		// settings for mapping cv to color
		else if (cmd.equals("cvcolor")) {
			DialogDataSourceColor dialog = new DialogDataSourceColor();
			desktop.display(dialog, Position.centered);
		}

		// settings for mapping cc to color
		else if (cmd.equals("cccolor")) {
			DialogConceptClassColor dialog = new DialogConceptClassColor();
			desktop.display(dialog, Position.centered);
		}

		// settings for mapping cc to shape
		else if (cmd.equals("ccshape")) {
			DialogConceptClassShape dialog = new DialogConceptClassShape();
			desktop.display(dialog, Position.centered);
		}

		// init command line
		else if (cmd.equals("console")) {
			initCommandLine(desktop);
		}

		// show editor for user defined popups
		else if (cmd.equals("popupeditor")) {
			PopupItemEditPanel editPanel = new PopupItemEditPanel();
			ItemEditor itemEditor = new ItemEditor("Popup Editor", editPanel, editPanel);
			itemEditor.setVisible(true);
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
		if (e.getInternalFrame() instanceof StatsFrame) {
			// remove statistics from list associated to viewer
			Iterator<OVTK2Viewer> it = stats.keySet().iterator();
			while (it.hasNext()) {
				// get statistics per viewer
				Iterator<StatsFrame> it2 = stats.get(it.next()).iterator();
				while (it2.hasNext()) {
					// check if statistics was closed
					if (it2.next().equals(e.getInternalFrame()))
						it2.remove();
				}
			}
		} else if (e.getInternalFrame() instanceof OVTK2Viewer) {

			// make all statistics frames disappear
			if (stats.containsKey(e.getInternalFrame())) {
				for (StatsFrame frame : stats.get(e.getInternalFrame()).toArray(new StatsFrame[0])) {
					close(frame);
				}
				stats.remove(e.getInternalFrame());
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
