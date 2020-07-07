package net.sourceforge.ondex.ovtk2.graph.custom;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

/**
 * Paints different colours at the same time.
 * 
 * @author taubertj
 * 
 */
public class MultiColorEdgePaint implements Paint {

	/**
	 * which colours to use
	 */
	private Color[] colors = null;

	/**
	 * Sets the ordered colours to use.
	 * 
	 * @param colors
	 *            ordered colours
	 */
	public MultiColorEdgePaint(Color[] colors) {
		this.colors = colors;
	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
		// fractions of when particular colour peaks, equal distance
		float[] fractions = new float[colors.length];
		float div = 1.0f / colors.length;
		for (int i = 0; i < colors.length; i++) {
			fractions[i] = (div / 2) + (i * div);
		}

		// start and end point
		Point2D dStart = new Point2D.Double(userBounds.getX(), userBounds.getY());
		Point2D dEnd = new Point2D.Double(userBounds.getX() + userBounds.getWidth(), userBounds.getY());

		try {
			return new LinearGradientPaintContext(cm, deviceBounds, userBounds, xform, hints, dStart, dEnd, fractions, colors, MultipleGradientPaint.NO_CYCLE, MultipleGradientPaint.SRGB);
		} catch (NoninvertibleTransformException nte) {
			nte.printStackTrace();
		}
		return null;
	}

	@Override
	public int getTransparency() {
		return OPAQUE;
	}

}
