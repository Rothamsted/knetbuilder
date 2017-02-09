package net.sourceforge.ondex.ovtk2.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import net.sourceforge.ondex.ovtk2.config.Config;

public abstract class OVTK2Dialog extends RegisteredJInternalFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	// width for all field elements
	protected int fieldWidth = Integer.parseInt(Config.config.getProperty("Dialog.FieldWidth"));

	// height for all field elements
	protected int fieldHeight = Integer.parseInt(Config.config.getProperty("Dialog.FieldHeight"));

	// background for required fields
	protected Color requiredColor = null;

	/**
	 * Constructor sets the title of the JInternalFrame for given config
	 * property.
	 * 
	 * @param configTitle
	 *            config property for internal frame title
	 * @param icon
	 *            relative icon filename of internal frame
	 */
	public OVTK2Dialog(String configTitle, String icon) {
		super(Config.language.getProperty(configTitle), "Dialog", Config.language.getProperty(configTitle), true, true, true, true);

		String[] split = Config.config.getProperty("Dialog.RequiredColor").split(",");
		requiredColor = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
		initIcon(icon);
	}

	/**
	 * Returns common field height.
	 * 
	 * @return int
	 */
	public int getFieldHeight() {
		return fieldHeight;
	}

	/**
	 * Returns common field width.
	 * 
	 * @return int
	 */
	public int getFieldWidth() {
		return fieldWidth;
	}

	/**
	 * Returns color for required fields.
	 * 
	 * @return Color
	 */
	public Color getRequiredColor() {
		return requiredColor;
	}

	/**
	 * Sets frame icon from file.
	 * 
	 */
	private void initIcon(String icon) {
		File imgLocation = new File("config/themes/" + Config.config.getProperty("Program.Theme") + "/icons/" + icon);
		URL imageURL = null;

		try {
			imageURL = imgLocation.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		this.setFrameIcon(new ImageIcon(imageURL));
	}

	/**
	 * Adds a given JInternalFrame to the desktop and centers it.
	 * 
	 * @param internal
	 *            JInternalFrame to be displayed centered
	 */
	protected void displayCentered(JInternalFrame internal) {

		// do the location calculation
		Rectangle visible = this.getDesktopPane().getVisibleRect();
		Dimension size = internal.getSize();
		internal.setLocation((visible.width / 2) - (size.width / 2), (visible.height / 2) - (size.height / 2));

		// add to desktop and show
		this.getDesktopPane().add(internal);
		internal.setVisible(true);
		internal.toFront();
	}

	/**
	 * Make Button input panel.
	 * 
	 * @param configApply
	 *            config property for button apply text
	 * @param configCancel
	 *            config property for button cancel text
	 * @return JPanel
	 */
	protected JPanel makeButtonsPanel(String configApply, String configCancel) {

		JPanel buttons = new JPanel();

		if (configApply != null) {
			JButton apply = new JButton(Config.language.getProperty(configApply));
			apply.setActionCommand("apply");
			apply.addActionListener(this);
			buttons.add(apply);
		}

		if (configCancel != null) {
			JButton cancel = new JButton(Config.language.getProperty(configCancel));
			cancel.setActionCommand("cancel");
			cancel.addActionListener(this);
			buttons.add(cancel);
		}

		return buttons;
	}
}
