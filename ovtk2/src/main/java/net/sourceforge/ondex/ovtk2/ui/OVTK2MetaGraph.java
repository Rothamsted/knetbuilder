package net.sourceforge.ondex.ovtk2.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConcept;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConceptLabels;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConceptShapes;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelation;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelationLabels;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelationStrokes;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogExport;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.ViewMenuAction;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.ImageWriterUtil;
import net.sourceforge.ondex.ovtk2.util.RegisteredFrame;
import net.sourceforge.ondex.ovtk2.util.xml.MetaGraphXMLReader;
import net.sourceforge.ondex.ovtk2.util.xml.MetaGraphXMLWriter;

import org.apache.commons.collections15.Transformer;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.ctc.wstx.io.CharsetNames;

/**
 * Represents a dynamic metagraph view on the graph of a OVTK2Viewer.
 * 
 * @author taubertj
 * 
 */
public class OVTK2MetaGraph extends RegisteredJInternalFrame implements ActionListener, RegisteredFrame, ChangeListener {

	private static final String SAVE = "save";

	private static final String LOAD = "load";

	private static final String EDGELABELS = "edgelabels";

	private static final String NODELABELS = "nodelabels";

	public static final String KEY = "metagraphappearance";

	private class LabelTransformer<T, Z extends java.awt.Font> implements Transformer<T, java.awt.Font> {

		private java.awt.Font font;

		public LabelTransformer(java.awt.Font f, int size) {
			font = f.deriveFont(Float.valueOf(size));
		}

		@Override
		public java.awt.Font transform(T arg0) {
			return font;
		}

	}

	// default edge thickness in meta graph
	private static final int defaultEdgeSize = 1;

	// default size of font for meta graph
	private static final int defaultFontSize = 11;

	// default size of nodes in meta graph
	private static final int defaultNodeSize = 20;

	// generated
	private static final long serialVersionUID = 4741083045348644939L;

	// constant for height
	private static final int spinnerHight = 22;

	// constant for width
	private static final int spinnerWidth = 60;

	// setup of different sizes
	private JSpinner edgeSize, nodeSize, fontSize;

	// show different labels
	private JMenuItem nodeLabels, edgeLabels;

	// current OVTK2Viewer
	private OVTK2Viewer viewer = null;

	/**
	 * Initialize metagraph view on a given viewer.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public OVTK2MetaGraph(OVTK2Viewer viewer) {
		// set title and icon
		super(Config.language.getProperty("MetaGraph.Title"), "MetaGraph", Config.language.getProperty("MetaGraph.Title"), true, true, true, true);

		// dispose viewer on close
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		initIcon();

		// new menubar
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		// file menu
		JMenu file = makeMenu("MetaGraph.File");
		menuBar.add(file);

		// export meta-graph
		JMenuItem export = makeMenuItem("MetaGraph.File.Export", "export");
		file.add(export);

		// edit menu
		JMenu edit = makeMenu("MetaGraph.Edit");
		menuBar.add(edit);

		// show sub-menu
		JMenu show = makeMenu("MetaGraph.Edit.Show");
		edit.add(show);

		// show all
		JMenuItem showAll = makeMenuItem("MetaGraph.Edit.ShowAll", "showall");
		show.add(showAll);

		// show selection
		JMenuItem showSel = makeMenuItem("MetaGraph.Edit.ShowSelection", "showsel");
		show.add(showSel);

		// hide sub-menu
		JMenu hide = makeMenu("MetaGraph.Edit.Hide");
		edit.add(hide);

		// hide all
		JMenuItem hideAll = makeMenuItem("MetaGraph.Edit.HideAll", "hideall");
		hide.add(hideAll);

		// hide selection
		JMenuItem hideSel = makeMenuItem("MetaGraph.Edit.HideSelection", "hidesel");
		hide.add(hideSel);

		// appearance menu
		JMenu appearance = makeMenu("MetaGraph.Appearance");
		menuBar.add(appearance);

		// labels sub-menu
		JMenu labels = makeMenu("MetaGraph.Appearance.Labels");
		appearance.add(labels);

		// show node labels
		nodeLabels = makeCheckBoxMenuItem("MetaGraph.Appearance.NodeLabels", NODELABELS);
		nodeLabels.setSelected(true);
		labels.add(nodeLabels);

		// show edge labels
		edgeLabels = makeCheckBoxMenuItem("MetaGraph.Appearance.EdgeLabels", EDGELABELS);
		edgeLabels.setSelected(true);
		labels.add(edgeLabels);

		// config sub-menu
		JMenu config = makeMenu("MetaGraph.Appearance.Config");
		appearance.add(config);

		// panel for node size
		JPanel nodePanel = new JPanel(new BorderLayout());
		config.add(nodePanel);
		JLabel nodeSizeLabel = new JLabel(Config.language.getProperty("MetaGraph.Appearance.NodeSize"));
		nodePanel.add(nodeSizeLabel, BorderLayout.WEST);
		nodeSize = new JSpinner(new SpinnerNumberModel(defaultNodeSize, 1, 1000, 1));
		nodeSize.addChangeListener(this);
		forceExactSize(nodeSize, spinnerWidth, spinnerHight);
		nodePanel.add(nodeSize, BorderLayout.EAST);

		// panel for edge thickness
		JPanel edgePanel = new JPanel(new BorderLayout());
		config.add(edgePanel);
		JLabel edgeSizeLabel = new JLabel(Config.language.getProperty("MetaGraph.Appearance.EdgeSize"));
		edgePanel.add(edgeSizeLabel, BorderLayout.WEST);
		edgeSize = new JSpinner(new SpinnerNumberModel(defaultEdgeSize, 0, 100, 1));
		edgeSize.addChangeListener(this);
		forceExactSize(edgeSize, spinnerWidth, spinnerHight);
		edgePanel.add(edgeSize, BorderLayout.EAST);

		// panel for font size
		JPanel fontPanel = new JPanel(new BorderLayout());
		config.add(fontPanel);
		JLabel fontSizeLabel = new JLabel(Config.language.getProperty("MetaGraph.Appearance.FontSize"));
		fontPanel.add(fontSizeLabel, BorderLayout.WEST);
		fontSize = new JSpinner(new SpinnerNumberModel(defaultFontSize, 4, 250, 1));
		fontSize.addChangeListener(this);
		forceExactSize(fontSize, spinnerWidth, spinnerHight);
		fontPanel.add(fontSize, BorderLayout.EAST);

		// scale button
		JMenuItem scaleToFit = makeMenuItem("MetaGraph.Appearance.ScaleToFit", "scaletofit");
		appearance.add(scaleToFit);

		// re-layout button
		JMenuItem relayout = makeMenuItem("MetaGraph.Appearance.ReLayout", "relayout");
		appearance.add(relayout);

		// load button
		JMenuItem load = makeMenuItem("MetaGraph.Appearance.Load", LOAD);
		appearance.add(load);

		// save button
		JMenuItem save = makeMenuItem("MetaGraph.Appearance.Save", SAVE);
		appearance.add(save);

		// set layout
		this.getContentPane().setLayout(new GridLayout(1, 1));
		this.setViewer(viewer);
		this.pack();
	}

	/**
	 * Sets whether or not to show labels on nodes.
	 * 
	 * @param show
	 *            labels on nodes
	 */
	public void showNodeLabels(boolean show) {
		if (show) {
			viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().setVertexLabelTransformer(new ONDEXMetaConceptLabels(viewer.getONDEXJUNGGraph()));
		} else {
			viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().setVertexLabelTransformer(new Transformer<ONDEXMetaConcept, String>() {

				@Override
				public String transform(ONDEXMetaConcept input) {
					return null;
				}
			});
		}
		updateMenuBar();
		viewer.getMetaGraphPanel().repaint();
	}

	/**
	 * Returns whether or not node labels are shown.
	 * 
	 * @return node labels shown
	 */
	public boolean isShowNodeLabels() {
		return viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().getVertexLabelTransformer() instanceof ONDEXMetaConceptLabels;
	}

	/**
	 * Sets whether or not to show labels on edges.
	 * 
	 * @param show
	 *            labels on edges
	 */
	public void showEdgeLabels(boolean show) {
		if (show) {
			viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().setEdgeLabelTransformer(new ONDEXMetaRelationLabels(viewer.getONDEXJUNGGraph()));
		} else {
			viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().setEdgeLabelTransformer(new Transformer<ONDEXMetaRelation, String>() {

				@Override
				public String transform(ONDEXMetaRelation input) {
					return null;
				}
			});
		}
		updateMenuBar();
		viewer.getMetaGraphPanel().repaint();
	}

	/**
	 * Returns whether or not edge labels are shown.
	 * 
	 * @return edge labels shown
	 */
	public boolean isShowEdgeLabels() {
		return viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().getEdgeLabelTransformer() instanceof ONDEXMetaRelationLabels;
	}

	/**
	 * Returns the current set node size.
	 * 
	 * @return node size
	 */
	public Integer getNodeSize() {
		return (Integer) nodeSize.getValue();
	}

	/**
	 * Sets the node size.
	 * 
	 * @param size
	 *            node size
	 */
	public void setNodeSize(Integer size) {
		nodeSize.setValue(size);
	}

	/**
	 * Returns the current set edge size.
	 * 
	 * @return edge size
	 */
	public Integer getEdgeSize() {
		return (Integer) edgeSize.getValue();
	}

	/**
	 * Sets the edge size.
	 * 
	 * @param size
	 *            edge size
	 */
	public void setEdgeSize(Integer size) {
		edgeSize.setValue(size);
	}

	/**
	 * Returns the current set font size.
	 * 
	 * @return
	 */
	public Integer getFontSize() {
		return (Integer) fontSize.getValue();
	}

	/**
	 * Sets the font size.
	 * 
	 * @param size
	 *            font size
	 */
	public void setFontSize(Integer size) {
		fontSize.setValue(size);
	}

	/**
	 * Updates check box settings in menu
	 */
	private void updateMenuBar() {
		nodeLabels.setSelected(this.isShowNodeLabels());
		edgeLabels.setSelected(this.isShowEdgeLabels());
	}

	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// export meta-graph as graphic
		if (cmd.equals("export")) {
			File dir = (Config.lastSavedFile == null) ? new File(System.getProperty("user.dir")) : new File(Config.lastSavedFile);
			DialogExport chooser = new DialogExport(dir);
			chooser.addFormat("graphml");

			int i = chooser.showSaveDialog(viewer);
			if (i == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getFile();
				Config.lastSavedFile = file.getAbsolutePath();

				ImageWriterUtil<ONDEXMetaConcept, ONDEXMetaRelation> iw = new ImageWriterUtil<ONDEXMetaConcept, ONDEXMetaRelation>(this.viewer.getMetaGraphPanel());

				iw.writeImage(file, chooser.getSelectedFormat(), chooser.getScaleFactor());
			}
		}

		// show / hide node labels
		else if (cmd.equals(NODELABELS)) {
			showNodeLabels(nodeLabels.isSelected());
		}

		// show / hide edge labels
		else if (cmd.equals(EDGELABELS)) {
			showEdgeLabels(edgeLabels.isSelected());
		}

		// scale meta-graph to fit
		else if (cmd.equals("scaletofit")) {
			viewer.getMetaGraphPanel().scaleToFit();
		}

		// re-layout meta-graph
		else if (cmd.equals("relayout")) {
			viewer.getMetaGraphPanel().relayout();
		}

		// shows all nodes
		else if (cmd.equals("showall")) {
			viewer.getMetaGraph().updateLegend = false;
			// first show all concepts
			for (ONDEXMetaConcept mc : viewer.getMetaGraph().getVertices()) {
				viewer.getMetaGraph().actionPerformed(new ActionEvent(mc, ActionEvent.ACTION_PERFORMED, "show"));
			}
			// second show all relations
			for (ONDEXMetaRelation mr : viewer.getMetaGraph().getEdges()) {
				viewer.getMetaGraph().actionPerformed(new ActionEvent(mr, ActionEvent.ACTION_PERFORMED, "show"));
			}
			// refresh possible meta data legend
			if (ViewMenuAction.isLegendShown())
				ViewMenuAction.getLegend().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OVTK2Legend.REFRESH));
			viewer.getMetaGraph().updateLegend = true;
		}

		// hides all nodes
		else if (cmd.equals("hideall")) {
			viewer.getMetaGraph().updateLegend = false;
			// first hide all relations
			for (ONDEXMetaRelation mr : viewer.getMetaGraph().getEdges()) {
				viewer.getMetaGraph().actionPerformed(new ActionEvent(mr, ActionEvent.ACTION_PERFORMED, "hide"));
			}
			// second hide all concepts
			for (ONDEXMetaConcept mc : viewer.getMetaGraph().getVertices()) {
				viewer.getMetaGraph().actionPerformed(new ActionEvent(mc, ActionEvent.ACTION_PERFORMED, "hide"));
			}
			// refresh possible meta data legend
			if (ViewMenuAction.isLegendShown())
				ViewMenuAction.getLegend().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OVTK2Legend.REFRESH));
			viewer.getMetaGraph().updateLegend = true;
		}

		// shows selected nodes
		else if (cmd.equals("showsel")) {
			viewer.getMetaGraph().updateLegend = false;
			// first show all concepts
			for (ONDEXMetaConcept mc : viewer.getMetaGraphPanel().getVisualizationViewer().getPickedVertexState().getPicked()) {
				viewer.getMetaGraph().actionPerformed(new ActionEvent(mc, ActionEvent.ACTION_PERFORMED, "show"));
			}
			// second show all relations
			for (ONDEXMetaRelation mr : viewer.getMetaGraphPanel().getVisualizationViewer().getPickedEdgeState().getPicked()) {
				viewer.getMetaGraph().actionPerformed(new ActionEvent(mr, ActionEvent.ACTION_PERFORMED, "show"));
			}
			// refresh possible meta data legend
			if (ViewMenuAction.isLegendShown())
				ViewMenuAction.getLegend().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OVTK2Legend.REFRESH));
			viewer.getMetaGraph().updateLegend = true;
		}

		// hides selected nodes
		else if (cmd.equals("hidesel")) {
			viewer.getMetaGraph().updateLegend = false;
			// first hide all relations
			for (ONDEXMetaRelation mr : viewer.getMetaGraphPanel().getVisualizationViewer().getPickedEdgeState().getPicked()) {
				viewer.getMetaGraph().actionPerformed(new ActionEvent(mr, ActionEvent.ACTION_PERFORMED, "hide"));
			}
			// second hide all concepts
			for (ONDEXMetaConcept mc : viewer.getMetaGraphPanel().getVisualizationViewer().getPickedVertexState().getPicked()) {
				viewer.getMetaGraph().actionPerformed(new ActionEvent(mc, ActionEvent.ACTION_PERFORMED, "hide"));
			}
			// refresh possible meta data legend
			if (ViewMenuAction.isLegendShown())
				ViewMenuAction.getLegend().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OVTK2Legend.REFRESH));
			viewer.getMetaGraph().updateLegend = true;
		}

		// load appearance
		else if (cmd.equals(LOAD)) {

			// get current appearance from graph
			String xml = viewer.getONDEXJUNGGraph().getAnnotations().get(KEY);
			if (xml == null || xml.trim().length() == 0)
				return;

			// configure XML input
			System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
			XMLInputFactory2 xmlInput = (XMLInputFactory2) XMLInputFactory2.newInstance();
			xmlInput.configureForSpeed();

			// parse from a String
			final ByteArrayInputStream inStream = new ByteArrayInputStream(xml.getBytes());
			try {
				// configure Parser
				XMLStreamReader2 xmlReadStream = (XMLStreamReader2) xmlInput.createXMLStreamReader(inStream, CharsetNames.CS_UTF8);

				// de-serialise appearance from XML
				MetaGraphXMLReader.read(xmlReadStream, this);

				xmlReadStream.close();

			} catch (XMLStreamException e) {
				ErrorDialog.show(e);
			}
		}

		// save appearance
		else if (cmd.equals(SAVE)) {

			// configure XML output
			XMLOutputFactory2 xmlOutput = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
			xmlOutput.configureForSpeed();
			xmlOutput.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);

			// output goes into a String
			final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			try {
				// configure writer
				XMLStreamWriter2 xmlWriteStream = (XMLStreamWriter2) xmlOutput.createXMLStreamWriter(outStream, CharsetNames.CS_UTF8);

				// serialise appearance to XML
				MetaGraphXMLWriter.write(xmlWriteStream, this);

				xmlWriteStream.flush();
				xmlWriteStream.close();

				// set appearance data to graph
				viewer.getONDEXJUNGGraph().getAnnotations().put(KEY, outStream.toString());

			} catch (XMLStreamException e) {
				ErrorDialog.show(e);
			}
		}
	}

	/**
	 * Sets exact size of a component
	 * 
	 * @param c
	 * @param width
	 * @param height
	 */
	private void forceExactSize(Component c, int width, int height) {
		c.setMinimumSize(new Dimension(width, height));
		c.setMaximumSize(new Dimension(width, height));
		c.setPreferredSize(new Dimension(width, height));
		c.setSize(width, height);
	}

	@Override
	public String getGroup() {
		return "MetaGraph";
	}

	/**
	 * Returns current viewer.
	 * 
	 * @return OVTK2Viewer
	 */
	public OVTK2Viewer getViewer() {
		return viewer;
	}

	/**
	 * Sets frame icon from file.
	 * 
	 */
	private void initIcon() {
		File imgLocation = new File("config/toolbarButtonGraphics/development/Application16.gif");
		URL imageURL = null;

		try {
			imageURL = imgLocation.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		this.setFrameIcon(new ImageIcon(imageURL));
	}

	/**
	 * Create a JCheckBoxMenuItem labelled with text for the given property key.
	 * 
	 * @param key
	 *            property key
	 * @param actionCommand
	 *            internal command
	 * @return JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem makeCheckBoxMenuItem(String key, String actionCommand) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(Config.language.getProperty(key));
		item.setActionCommand(actionCommand);
		item.addActionListener(this);
		return item;
	}

	/**
	 * Create a JMenu labelled with text for the given property key.
	 * 
	 * @param key
	 *            property key
	 * @return JMenu
	 */
	private JMenu makeMenu(String key) {
		JMenu menu = new JMenu(Config.language.getProperty(key));
		menu.setMnemonic(Config.language.getProperty(key).charAt(0));
		return menu;
	}

	/**
	 * Create a JMenuItem labelled with text for the given property key.
	 * 
	 * @param key
	 *            property key
	 * @param actionCommand
	 *            internal command
	 * @return JMenuItem
	 */
	private JMenuItem makeMenuItem(String key, String actionCommand) {
		JMenuItem item = new JMenuItem(Config.language.getProperty(key));
		item.setActionCommand(actionCommand);
		item.addActionListener(this);
		return item;
	}

	/**
	 * Sets viewer to be used in meta-graph view.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public void setViewer(OVTK2Viewer viewer) {
		this.viewer = viewer;
		this.getContentPane().removeAll();
		this.getContentPane().add(viewer.getMetaGraphPanel());
		// reset to default values
		this.nodeLabels.setSelected(true);
		this.edgeLabels.setSelected(true);
		this.nodeSize.setValue(defaultNodeSize);
		this.edgeSize.setValue(defaultEdgeSize);
		this.fontSize.setValue(defaultFontSize);
		this.updateUI();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == nodeSize) {
			// scale node shapes to size
			ONDEXMetaConceptShapes nodeShapes = (ONDEXMetaConceptShapes) viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().getVertexShapeTransformer();
			nodeShapes.setSize((Integer) nodeSize.getValue());
		} else if (e.getSource() == edgeSize) {
			// update thickness of edge strokes
			ONDEXMetaRelationStrokes edgeStrokes = (ONDEXMetaRelationStrokes) viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().getEdgeStrokeTransformer();
			edgeStrokes.setThickness((Integer) edgeSize.getValue());
		} else if (e.getSource() == fontSize && viewer.getMetaGraph().getVertexCount() > 0) {
			// set new font size
			Font f = viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().getVertexFontTransformer().transform(viewer.getMetaGraph().getVertices().iterator().next());
			LabelTransformer<ONDEXMetaRelation, Font> ef = new LabelTransformer<ONDEXMetaRelation, Font>(f, (Integer) fontSize.getValue());
			viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().setEdgeFontTransformer(ef);
			LabelTransformer<ONDEXMetaConcept, Font> cf = new LabelTransformer<ONDEXMetaConcept, Font>(f, (Integer) fontSize.getValue());
			viewer.getMetaGraphPanel().getVisualizationViewer().getRenderContext().setVertexFontTransformer(cf);
		}

		viewer.getMetaGraphPanel().repaint();
	}

}