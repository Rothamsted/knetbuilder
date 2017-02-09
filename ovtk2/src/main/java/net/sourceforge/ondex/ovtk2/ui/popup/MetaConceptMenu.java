package net.sourceforge.ondex.ovtk2.ui.popup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConcept;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaGraph;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class MetaConceptMenu extends JPopupMenu {

	// ####FIELDS####

	/**
	 * 
	 */
	private static final long serialVersionUID = 3906322309135035446L;

	/**
	 * 
	 */
	private final ONDEXMetaGraph metagraph;

	private final OVTK2Viewer viewer;

	// ####CONSTRUCTOR####

	/**
	 * 
	 */
	public MetaConceptMenu(final OVTK2Viewer viewer) {
		this.viewer = viewer;
		metagraph = viewer.getMetaGraph();

		add(new MetaConceptInfo());
		addSeparator();
		add(new MetaConceptVisibilityItem());

	}

	// ####METHODS####

	// ####CLASSES####

	/**
	 * 
	 */
	public class MetaConceptInfo extends JPanel implements VertexMenuListener<ONDEXMetaConcept, ONDEXMetaRelation> {

		// %%%%Fields of MetaConceptInfo%%%%
		/**
		 * 
		 */
		private static final long serialVersionUID = 1907416171271130788L;

		// %%%%Constructor of MetaConceptInfo%%%%

		/**
		 * 
		 */
		public MetaConceptInfo() {
			BoxLayout contentLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
			this.setLayout(contentLayout);

			TitledBorder infoBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Viewer.MetaConceptMenu.Info"));
			this.setBorder(infoBorder);
		}

		/**
		 * 
		 * @see net.sourceforge.ondex.ovtk2.ui.popup.VertexMenuListener#setVertexAndView(java.lang.Object,
		 *      edu.uci.ics.jung.visualization.VisualizationViewer)
		 */
		@Override
		public void setVertexAndView(ONDEXMetaConcept vertex, VisualizationViewer<ONDEXMetaConcept, ONDEXMetaRelation> visComp) {

			// empty panel
			this.removeAll();

			// get actual concept class
			ConceptClass cc = vertex.getConceptClass();

			// construct class info panel
			JPanel typePanel = new JPanel();
			typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.LINE_AXIS));

			// label for concept class
			typePanel.add(new JLabel(Config.language.getProperty("Viewer.MetaConceptMenu.OfType")));

			// box layout refinements
			typePanel.add(Box.createHorizontalGlue());
			typePanel.add(Box.createRigidArea(new Dimension(10, 0)));

			// actual relation type
			typePanel.add(new JLabel(cc.getId()));
			add(typePanel);

			// get number of concepts
			int num_c = vertex.getNumberOfConcepts();
			int vis_c = vertex.getNumberOfVisibleConcepts();

			// construct number info panel
			JPanel numberPanel = new JPanel(new BorderLayout());
			numberPanel.setLayout(new BoxLayout(numberPanel, BoxLayout.LINE_AXIS));

			// construct title label
			numberPanel.add(new JLabel(Config.language.getProperty("Viewer.MetaConceptMenu.NumberOfConcepts")));

			// box layout refinements
			numberPanel.add(Box.createHorizontalGlue());
			numberPanel.add(Box.createRigidArea(new Dimension(10, 0)));

			// construct number label
			numberPanel.add(new JLabel(vis_c + " (" + num_c + ")"));
			add(numberPanel);

			pack();
		}
	}

	/**
	 * 
	 */
	public class MetaConceptVisibilityItem extends JCheckBoxMenuItem implements VertexMenuListener<ONDEXMetaConcept, ONDEXMetaRelation>, ActionListener {

		// %%%%Fields of ShowMetaConcept%%%%

		private ONDEXMetaConcept vertex = null;

		/**
		 * 
		 */
		private static final long serialVersionUID = -8456057091366654027L;

		public MetaConceptVisibilityItem() {
			super(Config.language.getProperty("Viewer.MetaConceptMenu.Visible"));
			addActionListener(metagraph);
			addActionListener(this);
		}

		// %%%%Methods of ShowMetaConcept%%%%

		/**
		 * 
		 */
		@Override
		public void setVertexAndView(ONDEXMetaConcept vertex, VisualizationViewer<ONDEXMetaConcept, ONDEXMetaRelation> visComp) {
			this.vertex = vertex;
			setSelected(vertex.isVisible());
			setActionCommand(vertex.isVisible() ? "hide" : "show");
		}

		public ONDEXMetaConcept getMetaConcept() {
			return vertex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			viewer.getMetaGraphPanel().getVisualizationViewer().getModel().fireStateChanged();
		}

	}

}
