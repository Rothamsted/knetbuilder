package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes.NodeShapeSelection;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

import org.apache.commons.collections15.Transformer;

/**
 * Changes the size of selected node(s)
 * 
 * @author taubertj
 * 
 */
public class ChangeNodeSizeItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		// accepts every node
		return true;
	}

	@Override
	protected void doAction() {
		// input for size
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("Enter new size:"));
		JTextField sizeField = new JTextField(10);
		sizeField.setText(Config.defaultNodeSize + "");
		panel.add(sizeField);
		// ask user for size
		int option = JOptionPane.showConfirmDialog((Component) viewer, panel, "Change size", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			try {
				final int size = Integer.parseInt(sizeField.getText());

				// get node shapes
				ONDEXNodeShapes shapes = viewer.getNodeShapes();
				shapes.setNodeShapeSelection(NodeShapeSelection.NONE);

				// update all selected nodes
				final Map<ONDEXConcept, Integer> entitiesSizes = new HashMap<ONDEXConcept, Integer>();
				for (ONDEXConcept vertex : entities) {
					entitiesSizes.put(vertex, size);
				}

				// wrap new transformer around old one
				final Transformer<ONDEXConcept, Integer> oldSizes = shapes.getNodeSizes();
				Transformer<ONDEXConcept, Integer> newSizes = new Transformer<ONDEXConcept, Integer>() {

					@Override
					public Integer transform(ONDEXConcept arg0) {
						if (entitiesSizes.containsKey(arg0))
							return entitiesSizes.get(arg0);
						return oldSizes.transform(arg0);
					}
				};
				shapes.setNodeSizes(newSizes);

				// update current selection
				for (ONDEXConcept vertex : entities) {
					shapes.updateShape(vertex);
				}
			} catch (NumberFormatException nfe) {
				ErrorDialog.show(nfe);
			}
		}
	}

	@Override
	public net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem.MENUCATEGORY getCategory() {
		return MENUCATEGORY.CHANGE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ChangeSize";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}

}
