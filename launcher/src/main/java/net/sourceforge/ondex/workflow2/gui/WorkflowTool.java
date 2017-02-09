package net.sourceforge.ondex.workflow2.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.util.GraphAdaptor;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.init.PluginDescription;
import net.sourceforge.ondex.init.PluginDocumentationFactory;
import net.sourceforge.ondex.init.PluginRegistry;
import net.sourceforge.ondex.init.PluginType;
import net.sourceforge.ondex.init.PluginType.UnknownPluginTypeException;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow.model.WorkflowDescription;
import net.sourceforge.ondex.workflow.model.WorkflowDescriptionIO;
import net.sourceforge.ondex.workflow.model.WorkflowTask;
import net.sourceforge.ondex.workflow2.gui.components.ComponentGUI;
import net.sourceforge.ondex.workflow2.gui.components.DocPanel;
import net.sourceforge.ondex.workflow2.gui.components.IniReader;
import net.sourceforge.ondex.workflow2.gui.components.ListPanelContainer;
import net.sourceforge.ondex.workflow2.gui.tree.DocumentedTreeNode;
import net.sourceforge.ondex.workflow2.gui.tree.PluginTreeModelBuilder;
import net.sourceforge.ondex.workflow2.gui.tree.PluginTreeNode;
import net.sourceforge.ondex.workflow2.gui.tree.PluginTreeRenderer;

import org.jdom.JDOMException;

/**
 * @author lysenkoa
 *         <p/>
 *         Created on 19-Oct-2009, 15:26:39
 */
public class WorkflowTool extends javax.swing.JFrame {
	private static final long serialVersionUID = 1L;
	private static JFileChooser fc = new JFileChooser();
	private static Cursor curNormal = new Cursor(Cursor.DEFAULT_CURSOR);
	private static Cursor curWait = new Cursor(Cursor.WAIT_CURSOR);
	private JButton btnRun;
	private JScrollPane jScrollPane1;
	private JMenuBar launcherMenu;
	private JMenu menuConfigure;
	private JMenu menuFile;
	private JMenu menuHelp;
	private JTree pluginsTree;
	private JTabbedPane tabpaneMain;
	private OndexDirField txtOndexDir;
	private ListPanelContainer selected;
	private DocPanel tabpaneDoc;
	private PluginRegistry pr;
	private IniReader ini;
	private List<JMenuItem> tempItems = new LinkedList<JMenuItem>();
	private final OpenFileActionListener ofa = new OpenFileActionListener();
	private GraphAdaptor graphLoader;
	private JTextField loadGraph = new JTextField("default");
	private JCheckBox chkLoad = new JCheckBox(
			"Load graph with the following name: ");

	public WorkflowTool() throws Exception {
		this(null);
	}

	/**
	 * Listens for all possible key events and filter CTRL+S for file save short
	 * cut
	 * 
	 * @author taubertj
	 * 
	 */
	private class SavingKeyEventDispatcher implements KeyEventDispatcher {

		/**
		 * which frame this dispatcher is registered for
		 */
		private WorkflowTool parent;

		public SavingKeyEventDispatcher(WorkflowTool parent) {
			this.parent = parent;
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {

			// check for CTRL+s
			if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == 19
					&& hasParent(e)) {
				try {
					// trigger save action
					parent.fileMenuActionPerformed(new ActionEvent(this,
							ActionEvent.ACTION_PERFORMED, "save_default"));
				} catch (UnknownPluginTypeException e1) {
					e1.printStackTrace();
				}
			}

			// check for CTRL+o
			if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == 15
					&& hasParent(e)) {
				try {
					// trigger open action
					parent.fileMenuActionPerformed(new ActionEvent(this,
							ActionEvent.ACTION_PERFORMED, "open"));
				} catch (UnknownPluginTypeException e1) {
					e1.printStackTrace();
				}
			}

			// check for CTRL+w
			if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == 23
					&& hasParent(e)) {
				try {
					// trigger close action
					parent.fileMenuActionPerformed(new ActionEvent(this,
							ActionEvent.ACTION_PERFORMED, "close"));
				} catch (UnknownPluginTypeException e1) {
					e1.printStackTrace();
				}
			}

			// else if (e.getID() == KeyEvent.KEY_TYPED) {
			// System.out.println((int) e.getKeyChar());
			// }

			// the event is NOT consumed
			return false;
		}

		/**
		 * Being overly paranoid here to make sure only key events from the
		 * workflow launcher are accepted.
		 * 
		 * @param e
		 * @return
		 */
		private boolean hasParent(KeyEvent e) {
			boolean hasParent = false;
			if (e.getSource() instanceof JComponent) {
				JComponent c = (JComponent) e.getSource();
				while (c.getParent() != null && c.getParent() != parent) {
					c = (JComponent) c.getParent();
				}
				;
				if (c.getParent() != null && c.getParent() == parent)
					hasParent = true;
			}
			return hasParent;
		}
	}

	/**
	 * @throws Exception
	 */
	public WorkflowTool(GraphAdaptor graphLoader) throws Exception {
		this.graphLoader = graphLoader;

		// register keyboard shortcuts
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new SavingKeyEventDispatcher(this));

		File f = new File(Config.ondexDir);
		if (f.exists() && f.isDirectory()) {
			fc.setCurrentDirectory(f);
		} else {
			fc.setCurrentDirectory(new File(this.getClass()
					.getProtectionDomain().getCodeSource().getLocation()
					.getFile()).getParentFile());
		}

		// PluginRegistry.init("D:/docout.txt", false); DEBUG CODE
		pr = PluginRegistry.getInstance();
		// pr.addPlugin(new Producer().getPluginDescription());
		initTree();
		initComponents();
		configureMenus();
		readConfig();
		ListPanelContainer def = createNewListPanel(null);
		def.setDefault(true);
		addComponent(def);
		tabpaneMain.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JTabbedPane tp = ((JTabbedPane) e.getSource());
				if (tp.getTabCount() > 0)
					selected = ((ListPanelContainer) tp.getSelectedComponent());
			}
		});

		this.setMinimumSize(new Dimension(820, 600));
		this.setPreferredSize(new Dimension(820, 600));
		this.setSize(new Dimension(820, 600));
		int x = (Toolkit.getDefaultToolkit().getScreenSize().width - this
				.getWidth()) / 2;
		int y = (Toolkit.getDefaultToolkit().getScreenSize().height - this
				.getHeight()) / 2;
		this.setLocation(x, y);
	}

	private void addComponent(ListPanelContainer lp) {
		tabpaneMain.add(lp);
		tabpaneMain.setTabComponentAt(tabpaneMain.indexOfComponent(lp),
				lp.getNamingComponent());
	}

	/**
	 * Reads integrator.ini and updates the IniReader
	 */
	private void readConfig() {
		String iniFilePath = System.getProperty("user.home") + File.separator
				+ "integrator.ini";
		ini = new IniReader(iniFilePath);
		ini.addBoundedListOption("WorkflowFileHistory", "#History", 7);
		try {
			ini.readConfig();
			refreshFileMenu();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     *
     */
	private void refreshFileMenu() {
		for (JMenuItem mi : tempItems) {
			menuFile.remove(mi);
		}
		List<String> list = ini.getOptionList("WorkflowFileHistory");
		// modified according to INTEGRATOR-20
		for (int i = 0; i < list.size(); i++) {
			String file = list.get(i);
			String name = file;
			if (name.length() > 20) {
				int index = name.lastIndexOf(File.separator);
				if (index < 0) {
					index = file.length() - 10;
				}
				name = name.substring(0, 7) + " ... "
						+ name.substring(index + 1, name.length());
			}
			JMenuItem jmi = new JMenuItem(name);
			jmi.setActionCommand(file);
			jmi.addActionListener(ofa);
			jmi.setToolTipText(file);
			menuFile.add(jmi, 7);
			tempItems.add(jmi);
		}
	}

	private void configureMenus() {
		createFileMenuItem("New", "new", menuFile);
		createFileMenuItem("Open (Ctrl+o)", "open", menuFile);
		createFileMenuItem("Save (Ctrl+s)", "save_default", menuFile);
		createFileMenuItem("Save As", "save", menuFile);
		createFileMenuItem("Validate", "validate", menuFile);
		createFileMenuItem("Close (Ctrl+w)", "close", menuFile);
		menuFile.addSeparator();
		menuFile.addSeparator();
		createFileMenuItem("Exit", "exit", menuFile);
		JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(
				"Show stable plugins only");
		jmi.setSelected(true);
		jmi.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				showReleaseOnly(e.getStateChange() == ItemEvent.DESELECTED ? false
						: true);
			}
		});
		menuConfigure.add(jmi);
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								null,
								"ONDEX integrator version 2.1 - 03/01/2010 \n Author: Artem Lysenko\n Other contributors: Matthew Hindle \n For support contact: lysenkoa@users.sourceforge.net",
								"About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menuHelp.add(about);

	}

	private void showReleaseOnly(boolean value) {
		pluginsTree.setModel(PluginTreeModelBuilder.build(pr.getAllPlugins(),
				value));
		pluginsTree.validate();
		pluginsTree.updateUI();
	}

	private void initTree() {
		// pr.readSimpleConfigFormat("D:/docout.txt"); DEBUG
		DefaultTreeModel model = PluginTreeModelBuilder.build(
				pr.getAllPlugins(), true);
		pluginsTree = new JTree(model);
		this.showReleaseOnly(true);
		pluginsTree.setRootVisible(false);
		pluginsTree.setShowsRootHandles(true);
		pluginsTree.setCellRenderer(new PluginTreeRenderer());
		pluginsTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					TreePath path = pluginsTree.getPathForLocation(e.getX(),
							e.getY());
					if (path != null) {
						pluginsTree.setSelectionPath(path);
						Object o = path.getLastPathComponent();
						if (o instanceof DocumentedTreeNode) {
							DocumentedTreeNode n = (DocumentedTreeNode) o;
							tabpaneDoc.setDocumentation(n.getDocumentation());
							tabpaneDoc.setArguments(n.getArguments());
							tabpaneDoc.setData(n.getFiles());
						}
						if (e.getClickCount() == 1)
							return;
						if (o instanceof PluginTreeNode) {
							PluginTreeNode n = (PluginTreeNode) o;
							tabpaneDoc.setComment(n.getComment());
							// add other producer plugins here...
							if (selected == null) {
								createNewWorkflow();
							}
							if (selected.getListPanel().getElementCount() == 0
									&& !n.getPlugin().getOndexId()
											.equals("memorygraph")) {
								ComponentGUI cg = new ComponentGUI(
										pr.getPluginDescription(
												net.sourceforge.ondex.init.PluginType.PRODUCER,
												"memorygraph"), true);
								ComponentGUISelectionListener listener = new ComponentGUISelectionListener(
										cg);
								cg.addPropertyChangeListener(listener);
								selected.getListPanel().addListElement(cg);
							}
							ComponentGUI cg = new ComponentGUI(n.getPlugin(),
									true);
							ComponentGUISelectionListener listener = new ComponentGUISelectionListener(
									cg);
							cg.addPropertyChangeListener(listener);
							selected.getListPanel().addListElement(cg);
							selected.getListPanel().sortGraphRefs(cg);
							selected.validate();
							JComponent parent = (JComponent) cg.getParent();
							Rectangle r = parent.getBounds();
							r.x = parent.getX();
							r.y = parent.getY();
							parent.scrollRectToVisible(r);
						}
					}
				}
			}
		});
		// pluginsTree.expandPath(new TreePath(((TreeNode)
		// model.getRoot()).getPath()));

	}

	// ============================

	private void initComponents() {
		jScrollPane1 = new JScrollPane();
		tabpaneMain = new JTabbedPane();
		txtOndexDir = new OndexDirField();
		btnRun = new JButton();
		tabpaneDoc = new DocPanel(this);
		launcherMenu = new JMenuBar();
		menuFile = new JMenu();
		menuConfigure = new JMenu();
		menuHelp = new JMenu();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Ondex Integrator");

		jScrollPane1.setViewportView(pluginsTree);

		txtOndexDir.setPreferredSize(new Dimension(8, 18));

		txtOndexDir.setText(Config.ondexDir);

		btnRun.setFont(new Font("Tahoma", 1, 11));
		btnRun.setText("Run workflow");
		// btnRun.setPreferredSize(new Dimension(73, 20));
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnRunActionPerformed(evt);
			}
		});

		menuFile.setText("File");
		launcherMenu.add(menuFile);

		menuConfigure.setText("Configure");
		launcherMenu.add(menuConfigure);

		menuHelp.setText("Help");
		launcherMenu.add(menuHelp);

		setJMenuBar(launcherMenu);

		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints con = new GridBagConstraints();
		con.fill = GridBagConstraints.BOTH;
		con.weightx = 1;
		con.weighty = 1;
		con.gridx = 0;
		con.gridy = 0;
		con.gridwidth = 4;
		p.add(tabpaneMain, con);

		con.fill = GridBagConstraints.HORIZONTAL;
		con.gridwidth = 1;
		if (graphLoader == null) {
			con.weightx = 1;
			con.weighty = 0;
			con.gridy = 1;
			JPanel z = new JPanel();
			p.add(z, con);
			con.weightx = 0;
			con.gridx = 1;
			p.add(btnRun, con);
		} else {
			con.weightx = 0;
			con.weighty = 0;
			con.gridy = 1;
			p.add(chkLoad, con);
			chkLoad.setSelected(true);
			con.weightx = 0;
			con.gridx = 1;
			Dimension d = new Dimension(160, 18);
			loadGraph.setSize(d);
			loadGraph.setPreferredSize(d);
			loadGraph.setMinimumSize(d);
			p.add(loadGraph, con);
			con.weightx = 1;
			con.gridx = 2;
			p.add(new JPanel(), con);
			con.weightx = 0;
			con.gridx = 3;
			p.add(btnRun, con);
		}

		tabpaneDoc.setMinimumSize(new Dimension(300, 250));
		tabpaneDoc.setSize(new Dimension(300, 250));
		tabpaneDoc.setPreferredSize(new Dimension(300, 250));
		jScrollPane1.setMinimumSize(new Dimension(300, 250));
		jScrollPane1.setSize(new Dimension(300, 250));
		jScrollPane1.setPreferredSize(new Dimension(300, 250));
		p.setMinimumSize(new Dimension(400, 500));
		JSplitPane spv = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				jScrollPane1, tabpaneDoc);
		spv.setResizeWeight(1d);
		JSplitPane sph = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spv, p);
		this.add(sph);
		pack();
	}

	private ListPanelContainer createNewListPanel(File f) {
		String name = f == null ? "NewWorkflow.xml" : f.getName();
		ListPanelContainer result = new ListPanelContainer(tabpaneMain, f, name);
		selected = result;
		return result;
	}

	boolean debug = true;

	private void btnRunActionPerformed(ActionEvent evt) {
		/*
		 * for(Object o : fkl.currentSources){ System.err.println(o); }
		 * if(debug)return;
		 */
		if (!checkOndexDir()) {
			return;
		}
		try {
			if (chkLoad.isSelected()) {
				String id = loadGraph.getText().trim();
				if (id.length() > 0) {
					try {
						Class cls = Class
								.forName("net.sourceforge.ondex.export.slotable.Export");
						PluginRegistry.getInstance().addOndexPlugin(cls,
								"Export", "slottableAdapter", "", "", "");
						List<BoundArgumentValue> args = new ArrayList<BoundArgumentValue>();
						PluginDescription pd = PluginRegistry.getInstance()
								.getPluginDescription(PluginType.EXPORT,
										"slottableAdapter");
						ArgumentDescription graphId = WorkflowDescriptionIO
								.getArgumentDescriptionByName(pd, "graphId");
						args.add(new BoundArgumentValue(graphId, loadGraph
								.getText().trim()));
						WorkflowTask graphLoadingTask = new WorkflowTask(pd,
								args);
						Class par = Class
								.forName("net.sourceforge.ondex.core.util.GraphAdaptor");
						Method m = cls.getMethod("setGraphAdaptor", par);
						m.invoke(null, graphLoader);
						if (selected.validateConfiguration()) {
							selected.executePlus(graphLoadingTask);
						} else {

						}
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}
			} else if (selected.validateConfiguration()) {
				selected.execute();
			}
		} catch (Exception e) {
			ErrorDialog.show(e, this);
		}
	}

	private static void removeDefault(final JTabbedPane p) {
		Integer remove = null;
		for (int i = 0; i < p.getTabCount(); i++) {
			ListPanelContainer lp = ((ListPanelContainer) p.getComponentAt(i));
			if (lp.isDefault() && lp.isEmpty()) {
				remove = i;
				break;
			}
		}
		if (remove != null) {
			p.remove(remove);
		}
	}

	private void createNewWorkflow() {
		removeDefault(tabpaneMain);
		ListPanelContainer c = createNewListPanel(null);
		addComponent(c);
		tabpaneMain.setSelectedComponent(c);
	}

	private void fileMenuActionPerformed(ActionEvent e)
			throws PluginType.UnknownPluginTypeException {
		String command = e.getActionCommand();
		if (command.equals("exit")) {
			this.dispose();
		} else if (command.equals("validate")) {
			try {
				if (selected.validateConfiguration()) {
					JOptionPane.showMessageDialog(this,
							"No problems detected in your workflow.",
							"Validation", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (Exception e2) {
				ErrorDialog.show(e2, this);
			}
		} else if (command.equals("new")) {
			createNewWorkflow();
		} else if (command.equals("open")) {
			removeDefault(tabpaneMain);
			int returnVal = fc.showOpenDialog(WorkflowTool.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				removeDefault(tabpaneMain);
				File file = fc.getSelectedFile();
				ofa.actionPerformed(new ActionEvent(WorkflowTool.this, 0, file
						.getAbsoluteFile().getAbsolutePath()));
				selected.setSourceFile(file);
			}
		} else if (command.equals("save_default")) {
			File file = selected.getSourceFile();
			if (file == null) {
				fileMenuActionPerformed(new ActionEvent(WorkflowTool.this, 0,
						"save"));
			}
			if (file == null) {
				return;
			}
			if (!file.getName().toLowerCase().endsWith(".xml"))
				file = new File(file.getAbsolutePath() + ".xml");
			tabpaneMain.setTitleAt(tabpaneMain.getSelectedIndex(),
					file.getName());

			try {
				WorkflowDescriptionIO.saveToFile(file, buildTask(selected));
				selected.setSaved(true);
			} catch (Exception e1) {
				JOptionPane
						.showMessageDialog(
								null,
								"Could not save the file. Check that you have permission to write to teh destination and the disk is not full.");
				e1.printStackTrace();
			}
			ini.addToEndOfList("WorkflowFileHistory", file.getAbsoluteFile()
					.getAbsolutePath());
			refreshFileMenu();
		} else if (command.equals("save")) {
			if (selected == null)
				return;
			int returnVal = fc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					Object[] options = { "Yes", "No" };
					int n = JOptionPane.showOptionDialog(WorkflowTool.this,
							"File " + file.getName()
									+ " already exists. Wanna replace it?",
							"File exists", JOptionPane.WARNING_MESSAGE,
							JOptionPane.YES_NO_OPTION, null, options,
							options[0]);
					if (n == 1)
						return;
				}
				if (!file.getName().toLowerCase().endsWith(".xml"))
					file = new File(file.getAbsolutePath() + ".xml");
				// tabpaneMain.setTitleAt(tabpaneMain.getSelectedIndex(),
				// file.getName());
				selected.setSourceFile(file);
				try {
					WorkflowDescriptionIO.saveToFile(file, buildTask(selected));
					selected.setSaved(true);
				} catch (Exception e1) {
					JOptionPane
							.showMessageDialog(
									null,
									"Could not save the file. Check that you have permission to write to teh destination and the disk is not full.");
					e1.printStackTrace();
				}
				ini.addToEndOfList("WorkflowFileHistory", file
						.getAbsoluteFile().getAbsolutePath());
				refreshFileMenu();
			}
		} else if (command.equals("close")) {
			try {
				selected.finalize();
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
			tabpaneMain.removeTabAt(tabpaneMain.getSelectedIndex());
			if (tabpaneMain.getTabCount() == 0) {
				selected = null;
			} else {
				selected = (ListPanelContainer) tabpaneMain
						.getComponentAt(tabpaneMain.getSelectedIndex());
			}
		}
	}

	private class OpenFileActionListener implements ActionListener {
		public OpenFileActionListener() {
		}

		public void actionPerformed(ActionEvent e) {
			// if (Config.ondexDir != null) fc.setCurrentDirectory(new
			// File(Config.ondexDir));
			WorkflowTool.this.setCursor(curWait);
			ListPanelContainer c = createNewListPanel(null);
			File file = new File(e.getActionCommand());
			selected.setSourceFile(file);
			addComponent(c);
			tabpaneMain.setSelectedComponent(c);
			try {
				WorkflowDescription td = WorkflowDescriptionIO.readFile(file,
						pr);
				for (WorkflowTask pc : td.getComponents()) {
					ComponentGUI cg = new ComponentGUI(pc);
					ComponentGUISelectionListener listener = new ComponentGUISelectionListener(
							cg);
					cg.addPropertyChangeListener(listener);
					selected.getListPanel().addListElement(cg);
				}
				removeDefault(tabpaneMain);
				c.setSaved(true);
			} catch (IOException e1) {
				tabpaneMain.removeTabAt(tabpaneMain.indexOfComponent(c));
				JOptionPane.showMessageDialog(null, "Could not open file!");
			} catch (JDOMException e2) {
				tabpaneMain.removeTabAt(tabpaneMain.indexOfComponent(c));
				JOptionPane.showMessageDialog(null,
						"Malformed XML found in file!");
			} catch (XMLStreamException e3) {
				tabpaneMain.removeTabAt(tabpaneMain.indexOfComponent(c));
				JOptionPane.showMessageDialog(null,
						"Malformed XML found in file!");
			} catch (UnknownPluginTypeException e4) {
				JOptionPane
						.showMessageDialog(null,
								"Unknown plugin type found in the input file, workflow was not loaded.");
			} finally {

				selected.getListPanel().validate();
				WorkflowTool.this.setCursor(curNormal);
				selected.updateUI();
			}
			ini.addToEndOfList("WorkflowFileHistory", e.getActionCommand());
			refreshFileMenu();
		}
	}

	/**
	 * @author lysenkoa
	 */
	private class OndexDirField extends JTextField implements KeyListener {
		private static final long serialVersionUID = -2905385403930611079L;
		private final JFileChooser fc = new JFileChooser();

		public OndexDirField() {
			this.addKeyListener(this);
			this.setToolTipText("ONDEX working directory to be used by the backend.");
		}

		public void keyPressed(final KeyEvent e) {
		}

		public void keyReleased(final KeyEvent e) {
		}

		public void keyTyped(final KeyEvent e) {
			if (e.getKeyChar() == '\t') {
				final String text = this.getText();
				this.setText(text.substring(0, this.getCaretPosition()) + "\t"
						+ text.substring(this.getCaretPosition()));
				Config.ondexDir = fc.getSelectedFile().getAbsolutePath();
			}
		}
	}

	public static boolean checkOndexDir() {
		if (Config.ondexDir != null) {
			File dir = new File(Config.ondexDir);
			if (dir.exists() && dir.isDirectory()) {
				if (new File(Config.ondexDir + File.separator + "xml"
						+ File.separator + "ondex_metadata.xml").exists()
						&& new File(Config.ondexDir + File.separator + "xml"
								+ File.separator + "ondex.xsd").exists()) {
					return true;
				}
			}
		}
		int choice = JOptionPane
				.showConfirmDialog(
						null,
						"No valid ONDEX working directory has been set - this should\n"
								+ "be the latest version of the 'data' directory distributed\n"
								+ "together with ONDEX plugins (NOT the 'data' directory distributed with\n"
								+ "OVTK). You can set the working directory by entering a correct path in the\n"
								+ "$ONDEXDIR field of the main window. Many components of the ONDEX core\n"
								+ "have dependancies stored there and may not function properly without them.\n"
								+ "Do you want to proceed regardless of this problem?",
						"Incorrect core configuration",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
		return choice == 0 ? true : false;
	}

	private JMenuItem createFileMenuItem(String name, String actionCommand,
			JMenu m) {
		JMenuItem jmi = new JMenuItem(name);
		jmi.setActionCommand(actionCommand);
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					fileMenuActionPerformed(e);
				} catch (PluginType.UnknownPluginTypeException e1) {
					e1.printStackTrace();
				}
			}
		});
		m.add(jmi);
		return jmi;
	}

	private class ComponentGUISelectionListener implements
			PropertyChangeListener {
		private final ComponentGUI cg;

		public ComponentGUISelectionListener(ComponentGUI cg) {
			this.cg = cg;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("isInFocus")
					&& (Boolean) evt.getNewValue()) {
				String comment = cg.getComment();
				if (comment != null) {
					WorkflowTool.this.tabpaneDoc.setComment(comment);
				}
				WorkflowTool.this.tabpaneDoc.setDocumentation(cg
						.getDocumentation());
				WorkflowTool.this.tabpaneDoc
						.setArguments(PluginDocumentationFactory
								.getArguments(cg.getPluginConfig()
										.getPluginDescription()));
				WorkflowTool.this.tabpaneDoc.setData(PluginDocumentationFactory
						.getFiles(cg.getPluginConfig().getPluginDescription()));
			}
		}
	}

	private static WorkflowDescription buildTask(ListPanelContainer z) {
		WorkflowDescription task = new WorkflowDescription();
		for (Object o : z.getListPanel().getContent()) {
			task.addPlugin(((ComponentGUI) o).getPluginConfig());
		}
		return task;
	}
}
