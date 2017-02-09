package net.sourceforge.ondex.ovtk2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.layout.OVTK2Layouter;
import net.sourceforge.ondex.ovtk2.util.VisualisationUtils;
import edu.uci.ics.jung.visualization.layout.ObservableCachingLayout;

/**
 * Presents the layout options of current active layout.
 * 
 * @author taubertj
 * 
 */
public class OVTK2LayoutOptions extends RegisteredJInternalFrame implements ActionListener {

	// generated
	private static final long serialVersionUID = 5016903790709100530L;

	// preferred size of this gadget
	private Dimension preferredSize = new Dimension(280, 210);

	// current OVTK2Viewer
	private OVTK2Viewer viewer = null;

	// current OVTK2Layouter
	private OVTK2Layouter ovtk2layouter = null;

	/**
	 * Initialize option view on a given viewer.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public OVTK2LayoutOptions(OVTK2Viewer viewer) {
		// set title and icon
		super(Config.language.getProperty("Options.Title"), "Options", Config.language.getProperty("Options.Title"), true, true, true, true);

		this.viewer = viewer;

		// dispose this on close
		this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

		initIcon();

		// set layout
		this.setViewer(viewer);
	}

	/**
	 * Sets frame icon from file.
	 * 
	 */
	private void initIcon() {
		File imgLocation = new File("config/toolbarButtonGraphics/development/Application16.gif");
		URL imageURL = null;

		try {
			imageURL = imgLocation.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		this.setFrameIcon(new ImageIcon(imageURL));
	}

	/**
	 * Sets GUI for a given OVTK2Layouter.
	 * 
	 * @param ovtk2layouter
	 *            OVTK2Layouter
	 */
	public void setLayouter(OVTK2Layouter ovtk2layouter) {
		this.ovtk2layouter = ovtk2layouter;

		JScrollPane scroll = new JScrollPane(ovtk2layouter.getOptionPanel());
		scroll.setPreferredSize(preferredSize);

		JButton button = new JButton(Config.language.getProperty("Options.Relayout"));
		button.addActionListener(this);

		// add to content pane
		this.getContentPane().removeAll();
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(scroll, BorderLayout.CENTER);
		this.getContentPane().add(button, BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Sets viewer to be used for these options.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public void setViewer(OVTK2Viewer viewer) {
		this.viewer = viewer;

		ObservableCachingLayout<ONDEXConcept, ONDEXRelation> layouter = (ObservableCachingLayout<ONDEXConcept, ONDEXRelation>) viewer.getVisualizationViewer().getGraphLayout();
		if (layouter.getDelegate() instanceof OVTK2Layouter) {
			setLayouter((OVTK2Layouter) layouter.getDelegate());
		} else {
			this.getContentPane().removeAll();
			this.getContentPane().setLayout(new GridLayout(1, 1));
			JLabel label = new JLabel(" Unsupported Layouter.");
			label.setPreferredSize(preferredSize);
			this.getContentPane().add(label);
			this.pack();
		}
	}

	/**
	 * Returns current viewer.
	 * 
	 * @return OVTK2Viewer
	 */
	public OVTK2Viewer getViewer() {
		return viewer;
	}

	public void actionPerformed(ActionEvent arg0) {
		if (ovtk2layouter != null && viewer != null) {
			VisualisationUtils.relayout(viewer, OVTK2Desktop.getInstance().getMainFrame());
		}
	}

}