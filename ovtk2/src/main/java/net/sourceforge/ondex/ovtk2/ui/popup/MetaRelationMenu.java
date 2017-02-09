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

import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConcept;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaGraph;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class MetaRelationMenu extends JPopupMenu {

	// ####FIELDS####

	/**
	 * 
	 */
	private static final long serialVersionUID = -4251565524559298093L;

	/**
	 * 
	 */
	private final ONDEXMetaGraph metagraph;

	private final OVTK2Viewer viewer;

	// ####CONSTRUCTOR####

	/**
	 * 
	 */
	public MetaRelationMenu(final OVTK2Viewer viewer) {
		this.viewer = viewer;
		metagraph = viewer.getMetaGraph();

		add(new MetaRelationInfo());
		addSeparator();
		add(new MetaRelationVisibilityItem());

	}

	// ####METHODS####

	public class MetaRelationInfo extends JPanel implements EdgeMenuListener<ONDEXMetaConcept, ONDEXMetaRelation> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8524248617933786472L;

		public MetaRelationInfo() {
			BoxLayout contentLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
			this.setLayout(contentLayout);

			TitledBorder infoBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Viewer.MetaRelationMenu.Info"));
			this.setBorder(infoBorder);
		}

		/**
		 * 
		 * @see net.sourceforge.ondex.ovtk2.ui.popup.EdgeMenuListener#setEdgeAndView(java.lang.Object,
		 *      edu.uci.ics.jung.visualization.VisualizationViewer)
		 */
		@Override
		public void setEdgeAndView(ONDEXMetaRelation edge, VisualizationViewer<ONDEXMetaConcept, ONDEXMetaRelation> visComp) {

			// empty panel
			this.removeAll();

			// get actual relation type
			RelationType rt = edge.getRelationType();

			// construct type info panel
			JPanel typePanel = new JPanel();
			typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.LINE_AXIS));

			// label for relation type
			typePanel.add(new JLabel(Config.language.getProperty("Viewer.MetaRelationMenu.OfType")));

			// box layout refinements
			typePanel.add(Box.createHorizontalGlue());
			typePanel.add(Box.createRigidArea(new Dimension(10, 0)));

			// actual relation type
			typePanel.add(new JLabel(rt.getId()));
			add(typePanel);

			// get numbers of relations
			int num_r = edge.getNumberOfRelations();
			int vis_r = edge.getNumberOfVisibleRelations();

			// construct number info panel
			JPanel numberPanel = new JPanel(new BorderLayout());
			numberPanel.setLayout(new BoxLayout(numberPanel, BoxLayout.LINE_AXIS));

			// construct title label
			numberPanel.add(new JLabel(Config.language.getProperty("Viewer.MetaRelationMenu.NumberOfRelations")));

			// box layout refinements
			numberPanel.add(Box.createHorizontalGlue());
			numberPanel.add(Box.createRigidArea(new Dimension(10, 0)));

			// construct number label
			numberPanel.add(new JLabel(vis_r + " (" + num_r + ")"));
			add(numberPanel);

			pack();
		}
	}

	public class MetaRelationVisibilityItem extends JCheckBoxMenuItem implements EdgeMenuListener<ONDEXMetaConcept, ONDEXMetaRelation>, ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1651533872669083135L;

		private ONDEXMetaRelation edge = null;

		public MetaRelationVisibilityItem() {
			super(Config.language.getProperty("Viewer.MetaRelationMenu.Visible"));
			addActionListener(metagraph);
			addActionListener(this);
		}

		@Override
		public void setEdgeAndView(ONDEXMetaRelation edge, VisualizationViewer<ONDEXMetaConcept, ONDEXMetaRelation> visComp) {
			this.edge = edge;
			setSelected(edge.isVisible());
			setActionCommand(edge.isVisible() ? "hide" : "show");
		}

		public ONDEXMetaRelation getMetaRelation() {
			return edge;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			viewer.getMetaGraphPanel().getVisualizationViewer().getModel().fireStateChanged();

		}

	}

}
