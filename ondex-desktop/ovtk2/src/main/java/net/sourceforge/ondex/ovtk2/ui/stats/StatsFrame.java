package net.sourceforge.ondex.ovtk2.ui.stats;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;

/**
 * This is the internal frame that displays the graph statistics module.
 * 
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class StatsFrame extends RegisteredJInternalFrame implements ActionListener {

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -8058498591275711263L;

	// ####FIELDS####

	/**
	 * the data extractor that collects the dataset from the graph.
	 */
	private DataExtractor extractor;

	/**
	 * the displaypanel contains the results of the analysis and is located at
	 * the bottom of the frame.
	 */
	private DisplayPanel displayPanel;

	/**
	 * the chooser panel offers the different elements that the user can either
	 * use as variables or filters: concept classes, relation types, attribute
	 * names or graph features (like node degree). It is located on the upper
	 * left side of the frame.
	 */
	private ElementChooserPanel chooserPanel;

	/**
	 * the seleciton panel holds the variable selection field and the filter
	 * selection table. it is located on the upper right side of the frame.
	 */
	private SelectionPanel selectionPanel;

	/**
	 * the viewer that holds the graph visualization.
	 */
	private OVTK2Viewer viewer;

	// ####CONSTRUCTOR####

	/**
	 * constructor sets up the complete interface and all the children classes.
	 */
	public StatsFrame(OVTK2Viewer viewer) {
		super(viewer.getName(), "Stats", "Graph Statistics", true, true, true, true);

		this.viewer = viewer;

		this.extractor = new DataExtractor(viewer.getONDEXJUNGGraph());

		setupPanels();

		pack();
		setSize(new Dimension(700, 700));
		setVisible(true);
	}

	// ####METHODS####

	/**
	 * sets up all panels.
	 */
	private void setupPanels() {
		getContentPane().setLayout(new GridLayout(2, 1, 20, 20));

		displayPanel = new DisplayPanel(extractor);

		getContentPane().add(createInteractionPanel());
		getContentPane().add(displayPanel);
	}

	/**
	 * creates the panel that holds the element chooser panel, the swapping
	 * buttons and the seleciton panel.
	 * 
	 * @return the panel.
	 */
	private JPanel createInteractionPanel() {
		JPanel interactionPanel = new JPanel();
		interactionPanel.setLayout(new BoxLayout(interactionPanel, BoxLayout.LINE_AXIS));

		chooserPanel = new ElementChooserPanel(viewer.getONDEXJUNGGraph());
		interactionPanel.add(chooserPanel);

		JPanel swapperPanel = new JPanel();
		swapperPanel.setLayout(new BoxLayout(swapperPanel, BoxLayout.PAGE_AXIS));

		swapperPanel.add(Box.createVerticalStrut(10));

		JPanel variableSwapper = createSwapPanel("variable");
		swapperPanel.add(variableSwapper);

		swapperPanel.add(Box.createVerticalStrut(110));

		JPanel filterSwapper = createSwapPanel("filter");
		swapperPanel.add(filterSwapper);

		swapperPanel.add(Box.createVerticalStrut(110));

		interactionPanel.add(swapperPanel);

		selectionPanel = new SelectionPanel(viewer.getONDEXJUNGGraph(), this);
		interactionPanel.add(selectionPanel);

		return interactionPanel;
	}

	/**
	 * creates a panel that holds a pair of swap buttons.
	 * 
	 * @param cmd_base
	 *            the action command base string (either "variable" or "filter".
	 * @return the panel.
	 */
	private JPanel createSwapPanel(String cmd_base) {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 1));
		JButton fwd = createButton("stats/arrow", cmd_base + "_fwd");
		JButton bck = createButton("stats/arrow_back", cmd_base + "_bck");
		p.add(fwd);
		p.add(bck);
		p.setMaximumSize(new Dimension(53, 32));
		return p;
	}

	/**
	 * creates a swap button.
	 * 
	 * @param imageName
	 *            the name of the image file.
	 * @param actionCommand
	 *            the action command.
	 * @return
	 */
	private JButton createButton(String imageName, String actionCommand) {
		// Look for the image.
		File imgLocation = new File("config/toolbarButtonGraphics/" + imageName + ".png");
		URL imageURL = null;

		try {
			imageURL = imgLocation.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		// Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.addActionListener(this);

		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL));
		} else { // no image found
			System.err.println("Resource not found: " + imgLocation.getAbsolutePath());
		}

		return button;
	}

	/**
	 * activated whenever a swap-button was clicked or a value in the filter
	 * list was changed.
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("variable_fwd")) {
			if (selectionPanel.isVariableSet())
				chooserPanel.addElement(selectionPanel.popVariable());
			selectionPanel.setVariable(chooserPanel.popElement());
		} else if (cmd.equals("variable_bck")) {
			if (selectionPanel.isVariableSet())
				chooserPanel.addElement(selectionPanel.popVariable());
		} else if (cmd.equals("filter_fwd")) {
			if (chooserPanel.isElementSelected())
				selectionPanel.addFilter(chooserPanel.popElement());
		} else if (cmd.equals("filter_bck")) {
			if (selectionPanel.isFilterSelected())
				chooserPanel.addElement(selectionPanel.popSelectedFilter());
		}
		// finally:
		if (selectionPanel.isVariableSet()) {
			configureExtractor();
			displayPanel.updateWholeStatistics();
		}
	}

	/**
	 * configures the data extractor according to the current selections in the
	 * gui.
	 */
	private void configureExtractor() {
		extractor.setVariable(selectionPanel.getVariable());
		extractor.clearAllFilters();
		HashMap<Object, Object> filters = selectionPanel.getFilters();
		for (Object element : filters.keySet()) {
			if (element instanceof AttributeName) {
				AttributeName an = (AttributeName) element;
				extractor.addGDSFilter(an, filters.get(element));
			} else if (element instanceof ConceptClass) {
				ConceptClass cc = (ConceptClass) element;
				extractor.addCCFilter(cc);
			} else if (element instanceof RelationType) {
				RelationType rt = (RelationType) element;
				extractor.addRTFilter(rt);
			}
		}
	}

}
