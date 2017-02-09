package net.sourceforge.ondex.ovtk2.ui.toolbars;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogChemicalSearch;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogProteinSearch;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogSearchResult;
import net.sourceforge.ondex.ovtk2.ui.toolbars.MenuGraphSearchBox.MetaDataWrapper;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.IntegerStringWrapper;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

/**
 * Handles search box related action events.
 * 
 * @author taubertj
 * 
 */
public class SearchBoxAction implements ActionListener, InternalFrameListener {

	// map of table graph editors to viewer windows
	private static Map<OVTK2Viewer, Set<OVTK2Dialog>> dialogs = LazyMap.decorate(new HashMap<OVTK2Viewer, Set<OVTK2Dialog>>(), new Factory<Set<OVTK2Dialog>>() {
		@Override
		public Set<OVTK2Dialog> create() {
			return new HashSet<OVTK2Dialog>();
		}
	});

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();
		OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();

		// search functionality
		if (cmd.equals("search")) {
			if (viewer != null) {
				MenuGraphSearchBox searchBox = (MenuGraphSearchBox) ae.getSource();
				String searchMode = searchBox.getSearchMode();

				// get concept class restriction, null is valid
				ConceptClass conceptClass = null;
				if (searchBox.getConceptClasses().getSelectedItem() instanceof MetaDataWrapper) {
					conceptClass = (ConceptClass) ((MetaDataWrapper) searchBox.getConceptClasses().getSelectedItem()).getMetaData();
				}

				// get data source restriction, null is valid
				DataSource dataSource = null;
				if (searchBox.getDataSources().getSelectedItem() instanceof MetaDataWrapper) {
					dataSource = (DataSource) ((MetaDataWrapper) searchBox.getDataSources().getSelectedItem()).getMetaData();
				}

				// get context restriction, null is valid
				ONDEXConcept context = null;
				if (searchBox.getTags().getSelectedItem() instanceof IntegerStringWrapper) {
					context = viewer.getONDEXJUNGGraph().getConcept(((IntegerStringWrapper) searchBox.getTags().getSelectedItem()).getValue());
				}

				if (searchMode == null || searchMode.equals(Config.language.getProperty("ToolBar.Search.Mode.Default"))) {
					// fire up default search
					DialogSearchResult results = new DialogSearchResult(viewer, searchBox.getSearchText(), searchBox.isRegex(), searchBox.isCaseSensitive(), conceptClass, dataSource, context);
					desktop.display(results, Position.leftTop);
					if (!dialogs.containsKey(viewer))
						viewer.addInternalFrameListener(this);
					dialogs.get(viewer).add(results);
				} else if (searchMode.equals(Config.language.getProperty("ToolBar.Search.Mode.SMILES")) || searchMode.equals(Config.language.getProperty("ToolBar.Search.Mode.InChI")) || searchMode.equals(Config.language.getProperty("ToolBar.Search.Mode.InChIKey")) || searchMode.equals(Config.language.getProperty("ToolBar.Search.Mode.ChEMBL"))) {
					// special case for chemical search
					DialogChemicalSearch results = new DialogChemicalSearch(viewer, searchBox.getSearchText(), conceptClass, dataSource, context, searchMode, searchBox.getTanimotoSimilarity(), searchBox.isUseChEMBL());
					desktop.display(results, Position.leftTop);
					if (!dialogs.containsKey(viewer))
						viewer.addInternalFrameListener(this);
					dialogs.get(viewer).add(results);
				} else if (searchMode.equals(Config.language.getProperty("ToolBar.Search.Mode.UniProt"))) {
					// special case for protein search
					DialogProteinSearch results = new DialogProteinSearch(viewer, searchBox.getSearchText());
					desktop.display(results, Position.leftTop);
					if (!dialogs.containsKey(viewer))
						viewer.addInternalFrameListener(this);
					dialogs.get(viewer).add(results);
				}
			}
		}
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		OVTK2Viewer viewer = (OVTK2Viewer) e.getSource();
		// close all associated search results
		for (OVTK2Dialog dialog : dialogs.get(viewer)) {
			try {
				dialog.setClosed(true);
			} catch (PropertyVetoException e1) {
				ErrorDialog.show(e1);
			}
		}
		dialogs.remove(viewer);
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
	}
}
