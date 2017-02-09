package net.sourceforge.ondex.ovtk2.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.transform.TransformerException;

import net.sourceforge.ondex.config.Config;

import org.jdom.JDOMException;

/**
 * @author lysenkoa
 */
public class ModuleBrowser extends javax.swing.JFrame implements ActionListener {
	private JTextPane documentationFrame;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JButton jButton1;
	private JTree moduleTree;

	private static String encoding = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private FetchPluginDescription pluginDescriptor = new FetchPluginDescription();

	/**
	 * Creates new form ModuleBrowser
	 */
	public ModuleBrowser() throws JDOMException, IOException {
		initComponents();
	}

	private void initComponents() throws JDOMException, IOException {

		jScrollPane1 = new JScrollPane();
		PluginTreeModelBuilder<Artifact> builder = new PluginTreeModelBuilder<Artifact>();

		Artifact[] artifacts = ListPlugins.getArtifacts();
		Arrays.sort(artifacts);
		for (Artifact artifact : artifacts) {

			String documentation;
			// String xml =
			// convertStreamToString(pluginDescriptor.getXML(artifact));

			try {
				documentation = convertStreamToString(pluginDescriptor.getDocumentation(artifact));
			} catch (TransformerException e) {
				documentation = "xsl failed to generate documentation.";
			} catch (IOException e) {
				continue;
			}
			String[] versionComponants = artifact.getVersion().split("-");

			if (documentation.startsWith(encoding)) {
				documentation = documentation.substring(encoding.length());
			}

			builder.addPlugin(artifact, documentation, artifact.getGroupId(), artifact.getArtifactId(), versionComponants[1], versionComponants[0]);
		}

		DefaultTreeModel model = builder.getTreeModel();
		moduleTree = new JTree(model);
		moduleTree.setRootVisible(false);
		moduleTree.setShowsRootHandles(true);
		moduleTree.setCellRenderer(new PluginTreeRenderer());

		jScrollPane2 = new JScrollPane();
		documentationFrame = new JTextPane();
		documentationFrame.setContentType("text/html");
		jButton1 = new JButton();

		final JPopupMenu popup = new JPopupMenu();
		createItem(popup, "Install");
		createItem(popup, "Uninstall");
		createItem(popup, "Upgrade");
		createItem(popup, "Downgrade");
		popup.setOpaque(true);
		popup.setLightWeightPopupEnabled(true);

		moduleTree.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show((JComponent) e.getSource(), e.getX(), e.getY());
				} else if (e.getButton() == MouseEvent.BUTTON1) {
					TreePath path = moduleTree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						moduleTree.setSelectionPath(path);
						Object o = path.getLastPathComponent();
						if (o instanceof DocumentedTreeNode) {
							DocumentedTreeNode n = (DocumentedTreeNode) o;
							if (n.isLeaf())
								documentationFrame.setText(n.getDocumentation());
						}
					}
				}
			}
		});

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		jScrollPane1.setViewportView(moduleTree);

		jScrollPane2.setViewportView(documentationFrame);

		jButton1.setText("Close");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				closeActionPerformed(evt);
			}
		});

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 267, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)).addComponent(jButton1, Alignment.TRAILING)).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.TRAILING, false).addComponent(jScrollPane2, Alignment.LEADING).addComponent(jScrollPane1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED).addComponent(jButton1).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
	}

	private JMenuItem createItem(JPopupMenu popup, String name) {
		JMenuItem mi = new JMenuItem(name);
		mi.addActionListener(this);
		mi.setActionCommand("Install");
		popup.add(mi);
		return mi;
	}

	private void closeActionPerformed(ActionEvent evt) {

	}

	public void actionPerformed(ActionEvent ae) {

		TreePath path = moduleTree.getSelectionPath();
		PluginTreeNode node = (PluginTreeNode) path.getLastPathComponent();
		if (ae.getActionCommand().equals("Install")) {
			try {
				SavePlugin.downloadPlugin((Artifact) node.getPlugin(), new File(Config.pluginDir));
			} catch (IOException e) {
				e.printStackTrace(); // To change body of catch statement use
										// File | Settings | File Templates.
			}
		} else if (ae.getActionCommand().equals("Uninstall")) {
			// TODO Do your dirty deed here.
		} else if (ae.getActionCommand().equals("Downgrade")) {
			// TODO Do your dirty deed here.
		} else if (ae.getActionCommand().equals("Upgrade")) {
			// TODO Do your dirty deed here.
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new ModuleBrowser().setVisible(true);
				} catch (JDOMException e) {
					e.printStackTrace(); // To change body of catch statement
											// use File | Settings | File
											// Templates.
				} catch (IOException e) {
					e.printStackTrace(); // To change body of catch statement
											// use File | Settings | File
											// Templates.
				}
			}
		});
	}

	public String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}
}
