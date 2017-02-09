package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JColorChooser;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint.NodeFillPaintSelection;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Changes the colour of selected node(s)
 * 
 * @author taubertj
 * 
 */
public class ChangeNodeColorItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		// accepts every node
		return true;
	}

	@Override
	protected void doAction() {
		// ask user for colour
		Color c = JColorChooser.showDialog((Component) viewer, "Choose Color", null);
		if (c != null) {
			// change node colour to manual
			ONDEXNodeFillPaint fillPaint = viewer.getNodeColors();
			fillPaint.setFillPaintSelection(NodeFillPaintSelection.MANUAL);
			// update colour of all selected nodes
			for (ONDEXConcept vertex : entities) {
				fillPaint.updateColor(vertex, c);
			}
		}
	}

	@Override
	public net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem.MENUCATEGORY getCategory() {
		return MENUCATEGORY.CHANGE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ChangeColor";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}

}
