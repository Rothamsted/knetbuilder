/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.ondex.ovtk2.filter.collapser;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;


import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.StateEdit;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import uk.ac.ncl.cs.nclondexexpression.tools.DefaultPathTemplates;
import uk.ac.ncl.cs.ondex.semantics.SemanticCollapser;

/**
 *
 * @author Wojciech Musialkiewicz
 */
public class Collapser extends OVTK2Filter implements ActionListener, MouseListener{
    
    private JList list = null;
    private DefaultPathTemplates templates = null;
    private JButton expand = null;

    private boolean used = false;
    
    public Collapser(OVTK2Viewer viewer)
    {
        super(viewer);
        initializeMainWindow();
    }

    @Override
    public String getName() {
        return "Collapser plugin";
    }

    

    private void initializeMainWindow() {
        
        setLayout(new SpringLayout());
        
        JLabel l = new JLabel("Please select a motif type to collapse");
        add(l);      
        
        //crete the variable for collapse button
        final JButton collapse = new JButton("Collapse");
        collapse.setActionCommand("COLLAPSE");
        collapse.addActionListener(this);
        collapse.setEnabled(false);
        
        //read the templates off the graph
        try {
            templates = new DefaultPathTemplates(graph);
        } catch (Exception ex) {
            ErrorDialog.show(ex);
        }
        
        //create a new list of templates
        list = new JList(templates.getTemplateKeys().toArray());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addListSelectionListener(new ListSelectionListener() {
            
            //make the collapse button enabled/disabled depending whatever something is selected in the list
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (((JList) e.getSource()).getSelectedIndex() == -1) {
                    collapse.setEnabled(false);
                } else {
                    collapse.setEnabled(true);
                }
            }
        });
        add(list);
        
        //add the collapse button
        add(collapse);
        
        //add the expand button
        expand = new JButton("Expand selected edge");
        expand.setActionCommand("EXPAND");
        expand.addActionListener(this);
        expand.setEnabled(false);
        add(expand);
        
        //add the mouse listener to change the state of expand button
        viewer.getVisualizationViewer().addMouseListener(this);
        
        //all components will be arranged in a grid
        SpringUtilities.makeCompactGrid(this, //parent
                this.getComponentCount() , 1, //rows, columns
                5, 5, //initx, inity
                5, 5); //padX, padY
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("COLLAPSE"))
        {
            //get the template's name
            String selection = list.getSelectedValue().toString();
            //create a name for the new relation
            String relationName = selection + "_r";
            
            RelationType rtR = graph.getMetaData().getRelationType("r");
            //create a new relation that will be used for collapsing 
            RelationType selectedRelation = graph.getMetaData().getFactory().createRelationType(relationName, relationName, "relation between two genes", rtR);
            
            SemanticCollapser sC = null;
            sC = new SemanticCollapser();

            ONDEXPluginArguments args = new ONDEXPluginArguments(sC.getArgumentDefinitions());
            try{
                
                //pass the template's motif(string with overlapping concepts and relations)
                args.addOption("Motif", templates.getTemplate(selection).getMotif());
                //pass the name of the newly created relation
                args.addOption("RelationType", relationName);
                args.addOption("AnnotationMode", true);
                
                sC.setArguments(args);
                sC.setONDEXGraph(graph);
                
                //start the semantic collapser
                sC.start();
            }catch(Exception ex)
            {                    
                ErrorDialog.show(ex);
            }
            

            
            StateEdit edit = new StateEdit(new VisibilityUndo(viewer.getONDEXJUNGGraph()), this.getName());
            OVTK2Desktop desktop = OVTK2Desktop.getInstance();
            desktop.setRunningProcess(this.getName());

            //get the first(and last) concept, these are the ones that will stay visible
            ConceptClass startCC = templates.getTemplate(selection).getCcs()[0];
            
            //attribute common to the collapsed edges
            AttributeName anPath = graph.getMetaData().getAttributeName("collapsedPath");
            
            //go through all relations
            for (ONDEXRelation r : graph.getRelations()) {
                if (r.inheritedFrom(selectedRelation) && r.getAttribute(anPath) != null) {
                    
                    //make the new edge visible
                    graph.setVisibility(r, true);
                    
                    Set<ONDEXEntity> set = (Set<ONDEXEntity>) r.getAttribute(anPath).getValue();
                    for (ONDEXEntity oe : set) {
                        //make the collapsed concepts and relations invisible
                        if (oe instanceof ONDEXConcept && 
                                !((ONDEXConcept)oe).getOfType().equals(startCC)) {
                            graph.setVisibility((ONDEXConcept)oe, false);
                        } else if (oe instanceof ONDEXRelation) {
                            graph.setVisibility((ONDEXRelation)oe, false);
                            
                        }
                    }
                }
            }
            
            // propagate change to viewer
            viewer.getVisualizationViewer().getModel().fireStateChanged();
            
            edit.end();
            viewer.undoManager.addEdit(edit);
            desktop.getOVTK2Menu().updateUndoRedo(viewer);
            desktop.notifyTerminationOfProcess();

            used = true;
        }
        else if(e.getActionCommand().equals("EXPAND"))
        {
            //code for expanding a selected edge
            StateEdit edit = new StateEdit(new VisibilityUndo(viewer.getONDEXJUNGGraph()), this.getName());
            OVTK2Desktop desktop = OVTK2Desktop.getInstance();
            desktop.setRunningProcess(this.getName());
            
            //attrribute common to collapsed edges
            AttributeName anPath = graph.getMetaData().getAttributeName("collapsedPath");
            
            //look through selected edges
            for(ONDEXRelation r : viewer.getPickedEdges())
            {
                
                if(r.getAttribute(anPath)!=null)
                {
                    //set the collapsed edge to invisible
                    graph.setVisibility(r, false);
                    
                    Set<ONDEXEntity> set = (Set<ONDEXEntity>) r.getAttribute(anPath).getValue();
                    for (ONDEXEntity oe : set) {
                        //make the collapsed concepts and relations visible
                        if (oe instanceof ONDEXConcept) {
                            graph.setVisibility((ONDEXConcept) oe, true);
                        } else if (oe instanceof ONDEXRelation) {
                            graph.setVisibility((ONDEXRelation) oe, true);
                        }
                    }
                }
                
            }
            
            // propagate change to viewer
            viewer.getVisualizationViewer().getModel().fireStateChanged();
            edit.end();
            viewer.undoManager.addEdit(edit);
            desktop.getOVTK2Menu().updateUndoRedo(viewer);
            desktop.notifyTerminationOfProcess();

            
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        
        /*
         * checks what has the user selected
         * if one of the collapsed edges, then it enables the expand button
         * it anything else(or nothing at all) it disables the button
         */
        if (viewer.getPickedEdges() != null) {

                    boolean valid = false;
                    AttributeName anPath = graph.getMetaData().getAttributeName("collapsedPath");
                    for (ONDEXRelation r : viewer.getPickedEdges()) {
                        if (r.getAttribute(anPath) != null) {
                            valid = true;
                        }
                    }

                    if (valid) {
                        expand.setEnabled(true);
                    } else {
                        expand.setEnabled(false);
                    }
                }
        
       
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {

    }

    @Override
    public void mouseEntered(MouseEvent me) {

    }

    @Override
    public void mouseExited(MouseEvent me) {

    }

    @Override
    public boolean hasBeenUsed() {
        return used;
    }
}
