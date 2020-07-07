package net.sourceforge.ondex.ovtk2.ui.console;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * 
 * @author lysenkoa
 * 
 */
public class OverviewWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	DrawPanel p;

	public OverviewWindow() {
		this.setTitle("Overalp statistics");
		p = new DrawPanel();
		p.setMinimumSize(new Dimension(800, 800));
		p.setMaximumSize(new Dimension(800, 800));
		p.setPreferredSize(new Dimension(800, 800));
		p.setSize(new Dimension(800, 800));
		JScrollPane jsp = new JScrollPane(p);
		this.getContentPane().add(jsp);
		this.setSize(800, 800);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.toFront();
		this.setVisible(true);
	}

	public JComponent getPanle() {
		return p;
	}

	private class DrawPanel extends JPanel {
		private static final long serialVersionUID = -4721639647150248020L;

		public void paint(final Graphics g) {
			final Graphics2D g2d = (Graphics2D) g;

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());

			g2d.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.getRenderingHints().put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
			g2d.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.getRenderingHints().put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.getRenderingHints().put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2d.getRenderingHints().put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.getRenderingHints().put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

			final Composite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f);
			g2d.setComposite(alphaComp);
			final double i = 3;
			final Color[] cls = new Color[] { Color.red, Color.green, Color.blue };
			final Shape circle = getCircle(320, 400, 160);
			g2d.fill(circle);
			for (double j = 0; j < i; j++) {
				final AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(360 / i * j), 400, 400);
				final Shape shape = at.createTransformedShape(circle);
				g2d.setColor(cls[(int) j]);
				g2d.fill(shape);
			}
		}
	}

	public Shape getCircle(double x, double y, double radius) {
		double lx = x - radius;
		double ly = y - radius;
		Ellipse2D.Double c = new Ellipse2D.Double(lx, ly, radius * 2, radius * 2);
		return c;
	}
}
