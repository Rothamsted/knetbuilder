package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

public class ExportNodeLabelItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		for (ONDEXConcept node : entities) {
			if (viewer.getNodeLabels().transform(node).length() > 0)
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {

		// Create a file chooser
		final JFileChooser fc = new JFileChooser();

		// In response to a button click:
		int returnVal = fc.showSaveDialog((Component) viewer);

		// check for approval
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			// get current state of HTML tag inclusion and set to false
			boolean oldIncludeHTML = viewer.getNodeLabels().isIncludeHTML();
			viewer.getNodeLabels().setIncludeHTML(false);

			// get file
			File file = fc.getSelectedFile();
			BufferedWriter writer;
			try {
				// write to file
				writer = new BufferedWriter(new FileWriter(file));
				for (ONDEXConcept node : entities) {
					// get label of node
					String label = viewer.getNodeLabels().transform(node);
					// only write showing labels
					if (label.length() > 0)
						writer.write(label + "\n");
				}
				// close file
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// revert to previous state
			viewer.getNodeLabels().setIncludeHTML(oldIncludeHTML);
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.LINK;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ExportLabel";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}

}
