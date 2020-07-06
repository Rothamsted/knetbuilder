package net.sourceforge.ondex.ovtk2.filter.relationtypemissing;

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
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.filter.relationtypemissing.Filter;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.ConceptClassListModel;
import net.sourceforge.ondex.ovtk2.util.listmodel.RelationTypeListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * @author Jan Taubert
 */
public class RelationTypeMissingFilter extends OVTK2Filter implements
        ListSelectionListener, ActionListener {

    // ####FIELDS####

    /**
     * serial version id.
     */
    private static final long serialVersionUID = 9095797830070129812L;

    // used to wrap concept class list
    private ConceptClassListModel cclm = null;

    // used to wrap relation type list
    private RelationTypeListModel rtslm = null;

    // displays selection list
    private JList cclist = null;

    // displays selection list
    private JList rtlist = null;

    // the important button
    private JButton goButton = null;

    // multiple selection is enabled
    private ArrayList<ConceptClass> ccs = null;

    // multiple selection is enabled
    private ArrayList<RelationType> rts = null;

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
    public RelationTypeMissingFilter(OVTK2Viewer viewer) {
        super(viewer);

        setLayout(new SpringLayout());

        // The magic button
        goButton = new JButton("Filter Graph");
        goButton.setEnabled(false);
        goButton.addActionListener(this);

        cclm = new ConceptClassListModel();

        cclist = new JList(cclm);
        cclist.setCellRenderer(new CustomCellRenderer());

        // get concept classes from meta data
        for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
            Set<ONDEXConcept> concepts = graph
                    .getConceptsOfConceptClass(cc);
            if (concepts != null) {
                // check concepts exists on this ConceptClass
                if (concepts.size() > 0) {
                    cclm.addConceptClass(cc);
                }
            }
        }

        // check if list is populated
        if (cclm.getSize() == 0) {
            add(new JLabel("There are no ConceptClass Objects in the Graph."));
        } else {
            cclist.validate();
            cclist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            cclist.addListSelectionListener(this);

            add(new JLabel("Select ConceptClass to filter nodes with"));
            add(new JScrollPane(cclist));
        }

        rtslm = new RelationTypeListModel();

        rtlist = new JList(rtslm);
        rtlist.setCellRenderer(new CustomCellRenderer());

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
            add(new JLabel("There are no RelationType Objects in the Graph."));
        } else {
            rtlist.validate();
            rtlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            rtlist.addListSelectionListener(this);

            add(new JLabel("Select RelationType to filter nodes with"));
            add(new JScrollPane(rtlist));
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
        return Config.language
                .getProperty("Name.Menu.Filter.RelationTypeMissing");
    }

    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource().equals(rtlist)) {
            int[] indices = rtlist.getSelectedIndices();
            if (indices.length > 0) {
                goButton.setEnabled(true);
                rts = new ArrayList<RelationType>();
                for (int i : indices) {
                    rts.add(((RelationTypeListModel) rtlist.getModel())
                            .getRelationTypeAt(i));
                }
            }
        }
        if (e.getSource().equals(cclist)) {
            int[] indices = cclist.getSelectedIndices();
            if (indices.length > 0) {
                goButton.setEnabled(true);
                ccs = new ArrayList<ConceptClass>();
                for (int i : indices) {
                    ccs.add(((ConceptClassListModel) cclist.getModel())
                            .getConceptClassAt(i));
                }
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
        if (rts != null && rts.size() > 0 && ccs != null && ccs.size() > 0) {
        	StateEdit edit = new StateEdit(
    				new VisibilityUndo(viewer.getONDEXJUNGGraph()), this.getName());
        	OVTK2Desktop desktop = OVTK2Desktop.getInstance();
    		desktop.setRunningProcess(this.getName());
    		
            // new instance of filter and set arguments
            Filter filter = new Filter();

            // construct filter arguments
            ONDEXPluginArguments fa = new ONDEXPluginArguments(filter.getArgumentDefinitions());
            Iterator<ConceptClass> it = ccs.iterator();
            while (it.hasNext()) {
                fa.addOption(Filter.TARGETCC_ARG, it.next().getId());
            }
            Iterator<RelationType> it2 = rts.iterator();
            while (it2.hasNext()) {
                fa.addOption(Filter.TARGETRT_ARG, it2.next().getId());
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
                    graph.setVisibility(c, false);
                }
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
