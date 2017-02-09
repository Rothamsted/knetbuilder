package net.sourceforge.ondex.ovtk2.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeColors;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeColors.EdgeColorSelection;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeStrokes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeDrawPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeDrawPaint.NodeDrawPaintSelection;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint.NodeFillPaintSelection;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes.NodeShapeSelection;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConceptLabel;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.AppearanceMenuAction;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * Provides methods for storing and retrieving graphical attributes of the graph
 * in the Attribute.
 * 
 * @author taubertj
 */
public class AppearanceSynchronizer {

	public static final String ALPHA = "alpha";

	public static final String COLOR = "color";

	public static final String DRAWALPHA = "drawalpha";

	public static final String DRAWCOLOR = "drawcolor";

	public static final String GRAPHICAL_X = "graphicalX";

	public static final String GRAPHICAL_Y = "graphicalY";

	public static final String HEIGHT = "height";

	public static final String LABEL = "label";

	public static final String LINE = "line";

	public static final String SHAPE = "shape";

	public static final String SIZE = "size";

	public static final String VISIBLE = "visible";

	public static final String WIDTH = "width";

	public static final String FLAGGED = "flagged";

	/**
	 * Contains all attribute name identifiers used
	 */
	public static final Set<String> attr = new HashSet<String>();

	static {
		attr.add(HEIGHT);
		attr.add(LABEL);
		attr.add(LINE);
		attr.add(SHAPE);
		attr.add(SIZE);
		attr.add(VISIBLE);
		attr.add(ALPHA);
		attr.add(COLOR);
		attr.add(DRAWALPHA);
		attr.add(DRAWCOLOR);
		attr.add(GRAPHICAL_X);
		attr.add(GRAPHICAL_Y);
		attr.add(WIDTH);
		attr.add(FLAGGED);
	}

	/**
	 * Retrieve appearance from stored Attribute values.
	 * 
	 * @param listener
	 *            required to set menu items right
	 * @param activeViewer
	 *            OVTK2Viewer to modify
	 */
	public static void loadAppearance(ActionListener listener, OVTK2PropertiesAggregator activeViewer) {
		ONDEXJUNGGraph graph = activeViewer.getONDEXJUNGGraph();
		ONDEXGraphMetaData meta = graph.getMetaData();
		AttributeName anVisible, anLabel;
		boolean nodeLabelsShown = false, edgeLabelsShown = false;
		if ((anVisible = meta.getAttributeName(VISIBLE)) == null) {
			JOptionPane.showMessageDialog(OVTK2Desktop.getDesktopResources().getParentPane(), Config.language.getProperty("Dialog.Appearance.NotFound"), Config.language.getProperty("Dialog.Appearance.NotFoundTitle"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		anLabel = meta.getAttributeName(LABEL);

		// load node visibility
		for (ONDEXConcept c : graph.getConcepts()) {
			Attribute attribute = c.getAttribute(anVisible);
			if (attribute != null) {
				graph.setVisibility(c, (Boolean) attribute.getValue());
				// get visibility of label
				if (anLabel != null) {
					attribute = c.getAttribute(anLabel);
					if (attribute != null) {
						Boolean value = (Boolean) attribute.getValue();
						activeViewer.getNodeLabels().getMask().put(c, value);
						if (value)
							nodeLabelsShown = true;
					}
				}
			} else
				graph.setVisibility(c, false);
		}

		// load edge visibility
		for (ONDEXRelation r : graph.getRelations()) {
			Attribute attribute = r.getAttribute(anVisible);
			if (attribute != null) {
				graph.setVisibility(r, (Boolean) attribute.getValue());
				// get visibility of label
				if (anLabel != null) {
					attribute = r.getAttribute(anLabel);
					if (attribute != null) {
						Boolean value = (Boolean) attribute.getValue();
						activeViewer.getEdgeLabels().getMask().put(r, value);
						if (value)
							edgeLabelsShown = true;
					}
				}
			} else
				graph.setVisibility(r, false);
		}

		// trigger parse attribute ... and static layout
		JCheckBoxMenuItem item = new JCheckBoxMenuItem();
		item.setSelected(true);
		listener.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, AppearanceMenuAction.NODESHAPE));
		listener.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, AppearanceMenuAction.NODECOLOR));
		listener.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, AppearanceMenuAction.EDGESIZE));
		listener.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, AppearanceMenuAction.EDGECOLOR));
		listener.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, "Menu.Layout.Static"));

		// get saved font
		final Font vertexFont = activeViewer.getVertexFont();
		if (vertexFont != null)
			activeViewer.getVisualizationViewer().getRenderContext().setVertexFontTransformer(new Transformer<ONDEXConcept, Font>() {
				@Override
				public Font transform(ONDEXConcept input) {
					return vertexFont;
				}
			});
		final Font edgeFont = activeViewer.getEdgeFont();
		if (edgeFont != null)
			activeViewer.getVisualizationViewer().getRenderContext().setEdgeFontTransformer(new Transformer<ONDEXRelation, Font>() {
				@Override
				public Font transform(ONDEXRelation input) {
					return edgeFont;
				}
			});

		// hack to get saved label positions set
		DialogConceptLabel labels = new DialogConceptLabel(activeViewer);
		labels.actionPerformed(new ActionEvent(labels, ActionEvent.ACTION_PERFORMED, "apply"));

		// update menu bar settings if some labels have been loaded
		if (anLabel != null) {
			activeViewer.setShowNodeLabels(nodeLabelsShown);
			activeViewer.setShowEdgeLabels(edgeLabelsShown);
		}
	}

	/**
	 * Retrieve edge color from stored Attribute values.
	 * 
	 * @param graph
	 *            ONDEXGraph
	 * @param edgePaint
	 *            ONDEXEdgeColors
	 */
	public static void loadEdgeColor(ONDEXGraph graph, ONDEXEdgeColors edgePaint) {

		ONDEXGraphMetaData meta = graph.getMetaData();

		// load colour
		AttributeName anColor = meta.getAttributeName(COLOR);
		if (anColor != null) {
			edgePaint.setEdgeColorSelection(EdgeColorSelection.MANUAL);
			for (ONDEXRelation r : graph.getRelationsOfAttributeName(anColor)) {
				edgePaint.updateColor(r, (Color) r.getAttribute(anColor).getValue());
			}
		}
	}

	/**
	 * Retrieve edge size from stored Attribute values.
	 * 
	 * @param graph
	 *            ONDEXGraph
	 * @param edgeStrokes
	 *            ONDEXEdgeStrokes
	 */
	public static void loadEdgeSize(ONDEXGraph graph, ONDEXEdgeStrokes edgeStrokes) {

		ONDEXGraphMetaData meta = graph.getMetaData();

		// load edge size
		AttributeName anSize = meta.getAttributeName(SIZE);
		if (anSize != null) {

			// used in size transformer
			final Map<ONDEXRelation, Integer> sizes = new HashMap<ONDEXRelation, Integer>();

			// get sizes from graph
			for (ONDEXRelation r : graph.getRelationsOfAttributeName(anSize)) {
				sizes.put(r, (Integer) r.getAttribute(anSize).getValue());
			}

			// set new edge size function
			edgeStrokes.setEdgeSizes(new Transformer<ONDEXRelation, Integer>() {
				@Override
				public Integer transform(ONDEXRelation input) {
					if (sizes.containsKey(input))
						return sizes.get(input);
					return Config.defaultEdgeSize;
				}
			});
		}
	}

	/**
	 * Retrieve node colour from stored Attribute values.
	 * 
	 * @param graph
	 *            ONDEXGraph
	 * @param nodeFillPaint
	 *            ONDEXNodeFillPaint
	 * @param nodeDrawPaint
	 *            ONDEXNodeDrawPaint
	 */
	public static void loadNodeColor(ONDEXJUNGGraph graph, ONDEXNodeFillPaint nodeFillPaint, ONDEXNodeDrawPaint nodeDrawPaint) {

		ONDEXGraphMetaData meta = graph.getMetaData();

		// load fill colour
		AttributeName anColor = meta.getAttributeName(COLOR);
		if (anColor != null) {
			// additional alpha value
			AttributeName anAlpha = meta.getAttributeName(ALPHA);
			if (anAlpha != null) {
				for (ONDEXConcept c : graph.getConceptsOfAttributeName(anAlpha)) {
					nodeFillPaint.updateAlpha(c, (Integer) c.getAttribute(anAlpha).getValue());
				}
			}
			nodeFillPaint.setFillPaintSelection(NodeFillPaintSelection.MANUAL);
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(anColor)) {
				Color color = (Color) c.getAttribute(anColor).getValue();
				if (nodeFillPaint.transformAlpha(c) != null) {
					color = new Color(color.getRed(), color.getGreen(), color.getBlue(), nodeFillPaint.transformAlpha(c));
				}
				nodeFillPaint.updateColor(c, color);
			}
		}

		// load draw colour
		AttributeName anDrawColor = meta.getAttributeName(DRAWCOLOR);
		if (anDrawColor != null) {
			// additional alpha value
			AttributeName anDrawAlpha = meta.getAttributeName(DRAWALPHA);
			if (anDrawAlpha != null) {
				for (ONDEXConcept c : graph.getConceptsOfAttributeName(anDrawAlpha)) {
					nodeDrawPaint.updateAlpha(c, (Integer) c.getAttribute(anDrawAlpha).getValue());
				}
			}
			nodeDrawPaint.setDrawPaintSelection(NodeDrawPaintSelection.MANUAL);
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(anDrawColor)) {
				Color color = (Color) c.getAttribute(anDrawColor).getValue();
				if (nodeDrawPaint.transformAlpha(c) != null) {
					color = new Color(color.getRed(), color.getGreen(), color.getBlue(), nodeDrawPaint.transformAlpha(c));
				}
				nodeDrawPaint.updateColor(c, color);
			}
		}
	}

	/**
	 * Retrieve node shape from stored Attribute values.
	 * 
	 * @param graph
	 *            ONDEXJUNGGraph
	 * @param nodeShapes
	 *            ONDEXNodeShapes
	 */
	public static void loadNodeShape(ONDEXJUNGGraph graph, ONDEXNodeShapes nodeShapes) {

		ONDEXGraphMetaData meta = graph.getMetaData();

		// load node size
		AttributeName anSize = meta.getAttributeName(SIZE);
		if (anSize != null) {

			// used in size transformer
			final Map<ONDEXConcept, Integer> sizes = new HashMap<ONDEXConcept, Integer>();

			// get sizes from graph
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(anSize)) {
				sizes.put(c, ((Number) c.getAttribute(anSize).getValue()).intValue());
			}

			// set new node size function
			nodeShapes.setNodeSizes(new Transformer<ONDEXConcept, Integer>() {
				@Override
				public Integer transform(ONDEXConcept input) {
					if (sizes.containsKey(input))
						return sizes.get(input);
					return Config.defaultNodeSize;
				}
			});
		}

		// load possible aspect ratios
		AttributeName anHeight = meta.getAttributeName(HEIGHT);
		AttributeName anWidth = meta.getAttributeName(WIDTH);
		if (anHeight != null && anWidth != null) {

			// used in size transformer
			final Map<ONDEXConcept, Integer> sizes = LazyMap.decorate(new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
				@Override
				public Integer create() {
					return Config.defaultNodeSize;
				}
			});

			// used in aspect ration transformer
			final Map<ONDEXConcept, Float> ratios = LazyMap.decorate(new HashMap<ONDEXConcept, Float>(), new Factory<Float>() {
				@Override
				public Float create() {
					return 1.0f;
				}
			});

			// get width and height from graph
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(anWidth)) {
				Integer width = (Integer) c.getAttribute(anWidth).getValue();
				Integer height = (Integer) c.getAttribute(anHeight).getValue();
				ratios.put(c, (float) height / (float) width);
				sizes.put(c, width);
			}

			// set new node aspect ratio function
			nodeShapes.setNodeAspectRatios(new Transformer<ONDEXConcept, Float>() {
				@Override
				public Float transform(ONDEXConcept input) {
					return ratios.get(input);
				}
			});

			// set new node size function
			nodeShapes.setNodeSizes(new Transformer<ONDEXConcept, Integer>() {
				@Override
				public Integer transform(ONDEXConcept input) {
					return sizes.get(input);
				}
			});
		}

		// update all concepts not vertices before
		for (ONDEXConcept node : graph.getConcepts()) {
			nodeShapes.updateShape(node);
		}
		nodeShapes.setNodeShapeSelection(NodeShapeSelection.MANUAL);

		// load node shape
		AttributeName anShape = meta.getAttributeName(SHAPE);
		if (anShape != null) {
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(anShape)) {
				nodeShapes.updateShape(c, ONDEXNodeShapes.getShape((Integer) c.getAttribute(anShape).getValue()));
			}
		}

		// special case for line shape
		AttributeName anLine = meta.getAttributeName(LINE);
		if (anLine != null) {

			// get line shapes from graph
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(anLine)) {
				String lineCoords = (String) c.getAttribute(anLine).getValue();
				String[] semiSplit = lineCoords.split(";");

				// compose new line shape
				Path2D path = new Path2D.Float();
				for (String partCoords : semiSplit) {
					// comma separated list of x,y coordinates
					String[] coords = partCoords.split(",");
					float startX = Float.parseFloat(coords[0]);
					float startY = Float.parseFloat(coords[1]);
					// construct relative shape
					path.moveTo(startX, startY);
					for (int i = 2; i < coords.length - 1; i += 2) {
						path.lineTo(Float.parseFloat(coords[i]), Float.parseFloat(coords[i + 1]));
					}
				}

				// set new shape
				nodeShapes.updateShape(c, path);
			}
		}
	}

	/**
	 * Saves appearance to the Attribute on the graph.
	 * 
	 * @param activeViewer
	 *            OVTK2Viewer to modify
	 */
	public static void saveAppearance(OVTK2PropertiesAggregator activeViewer) {

		// get meta data
		ONDEXJUNGGraph graph = activeViewer.getONDEXJUNGGraph();
		ONDEXGraphMetaData meta = graph.getMetaData();

		// check for attribute names
		AttributeName attrGraphicalX, attrGraphicalY, attrSize;
		AttributeName attrColor, attrAlpha, attrDrawColor, attrDrawAlpha;
		AttributeName attrShape, attrVisible, attrLabel;
		if ((attrGraphicalX = meta.getAttributeName(GRAPHICAL_X)) == null)
			attrGraphicalX = meta.getFactory().createAttributeName(GRAPHICAL_X, Double.class);
		if ((attrGraphicalY = meta.getAttributeName(GRAPHICAL_Y)) == null)
			attrGraphicalY = meta.getFactory().createAttributeName(GRAPHICAL_Y, Double.class);
		if ((attrShape = meta.getAttributeName(SHAPE)) == null)
			attrShape = meta.getFactory().createAttributeName(SHAPE, java.lang.Integer.class);
		if ((attrVisible = meta.getAttributeName(VISIBLE)) == null)
			attrVisible = meta.getFactory().createAttributeName(VISIBLE, java.lang.Boolean.class);
		if ((attrLabel = meta.getAttributeName(LABEL)) == null)
			attrLabel = meta.getFactory().createAttributeName(LABEL, java.lang.Boolean.class);
		if ((attrSize = meta.getAttributeName(SIZE)) == null)
			attrSize = meta.getFactory().createAttributeName(SIZE, java.lang.Integer.class);
		if ((attrColor = meta.getAttributeName(COLOR)) == null)
			attrColor = meta.getFactory().createAttributeName(COLOR, java.awt.Color.class);
		if ((attrAlpha = meta.getAttributeName(ALPHA)) == null)
			attrAlpha = meta.getFactory().createAttributeName(ALPHA, java.lang.Integer.class);
		if ((attrDrawColor = meta.getAttributeName(DRAWCOLOR)) == null)
			attrDrawColor = meta.getFactory().createAttributeName(DRAWCOLOR, java.awt.Color.class);
		if ((attrDrawAlpha = meta.getAttributeName(DRAWALPHA)) == null)
			attrDrawAlpha = meta.getFactory().createAttributeName(DRAWALPHA, java.lang.Integer.class);

		// clear visibility
		for (ONDEXConcept c : graph.getConcepts()) {
			Attribute attribute = c.getAttribute(attrVisible);
			if (attribute != null)
				attribute.setValue(Boolean.FALSE);
			attribute = c.getAttribute(attrLabel);
			if (attribute != null)
				attribute.setValue(Boolean.FALSE);
		}
		for (ONDEXRelation r : graph.getRelations()) {
			Attribute attribute = r.getAttribute(attrVisible);
			if (attribute != null)
				attribute.setValue(Boolean.FALSE);
			attribute = r.getAttribute(attrLabel);
			if (attribute != null)
				attribute.setValue(Boolean.FALSE);
		}

		// set positions and visibility
		Layout<ONDEXConcept, ONDEXRelation> layout = activeViewer.getVisualizationViewer().getGraphLayout();
		for (ONDEXConcept c : graph.getVertices()) {
			Point2D p = layout.transform(c);
			// store X coordinate
			Attribute graphicalX = c.getAttribute(attrGraphicalX);
			if (graphicalX == null)
				c.createAttribute(attrGraphicalX, p.getX(), false);
			else
				graphicalX.setValue(p.getX());
			// store Y coordinate
			Attribute graphicalY = c.getAttribute(attrGraphicalY);
			if (graphicalY == null)
				c.createAttribute(attrGraphicalY, p.getY(), false);
			else
				graphicalY.setValue(p.getY());
			// store visibility, is true for all node in JUNG Graph
			Attribute visible = c.getAttribute(attrVisible);
			if (visible == null)
				c.createAttribute(attrVisible, Boolean.TRUE, false);
			else
				visible.setValue(Boolean.TRUE);
			// store label mask
			Attribute label = c.getAttribute(attrLabel);
			if (label == null)
				c.createAttribute(attrLabel, activeViewer.getNodeLabels().getMask().get(c), false);
			else
				label.setValue(activeViewer.getNodeLabels().getMask().get(c));
		}

		// set shapes, colours and size
		ONDEXNodeShapes nodeShapes = activeViewer.getNodeShapes();
		ONDEXNodeFillPaint nodeColors = activeViewer.getNodeColors();
		ONDEXNodeDrawPaint nodeDrawPaint = activeViewer.getNodeDrawPaint();
		Transformer<ONDEXConcept, Integer> nodeSizes = nodeShapes.getNodeSizes();
		for (ONDEXConcept c : graph.getVertices()) {
			// save colour
			Paint paint = nodeColors.transform(c);
			if (paint instanceof Color) {
				Attribute color = c.getAttribute(attrColor);
				if (color == null)
					c.createAttribute(attrColor, paint, false);
				else
					color.setValue(paint);
			}
			Integer alpha = nodeColors.transformAlpha(c);
			if (alpha != null) {
				Attribute attr = c.getAttribute(attrAlpha);
				if (attr == null)
					c.createAttribute(attrAlpha, alpha, false);
				else
					attr.setValue(alpha);
			}
			// save draw colour
			paint = nodeDrawPaint.transform(c);
			if (paint instanceof Color) {
				Attribute draw = c.getAttribute(attrDrawColor);
				if (draw == null)
					c.createAttribute(attrDrawColor, paint, false);
				else
					draw.setValue(paint);
			}
			alpha = nodeDrawPaint.transformAlpha(c);
			if (alpha != null) {
				Attribute attr = c.getAttribute(attrDrawAlpha);
				if (attr == null)
					c.createAttribute(attrDrawAlpha, alpha, false);
				else
					attr.setValue(alpha);
			}
			// save shape
			Integer shapeID = nodeShapes.transformId(c);
			Attribute attribute = c.getAttribute(attrShape);
			if (attribute == null)
				c.createAttribute(attrShape, shapeID, false);
			else
				attribute.setValue(shapeID);
			// save size
			Integer size = nodeSizes.transform(c);
			attribute = c.getAttribute(attrSize);
			if (attribute == null)
				c.createAttribute(attrSize, size, false);
			else
				attribute.setValue(size);
		}

		// set edge colours and visibility
		ONDEXEdgeColors edgeColors = activeViewer.getEdgeColors();
		Transformer<ONDEXRelation, Integer> edgeSizes = activeViewer.getEdgeStrokes().getEdgeSizeTransformer();
		for (ONDEXRelation r : graph.getEdges()) {
			// save colour
			Paint paint = edgeColors.transform(r);
			if (paint instanceof Color) {
				Attribute color = r.getAttribute(attrColor);
				if (color == null)
					r.createAttribute(attrColor, paint, false);
				else
					color.setValue(paint);
			}
			// save visibility
			Attribute visible = r.getAttribute(attrVisible);
			if (visible == null)
				r.createAttribute(attrVisible, Boolean.TRUE, false);
			else
				visible.setValue(Boolean.TRUE);
			// store label mask
			Attribute label = r.getAttribute(attrLabel);
			if (label == null)
				r.createAttribute(attrLabel, activeViewer.getEdgeLabels().getMask().get(r), false);
			else
				label.setValue(activeViewer.getEdgeLabels().getMask().get(r));
			// save size
			if (edgeSizes != null) {
				Integer size = edgeSizes.transform(r);
				Attribute attribute = r.getAttribute(attrSize);
				if (attribute == null)
					r.createAttribute(attrSize, size, false);
				else
					attribute.setValue(size);
			}
		}
	}

}
