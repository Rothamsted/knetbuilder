package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Shape;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes.NodeShapeSelection;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ShapeComboBoxRenderer;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Changes the shape of selected node(s)
 * 
 * @author taubertj
 * 
 */
public class ChangeNodeShapeItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		// accepts every node
		return true;
	}

	@Override
	protected void doAction() {
		// list of available shapes
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("Select new shape:"));
		JComboBox comboBox = new JComboBox();
		comboBox.setRenderer(new ShapeComboBoxRenderer());
		for (Shape s : ONDEXNodeShapes.getAvailableShapes()) {
			comboBox.addItem(s);
		}
		panel.add(comboBox);
		// ask user for shape
		int option = JOptionPane.showConfirmDialog((Component) viewer, panel, "Shape selection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			Shape s = (Shape) comboBox.getSelectedItem();
			// set node shapes to manual
			ONDEXNodeShapes shapes = viewer.getNodeShapes();
			shapes.setNodeShapeSelection(NodeShapeSelection.MANUAL);
			// update all selected nodes
			for (ONDEXConcept vertex : entities) {
				shapes.updateShape(vertex, s);
			}
		}
	}

	@Override
	public net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem.MENUCATEGORY getCategory() {
		return MENUCATEGORY.CHANGE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ChangeShape";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}

}
