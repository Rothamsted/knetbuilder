package net.sourceforge.ondex.ovtk2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer;
import edu.uci.ics.jung.visualization.control.ScalingControl;

/**
 * Represents a dynamic satellite view on the graph of a OVTK2Viewer.
 * 
 * @author taubertj
 * 
 */
public class OVTK2Satellite extends RegisteredJInternalFrame implements ActionListener {

	// generated
	private static final long serialVersionUID = 5016903790709100530L;

	// preferred size of this gadget
	private Dimension preferredSize = new Dimension(280, 210);

	// used for scaling by scaleToFit
	private ScalingControl scaler = new CrossoverScalingControl();

	// current OVTK2Viewer
	private OVTK2Viewer viewer = null;

	// contained satallite viewer
	private SatelliteVisualizationViewer<ONDEXConcept, ONDEXRelation> satellite = null;

	/**
	 * Initialise satellite view on a given viewer.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public OVTK2Satellite(OVTK2Viewer viewer) {
		// set title and icon
		super(Config.language.getProperty("Satellite.Title"), "Satellite", Config.language.getProperty("Satellite.Title"), true, true, true, true);

		// dispose viewer on close
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		initIcon();

		// set layout
		this.getContentPane().setLayout(new BorderLayout());
		this.setViewer(viewer);
		this.pack();
	}

	/**
	 * Sets frame icon from file.
	 * 
	 */
	private void initIcon() {
		File imgLocation = new File("config/toolbarButtonGraphics/development/Application16.gif");
		URL imageURL = null;

		try {
			imageURL = imgLocation.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		this.setFrameIcon(new ImageIcon(imageURL));
	}

	/**
	 * Calculates the bounds of all nodes in a given viewer.
	 * 
	 * @return Point2D[] min bounds, max bounds
	 */
	private Point2D[] calcBounds() {
		Point2D[] result = new Point2D[2];
		Point2D min = null;
		Point2D max = null;
		Layout<ONDEXConcept, ONDEXRelation> layout = viewer.getVisualizationViewer().getGraphLayout();
		Iterator<ONDEXConcept> it = layout.getGraph().getVertices().iterator();
		while (it.hasNext()) {
			Point2D point = layout.transform(it.next());
			if (min == null) {
				min = new Point2D.Double(0, 0);
				min.setLocation(point);
			}
			if (max == null) {
				max = new Point2D.Double(0, 0);
				max.setLocation(point);
			}
			min.setLocation(Math.min(min.getX(), point.getX()), Math.min(min.getY(), point.getY()));
			max.setLocation(Math.max(max.getX(), point.getX()), Math.max(max.getY(), point.getY()));
		}
		result[0] = min;
		result[1] = max;
		return result;
	}

	/**
	 * Scale current satellite view to fit in whole graph.
	 * 
	 */
	public void scaleToFit() {

		// reset scaling for predictive behaviour
		satellite.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).setToIdentity();
		satellite.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).setToIdentity();

		// place layout center in center of the view
		Point2D[] calc = calcBounds();
		Point2D min = calc[0];
		Point2D max = calc[1];
		Point2D layout_bounds = new Point2D.Double(max.getX() - min.getX(), max.getY() - min.getY());
		// layouter produced nice bounds
		if (layout_bounds.getX() > 0 && layout_bounds.getY() > 0) {
			Point2D screen_center = satellite.getCenter();
			Point2D layout_center = new Point2D.Double(screen_center.getX() - (layout_bounds.getX() / 2) - min.getX(), screen_center.getY() - (layout_bounds.getY() / 2) - min.getY());
			satellite.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).translate(layout_center.getX(), layout_center.getY());

			// scale graph
			Point2D scale_bounds = new Point2D.Double(satellite.getWidth() / layout_bounds.getX(), satellite.getHeight() / layout_bounds.getY());
			float scale = (float) Math.min(scale_bounds.getX(), scale_bounds.getY());
			scale = 0.85f * scale;
			scaler.scale(satellite, scale, satellite.getCenter());
		} else {
			// default scaler if layout not yet ready
			satellite.scaleToLayout(scaler);
		}
	}

	/**
	 * Sets viewer to be used in satellite view.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public void setViewer(OVTK2Viewer viewer) {
		this.viewer = viewer;

		// new satellite viewer
		satellite = new SatelliteVisualizationViewer<ONDEXConcept, ONDEXRelation>(viewer.getVisualizationViewer(), preferredSize);
		satellite.setPreferredSize(this.preferredSize);
		satellite.setSize(this.preferredSize);

		RenderContext<ONDEXConcept, ONDEXRelation> context = viewer.getVisualizationViewer().getRenderContext();

		// configure satellite appearance
		satellite.getRenderContext().setVertexDrawPaintTransformer(context.getVertexDrawPaintTransformer());
		satellite.getRenderContext().setVertexFillPaintTransformer(context.getVertexFillPaintTransformer());
		satellite.getRenderContext().setVertexShapeTransformer(context.getVertexShapeTransformer());
		satellite.getRenderContext().setEdgeDrawPaintTransformer(context.getEdgeDrawPaintTransformer());
		satellite.getRenderContext().setEdgeArrowPredicate(context.getEdgeArrowPredicate());
		satellite.getRenderContext().setEdgeStrokeTransformer(context.getEdgeStrokeTransformer());

		// add to content pane
		this.getContentPane().removeAll();
		this.getContentPane().add(satellite, BorderLayout.CENTER);

		JButton scaleToFit = new JButton(Config.language.getProperty("Satellite.ScaleToFit"));
		scaleToFit.addActionListener(this);
		this.getContentPane().add(scaleToFit, BorderLayout.SOUTH);
		this.revalidate();

		// fit graph in
		scaleToFit();
	}

	/**
	 * Returns current viewer.
	 * 
	 * @return OVTK2Viewer
	 */
	public OVTK2Viewer getViewer() {
		return viewer;
	}

	public void actionPerformed(ActionEvent arg0) {
		scaleToFit();
	}
}
