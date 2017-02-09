package net.sourceforge.ondex.ovtk2.annotator.scaling;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

/**
 * Scales concepts and relation according to their meta data.
 * 
 * @author taubertj
 * 
 */
public class ScalingAnnotator extends OVTK2Annotator implements ActionListener {

	/**
	 * Make sure meta data id is not editable.
	 * 
	 * @author taubertj
	 * 
	 */
	private class MyTableModel extends DefaultTableModel {

		/**
		 * generated
		 */
		private static final long serialVersionUID = 2747602295399820654L;

		/**
		 * From super class.
		 * 
		 * @param data
		 * @param columnNames
		 */
		public MyTableModel(Object[][] data, Object[] columnNames) {
			super(data, columnNames);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 0)
				return false;
			return super.isCellEditable(row, column);
		}
	}

	/**
	 * generated
	 */
	private static final long serialVersionUID = -1543742100221307550L;

	/**
	 * Contains sizes for concept classes
	 */
	private JTable ccTable = null;

	/**
	 * Contains sizes for relation types
	 */
	private JTable rtTable = null;
	
	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	/**
	 * Map with scaling per concept id
	 */
	private final Map<ONDEXConcept, Integer> ccScaling = LazyMap.decorate(
			new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
				@Override
				public Integer create() {
					return Config.defaultNodeSize;
				}
			});

	/**
	 * Map with scaling per relation id
	 */
	private final Map<ONDEXRelation, Integer> rtScaling = LazyMap.decorate(
			new HashMap<ONDEXRelation, Integer>(), new Factory<Integer>() {
				@Override
				public Integer create() {
					return Config.defaultEdgeSize;
				}
			});

	/**
	 * Constructor to work with the current OVTK2Viewer.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public ScalingAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);

		// construct concept class table
		String[] ccHeader = new String[] { "ConceptClass", "Node Size" };
		Object[][] ccData = extractConceptClasses();
		ccTable = new JTable(new MyTableModel(ccData, ccHeader));

		// construct relation type table
		String[] rtHeader = new String[] { "RelationType", "Relation Width" };
		Object[][] rtData = extractRelationTypes();
		rtTable = new JTable(new MyTableModel(rtData, rtHeader));

		// define layout of elements
		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		this.setLayout(layout);

		// scroll pane for concept classes
		JScrollPane ccPane = new JScrollPane(ccTable);
		ccPane.setBorder(BorderFactory.createTitledBorder("ConceptClasses"));
		this.add(ccPane);

		// scroll pane for relation types
		JScrollPane rtPane = new JScrollPane(rtTable);
		rtPane.setBorder(BorderFactory.createTitledBorder("RelationTypes"));
		this.add(rtPane);

		// the important button
		JButton go = new JButton("Annotate");
		go.addActionListener(this);
		this.add(go);

		// not too big
		this.setPreferredSize(new Dimension(400, 600));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// get final results for concept classes
		ccScaling.clear();
		MyTableModel ccModel = (MyTableModel) ccTable.getModel();
		for (Object o : ccModel.getDataVector()) {
			Vector<?> row = (Vector<?>) o;
			String id = (String) row.get(0);
			int size = Integer.parseInt(row.get(1).toString());
			ConceptClass cc = graph.getMetaData().getConceptClass(id);
			for (ONDEXConcept c : graph.getConceptsOfConceptClass(cc)) {
				ccScaling.put(c, size);
			}
		}

		// change node sizes
		viewer.getNodeShapes().setNodeSizes(
				new Transformer<ONDEXConcept, Integer>() {
					@Override
					public Integer transform(ONDEXConcept arg0) {
						return ccScaling.get(arg0);
					}
				});
		viewer.getNodeShapes().updateAll();

		// get final results for relation types
		rtScaling.clear();
		MyTableModel rtModel = (MyTableModel) rtTable.getModel();
		for (Object o : rtModel.getDataVector()) {
			Vector<?> row = (Vector<?>) o;
			String id = (String) row.get(0);
			int size = Integer.parseInt(row.get(1).toString());
			RelationType rt = graph.getMetaData().getRelationType(id);
			for (ONDEXRelation r : graph.getRelationsOfRelationType(rt)) {
				rtScaling.put(r, size);
			}
		}

		// change edge sizes
		viewer.getEdgeStrokes().setEdgeSizes(
				new Transformer<ONDEXRelation, Integer>() {
					@Override
					public Integer transform(ONDEXRelation arg0) {
						return rtScaling.get(arg0);
					}
				});

		// update visualisation
		viewer.getVisualizationViewer().getModel().fireStateChanged();
		
		used = true;
	}

	/**
	 * Initialises the table data for concept classes with default size.
	 * 
	 * @return table data
	 */
	private Object[][] extractConceptClasses() {
		// get all concept classes and check for visible concepts
		List<String> ccs = new ArrayList<String>();
		for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
			if (graph.getConceptsOfConceptClass(cc).size() > 0) {
				ccs.add(cc.getId());
			}
		}

		// sort by concept class ids
		String[] ccsSorted = ccs.toArray(new String[0]);
		Arrays.sort(ccsSorted, String.CASE_INSENSITIVE_ORDER);

		// initialise data with default size
		Object[][] ccData = new Object[ccsSorted.length][2];
		for (int i = 0; i < ccsSorted.length; i++) {
			ccData[i][0] = ccsSorted[i];
			ccData[i][1] = Config.defaultNodeSize;
		}

		return ccData;
	}

	/**
	 * Initialises the table data for relation types with default size.
	 * 
	 * @return table data
	 */
	private Object[][] extractRelationTypes() {
		// get all relation types and check for visible relations
		List<String> rts = new ArrayList<String>();
		for (RelationType rt : graph.getMetaData().getRelationTypes()) {
			if (graph.getRelationsOfRelationType(rt).size() > 0) {
				rts.add(rt.getId());
			}
		}

		// sort by relation type ids
		String[] rtsSorted = rts.toArray(new String[0]);
		Arrays.sort(rtsSorted, String.CASE_INSENSITIVE_ORDER);

		// initialise data with default size
		Object[][] rtData = new Object[rtsSorted.length][2];
		for (int i = 0; i < rtsSorted.length; i++) {
			rtData[i][0] = rtsSorted[i];
			rtData[i][1] = Config.defaultEdgeSize;
		}

		return rtData;
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.Scaling");
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
