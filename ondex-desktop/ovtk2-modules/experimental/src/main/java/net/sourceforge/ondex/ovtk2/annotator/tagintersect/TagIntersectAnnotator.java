package net.sourceforge.ondex.ovtk2.annotator.tagintersect;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JButton;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeColors;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

/**
 * Colours edges green if the sets of tags intersect and red otherwise.
 * 
 * @author taubertj
 * 
 */
public class TagIntersectAnnotator extends OVTK2Annotator implements
		ActionListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -4269166402221482219L;
	
	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	public TagIntersectAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);

		// simple button
		JButton go = new JButton("Colour edges");
		go.addActionListener(this);
		this.add(go);

		// a bit more obvious size
		this.setPreferredSize(new Dimension(300, 200));
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.TagIntersect");
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		ONDEXEdgeColors colors = viewer.getEdgeColors();
		colors.setEdgeColorSelection(ONDEXEdgeColors.EdgeColorSelection.MANUAL);

		// for all visible edges
		for (ONDEXRelation edge : graph.getEdges()) {

			// get from and to concept from relation
			ONDEXConcept from = edge.getFromConcept();
			ONDEXConcept to = edge.getToConcept();

			// get tag on from and to concept
			Collection<ONDEXConcept> fromCol = new HashSet<ONDEXConcept>(
					from.getTags());
			Collection<ONDEXConcept> toCol = to.getTags();

			// requires new set so no unmodifiable exception
			fromCol.retainAll(toCol);
			if (fromCol.size() > 1) {
				colors.updateColor(edge, Color.GREEN);
			} else {
				colors.updateColor(edge, Color.RED);
			}
		}

		// update viewer
		viewer.getVisualizationViewer().fireStateChanged();
		viewer.getVisualizationViewer().repaint();
		
		used = true;
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
