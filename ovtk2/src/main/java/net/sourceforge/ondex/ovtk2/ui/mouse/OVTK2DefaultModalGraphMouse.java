package net.sourceforge.ondex.ovtk2.ui.mouse;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPopupMenu;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.popup.EdgeMenu;
import net.sourceforge.ondex.ovtk2.ui.popup.PopupVertexEdgeMenuMousePlugin;
import net.sourceforge.ondex.ovtk2.ui.popup.VertexMenu;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.xml.AnnotationXMLReader;
import net.sourceforge.ondex.ovtk2.util.xml.AnnotationXMLWriter;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.ctc.wstx.io.CharsetNames;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.annotations.AnnotatingModalGraphMouse;
import edu.uci.ics.jung.visualization.annotations.Annotation;
import edu.uci.ics.jung.visualization.annotations.AnnotationControls;
import edu.uci.ics.jung.visualization.annotations.AnnotationManager;
import edu.uci.ics.jung.visualization.annotations.AnnotationPaintable;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;

/**
 * Class extends JUNGs DefaultModalGraphMouse to be able to override the picking
 * producer. Also sets the popup menus.
 * 
 * @author taubertj, lysenkoa
 * @version 27.05.2008
 */
public class OVTK2DefaultModalGraphMouse extends AnnotatingModalGraphMouse<ONDEXConcept, ONDEXRelation> implements ActionListener, OVTK2GraphMouse {

	public static final String KEY = "graphmouse";

	private PopupVertexEdgeMenuMousePlugin<ONDEXConcept, ONDEXRelation> myPlugin = null;

	private boolean restoreMode = false;

	private ScalingControl scaler = new CrossoverScalingControl();

	private JInternalFrame toolBar = null;

	private OVTK2Viewer viewer = null;

	/**
	 * Initialises plugins for a given OVTK2Viewer. Every viewer has its own
	 * mouse.
	 * 
	 * @param viewer
	 *            current OVTK2Viewer
	 */
	public OVTK2DefaultModalGraphMouse(OVTK2Viewer viewer, AnnotatingGraphMousePlugin<ONDEXConcept, ONDEXRelation> annotatingPlugin) {
		super(viewer.getVisualizationViewer().getRenderContext(), annotatingPlugin);
		this.viewer = viewer;
		modeMenu = null;
		modeMenu = this.getModeMenu();
		// System.err.println(this.getModeMenu());

		// Trying out our new popup menu mouse producer...
		myPlugin = new PopupVertexEdgeMenuMousePlugin<ONDEXConcept, ONDEXRelation>();

		// Add some popup menus for the edges and vertices to our mouse
		// producer.
		JPopupMenu edgeMenu = new EdgeMenu(viewer);
		JPopupMenu vertexMenu = new VertexMenu(viewer);
		myPlugin.setEdgePopup(edgeMenu);
		myPlugin.setVertexPopup(vertexMenu);

		this.add(myPlugin); // Add our new producer to the mouse

		// picking mouse producer with mouse over highlighting
		this.pickingPlugin = new OVTK2PickingMousePlugin(viewer.getVisualizationViewer());

		// set default behaviour
		setViewScaling(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		// current graph
		ONDEXJUNGGraph graph = this.viewer.getONDEXJUNGGraph();

		// gain access to current plugin
		OVTK2AnnotatingGraphMousePlugin plugin = (OVTK2AnnotatingGraphMousePlugin) this.annotatingPlugin;

		// keeps track of annotations
		AnnotationManager manager = plugin.getAnnotationMananger();

		// System.out.println(graph.getAnnotations());

		// pass annotations to AnnotationControls
		if (cmd.equals("load") && graph.getAnnotations() != null && graph.getAnnotations().containsKey(KEY)) {

			// get current annotation from graph
			String xml = graph.getAnnotations().get(KEY);
			if (xml == null || xml.trim().length() == 0)
				return;

			// will contain de-serialised annotations
			Set<Annotation> annos = new HashSet<Annotation>();

			// configure XML input
			System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
			XMLInputFactory2 xmlInput = (XMLInputFactory2) XMLInputFactory2.newInstance();
			xmlInput.configureForSpeed();

			// parse from a String
			final ByteArrayInputStream inStream = new ByteArrayInputStream(xml.getBytes());
			try {
				// configure Parser
				XMLStreamReader2 xmlReadStream = (XMLStreamReader2) xmlInput.createXMLStreamReader(inStream, CharsetNames.CS_UTF8);

				// de-serialise annotations from XML
				AnnotationXMLReader.read(xmlReadStream, annos);

				xmlReadStream.close();

			} catch (XMLStreamException e1) {
				ErrorDialog.show(e1);
			}

			// add all annotations to manager
			for (Annotation anno : annos) {
				manager.add(anno.getLayer(), anno);
			}

			// update visualisation
			viewer.getVisualizationViewer().fireStateChanged();
			viewer.getVisualizationViewer().repaint();
		}

		// get annotations from AnnotationControls
		else if (cmd.equals("save")) {

			// annotations of lower layer
			AnnotationPaintable lower = manager.getAnnotationPaintable(Annotation.Layer.LOWER);

			// annotations of upper layer
			AnnotationPaintable upper = manager.getAnnotationPaintable(Annotation.Layer.UPPER);

			// all annotations of lower layer
			Set<Annotation> annos = new HashSet<Annotation>(lower.getAnnotations());

			// all annotations of upper layer
			annos.addAll(upper.getAnnotations());

			// configure XML output
			XMLOutputFactory2 xmlOutput = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
			xmlOutput.configureForSpeed();
			xmlOutput.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);

			// output goes into a String
			final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			try {
				// configure writer
				XMLStreamWriter2 xmlWriteStream = (XMLStreamWriter2) xmlOutput.createXMLStreamWriter(outStream, CharsetNames.CS_UTF8);

				// serialise Annotations to XML
				AnnotationXMLWriter.write(xmlWriteStream, annos);

				xmlWriteStream.flush();
				xmlWriteStream.close();

				// set annotation data to graph
				graph.getAnnotations().put(KEY, outStream.toString());

			} catch (XMLStreamException e1) {
				ErrorDialog.show(e1);
			}
		}
	}

	@Override
	public OVTK2PickingMousePlugin getOVTK2PickingMousePlugin() {
		if (this.pickingPlugin instanceof OVTK2PickingMousePlugin)
			return (OVTK2PickingMousePlugin) this.pickingPlugin;
		else
			return null;
	}

	/**
	 * @return the scaler
	 */
	@Override
	public ScalingControl getScaler() {
		return scaler;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isShiftDown()) {
			super.mousePressed(e);
			return;
		}
		if (e.getSource() instanceof VisualizationViewer<?, ?>) {

			Layout<ONDEXConcept, ONDEXRelation> layout = viewer.getVisualizationViewer().getGraphLayout();
			if (super.mode == Mode.TRANSFORMING || super.mode == Mode.ANNOTATING) {
				super.mousePressed(e);
				return;
			}
			restoreMode = true;
			Point2D p = e.getPoint();
			// is pick support available
			GraphElementAccessor<ONDEXConcept, ONDEXRelation> pickSupport = viewer.getVisualizationViewer().getPickSupport();
			if (pickSupport != null && (pickSupport.getEdge(layout, p.getX(), p.getY()) == null && pickSupport.getVertex(layout, p.getX(), p.getY()) == null)) {
				viewer.getVisualizationViewer().getPickedVertexState().clear();
				viewer.getVisualizationViewer().getPickedEdgeState().clear();
				((ModalGraphMouse) viewer.getVisualizationViewer().getGraphMouse()).setMode(Mode.TRANSFORMING);
			}
		}
		super.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (restoreMode == true) {
			restoreMode = false;
			if (e.getSource() instanceof VisualizationViewer<?, ?>) {
				VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
				((ModalGraphMouse) vv.getGraphMouse()).setMode(Mode.PICKING);
			}
		}
		super.mouseReleased(e);
	}

	@Override
	public void setMode(Mode mode) {
		super.setMode(mode);
		// cleanup
		this.remove(myPlugin);
		if (toolBar != null) {
			try {
				toolBar.setClosed(true);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
			toolBar = null;
		}

		// respect modes
		if (mode == Mode.TRANSFORMING) {
			this.add(myPlugin);
		} else if (mode == Mode.PICKING) {
			this.add(myPlugin);
		} else if (mode == Mode.ANNOTATING) {
			viewer.getVisualizationViewer().setCursor(this.annotatingPlugin.getCursor());

			AnnotationControls<ONDEXConcept, ONDEXRelation> annotationControls = new AnnotationControls<ONDEXConcept, ONDEXRelation>(annotatingPlugin);

			toolBar = new JInternalFrame(Config.language.getProperty("Viewer.ToolBar.AnnotationControls"), false, true, false, true);
			toolBar.setLayout(new FlowLayout());
			toolBar.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			toolBar.add(annotationControls.getAnnotationsToolBar());
			JButton load = new JButton("Load");
			load.setActionCommand("load");
			load.addActionListener(this);
			toolBar.add(load);
			JButton save = new JButton("Save");
			save.setActionCommand("save");
			save.addActionListener(this);
			toolBar.add(save);
			toolBar.pack();
			viewer.getDesktopPane().add(toolBar);
			toolBar.setVisible(true);
		}
	}

	/**
	 * @param scaler
	 *            the scaler to set
	 */
	@Override
	public void setScaler(ScalingControl scaler) {
		this.scaler = scaler;
	}

	/**
	 * Sets view scaling enabled or disabled.
	 * 
	 * @param enabled
	 */
	@Override
	public void setViewScaling(boolean enabled) {
		if (enabled) {
			// only use view scaling
			this.remove(scalingPlugin);
			this.scaler = new ViewScalingControl();
			this.scalingPlugin = new ScalingGraphMousePlugin(scaler, 0, out, in);
			this.add(scalingPlugin);
		} else {
			// cross over scaling
			this.remove(scalingPlugin);
			this.scaler = new CrossoverScalingControl();
			this.scalingPlugin = new ScalingGraphMousePlugin(scaler, 0, out, in);
			this.add(scalingPlugin);
		}
	}
}
