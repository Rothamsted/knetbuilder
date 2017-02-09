package net.sourceforge.ondex.ovtk2.ui.toolbars;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.EventListenerList;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.ConceptListUtils;
import net.sourceforge.ondex.ovtk2.util.IntegerStringWrapper;
import net.sourceforge.ondex.ovtk2.util.renderer.HtmlComboBoxRenderer;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

/**
 * Provides the search panel for the OVTK2ToolBar.
 * 
 * @author hindlem
 * @version 14.07.2008
 */
public class MenuGraphSearchBox extends JPanel implements ActionListener, CaretListener {

	/**
	 * Wraps meta data elements for comparison with String
	 * 
	 * @author taubertj
	 * 
	 */
	public static class MetaDataWrapper implements Comparable<Object> {

		final MetaData m;

		final String s;

		/**
		 * Wraps given meta data element.
		 * 
		 * @param m
		 */
		public MetaDataWrapper(MetaData m) {
			this.m = m;
			this.s = m.getFullname().length() > 0 ? m.getFullname() : m.getId();
		}

		@Override
		public int compareTo(Object o) {
			if (o instanceof MetaDataWrapper)
				return s.toUpperCase().compareTo(((MetaDataWrapper) o).s.toUpperCase());
			return 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof MetaDataWrapper))
				return false;
			return m.equals(((MetaDataWrapper) obj).m);
		}

		public String getDescription() {
			return s;
		}

		/**
		 * Returns wrapped meta data.
		 * 
		 * @return MetaData
		 */
		public MetaData getMetaData() {
			return m;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return m.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			if (s.length() > 22) {
				// then trim it to a reasonable size
				return s.substring(0, 22) + "...";
			}
			return s;
		}
	}

	/**
	 * Case sensitive action command
	 */
	private static final String CASE_SENSITIVE = "CASE_SENSITIVE";

	/**
	 * Regex action command
	 */
	private static final String REGEXBUTTON = "REGEX_BUTTON";

	/**
	 * Search action command
	 */
	private static final String SEARCH = "SEARCH";

	/**
	 * Default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * To search case sensitive
	 */
	private JCheckBox caseSensitiveBox;

	/**
	 * ActionCommand needed for OVTK2Desktop
	 */
	private String command;

	/**
	 * Search restrictions concept classes
	 */
	private JComboBox conceptClasses = new JComboBox(new String[] { Config.language.getProperty("ToolBar.Search.ConceptClass") });

	/**
	 * Search restrictions data source
	 */
	private JComboBox dataSources = new JComboBox(new String[] { Config.language.getProperty("ToolBar.Search.DataSource") });

	/**
	 * Keep default text field colour
	 */
	private Color defaultBackColor;

	/**
	 * The important button
	 */
	private JButton goButton;

	/**
	 * To use search string as regex
	 */
	private JCheckBox isRegexBox;

	/**
	 * Observe changes in concept numbers
	 */
	private int lastCount = 0;

	/**
	 * Which viewer to work on
	 */
	private OVTK2PropertiesAggregator lastViewer = null;

	/**
	 * Panel for changing options
	 */
	private JPanel optionsPanel;

	/**
	 * Text field for search String
	 */
	private JTextField searchField;

	/**
	 * Decide search mode
	 */
	private JComboBox searchMode = new JComboBox(new String[] { Config.language.getProperty("ToolBar.Search.Mode.Default"), Config.language.getProperty("ToolBar.Search.Mode.SMILES"), Config.language.getProperty("ToolBar.Search.Mode.InChI"), Config.language.getProperty("ToolBar.Search.Mode.InChIKey"), Config.language.getProperty("ToolBar.Search.Mode.ChEMBL"), Config.language.getProperty("ToolBar.Search.Mode.UniProt") });

	/**
	 * auto completion of user entry
	 */
	private AutoCompleteSupport<?> supportConceptClass;

	/**
	 * auto completion of user entry
	 */
	private AutoCompleteSupport<?> supportDataSource;

	/**
	 * auto completion of user entry
	 */
	private AutoCompleteSupport<?> supportTag;

	/**
	 * Search restrictions concept tags
	 */
	private JComboBox tags = new JComboBox(new String[] { Config.language.getProperty("ToolBar.Search.Tag") });

	/**
	 * Spinner for tanimoto cutoff
	 */
	private JSpinner tanimoto;

	/**
	 * Use ChEMBL for SMILES or InCHI search
	 */
	private JCheckBox useChEMBL = new JCheckBox(Config.language.getProperty("ToolBar.Search.ChEMBL"));

	/**
	 * Initialise layout.
	 */
	public MenuGraphSearchBox() {
		super();
		HtmlComboBoxRenderer renderer = new HtmlComboBoxRenderer();
		conceptClasses.setEnabled(false);
		conceptClasses.setRenderer(renderer);
		dataSources.setEnabled(false);
		dataSources.setRenderer(renderer);
		tags.setEnabled(false);
		tags.setRenderer(renderer);
		initGui();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (SEARCH.equals(e.getActionCommand())) {
			// default mode
			if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.Default"))) {
				// validate regex
				if (!isRegexBox.isSelected() || isValidRegex(searchField.getText())) {
					fireActionPerformed();
				}
			}
			// chemical mode validate SMILE
			else if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.InChIKey"))) {
				fireActionPerformed();
			} else if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.ChEMBL")) && isValidChEMBL(searchField.getText())) {
				fireActionPerformed();
			} else if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.UniProt"))) {
				fireActionPerformed();
			}
		} else if (e.getActionCommand().equals("MODE")) {

			optionsPanel.removeAll();

			// default search mode with two check boxes
			if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.Default"))) {
				optionsPanel.add(isRegexBox);
				optionsPanel.add(caseSensitiveBox);
			}

			// add chemical options for SMILES
			else if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.SMILES"))) {
				optionsPanel.add(tanimoto);
				optionsPanel.add(useChEMBL);
			}

			// with InChi only local search supported
			else if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.InChI"))) {
				optionsPanel.add(tanimoto);
				JCheckBox box = new JCheckBox(Config.language.getProperty("ToolBar.Search.LocalOnly"));
				box.setSelected(true);
				box.setEnabled(false);
				optionsPanel.add(box);
			}

			// with InChiKey only remote search supported
			else if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.InChIKey"))) {
				optionsPanel.add(new JLabel(Config.language.getProperty("ToolBar.Search.RemoteOnly")));
			}

			// with ChEMBL ID only remote search supported
			else if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.ChEMBL"))) {
				optionsPanel.add(new JLabel(Config.language.getProperty("ToolBar.Search.RemoteOnly")));
			}

			// with ChEMBL ID only remote search supported
			else if (searchMode.getSelectedItem().equals(Config.language.getProperty("ToolBar.Search.Mode.UniProt"))) {
				optionsPanel.add(new JLabel(Config.language.getProperty("ToolBar.Search.RemoteOnly")));
			}

			optionsPanel.revalidate();
		}
	}

	/**
	 * Adds the specified action listener to receive action events from this
	 * MenuGraphSearchBox.
	 * 
	 * @param l
	 *            the action listener to be added
	 */
	public synchronized void addActionListener(ActionListener l) {
		listenerList.add(ActionListener.class, l);
	}

	/**
	 * Adds auto complete support to the drop down box.
	 * 
	 * @param list
	 *            list representation of content
	 * @param box
	 *            JComboBox to wrap
	 */
	private void addAutoCompleteSupport(List<Object> list, final JComboBox box) {
		final Object[] elements = list.toArray();

		// request the event-dispatching thread to run certain code
		Runnable doWorkRunnable = new Runnable() {
			public void run() {
				SortedList<Object> sorted = new SortedList<Object>(GlazedLists.eventListOf(elements));

				// auto completion support for selection list
				if (box == tags) {
					if (supportTag == null || !supportTag.isInstalled())
						supportTag = AutoCompleteSupport.install(box, sorted);
					supportTag.setStrict(true);
					supportTag.setFilterMode(TextMatcherEditor.STARTS_WITH);
				} else if (box == dataSources) {
					if (supportDataSource == null || !supportDataSource.isInstalled())
						supportDataSource = AutoCompleteSupport.install(box, sorted);
					supportDataSource.setStrict(true);
					supportDataSource.setFilterMode(TextMatcherEditor.STARTS_WITH);
				} else if (box == conceptClasses) {
					if (supportConceptClass == null || !supportConceptClass.isInstalled())
						supportConceptClass = AutoCompleteSupport.install(box, sorted);
					supportConceptClass.setStrict(true);
					supportConceptClass.setFilterMode(TextMatcherEditor.STARTS_WITH);
				}

				// OVTK-328
				box.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent evt) {
						if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
							goButton.doClick();
						}
					}
				});
				box.revalidate();
			}
		};
		SwingUtilities.invokeLater(doWorkRunnable);
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		JTextField box = ((JTextField) e.getSource());
		// let filler in text field disappear once clicked
		if (box.getText().equals(Config.language.getProperty("ToolBar.Search.Filler"))) {
			box.setText("");
		}

		// valid input InChIKey
		else if (searchMode.getSelectedItem().equals(Config.language.get("ToolBar.Search.Mode.InChIKey"))) {
			// reset background
			box.setBackground(defaultBackColor);
		}

		// valid input ChEMBL id
		else if (searchMode.getSelectedItem().equals(Config.language.get("ToolBar.Search.Mode.ChEMBL"))) {
			if (isValidChEMBL(box.getText())) {
				box.setBackground(defaultBackColor);
			} else {
				box.setBackground(new Color(255, 70, 70)); // light red
			}
		}

		// valid input UniProt id
		else if (searchMode.getSelectedItem().equals(Config.language.get("ToolBar.Search.Mode.UniProt"))) {
			// reset background
			box.setBackground(defaultBackColor);
		}

		// check for valid REGEXP
		else if (isRegexBox.isSelected()) {
			if (isValidRegex(box.getText())) {
				box.setBackground(defaultBackColor);
			} else {
				box.setBackground(new Color(255, 70, 70)); // light red
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created. The listener list
	 * is processed in last to first order.
	 * 
	 * @see EventListenerList
	 */
	protected void fireActionPerformed() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		int modifiers = 0;
		AWTEvent currentEvent = EventQueue.getCurrentEvent();
		if (currentEvent instanceof InputEvent) {
			modifiers = ((InputEvent) currentEvent).getModifiers();
		} else if (currentEvent instanceof ActionEvent) {
			modifiers = ((ActionEvent) currentEvent).getModifiers();
		}
		ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, (command != null) ? command : searchField.getText(), EventQueue.getMostRecentEventTime(), modifiers);

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				((ActionListener) listeners[i + 1]).actionPerformed(e);
			}
		}
	}

	/**
	 * Returns an array of all the <code>ActionListener</code>s added to this
	 * JTextField with addActionListener().
	 * 
	 * @return all of the <code>ActionListener</code>s added or an empty array
	 *         if no listeners have been added
	 * @since 1.4
	 */
	public synchronized ActionListener[] getActionListeners() {
		return listenerList.getListeners(ActionListener.class);
	}

	/**
	 * Return selection of concept class restriction.
	 * 
	 * @return concept class combo box
	 */
	public JComboBox getConceptClasses() {
		return conceptClasses;
	}

	/**
	 * Return selection of data source restriction.
	 * 
	 * @return data source combo box
	 */
	public JComboBox getDataSources() {
		return dataSources;
	}

	/**
	 * Returns the mode of search.
	 * 
	 * @return mode of search
	 */
	public String getSearchMode() {
		return (String) searchMode.getSelectedItem();
	}

	/**
	 * Returns text to search for.
	 * 
	 * @return text to search
	 */
	public String getSearchText() {
		return searchField.getText();
	}

	/**
	 * Return selection of tag restriction.
	 * 
	 * @return tag combo box
	 */
	public JComboBox getTags() {
		return tags;
	}

	/**
	 * Returns the selected tanimoto cut-off
	 * 
	 * @return
	 */
	public int getTanimotoSimilarity() {
		return ((Integer) tanimoto.getModel().getValue()).intValue();
	}

	/**
	 * Returns true if the receiver has an <code>ActionListener</code>
	 * installed.
	 */
	boolean hasActionListener() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create the GUI.
	 */
	private void initGui() {

		// specify group layout
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(false);

		// top line first
		searchMode.setToolTipText(Config.language.getProperty("ToolBar.Search.Mode.ToolTip"));
		searchMode.setActionCommand("MODE");
		searchMode.addActionListener(this);

		// top line
		searchField = new JTextField(Config.language.getProperty("ToolBar.Search.Filler"));
		searchField.addCaretListener(this);
		searchField.addActionListener(this);
		searchField.setActionCommand(SEARCH);
		searchField.setToolTipText(Config.language.getProperty("ToolBar.Search.ToolTip"));

		defaultBackColor = searchField.getBackground();

		// top line
		// JButton goButton = new JButton(Config.language
		// .getProperty("ToolBar.Search.Button"));
		URL url = makeFileURL(Config.ovtkDir, "themes", Config.config.getProperty("Program.Theme"), "icons", "search_11x11.png");
		// System.out.println(url);
		Icon icon = new ImageIcon(url);
		goButton = new JButton(icon);
		goButton.addActionListener(this);
		goButton.setActionCommand(SEARCH);
		goButton.setToolTipText(Config.language.getProperty("ToolBar.Search.Button.ToolTip"));

		// bottom line
		JLabel searchLabel = new JLabel(Config.language.getProperty("ToolBar.Search.RestrictBy"));

		// bottom line
		conceptClasses.setToolTipText(Config.language.getProperty("ToolBar.Search.ConceptClass"));

		// bottom line
		dataSources.setToolTipText(Config.language.getProperty("ToolBar.Search.DataSource"));

		// bottom line
		tags.setToolTipText(Config.language.getProperty("ToolBar.Search.Tag"));

		// flexible options panel
		optionsPanel = new JPanel();
		BoxLayout optionsLayout = new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS);
		optionsPanel.setLayout(optionsLayout);

		// at the right
		isRegexBox = new JCheckBox(Config.language.getProperty("ToolBar.Search.IsRegexButton"));
		isRegexBox.setActionCommand(REGEXBUTTON);
		isRegexBox.setToolTipText(Config.language.getProperty("ToolBar.Search.IsRegexButton.ToolTip"));
		optionsPanel.add(isRegexBox);

		// at the right
		caseSensitiveBox = new JCheckBox(Config.language.getProperty("ToolBar.Search.CaseSensitive"));
		caseSensitiveBox.setActionCommand(CASE_SENSITIVE);
		caseSensitiveBox.setToolTipText(Config.language.getProperty("ToolBar.Search.CaseSensitive.ToolTip"));
		optionsPanel.add(caseSensitiveBox);

		// setup spinner for cutoff
		SpinnerModel model = new SpinnerNumberModel(90, 70, 100, 1);
		tanimoto = new JSpinner(model);
		tanimoto.setToolTipText(Config.language.getProperty("ToolBar.Search.Tanimoto"));

		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.add(searchMode, BorderLayout.WEST);
		searchPanel.add(searchField, BorderLayout.CENTER);
		searchPanel.add(goButton, BorderLayout.EAST);

		// horizontal arrangement
		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(searchPanel).addGroup(layout.createSequentialGroup().addComponent(searchLabel).addComponent(conceptClasses).addComponent(dataSources).addComponent(tags))).addComponent(optionsPanel, 100, 100, 100));

		// vertical arrangement
		layout.setVerticalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(searchPanel)).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(searchLabel).addComponent(conceptClasses).addComponent(dataSources).addComponent(tags))).addComponent(optionsPanel));

	}

	/**
	 * If search should be case sensitive
	 * 
	 * @return
	 */
	public boolean isCaseSensitive() {
		return caseSensitiveBox.isSelected();
	}

	/**
	 * If search term should be treated a regular expression
	 * 
	 * @return
	 */
	public boolean isRegex() {
		return isRegexBox.isSelected();
	}

	/**
	 * If similarity search should be run against ChEMBL
	 * 
	 * @return
	 */
	public boolean isUseChEMBL() {
		return useChEMBL.isSelected();
	}

	/**
	 * Checks for valid ChEMBL IDs.
	 * 
	 * @param s
	 * @return
	 */
	private boolean isValidChEMBL(String s) {
		return s.startsWith("CHEMBL") && s.length() > 6;
	}


	/**
	 * Checks validity of regex.
	 * 
	 * @param regexp
	 *            to check
	 * @return result of check
	 */
	private boolean isValidRegex(String regexp) {
		try {
			Pattern.compile(regexp);
			return true;
		} catch (PatternSyntaxException e1) {
			return false;
		}
	}


	/**
	 * Concatenate different file name parts
	 * 
	 * @param first
	 * @param parts
	 * @return
	 */
	private URL makeFileURL(String first, String... parts) {
		if (first.startsWith("http") || first.startsWith("https") || first.startsWith("file")) {
			for (String p : parts) {
				first += "/" + p;
			}
			try {
				return new URL(first);
			} catch (MalformedURLException e) {
				throw new Error(e);
			}
		} else {
			File f = new File(first);
			for (String p : parts) {
				f = new File(f, p);
			}
			try {
				return f.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new Error(e);
			}
		}
	}

	/**
	 * Populate combo box of concept classes
	 * 
	 * @param graph
	 */
	private void populateConceptClasses(final ONDEXGraph graph) {
		final List<Object> list = new ArrayList<Object>();
		Runnable doWorkRunnable = new Runnable() {
			public void run() {
				if (supportConceptClass != null && supportConceptClass.isInstalled())
					supportConceptClass.uninstall();
				conceptClasses.removeAllItems();
				list.add(Config.language.getProperty("ToolBar.Search.ConceptClass"));
				conceptClasses.addItem(Config.language.getProperty("ToolBar.Search.ConceptClass"));
				boolean hasCCs = false;
				for (ConceptClass conceptClass : graph.getMetaData().getConceptClasses()) {
					Set<ONDEXConcept> view = graph.getConceptsOfConceptClass(conceptClass);
					if (view.size() > 0) {
						hasCCs = true;
						MetaDataWrapper wrapper = new MetaDataWrapper(conceptClass);
						conceptClasses.addItem(wrapper);
						list.add(wrapper);
					}
				}
				conceptClasses.setEnabled(hasCCs);
				if (hasCCs)
					addAutoCompleteSupport(list, conceptClasses);

			}
		};

		// check if we are in an event dispatch thread or not
		if (SwingUtilities.isEventDispatchThread()) {
			doWorkRunnable.run();
		} else {
			SwingUtilities.invokeLater(doWorkRunnable);
		}
	}

	/**
	 * Populate combo box of data sources
	 * 
	 * @param graph
	 */
	private void populateDataSources(final ONDEXGraph graph) {
		final List<Object> list = new ArrayList<Object>();
		Runnable doWorkRunnable = new Runnable() {
			public void run() {
				if (supportDataSource != null && supportDataSource.isInstalled())
					supportDataSource.uninstall();
				dataSources.removeAllItems();
				list.add(Config.language.getProperty("ToolBar.Search.DataSource"));
				dataSources.addItem(Config.language.getProperty("ToolBar.Search.DataSource"));
				boolean hasDSs = false;
				for (DataSource dataSource : graph.getMetaData().getDataSources()) {
					Set<ONDEXConcept> view = graph.getConceptsOfDataSource(dataSource);
					if (view.size() > 0) {
						hasDSs = true;
						MetaDataWrapper wrapper = new MetaDataWrapper(dataSource);
						dataSources.addItem(wrapper);
						list.add(wrapper);
					}
				}
				dataSources.setEnabled(hasDSs);
				if (hasDSs)
					addAutoCompleteSupport(list, dataSources);
			}
		};

		// check if we are in an event dispatch thread or not
		if (SwingUtilities.isEventDispatchThread()) {
			doWorkRunnable.run();
		} else {
			SwingUtilities.invokeLater(doWorkRunnable);
		}
	}

	/**
	 * Populate combobox of tags
	 * 
	 * @param graph
	 */
	private void populateTags(final ONDEXGraph graph) {
		final List<Object> list = new ArrayList<Object>();
		Runnable doWorkRunnable = new Runnable() {
			public void run() {
				if (supportTag != null && supportTag.isInstalled())
					supportTag.uninstall();
				tags.removeAllItems();
				list.add(Config.language.getProperty("ToolBar.Search.Tag"));
				tags.addItem(Config.language.getProperty("ToolBar.Search.Tag"));
				boolean hasContexts = false;
				for (ONDEXConcept c : graph.getAllTags()) {
					hasContexts = true;
					String name = ConceptListUtils.getDefaultNameForConcept(c);
					IntegerStringWrapper wrapper = new IntegerStringWrapper(c.getId(), name);
					tags.addItem(wrapper);
					list.add(wrapper);
				}
				tags.setEnabled(hasContexts);
				if (hasContexts)
					addAutoCompleteSupport(list, tags);
			}
		};

		// check if we are in an event dispatch thread or not
		if (SwingUtilities.isEventDispatchThread()) {
			doWorkRunnable.run();
		} else {
			SwingUtilities.invokeLater(doWorkRunnable);
		}
	}

	/**
	 * Removes the specified action listener so that it no longer receives
	 * action events from this MenuGraphSearchBox.
	 * 
	 * @param l
	 *            the action listener to be removed
	 */
	public synchronized void removeActionListener(ActionListener l) {
		listenerList.remove(ActionListener.class, l);
	}

	/**
	 * Sets the command string used for action events.
	 * 
	 * @param command
	 *            the command string
	 */
	public void setActionCommand(String command) {
		this.command = command;
	}

	/**
	 * Update restriction boxes according to what is in graph.
	 * 
	 * @param viewer
	 *            OVTK2Viewer currently active viewer
	 */
	public void updateRestrictions(OVTK2PropertiesAggregator viewer) {

		// reset search box
		if (viewer == null) {
			CaretListener[] listeners = searchField.getCaretListeners();
			for (CaretListener l : listeners)
				searchField.removeCaretListener(l);
			searchField.setText(Config.language.getProperty("ToolBar.Search.Filler"));
			for (CaretListener l : listeners)
				searchField.addCaretListener(l);

			// clear concept classes
			if (supportConceptClass != null && supportConceptClass.isInstalled())
				supportConceptClass.uninstall();
			conceptClasses.removeAllItems();
			conceptClasses.addItem(Config.language.getProperty("ToolBar.Search.ConceptClass"));
			conceptClasses.setEnabled(false);

			// clear data sources
			if (supportDataSource != null && supportDataSource.isInstalled())
				supportDataSource.uninstall();
			dataSources.removeAllItems();
			dataSources.addItem(Config.language.getProperty("ToolBar.Search.DataSource"));
			dataSources.setEnabled(false);

			// clear tag lists
			if (supportTag != null && supportTag.isInstalled())
				supportTag.uninstall();
			tags.removeAllItems();
			tags.addItem(Config.language.getProperty("ToolBar.Search.Tag"));
			tags.setEnabled(false);

			// clear checkboxes
			isRegexBox.setSelected(false);
			caseSensitiveBox.setSelected(false);
			tanimoto.getModel().setValue(90);
			useChEMBL.setSelected(false);

			// default search mode
			searchMode.setSelectedIndex(0);

			return;
		}

		// only update if viewer really has changed
		if (viewer.equals(lastViewer) && lastCount == viewer.getONDEXJUNGGraph().getVertexCount())
			return;
		else {
			lastViewer = viewer;
			lastCount = viewer.getONDEXJUNGGraph().getVertexCount();
		}

		// get current ONDEX graph and meta data
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();

		// populate concept classes combo box
		populateConceptClasses(graph);

		// populate data sources combo box
		populateDataSources(graph);

		// populate tag combo box
		populateTags(graph);
	}
}
