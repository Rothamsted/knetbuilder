package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;
import org.apache.log4j.Logger;

/**
 * Shows everything in the neighbourhood of this node for a certain concept
 * class
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ShowNodeNeighbourhoodConceptClassItem extends EntityMenuItem<ONDEXConcept> {

	/**
	 * Helper class for each concept class
	 * 
	 * @author taubertj
	 * 
	 */
	private class ConceptClassHelperItem extends EntityMenuItem<ONDEXConcept> {

		private ConceptClass cc;

		public ConceptClassHelperItem(ConceptClass cc) {
			super();
			this.cc = cc;
		}

		@Override
		public boolean accepts() {
			return false;
		}

		@Override
		public MENUCATEGORY getCategory() {
			return null;
		}

		@Override
		protected void doAction() {
			ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
			LOG.info("Showing node neighbourhood for " + entities + " and " + cc);
			for (ONDEXConcept n : entities) {
				Set<ONDEXConcept> neighbours = new HashSet<ONDEXConcept>();
				for (ONDEXRelation r : graph.getRelationsOfConcept(n)) {
					ONDEXConcept fromConcept = r.getFromConcept();
					ONDEXConcept toConcept = r.getToConcept();
					if ((fromConcept.equals(n) && toConcept.getOfType().equals(cc)) || (toConcept.equals(n) && fromConcept.getOfType().equals(cc))) {
						// check if this is a invisible edge
						LOG.info("Considering " + r + " visible?=" + graph.isVisible(r));
						graph.setVisibility(r, true);

						// make from concept visible
						if (!graph.isVisible(fromConcept)) {
							graph.setVisibility(fromConcept, true);
							neighbours.add(fromConcept);
						}

						// make to concept visible
						if (!graph.isVisible(toConcept)) {
							graph.setVisibility(toConcept, true);
							neighbours.add(toConcept);
						}
					}
				}
				LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(), n, neighbours);
			}
		}

		@Override
		protected String getMenuPropertyName() {
			return "";
		}

		@Override
		protected String getUndoPropertyName() {
			return "Undo.ShowNodeNeighbourhoodConceptClass";
		}
	}

	private static Logger LOG = Logger.getLogger(ShowNodeNeighbourhoodConceptClassItem.class);

	public ShowNodeNeighbourhoodConceptClassItem() {
		super();
		this.item = new JMenu();
		item.setText(Config.language.getProperty(getMenuPropertyName()));
	}

	@Override
	public boolean accepts() {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();

		Map<ConceptClass, Integer> allCCCounts = LazyMap.decorate(new HashMap<ConceptClass, Integer>(), new Factory<Integer>() {
			@Override
			public Integer create() {
				return 0;
			}
		});
		Map<ConceptClass, Integer> invisibleCCCounts = LazyMap.decorate(new HashMap<ConceptClass, Integer>(), new Factory<Integer>() {
			@Override
			public Integer create() {
				return 0;
			}
		});
		Set<ONDEXConcept> neighbours = new HashSet<ONDEXConcept>();

		for (ONDEXConcept n : entities) {
			for (ONDEXRelation r : graph.getRelationsOfConcept(n)) {
				ONDEXConcept from = r.getFromConcept();
				ONDEXConcept to = r.getToConcept();

				ONDEXConcept neighbour = null;
				ConceptClass cc = null;

				if (from.equals(n)) {
					cc = to.getOfType();
					neighbour = to;
				} else if (to.equals(n)) {
					cc = from.getOfType();
					neighbour = from;
				}

				if (!neighbours.contains(neighbour)) {
					allCCCounts.put(cc, allCCCounts.get(cc) + 1);
					if (!graph.isVisible(neighbour)) {
						invisibleCCCounts.put(cc, invisibleCCCounts.get(cc) + 1);
					}
					neighbours.add(neighbour);
				}
			}
		}

		if (!invisibleCCCounts.isEmpty()) {
			for (ConceptClass cc : invisibleCCCounts.keySet()) {
				ConceptClassHelperItem helper = new ConceptClassHelperItem(cc);
				helper.init(viewer, entities);
				helper.getItem().setText(cc.toString() + " (" + invisibleCCCounts.get(cc) + ":" + allCCCounts.get(cc) + ")");
				item.add(helper.getItem());
			}
			return true;
		}

		return false;
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.SHOW;
	}

	@Override
	protected void doAction() {
		// nothing
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.NeighbourhoodByConceptClass";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}
}
