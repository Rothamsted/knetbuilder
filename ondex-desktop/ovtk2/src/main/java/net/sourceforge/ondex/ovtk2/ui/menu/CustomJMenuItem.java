package net.sourceforge.ondex.ovtk2.ui.menu;

import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

/**
 * JMenuItem which aligns text on the left and the given icon on the right
 * border.
 * 
 * @author taubertj
 * @version 25.02.2010
 */
public class CustomJMenuItem extends JMenuItem {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 6964692321527850804L;

	/**
	 * icon for tooltip
	 */
	private CustomIcon customIcon = null;

	/**
	 * Constructors sets text of JMenuItem, an ImageIcon for a given path and a
	 * tooltip and the ImageIcon at the right.
	 * 
	 * @param text
	 *            menu item text
	 * @param path
	 *            path to icon image
	 * @param toolTip
	 *            tooltip to display at icon
	 */
	public CustomJMenuItem(String text, String path, String toolTip) {
		super(text);
		setToolTipText(toolTip);

		// icon on the right
		customIcon = new CustomIcon(path, this);
		setIcon(customIcon);
		setIconTextGap(10);

		// text on the left
		setHorizontalTextPosition(SwingConstants.LEFT);

		// 10 seconds until tool-tip disappears
		ToolTipManager.sharedInstance().setDismissDelay(10000);
	}

	/**
	 * Returns CustomIcon to keep track of actual location.
	 * 
	 * @return CustomIcon containing location information
	 */
	public CustomIcon getCustomIcon() {
		return customIcon;
	}

	/**
	 * Show tooltip only at icon position.
	 */
	@Override
	public String getToolTipText(MouseEvent event) {
		if (event.getX() > customIcon.getRegionMin() && event.getX() < customIcon.getRegionMax())
			return getToolTipText();
		else
			return null;
	}
}
