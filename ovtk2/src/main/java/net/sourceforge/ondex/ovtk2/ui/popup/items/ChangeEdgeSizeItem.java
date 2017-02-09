package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeStrokes;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

import org.apache.commons.collections15.Transformer;

/**
 * Changes the size of selected edges(s)
 * 
 * @author taubertj
 * 
 */
public class ChangeEdgeSizeItem extends EntityMenuItem<ONDEXRelation> {

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
		sizeField.setText(Config.defaultEdgeSize + "");
		panel.add(sizeField);
		// ask user for size
		int option = JOptionPane.showConfirmDialog((Component) viewer, panel, "Change size", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			try {
				final int size = Integer.parseInt(sizeField.getText());

				// get edge shapes
				ONDEXEdgeStrokes strokes = viewer.getEdgeStrokes();

				// update all selected edges
				final Map<ONDEXRelation, Integer> entitiesSizes = new HashMap<ONDEXRelation, Integer>();
				for (ONDEXRelation edge : entities) {
					entitiesSizes.put(edge, size);
				}

				// wrap new transformer around old one
				final Transformer<ONDEXRelation, Integer> oldSizes = strokes.getEdgeSizeTransformer();
				Transformer<ONDEXRelation, Integer> newSizes = new Transformer<ONDEXRelation, Integer>() {

					@Override
					public Integer transform(ONDEXRelation arg0) {
						if (entitiesSizes.containsKey(arg0))
							return entitiesSizes.get(arg0);
						if (oldSizes != null)
							return oldSizes.transform(arg0);
						return Config.defaultEdgeSize;
					}
				};
				strokes.setEdgeSizes(newSizes);
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
