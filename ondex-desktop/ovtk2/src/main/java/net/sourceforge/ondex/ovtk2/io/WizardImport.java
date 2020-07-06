package net.sourceforge.ondex.ovtk2.io;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;

/**
 * Class that parses the ImportWizard output and populate an OVTK2Viewer.
 * 
 * @author taubertj
 */
public class WizardImport {

	// the data vector
	private Vector<Object> v;

	// get AttributeName from headers
	private JCheckBox useHeaders = new JCheckBox("Use column headers?");

	private JInternalFrame internalFrame;

	/**
	 * Parses return data of ImportWizard onto AbstractConcepts and
	 * AbstractRelations.
	 * 
	 * @param data
	 *            return data from ImportWizard
	 */
	public WizardImport(final ONDEXGraph graph, Vector<Object> data, final JFrame parent) {

		this.v = data;

		Container contentPane;

		// Compatibility to applet
		if (parent == null) {
			internalFrame = new RegisteredJInternalFrame("Value Mapping", "Import", "Define Value Mapping...", true, true, true, true);
			contentPane = internalFrame.getContentPane();
		} else {
			contentPane = parent.getContentPane();
		}

		// set layout
		BoxLayout layout = new BoxLayout(contentPane, BoxLayout.PAGE_AXIS);
		contentPane.setLayout(layout);

		// concept class selection
		JPanel ccPanel = new JPanel(new GridLayout(1, 1));
		ccPanel.setBorder(BorderFactory.createTitledBorder("Select ConceptClass"));
		ConceptClass[] ccs = graph.getMetaData().getConceptClasses().toArray(new ConceptClass[0]);
		Arrays.sort(ccs);
		final JComboBox box = new JComboBox();
		for (ConceptClass cc : ccs) {
			// only concept classes with concepts
			if (graph.getConceptsOfConceptClass(cc).size() > 0)
				box.addItem(cc);
		}
		ccPanel.add(box);
		contentPane.add(ccPanel);

		// attribute name
		JPanel anPanel = new JPanel(new GridLayout(2, 1));
		anPanel.setBorder(BorderFactory.createTitledBorder("Provide AttributeName"));
		final JTextField field = new JTextField();
		anPanel.add(field);
		anPanel.add(useHeaders);
		contentPane.add(anPanel);

		// decide how to treat multiple values
		JPanel multiPanel = new JPanel(new GridLayout(1, 2));
		multiPanel.setBorder(BorderFactory.createTitledBorder("Multiple values"));
		final JRadioButton average = new JRadioButton("Use Average");
		average.setSelected(true);
		final JRadioButton maximum = new JRadioButton("Use Maximum");
		ButtonGroup group = new ButtonGroup();
		group.add(average);
		group.add(maximum);
		multiPanel.add(average);
		multiPanel.add(maximum);
		contentPane.add(multiPanel);

		JButton button = new JButton("Proceed");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (field.getText().trim().length() == 0 && !useHeaders.isSelected()) {
					if (internalFrame != null)
						JOptionPane.showInternalMessageDialog(internalFrame, "No AttributeName given.", "Error", JOptionPane.ERROR_MESSAGE);
					else
						JOptionPane.showMessageDialog(parent, "No AttributeName given.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					process(graph, (ConceptClass) box.getSelectedItem(), field.getText(), average.isSelected());
					if (internalFrame != null)
						internalFrame.setVisible(false);
					else
						parent.setVisible(false);
				}
			}
		});
		contentPane.add(button);

		if (internalFrame != null) {
			internalFrame.pack();
			internalFrame.setVisible(true);
			OVTK2Desktop.getInstance().getDesktopPane().add(internalFrame);
			internalFrame.toFront();
		} else {
			parent.pack();
			parent.setVisible(true);
			parent.toFront();
		}

		synchronized (this) {
			while (internalFrame != null && internalFrame.isVisible()) {
				try {
					this.wait(10);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			while (parent != null && parent.isVisible()) {
				try {
					this.wait(10);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}

	/**
	 * Does all the transformation work.
	 * 
	 * @param graph
	 * @param cc
	 * @param an
	 * @param average
	 */
	@SuppressWarnings("unchecked")
	private void process(ONDEXGraph graph, ConceptClass cc, String an, boolean average) {

		// create mappings here
		List<String> headers = (List<String>) v.get(0);
		Vector<Vector<Object>> data = (Vector<Vector<Object>>) v.get(1);
		Map<String, List<Integer>> index = (Map<String, List<Integer>>) v.get(2);
		Map<String, double[]> uniqueValues = new HashMap<String, double[]>();
		// track optional name for accession
		Map<String, String> cnames = new HashMap<String, String>();
		for (Vector<Object> row : data) {

			// optional
			String name = null;
			if (index.containsKey("cname")) {
				name = row.get(index.get("cname").get(0).intValue()).toString();
			}

			// always present
			String accString = row.get(index.get("cacc").get(0).intValue()).toString();
			accString = accString.replaceAll("\"", "");
			String[] accs = accString.split(",");
			for (String acc : accs) {

				List<Integer> valueIndicies = index.get("value");

				// fill new array of values
				if (!uniqueValues.containsKey(acc)) {
					double[] values = new double[valueIndicies.size()];
					for (int i = 0; i < valueIndicies.size(); i++) {
						String valueAsString = row.get(valueIndicies.get(i)).toString();
						double value = Double.parseDouble(valueAsString);
						values[i] = value;
					}
					uniqueValues.put(acc, values);
					cnames.put(acc, name);
				} else {
					// process existing array of values
					double[] values = uniqueValues.get(acc);
					for (int i = 0; i < valueIndicies.size(); i++) {
						String valueAsString = row.get(valueIndicies.get(i)).toString();
						double oldValue = values[i];
						double newValue = Double.parseDouble(valueAsString);
						if (average) {
							// use average
							values[i] = (oldValue + newValue) / 2;
						} else {
							// use maximum
							values[i] = Math.max(oldValue, newValue);
						}
					}
				}
			}
		}

		int totalHits = 0;
		Set<ONDEXConcept> hitConcepts = new HashSet<ONDEXConcept>();

		// search in graph and assign Attribute
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(cc)) {
			for (ConceptAccession ca : c.getConceptAccessions()) {
				String acc = ca.getAccession();

				// accession hit found
				if (uniqueValues.containsKey(acc)) {
					totalHits++;
					hitConcepts.add(c);

					// add optional name as only preferred name to graph
					if (cnames.containsKey(acc) && cnames.get(acc) != null) {
						String name = cnames.get(acc);
						boolean found = false;
						for (ConceptName cn : c.getConceptNames()) {
							if (cn.getName().equals(name)) {
								found = true;
								cn.setPreferred(true);
							} else
								cn.setPreferred(false);
						}
						if (!found)
							c.createConceptName(name, true);
					}

					double[] values = uniqueValues.get(acc);
					for (int i = 0; i < values.length; i++) {

						// construct AttributeName
						AttributeName attr = null;
						if (!useHeaders.isSelected()) {
							String name = an;
							if (values.length > 1)
								name = name + i;
							attr = graph.getMetaData().getAttributeName(name);
							if (attr == null)
								attr = graph.getMetaData().getFactory().createAttributeName(name, Double.class);
						} else {
							String name = headers.get(i);
							name = name.replaceAll("\\s", "_");
							attr = graph.getMetaData().getAttributeName(name);
							if (attr == null)
								attr = graph.getMetaData().getFactory().createAttributeName(name, Double.class);
						}

						// only assign first hit
						if (c.getAttribute(attr) == null) {
							c.createAttribute(attr, Double.valueOf(values[i]), false);
						} else {

							// process multiple hits
							Attribute attribute = c.getAttribute(attr);
							double oldValue = (Double) attribute.getValue();
							double newValue = values[i];

							if (average) {
								// use average
								attribute.setValue(Double.valueOf((oldValue + newValue) / 2));
							} else {
								// use maximum
								attribute.setValue(Double.valueOf(Math.max(oldValue, newValue)));
							}
						}
					}
				}
			}
		}

		System.out.println("Total accession hits: " + totalHits);
		System.out.println("Total unique concept hits: " + hitConcepts.size());
	}
}
