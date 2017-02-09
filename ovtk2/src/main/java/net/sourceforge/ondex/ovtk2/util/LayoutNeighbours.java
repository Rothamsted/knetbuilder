package net.sourceforge.ondex.ovtk2.util;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class LayoutNeighbours {

	static double arc = 360;

	static boolean clockwise = true;

	static boolean evendistribution = true;

	static double startangle = Math.random() * 360.0;

	public static void layoutNodes(VisualizationViewer<ONDEXConcept, ONDEXRelation> vv, ONDEXConcept center, Set<ONDEXConcept> neighbours) {

		// get position of centre node
		Layout<ONDEXConcept, ONDEXRelation> layout = vv.getGraphLayout();
		Point2D c;
		if (center == null)
			c = vv.getCenter();
		else
			c = layout.transform(center);

		// layout all concepts on one big circle
		ONDEXConcept[] vertices = neighbours.toArray(new ONDEXConcept[0]);
		Arrays.sort(vertices, new Comparator<ONDEXConcept>() {

			@Override
			public int compare(ONDEXConcept o1, ONDEXConcept o2) {
				ONDEXConcept c1 = o1;
				ONDEXConcept c2 = o2;
				String cc1 = c1.getOfType().getId();
				String cc2 = c2.getOfType().getId();
				return cc1.compareTo(cc2);
			}
		});

		double circleradius = 50;
		if (vertices.length > 1)
			circleradius = circleradius / Math.sin(Math.PI / vertices.length);

		double mpi = Math.PI / 180;
		double startRadians = startangle * mpi;

		double incrementAngle = arc / vertices.length;
		double incrementRadians = incrementAngle * mpi;

		if (arc < 360) {
			// this spreads the points out evenly across the arc
			if (evendistribution) {
				incrementAngle = arc / (vertices.length - 1);
				incrementRadians = incrementAngle * mpi;
			} else {
				incrementAngle = arc / vertices.length;
				incrementRadians = incrementAngle * mpi;
			}
		}

		for (int i = 0; i < vertices.length; i++) {
			Point2D coord = layout.transform(vertices[i]);

			double xp = c.getX() + Math.sin(startRadians) * circleradius;
			double yp = c.getY() + Math.cos(startRadians) * circleradius;

			coord.setLocation(xp, yp);

			layout.setLocation(vertices[i], coord);

			if (!clockwise) {
				startRadians += incrementRadians;
			} else {
				startRadians -= incrementRadians;
			}
		}

		startangle = Math.random() * 360.0;
	}
}
