package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.awt.Component;

import javax.swing.JOptionPane;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.transformer.relationcollapser.ClusterCollapser;

/**
 * Merges selected concepts into one
 * 
 * @author taubertj
 * 
 */
public class MergeConceptsItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		if (entities.size() > 1)
			return true;
		return false;
	}

	@Override
	protected void doAction() {

		int option = JOptionPane.showConfirmDialog((Component) viewer, Config.language.getProperty("Dialog.Merging.WarningMessage"), Config.language.getProperty("Dialog.Merging.WarningTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (option == JOptionPane.YES_OPTION) {
			ClusterCollapser collapser = new ClusterCollapser(true, true, null);

			// process collapsing here
			try {

				System.out.println("Collapsing " + entities.size() + " concepts.");

				// collapse every cluster
				ONDEXConcept c = collapser.collapseConceptCluster(viewer.getONDEXJUNGGraph(), entities);

				// make new concept visible
				viewer.getONDEXJUNGGraph().setVisibility(c, true);

				// make all relations visible
				viewer.getONDEXJUNGGraph().setVisibility(viewer.getONDEXJUNGGraph().getRelationsOfConcept(c), true);

				if (viewer.getMetaGraph() != null)
					viewer.getMetaGraph().updateMetaData();
			} catch (InconsistencyException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.LINK;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.MergeConcepts";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}

}
