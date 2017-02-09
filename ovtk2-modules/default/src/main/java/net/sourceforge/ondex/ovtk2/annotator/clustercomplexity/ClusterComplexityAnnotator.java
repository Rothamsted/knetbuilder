package net.sourceforge.ondex.ovtk2.annotator.clustercomplexity;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.ondex.algorithm.entropy.ShannonEntropy;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.BronKerboschCliqueFinder;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.VisualisationUtils;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Identifies all maximal cliques in a graph and ranks them according to their
 * complexity (entropy measure) based on a Attribute attribute.
 * 
 * @author taubertj
 * 
 */
public class ClusterComplexityAnnotator extends OVTK2Annotator implements
		ActionListener, ListSelectionListener, InternalFrameListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -6250734636258076283L;

	/**
	 * the selected attribute name.
	 */
	private AttributeName an;

	/**
	 * the list model for the above list.
	 */
	private AttributeNameListModel anlm;

	/**
	 * contains all maximal cliques in graph
	 */
	private List<Set<ONDEXConcept>> cliques;

	/**
	 * clique number to entropy measure
	 */
	private Map<Integer, Double> entropies;

	/**
	 * the start button.
	 */
	private JButton goButton;

	/**
	 * the list containing the fitting attribute names.
	 */
	private JList list;

	/**
	 * result table
	 */
	private JTable table;

	/**
	 * spinner for cluster size
	 */
	private JSpinner spinner;

	/**
	 * Result table
	 */
	private RegisteredJInternalFrame results;

	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	public ClusterComplexityAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);

		setLayout(new SpringLayout());

		SpinnerModel model = new SpinnerNumberModel(3, 2, 100, 1);
		spinner = new JSpinner(model);

		anlm = new AttributeNameListModel();

		// close result frame when viewer closes
		if (viewer instanceof OVTK2Viewer)
			((OVTK2Viewer) viewer).addInternalFrameListener(this);

		// The magic button
		goButton = new JButton("Annotate Graph");
		goButton.setEnabled(false);

		list = new JList(anlm);
		list.setCellRenderer(new CustomCellRenderer());

		addAttributeNamesToList();

		if (anlm.getSize() == 0) {
			add(new JLabel("There are no suitable attributes in the graph."));
		} else {
			list.validate();
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					int index = list.getSelectedIndex();
					if (index > -1)
						goButton.setEnabled(true);
				}

			});

			add(new JLabel("Select attribute to annotate concepts with"));
			add(new JScrollPane(list));
			add(new JLabel("Select minimum cluster size:"));
			add(spinner);

			goButton.addActionListener(this);
			add(goButton);
		}

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
				5, 5, 5);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int index = list.getSelectedIndex();
		if (index > -1) {
			String attNameName = ((JLabel) list.getModel().getElementAt(index))
					.getName();
			an = graph.getMetaData().getAttributeName(attNameName);

			// find all cliques in graph
			BronKerboschCliqueFinder<ONDEXConcept, ONDEXRelation> bkcf = new BronKerboschCliqueFinder<ONDEXConcept, ONDEXRelation>(
					this.graph);
			cliques = new ArrayList<Set<ONDEXConcept>>(); // as a list
			for (Set<ONDEXConcept> clique : bkcf.getAllMaximalCliques()) {
				cliques.add(clique);
			}

			// calculate entropy of clique according to AttributeName
			entropies = new HashMap<Integer, Double>();
			int i = 0;
			int min = (Integer) spinner.getValue();
			for (Set<ONDEXConcept> clique : cliques) {
				if (clique.size() >= min) {
					List<String> values = new ArrayList<String>();
					for (ONDEXConcept n : clique) {
						Attribute attribute = n.getAttribute(an);
						if (attribute != null) {
							values.add(attribute.getValue().toString());
						}
					}
					Double entropy = ShannonEntropy
							.calculateShannonEntropy(values);
					if (entropy > 0)
						entropies.put(Integer.valueOf(i), entropy);
				}
				i++;
			}

			// display result ranking
			showResults();

			used = true;
		}
	}

	/**
	 * adds all suitable attribute names to the jlist.
	 */
	private void addAttributeNamesToList() {
		for (AttributeName attn : graph.getMetaData().getAttributeNames()) {
			// should also accept list types now.
			if (Comparable.class.isAssignableFrom(attn.getDataType())
					|| Collection.class.isAssignableFrom(attn.getDataType())) {
				Set<ONDEXConcept> concepts = graph
						.getConceptsOfAttributeName(attn);
				if (concepts != null && concepts.size() > 0
						&& !AppearanceSynchronizer.attr.contains(attn.getId()))
					anlm.addAttributeName(attn);
			}
		}
	}

	@Override
	public String getName() {
		return Config.language
				.getProperty("Name.Menu.Annotator.ClusterComplexity");
	}

	private void showResults() {

		// display frame for results
		if (results != null)
			try {
				results.setClosed(true);
			} catch (PropertyVetoException v) {
				ErrorDialog.show(v);
			}
		results = new RegisteredJInternalFrame("Result ranking min size "
				+ spinner.getValue(), "Annotator",
				Config.language
						.getProperty("Name.Menu.Annotator.ClusterComplexity")
						+ " - Result", true, true, true, true);
		results.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		results.setLayout(new GridLayout(1, 1));

		// copy data into table structure
		String[] columnNames = new String[] { "Cluster nb", "Entropy",
				"Cluster size" };
		Object[][] data = new Object[entropies.size()][3];
		int i = 0;
		for (Integer key : entropies.keySet()) {
			data[i][0] = key;
			data[i][1] = entropies.get(key);
			data[i][2] = cliques.get(key).size();
			i++;
		}

		// result table with single selection
		table = new JTable(data, columnNames);
		table.setAutoCreateRowSorter(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		results.add(new JScrollPane(table));

		// add frame to desktop
		results.pack();
		OVTK2Desktop.getInstance().display(results, Position.leftTop);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		// get pick state from viewer
		PickedState<ONDEXConcept> state = viewer.getVisualizationViewer()
				.getPickedVertexState();
		state.clear();

		if (!lsm.isSelectionEmpty()) {
			// Find out which indexes are selected.
			int minIndex = lsm.getMinSelectionIndex();
			int maxIndex = lsm.getMaxSelectionIndex();
			for (int i = minIndex; i <= maxIndex; i++) {
				if (lsm.isSelectedIndex(i)) {
					int index = table.convertRowIndexToModel(i);
					Integer selection = (Integer) table.getModel().getValueAt(
							index, 0);
					// pick nodes in clique
					Set<ONDEXConcept> clique = cliques.get(selection);
					for (ONDEXConcept n : clique) {
						state.pick(n, true);
					}
				}
			}

			if (state.getPicked().size() > 1 && viewer instanceof OVTK2Viewer)
				// zooming into
				VisualisationUtils.zoomIn((OVTK2Viewer) viewer);
		}
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		try {
			results.setClosed(true);
		} catch (PropertyVetoException v) {
			ErrorDialog.show(v);
		}
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
