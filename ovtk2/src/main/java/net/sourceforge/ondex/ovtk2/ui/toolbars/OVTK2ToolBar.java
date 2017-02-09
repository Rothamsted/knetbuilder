package net.sourceforge.ondex.ovtk2.ui.toolbars;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.Border;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.menu.actions.ViewMenuAction;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

/**
 * Implements toolbar functionality of OVTK2.
 * 
 * @author taubertj
 * @version 14.05.2008
 */
public class OVTK2ToolBar extends JToolBar implements ActionListener, ItemListener {
	public static final String ANNOTATION_MODE = "annotation_mode";
	public static final String TRANSFORMING_MODE = "transforming_mode";
	public static final String PICKING_MODE = "picking_mode";
	public static final String REFRESH = "refresh";
	public static final String ZOOMIN = "zoomin";
	public static final String ZOOMOUT = "zoomout";
	public static final String CENTER = "center";

	/**
	 * generated
	 */
	private static final long serialVersionUID = -3571982833485712256L;

	/**
	 * parent OVTK2Desktop
	 */
	private OVTK2Desktop desktop = null;

	/**
	 * Transforming mouse mode.
	 */
	private JToggleButton transformingToolBarButton;

	/**
	 * Picking mouse mode.
	 */
	private JToggleButton pickingToolBarButton;

	/**
	 * Annotation mouse mode.
	 */
	private JToggleButton annotationToolBarButton;

	/**
	 * Whether or not we have to re-scale our icons.
	 */
	private boolean rescale = false;

	/**
	 * Provides graph search functions.
	 */
	private MenuGraphSearchBox searchField = null;

	/**
	 * Current mouse mode selection
	 */
	private String mouseMode = null;

	/**
	 * Setup toolbar for a given OVTK2Desktop.
	 * 
	 * @param desktop
	 *            parent OVTK2Desktop
	 */
	public OVTK2ToolBar(OVTK2Desktop desktop) {

		// add new JToolBar to desktop
		super(Config.language.getProperty("ToolBar.Title"));

		// Get the default toolkit
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		// Get the current screen size
		Dimension scrnsize = toolkit.getScreenSize();

		// half size buttons for small screens
		if (scrnsize.getWidth() < 1200)
			rescale = true;

		// make sure tool bar actions are handled
		this.desktop = desktop;
		ToolBarAction toolbarAction = new ToolBarAction();
		desktop.addActionListener(toolbarAction);
		SearchBoxAction searchAction = new SearchBoxAction();
		desktop.addActionListener(searchAction);

		// general program function
		if (Boolean.parseBoolean(Config.config.getProperty("Webservice.Enable"))) {
			this.add(makeToolBarButton("load", Config.language.getProperty("ToolBar.LoadToolTip")));
		}

		// this.add(makeToolBarButton("new", Config.properties
		// .getProperty("ToolBar.NewToolTip")));

		this.add(makeToolBarButton("open", Config.language.getProperty("ToolBar.OpenToolTip")));

		this.add(makeToolBarButton("save", Config.language.getProperty("ToolBar.SaveToolTip")));

		// this.add(makeToolBarButton("about", Config.properties
		// .getProperty("ToolBar.AboutToolTip")));

		this.addSeparator();

		// graph edit functions
		this.add(makeToolBarButton("add", Config.language.getProperty("ToolBar.AddToolTip")));

		this.add(makeToolBarCheckBox("contentsdisplay", Config.language.getProperty("ToolBar.ContentsDisplayToolTip")));

		this.add(makeToolBarButton("edit", Config.language.getProperty("ToolBar.EditToolTip")));

		this.add(makeToolBarButton("delete", Config.language.getProperty("ToolBar.DeleteToolTip")));

		this.add(makeToolBarButton("copy", Config.language.getProperty("ToolBar.CopyToolTip")));

		this.addSeparator();

		// visualisation functions
		this.add(makeToolBarButton(REFRESH, Config.language.getProperty("ToolBar.RefreshToolTip")));

		this.add(makeToolBarButton(ZOOMIN, Config.language.getProperty("ToolBar.ZoomInToolTip")));

		this.add(makeToolBarButton(ZOOMOUT, Config.language.getProperty("ToolBar.ZoomOutToolTip")));

		this.add(makeToolBarButton(CENTER, Config.language.getProperty("ToolBar.CenterToolTip")));

		this.addSeparator();

		// mouse modes
		transformingToolBarButton = makeToggleToolBarButton("arrow_pointer", TRANSFORMING_MODE, Config.language.getProperty("ToolBar.TransformingToolTip"));
		transformingToolBarButton.setSelected(false);
		this.add(transformingToolBarButton);

		pickingToolBarButton = makeToggleToolBarButton("hand_pointer", PICKING_MODE, Config.language.getProperty("ToolBar.PickingToolTip"));
		pickingToolBarButton.setSelected(true);
		this.add(pickingToolBarButton);

		annotationToolBarButton = makeToggleToolBarButton("anno_pointer", ANNOTATION_MODE, Config.language.getProperty("ToolBar.AnnotationToolTip"));
		annotationToolBarButton.setSelected(false);
		this.add(annotationToolBarButton);

		this.addSeparator();

		// construct search panel
		searchField = new MenuGraphSearchBox();
		searchField.setActionCommand("search");
		searchField.addActionListener(desktop);
		this.add(searchField);
	}

	// used for transparent buttons
	Border emptyBorder = BorderFactory.createEmptyBorder();

	// used for transparent buttons
	Insets noInsets = new Insets(0, 0, 0, 0);

	/**
	 * Creates a JButton with picture for the toolbar.
	 * 
	 * @param actionCommand
	 *            internal command and name of picture
	 * @param toolTipText
	 *            text of tool tip
	 * @return JButton
	 */
	private JButton makeToolBarButton(String actionCommand, String toolTipText) {

		String s = File.separator;

		// Look for the image.
		String path = Config.ovtkDir + s + "themes" + s + Config.config.getProperty("Program.Theme") + s + "icons" + s + "toolbar" + s;
		File imgLocation = new File(path + actionCommand + ".png");
		if (!imgLocation.exists()) {
			ErrorDialog.show(new IOException("File not found " + imgLocation.getAbsolutePath()));
		}

		File imgLocationOver = new File(path + actionCommand + "_over.png");
		if (!imgLocationOver.exists()) {
			ErrorDialog.show(new IOException("File not found " + imgLocationOver.getAbsolutePath()));
		}
		File imgLocationPressed = new File(path + actionCommand + "_pressed.png");
		if (!imgLocationPressed.exists()) {
			ErrorDialog.show(new IOException("File not found " + imgLocationPressed.getAbsolutePath()));
		}

		URL imageURL = null;
		URL imageURLOver = null;
		URL imageURLPressed = null;

		try {
			imageURL = imgLocation.toURI().toURL();
			imageURLOver = imgLocationOver.toURI().toURL();
			imageURLPressed = imgLocationPressed.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		// Create and initialize the button.
		JButton button = new JButton();
		button.setMargin(noInsets);
		button.setBorder(emptyBorder);
		button.setContentAreaFilled(false);
		button.setFocusable(false);
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(desktop);

		if (imageURL != null) { // image found
			ImageIcon icon = new ImageIcon(imageURL);
			if (rescale)
				icon = scale(icon.getImage(), 0.66);
			button.setIcon(icon);
		} else { // no image found
			System.err.println("Resource not found: " + imgLocation.getAbsolutePath());
		}

		if (imageURLOver != null) { // image found
			ImageIcon icon = new ImageIcon(imageURLOver);
			if (rescale)
				icon = scale(icon.getImage(), 0.66);
			button.setRolloverIcon(icon);
		} else { // no image found
			System.err.println("Resource not found: " + imgLocationOver.getAbsolutePath());
		}

		if (imageURLPressed != null) { // image found
			ImageIcon icon = new ImageIcon(imageURLPressed);
			if (rescale)
				icon = scale(icon.getImage(), 0.66);
			button.setPressedIcon(icon);
		} else { // no image found
			System.err.println("Resource not found: " + imgLocationPressed.getAbsolutePath());
		}

		return button;
	}

	/**
	 * Scales a given Image and returns a new ImageIcon.
	 * 
	 * @param src
	 *            Image to scale
	 * @param scale
	 *            scale factor
	 * @return new ImageIcon
	 */
	private ImageIcon scale(Image src, double scale) {
		int w = (int) (scale * src.getWidth(this));
		int h = (int) (scale * src.getHeight(this));
		int type = BufferedImage.TYPE_INT_ARGB;
		BufferedImage dst = new BufferedImage(w, h, type);
		Graphics2D g2 = dst.createGraphics();
		g2.drawImage(src, 0, 0, w, h, this);
		g2.dispose();
		return new ImageIcon(dst);
	}

	/**
	 * Creates a JButton to propagate to a JCheckBoxMenuItem with picture for
	 * the toolbar.
	 * 
	 * @param actionCommand
	 *            internal command and name of picture
	 * @param toolTipText
	 *            text of tool tip
	 * @return JButton
	 */
	private JButton makeToolBarCheckBox(String actionCommand, String toolTipText) {

		String s = File.separator;
		// Look for the image.
		String path = Config.ovtkDir + s + "themes" + s + Config.config.getProperty("Program.Theme") + s + "icons" + s + "toolbar" + s;
		File imgLocation = new File(path + actionCommand + ".png");
		if (!imgLocation.exists()) {
			ErrorDialog.show(new IOException("File not found " + imgLocation.getAbsolutePath()));
		}
		File imgLocationOver = new File(path + actionCommand + "_over.png");
		if (!imgLocationOver.exists()) {
			ErrorDialog.show(new IOException("File not found " + imgLocationOver.getAbsolutePath()));
		}
		File imgLocationPressed = new File(path + actionCommand + "_pressed.png");
		if (!imgLocationPressed.exists()) {
			ErrorDialog.show(new IOException("File not found " + imgLocationPressed.getAbsolutePath()));
		}
		URL imageURL = null;
		URL imageURLOver = null;
		URL imageURLPressed = null;

		try {
			imageURL = imgLocation.toURI().toURL();
			imageURLOver = imgLocationOver.toURI().toURL();
			imageURLPressed = imgLocationPressed.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		// Create and initialize the button.
		JButton button = new JButton();
		button.setMargin(noInsets);
		button.setBorder(emptyBorder);
		button.setContentAreaFilled(false);
		button.setFocusable(false);
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// wrap in check box menu item
				JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem();
				// this is a hack for solving OVTK-253, makes method not generic
				checkBox.setSelected(!ViewMenuAction.isContentsDisplayShown());
				desktop.actionPerformed(new ActionEvent(checkBox, ActionEvent.ACTION_PERFORMED, e.getActionCommand()));
			}
		});

		if (imageURL != null) { // image found
			ImageIcon icon = new ImageIcon(imageURL);
			if (rescale)
				icon = scale(icon.getImage(), 0.66);
			button.setIcon(icon);
		} else { // no image found
			System.err.println("Resource not found: " + imgLocation.getAbsolutePath());
		}

		if (imageURLOver != null) { // image found
			ImageIcon icon = new ImageIcon(imageURLOver);
			if (rescale)
				icon = scale(icon.getImage(), 0.66);
			button.setRolloverIcon(icon);
		} else { // no image found
			System.err.println("Resource not found: " + imgLocationOver.getAbsolutePath());
		}

		if (imageURLPressed != null) { // image found
			ImageIcon icon = new ImageIcon(imageURLPressed);
			if (rescale)
				icon = scale(icon.getImage(), 0.66);
			button.setPressedIcon(icon);
		} else { // no image found
			System.err.println("Resource not found: " + imgLocationPressed.getAbsolutePath());
		}

		return button;
	}

	/**
	 * Creates a JButton with picture for the toolbar.
	 * 
	 * @param imageName
	 *            name of picture to use
	 * @param actionCommand
	 *            internal command
	 * @param toolTipText
	 *            text of tool tip
	 * @return JButton
	 */
	private JToggleButton makeToggleToolBarButton(String imageName, String actionCommand, String toolTipText) {
		String s = File.separator;
		// Look for the image.
		File imgLocation = new File(Config.ovtkDir + s + "themes" + s + Config.config.getProperty("Program.Theme") + s + "icons" + s + "toolbar" + s + imageName + ".png");
		if (!imgLocation.exists()) {
			ErrorDialog.show(new IOException("File not found " + imgLocation.getAbsolutePath()));
		}
		URL imageURL = null;

		try {
			imageURL = imgLocation.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		// Create and initialize the button.
		JToggleButton button = new JToggleButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(desktop);
		button.addActionListener(this);

		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL));
		} else { // no image found
			System.err.println("Resource not found: " + imgLocation.getAbsolutePath());
		}

		return button;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		String cmd = arg0.getActionCommand();

		// switch to picking mode
		if (cmd.equals(PICKING_MODE)) {
			pickingToolBarButton.setSelected(true);
			transformingToolBarButton.setSelected(false);
			annotationToolBarButton.setSelected(false);
			mouseMode = cmd;
		}

		// switch to transforming mode
		else if (cmd.equals(TRANSFORMING_MODE)) {
			pickingToolBarButton.setSelected(false);
			transformingToolBarButton.setSelected(true);
			annotationToolBarButton.setSelected(false);
			mouseMode = cmd;
		}

		// switch to annotation mode
		else if (cmd.equals(ANNOTATION_MODE)) {
			pickingToolBarButton.setSelected(false);
			transformingToolBarButton.setSelected(false);
			annotationToolBarButton.setSelected(true);
			mouseMode = cmd;
		}
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {

		Object item = arg0.getItem();

		// switch to picking mode
		if (item.equals(ModalGraphMouse.Mode.PICKING)) {
			pickingToolBarButton.setSelected(true);
			transformingToolBarButton.setSelected(false);
			annotationToolBarButton.setSelected(false);
			mouseMode = PICKING_MODE;
		}

		// switch to transforming mode
		else if (item.equals(ModalGraphMouse.Mode.TRANSFORMING)) {
			pickingToolBarButton.setSelected(false);
			transformingToolBarButton.setSelected(true);
			annotationToolBarButton.setSelected(false);
			mouseMode = TRANSFORMING_MODE;
		}

		// switch to annotation mode
		else if (item.equals(ModalGraphMouse.Mode.ANNOTATING)) {
			pickingToolBarButton.setSelected(false);
			transformingToolBarButton.setSelected(false);
			annotationToolBarButton.setSelected(true);
			mouseMode = ANNOTATION_MODE;
		}
	}

	/**
	 * Returns the search box used in this toolbar.
	 * 
	 * @return MenuGraphSearchBox
	 */
	public MenuGraphSearchBox getSearchBox() {
		return searchField;
	}

	/**
	 * Returns current mouse mode setting.
	 * 
	 * @return String
	 */
	public String getMouseMode() {
		return mouseMode;
	}

}
