package net.sourceforge.ondex.ovtk2.io;

import static net.sourceforge.ondex.export.fasta.ArgumentNames.CONCEPTS_ARG;
import static net.sourceforge.ondex.export.fasta.ArgumentNames.HEADER_FIELDS_ARG;
import static net.sourceforge.ondex.export.fasta.ArgumentNames.INCLUDE_VARIENTS_ARG;
import static net.sourceforge.ondex.export.fasta.ArgumentNames.SEQUENCE_TYPE_ARG;
import static net.sourceforge.ondex.export.fasta.ArgumentNames.TRANSLATE_TAXID_ARG;
import static net.sourceforge.ondex.export.fasta.ArgumentNames.ZIP_FILE_ARG;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.fasta.Export;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * This is a work in progress...It is no where near finished.
 * 
 * @author hindlem
 */
public class FASTAExporter extends JInternalFrame implements ActionListener,
		OVTK2IO {

	private static final long serialVersionUID = 1L;

	private static final String UPDATE = "UPDATE";
	private static final String GO = "GO";

	private ONDEXGraph graph;

	public FASTAExporter() {
	}

	private Map<String, Integer> selected = new HashMap<String, Integer>();

	private JList selectedConcepts;

	private File file;

	private void init() {
		// dispose viewer on close
		this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel(new SpringLayout());

		updateSelectedNodes();
		selectedConcepts = new JList(selected.keySet().toArray());
		mainPanel.add(new JScrollPane(selectedConcepts));

		JButton updateSelected = new JButton("Update Selected");
		updateSelected.addActionListener(this);
		updateSelected.setActionCommand(UPDATE);
		mainPanel.add(updateSelected);

		JComboBox attribute = new JComboBox(getAttributeNames());
		attribute.setActionCommand(UPDATE);
		mainPanel.add(attribute);

		JCheckBox translateTaxIds = new JCheckBox("Translate Taxid");
		translateTaxIds.setSelected(true);

		JCheckBox includeVarients = new JCheckBox(
				"Include merged sequence varients");
		includeVarients.setSelected(false);

		JButton exportGo = new JButton("Export");
		exportGo.addActionListener(this);
		exportGo.setActionCommand(GO);
		exportGo.setEnabled(false);
		mainPanel.add(exportGo);

		SpringUtilities.makeCompactGrid(mainPanel,
				mainPanel.getComponentCount(), 1, 5, 5, 5, 5);

		// set layout
		this.getContentPane().setLayout(new GridLayout(1, 1));
		this.getContentPane().add(mainPanel);
		this.pack();

		OVTK2Desktop.getInstance().getDesktopPane().add(this);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals(UPDATE)) {
			updateSelectedNodes();
			selectedConcepts = new JList(selected.keySet().toArray());
			validate();
			repaint();
		} else if (e.getActionCommand().equals(GO)) {

			if (file != null && selectedConcepts.getSelectedValues().length > 0) {
				Object[] values = selectedConcepts.getSelectedValues();
				Integer[] cids = new Integer[values.length];

				for (int i = 0; i < values.length; i++) {
					cids[i] = selected.get(values[i]);
				}

				try {
					Export export = new Export();

					ONDEXPluginArguments ea = new ONDEXPluginArguments(
							export.getArgumentDefinitions());
					ea.setOption(FileArgumentDefinition.EXPORT_FILE,
							file.getAbsolutePath());
					ea.setOption(CONCEPTS_ARG, cids);
					ea.setOption(SEQUENCE_TYPE_ARG, values);
					ea.setOption(HEADER_FIELDS_ARG, null);
					ea.setOption(TRANSLATE_TAXID_ARG, null);
					ea.setOption(INCLUDE_VARIENTS_ARG, null);
					ea.setOption(ZIP_FILE_ARG, null);
					export.setArguments(ea);

					export.setONDEXGraph(graph);
					export.start();
				} catch (InvalidPluginArgumentException e1) {
					ErrorDialog.show(e1);
				} catch (Exception e2) {
					ErrorDialog.show(e2);
				}
			}
		}
	}

	private void updateSelectedNodes() {
		selected.clear();
		OVTK2PropertiesAggregator viewer = OVTK2Desktop.getDesktopResources()
				.getSelectedViewer();
		if (viewer != null) {
			PickedState<ONDEXConcept> state = viewer.getVisualizationViewer()
					.getPickedVertexState();
			Set<ONDEXConcept> set = state.getPicked();

			for (ONDEXConcept node : set) {
				ONDEXConcept ac = node;
				selected.put(ac.getId() + ":" + ac.getPID(), ac.getId());
			}
		}
	}

	private String[] getAttributeNames() {
		Set<AttributeName> atts = graph.getMetaData().getAttributeNames();
		String[] names = new String[atts.size()];
		int i = 0;
		for (AttributeName at : atts) {
			names[i] = at.getId();
			i++;
		}
		Arrays.sort(names);
		return names;
	}

	@Override
	public void start(File file) {
		this.file = file;
		init();
	}

	@Override
	public void setGraph(ONDEXGraph graph) {
		this.graph = graph;
	}

	@Override
	public String getExt() {
		return "fasta";
	}

	@Override
	public boolean isImport() {
		return false;
	}
}
