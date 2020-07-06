package net.sourceforge.ondex.ovtk2.ui.menu;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.help.HelpBroker;

/**
 * Captures click events on the question marks in the menu and calls help pages.
 * 
 * @author taubertj
 * @version 19.05.2008
 */
public class CustomMouseListener implements MouseListener {

	/**
	 * Former mouse action.
	 */
	public static boolean pressed = false;

	/**
	 * For mouse click help.
	 */
	private HelpBroker hb = null;

	/**
	 * Sets HelpBroker of JavaHelp for on-click help.
	 * 
	 * @param hb
	 *            HelpBroker of JavaHelp
	 */
	public void setHelpBroker(HelpBroker hb) {
		this.hb = hb;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getSource() instanceof CustomJMenuItem) {
			CustomJMenuItem menu = (CustomJMenuItem) e.getSource();
			CustomIcon icon = menu.getCustomIcon();
			if (e.getX() > icon.getRegionMin() && e.getX() < icon.getRegionMax()) {
				pressed = true;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() instanceof CustomJMenuItem) {
			CustomJMenuItem menu = (CustomJMenuItem) e.getSource();
			CustomIcon icon = menu.getCustomIcon();
			// make sure mouse was pressed and released on icon
			if (hb != null && pressed && e.getX() > icon.getRegionMin() && e.getX() < icon.getRegionMax()) {
				String cmd = menu.getActionCommand().substring(5);
				// try to call context sensitive help
				try {
					hb.setCurrentID(cmd);
					hb.setDisplayed(true);
				} catch (Exception ee) {
					// ee.printStackTrace();
				}
			}
		}
		pressed = false;
	}
}
