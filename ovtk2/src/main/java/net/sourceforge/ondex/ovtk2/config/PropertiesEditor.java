package net.sourceforge.ondex.ovtk2.config;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.ovtk2.util.CustomFileFilter;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.ctc.wstx.io.CharsetNames;

/**
 * Simple GUI for editing JAVA Properties files.
 * 
 * @author taubertj
 * 
 */
public class PropertiesEditor implements ActionListener {

	/**
	 * Wrap a Properties Object in a TableModel.
	 * 
	 * @author taubertj
	 * 
	 */
	class PropertiesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 6099005154164113831L;

		private String[] columnNames = new String[] { "Name", "Value", "Delete?" };

		private Hashtable<Integer, String[]> pairs = null;

		private Properties properties = null;

		public PropertiesTableModel(Properties properties) {
			this.properties = properties;
			syncProperties();
		}

		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getRowCount() {
			return pairs.size();
		}

		public Object getValueAt(int row, int col) {
			if (col == 2)
				return Boolean.FALSE;
			return pairs.get(row)[col];
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			if (col == 0) {
				properties.remove(pairs.get(row)[0]);
				properties.setProperty(value.toString(), pairs.get(row)[1]);
				pairs.put(row, new String[] { value.toString(), pairs.get(row)[1] });
				fireTableCellUpdated(row, col);
			} else if (col == 1) {
				properties.setProperty(pairs.get(row)[0], value.toString());
				pairs.put(row, new String[] { pairs.get(row)[0], value.toString() });
				fireTableCellUpdated(row, col);
			} else if (col == 2) {
				properties.remove(pairs.get(row)[0]);
				syncProperties();
				fireTableRowsDeleted(row, row);
			}
			if (row == pairs.size() - 1) {
				row++;
				pairs.put(row, new String[] { "", "" });
				fireTableRowsInserted(row, row);
			}
		}

		private void syncProperties() {
			pairs = new Hashtable<Integer, String[]>();
			String[] keys = properties.keySet().toArray(new String[0]);
			Arrays.sort(keys);
			for (int i = 0; i < keys.length; i++) {
				String name = keys[i];
				String value = properties.getProperty(name);
				pairs.put(i, new String[] { name, value });
			}
			pairs.put(pairs.size(), new String[] { "", "" });
		}
	}

	public static void main(String[] args) {
		new PropertiesEditor();
	}

	// JFrame displaying the editor
	private JFrame frame = null;

	// contains config properties
	private Properties properties = new Properties();

	// contains config properties as table
	private JTable table = new JTable(new PropertiesTableModel(properties));

	/**
	 * Constructor to initialize UI.
	 * 
	 */
	public PropertiesEditor() {
		initUI();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {

		String cmd = arg0.getActionCommand();

		if (cmd.equals("new")) {
			newProperties();
		} else if (cmd.equals("load")) {
			loadProperties();
		} else if (cmd.equals("save")) {
			saveProperties();
		} else if (cmd.equals("exit")) {
			showSaveExitDialog();
			System.exit(0);
		} else if (cmd.equals("about")) {
			JOptionPane.showMessageDialog(frame, "Simple Java Properties Editor 1.1, The ONDEX Project 2011.", "About...", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Constructs JFrame and displays it.
	 * 
	 */
	private void initUI() {
		frame = new JFrame("PropertiesEditor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JFrame.setDefaultLookAndFeelDecorated(true);

		// contains menu
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		// create file menu
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem newItem = new JMenuItem("New");
		newItem.addActionListener(this);
		newItem.setActionCommand("new");
		fileMenu.add(newItem);

		JMenuItem loadItem = new JMenuItem("Load");
		loadItem.addActionListener(this);
		loadItem.setActionCommand("load");
		fileMenu.add(loadItem);

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(this);
		saveItem.setActionCommand("save");
		fileMenu.add(saveItem);

		fileMenu.addSeparator();

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		exitItem.setActionCommand("exit");
		fileMenu.add(exitItem);

		// create help menu
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(this);
		aboutItem.setActionCommand("about");
		helpMenu.add(aboutItem);

		// add table to frame
		JScrollPane scrollPane = new JScrollPane(table);
		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(scrollPane);

		// make frame visible and set it to the front
		frame.pack();
		frame.setVisible(true);
		frame.toFront();
	}

	/**
	 * Creates new empty properties.
	 * 
	 */
	private void newProperties() {
		System.out.println("Creating new document.");
		properties = new Properties();
		table.setModel(new PropertiesTableModel(properties));
	}

	/**
	 * Show file open dialog and loads properties.
	 * 
	 */
	private void loadProperties() {
		System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");

		// create a file chooser
		final JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
		fc.addChoosableFileFilter(new CustomFileFilter("xml"));

		// in response to a button click:
		int returnVal = fc.showOpenDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			System.out.println("Opening: " + file.getName() + ".");
			try {
				properties.loadFromXML(new FileInputStream(file));
				table.setModel(new PropertiesTableModel(properties));
			} catch (InvalidPropertiesFormatException ipfe) {
				System.err.println("Error in " + file.getName() + " " + ipfe.getMessage());
			} catch (FileNotFoundException fnfe) {
				System.err.println("Error in " + file.getName() + " " + fnfe.getMessage());
			} catch (IOException ioe) {
				System.err.println("Error in " + file.getName() + " " + ioe.getMessage());
			}
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}

	/**
	 * Show file save dialog and saves properties.
	 * 
	 */
	private void saveProperties() {
		// create a file chooser
		final JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
		fc.addChoosableFileFilter(new CustomFileFilter("xml"));

		// in response to a button click:
		int returnVal = fc.showSaveDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			System.out.println("Saving: " + file.getName() + ".");

			// configure XML output
			XMLOutputFactory2 xmlOutput = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
			xmlOutput.configureForXmlConformance();
			xmlOutput.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);
			try {
				// configure writer
				XMLStreamWriter2 xmlw = (XMLStreamWriter2) xmlOutput.createXMLStreamWriter(new FileWriter(file), CharsetNames.CS_UTF8);

				xmlw.writeStartDocument();
				xmlw.writeCharacters("\n"); // indent
				xmlw.writeDTD("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
				xmlw.writeCharacters("\n"); // indent
				xmlw.writeStartElement("properties");
				xmlw.writeCharacters("\n"); // indent
				xmlw.writeStartElement("comment");
				xmlw.writeCharacters("Generated by PropertiesEditor 1.1");
				xmlw.writeEndElement();
				xmlw.writeCharacters("\n"); // indent

				Object[] keys = properties.keySet().toArray();
				Arrays.sort(keys);
				for (Object key : keys) {
					xmlw.writeStartElement("entry");
					xmlw.writeAttribute("key", key.toString());
					xmlw.writeCharacters(properties.get(key).toString());
					xmlw.writeEndElement();
					xmlw.writeCharacters("\n"); // indent
				}

				xmlw.writeEndElement();
				xmlw.writeEndDocument();

				xmlw.flush();
				xmlw.close();
			} catch (FileNotFoundException fnfe) {
				System.err.println("Error in " + file.getName() + " " + fnfe.getMessage());
			} catch (IOException ioe) {
				System.err.println("Error in " + file.getName() + " " + ioe.getMessage());
			} catch (XMLStreamException e) {
				System.err.println("Error in " + file.getName() + " " + e.getMessage());
			}

		} else {
			System.out.println("Save command cancelled by user.");
		}
	}

	/**
	 * User dialog asking to save the current document on exit.
	 * 
	 */
	private void showSaveExitDialog() {
		final JOptionPane optionPane = new JOptionPane("Would you like to save the current content?\n", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		final JDialog dialog = new JDialog(frame, "Save current?", true);
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();

				if (dialog.isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
					dialog.setVisible(false);
				}
			}
		});
		dialog.pack();
		dialog.setVisible(true);

		int value = (Integer) optionPane.getValue();
		if (value == JOptionPane.YES_OPTION) {
			saveProperties();
		}
	}

}
