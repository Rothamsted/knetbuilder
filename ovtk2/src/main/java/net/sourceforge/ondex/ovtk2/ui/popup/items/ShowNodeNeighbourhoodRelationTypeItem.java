package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

/**
 * Shows everything in the neighbourhood of this node for a certain concept
 * class
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ShowNodeNeighbourhoodRelationTypeItem extends EntityMenuItem<ONDEXConcept> {

	/**
	 * Helper class for each relation type
	 * 
	 * @author taubertj
	 * 
	 */
	private class RelationTypeHelperItem extends EntityMenuItem<ONDEXConcept> {

		private RelationType rt;

		public RelationTypeHelperItem(RelationType rt) {
			this.rt = rt;
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
			ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
			for (ONDEXConcept n : entities) {
				Set<ONDEXConcept> neighbours = new HashSet<ONDEXConcept>();
				for (ONDEXRelation r : viewer.getONDEXJUNGGraph().getRelationsOfConcept(n)) {
					ONDEXConcept fromConcept = r.getFromConcept();
					ONDEXConcept toConcept = r.getToConcept();
					if ((fromConcept.equals(n) && r.getOfType().equals(rt)) || (toConcept.equals(n) && r.getOfType().equals(rt))) {
						// check if this is a invisible edge
						if (!jung.isVisible(r)) {
							if (fromConcept.equals(n)) {
								// check if to and qualifier are visible
								ONDEXConcept to = r.getToConcept();
								if (!jung.isVisible(to)) {
									jung.setVisibility(to, true);
									neighbours.add(to);
								}
								jung.setVisibility(r, true);
							} else if (toConcept.equals(n)) {
								// check if from and qualifier are visible
								ONDEXConcept from = r.getFromConcept();
								if (!jung.isVisible(from)) {
									jung.setVisibility(from, true);
									neighbours.add(from);
								}
								jung.setVisibility(r, true);
							} else {
								// check if from and to are visible
								ONDEXConcept from = r.getFromConcept();
								if (!jung.isVisible(from)) {
									jung.setVisibility(from, true);
									neighbours.add(from);
								}
								ONDEXConcept to = r.getToConcept();
								if (!jung.isVisible(to)) {
									jung.setVisibility(to, true);
									neighbours.add(to);
								}
								jung.setVisibility(r, true);
							}
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
			return "Undo.ShowNodeNeighbourhoodRelationType";
		}

	}

	public ShowNodeNeighbourhoodRelationTypeItem() {
		super();
		this.item = new JMenu();
		item.setText(Config.language.getProperty(getMenuPropertyName()));
	}

	@Override
	public boolean accepts() {

		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();

		Map<RelationType, Integer> allRTCounts = LazyMap.decorate(new HashMap<RelationType, Integer>(), new Factory<Integer>() {
			@Override
			public Integer create() {
				return 0;
			}
		});
		Map<RelationType, Integer> invisibleRTCounts = LazyMap.decorate(new HashMap<RelationType, Integer>(), new Factory<Integer>() {
			@Override
			public Integer create() {
				return 0;
			}
		});

		for (ONDEXConcept n : entities) {
			for (ONDEXRelation r : graph.getRelationsOfConcept(n)) {
				RelationType rt = r.getOfType();

				// count up relation types from concept
				allRTCounts.put(rt, allRTCounts.get(rt) + 1);

				// count up invisible relations
				if (!graph.isVisible(r))
					invisibleRTCounts.put(rt, invisibleRTCounts.get(rt) + 1);
			}
		}

		if (!invisibleRTCounts.isEmpty()) {
			for (RelationType rt : invisibleRTCounts.keySet()) {
				RelationTypeHelperItem helper = new RelationTypeHelperItem(rt);
				helper.init(viewer, entities);
				helper.getItem().setText(rt.toString() + " (" + invisibleRTCounts.get(rt) + ":" + allRTCounts.get(rt) + ")");
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
		return "Viewer.VertexMenu.NeighbourhoodByRelationType";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}
}