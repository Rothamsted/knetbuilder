/**
 *
 */
package net.sourceforge.ondex.ovtk2.io;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.parser.cytoscape.Parser;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;

/**
 * @author jweile
 */
public class CytoscapeImporter extends JDialog implements ActionListener,
		OVTK2IO {

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -1808810935778135986L;

	/**
	 * ondex graph
	 */
	private ONDEXGraph graph;

	/**
	 * the cytoscape file to import.
	 */
	private File file;

	private JList entryList;

	private JTextField cc, rt, cv, et;

	boolean ok = false;

	Frame parent = null;

	/**
	 * Default constructor.
	 */
	public CytoscapeImporter() {
		this(OVTK2Desktop.getInstance().getMainFrame());
	}

	/**
	 * Default constructor.
	 */
	public CytoscapeImporter(Frame parent) {
		super(parent, "Cytoscape Importer", true);
		this.parent = parent;
	}

	private void populateList() throws IOException {
		DefaultListModel model = new DefaultListModel();
		entryList = new JList(model);
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().endsWith(".xgmml")) {
				String[] dirs = entry.getName().split("/");
				String name = dirs[dirs.length - 1];
				int index = name.lastIndexOf(".xgmml");
				model.addElement(name.substring(0, index));
			}
		}
		if (model.getSize() > 0) {
			entryList.setSelectedIndex(0);
		}
	}

	/**
     *
     */
	private void setupGUI() {
		getContentPane().setLayout(new BorderLayout());

		JPanel argPanel = new JPanel();
		argPanel.setLayout(new BoxLayout(argPanel, BoxLayout.PAGE_AXIS));
		cc = new JTextField("Thing");
		argPanel.add(createForm("ConceptClass", cc));
		rt = new JTextField("r");
		argPanel.add(createForm("RelationType", rt));
		cv = new JTextField("unknown");
		argPanel.add(createForm("DataSource", cv));
		et = new JTextField("IMPD");
		argPanel.add(createForm("EvidenceType", et));

		JPanel listpanel = new JPanel();
		listpanel.setBorder(BorderFactory
				.createTitledBorder("Cytoscape graph:"));
		listpanel.add(new JScrollPane(entryList));

		JPanel mainpanel = new JPanel(new GridLayout(1, 2, 5, 5));
		mainpanel.add(listpanel);
		mainpanel.add(argPanel);
		getContentPane().add(mainpanel);

		JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonpanel.add(createButton("OK", "ok",
				entryList.getModel().getSize() > 0));
		buttonpanel.add(createButton("Cancel", "cancel", true));
		getContentPane().add(buttonpanel, BorderLayout.SOUTH);

		pack();
		if (parent != null) {
			int pw = parent.getWidth();
			int ph = parent.getHeight();
			setLocation((pw - getWidth()) / 2, (ph - getHeight()) / 2);
		} else {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension dim = toolkit.getScreenSize();
			setLocation((dim.width - getWidth()) / 2,
					(dim.height - getHeight()) / 2);
		}
	}

	private JPanel createForm(String label, JTextField tf) {
		JPanel p = new JPanel(new GridLayout(1, 2, 2, 2));
		p.add(new JLabel(label));
		p.add(tf);
		return p;
	}

	private JButton createButton(String label, String cmd, boolean enabled) {
		JButton b = new JButton(label);
		b.addActionListener(this);
		b.setActionCommand(cmd);
		b.setEnabled(enabled);
		return b;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("ok")) {
			IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
				public void task() {
					try {
						Parser parser = new Parser();
						parser.setONDEXGraph(graph);
						ONDEXPluginArguments pa = new ONDEXPluginArguments(
								parser.getArgumentDefinitions());
						pa.setOption(FileArgumentDefinition.INPUT_FILE,
								file.getAbsolutePath());
						pa.setOption(Parser.GRAPH_NAME_ARG,
								entryList.getSelectedValue());
						pa.setOption(Parser.CONCEPT_CLASS_ARG, cc.getText());
						pa.setOption(Parser.RELATION_TYPE_ARG, rt.getText());
						pa.setOption(Parser.CV_ARG, cv.getText());
						pa.setOption(Parser.EVIDENCE_ARG, et.getText());
						parser.setArguments(pa);
						parser.start();
					} catch (Exception ex) {
						ErrorDialog.show(parent != null, ex,
								Thread.currentThread());
					} finally {
						ok = true;
						dispose();
					}
				}
			};
			p.start();
			OVTKProgressMonitor.start(parent, "Reading file", p);
		} else if (cmd.equals("cancel")) {
			dispose();
		}
	}

	public boolean ok() {
		return ok;
	}

	@Override
	public void start(File file) {
		this.file = file;
		try {
			populateList();
			setupGUI();
			setVisible(true);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(parent, "File unreadable!", "Error",
					JOptionPane.ERROR_MESSAGE);
			dispose();
		}
	}

	@Override
	public void setGraph(ONDEXGraph graph) {
		this.graph = graph;
	}

	@Override
	public String getExt() {
		return "cys";
	}

	@Override
	public boolean isImport() {
		return true;
	}

}
