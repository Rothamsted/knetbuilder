package net.sourceforge.ondex.ovtk2.ui.popup;

import java.awt.Point;

/**
 * Used to set the point at which the mouse was clicked for those menu items
 * interested in this information. Useful, for example, if you want to bring up
 * a dialog box right at the point the mouse was clicked. The
 * PopupVertexEdgeMenuMousePlugin checks to see if a menu component implements
 * this interface and if so calls it to set the point.
 * 
 * @author Dr. Greg M. Bernstein
 */
public interface MenuPointListener {

	/**
	 * Sets the point of the mouse click.
	 * 
	 * @param point
	 *            Point
	 */
	void setPoint(Point point);

}
