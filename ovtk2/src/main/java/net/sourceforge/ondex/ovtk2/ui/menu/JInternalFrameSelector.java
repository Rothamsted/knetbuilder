package net.sourceforge.ondex.ovtk2.ui.menu;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.LauncherFrame;
import net.sourceforge.ondex.ovtk2.util.RegisteredFrame;
import net.sourceforge.ondex.ovtk2.util.WindowRegister;
import net.sourceforge.ondex.ovtk2.util.WindowRegisteryChangeListener;

/**
 * Menu that shows all open frames that register with the WindowRegister
 * 
 * @author hindlem
 * 
 */
public class JInternalFrameSelector extends JMenu implements WindowRegisteryChangeListener, ActionListener {

	private class CloseListener implements ActionListener {

		private String group;

		public CloseListener() {
			this(null);
		}

		public CloseListener(String group) {
			this.group = group;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			// prevent warnings from appearing
			for (RegisteredFrame frame : frameRegistry.getInternalFrames()) {
				if (frame instanceof OVTK2Viewer) {
					OVTK2Viewer viewer = (OVTK2Viewer) frame;
					viewer.setDestroy(true);
				}
			}

			// show warning message
			int result = JOptionPane.showConfirmDialog((Component) e.getSource(), Config.language.getProperty("Menu.Windows.ClosureWarning").toString(), Config.language.getProperty("Menu.Windows.ClosureWarningTitle").toString(), JOptionPane.YES_NO_OPTION);

			if (result == JOptionPane.NO_OPTION) {
				return;
			}

			// dont listen for events right now
			frameRegistry.removeListener(instance);

			// collect frame to close
			List<JInternalFrame> closableFrames = new ArrayList<JInternalFrame>();
			for (RegisteredFrame frame : frameRegistry.getInternalFrames()) {
				if (frame instanceof JInternalFrame && (group == null || frame.getGroup().equals(group))) {
					JInternalFrame internalF = ((JInternalFrame) frame);
					if (internalF.isClosable()) {
						closableFrames.add(internalF);
					}
				}
			}

			// close frames
			for (JInternalFrame internalF : closableFrames) {
				try {
					// close frame
					internalF.setClosed(true);
					frameRegistry.deregisterInternalFrame((RegisteredFrame) internalF);
				} catch (PropertyVetoException e1) {
					// it is legal to veto the close
				}
			}

			// listening for events again
			frameRegistry.registerListener(instance);
			registeryChangedEvent();

			// switch on warnings for remaining windows when closing group
			for (RegisteredFrame frame : frameRegistry.getInternalFrames()) {
				if (frame instanceof OVTK2Viewer) {
					OVTK2Viewer viewer = (OVTK2Viewer) frame;
					viewer.setDestroy(false);
				}
			}
		}
	}

	/**
	 * 
	 * @author hindlem
	 * 
	 */
	private class InternalFrameMenuItem extends JMenuItem implements ActionListener {

		private static final long serialVersionUID = 1L;

		private RegisteredFrame frame;

		public InternalFrameMenuItem(RegisteredFrame frame, String text) {
			this(frame, text, null);
		}

		public InternalFrameMenuItem(RegisteredFrame frame, String text, Icon icon) {
			super(text, icon);
			this.frame = frame;
			this.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (frame instanceof JInternalFrame) {
				JInternalFrame internalF = (JInternalFrame) frame;
				try {
					internalF.setSelected(true);
					if (internalF.isIcon())
						internalF.setIcon(false);
					internalF.moveToFront();
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
			} else if (frame instanceof LauncherFrame) {
				LauncherFrame launcher = (LauncherFrame) frame;
				launcher.getJFrame().setState(Frame.NORMAL);
				launcher.getJFrame().toFront();
			}
		}
	}

	/**
	 * 
	 * @author hindlem
	 * 
	 */
	private class MinimizeListener implements ActionListener {

		private String group;

		public MinimizeListener() {
			this(null);
		}

		public MinimizeListener(String group) {
			this.group = group;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RegisteredFrame> frames = frameRegistry.getInternalFrames();
			for (RegisteredFrame frame : frames) {
				if (frame instanceof JInternalFrame && (group == null || frame.getGroup().equals(group))) {
					JInternalFrame internalF = ((JInternalFrame) frame);
					if (internalF.isIconifiable() && !internalF.isIcon()) {
						try {
							internalF.setIcon(true);
						} catch (PropertyVetoException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}

	}

	/**
	 * 
	 * @author hindlem, taubertj
	 * 
	 */
	private class ShowListener implements ActionListener {

		private String group;

		public ShowListener() {
			this(null);
		}

		public ShowListener(String group) {
			this.group = group;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RegisteredFrame> frames = frameRegistry.getInternalFrames();
			for (RegisteredFrame frame : frames) {
				if (frame instanceof JInternalFrame && (group == null || frame.getGroup().equals(group))) {
					JInternalFrame internalF = ((JInternalFrame) frame);
					if (internalF.isIconifiable() && internalF.isIcon()) {
						try {
							internalF.setIcon(false);
						} catch (PropertyVetoException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	public static final String NONE = "NONE";

	private static final long serialVersionUID = 1L;

	private int frameDistance = 75;

	private WindowRegister frameRegistry;

	private JInternalFrameSelector instance;

	private JMenuItem nextItem, cascadeItem, tileItem;

	/**
	 * 
	 * @param s
	 *            the name of this menu
	 */
	public JInternalFrameSelector(String s) {
		super(s);
		frameRegistry = WindowRegister.getInstance();
		frameRegistry.registerListener(this);
		this.instance = this;

	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source == nextItem)
			selectNextWindow();
		else if (source == cascadeItem)
			cascadeWindows();
		else if (source == tileItem)
			tileWindows();
	}

	private void buildMenu() {

		HashMap<String, List<RegisteredFrame>> groupToName = new HashMap<String, List<RegisteredFrame>>();

		List<RegisteredFrame> frames = frameRegistry.getInternalFrames();
		for (RegisteredFrame frame : frames) {
			List<RegisteredFrame> list = groupToName.get(frame.getGroup());
			if (list == null) {
				list = new ArrayList<RegisteredFrame>();
				String group = NONE;
				if (frame.getGroup() != null || frame.getGroup().trim().length() != 0)
					group = frame.getGroup();

				groupToName.put(group, list);
			}
			list.add(frame);
		}
		boolean start = true;
		for (String group : groupToName.keySet()) {
			List<RegisteredFrame> list = groupToName.get(group);
			Collections.sort(list, new Comparator<RegisteredFrame>() {

				@Override
				public int compare(RegisteredFrame o1, RegisteredFrame o2) {
					return o1.getName().compareTo(o2.getName());
				}

			});
			if (!start)
				addSeparator();
			if (list.size() < 3 || NONE.equals(group) || group.length() < 0) {
				for (RegisteredFrame frame : list) {
					String name = frame.getName();
					if (!NONE.equals(group) && group.length() > 0) {
						name = frame.getGroup() + " - " + name;
					}
					InternalFrameMenuItem item = new InternalFrameMenuItem(frame, name);
					add(item);
				}
			} else {
				JMenu submenu = new JMenu(group);
				for (RegisteredFrame frame : list) {
					String name = frame.getName();
					InternalFrameMenuItem item = new InternalFrameMenuItem(frame, name);
					submenu.add(item);
				}
				submenu.addSeparator();

				JMenuItem minimize = new JMenuItem(Config.language.getProperty("Menu.Windows.MinimizeGroup"));
				minimize.addActionListener(new MinimizeListener(group));
				submenu.add(minimize);

				JMenuItem show = new JMenuItem(Config.language.getProperty("Menu.Windows.ShowGroup"));
				show.addActionListener(new ShowListener(group));
				submenu.add(show);

				JMenuItem close = new JMenuItem(Config.language.getProperty("Menu.Windows.CloseGroup"));
				close.addActionListener(new CloseListener(group));
				submenu.add(close);

				add(submenu);
			}
			start = false;
		}
		addSeparator();
		JMenuItem minimizeAll = new JMenuItem(Config.language.getProperty("Menu.Windows.MinimizeAll"));
		minimizeAll.addActionListener(new MinimizeListener());
		add(minimizeAll);

		JMenuItem showAll = new JMenuItem(Config.language.getProperty("Menu.Windows.ShowAll"));
		showAll.addActionListener(new ShowListener());
		add(showAll);

		JMenuItem closeAll = new JMenuItem(Config.language.getProperty("Menu.Windows.CloseAll"));
		closeAll.addActionListener(new CloseListener());
		add(closeAll);

		addSeparator();

		nextItem = new JMenuItem(Config.language.getProperty("Menu.Windows.Next"));
		nextItem.addActionListener(this);
		add(nextItem);

		cascadeItem = new JMenuItem(Config.language.getProperty("Menu.Windows.Cascade"));
		cascadeItem.addActionListener(this);
		add(cascadeItem);

		tileItem = new JMenuItem(Config.language.getProperty("Menu.Windows.Tile"));
		tileItem.addActionListener(this);
		add(tileItem);

	}

	public void cascadeWindows() {
		JDesktopPane desktop = OVTK2Desktop.getInstance().getDesktopPane();

		JInternalFrame[] frames = desktop.getAllFrames();
		int x = 0;
		int y = 0;
		int width = desktop.getWidth() / 2;
		int height = desktop.getHeight() / 2;

		for (int i = 0; i < frames.length; i++) {
			if (!frames[i].isIcon()) {
				try { /*
					 * try to make maximized frames resizable this might be
					 * vetoed
					 */
					frames[i].setMaximum(false);
					frames[i].reshape(x, y, width, height);

					x += frameDistance;
					y += frameDistance;
					// wrap around at the desktop edge
					if (x + width > desktop.getWidth())
						x = 0;
					if (y + height > desktop.getHeight())
						y = 0;
				} catch (PropertyVetoException e) {
				}
			}
		}
	}

	@Override
	public synchronized void registeryChangedEvent() {
		removeAll();
		buildMenu();
		validate();
		repaint();
	}

	public void selectNextWindow() {
		JDesktopPane desktop = OVTK2Desktop.getInstance().getDesktopPane();
		JInternalFrame[] frames = desktop.getAllFrames();
		for (int i = 0; i < frames.length; i++) {
			if (frames[i].isSelected()) { /*
										 * find next frame that isn't an icon
										 * and can be selected
										 */
				try {
					int next = i + 1;
					// check just in case
					if (next > i)
						return;
					while (next != i && frames[next].isIcon())
						next++;
					if (next == i)
						return;
					// all other frames are icons or veto selection
					frames[next].setSelected(true);
					frames[next].toFront();
					return;
				} catch (PropertyVetoException e) {
				}
			}
		}
	}

	public void tileWindows() {

		JDesktopPane desktop = OVTK2Desktop.getInstance().getDesktopPane();
		JInternalFrame[] frames = desktop.getAllFrames();

		// count frames that aren't iconized
		int frameCount = 0;
		for (int i = 0; i < frames.length; i++) {
			if (!frames[i].isIcon())
				frameCount++;
		}

		int rows = (int) Math.sqrt(frameCount);
		int cols = frameCount / rows;
		int extra = frameCount % rows;
		// number of columns with an extra row

		int width = desktop.getWidth() / cols;
		int height = desktop.getHeight() / rows;
		int r = 0;
		int c = 0;
		for (int i = 0; i < frames.length; i++) {
			if (!frames[i].isIcon()) {
				try {
					frames[i].setMaximum(false);
					frames[i].reshape(c * width, r * height, width, height);
					r++;
					if (r == rows) {
						r = 0;
						c++;
						if (c == cols - extra) { // start adding an extra row
							rows++;
							height = desktop.getHeight() / rows;
						}
					}
				} catch (PropertyVetoException e) {
				}
			}
		}
	}
}
