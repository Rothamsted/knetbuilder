/*
 * Created on 14.07.2003
 *
 */
package net.sourceforge.ondex.ovtk2.io.importwizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

/**
 * <b>ImportWizard</b> is a graphical front-end for importing data from
 * micro-array experiment files, behaviour can be configured by a given config
 * hash to the execute method
 * 
 * @author Jan Taubert - Uni Bielefeld
 * @version aug06
 * @see ConfigTool
 */
@SuppressWarnings("unchecked")
public class ImportWizard extends JInternalFrame implements ActionListener {

	/**
	 * Default serialisation unique id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Container class for java 1.5 type compatibility.
	 * 
	 * @author taubertj
	 * 
	 */
	private class ResultContainer {
		ArrayList<String> columnNames;

		ArrayList<ArrayList<Object>> data;
	}

	/**
	 * extends the Swing BasicComboBoxRenderer
	 * 
	 * enables tooltip text over a normal ComboBox
	 * 
	 * @author Jan Taubert - Uni Bielefeld
	 * @see javax.swing.plaf.basic.BasicComboBoxRenderer
	 */
	private class MyComboBoxRenderer extends BasicComboBoxRenderer {

		/**
		 * Default serialisation unique id.
		 */
		private static final long serialVersionUID = 1L;

		// list of tooltips
		private String[] tooltips;

		public MyComboBoxRenderer(String[] tooltips) {
			// build default BasicComboBoxRenderer
			super();
			this.tooltips = tooltips;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				// change highlighting of selection
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());

				// get the JList index tooltip here
				if (index > -1) {
					list.setToolTipText(tooltips[index]);
				}
			} else {
				// normal back/foreground for non selection
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			// set font and text or empty text if there is no value
			setFont(list.getFont());
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	/**
	 * extends the Swing JTable
	 * 
	 * enables the first element of each row and the first row to use special
	 * cellrenderer and celleditor, this makes the checkboxes for boolean values
	 * and the dropdown boxes of these special cells
	 * 
	 * @author Jan Taubert - Uni Bielefeld
	 * @see javax.swing.JTable
	 */
	private class MyJTable extends JTable {

		/**
		 * Default serialisation unique id.
		 */
		private static final long serialVersionUID = 1L;

		private boolean choose;

		private DefaultCellEditor defaultCellEditor;

		private DefaultTableCellRenderer defaultTableCellRenderer;

		public MyJTable(MyTableModel myModel, boolean choose) {
			// build a normal JTable
			super(myModel);
			this.choose = choose;
			if (choose)
				createDescription(null);
		}

		public MyJTable(Vector<?> rowData, Vector<?> columnNames, boolean choose) {
			// build a normal JTable
			super(rowData, columnNames);
			this.choose = choose;
			if (choose)
				createDescription(columnNames);
		}

		private void createDescription(Vector<?> columnNames) {
			// get all necessary options
			Hashtable<String, String> cells = globalConfiguration.get("cells");

			// contains the names of the column descriptions
			String[] tmpid = new String[cells.size()];
			int i = 0;
			for (Enumeration<String> e = cells.keys(); e.hasMoreElements();) {
				tmpid[i] = e.nextElement();
				i++;
			}

			// sort alphabetically
			Arrays.sort(tmpid);

			// set up the editor for the desc cells
			String[] ids = new String[cells.size() + 1];
			ids[0] = "none"; // Default value do not change
			// append names from config to the default value
			for (int j = 1; j < ids.length; j++) {
				ids[j] = tmpid[j - 1];
			}

			Hashtable<String, String> options = globalConfiguration.get("advanced");

			boolean comboBoxEditable = Boolean.parseBoolean((String) options.get("comboBoxEditable"));
			boolean copyHeaders = Boolean.parseBoolean((String) options.get("copyHeaderNames"));

			// get all tooltips for the desc cells
			String[] tooltips = new String[cells.size() + 1];
			tooltips[0] = "none"; // Default value do not change
			for (int j = 1; j < ids.length; j++) {
				tooltips[j] = cells.get(ids[j]);
			}

			// this use a modified comboxrenderer which supports tooltips
			final JComboBox comboBox;

			if (columnNames != null && copyHeaders) {
				comboBox = new JComboBox(ids);
				for (int a = 0; a < columnNames.size(); a++) {
					String colName = (String) columnNames.get(a);

					if (!(colName.length() == 0) && !(colName.startsWith("col") && colName.length() <= 5)) {
						comboBox.addItem(colName);
					}
				}
			} else {
				comboBox = new JComboBox(ids);
			}

			// comboBox.setEditable(true);
			comboBox.setFont(new Font("Default", Font.BOLD, 16));
			comboBox.setBackground(Color.WHITE);

			defaultCellEditor = new DefaultCellEditor(comboBox);
			// set up tooltips for the desc cells
			defaultTableCellRenderer = new DefaultTableCellRenderer();

			if (comboBoxEditable) {
				comboBox.setEditable(true);
				comboBox.setToolTipText("Click to select item form list or use keyboard to change name");
			} // this doesn't allow for the use of tooltips
			else {
				comboBox.setRenderer(new MyComboBoxRenderer(tooltips));
				defaultTableCellRenderer.setToolTipText("Click to select item from list");
			}
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (choose) {
				if (row == 0 && column >= 0) {
					return defaultCellEditor;
				}
				// else...
				return null;
			} // else...
			if (row == 0 && column == 0) {
				return super.getCellEditor(1, 1);
			}
			if (row == 0 && column > 0) {
				return super.getCellEditor(1, 0);
			}
			// else...
			return super.getCellEditor(row, column);
		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (choose) {
				if (row == 0 && column >= 0) {
					return defaultTableCellRenderer;
				}
				// else...
				return super.getCellRenderer(row, column);
			} // else...
			if (row == 0 && column == 0) {
				return super.getCellRenderer(1, 1);
			}
			if (row == 0 && column > 0) {
				return super.getCellRenderer(1, 0);
			}
			// else...
			return super.getCellRenderer(row, column);
		}
	}

	/**
	 * extends the Swing AbstractTableModel
	 * 
	 * used for vector based data processing
	 * 
	 * @author Jan Taubert - Uni Bielefeld
	 * @see javax.swing.table.AbstractTableModel
	 */
	private class MyTableModel extends AbstractTableModel {

		/**
		 * Default serialisation unique id.
		 */
		private static final long serialVersionUID = 1L;

		ArrayList<String> columnNames = new ArrayList<String>();

		ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();

		boolean DEBUG = false;

		/**
		 * Constructur of a table model for given column names and data.
		 * 
		 * @param rc
		 *            ResultContainer
		 */
		public MyTableModel(ResultContainer rc) {
			// get the content
			if (stringFilename != null && !stringFilename.equals("")) {
				columnNames = rc.columnNames;
				data = rc.data;
			}
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		@Override
		public Class<?> getColumnClass(int c) {
			return getValueAt(1, c).getClass();
		}

		public int getColumnCount() {
			return columnNames.size();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames.get(col);
		}

		/*
		 * returns the actual content of the table
		 */
		public ResultContainer getContent() {
			ResultContainer content = new ResultContainer();
			content.columnNames = columnNames;
			content.data = data;
			return content;
		}

		public int getRowCount() {
			return data.size();
		}

		public Object getValueAt(int row, int col) {
			if (row < data.size()) {
				ArrayList<Object> cols = data.get(row);
				if (col < cols.size()) {
					Object obj = cols.get(col);
					if (obj != null) {
						return obj;
					}
				}
			}
			return new String("");
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col == 0 && row > 0) {
				if (getColumnClass(col) == Boolean.class) {
					return true;
				} else {
					return false;
				}
			}
			if (col > 0 && row == 0) {
				if ((getValueAt(row, col)).getClass() == Boolean.class) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		}

		private void printDebugData() {
			int numRows = getRowCount();
			int numCols = getColumnCount();

			for (int i = 0; i < numRows; i++) {
				System.out.print("    row " + i + ":");
				for (int j = 0; j < numCols; j++) {
					Object obj = data.get(i).get(j);
					System.out.print("  " + obj);
				}
				System.out.println();
			}
			System.out.println("--------------------------");
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		@Override
		public void setValueAt(Object value, int row, int col) {
			if (DEBUG) {
				System.out.println("Setting value at " + row + "," + col + " to " + value + " (an instance of " + value.getClass() + ")");
			}

			// put value into datastructure
			data.get(row).set(col, value);

			// do tableupdate
			fireTableCellUpdated(row, col);

			if (DEBUG) {
				System.out.println("New value of data:");
				printDebugData();
			}
		}
	}

	private boolean booleanFirstLineAsTableHeader;

	private boolean booleanIsParsingChanged;

	private Hashtable<String, Hashtable<String, String>> globalConfiguration;

	private int intWhichJPanelIsActive;

	private int intWhichSeparatorToUse;

	private JComboBox jComboBoxSelectMapping;

	private JComboBox jComboBoxUseFirstLineAsTableHeader;

	private JTable jTableFirstStep;

	private JTable jTableSecondStep;

	private JTable jTableThirdStep;

	private JTextField jTextFieldFilename;

	private JTextField jTextFieldNbRowsToSkip;

	private Container contentPane;

	private MyTableModel myTableModel;

	private String stringFilename;

	private String stringMappingName;

	private String stringRowsToSkip;

	private Vector<Object> vectorReturnContent;

	private ArrayList<String> columnNames = new ArrayList<String>();

	private boolean finished = false;

	/**
	 * Case of running in applet
	 */
	private JFrame parent;

	/**
	 * constructor
	 * 
	 * used when no filename is given, filename is read for xml
	 * 
	 * @param config
	 *            Configuration Hash
	 */
	public ImportWizard(Hashtable<String, Hashtable<String, String>> config) {
		this(new File((config.get("default")).get("filename")).getAbsolutePath(), config, null);
	}

	/**
	 * construtor
	 * 
	 * used with a given filename
	 * 
	 * @param file
	 *            file to open
	 * @param config
	 *            Configuration Hash
	 */
	public ImportWizard(String filename, Hashtable<String, Hashtable<String, String>> config, JFrame parent) {
		super("ImportWizard", true, true, true, true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// check for configuration
		if (config == null) {
			System.out.println("Config error");
			throw new RuntimeException("Config error");
		}
		this.globalConfiguration = config;

		this.parent = parent;

		// add content either to this or parent
		if (parent != null)
			contentPane = parent.getContentPane();
		else
			contentPane = this.getContentPane();

		// first JPanel is active
		intWhichJPanelIsActive = 1;

		// get all necessary options to fill the text labels
		Hashtable<String, String> options = globalConfiguration.get("default");

		// set default filename
		stringFilename = filename;

		// set default separator
		intWhichSeparatorToUse = Integer.parseInt((String) options.get("separator"));

		// set default number of rows to skip from the beginnig of the original
		// document
		stringRowsToSkip = (String) options.get("rowstoskip");

		// set default behavior whether or not to use the first line as the
		// table header
		if (options.get("firstlineheader").equals("true")) {
			booleanFirstLineAsTableHeader = true;
		} else {
			booleanFirstLineAsTableHeader = false;
		}

		// table has to be updated
		booleanIsParsingChanged = true;

		// initialize the preview table
		jTableFirstStep = null;

		// initialize dummy vector
		vectorReturnContent = new Vector<Object>();

		// add GUI to the JFrame
		contentPane.setSize(800, 600);
		contentPane.setLayout(new GridLayout(1, 1));
		contentPane.add(stepFirst());

		if (parent == null)
			this.setSize(800, 600);
	}

	/**
	 * standard method handling ActionEvents from the GUI
	 */
	public void actionPerformed(ActionEvent ae) {

		// set the new filename from the input field
		if (ae.getActionCommand().equals("filechange")) {
			stringFilename = jTextFieldFilename.getText();
			if (stringFilename.length() > 0) {
				// table has to be updated
				booleanIsParsingChanged = true;
			}
		}

		// opens up a standard file chooser
		if (ae.getActionCommand().equals("chooser")) {
			showFileChooser();
			// table has to be updated
			booleanIsParsingChanged = true;
		}

		/*
		 * here each possible separator is handled and according to this the
		 * separator state and table is updated
		 */
		if (ae.getActionCommand().equals("tabulator")) {
			intWhichSeparatorToUse = 0;
			booleanIsParsingChanged = true;
		}

		if (ae.getActionCommand().equals("space")) {
			intWhichSeparatorToUse = 1;
			booleanIsParsingChanged = true;
		}

		if (ae.getActionCommand().equals("comma")) {
			intWhichSeparatorToUse = 2;
			booleanIsParsingChanged = true;
		}

		// sets whether the first line should be used as a table header or not
		if (ae.getActionCommand().equals("lineheader")) {
			JComboBox cb = (JComboBox) ae.getSource();
			String yesno = (String) cb.getSelectedItem();
			if (yesno.equals("yes")) {
				booleanFirstLineAsTableHeader = true;
			} else {
				booleanFirstLineAsTableHeader = false;
			}
			// table has to be updated
			booleanIsParsingChanged = true;
		}

		// get numbers of rows which should be skipped from the beginning of the
		// document
		if (ae.getActionCommand().equals("rowskip")) {
			stringRowsToSkip = jTextFieldNbRowsToSkip.getText();
			// table has to be updated
			booleanIsParsingChanged = true;
		}

		// sets name which mapping list will be used
		if (ae.getActionCommand().equals("mapping")) {
			JComboBox cb = (JComboBox) ae.getSource();
			stringMappingName = (String) cb.getSelectedItem();
		}

		// the next three parts control the program flow
		if (ae.getActionCommand().equals("back")) {
			intWhichJPanelIsActive--;
		}

		if (ae.getActionCommand().equals("next")) {
			intWhichJPanelIsActive++;
		}

		if (ae.getActionCommand().equals("abort")) {
			if (parent == null) {
				// case when running in OVTK2Desktop mode
				try {
					this.setClosed(true);
				} catch (PropertyVetoException ve) {
					ErrorDialog.show(ve);
				}
			} else {
				// running in applet mode within a JDesktopPane within a JFrame
				parent.setVisible(false);
			}
		}

		// show first step
		if (intWhichJPanelIsActive == 1) {
			// clear up
			contentPane.removeAll();
			// make JPanel
			JPanel first = stepFirst();
			// add and update it to be shown exactly
			contentPane.add(first);
			first.updateUI();
			contentPane.repaint();
			jComboBoxUseFirstLineAsTableHeader.updateUI();
		}

		// show second step
		if (intWhichJPanelIsActive == 2) {
			// set the filename
			stringFilename = jTextFieldFilename.getText();
			// clear up
			contentPane.removeAll();
			// make JPanel
			JPanel second = stepSecond();
			// add and update it to be shown exactly
			contentPane.add(second);
			second.updateUI();
			contentPane.repaint();
		}

		// show third step
		if (intWhichJPanelIsActive == 3) {
			// clear up
			contentPane.removeAll();
			// make JPanel
			JPanel third = stepThird();
			// add and update it to be shown exactly
			contentPane.add(third);
			third.updateUI();
			contentPane.repaint();
		}

		/*
		 * this part handles the checking of the last step before this program
		 * return something this is important to guarantee the minimum of labled
		 * data which will be returned to the calling application
		 */
		if (intWhichJPanelIsActive == 4) {

			// get all necessary options for the conditions
			Hashtable<String, String> options = globalConfiguration.get("conditions");

			// create a hashtable of all required values
			Hashtable<String, Boolean> required = new Hashtable<String, Boolean>();

			String message = ""; // error message for required conditions

			if (options.get("required") != null) {

				String[] temp = ((String) options.get("required")).split("\\s");

				for (int i = 0; i < temp.length; i++) {
					required.put(temp[i], new Boolean(false));
					message = message + temp[i];
					if (i < temp.length - 2)
						message = message + ", ";
					if (i == temp.length - 2)
						message = message + " and ";
				}
			}
			// get all names for the required conditions
			// Hashtable cells = (Hashtable) globalConfiguration.get("cells");

			// get datamodel of last table
			TableModel data = jTableThirdStep.getModel();

			// count up all set describtions
			Hashtable<Object, Integer> useDesc = new Hashtable<Object, Integer>();
			for (int i = 0; i < data.getColumnCount(); i++) {
				Integer nb = useDesc.get(data.getValueAt(0, i));
				int nbs = 1;
				// if it is set yet count up
				if (nb != null)
					nbs = nb + 1;
				useDesc.put(data.getValueAt(0, i), new Integer(nbs));
			}

			// check for duplications and conditions
			boolean error = false;
			Enumeration<Object> keys = useDesc.keys();
			while (keys.hasMoreElements()) {
				Object key = keys.nextElement();

				// get occurrency number of desc
				int nbs = useDesc.get(key);

				// set condition states
				String strkey = (String) key;
				if (required.get(strkey) != null) {
					required.remove(strkey);
					required.put(strkey, new Boolean(true));
				}

				// check here for duplication, exception for value
				if (!strkey.equals("none") && !strkey.equals("value") && nbs > 1) {
					JOptionPane.showMessageDialog(this, "Doubled choose of a unique description " + strkey, "Error", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}

			// check if conditions are complied
			boolean complied = true;
			Enumeration<String> keys2 = required.keys();
			while (keys2.hasMoreElements()) {
				Object key = keys2.nextElement();
				String strkey = (String) key;
				complied = complied && required.get(strkey);
			}

			// if something required is missing
			if (!error && !complied) {
				JOptionPane.showMessageDialog(this, message + " have to be used at least", "Error", JOptionPane.ERROR_MESSAGE);
				error = true;
			}

			// if an error occurred return to previous selection else process
			if (!error) {
				prepareReturnContent();
				if (parent == null) {
					// case when running in OVTK2Desktop mode
					try {
						this.setClosed(true);
					} catch (PropertyVetoException ve) {
						ErrorDialog.show(ve);
					}
				} else {
					// running in applet mode within a JDesktopPane within a
					// JFrame
					parent.setVisible(false);
				}
				// sets everything to start state
				intWhichJPanelIsActive = 1;
				// clear up
				contentPane.removeAll();
				// make JPanel
				JPanel first = stepFirst();
				// add and update it to be shown exactly
				contentPane.add(first);
				first.updateUI();
				contentPane.repaint();
				jComboBoxUseFirstLineAsTableHeader.updateUI();
			} else {
				intWhichJPanelIsActive--;
			}
		}

	}

	/**
	 * make some buttons to control program flow
	 * 
	 * @param pane
	 *            JPanel where button are added to
	 * @param nb
	 *            which step is active
	 */
	private void addButtonsToJPanel(JPanel pane, int nb) {

		// use GridBagLayout
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		pane.setLayout(gridbag);

		// create buttons
		JButton back = new JButton("back");
		JButton next = new JButton("next");
		JButton abort = new JButton("abort");

		// set accessibility properties
		back.setActionCommand("back");
		next.setActionCommand("next");
		abort.setActionCommand("abort");

		back.setMnemonic(KeyEvent.VK_B);
		next.setMnemonic(KeyEvent.VK_N);
		abort.setMnemonic(KeyEvent.VK_A);

		// add actionlistener
		back.addActionListener(this);
		next.addActionListener(this);
		abort.addActionListener(this);

		// check current state
		if (nb == 1)
			back.setEnabled(false);
		if (nb == 3 && vectorReturnContent.size() == 0)
			next.setEnabled(false);

		// now a couple of formating strings follows
		back.setPreferredSize(new Dimension(100, 25));
		next.setPreferredSize(new Dimension(100, 25));
		abort.setPreferredSize(new Dimension(100, 25));

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 1;
		gridbag.setConstraints(back, c);
		pane.add(back);

		c.gridx = 2;
		gridbag.setConstraints(next, c);
		pane.add(next);

		c.gridx = 3;
		gridbag.setConstraints(abort, c);
		pane.add(abort);
	}

	/**
	 * calculates the optimal width of a given table by summing the maximal
	 * width of each column
	 * 
	 * @param table
	 *            table which should be calculated
	 */
	private void calcColumnWidths(JTable table) {

		// get table header and renderer
		JTableHeader header = table.getTableHeader();
		TableCellRenderer defaultHeaderRenderer = null;
		if (header != null)
			defaultHeaderRenderer = header.getDefaultRenderer();

		TableColumnModel columns = table.getColumnModel();
		TableModel data = table.getModel();

		int margin = columns.getColumnMargin();
		int rowCount = data.getRowCount();
		int totalWidth = 0;
		// go through all columns
		for (int i = columns.getColumnCount() - 1; i >= 0; --i) {
			// get current column
			TableColumn column = columns.getColumn(i);
			int columnIndex = column.getModelIndex();
			int width = -1;

			// get header for current column
			TableCellRenderer h = column.getHeaderRenderer();
			if (h == null)
				h = defaultHeaderRenderer;
			if (h != null) { // Not explicitly impossible
				Component c = h.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, -1, i);
				// get header width
				width = (int) (c.getPreferredSize().width * 1.1);
			}

			// go through all rows
			for (int row = rowCount - 1; row >= 0; --row) {
				TableCellRenderer r = table.getCellRenderer(row, i);
				Component c = r.getTableCellRendererComponent(table, data.getValueAt(row, columnIndex), false, false, row, i);
				// max of header width and actual component width
				width = Math.max(width, c.getPreferredSize().width);
			}

			if (width >= 0)
				column.setPreferredWidth(width + margin);
			// setting and summing preferred width of each column
			totalWidth += column.getPreferredWidth();
		}
		// set width to table
		Dimension size = table.getPreferredScrollableViewportSize();
		size.width = totalWidth;
		table.setPreferredScrollableViewportSize(size);
	}

	/**
	 * only returns the resulting vector
	 * 
	 * @return constructed return vector
	 */
	public Vector<Object> getReturnContent() {
		if (finished)
			return vectorReturnContent;
		else
			return new Vector<Object>();
	}

	/**
	 * Opens a file and parses its content into a special vector
	 * 
	 * @param preViewEnabled
	 *            sets a cutoffs to how many lines are used
	 * @return ResultContainer for tablemodel
	 */
	private ResultContainer openFile(boolean preViewEnabled) {

		String separator = "\t";
		String linecontent;
		String[] splitedline;
		int imax = 0;
		int lineread = 0;

		// create content vectors
		ArrayList<ArrayList<Object>> precontent = new ArrayList<ArrayList<Object>>();

		// set line pre selection
		int toSkip = Integer.parseInt(stringRowsToSkip);
		int linesPreView = 5;
		if (booleanFirstLineAsTableHeader) {
			linesPreView++;
		}

		// set the separator
		if (intWhichSeparatorToUse == 0)
			separator = "\t";
		if (intWhichSeparatorToUse == 1)
			separator = " ";
		if (intWhichSeparatorToUse == 2)
			separator = ",";

		// compile state machine
		Pattern p = Pattern.compile(".+", Pattern.UNIX_LINES);

		// check for valid filename
		if (stringFilename == null || stringFilename.equals(""))
			return null;
		if (stringFilename.length() > 0) {
			try {
				// open file
				BufferedReader in = new BufferedReader(new FileReader(stringFilename));
				while (in.ready()) {
					// go through the file
					linecontent = in.readLine();
					Matcher m = p.matcher(linecontent);
					if (m.matches()) { // process regexp
						if (lineread >= toSkip) {
							// split lines
							splitedline = linecontent.split(separator);
							ArrayList<Object> temp = new ArrayList<Object>();
							// add column to vector
							if (!preViewEnabled)
								temp.add(new Boolean(true));
							temp.addAll(new ArrayList<Object>(Arrays.asList(splitedline)));
							// get the maximum of columns
							if (splitedline.length + 1 > imax)
								imax = splitedline.length + 1;
							precontent.add(temp);
						}
						// check process state
						if (lineread - toSkip > linesPreView && preViewEnabled)
							break;
						lineread++;
					}
				}
				in.close();
			} catch (Exception e) {
				// error handling
				System.out.println(e);
				JOptionPane.showMessageDialog(this, "An error occurred while trying to read the file " + stringFilename, "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (preViewEnabled)
			imax--; // skip first column with boolean if preview

		// create headers for the table
		columnNames.clear();
		if (booleanFirstLineAsTableHeader) {
			// copy column names from values of first line
			Iterator<Object> it = precontent.get(0).iterator();
			while (it.hasNext()) {
				columnNames.add(it.next().toString());
			}
			precontent.remove(0);
			// default header for first column
			if (!preViewEnabled)
				columnNames.set(0, "use");

			// if some header (first row short than another) is missing assign
			// n/a
			for (int i = columnNames.size(); i < imax; i++) {
				columnNames.add("n/a");
			}
		} else {
			// automated header creation
			if (!preViewEnabled) {
				columnNames.add("use");
				for (int i = 1; i < imax; i++) {
					columnNames.add("col" + i);
				}
			} else {
				for (int i = 0; i < imax; i++) {
					columnNames.add("col" + i);
				}
			}

		}

		// first row for column selection
		if (!preViewEnabled) {
			ArrayList<Object> selection = new ArrayList<Object>();
			selection.add(null); // first row, first column keep empty
			for (int i = 1; i < imax; i++) {
				selection.add(new Boolean(true));
			}
			precontent.add(0, selection); // add it as first row
		}

		ResultContainer total = new ResultContainer();
		total.columnNames = columnNames;
		total.data = precontent;

		return total;
	}

	/**
	 * opens mapping file and parses its content into hashmap
	 * 
	 */
	private Map<String, String> parseMappingFile(Vector<Vector<Object>> data, Map<String, List<Integer>> index) {

		// get all necessary options
		Hashtable<String, String> mappings = globalConfiguration.get("mappings");

		// init matchTableTemp and path string
		Hashtable<String, String> matchTableTemp = new Hashtable<String, String>();
		String path = null;

		// get selected filename or use default
		if (stringMappingName != null) {
			path = mappings.get(stringMappingName);
		} else {
			path = mappings.get("default");
		}

		// what todo if no mapping file should be used
		if (path.equals("none")) {
			return null;
		}

		// try to open file
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException fnfex) {
			fnfex.printStackTrace();
			return null;
		}

		// kill comment lines ^^
		String s = null;
		try {
			while (true) {
				s = bufferedReader.readLine();
				if (s.charAt(0) != '#')
					break;
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return null;
		}

		// this parses the first line which isnt comment
		StringTokenizer tokenizer = new StringTokenizer(s, "\t");
		String key = tokenizer.nextToken();
		String value = tokenizer.nextToken();
		matchTableTemp.put(key, value);

		// do parsing, file has to be tab-seperated
		try {
			while (true) {
				s = bufferedReader.readLine();
				if (s == null)
					break;
				tokenizer = new StringTokenizer(s, "\t");
				key = tokenizer.nextToken();
				value = tokenizer.nextToken();
				matchTableTemp.put(key, value);
			}
			bufferedReader.close();
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return null;
		}
		// System.out.println(matchTableTemp);

		// init return matchTable
		Map<String, String> matchTable = new HashMap<String, String>();
		String bCode = "";
		for (int i = 0; i < data.size(); i++) {
			bCode = data.get(i).get(index.get("cacc").get(0)).toString();
			if (matchTableTemp.get(bCode) != null) {
				// key - value inverted for compatiblity issues
				matchTable.put(matchTableTemp.get(bCode), bCode);
			}
		}
		System.out.println("entries in match file: " + matchTableTemp.size() + " vs. entries in microarray file: " + data.size() + " = total mapping entries: " + matchTable.size());

		return matchTable;
	}

	/**
	 * prepares some content specification and calls mediator
	 * 
	 */
	private void prepareReturnContent() {

		// desc set by user
		List<String> headers = new ArrayList<String>();
		Map<String, List<Integer>> index = new Hashtable<String, List<Integer>>();

		// array contains column selection state
		int nbCols = jTableThirdStep.getColumnCount();
		boolean colselect[] = new boolean[nbCols];

		// collect desc from table
		int count = 0;
		for (int i = 0; i < nbCols; i++) {
			String heading = (String) jTableThirdStep.getValueAt(0, i);
			if (!heading.equals("none")) {
				// multiple headings for value
				if (!index.containsKey(heading))
					index.put(heading, new ArrayList<Integer>());
				index.get(heading).add(Integer.valueOf(count));
				if (heading.equals("value"))
					headers.add(jTableThirdStep.getColumnName(i));
				colselect[i] = true;
				// increase position in result row
				count++;
			}
		}

		// compute the new table data according to selection
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		for (int i = 0; i < vectorReturnContent.size(); i++) {
			@SuppressWarnings("rawtypes")
			Vector tmp = (Vector) vectorReturnContent.get(i);
			// compute data row-wise
			Vector<Object> tmp2 = new Vector<Object>();
			// check for each column of the row
			for (int j = 0; j < tmp.size(); j++) {
				if (colselect[j] == true) {
					tmp2.add(tmp.get(j)); // add data to row
				}
			}
			data.add(tmp2); // add row to table data
		}

		// compute returnContent
		Vector<Object> tmp = new Vector<Object>();
		tmp.add(headers);
		tmp.add(data);
		tmp.add(index);
		tmp.add(parseMappingFile(data, index));

		// set returnContent
		vectorReturnContent = tmp;
		finished = true;
	}

	/**
	 * show standard file chooser
	 * 
	 */
	private void showFileChooser() {

		final JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
		int returnVal = fc.showOpenDialog(this);

		// set global filename
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			stringFilename = fc.getSelectedFile().getPath();
		}
	}

	/**
	 * creates first step of user interaction, sets GUI elements
	 * 
	 * get the filename and some parsing options from the user
	 * 
	 * @return fully constracted JPanel
	 */
	private JPanel stepFirst() {

		// begin new layout
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		// create JPanel which should be returned later
		JPanel pane = new JPanel(null);
		pane.setMinimumSize(new Dimension(480, 350));
		pane.setLayout(gridbag);

		// get all necessary options to fill the text labels
		Hashtable<String, String> options = globalConfiguration.get("first");

		// this is the headline
		JLabel head = new JLabel();
		Font fontHead = new Font("Monospaced", Font.BOLD, 24);
		head.setFont(fontHead);
		head.setForeground(Color.BLUE);
		head.setText((String) options.get("head"));

		// this textbox contains all description for this step
		String text = (String) options.get("text");
		Font fontText = new Font("Helvetica", Font.BOLD, 18);
		JTextArea textArea = new JTextArea(text);
		textArea.setBackground(ImportWizard.this.getBackground());
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(fontText);
		/*
		 * the width of this textarea is controlled by the config, thats because
		 * some line wrapping matter which sometimes causes letters to have a
		 * part of them cutted off
		 */
		int xSize = Integer.parseInt((String) options.get("textsize"));
		int ySize = (int) textArea.getPreferredSize().getHeight() * 2;
		// this element has to be resize invariant
		textArea.setMinimumSize(new Dimension(xSize, ySize));
		textArea.setMaximumSize(new Dimension(xSize, ySize));
		textArea.setPreferredSize(new Dimension(xSize, ySize));

		// the separator height should alway be 3 pixel
		JSeparator separator = new JSeparator();
		separator.setMinimumSize(new Dimension(1, 3));

		// only decription text
		JLabel label4 = new JLabel("Filename");
		label4.setForeground(Color.BLUE);

		// create input field for the filename
		jTextFieldFilename = new JTextField();
		jTextFieldFilename.setText(stringFilename);
		jTextFieldFilename.setActionCommand("filechange");
		jTextFieldFilename.addActionListener(this);

		// some filechooser will be called by this button
		JButton button = new JButton("...");
		button.setActionCommand("chooser");
		button.addActionListener(this);

		// only description text
		JLabel label5 = new JLabel("Choose seperator");
		label5.setForeground(Color.BLUE);

		// create the radio buttons for separator choosing
		JRadioButton tabButton = new JRadioButton("tabulator");
		tabButton.setMnemonic(KeyEvent.VK_T);
		tabButton.setActionCommand("tabulator");
		// get the current state
		if (intWhichSeparatorToUse == 0)
			tabButton.setSelected(true);

		JRadioButton spaceButton = new JRadioButton("space");
		spaceButton.setMnemonic(KeyEvent.VK_S);
		spaceButton.setActionCommand("space");
		// get the current state
		if (intWhichSeparatorToUse == 1)
			spaceButton.setSelected(true);

		JRadioButton commaButton = new JRadioButton("comma");
		commaButton.setMnemonic(KeyEvent.VK_K);
		commaButton.setActionCommand("comma");
		// get the current state
		if (intWhichSeparatorToUse == 2)
			commaButton.setSelected(true);

		// group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(tabButton);
		group.add(spaceButton);
		group.add(commaButton);

		// register a listener for the radio buttons.
		tabButton.addActionListener(this);
		spaceButton.addActionListener(this);
		commaButton.addActionListener(this);

		// only description text
		JLabel label6 = new JLabel("Skip comment rows");
		label6.setForeground(Color.BLUE);

		/*
		 * this inputfield is for the number of rows, which should be skipped
		 * while reading the original document this is because these lines could
		 * contain some comments or anything like that
		 */
		jTextFieldNbRowsToSkip = new JTextField();
		jTextFieldNbRowsToSkip.setHorizontalAlignment(JTextField.RIGHT);
		jTextFieldNbRowsToSkip.setText(stringRowsToSkip);
		jTextFieldNbRowsToSkip.setActionCommand("rowskip");
		jTextFieldNbRowsToSkip.addActionListener(this);
		// this element has to be resize invariant
		jTextFieldNbRowsToSkip.setMinimumSize(new Dimension(25, 20));
		jTextFieldNbRowsToSkip.setMaximumSize(new Dimension(25, 20));
		jTextFieldNbRowsToSkip.setPreferredSize(new Dimension(25, 20));

		// only description text
		JLabel label7 = new JLabel("Use first line as table header");
		label7.setForeground(Color.BLUE);

		/*
		 * this lets the user choose whether or not to use the first line after
		 * the rows to skip as the tabels header, so the user can create its own
		 * cols headings
		 */
		String[] yesno = { "no", "yes" }; // for better accessibility
		jComboBoxUseFirstLineAsTableHeader = new JComboBox(yesno);
		// get current state
		if (booleanFirstLineAsTableHeader) {
			jComboBoxUseFirstLineAsTableHeader.setSelectedIndex(1);
		}
		jComboBoxUseFirstLineAsTableHeader.setActionCommand("lineheader");
		jComboBoxUseFirstLineAsTableHeader.addActionListener(this);

		/*
		 * this table is for preview prupose only, so it contains a smaller
		 * number of rows than the original document
		 */
		if (booleanIsParsingChanged == true) {
			// parse the file given by stringFilename
			ResultContainer content = openFile(true);

			// create a table using a simple vector enabled table model
			MyTableModel myModel = new MyTableModel(content);
			jTableFirstStep = new JTable(myModel);

			// Set the font a little bit higher.
			jTableFirstStep.setFont(new Font("Default", Font.PLAIN, 16));
			jTableFirstStep.setRowHeight(23);

			// now table editing should be prohibited
			jTableFirstStep.setRowSelectionAllowed(false);
			jTableFirstStep.setColumnSelectionAllowed(false);
			jTableFirstStep.setCellSelectionEnabled(false);
			// perfectly fit the content into the table
			calcColumnWidths(jTableFirstStep);
			/*
			 * if contentwidth is greater than the minimal width of the table
			 * disable the auto resizing function of the table and use the
			 * scrollbars of the scrollpane containing this table instead
			 */
			if (jTableFirstStep.getPreferredSize().width > 460) {
				jTableFirstStep.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			}
		}

		// put the table from above into a scrollpane
		JScrollPane scrollPane = new JScrollPane(jTableFirstStep);

		// here to back,abort,next buttons are added
		JPanel buttons = new JPanel();
		addButtonsToJPanel(buttons, 1);

		/*
		 * the following section contains a lot of formating primativs to enable
		 * a resizing of all the elements above using GridBagLayout
		 */
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5, 10, 0, 0);
		c.weightx = 0.0;
		c.gridwidth = 7;
		c.gridx = 1;
		c.gridy = 1;
		gridbag.setConstraints(head, c);
		pane.add(head);

		c.insets = new Insets(5, 25, 0, 0);
		c.gridwidth = 7;
		c.gridx = 1;
		c.gridy = 2;
		gridbag.setConstraints(textArea, c);
		pane.add(textArea);

		c.insets = new Insets(10, 0, 0, 0);
		c.gridwidth = 9;
		c.weightx = 1.0;
		c.gridy = 3;
		gridbag.setConstraints(separator, c);
		pane.add(separator);

		c.insets = new Insets(15, 10, 0, 2);
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.gridx = 1;
		c.gridy = 4;
		gridbag.setConstraints(label4, c);
		pane.add(label4);

		c.insets = new Insets(15, 3, 0, 3);
		c.gridwidth = 7;
		c.weightx = 0.8;
		c.gridx = 2;
		gridbag.setConstraints(jTextFieldFilename, c);
		pane.add(jTextFieldFilename);

		c.insets = new Insets(15, 0, 0, 15);
		c.gridwidth = 1;
		c.gridx = 9;
		c.weightx = 0.0;
		gridbag.setConstraints(button, c);
		pane.add(button);

		c.insets = new Insets(3, 10, 0, 0);
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 2;

		gridbag.setConstraints(label5, c);
		pane.add(label5);

		c.insets = new Insets(3, 0, 0, 0);
		c.gridwidth = 2;
		c.gridx = 3;
		gridbag.setConstraints(tabButton, c);
		pane.add(tabButton);

		c.gridx = 5;
		c.gridwidth = 1;
		gridbag.setConstraints(spaceButton, c);
		pane.add(spaceButton);

		c.gridx = 6;
		gridbag.setConstraints(commaButton, c);
		pane.add(commaButton);

		c.insets = new Insets(3, 10, 0, 0);
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 6;
		gridbag.setConstraints(label6, c);
		pane.add(label6);

		c.insets = new Insets(3, 5, 0, 0);
		c.gridwidth = 1;
		c.gridx = 3;
		gridbag.setConstraints(jTextFieldNbRowsToSkip, c);
		pane.add(jTextFieldNbRowsToSkip);

		c.gridwidth = 2;
		c.gridx = 5;
		gridbag.setConstraints(label7, c);
		pane.add(label7);

		c.gridwidth = 1;
		c.gridx = 7;
		gridbag.setConstraints(jComboBoxUseFirstLineAsTableHeader, c);
		pane.add(jComboBoxUseFirstLineAsTableHeader);

		c.insets = new Insets(3, 10, 0, 10);
		c.gridwidth = 9;
		c.weighty = 0.8;
		c.gridx = 1;
		c.gridy = 7;
		gridbag.setConstraints(scrollPane, c);
		pane.add(scrollPane);

		c.weighty = 0.0;
		c.gridy = 8;
		gridbag.setConstraints(buttons, c);
		pane.add(buttons);

		return pane;
	}

	/**
	 * creates second step of user interaction, sets GUI elements
	 * 
	 * lets the user select which cols and rows should be used
	 * 
	 * @return fully constracted JPanel
	 */
	private JPanel stepSecond() {

		// create new GridBayLayout
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		// create new JPanel which should be returned at the end
		JPanel pane = new JPanel(null);
		// set the minimal visible size for resize matter
		pane.setMinimumSize(new Dimension(480, 350));
		pane.setLayout(gridbag);

		// get all necessary options
		Hashtable<String, String> options = globalConfiguration.get("second");

		// this is the headline
		JLabel head = new JLabel();
		Font fontHead = new Font("Monospaced", Font.BOLD, 24);
		head.setFont(fontHead);
		head.setForeground(Color.BLUE);
		head.setText((String) options.get("head"));

		// this textarea contains the description for the headline
		String text = (String) options.get("text");
		Font fontText = new Font("Helvetica", Font.BOLD, 18);
		JTextArea textArea = new JTextArea(text);
		textArea.setBackground(ImportWizard.this.getBackground());
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(fontText);
		// the textarea width is set according to the config for line wrapping
		// prupose
		int xSize = Integer.parseInt((String) options.get("textsize"));
		int ySize = (int) textArea.getPreferredSize().getHeight() * 2;
		// this element should be resize invariant
		textArea.setMinimumSize(new Dimension(xSize, ySize));
		textArea.setMaximumSize(new Dimension(xSize, ySize));
		textArea.setPreferredSize(new Dimension(xSize, ySize));

		// a separator with height 3 Pixel
		JSeparator separator = new JSeparator();
		separator.setMinimumSize(new Dimension(1, 3));

		// check for new table content
		if (booleanIsParsingChanged) {
			// get content from file
			ResultContainer content = openFile(false);
			// create a vector based table model from the content
			if (content != null)
				myTableModel = new MyTableModel(content);
			booleanIsParsingChanged = false;
		}

		// set some attributes of the table
		if (myTableModel != null) {
			// create a new table with specific properties
			jTableSecondStep = new MyJTable(myTableModel, false);

			// Set the font a little bit higher.
			jTableSecondStep.setFont(new Font("Default", Font.PLAIN, 16));
			jTableSecondStep.setRowHeight(23);

			// now table editing should be prohibited
			jTableSecondStep.setRowSelectionAllowed(false);
			jTableSecondStep.setColumnSelectionAllowed(false);
			jTableSecondStep.setCellSelectionEnabled(false);
			// perfectly fit the content into the table
			calcColumnWidths(jTableSecondStep);
			/*
			 * if contentwidth is greater than the minimal width of the table
			 * disable the auto resizing function of the table and use the
			 * scrollbars of the scrollpane containing this table instead
			 */
			if (jTableSecondStep.getPreferredSize().width > 460) {
				jTableSecondStep.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			}
		}

		// here to back,abort,next buttons are added
		JPanel buttons = new JPanel();
		addButtonsToJPanel(buttons, 2);

		/*
		 * the following section contains a lot of formating primativs to enable
		 * a resizing of all the elements above using GridBagLayout
		 */
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5, 10, 0, 0);
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.gridx = 1;
		c.gridy = 1;
		gridbag.setConstraints(head, c);
		pane.add(head);

		c.insets = new Insets(5, 25, 0, 0);
		c.gridy = 2;
		gridbag.setConstraints(textArea, c);
		pane.add(textArea);

		JLabel dummyLabel = new JLabel(" ");

		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = 1;
		c.weightx = 1.0;
		c.gridx = 2;
		c.gridy = 2;
		gridbag.setConstraints(dummyLabel, c);
		pane.add(dummyLabel);

		c.insets = new Insets(10, 0, 0, 0);
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.gridx = 1;
		c.gridy = 3;
		gridbag.setConstraints(separator, c);
		pane.add(separator);

		JScrollPane scrollPane = new JScrollPane(jTableSecondStep);

		c.insets = new Insets(3, 10, 0, 10);
		c.weighty = 0.8;
		c.gridy = 4;
		gridbag.setConstraints(scrollPane, c);
		pane.add(scrollPane);

		c.weighty = 0.0;
		c.gridy = 5;
		gridbag.setConstraints(buttons, c);
		pane.add(buttons);

		return pane;
	}

	/**
	 * creates third step of user interaction, sets GUI elements
	 * 
	 * lets the user label each row and chooses a mapping
	 * 
	 * @return fully constracted JPanel
	 */
	@SuppressWarnings("rawtypes")
	private JPanel stepThird() {

		// create new GridBayLayout
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		// create new JPanel which should be returned at the end
		JPanel pane = new JPanel(null);
		// set the minimal visible size for resize matter
		pane.setMinimumSize(new Dimension(480, 350));
		pane.setLayout(gridbag);

		// get all necessary options
		Hashtable<String, String> options = globalConfiguration.get("third");

		// this is the headline
		JLabel head = new JLabel();
		Font fontHead = new Font("Monospaced", Font.BOLD, 24);
		head.setFont(fontHead);
		head.setForeground(Color.BLUE);
		head.setText((String) options.get("head"));

		// this textarea contains the description for the headline
		String text = (String) options.get("text");
		Font fontText = new Font("Helvetica", Font.BOLD, 18);
		JTextArea textArea = new JTextArea(text);
		textArea.setBackground(ImportWizard.this.getBackground());
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(fontText);
		// the textarea width is set according to the config for line wrapping
		// prupose
		int xSize = Integer.parseInt((String) options.get("textsize"));
		int ySize = (int) textArea.getPreferredSize().getHeight() * 2;
		// this element should be resize invariant
		textArea.setMinimumSize(new Dimension(xSize, ySize));
		textArea.setMaximumSize(new Dimension(xSize, ySize));
		textArea.setPreferredSize(new Dimension(xSize, ySize));

		// a separator with height 3 Pixel
		JSeparator separator = new JSeparator();
		separator.setMinimumSize(new Dimension(1, 3));

		/*
		 * this lets the user choose what mapping file should be concerned to
		 * map spot ids on genenames, selection will be processed later and
		 * results in mapping vector
		 */
		JLabel label = new JLabel("Choose mapping");
		label.setForeground(Color.BLUE);
		// get all necessary options
		Hashtable<String, String> mappings = globalConfiguration.get("mappings");
		String mappingNames[] = new String[mappings.size()];
		String tooltips[] = new String[mappings.size()];
		int countElements = 0;
		for (Enumeration<String> e = mappings.keys(); e.hasMoreElements();) {
			String keyname = e.nextElement();
			mappingNames[countElements] = keyname;
			tooltips[countElements] = (String) mappings.get(keyname);
			countElements++;
		}
		// chooser for mapping spotid -> genename
		jComboBoxSelectMapping = new JComboBox(mappingNames);
		jComboBoxSelectMapping.setActionCommand("mapping");
		jComboBoxSelectMapping.setRenderer(new MyComboBoxRenderer(tooltips));
		jComboBoxSelectMapping.addActionListener(this);

		// myTableModel was created in the secons step and contains all data
		if (myTableModel != null) {
			ResultContainer content = myTableModel.getContent();

			// get headings and table content
			ArrayList<String> headings = content.columnNames;
			ArrayList<ArrayList<Object>> precontent = content.data;
			// first row with contains column selection state
			ArrayList<Object> colselect = precontent.get(0);

			// create new table headings and a new first row
			Vector<String> columnNames = new Vector<String>();
			Vector<String> desc = new Vector<String>();
			for (int i = 1; i < headings.size(); i++) {
				// process only selected columns
				if ((Boolean) colselect.get(i) == true) {
					columnNames.add(headings.get(i));
					desc.add(new String("none")); // default String do not
					// change this
				}
			}

			// compute the new table data according to selection
			Vector<Object> data = new Vector<Object>();
			data.add(desc); // add first row
			for (int i = 1; i < precontent.size(); i++) {
				ArrayList<Object> tmp = precontent.get(i);
				// compute data rowwise
				if ((Boolean) tmp.get(0) == true) {
					Vector<Object> tmp2 = new Vector<Object>();
					// check for each column of the row
					for (int j = 1; j < tmp.size(); j++) {
						// check if this column is selected
						if ((Boolean) colselect.get(j) == true) {
							tmp2.add(tmp.get(j)); // create new row
						}
					}
					data.add(tmp2); // add row to table data
				}
			}

			// make a copy without the describtion row
			vectorReturnContent = (Vector) data.clone();
			vectorReturnContent.remove(0);

			// create a new table with specific properties
			jTableThirdStep = new MyJTable(data, columnNames, true);

			// Set the font a little bit higher.
			jTableThirdStep.setFont(new Font("Default", Font.PLAIN, 16));
			jTableThirdStep.setRowHeight(23);

			// now table editing should be prohibited
			jTableThirdStep.setRowSelectionAllowed(false);
			jTableThirdStep.setColumnSelectionAllowed(false);
			jTableThirdStep.setCellSelectionEnabled(false);
			// perfectly fit the content into the table
			calcColumnWidths(jTableThirdStep);
			/*
			 * if contentwidth is greater than the minimal width of the table
			 * disable the auto resizing function of the table and use the
			 * scrollbars of the scrollpane containing this table instead
			 */
			if (jTableThirdStep.getPreferredSize().width > 460) {
				jTableThirdStep.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			}

			Vector firstRow = (Vector) vectorReturnContent.get(0);
			for (int i = 0; i < firstRow.size(); i++) {
				try {
					Double.parseDouble(firstRow.get(i).toString());
					DefaultCellEditor cellEditor = (DefaultCellEditor) jTableThirdStep.getCellEditor(0, i);
					JComboBox box = (JComboBox) cellEditor.getComponent();
					// assumes the last selection to be for value
					jTableThirdStep.setValueAt(box.getItemAt(box.getItemCount() - 1), 0, i);
				} catch (NumberFormatException nfe) {
					continue;
				}
			}
			// for OVTK-334 set first tow columns as cname and cacc
			DefaultCellEditor cellEditor = (DefaultCellEditor) jTableThirdStep.getCellEditor(0, 0);
			JComboBox box = (JComboBox) cellEditor.getComponent();
			jTableThirdStep.setValueAt(box.getItemAt(2), 0, 0);
			jTableThirdStep.setValueAt(box.getItemAt(1), 0, 1);
		}

		// here to back,abort,next buttons are added
		JPanel buttons = new JPanel();
		addButtonsToJPanel(buttons, 3);

		/*
		 * the following section contains a lot of formating primativs to enable
		 * a resizing of all the elements above using GridBagLayout
		 */
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5, 10, 0, 0);
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.gridx = 1;
		c.gridy = 1;
		gridbag.setConstraints(head, c);
		pane.add(head);

		c.insets = new Insets(5, 25, 0, 0);
		c.gridy = 2;
		gridbag.setConstraints(textArea, c);
		pane.add(textArea);

		JLabel dummyLabel = new JLabel(" ");

		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = 1;
		c.weightx = 1.0;
		c.gridx = 3;
		c.gridy = 2;
		gridbag.setConstraints(dummyLabel, c);
		pane.add(dummyLabel);

		c.insets = new Insets(10, 0, 0, 0);
		c.gridwidth = 4;
		c.weightx = 0.0;
		c.gridx = 1;
		c.gridy = 3;
		gridbag.setConstraints(separator, c);
		pane.add(separator);

		c.insets = new Insets(3, 10, 0, 2);
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.gridx = 1;
		c.gridy = 4;
		gridbag.setConstraints(label, c);
		pane.add(label);

		c.insets = new Insets(3, 3, 0, 10);
		c.gridwidth = 3;
		c.weightx = 0.8;
		c.gridx = 2;
		gridbag.setConstraints(jComboBoxSelectMapping, c);
		pane.add(jComboBoxSelectMapping);

		JScrollPane scrollPane = new JScrollPane(jTableThirdStep);

		c.insets = new Insets(3, 10, 0, 10);
		c.gridwidth = 3;
		c.weightx = 0.0;
		c.weighty = 0.8;
		c.gridx = 1;
		c.gridy = 5;
		gridbag.setConstraints(scrollPane, c);
		pane.add(scrollPane);

		c.weighty = 0.0;
		c.gridy = 6;
		gridbag.setConstraints(buttons, c);
		pane.add(buttons);

		return pane;
	}

}
