package net.sourceforge.ondex.ovtk2.ui.menu;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import net.sourceforge.ondex.ovtk2.config.Config;

/*************************************************************************
 * Provide a file history mechanism for the File menu of a parent frame).
 * 
 * @since JDK 1.2
 * @author Klaus Berg, taubertj
 *************************************************************************/
public class FileHistory {

	private static final int MAX_ITEM_LEN = 40;
	private static final String FILE_SEPARATOR_STR = System.getProperty("file.separator");
	private static String historySerFile;
	private static int max_itemnames;
	private static ArrayList<String> itemnameHistory = new ArrayList<String>(max_itemnames);
	private static ArrayList<String> pathnameHistory = new ArrayList<String>(max_itemnames);

	private IFileHistory caller;
	private JMenu fileMenu;

	// --- IFileHistory interface ----------------------------------------------

	/**
	 * Interface that must be implemented by a GUI application frame that wants
	 * to use the FileHistory class.
	 * 
	 * @author Klaus Berg
	 * @since JDK 1.2
	 */
	public static interface IFileHistory {

		/**
		 * Get the application name to identify the configuration file in the
		 * the USER_HOME directory. This name should be unique in this
		 * directory.
		 * 
		 * @return the application name
		 */
		public String getApplicationName();

		/**
		 * Get a handle to the frame's file menu.
		 * 
		 * @return the frame's file menu
		 */
		public JMenu getFileMenu();

		/**
		 * Return the size of the main application frame. It is used to centre
		 * the file history maintenance window.
		 * 
		 * @return the main GUI frame's size
		 */
		public Dimension getParentSize();

		/**
		 * Return the main application frame. It is used to centre the file
		 * history maintenance window.
		 * 
		 * @return the main GUI frame
		 */
		public JFrame getParentFrame();

		/**
		 * Perform the load file activity.
		 * 
		 * @param path
		 *            the pathname of the loaded file
		 */
		public void loadFile(String pathname);
	}

	// -------------------------------------------------------------------------

	// CONSTRUCTOR: caller is the parent frame that hosts the file menu
	public FileHistory(IFileHistory caller) {
		this.caller = caller;
		historySerFile = System.getProperty("user.home") + FILE_SEPARATOR_STR + caller.getApplicationName() + "_FILE_HISTORY.cfg";
		String max_itemnames_str = System.getProperty("itemnames.max", "5");
		try {
			max_itemnames = Integer.parseInt(max_itemnames_str);
		} catch (NumberFormatException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
		if (max_itemnames < 1) {
			max_itemnames = 9;
		}
		fileMenu = caller.getFileMenu();
	}

	/*******************************************************************
	 * Initialise itemname and pathname arraylists from historySerFile. build up
	 * the additional entries in the File menu.
	 *******************************************************************/
	public final void initFileMenuHistory() {
		if (new File(historySerFile).exists()) {
			try {
				FileInputStream fis = new FileInputStream(historySerFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				int itemnameCount = ois.readInt();
				// if the user has reduced filehistory.size in the past: cut
				// last items
				if (itemnameCount > max_itemnames) {
					itemnameCount = max_itemnames;
				}
				if (itemnameCount > 0) {
					fileMenu.addSeparator();
				}
				for (int i = 0; i < itemnameCount; i++) {
					itemnameHistory.add((String) ois.readObject());
					pathnameHistory.add((String) ois.readObject());
					MenuItemWithFixedTooltip item = new MenuItemWithFixedTooltip((i + 1) + ": " + itemnameHistory.get(i));
					item.setToolTipText(pathnameHistory.get(i));
					item.addActionListener(new ItemListener(i));
					fileMenu.add(item);
				}
				ois.close();
				fis.close();
			} catch (Exception e) {
				System.err.println("Trouble reading file history entries: " + e);
				e.printStackTrace();
			}
		}
	}

	/***********************************************************
	 * Save itemname and pathname arraylists to historySerFile.*
	 ***********************************************************/
	public void saveHistoryEntries() {
		try {
			FileOutputStream fos = new FileOutputStream(historySerFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			int itemnameCount = itemnameHistory.size();
			oos.writeInt(itemnameCount);
			for (int i = 0; i < itemnameCount; i++) {
				oos.writeObject(itemnameHistory.get(i));
				oos.writeObject(pathnameHistory.get(i));
			}
			oos.flush();
			oos.close();
			fos.close();
		} catch (Exception e) {
			System.err.println("Trouble saving file history entries: " + e);
			e.printStackTrace();
		}
	}

	/*******************************************************************
	 * Insert the last loaded pathname into the File menu if it is not present
	 * yet. Only max pathnames are shown (the max number can be set in Jmon.ini,
	 * default is 9). Every item starts with "<i>: ", where <i> is in the range
	 * [1..max]. The loaded itemname will become item number 1 in the list.
	 *******************************************************************/
	public final void insertPathname(String pathname) {
		for (int k = 0; k < pathnameHistory.size(); k++) {
			if (pathnameHistory.get(k).equals(pathname)) {
				int index = fileMenu.getItemCount() - itemnameHistory.size() + k;
				fileMenu.remove(index);
				pathnameHistory.remove(k);
				itemnameHistory.remove(k);
				if (itemnameHistory.isEmpty()) {
					// JSeparator is the last menu item at (index-1)
					fileMenu.remove(index - 1);
				}
				insertPathname(pathname);
				return;
			}
		}
		if (itemnameHistory.isEmpty()) {
			fileMenu.addSeparator();
		} else {
			// remove all itemname entries to prepare for re-arrangement
			for (int i = fileMenu.getItemCount() - 1, j = 0; j < itemnameHistory.size(); i--, j++) {
				fileMenu.remove(i);
			}
		}
		if (itemnameHistory.size() == max_itemnames) {
			// fileList is full: remove last entry to get space for the first
			// item
			itemnameHistory.remove(max_itemnames - 1);
			pathnameHistory.remove(max_itemnames - 1);
		}
		itemnameHistory.add(0, getItemname(pathname));
		pathnameHistory.add(0, pathname);
		for (int i = 0; i < itemnameHistory.size(); i++) {
			MenuItemWithFixedTooltip item = new MenuItemWithFixedTooltip((i + 1) + ": " + itemnameHistory.get(i));
			item.setToolTipText(pathnameHistory.get(i));
			item.addActionListener(new ItemListener(i));
			fileMenu.add(item);
		}
	}

	/*******************************************************************
	 * Process the file history list that is appended to the file menu: display
	 * a dialog to delete itemname items.
	 *******************************************************************/
	public void processList() {
		final JDialog dialog = new JDialog(caller.getParentFrame(), Config.language.getProperty("FileHistory.Title"), true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final JList itemList = createItemList();
		Container c = dialog.getContentPane();
		JScrollPane scroller = new JScrollPane(itemList);
		scroller.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createLoweredBevelBorder()));
		c.add(scroller, BorderLayout.CENTER);
		JButton deleteB = new JButton(Config.language.getProperty("FileHistory.Delete"));
		deleteB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] indicesToDelete = itemList.getSelectedIndices();
				if (indicesToDelete.length > 0) {
					int oldFileHistorySize = itemnameHistory.size();
					ArrayList<String> itemnames = new ArrayList<String>(oldFileHistorySize - indicesToDelete.length);
					ArrayList<String> pathnames = new ArrayList<String>(oldFileHistorySize - indicesToDelete.length);
					for (int i = 0; i < oldFileHistorySize; i++) {
						boolean copyItem = true;
						for (int j = 0; j < indicesToDelete.length; j++) {
							if (i == indicesToDelete[j]) {
								copyItem = false;
								break;
							}
						}
						if (copyItem) {
							itemnames.add(itemnameHistory.get(i));
							pathnames.add(pathnameHistory.get(i));
						}
					}
					itemnameHistory = itemnames;
					pathnameHistory = pathnames;
					itemList.revalidate();
					itemList.repaint();
					// re-arrange file menu history
					for (int i = fileMenu.getItemCount() - 1, j = 0; j < oldFileHistorySize; i--, j++) {
						fileMenu.remove(i);
					}
					int lastIndex = fileMenu.getItemCount() - 1;
					for (int i = 0; i < itemnameHistory.size(); i++) {
						MenuItemWithFixedTooltip item = new MenuItemWithFixedTooltip((i + 1) + ": " + itemnameHistory.get(i));
						item.setToolTipText(pathnameHistory.get(i));
						item.addActionListener(new ItemListener(i));
						fileMenu.add(item);
					}
					if (itemnameHistory.isEmpty()) {
						fileMenu.remove(lastIndex); // no items were added:
						// remove JSeparator too
					}
				}
			}
		});
		JButton closeB = new JButton(Config.language.getProperty("FileHistory.Close"));
		closeB.setMaximumSize(deleteB.getPreferredSize());
		closeB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		JPanel buttonBox = new JPanel();
		buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.Y_AXIS));
		buttonBox.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		buttonBox.add(Box.createVerticalStrut(10));
		buttonBox.add(deleteB);
		buttonBox.add(Box.createVerticalStrut(10));
		buttonBox.add(closeB);
		buttonBox.add(Box.createVerticalGlue());
		c.add(buttonBox, BorderLayout.EAST);
		dialog.pack();
		dialog.setSize(350, 200);
		// centre dialog in parent frame
		Dimension parentSize = caller.getParentSize();
		Dimension mySize = dialog.getSize();
		dialog.setLocation(parentSize.width / 2 - mySize.width / 2, parentSize.height / 2 - mySize.height / 2);
		dialog.setVisible(true);
	}

	/**********************************************************
	 * Return the itemname (abbreviated itemname if necessary) to be shown in
	 * the file menu open item list. A maximum of MAX_ITEM_LEN characters is
	 * used for the itemname because we do not want to make the JMenuItem entry
	 * too wide.
	 **********************************************************/
	protected String getItemname(String pathname) {
		final char FILE_SEPARATOR = FILE_SEPARATOR_STR.charAt(0);
		final int pathnameLen = pathname.length();
		// if the pathname is short enough: return whole pathname
		if (pathnameLen <= MAX_ITEM_LEN) {
			return pathname;
		}
		// if we have only one directory: return whole pathname
		// we will not cut to MAX_ITEM_LEN here
		if (pathname.indexOf(FILE_SEPARATOR_STR) == pathname.lastIndexOf(FILE_SEPARATOR_STR)) {
			return pathname;
		} else {
			// abbreviate pathname: Windows OS like solution
			final int ABBREVIATED_PREFIX_LEN = 6; // e.g.: C:\..\
			final int MAX_PATHNAME_LEN = MAX_ITEM_LEN - ABBREVIATED_PREFIX_LEN;
			int firstFileSeparatorIndex = 0;
			for (int i = pathnameLen - 1; i >= (pathnameLen - MAX_PATHNAME_LEN); i--) {
				if (pathname.charAt(i) == FILE_SEPARATOR) {
					firstFileSeparatorIndex = i;
				}
			}
			if (firstFileSeparatorIndex > 0) {
				return pathname.substring(0, 3) + ".." + pathname.substring(firstFileSeparatorIndex, pathnameLen);
			} else {
				return pathname.substring(0, 3) + ".." + FILE_SEPARATOR_STR + ".." + pathname.substring(pathnameLen - MAX_PATHNAME_LEN, pathnameLen);
			}
		}
	}

	/**************************************************************
	 * Create a JList instance with itemnameHistory as its model. *
	 **************************************************************/
	private final JList createItemList() {
		ListModel model = new ListModel();
		JList list = new JList(model);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		return list;
	}

	// --- Helper classes ------------------------------------------------------

	/***********************************************************
	 * Create a tool-tip location directly over the menu item, i.e., left align
	 * the tool-tip text in "overlay" technique.
	 ***********************************************************/
	private final class MenuItemWithFixedTooltip extends JMenuItem {

		/**
		 * generated
		 */
		private static final long serialVersionUID = -3207273418566534726L;

		public MenuItemWithFixedTooltip(String text) {
			super(text);
		}

		public Point getToolTipLocation(MouseEvent e) {
			Graphics g = getGraphics();
			FontMetrics metrics = g.getFontMetrics(g.getFont());
			String prefix = itemnameHistory.size() <= 9 ? "8: " : "88: ";
			int prefixWidth = metrics.stringWidth(prefix);
			int x = JButton.TRAILING + JButton.LEADING - 1 + prefixWidth;
			return new Point(x, 0);
		}
	}

	/***********************************
	 * Listen to menu item selections. *
	 ***********************************/
	private final class ItemListener implements ActionListener {
		int itemNbr;

		ItemListener(int itemNbr) {
			this.itemNbr = itemNbr;
		}

		public void actionPerformed(ActionEvent e) {
			caller.loadFile(pathnameHistory.get(itemNbr));
			JMenuItem item = (JMenuItem) e.getSource();
			FileHistory.this.insertPathname(item.getToolTipText());
		}
	}

	/********************************************************
	 * The list model for our File History dialog itemList. *
	 ********************************************************/
	private final class ListModel extends AbstractListModel {

		/**
		 * generated
		 */
		private static final long serialVersionUID = 1218909196670073607L;

		public Object getElementAt(int i) {
			return itemnameHistory.get(i);
		}

		public int getSize() {
			return itemnameHistory.size();
		}

	}

} // end class FileHistory
