package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeColors;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeColors.EdgeColorSelection;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeShapes.EdgeShape;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeDrawPaint.NodeDrawPaintSelection;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint.NodeFillPaintSelection;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes.NodeShapeSelection;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2GraphMouse;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2PickingMousePlugin;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;

import org.apache.commons.collections15.Transformer;

/**
 * Listens to action events specific to the appearance menu.
 * 
 * @author taubertj
 * 
 */
public class AppearanceMenuAction implements ActionListener {

	public static final String COLOR_CONCEPT_BY_SOURCE = "Menu.Appearance.Default.colorCV";
	public static final String COLOR_CONCEPT_BY_CLASS = "Menu.Appearance.Default.colorCC";
	public static final String COLOR_CONCEPT_BY_EVIDENCE = "Menu.Appearance.Default.colorET";
	public static final String SHAPE_LINE = "Menu.Appearance.Default.shapeLine";
	public static final String SHAPE_BENT = "Menu.Appearance.Default.shapeBent";
	public static final String SHAPE_CUBIC = "Menu.Appearance.Default.shapeCubic";
	public static final String SHAPE_QUAD = "Menu.Appearance.Default.shapeQuad";
	public static final String COLOR_RELATION_BY_EVIDECE = "Menu.Appearance.Default.colorRelET";
	public static final String COLOR_RELATION_BY_TYPE = "Menu.Appearance.Default.colorRelRT";
	public static final String SAVEAPPEARANCE = "saveappearance";
	public static final String EDGESIZE = "edgesize";
	public static final String EDGECOLOR = "edgecolor";
	public static final String NODESHAPE = "nodeshape";
	public static final String NODECOLOR = "nodecolor";
	public static final String LOADAPPEARANCE = "loadappearance";
	public static final String SHOWMOUSEOVER = "showmouseover";

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();
		OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();

		// triggers all events to load saved appearance
		if (cmd.equals(LOADAPPEARANCE)) {
			if (viewer != null) {
				AppearanceSynchronizer.loadAppearance(desktop, viewer);
			}
		}

		// toggle node Attribute colour parsing
		else if (cmd.equals(NODECOLOR)) {
			if (viewer != null) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) ae.getSource();
				if (item.isSelected())
					AppearanceSynchronizer.loadNodeColor(viewer.getONDEXJUNGGraph(), viewer.getNodeColors(), viewer.getNodeDrawPaint());
				else {
					viewer.getNodeColors().setFillPaintSelection(NodeFillPaintSelection.CONCEPTCLASS);
					viewer.getNodeColors().updateAll();
					viewer.getNodeDrawPaint().setDrawPaintSelection(NodeDrawPaintSelection.NONE);
					viewer.getNodeDrawPaint().updateAll();
				}
				// notify model of change
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
		}

		// toggle node shape Attribute parsing
		else if (cmd.equals(NODESHAPE)) {
			if (viewer != null) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) ae.getSource();
				if (item.isSelected())
					AppearanceSynchronizer.loadNodeShape(viewer.getONDEXJUNGGraph(), viewer.getNodeShapes());
				else {
					viewer.getNodeShapes().setNodeShapeSelection(NodeShapeSelection.NONE);
					viewer.getNodeShapes().setNodeSizes(new Transformer<ONDEXConcept, Integer>() {
						@Override
						public Integer transform(ONDEXConcept input) {
							return Config.defaultNodeSize;
						}
					});
					viewer.getNodeShapes().setNodeAspectRatios(new Transformer<ONDEXConcept, Float>() {
						@Override
						public Float transform(ONDEXConcept input) {
							return 1.0f;
						}
					});
					viewer.getNodeShapes().updateAll();
				}
				// notify model of change
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
		}

		// toggle edge Attribute colour parsing
		else if (cmd.equals(EDGECOLOR)) {
			if (viewer != null) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) ae.getSource();
				if (item.isSelected())
					AppearanceSynchronizer.loadEdgeColor(viewer.getONDEXJUNGGraph(), viewer.getEdgeColors());
				else {
					viewer.getEdgeColors().setEdgeColorSelection(EdgeColorSelection.RELATIONTYPE);
					viewer.getEdgeColors().updateAll();
				}
				// notify model of change
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
		}

		// toggle edge size Attribute parsing
		else if (cmd.equals(EDGESIZE)) {
			if (viewer != null) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) ae.getSource();
				if (item.isSelected())
					AppearanceSynchronizer.loadEdgeSize(viewer.getONDEXJUNGGraph(), viewer.getEdgeStrokes());
				else
					viewer.getEdgeStrokes().setEdgeSizes(null);
				// notify model of change
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
		}

		// toggle mouse over for current viewer
		else if (cmd.equals(SHOWMOUSEOVER)) {
			if (viewer != null) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) ae.getSource();
				OVTK2GraphMouse mouse = (OVTK2GraphMouse) viewer.getVisualizationViewer().getGraphMouse();
				OVTK2PickingMousePlugin picking = mouse.getOVTK2PickingMousePlugin();
				if (picking != null)
					picking.setShowMouseOver(item.isSelected());
			}
		}

		// sync node positions to Attribute
		else if (cmd.equals(SAVEAPPEARANCE)) {
			if (viewer != null) {
				AppearanceSynchronizer.saveAppearance(viewer);
			}
		}

		// toggle edge arrows
		else if (cmd.equals("edgearrow")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				viewer.getEdgeArrows().setShowArrow(selected);
				// notify model of change
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
		}

		// toggle node label visibility
		else if (cmd.equals("nodelabels")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				viewer.setShowNodeLabels(selected);
			}
		}

		// toggle edge label visibility
		else if (cmd.equals("edgelabels")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				viewer.setShowEdgeLabels(selected);
			}
		}

		// toggle both label visibility
		else if (cmd.equals("bothlabels")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				viewer.setShowNodeLabels(selected);
				viewer.setShowEdgeLabels(selected);
			}
		}

		// centre graph
		else if (cmd.equals("center")) {
			if (viewer != null) {
				viewer.center();
			}
		}

		// toggle anti-aliased painting
		else if (cmd.equals("antialiased")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				viewer.setAntiAliased(selected);
			}
		}

		// updates all graphical graph settings
		else if (cmd.equals("update")) {
			if (viewer != null) {
				viewer.updateViewer(null);
			}
		}

		// change colour selection strategy
		else if (cmd.startsWith("Menu.Appearance.Default.")) {
			if (viewer != null) {
				boolean selected = ((JRadioButtonMenuItem) ae.getSource()).isSelected();
				if (selected) {
					if (cmd.equals(COLOR_CONCEPT_BY_SOURCE))
						viewer.getNodeColors().setFillPaintSelection(ONDEXNodeFillPaint.NodeFillPaintSelection.DATASOURCE);
					else if (cmd.equals(COLOR_CONCEPT_BY_CLASS))
						viewer.getNodeColors().setFillPaintSelection(ONDEXNodeFillPaint.NodeFillPaintSelection.CONCEPTCLASS);
					else if (cmd.equals(COLOR_CONCEPT_BY_EVIDENCE))
						viewer.getNodeColors().setFillPaintSelection(ONDEXNodeFillPaint.NodeFillPaintSelection.EVIDENCETYPE);
					else if (cmd.equals(COLOR_RELATION_BY_TYPE))
						viewer.getEdgeColors().setEdgeColorSelection(ONDEXEdgeColors.EdgeColorSelection.RELATIONTYPE);
					else if (cmd.equals(COLOR_RELATION_BY_EVIDECE))
						viewer.getEdgeColors().setEdgeColorSelection(ONDEXEdgeColors.EdgeColorSelection.EVIDENCETYPE);
					else if (cmd.equals(SHAPE_QUAD))
						viewer.getEdgeShapes().setEdgeShape(EdgeShape.QUAD);
					else if (cmd.equals(SHAPE_CUBIC))
						viewer.getEdgeShapes().setEdgeShape(EdgeShape.CUBIC);
					else if (cmd.equals(SHAPE_BENT))
						viewer.getEdgeShapes().setEdgeShape(EdgeShape.BENT);
					else if (cmd.equals(SHAPE_LINE))
						viewer.getEdgeShapes().setEdgeShape(EdgeShape.LINE);
					else
						System.err.println("Command is not understood " + cmd);
					viewer.updateViewer(null);
				}
			}
		}
	}
}
