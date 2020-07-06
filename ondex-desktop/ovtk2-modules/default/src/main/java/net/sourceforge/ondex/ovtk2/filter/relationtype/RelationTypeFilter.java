package net.sourceforge.ondex.ovtk2.filter.relationtype;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.filter.relationtype.Filter;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.RelationTypeListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * @author Jochen Weile, B.Sc.
 */
public class RelationTypeFilter extends OVTK2Filter implements
        ListSelectionListener, ActionListener {

    // ####FIELDS####

    /**
     * serial version id.
     */
    private static final long serialVersionUID = 9095797830070129811L;

    // used to wrap relation type set list
    private RelationTypeListModel rtslm = null;

    // displays selection list
    private JList list = null;

    // the important button
    private JButton goButton = null;

    // multiple selection is enabled
    private ArrayList<RelationType> targets = null;

    // change visibility
    private boolean visibility = false;
    
    /**
	 * Filter has been used
	 */
	private boolean used = false;

    // ####CONSTRUCTOR####

    /**
     *
     */
    public RelationTypeFilter(OVTK2Viewer viewer) {
        super(viewer);

        setLayout(new SpringLayout());

        // The magic button
        goButton = new JButton("Filter Graph");
        goButton.setEnabled(false);
        goButton.addActionListener(this);

        rtslm = new RelationTypeListModel();

        list = new JList(rtslm);
        list.setCellRenderer(new CustomCellRenderer());

        // get concept classes from meta data
        for (RelationType rts : graph.getMetaData().getRelationTypes()) {
            Set<ONDEXRelation> relations = graph
                    .getRelationsOfRelationType(rts);
            if (relations != null) {
                // check concepts exists on this ConceptClass
                if (relations.size() > 0) {
                    rtslm.addRelationType(rts);
                }
            }
        }

        // check if list is populated
        if (rtslm.getSize() == 0) {
            add(new JLabel("There are no RelationTypeSet Objects in the Graph."));
        } else {
            list.validate();
            list
                    .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list.addListSelectionListener(this);

            add(new JLabel("Select RelationTypeSet to filter nodes with"));
            add(new JScrollPane(list));
        }

        JRadioButton yesButton = new JRadioButton("true", true);
        yesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                visibility = true;
            }
        });
        JRadioButton noButton = new JRadioButton("false", false);
        noButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                visibility = false;
            }
        });

        ButtonGroup bgroup = new ButtonGroup();
        bgroup.add(yesButton);
        bgroup.add(noButton);
        noButton.setSelected(true);

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(1, 2));
        radioPanel.add(yesButton);
        radioPanel.add(noButton);
        radioPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Change visibility to:"));
        add(radioPanel);

        add(goButton);
        SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
                5, 5, 5);
    }

    // ####METHODS####

    /**
     *
     */
    @Override
    public String getName() {
        return Config.language.getProperty("Name.Menu.Filter.RelationType");
    }

    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        int[] indices = list.getSelectedIndices();
        if (indices.length > 0) {
            goButton.setEnabled(true);
            targets = new ArrayList<RelationType>();
            for (int i : indices) {
                targets.add(((RelationTypeListModel) list.getModel())
                        .getRelationTypeAt(i));
            }
        }
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            callFilter();
            used = true;
        } catch (InvalidPluginArgumentException e1) {
            ErrorDialog.show(e1);
        }
    }

    /**
     * Calls backend filter.
     */
    private void callFilter() throws InvalidPluginArgumentException {
        if (targets != null && targets.size() > 0) {
        	StateEdit edit = new StateEdit(
    				new VisibilityUndo(viewer.getONDEXJUNGGraph()), this.getName());
        	OVTK2Desktop desktop = OVTK2Desktop.getInstance();
    		desktop.setRunningProcess(this.getName());
        	
            // new instance of filter and set arguments
            Filter filter = new Filter();
            // construct filter arguments
            ONDEXPluginArguments fa = new ONDEXPluginArguments(filter.getArgumentDefinitions());
            Iterator<RelationType> it = targets.iterator();
            while (it.hasNext()) {
                fa.addOption(Filter.TARGETRTSET_ARG, it.next().getId());
            }

            filter.setONDEXGraph(graph);
            filter.setArguments(fa);

            // execute filter
            filter.start();

            // get results from filter
            Set<ONDEXConcept> concepts = filter.getInVisibleConcepts();
            Set<ONDEXRelation> relations = filter.getInVisibleRelations();

            // check for visibility selection
            if (visibility) {

                // first set concepts visible
                for (ONDEXConcept c : concepts) {
                    graph.setVisibility(c, true);
                }

                // second set relations visible
                for (ONDEXRelation r : relations) {
                    graph.setVisibility(r, true);
                }

            } else {

                // change visibility of relations
                for (ONDEXRelation r : relations) {
                    graph.setVisibility(r, false);
                }

                // change visibility of concepts
                for (ONDEXConcept c : concepts) {
                    graph.setVisibility(c, false);                }
            }

            // propagate change to viewer
            viewer.getVisualizationViewer().getModel().fireStateChanged();
            
            edit.end();
    		viewer.getUndoManager().addEdit(edit);
    		desktop.getOVTK2Menu().updateUndoRedo(viewer);
    		desktop.notifyTerminationOfProcess();
        }
    }

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
