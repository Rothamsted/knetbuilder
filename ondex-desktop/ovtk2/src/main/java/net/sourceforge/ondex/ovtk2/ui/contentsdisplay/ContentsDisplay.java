package net.sourceforge.ondex.ovtk2.ui.contentsdisplay;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkListener;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2HyperlinkListener;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins.generic.BooleanPlugin;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins.generic.NumberPlugin;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins.generic.StringPlugin;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

/**
 * The contents display calls the available plugins to generate a HTML based
 * information display on concepts and relations.
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class ContentsDisplay extends RegisteredJInternalFrame implements ActionListener {
	// ####FIELDS####

	/**
	 * blah.
	 */
	private static final long serialVersionUID = -5815404704933733861L;

	/**
	 * Debug.
	 */
	private static final boolean DEBUG = false;

	/**
	 * the actual html panel that displays the content.
	 */
	private JEditorPane pane;

	/**
	 * a map that stores the plugins
	 */
	private Map<String, AbstractContentDisplayPlugin> name2plugin;

	/**
	 * a vector that stores the names of the plugins in the user defined order.
	 */
	private Vector<String> pluginOrder;

	/**
	 * stores the activation states of all plugins.
	 */
	private Map<String, Boolean> name2activation;

	/**
	 * the graph.
	 */
	private ONDEXGraph aog;

	/**
	 * The currently displayed concept or relation.
	 */
	private ONDEXEntity curr;

	/**
	 * Special listener for hyper links.
	 */
	private HyperlinkListener listener;

	/**
	 * Case of running in applet
	 */
	private JFrame parent;

	/**
	 * Either of parent or this
	 */
	private Container contentPane;

	// ####CONSTRUCTOR####

	/**
	 * the constructor. sets up everything.
	 */
	public ContentsDisplay(ONDEXGraph aog, HyperlinkListener listener, JFrame parent) {
		super(Config.language.getProperty("ContentsDisplay.Title"), "Info", Config.language.getProperty("ContentsDisplay.Title"), true, true, true, true);
		this.aog = aog;
		this.parent = parent;

		// add content either to this or parent
		if (parent != null)
			contentPane = parent.getContentPane();
		else
			contentPane = this.getContentPane();

		// set OVTK2 listener are custom one
		if (listener == null)
			this.listener = new OVTK2HyperlinkListener(OVTK2Desktop.getInstance());
		else
			this.listener = listener;

		if (parent == null)
			// dispose on close
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		registerPlugins();
		setupEditorPane();
		setupMenu();

		if (parent != null)
			parent.setSize(300, 600);
		else
			setSize(300, 600);
	}

	/**
	 * Overloaded constructor
	 * 
	 * @param aog
	 *            ONDEXGraph to query
	 */
	public ContentsDisplay(ONDEXGraph aog) {
		this(aog, null, null);
	}

	// ####METHODS####

	/**
	 * sets up the menu bar.
	 */
	private void setupMenu() {
		JMenuBar mb = new JMenuBar();

		JMenu menu = new JMenu(Config.language.getProperty("ContentsDisplay.Menu.Title"));

		menu.add(createMenuItem(Config.language.getProperty("ContentsDisplay.Menu.Options"), "options"));
		menu.addSeparator();
		menu.add(createMenuItem(Config.language.getProperty("ContentsDisplay.Menu.Close"), "close"));

		mb.add(menu);

		if (parent != null)
			parent.setJMenuBar(mb);
		else
			setJMenuBar(mb);
	}

	/**
	 * creates a new menu item.
	 * 
	 * @param title
	 *            the title of the item.
	 * @param cmd
	 *            the action command.
	 * @return the new item.
	 */
	private JMenuItem createMenuItem(String title, String cmd) {
		JMenuItem item = new JMenuItem(title);
		item.setActionCommand(cmd);
		item.addActionListener(this);
		return item;
	}

	/**
	 * sets up the editor pane.
	 */
	private void setupEditorPane() {
		contentPane.setLayout(new BorderLayout());

		// create antialiased editor pane.
		pane = new JEditorPane() {
			private static final long serialVersionUID = -789611244532767255L;

			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				super.paint(g);
			}
		};
		// no editing.
		pane.setEditable(false);
		// enable html support.
		pane.setContentType("text/html");
		// enable hyperlink handling.
		pane.addHyperlinkListener(listener);

		// default text
		pane.setText(Config.language.getProperty("ContentsDisplay.EmptyText"));

		contentPane.add(new JScrollPane(pane), BorderLayout.CENTER);
	}

	/**
	 * recieves and handles both commands from the menubar and commands that
	 * come from new picking actions on the OVTK2Viewer.
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		registerPlugins();
		String cmd = e.getActionCommand();
		if (cmd.equals("close"))
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
		else if (cmd.equals("options")) {
			if (parent == null) {
				// case when running in OVTK2Desktop mode
				new CDOptionsDialog(OVTK2Desktop.getInstance().getMainFrame(), pluginOrder, name2activation);
			} else {
				// running in applet mode within a JDesktopPane within a JFrame
				new CDOptionsDialog(parent, pluginOrder, name2activation);
			}
			refresh();
		} else if (cmd.equals("putative node pick")) {
			if (e.getSource() instanceof ONDEXConcept) {
				ONDEXConcept n = (ONDEXConcept) e.getSource();
				showInfoFor(n);
			}
		} else if (cmd.equals("putative edge pick")) {
			if (e.getSource() instanceof ONDEXRelation) {
				ONDEXRelation n = (ONDEXRelation) e.getSource();
				showInfoFor(n);
			}
		}

	}

	public void refresh() {
		showInfoFor(curr);
	}

	/**
	 * finds, instatiates and registers all plugins.
	 */
	private void registerPlugins() {
		name2plugin = new HashMap<String, AbstractContentDisplayPlugin>();
		name2activation = new HashMap<String, Boolean>();
		pluginOrder = new Vector<String>();

		Set<String> attsAccountedFor = new HashSet<String>();

		String pluginsProp = Config.config.getProperty("ContentsDisplay.Plugins");
		// System.out.println("Loading ContentDisplay plugins: "+pluginsProp);
		String[] pluginNames = pluginsProp.split(";");
		for (String pluginName : pluginNames) {
			try {
				String classpath = "net.sourceforge.ondex.ovtk2.ui.contentsdisplay.plugins." + pluginName;
				Class<?>[] args = new Class<?>[] { ONDEXGraph.class };
				Constructor<?> constr = AbstractContentDisplayPlugin.class.getClassLoader().loadClass(classpath).getConstructor(args);
				AbstractContentDisplayPlugin plugin = (AbstractContentDisplayPlugin) constr.newInstance(aog);

				if (plugin instanceof AttributePlugin) {
					String[] atts = ((AttributePlugin) plugin).getAttributeNames();
					for (String att : atts) {
						attsAccountedFor.add(att);
					}
				}

				String name = plugin.getName();
				name2plugin.put(name, plugin);
				pluginOrder.add(name);
				name2activation.put(name, true);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

		SortedSet<String> sort = new TreeSet<String>();
		for (AttributeName att : aog.getMetaData().getAttributeNames()) {
			if (!attsAccountedFor.contains(att.getId()) && !AppearanceSynchronizer.attr.contains(att.getId())) {
				// no producer for it
				if (Number.class.isAssignableFrom(att.getDataType())) {
					name2plugin.put(att.getId(), new NumberPlugin(aog, att));
					name2activation.put(att.getId(), true);
					sort.add(att.getId());
				} else if (String.class.isAssignableFrom(att.getDataType())) {
					name2plugin.put(att.getId(), new StringPlugin(aog, att));
					name2activation.put(att.getId(), true);
					sort.add(att.getId());
				} else if (Boolean.class.isAssignableFrom(att.getDataType())) {
					name2plugin.put(att.getId(), new BooleanPlugin(aog, att));
					name2activation.put(att.getId(), true);
					sort.add(att.getId());
				}
			}
		}
		pluginOrder.addAll(sort);

	}

	/**
	 * Calls all plugins to process the given concept or relation and then
	 * displays the result on the frame.
	 * 
	 * @param e
	 *            the concept or relation.
	 */
	public void showInfoFor(ONDEXEntity e) {

		String htmlHead = "<html><head><style type=\"text/css\"><!--\n" + "body {font-family:Arial,sans-serif; font-size:10px; color:black; }\n" + "h1 { font-size:14px; margin-bottom:4px;}" + "h2 { font-size:12px; margin-bottom:4px; color:#008800;}" + "h3 { font-size:10px; margin-bottom:4px;}" + "code { font-family:Courier New; font-size:8px; }" +
		// ".xmpcode { border-width:10px; border-style:solid;
		// border-color:#EEEEEE; background-color:#FFFFE0;
		// font-family:Arial,sans-serif; }"+
				"a:link { color:red; text-decoration:none;}\n" + "a:visited { color:#772200; text-decoration:none; }\n" + "a:active { color:#000000; text-decoration:none; }\n" + "--></style></head><body>";

		StringBuffer b = new StringBuffer(htmlHead);

		// make sure there is something to display
		if (e != null) {
			for (String pluginID : pluginOrder) {
				if (name2activation.get(pluginID)) {
					AbstractContentDisplayPlugin plugin = name2plugin.get(pluginID);
					String pluginOutput = plugin.compileContent(e);
					b.append(pluginOutput);
				}
			}
		}
		b.append("</body></html>");
		if (DEBUG)
			System.out.println(b);
		pane.setText(b.toString());
		pane.setCaretPosition(0);// reset to top
		if (DEBUG)
			System.out.println(pane.getText());
		curr = e;
		pane.updateUI();
		pane.repaint();

		// bring item information to front if hidden
		this.toFront();
	}

}
