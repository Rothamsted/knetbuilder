package net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

/**
 * Paints a given shape as an icon.
 * 
 * @author taubertj
 * @version 23.05.2008
 */
public class ShapeIcon implements Icon {

	/**
	 * default width
	 */
	private int width = 20;

	/**
	 * default height
	 */
	private int height = 20;

	/**
	 * actual Shape to draw
	 */
	private Shape shape = null;

	/**
	 * draw only outline of Shape
	 */
	private boolean outline = false;

	/**
	 * Initialises a ShapeIcon wrapping the given Shape.
	 * 
	 * @param shape
	 *            Shape to wrap
	 */
	public ShapeIcon(Shape shape) {
		this(shape, false);
	}

	/**
	 * Initialises a ShapeIcon wrapping the given Shape and specifying whether
	 * or not to only draw the outline.
	 * 
	 * @param shape
	 *            Shape to wrap
	 * @param outline
	 *            only draw outline
	 */
	public ShapeIcon(Shape shape, boolean outline) {
		this.shape = shape;
		this.outline = outline;
		Rectangle2D bounds = shape.getBounds2D();
		this.width = (int) bounds.getWidth();
		this.height = (int) bounds.getHeight();
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g;

		// turn anti-aliasing on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.translate(x, y);
		g2d.translate(width / 2, height / 2);

		// fill shape completely
		if (!outline) {
			g2d.setColor(Color.BLUE);
			g2d.fill(shape);

			// draw only outline of shape
		} else {
			g2d.setColor(Color.BLACK);
			g2d.draw(shape);
		}

		g2d.dispose();
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
}
