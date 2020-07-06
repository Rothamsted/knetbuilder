package net.sourceforge.ondex.ovtk2.ui.menu;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.ImageIcon;

/**
 * Paints a ImageIcon with X offset depending on menu.
 * 
 * @author taubertj
 * @version 25.02.2010
 */
public class CustomIcon extends ImageIcon {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 743724785346473584L;

	// menu component this icon is part of
	private CustomJMenuItem menu = null;

	/**
	 * Constructor for a given ImageIcon.
	 * 
	 * @param icon
	 *            ImageIcon to wrap
	 */
	public CustomIcon(String path, CustomJMenuItem menu) {
		super(path);
		this.menu = menu;
	}

	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		g.drawImage(getImage(), menu.getWidth() - getIconWidth(), y, c);
	}

	public int getRegionMin() {
		return menu.getWidth() - getIconWidth();
	}

	public int getRegionMax() {
		return menu.getWidth();
	}
}
