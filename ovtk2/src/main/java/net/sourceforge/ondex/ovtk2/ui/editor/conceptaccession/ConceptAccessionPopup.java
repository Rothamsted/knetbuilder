package net.sourceforge.ondex.ovtk2.ui.editor.conceptaccession;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.ovtk2.ui.editor.util.Util;

/**
 * Displays a popup menu with check box for ambiguity and drop-down for element
 * of.
 * 
 * @author taubertj
 * 
 */
public class ConceptAccessionPopup extends JPopupMenu implements ActionListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 8120691497155750890L;

	/**
	 * check box for ambiguity
	 */
	JCheckBox box;

	/**
	 * combo box for selection of DataSource
	 */
	JComboBox selection;

	/**
	 * propagate changes to DataSource upwards
	 */
	ConceptAccessionTableCellEditor editor;

	/**
	 * Modify given concept accession
	 * 
	 * @param editor
	 *            to propagate changes to element of upwards
	 */
	public ConceptAccessionPopup(ConceptAccessionTableCellEditor editor) {
		this.editor = editor;

		// editor panel
		BoxLayout layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
		this.setLayout(layout);

		// selection of DataSource for element of
		this.add(new JLabel("Element of: "));
		Object[] cvs = editor.graph.getMetaData().getDataSources().toArray();
		Arrays.sort(cvs, new Util());
		selection = new JComboBox(cvs);
		selection.setSelectedItem(editor.acc.getElementOf());
		this.add(selection);

		// check box for ambiguity
		box = new JCheckBox("Is ambiguous concept accession?");
		box.setSelected(editor.acc.isAmbiguous());
		this.add(box);

		// button to close popup
		JButton close = new JButton("close");
		close.addActionListener(this);
		this.add(close);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.setVisible(false);
		editor.acc.setAmbiguous(box.isSelected());

		// has the DataSource of accession changed?
		DataSource dataSource = (DataSource) selection.getSelectedItem();
		if (!editor.acc.getElementOf().equals(dataSource)) {
			// delete old one
			ConceptAccession old = editor.concept.getConceptAccession(editor.acc.getAccession(), editor.acc.getElementOf());
			editor.concept.deleteConceptAccession(editor.acc.getAccession(), editor.acc.getElementOf());
			// create one with new DataSource
			editor.acc = editor.concept.createConceptAccession(old.getAccession(), dataSource, old.isAmbiguous());
		}
	}
}
