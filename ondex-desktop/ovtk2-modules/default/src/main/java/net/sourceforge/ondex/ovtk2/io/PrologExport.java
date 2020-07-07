package net.sourceforge.ondex.ovtk2.io;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.export.prolog.Export;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

/**
 * Provides a mapping from the ONDEX graph into clauses for Prolog.
 * 
 * @author taubertj
 */
public class PrologExport implements ActionListener, OVTK2IO {

	/**
	 * Current displaying ONDEX graph
	 */
	private ONDEXGraph graph;

	/**
	 * Reference to current viewer
	 */
	private OVTK2PropertiesAggregator viewer;

	/**
	 * The file to export to
	 */
	private File file;

	/**
	 * Contains GUI for relation type mapping
	 */
	private JInternalFrame frameFirstStep;

	/**
	 * Contains GUI for concept class mapping
	 */
	private JInternalFrame frameSecondStep;

	/**
	 * Contains GUI for Attribute attribute name mapping
	 */
	private JInternalFrame frameThirdStep;

	/**
	 * Mappings for relation types to clause names
	 */
	private Map<String, JTextField> fieldsFirstStep = new Hashtable<String, JTextField>();

	/**
	 * Mapping for concept classes to clause names
	 */
	private Map<String, JTextField> fieldsSecondStep = new Hashtable<String, JTextField>();

	/**
	 * Mapping for Attribute attribute name to clause names
	 */
	private Map<String, JTextField> fieldsThirdStep = new Hashtable<String, JTextField>();

	/**
	 * Whether or not to take a particular relation type into consideration
	 */
	private Map<String, JCheckBox> boxesFirstStep = new Hashtable<String, JCheckBox>();

	/**
	 * Whether or not to take a particular concept class into consideration
	 */
	private Map<String, JCheckBox> boxesSecondStep = new Hashtable<String, JCheckBox>();

	/**
	 * Whether or not to output a particular Attribute
	 */
	private Map<String, JCheckBox> boxesThirdStep = new Hashtable<String, JCheckBox>();

	/**
	 * Names of ONDEX relation types
	 */
	private List<JLabel> labelsFirstStep = new ArrayList<JLabel>();

	/**
	 * Names of ONDEX concept classes
	 */
	private List<JLabel> labelsSecondStep = new ArrayList<JLabel>();

	/**
	 * Names of ONDEX Attribute attribute names
	 */
	private List<JLabel> labelsThirdStep = new ArrayList<JLabel>();

	/**
	 * Turn off concatenation of names with ids.
	 */
	private JCheckBox concatNames = new JCheckBox(
			"Concatenate concept id and parser ids?");

	/**
	 * Turn on length restriction for quoted Strings
	 */
	private JCheckBox restrictLength = new JCheckBox(
			"Restrict length of any quoted String?");

	public PrologExport() {
	}

	/**
	 * Builds first step of relation type to clause name mapping.
	 */
	private void initFirstStep() {

		// get all graph related things
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
		ONDEXGraphMetaData meta = graph.getMetaData();

		// setup frame and layout
		frameFirstStep = new JInternalFrame("Prolog Export First Step...",
				false, true, false, true);
		Container contentPane = frameFirstStep.getContentPane();
		GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		// groups in two directions
		ParallelGroup horizontal = layout.createParallelGroup();
		SequentialGroup vertical = layout.createSequentialGroup();

		// add hint to frame
		JLabel hint = new JLabel(
				"Select relationships to export and define clause names.");
		contentPane.add(hint);

		// align current group
		horizontal.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(hint));
		vertical.addGroup(layout.createBaselineGroup(false, false)
				.addComponent(hint));

		// check for non-empty relation types
		for (RelationType rt : meta.getRelationTypes()) {
			boolean nonempty = false;
			// check if there are actually relations for this type in the graph
			for (ONDEXRelation r : graph.getRelationsOfRelationType(rt)) {
				ONDEXRelation edge = jung.getRelation(r.getId());
				// if at least one edge is visible, stop here
				if (jung.isVisible(edge)) {
					nonempty = true;
					break;
				}
			}
			// add relation type to user selection
			if (nonempty) {
				// first the ONDEX name
				JLabel label = new JLabel(rt.getId());
				label.setToolTipText(rt.getFullname());
				contentPane.add(label);
				labelsFirstStep.add(label);
				// second text field with ONDEX name as default
				String rtid = rt.getFullname();
				if (rtid == null || rtid.trim().length() == 0)
					rtid = rt.getId();
				JTextField field = new JTextField(Export.makeLower(rtid));
				fieldsFirstStep.put(rt.getId(), field);
				contentPane.add(field);
				// third check box for relation type selection
				JCheckBox box = new JCheckBox();
				box.setSelected(true);
				boxesFirstStep.put(rt.getId(), box);
				contentPane.add(box);

				// align current group
				horizontal.addGroup(layout.createSequentialGroup()
						.addComponent(label).addComponent(field)
						.addComponent(box));
				vertical.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label).addComponent(field)
						.addComponent(box));
			}
		}

		// link sizes of all label buttons
		layout.linkSize(SwingConstants.HORIZONTAL, fieldsFirstStep.values()
				.toArray(new JTextField[0]));
		layout.linkSize(SwingConstants.HORIZONTAL,
				labelsFirstStep.toArray(new JLabel[0]));
		layout.linkSize(SwingConstants.VERTICAL, boxesFirstStep.values()
				.toArray(new JCheckBox[0]));
		layout.setHorizontalGroup(horizontal);
		layout.setVerticalGroup(vertical);

		// the magic button
		JButton button = new JButton("Next");
		button.setActionCommand("second");
		button.addActionListener(this);
		contentPane.add(button);

		// align current group
		horizontal.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(button));
		vertical.addGroup(layout.createBaselineGroup(false, false)
				.addComponent(button));

		// add frame to desktop
		frameFirstStep.pack();
		frameFirstStep.setVisible(true);
		OVTK2Desktop.getDesktopResources().getParentPane().add(frameFirstStep);
		frameFirstStep.toFront();
		try {
			frameFirstStep.setSelected(true);
		} catch (PropertyVetoException pve) {
			// ignore
		}
	}

	/**
	 * Builds second step of concept class to clause name mapping.
	 */
	private void initSecondStep() {

		// get all graph related things
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
		ONDEXGraphMetaData meta = graph.getMetaData();

		// setup frame and layout
		frameSecondStep = new JInternalFrame("Prolog Export Second Step...",
				false, true, false, true);
		Container contentPane = frameSecondStep.getContentPane();
		GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		// groups in two directions
		ParallelGroup horizontal = layout.createParallelGroup();
		SequentialGroup vertical = layout.createSequentialGroup();

		// add hint to frame
		JLabel hint = new JLabel(
				"Select conceptclasses to export and define clause names.");
		contentPane.add(hint);

		// align current group
		horizontal.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(hint));
		vertical.addGroup(layout.createBaselineGroup(false, false)
				.addComponent(hint));

		// check for non-empty concept classes
		for (ConceptClass cc : meta.getConceptClasses()) {
			boolean nonempty = false;
			// check if there are actually concepts for this class in the graph
			for (ONDEXConcept c : graph.getConceptsOfConceptClass(cc)) {
				ONDEXConcept node = jung.getConcept(c.getId());
				// if at least one node is visible, stop here
				if (jung.isVisible(node)) {
					nonempty = true;
					break;
				}
			}
			// add concept class to user selection
			if (nonempty) {
				// first the ONDEX name
				JLabel label = new JLabel(cc.getId());
				label.setToolTipText(cc.getFullname());
				contentPane.add(label);
				labelsSecondStep.add(label);
				// second text field with ONDEX name as default
				String ccid = cc.getFullname();
				if (ccid == null || ccid.trim().length() == 0)
					ccid = cc.getId();
				JTextField field = new JTextField(Export.makeLower(ccid));
				fieldsSecondStep.put(cc.getId(), field);
				contentPane.add(field);
				// third check box for concept class selection
				JCheckBox box = new JCheckBox();
				box.setSelected(true);
				boxesSecondStep.put(cc.getId(), box);
				contentPane.add(box);

				// align current group
				horizontal.addGroup(layout.createSequentialGroup()
						.addComponent(label).addComponent(field)
						.addComponent(box));
				vertical.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label).addComponent(field)
						.addComponent(box));
			}
		}

		// link sizes of all label buttons
		layout.linkSize(SwingConstants.HORIZONTAL, fieldsSecondStep.values()
				.toArray(new JTextField[0]));
		layout.linkSize(SwingConstants.HORIZONTAL,
				labelsSecondStep.toArray(new JLabel[0]));
		layout.linkSize(SwingConstants.VERTICAL, boxesSecondStep.values()
				.toArray(new JCheckBox[0]));
		layout.setHorizontalGroup(horizontal);
		layout.setVerticalGroup(vertical);

		// the magic button
		JButton button = new JButton("Next");
		button.setActionCommand("third");
		button.addActionListener(this);
		contentPane.add(button);

		// align current group
		horizontal.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(button));
		vertical.addGroup(layout.createBaselineGroup(false, false)
				.addComponent(button));

		// add frame to desktop
		frameSecondStep.pack();
		frameSecondStep.setVisible(true);
		OVTK2Desktop.getDesktopResources().getParentPane().add(frameSecondStep);
		frameSecondStep.toFront();
		try {
			frameSecondStep.setSelected(true);
		} catch (PropertyVetoException pve) {
			// ignore
		}
	}

	/**
	 * Builds third step of attribute name to clause name mapping.
	 */
	private void initThirdStep() {

		// get all graph related things
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
		ONDEXGraphMetaData meta = graph.getMetaData();

		// setup frame and layout
		frameThirdStep = new JInternalFrame("Prolog Export Third Step...",
				false, true, false, true);
		Container contentPane = frameThirdStep.getContentPane();
		GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		// groups in two directions
		ParallelGroup horizontal = layout.createParallelGroup();
		SequentialGroup vertical = layout.createSequentialGroup();

		// add hint to frame
		JLabel hint = new JLabel(
				"Select Attribute to export and define clause names.");
		contentPane.add(hint);

		// align current group
		horizontal.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(hint));
		vertical.addGroup(layout.createBaselineGroup(false, false)
				.addComponent(hint));

		// check for non-empty attribute name
		for (AttributeName an : meta.getAttributeNames()) {
			boolean nonempty = false;
			// check if there are actually concepts for this attribute name
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(an)) {
				ONDEXConcept node = jung.getConcept(c.getId());
				// if at least one node is visible, stop here
				if (jung.isVisible(node)) {
					nonempty = true;
					break;
				}
			}
			// check if there are actually relation for this attribute name
			for (ONDEXRelation r : graph.getRelationsOfAttributeName(an)) {
				ONDEXRelation edge = jung.getRelation(r.getId());
				// if at least one edge is visible, stop here
				if (jung.isVisible(edge)) {
					nonempty = true;
					break;
				}
			}
			// add attribute name to user selection
			if (nonempty) {
				// first the ONDEX name
				JLabel label = new JLabel(an.getId());
				label.setToolTipText(an.getFullname());
				contentPane.add(label);
				labelsThirdStep.add(label);
				// second text field with ONDEX name as default
				String anid = an.getFullname();
				if (anid == null || anid.trim().length() == 0)
					anid = an.getId();
				JTextField field = new JTextField(Export.makeLower(anid));
				fieldsThirdStep.put(an.getId(), field);
				contentPane.add(field);
				// third check box for attribute name selection
				JCheckBox box = new JCheckBox();
				box.setSelected(false);
				boxesThirdStep.put(an.getId(), box);
				contentPane.add(box);

				// align current group
				horizontal.addGroup(layout.createSequentialGroup()
						.addComponent(label).addComponent(field)
						.addComponent(box));
				vertical.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label).addComponent(field)
						.addComponent(box));
			}
		}

		// link sizes of all label buttons
		if (fieldsThirdStep.size() > 0) {
			layout.linkSize(SwingConstants.HORIZONTAL, fieldsThirdStep.values()
					.toArray(new JTextField[0]));
			layout.linkSize(SwingConstants.HORIZONTAL,
					labelsThirdStep.toArray(new JLabel[0]));
			layout.linkSize(SwingConstants.VERTICAL, boxesThirdStep.values()
					.toArray(new JCheckBox[0]));
		}
		layout.setHorizontalGroup(horizontal);
		layout.setVerticalGroup(vertical);

		concatNames.setSelected(false);
		contentPane.add(concatNames);

		restrictLength.setSelected(false);
		contentPane.add(restrictLength);

		// the magic button
		JButton button = new JButton("Finish");
		button.setActionCommand("finish");
		button.addActionListener(this);
		contentPane.add(button);

		// align current group
		horizontal.addGroup(
				Alignment.TRAILING,
				layout.createParallelGroup()
						.addComponent(restrictLength)
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(concatNames)
										.addComponent(button)));
		vertical.addGroup(layout
				.createSequentialGroup()
				.addComponent(restrictLength)
				.addGroup(
						layout.createBaselineGroup(false, false)
								.addComponent(concatNames).addComponent(button)));

		// add frame to desktop
		frameThirdStep.pack();
		frameThirdStep.setVisible(true);
		OVTK2Desktop.getDesktopResources().getParentPane().add(frameThirdStep);
		frameThirdStep.toFront();
		try {
			frameThirdStep.setSelected(true);
		} catch (PropertyVetoException pve) {
			// ignore
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String ae = e.getActionCommand();
		if (ae.equals("second")) {
			// close frame
			try {
				frameFirstStep.setClosed(true);
			} catch (PropertyVetoException pve) {
				pve.printStackTrace();
			}
			initSecondStep();
		} else if (ae.equals("third")) {
			// close frame
			try {
				frameSecondStep.setClosed(true);
			} catch (PropertyVetoException pve) {
				pve.printStackTrace();
			}
			initThirdStep();
		} else if (ae.equals("finish")) {
			// close frame
			try {
				frameThirdStep.setClosed(true);
			} catch (PropertyVetoException pve) {
				pve.printStackTrace();
			}
			// start exporter
			Export export = new Export();
			try {
				// compose arguments and start exporter
				ONDEXPluginArguments args = new ONDEXPluginArguments(
						export.getArgumentDefinitions());
				args.setOption(FileArgumentDefinition.EXPORT_FILE,
						file.getAbsolutePath());
				args.addOption(Export.CONCAT_ARG,
						new Boolean(concatNames.isSelected()));
				args.addOption(Export.RESTRICTLENGTH_ARG, new Boolean(
						restrictLength.isSelected()));

				// fill arguments of export
				Iterator<String> it = fieldsFirstStep.keySet().iterator();
				while (it.hasNext()) {
					String rtid = it.next();
					if (boxesFirstStep.get(rtid).isSelected())
						args.addOption(Export.RELATIONTYPE_ARG, rtid + ","
								+ fieldsFirstStep.get(rtid).getText());
				}

				it = fieldsSecondStep.keySet().iterator();
				while (it.hasNext()) {
					String ccid = it.next();
					if (boxesSecondStep.get(ccid).isSelected())
						args.addOption(Export.CONCEPTCLASS_ARG, ccid + ","
								+ fieldsSecondStep.get(ccid).getText());
				}

				it = fieldsThirdStep.keySet().iterator();
				while (it.hasNext()) {
					String anid = it.next();
					if (boxesThirdStep.get(anid).isSelected())
						args.addOption(Export.ATTRNAME_ARG, anid + ","
								+ fieldsThirdStep.get(anid).getText());
				}

				export.setArguments(args);
				export.setONDEXGraph(graph);

				export.start();
			} catch (Exception ex) {
				ErrorDialog.show(ex);
			}
		}
	}

	@Override
	public void setGraph(ONDEXGraph graph) {
		this.graph = graph;
		// hack to get to the selected viewer
		viewer = OVTK2Desktop.getDesktopResources().getSelectedViewer();
	}

	@Override
	public void start(File file) throws Exception {
		this.file = file;
		initFirstStep();
	}

	@Override
	public String getExt() {
		return "pro";
	}

	@Override
	public boolean isImport() {
		return false;
	}

}
