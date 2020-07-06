//package net.sourceforge.ondex.ovtk2.annotator.chemical;
//
//import java.awt.GridLayout;
//import java.awt.Rectangle;
//import java.awt.Shape;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//import javax.swing.Icon;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JTextField;
//
//import net.sourceforge.ondex.core.AttributeName;
//import net.sourceforge.ondex.core.ONDEXConcept;
//import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
//import net.sourceforge.ondex.ovtk2.config.Config;
//import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
//import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
//import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2GraphMouse;
//import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2PickingMousePlugin;
//
//import org.apache.commons.collections15.Transformer;
//
///**
// * Change nodes to chemical compound drawings
// * 
// * @author taubertj
// * 
// */
//public class ChemicalAnnotator extends OVTK2Annotator implements ActionListener {
//
//	/**
//	 * generated
//	 */
//	private static final long serialVersionUID = -6080613033694298119L;
//
//	/**
//	 * Chemical attribute name
//	 */
//	private AttributeName an;
//
//	/**
//	 * Save previous shapes used
//	 */
//	private Transformer<ONDEXConcept, Shape> oldShapes;
//
//	/**
//	 * Track previous mouse over setting
//	 */
//	private boolean showMouseOver;
//
//	/**
//	 * This is the size of the drawing
//	 */
//	private JTextField sizeField = new JTextField("100");
//
//	/**
//	 * Use existing concept colour for border
//	 */
//	private JCheckBox useBorderColour;
//
//	/**
//	 * Annotator has been used
//	 */
//	private boolean used = false;
//
//	public ChemicalAnnotator(OVTK2PropertiesAggregator viewer) {
//		super(viewer);
//
//		// save the shapes
//		oldShapes = viewer.getVisualizationViewer().getRenderContext()
//				.getVertexShapeTransformer();
//
//		this.setLayout(new GridLayout(5, 1));
//		this.add(new JLabel(
//				"This will change nodes to chemical drawings for concepts with chemical attributes."));
//
//		// get size of drawing
//		JPanel sizePanel = new JPanel();
//		sizePanel.add(new JLabel("Size of drawing: "));
//		sizePanel.add(sizeField);
//		this.add(sizePanel);
//
//		useBorderColour = new JCheckBox("Use concept colour for border?");
//		this.add(useBorderColour);
//
//		// The magic button
//		JButton goButton = new JButton("Annotate Graph");
//		goButton.addActionListener(this);
//
//		// check for attributes in graph
//		an = viewer.getONDEXJUNGGraph().getMetaData()
//				.getAttributeName("ChemicalStructure");
//		if (an == null
//				|| viewer.getONDEXJUNGGraph().getConceptsOfAttributeName(an)
//						.size() == 0) {
//			this.add(new JLabel(
//					"No concepts with chemical attributes found. Cannot continue."));
//			goButton.setEnabled(false);
//		} else {
//			this.add(new JLabel("A total of "
//					+ viewer.getONDEXJUNGGraph().getConceptsOfAttributeName(an)
//							.size()
//					+ " concepts with chemical attributes found."));
//			goButton.setEnabled(true);
//		}
//		this.add(goButton);
//
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent e) {
//		used = true;
//
//		// reset icon transformer, might have been set by annotator
//		viewer.getVisualizationViewer().getRenderContext()
//				.setVertexIconTransformer(null);
//
//		// disable mouse over
//		OVTK2GraphMouse mouse = (OVTK2GraphMouse) viewer
//				.getVisualizationViewer().getGraphMouse();
//		OVTK2PickingMousePlugin picking = mouse.getOVTK2PickingMousePlugin();
//		if (picking != null) {
//			showMouseOver = picking.isShowMouseOver();
//			picking.setShowMouseOver(false);
//		}
//
//		// get drawing size
//		int size = 100;
//		try {
//			size = Integer.parseInt(sizeField.getText());
//		} catch (NumberFormatException nfe) {
//			// ignore
//		}
//
//		// this does all the work
//		Transformer<ONDEXConcept, Icon> vertexIconTransformer = new ChemicalNodeIconTransformer(
//				viewer, size, useBorderColour.isSelected());
//
//		// pick support uses shapes, so requires same size of shape too
//		Transformer<ONDEXConcept, Shape> vertexShapeTransformer = new Transformer<ONDEXConcept, Shape>() {
//
//			@Override
//			public Shape transform(ONDEXConcept arg0) {
//				if (viewer.getVisualizationViewer().getRenderContext()
//						.getVertexIconTransformer() != null) {
//					Icon icon = viewer.getVisualizationViewer()
//							.getRenderContext().getVertexIconTransformer()
//							.transform(arg0);
//					if (icon != null) {
//						int w = icon.getIconWidth();
//						int h = icon.getIconHeight();
//						return new Rectangle(-w / 2, -h / 2, w, h);
//					}
//				}
//				return oldShapes.transform(arg0);
//			}
//		};
//
//		// update visualisation
//		viewer.getVisualizationViewer().getRenderContext()
//				.setVertexIconTransformer(vertexIconTransformer);
//		viewer.getVisualizationViewer().getRenderContext()
//				.setVertexShapeTransformer(vertexShapeTransformer);
//		viewer.getVisualizationViewer().getModel().fireStateChanged();
//		viewer.getVisualizationViewer().repaint();
//		if (!Config.isApplet)
//			OVTK2Desktop.getInstance().getOVTK2Menu().updateMenuBar(viewer);
//	}
//
//	@Override
//	public String getName() {
//		return Config.language.getProperty("Name.Menu.Annotator.Chemical");
//	}
//
//	@Override
//	public boolean hasBeenUsed() {
//		// revert mouse over
//		OVTK2GraphMouse mouse = (OVTK2GraphMouse) viewer
//				.getVisualizationViewer().getGraphMouse();
//		OVTK2PickingMousePlugin picking = mouse.getOVTK2PickingMousePlugin();
//		if (picking != null)
//			picking.setShowMouseOver(showMouseOver);
//		return used;
//	}
//
//}
