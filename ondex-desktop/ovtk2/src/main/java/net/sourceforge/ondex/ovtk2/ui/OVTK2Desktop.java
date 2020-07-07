package net.sourceforge.ondex.ovtk2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.EventListenerList;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.io.OXLExport;
import net.sourceforge.ondex.ovtk2.ui.dialog.WelcomeDialog;
import net.sourceforge.ondex.ovtk2.ui.menu.OVTK2Menu;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.ViewMenuAction;
import net.sourceforge.ondex.ovtk2.ui.toolbars.OVTK2ToolBar;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.ovtk2.util.OVTKSplashScreen;
import net.sourceforge.ondex.ovtk2.util.SwingExceptionHandler;
import net.sourceforge.ondex.tools.threading.monitoring.FailableThread;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import net.sourceforge.ondex.tools.threading.monitoring.SimpleMonitor;

/**
 * The main entry point for the user interface.
 * 
 * @author taubertj
 * 
 */
public class OVTK2Desktop implements ActionListener, InternalFrameListener, VetoableChangeListener, DropTargetListener {

	/**
	 * 
	 * Class of desktop resources
	 * 
	 */
	private class DesktopResources implements OVTK2ResourceAssesor {
		@Override
		public OVTK2PropertiesAggregator[] getAllViewers() {
			return viewers.toArray(new OVTK2Viewer[viewers.size()]);
		}

		@Override
		public ONDEXLogger getLogger() {
			return logger;
		}

		@Override
		public JDesktopPane getParentPane() {
			return desktop;
		}

		@Override
		public OVTK2MetaGraph getSelectedMetagraph() {
			return ViewMenuAction.getMetaGraph();
		}

		@Override
		public OVTK2PropertiesAggregator getSelectedViewer() {
			return activeViewer;
		}

		@Override
		public OVTK2ToolBar getToolBar() {
			return toolBar;
		}

		@Override
		public void setSelectedViewer(OVTK2PropertiesAggregator viewer) {
			if (viewer instanceof OVTK2Viewer)
				activeViewer = (OVTK2Viewer) viewer;
		}
	}

	private class OVTKInitializer extends FailableThread {

		private OVTK2Desktop desktop;

		private SimpleMonitor monitor;

		public OVTKInitializer(OVTK2Desktop d) {
			desktop = d;
			monitor = new SimpleMonitor("Starting OVTK2", 10);
			// catch all UncaughtExceptions from SWING
			new SwingExceptionHandler();
		}

		@Override
		public void failableRun() throws Throwable {
			try {
				// ErrorHandler.enforceErrorHandling();
				monitor.next("initializing config...");
				Config.loadConfig(false);

				monitor.next("initializing look&feel...");
				initLaF();

				monitor.next("initializing user interface...");
				initUI();

				monitor.next("initializing proxy settings...");
				Config.initProxySetting();

				monitor.next("initializing menu bar...");

				// new menubar
				menuBar = new OVTK2Menu(desktop);
				frame.setJMenuBar(menuBar);

				monitor.next("initializing desktop...");
				initDesktop();

				monitor.next("initializing tool bar...");

				// show toolbar
				toolBar = new OVTK2ToolBar(desktop);
				frame.add(toolBar, BorderLayout.PAGE_START);
				toolBar.setVisible(true);

				monitor.next("showing desktop...");

				// make frame visible and set it to the front
				frame.setVisible(true);

				monitor.next("initializing welcome screen...");

				// check if welcome should be shown
				if (Boolean.parseBoolean(Config.config.getProperty("Welcome.Show")))
					WelcomeDialog.getInstance(desktop);

				monitor.next("cleaning up...");

				frame.toFront();

				monitor.complete();

				finishedInit = true;
			} catch (Throwable t) {
				monitor.failed(t);
				throw t;
			}
		}

		public Monitorable getMonitor() {
			return monitor;
		}

	}

	/**
	 * enumeration for JInternalFrame display parameter
	 * 
	 * @author peschr
	 * 
	 */
	public enum Position {
		centered, centeredIconified, leftBottom, leftTop, right, rightBottom
	}

	// used for the singleton pattern
	private static OVTK2Desktop instance = null;

	// status check if all load correctly
	private static boolean finishedInit = false;

	/**
	 * Implements the singleton pattern to ensure that there is just one
	 * OVTK2Desktop instance
	 * 
	 * @return every time the same OVTK2Desktop instance
	 */
	public synchronized static OVTK2Desktop getInstance() {
		if (instance == null) {
			new OVTK2Desktop();
		}
		return instance;
	}

	/**
	 * Returns true if initialisation of OVTK has finished.
	 * 
	 * @return finished initialisation
	 */
	public static boolean hasFinishedInit() {
		return finishedInit;
	}

	// registers, which viewer is currently active
	private OVTK2Viewer activeViewer = null;

	// contains all internal frames
	private JDesktopPane desktop = null;

	// store registered listeners
	private EventListenerList events = new EventListenerList();

	// main ui window
	private JFrame frame = null;

	// contains locations of gadgets
	public Hashtable<JInternalFrame, Point2D> locations = new Hashtable<JInternalFrame, Point2D>();

	// core logger for graph events
	private ONDEXLogger logger = new ONDEXLogger();

	// dynamic changing enabled menus
	private OVTK2Menu menuBar = null;

	// used as ItemListener for OVTK2Viewer with Modal
	private OVTK2ToolBar toolBar = null;

	// contains list of all viewers
	public List<OVTK2Viewer> viewers = new ArrayList<OVTK2Viewer>();

	/**
	 * Private Constructor to initialise UI use the static getInstance method in
	 * order to initialise.
	 * 
	 */
	private OVTK2Desktop() {
		instance = this;
		resourceAssesor = new DesktopResources();
		Throwable caught = null;
		OVTKInitializer initializer = null;
		try {
			initializer = new OVTKInitializer(this);

			OVTKSplashScreen.start(initializer.getMonitor());
			initializer.start();
			initializer.join();

		} catch (Throwable t) {
			caught = t;
		}

		if (initializer == null) {
			ErrorDialog.show(false, new Exception("Initialization unsuccessful!", caught), Thread.currentThread());
		} else if (initializer.getCaught() != null) {
			ErrorDialog.show(false, initializer.getCaught(), initializer);
			throw new RuntimeException(initializer.getCaught());
		} else if (caught != null) {
			ErrorDialog.show(false, caught, Thread.currentThread());
			throw new RuntimeException(caught);
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// System.err.println(arg0.getActionCommand());
		// System.err.println(arg0.getSource());
		// System.err.println(arg0.paramString());
		// System.err.println(arg0.getID());

		// propagate event to all interested listeners
		Object[] listeners = events.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				((ActionListener) listeners[i + 1]).actionPerformed(arg0);
				// System.err.println(listeners[i + 1].getClass().toString());
			}
		}

		// do not forget to update check-boxes in menu bar
		menuBar.updateMenuBar(activeViewer);
	}

	public void addActionListener(ActionListener listener) {
		events.add(ActionListener.class, listener);
	}

	public void addInternalFrameListener(InternalFrameListener listener) {
		events.add(InternalFrameListener.class, listener);
	}

	/**
	 * adds a JInternalFrame to the desktop
	 * 
	 * @param internal
	 *            the JInternalFrame
	 * @param position
	 *            the position
	 */
	public void display(JInternalFrame internal, Position position) {

		boolean iconified = position.equals(Position.centeredIconified);

		// displays in centre location
		if (position.equals(Position.centered) || position.equals(Position.centeredIconified)) {

			// do the location calculation
			Rectangle visible = desktop.getVisibleRect();
			Dimension size = internal.getSize();
			internal.setLocation((visible.width / 2) - (size.width / 2), (visible.height / 2) - (size.height / 2));

			if (size.height > visible.height)
				internal.setLocation(internal.getX(), 0);
		}

		// displays in bottom left
		else if (position.equals(Position.leftBottom)) {
			// do the location calculation
			Rectangle visible = desktop.getVisibleRect();
			Dimension size = internal.getSize();
			internal.setLocation(0, visible.height - size.height);
		}

		// displays in top left
		else if (position.equals(Position.leftTop)) {
			// do the location calculation
			internal.setLocation(0, 0);
		}

		// displays along the right side
		else if (position.equals(Position.right)) {
			boolean top_present = false;
			Point2D max = new Point2D.Double(0, 0);
			for (JInternalFrame frame : locations.keySet()) {
				if (locations.get(frame).getY() == 0)
					top_present = true;
				double y = locations.get(frame).getY() + frame.getHeight();
				max.setLocation(0, Math.max(max.getY(), y));
			}
			if (!top_present)
				max.setLocation(0, 0);

			// do the location calculation
			Rectangle visible = desktop.getVisibleRect();
			Dimension size = internal.getSize();
			internal.setLocation(visible.width - size.width, (int) max.getY());

			locations.put(internal, internal.getLocation());
		}

		// displays bottom right
		else if (position.equals(Position.rightBottom)) {
			// do the location calculation
			Rectangle visible = desktop.getVisibleRect();
			Dimension size = internal.getSize();
			internal.setLocation(visible.width - size.width, visible.height - size.height);
		}

		// add to desktop and show
		desktop.add(internal);

		// set icon status
		try {
			internal.setIcon(iconified);
		} catch (PropertyVetoException e) {
			ErrorDialog.show(e);
		}

		// set to front
		internal.setVisible(true);
		internal.toFront();
	}

	/**
	 * 
	 * @return the desktop pane
	 */
	public JDesktopPane getDesktopPane() {
		return desktop;
	}

	private static OVTK2ResourceAssesor resourceAssesor = null;

	/**
	 * Used by scripting internally
	 * 
	 * @return OVTK2ResourceAssesor
	 */
	public static OVTK2ResourceAssesor getDesktopResources() {
		return resourceAssesor;
	}

	/**
	 * Sets resources, used for applet
	 * 
	 * @return OVTK2ResourceAssesor
	 */
	public static void setDesktopResources(OVTK2ResourceAssesor ra) {
		resourceAssesor = ra;
	}

	/**
	 * Returns the JFrame this OVTK2Desktop is using.
	 * 
	 * @return JFrame used to display OVTK2Desktop
	 */
	public JFrame getMainFrame() {
		return frame;
	}

	/**
	 * Returns the OVTK2Menu in use.
	 * 
	 * @return OVTK2Menu
	 */
	public OVTK2MenuBar getOVTK2Menu() {
		return menuBar;
	}

	/**
	 * Setup desktop to contain all internal frames.
	 * 
	 */
	private void initDesktop() {

		// add new JDesktopPane to JFrame
		desktop = new JDesktopPane();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(desktop, BorderLayout.CENTER);
		// make dragging a little faster but perhaps uglier.
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

		// Make drop target
		final java.awt.dnd.DropTarget dt = new java.awt.dnd.DropTarget();
		try {
			dt.addDropTargetListener(this);
		} catch (java.util.TooManyListenersException e) {
			e.printStackTrace();
		}
		desktop.setDropTarget(dt);
	}

	/**
	 * init look and feel.
	 */
	private void initLaF() {
		try {

			// ask users on non windows OS
			if (Config.lookAndFeel == null && !Config.getOsName().equals("windows")) {
				int result = JOptionPane.showConfirmDialog(frame, "A non Microsoft Windows OS has been detected.\n" + "Would you like to use the JAVA cross-platform Look&Feel\n" + "to improve compatability of the user interface?", "Switch Look&Feel", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				// get user choice
				if (result == JOptionPane.YES_OPTION)
					Config.lookAndFeel = "java";
				else
					Config.lookAndFeel = "system";

				// save user choice
				Config.visual.setProperty("Default.LookAndFeel", Config.lookAndFeel);
				Config.saveVisual();
			}

			if (Config.lookAndFeel != null && Config.lookAndFeel.equals("java")) {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}

		} catch (InstantiationException ie) {
			ie.printStackTrace();
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} catch (UnsupportedLookAndFeelException lfe) {
			lfe.printStackTrace();
		}

		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
	}

	/**
	 * Constructs JFrame and displays it.
	 * 
	 */
	private void initUI() {

		frame = new JFrame(Config.config.getProperty("Program.Name") + " - Version " + Config.config.getProperty("Program.Version") + " (build " + DesktopUtils.extractBuildNumber() + ")");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// get screen size
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension dim = tk.getScreenSize();
		dim.height -= 50;
		frame.setSize(dim);

		String s = File.separator;
		String imgSrc = "config" + s + "themes" + s + "default" + s + "icons" + s + "ondex16" + ".png";
		Image icon = null;
		try {
			icon = Toolkit.getDefaultToolkit().getImage(imgSrc);
		} catch (Exception e) {
			// doesn't matter, it's just an icon
		}
		if (icon != null) {
			frame.setIconImage(icon);
		}

		// maximize jframe needs java 1.4 or later
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		frame.setMaximizedBounds(env.getMaximumWindowBounds());
		frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
	}

	/**
	 * Keep activeViewer field updated.
	 * 
	 */
	@Override
	public void internalFrameActivated(InternalFrameEvent arg0) {
		// propagate event to all interested listeners
		Object[] listeners = events.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == InternalFrameListener.class) {
				((InternalFrameListener) listeners[i + 1]).internalFrameActivated(arg0);
			}
		}

		// switch between viewers
		if (arg0.getInternalFrame() instanceof OVTK2Viewer) {
			activeViewer = (OVTK2Viewer) arg0.getInternalFrame();
			menuBar.updateMenuBar(activeViewer);
			toolBar.getSearchBox().updateRestrictions(activeViewer);
		}
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent arg0) {

		// clean up right side location settings
		if (locations.containsKey(arg0.getInternalFrame()))
			locations.remove(arg0.getInternalFrame());

		if (arg0.getInternalFrame() instanceof OVTK2Viewer) {

			// keep track of changed viewers
			viewers.remove(arg0.getInternalFrame());

			if (activeViewer.equals(arg0.getInternalFrame())) {
				// count left viewers
				JInternalFrame[] frames = desktop.getAllFrames();
				OVTK2Viewer next = null;
				int count = 0;
				for (int i = 0; i < frames.length; i++) {
					if (frames[i] instanceof OVTK2Viewer) {
						count++;
						next = (OVTK2Viewer) frames[i];
					}
				}
				if (count == 0) {
					activeViewer = null;
				} else {
					// set focus on next open OVTK2Viewer
					next.toFront();
					try {
						next.setSelected(true);
					} catch (PropertyVetoException pve) {
						ErrorDialog.show(pve);
					}
				}
			}
		}
		// do not forget to update check-boxes in menu bar
		menuBar.updateMenuBar(activeViewer);
		toolBar.getSearchBox().updateRestrictions(activeViewer);
	}

	/**
	 * Make sure work is saved before closing.
	 * 
	 */
	@Override
	public void internalFrameClosing(InternalFrameEvent arg0) {
		// propagate event to all interested listeners
		Object[] listeners = events.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == InternalFrameListener.class) {
				((InternalFrameListener) listeners[i + 1]).internalFrameClosing(arg0);
			}
		}
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent arg0) {
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent arg0) {
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent arg0) {
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent arg0) {
	}

	public void removeActionListener(ActionListener listener) {
		events.remove(ActionListener.class, listener);
	}

	public void removeInternalFrameListener(InternalFrameListener listener) {
		events.remove(InternalFrameListener.class, listener);
	}

	// current running process influencing menu bar enabling
	private String process = "none";

	// start time a process was set
	private long processStartTime = 0;

	/**
	 * sets a current running state for a process and thereby disables all menus
	 * that contain methods which could interfere.
	 * 
	 * @param process
	 */
	public void setRunningProcess(String process) {
		this.process = process;
		this.processStartTime = System.currentTimeMillis();
		updateMenuBarMode(process);
	}

	/**
	 * Returns the current running state for a process if the menu bar is
	 * blocked.
	 * 
	 * @return process
	 */
	public String getRunningProcess() {
		return process;
	}

	/**
	 * Resets the menus to normal mode after the process has ended.
	 */
	public void notifyTerminationOfProcess() {
		if (Boolean.parseBoolean(Config.config.getProperty("Notifications.Set")) && System.currentTimeMillis() - processStartTime > 500) {

			JOptionPane.showMessageDialog(desktop, process + " " + Config.language.getProperty("Progress.DialogEnd.Message"), Config.language.getProperty("Progress.DialogEnd.Title"), JOptionPane.INFORMATION_MESSAGE);
		}
		updateMenuBarMode("none");
	}

	/**
	 * Sets the menubar mode according to a running process.
	 * 
	 * @param mode
	 */
	public void updateMenuBarMode(String mode) {
		JMenuBar mb = frame.getJMenuBar();
		// when there is something going on
		for (int i = 0; i < mb.getMenuCount(); i++) {
			JMenu menu = mb.getMenu(i);
			if (menu.getText().equals(Config.language.getProperty("Menu.Tools"))) {
				if (!mode.equals("none"))
					menu.setEnabled(false);
				else
					menu.setEnabled(true);
			}
		}
	}

	@Override
	public void vetoableChange(final PropertyChangeEvent event) throws PropertyVetoException {

		String name = event.getPropertyName();
		Object value = event.getNewValue();

		if (event.getSource() instanceof OVTK2Viewer) {

			// we only want to check attempts to close a frame
			if (name.equals("closed") && value.equals(Boolean.TRUE)) {

				// get current viewer
				final OVTK2Viewer viewer = (OVTK2Viewer) event.getSource();

				// ignore warning messages here
				if (viewer.isDestroy())
					return;

				// ask user if it is ok to close
				int option = JOptionPane.showInternalConfirmDialog(desktop, Config.language.getProperty("Dialog.Save.View.Text"), Config.language.getProperty("Dialog.Save.View.Title"), JOptionPane.YES_NO_CANCEL_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					final File file = DesktopUtils.showSaveDialog(new String[] { "xml", "gz" });
					if (file != null) {

						// wrap into a process
						IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
							public void task() {

								try {
									// start OXL export
									new OXLExport(viewer.getONDEXJUNGGraph(), file);
								} catch (Exception e) {
									ErrorDialog.show(e);
								}
							}
						};
						// start processing and monitoring
						p.start();
						OVTKProgressMonitor.start(frame, "Saving file", p);
						resetMouseMode(viewer);
					}
				}

				// if the user doesn't agree, veto the close
				else if (option == JOptionPane.CANCEL_OPTION)
					throw new PropertyVetoException("User canceled close", event);
				else
					resetMouseMode(viewer);
			}
		}
	}

	private void resetMouseMode(OVTK2Viewer viewer) {
		// reset the mouse mode to make annotation bar disappear
		viewer.setMouseMode(true);
		if (viewer.getMetaGraphPanel() != null)
			viewer.getMetaGraphPanel().setMouseMode(true);
	}

	private static String ZERO_CHAR_STRING = "" + (char) 0;

	private static File[] createFileArray(BufferedReader bReader) {
		try {
			java.util.List<File> list = new java.util.ArrayList<File>();
			java.lang.String line = null;
			while ((line = bReader.readLine()) != null) {
				try {
					// kde seems to append a 0 char to the end of the reader
					if (ZERO_CHAR_STRING.equals(line))
						continue;

					java.io.File file = new java.io.File(new java.net.URI(line));
					list.add(file);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			return (java.io.File[]) list.toArray(new File[list.size()]);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return new File[0];
	}

	/** Determine if the dragged data is a file list. */
	private boolean isDragOk(final java.awt.dnd.DropTargetDragEvent evt) {
		boolean ok = false;

		// Get data flavors being dragged
		java.awt.datatransfer.DataFlavor[] flavors = evt.getCurrentDataFlavors();

		// See if any of the flavors are a file list
		int i = 0;
		while (!ok && i < flavors.length) {
			// Is the flavor a file list?
			final DataFlavor curFlavor = flavors[i];
			if (curFlavor.equals(java.awt.datatransfer.DataFlavor.javaFileListFlavor) || curFlavor.isRepresentationClassReader()) {
				ok = true;
			}
			i++;
		}
		return ok;
	}

	@Override
	public void dragEnter(java.awt.dnd.DropTargetDragEvent evt) {
		// Is this an acceptable drag event?
		if (isDragOk(evt)) {
			// Acknowledge that it's okay to enter
			// evt.acceptDrag( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
			evt.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY);
		} else {
			// Reject the drag event
			evt.rejectDrag();
		}
	}

	@Override
	public void dragOver(java.awt.dnd.DropTargetDragEvent evt) {
	}

	@Override
	public void drop(java.awt.dnd.DropTargetDropEvent evt) {
		try {
			// Get whatever was dropped
			java.awt.datatransfer.Transferable tr = evt.getTransferable();

			// Is it a file list?
			if (tr.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor)) {
				// Say we'll take it.
				// evt.acceptDrop (
				// java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
				evt.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);

				// Get a useful list
				@SuppressWarnings("unchecked")
				java.util.List<File> fileList = (java.util.List<File>) tr.getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);

				// only load files after initialisation
				if (OVTK2Desktop.hasFinishedInit()) {
					for (File file : fileList) {
						if (file.getName().endsWith(".oxl"))
							DesktopUtils.openFile(file);
					}
				}

				// Mark that drop is completed.
				evt.getDropTargetContext().dropComplete(true);
			} else {
				// this section will check for a reader flavor. -- Linux
				// (KDE/Gnome) support added.
				DataFlavor[] flavors = tr.getTransferDataFlavors();
				boolean handled = false;
				for (int zz = 0; zz < flavors.length; zz++) {
					if (flavors[zz].isRepresentationClassReader()) {
						// Say we'll take it.
						// evt.acceptDrop (
						// java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
						evt.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);

						Reader reader = flavors[zz].getReaderForText(tr);

						BufferedReader br = new BufferedReader(reader);

						java.io.File[] filesTemp = createFileArray(br);

						// only load files after initialisation
						if (OVTK2Desktop.hasFinishedInit()) {
							for (File file : filesTemp) {
								if (file.getName().endsWith(".oxl"))
									DesktopUtils.openFile(file);
							}
						}

						// Mark that drop is completed.
						evt.getDropTargetContext().dropComplete(true);
						handled = true;
						break;
					}
				}
				if (!handled) {
					evt.rejectDrop();
				}
			}
		} catch (java.io.IOException io) {
			io.printStackTrace();
			evt.rejectDrop();
		} catch (java.awt.datatransfer.UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
			evt.rejectDrop();
		}
	}

	@Override
	public void dragExit(java.awt.dnd.DropTargetEvent evt) {
	}

	@Override
	public void dropActionChanged(java.awt.dnd.DropTargetDragEvent evt) {
		// Is this an acceptable drag event?
		if (isDragOk(evt)) {
			// evt.acceptDrag(
			// java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
			evt.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY);
		} else {
			evt.rejectDrag();
		}
	}

}
