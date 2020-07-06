/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.ondex.ovtk2.filter.kpmfilter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.filter.tag.Filter;

import uk.ac.ncl.cs.nclondexexpression.KPMTransformer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.StateEdit;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
//import net.sourceforge.ondex.core.util.ONDEXBitSet;
import net.sourceforge.ondex.filter.tag.ArgumentNames;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
//import net.sourceforge.ondex.ovtk2.reusable_functions.Annotation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import uk.ac.ncl.cs.nclondexexpression.MAParser;
import uk.ac.ncl.cs.nclondexexpression.tools.DefaultPathTemplates;
/**
 *
 * @author Wojciech Musialkiewicz
 */
public class KPMFilter extends OVTK2Filter implements ActionListener {


    private JButton goButton = null;
    private JButton clearButton = null;
    private JButton showMoreButton= null;
    private JButton showLessButton = null;

    private boolean used = false;

    private JComboBox parserComboBox = null;

    private File dataFile = null;

    Map<String,JTextField> textFields;
    Map<String,JCheckBox> checkBoxes;
    Map<String,JComboBox> comboBoxes;
    
    Map<JTextField,JLabel> textLabels;
    Map<JCheckBox,JLabel> checkBoxLabels;
    Map<JComboBox,JLabel> comboBoxLabels;
    
    Map<JTextField, String> defaultValuesMap;
    
    Map<String,DataSource> dataMap;
    


    private KPMTransformer transformer = null;
    private ONDEXPluginArguments arguments = null;
    private ONDEXPluginArguments parserArguments = null;

    private MAParser parser = null;
    
    private DataSource[] dataSources;

    SwingWorker<Boolean, Void> worker;
    
    private Integer target;
    
    
    /*
     * list of parameters for the kpm algorithm that will be displayed in the main
     * window, all others will be hidden
     */
    List<String> mainParameters;

    MAInterface ma;

    public KPMFilter(OVTK2Viewer viewer)
    {
        super(viewer);
        
        mainParameters = new ArrayList<String>();
        
        //**************************************
        //Jochen Weile's MA Attacher
        //needs to be initialized already for the creation of components
        //in the kpm main window
        ma = new MaAttacher();
        dataMap = new HashMap<String,DataSource>();
        try{
            ma.setGraph(graph);
        }
        catch(Exception ex){
            ErrorDialog.show(ex);
        }
        //fill the array with datasources
        dataSources = ma.getGeneDataSources();
        for(DataSource ds : dataSources)
        {
            dataMap.put(ds.toString(), ds);
        }
        //end of MA attacher's initialization
        //*************************************
        
        String valid = "";
        valid = validateGraph();
  
        if( valid.equals("OK"))
        {
            initializeMainWindow();
            
        }
        else
        {
            int choice;
            choice = JOptionPane.showConfirmDialog(null, valid +
                    "\nDo you want to add custom data?", "KPM", JOptionPane.YES_NO_OPTION);
            if(choice == JOptionPane.YES_OPTION)
            {
                loadAdditionalData();
            }else
            {
                initializeEmptyWindow();
            }
        }
        

            
        
    }

    /*
     * loads additional data needed for the KPM algorithm, if it is not already
     * in the main graph.
     */
    private boolean loadAdditionalData()
    {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "*.tsv", "tsv");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            dataFile = chooser.getSelectedFile();

        }
        initializeDataSourceWindow();
        return true;
    }
        
        
    /*
     * creates the window for parameteres
     */
    private void initializeMainWindow()
    {
        //empty the list in case main window is initialized once again
        mainParameters.clear();
        //boxes for these parameters will be visible by default,all others will be hidden
        mainParameters.add("K");
        mainParameters.add("L");
        mainParameters.add("topKResults");
        mainParameters.add("threshold");
        mainParameters.add("Namespace");
        mainParameters.add("interactionType");
        

        setLayout(new SpringLayout());

        //create new KPMTransformer object
        transformer = new KPMTransformer();

        //create maps for components
        textFields = new HashMap<String, JTextField>();
        checkBoxes= new HashMap<String, JCheckBox>();
        comboBoxes = new HashMap<String, JComboBox>();
        
        textLabels = new HashMap<JTextField, JLabel>();
        checkBoxLabels = new HashMap<JCheckBox, JLabel>();
        comboBoxLabels = new HashMap<JComboBox, JLabel>();
        
        //create the map for relation between the textfields and their default values
        defaultValuesMap = new HashMap<JTextField,String>();

        //dynamic creation of components
         for (ArgumentDefinition<?> argdef : transformer.getArgumentDefinitions()) {
            if ((argdef.getClassType().equals(String.class) ||
                    argdef.getClassType().equals(Integer.class)||
                    argdef.getClassType().equals(Float.class)) &&
                    !argdef.getName().equals("interactionType")) {
                
                
                
                //create the label for the text field
                //fill it with appropriate description
                JLabel tempLabel = new JLabel(argdef.getDescription());
                JTextField tempText = null;

                //create the text field itself
                tempText = new JTextField(15);
                //set the label for it
                tempLabel.setLabelFor(tempText);
                textLabels.put(tempText, tempLabel);
                //add the textfield and it's description to the map
                textFields.put(argdef.getName(), tempText);
                
                //put the default value into the map
                if(argdef.getClassType().equals(Integer.class))
                {
                    defaultValuesMap.put(tempText, String.valueOf(argdef.getDefaultValue()));
                }else if(argdef.getClassType().equals(Float.class))
                {
                    defaultValuesMap.put(tempText, String.valueOf(argdef.getDefaultValue()));
                }else{
                    defaultValuesMap.put(tempText, (String)argdef.getDefaultValue());
                }
                
                //set the default text to the field
                tempText.setText(defaultValuesMap.get(tempText));

                //set text colour to grey for default text
                tempText.setForeground(Color.GRAY);
                
                //set the font to italic
                tempText.setFont(tempText.getFont().deriveFont(Font.ITALIC));
                
                
                //add listener to enable emptying fields on click
                tempText.addFocusListener(new FocusListener() {

                    @Override
                    public void focusGained(FocusEvent e) {
                        JTextField t = (JTextField)e.getSource();
                        if(t.getText().equals(defaultValuesMap.get(t)))
                        {
                            t.setText("");
                            t.setForeground(Color.BLACK);
                            t.setFont(t.getFont().deriveFont(Font.PLAIN));
                        }
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        JTextField t = (JTextField)e.getSource();
                        if(t.getText().equals(""))
                        {
                            t.setText(defaultValuesMap.get(t));
                            t.setForeground(Color.GRAY);
                            t.setFont(t.getFont().deriveFont(Font.ITALIC));
                        }
                    }
                });
                
                
                //set to invisible by default
                tempLabel.setVisible(false);
                tempText.setVisible(false);
                

            }else if(argdef.getClassType().equals(Boolean.class))
            {
                //type is boolean,so we want a checkbox

                //create label first
                JLabel tempLabel = new JLabel(argdef.getDescription());

                //then the checkbox
                JCheckBox tempCheck = new JCheckBox();
                //assign the label to the checkbox
                tempLabel.setLabelFor(tempCheck);

                //add the checkbox and it's description to the map
                checkBoxes.put(argdef.getName(), tempCheck);
                
                //add the checkbox's label to the map
                checkBoxLabels.put(tempCheck, tempLabel);
                
                //set to invisible by default
                tempLabel.setVisible(false);
                tempCheck.setVisible(false);
                
                
            }else if(argdef.getClassType().equals(DataSource.class))
            {
                //combo boxes for datasources
                JLabel tempLabel = new JLabel(argdef.getDescription());
                JComboBox tempComboBox = null;
                if(argdef.getName().equals("Namespace")){
                    tempComboBox = new JComboBox();
                    for(DataSource ds : dataSources)
                    {
                        tempComboBox.addItem(ds.getFullname());
                    }
                    tempLabel.setLabelFor(tempComboBox);
                    comboBoxLabels.put(tempComboBox, tempLabel);
                    comboBoxes.put(argdef.getName(), tempComboBox);
                    
                    //set to invisible by default
                    tempLabel.setVisible(false);
                    tempComboBox.setVisible(false);

                }
                
            }else if(argdef.getName().equals("interactionType"))
            {
                //combo box for interaction types
                JLabel tempLabel = new JLabel(argdef.getDescription());
                JComboBox tempComboBox = null;
                tempComboBox = new JComboBox();
                DefaultPathTemplates templates = null;
                
                //read the possible interaction types off the graph
                try{
                    templates = new DefaultPathTemplates(graph);
                }catch(Exception ex){
                    ErrorDialog.show(ex);
                }
                //add them to the comboBox
                for (String s : templates.getTemplateKeys()) {
                    tempComboBox.addItem(s);
                }
                tempLabel.setLabelFor(tempComboBox);
                comboBoxLabels.put(tempComboBox, tempLabel);
                comboBoxes.put(argdef.getName(), tempComboBox);
                
                //set to invisible by default
                tempLabel.setVisible(false);
                tempComboBox.setVisible(false);
                
            }
        }
         
        //make only the main components visible and add them to the window
        for (String s : mainParameters) {
            if (textFields.containsKey(s)) {
                textLabels.get(textFields.get(s)).setVisible(true);
                this.add(textLabels.get(textFields.get(s)));
                textFields.get(s).setVisible(true);
                this.add(textFields.get(s));
            } else if (comboBoxes.containsKey(s)) {
                comboBoxLabels.get(comboBoxes.get(s)).setVisible(true);
                this.add(comboBoxLabels.get(comboBoxes.get(s)));
                comboBoxes.get(s).setVisible(true);
                this.add(comboBoxes.get(s));
            } else if (checkBoxes.containsKey(s)) {
                checkBoxLabels.get(checkBoxes.get(s)).setVisible(true);
                this.add(checkBoxLabels.get(checkBoxes.get(s)));
                checkBoxes.get(s).setVisible(true);
                this.add(checkBoxes.get(s));
            }
        }
        
        showLessButton = new JButton("Show less parameters");
        showLessButton.setActionCommand("SHOW_LESS");
	showLessButton.addActionListener(this);
        showLessButton.setVisible(false);
        add(showLessButton);
        
        showMoreButton = new JButton("Show more parameters");
        showMoreButton.setActionCommand("SHOW_MORE");
	showMoreButton.addActionListener(this);
        add(showMoreButton);
        
	// button for the KPM filter
	goButton = new JButton("Apply KPM filter");
        goButton.setActionCommand("RUN_KPM");
	goButton.addActionListener(this);
        add(goButton);

        //clear button
        clearButton = new JButton("Clear");
        clearButton.setActionCommand("CLEAR");
        clearButton.addActionListener(this);
        add(clearButton);

        //all components will be arranged in a grid
        SpringUtilities.makeCompactGrid(this, //parent
                this.getComponentCount()/2, 2, //rows, columns
                5,  5,  //initx, inity
                5, 5); //padX, padY
    }

    /*
     * since it is impossible to close the JPanel from the constructor of
     * the KPMFilter class, because of the JInteralFrame coding, we have to
     * create an empty frame and instruct the user to close the window manualy.
     */
    private void initializeEmptyWindow()
    {
        setLayout(new SpringLayout());
        JLabel l = new JLabel("Please close this window");
        add(l);
        //all components will be arranged in a grid
        SpringUtilities.makeCompactGrid(this, //parent
                1, 1, //rows, columns
                5,  5,  //initx, inity
                5, 5); //padX, padY
    }


    /*
     * initializes the window just with datasource selection box
     */
    private void initializeDataSourceWindow()
    {
        
        


        setLayout(new SpringLayout());

        JLabel l = new JLabel("Please select the namespace by which genes are identified in the expression data file:");
        add(l);
        
        /*
         * hash map that binds the DataSource's name with the actual DataSource
         */
        

        parserComboBox = new JComboBox();
        for(DataSource ds : dataSources)
        {
            parserComboBox.addItem(ds.toString());
        }

        
        add(parserComboBox);

        JButton b = new JButton("Proceed");
        b.setActionCommand("LOAD");
        b.addActionListener(this);
        add(b);

        //all components will be arranged in a grid
        SpringUtilities.makeCompactGrid(this, //parent
                3, 1, //rows, columns
                5,  5,  //initx, inity
                5, 5); //padX, padY
    }

    /*
     * This window should be displayed while the kpm algorithm is running
     */
    private void initializeWaitWindow()
    {



        setLayout(new SpringLayout());
        JLabel l = new JLabel("Waiting for the KPM algorithm to finish.");
        add(l);

        JProgressBar progressBar = new JProgressBar(0,100);
        progressBar.setIndeterminate(true);              
        add(progressBar);


        /*
         * cannot interrupt running KPM plugin.
         * The algorithm runs in a separate thread, but SwingWorker can't kill it.
         * It can only set an interrupt flag to true, but since the plugin itself
         * doesn't check for interruptions, it won't stop. Therefore, no cancel button.

        JButton b = new JButton("Cancel");
        b.setActionCommand("STOP_KPM");
        b.addActionListener(this);
        add(b);
        */


        //all components will be arranged in a grid
        SpringUtilities.makeCompactGrid(this, //parent
                2, 1, //rows, columns
                5,  5,  //initx, inity
                5, 5); //padX, padY
    }
    
    private void initializeTagSelectionWindow()
    {
        setLayout(new SpringLayout());
        
        //get the concept path        
        ConceptClass ccPath = graph.getMetaData().getConceptClass("Path");
        
        //button that invokes the actual filtering
        final JButton filterButton = new JButton("Filter");
        filterButton.setActionCommand("FILTER");
        filterButton.addActionListener(this);
        
        JLabel l = new JLabel("Please select a tag to display");
        add(l);
        
        
        //list of tags(which are integers)
        List<Integer> tags = new ArrayList<Integer>();
        
        //fill the list with tags
        for(ONDEXConcept c: graph.getAllTags())
        {
            if(c.getOfType().equals(ccPath))
            {
                tags.add(c.getId());
            }
        }
        
        //sort the list
        Collections.sort(tags);
        
        //create the JList component filled with tags
        JList list = new JList(tags.toArray());
        //user can select only one tag at once
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addListSelectionListener( new ListSelectionListener() {
            @Override
             public void valueChanged(ListSelectionEvent e) {
                if(((JList)e.getSource()).getSelectedIndex()==-1)
                {
                    filterButton.setEnabled(false);
                }else {
                    target = (Integer)((JList)e.getSource()).getSelectedValue();
                    filterButton.setEnabled(true);
                }
            }
            
        });
        
        add(list);
        
        add(filterButton);
        
        //all components will be arranged in a grid
        SpringUtilities.makeCompactGrid(this, //parent
                this.getComponentCount(), 1, //rows, columns
                5,  5,  //initx, inity
                5, 5); //padX, padY
        
    }


    /*
     * checks if the graph is compatible with the KPM algorithm
     */
    private String validateGraph()
    {
        ConceptClass ccGene = graph.getMetaData().getConceptClass("Gene");
        AttributeName anExpMap = graph.getMetaData().getAttributeName("EXPMAP");

        if (ccGene == null) {
            return "No genes data! Graph not compatible with KPM algorithm!";
        }
        if (anExpMap == null) {
            return "No expmap data! Graph not compatible with KPM algorithm!";
        }

        int numberOfGenesWithExpression=0;

        for (ONDEXConcept c : graph.getConceptsOfConceptClass(ccGene)) {
            if (c.getAttribute(anExpMap) != null) {
                numberOfGenesWithExpression++;
            }
        }
        
        if (numberOfGenesWithExpression == 0) {
            return "No genes with expmap data! Graph not compatible with KPM algorithm!";
        }
        
        //less than 20% genes with expression data
        if ((numberOfGenesWithExpression * 100 / graph.getConceptsOfConceptClass(ccGene).size()) < 20) {
            int choice;
            choice = JOptionPane.showConfirmDialog(this,"Less than 20% of genes carry expression data,"
                    + "\nDo you want to continue?","KPM", JOptionPane.YES_NO_OPTION);
            if(choice == JOptionPane.YES_OPTION)
            {
                return "OK";
            }else
            {
                return "The amount of genes carrying expression data is less than 20%. ";
            }
        }

//        our testing data is missing some genes
//        so as long as it has any ,the algorithm will run
        return "OK";

        
        
    }

    private void startKPM()
    {
        //start the KPM algorithm
            try{
                transformer.start();
            }
            catch(Exception ex)
            {
                ErrorDialog.show(ex);
                
            }
    }


    /**
    * Returns the name of this filter.
    *
    * @return name of filter
    */
    @Override
    public String getName(){
        return "Kpm filter";
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("RUN_KPM"))
        {
            

            //pass the graph
            transformer.setONDEXGraph(graph);

            //create new object to hold all the arguments needed for the KPM algorithm
            arguments = new ONDEXPluginArguments(transformer.getArgumentDefinitions());
                //parse arguments and add them to the list
               try {
                    int x=0;
                    for (ArgumentDefinition<?> argdef : transformer.getArgumentDefinitions()) {
                        x++;
                        if (argdef.getClassType().equals(String.class)) {
                            
                            if(!argdef.getName().equals("interactionType"))
                            {
                                JTextField t = textFields.get(argdef.getName());
                                if (t.getText() == null || t.getText().length() == 0) {
                                    arguments.addOption(argdef.getName(), argdef.getDefaultValue());
                                } else {
                                    arguments.addOption(argdef.getName(), t.getText());
                                }
                            }else {
                                JComboBox cb = comboBoxes.get(argdef.getName());
                                arguments.addOption(argdef.getName(), cb.getSelectedItem().toString());
                            }

                        } else if (argdef.getClassType().equals(Integer.class)) {

                            JTextField t = textFields.get(argdef.getName());
                            if (t.getText() == null || t.getText().length() == 0) {
                                arguments.addOption(argdef.getName(), argdef.getDefaultValue());
                            } else {
                                arguments.addOption(argdef.getName(), Integer.parseInt(t.getText()));
                            }

                        } else if (argdef.getClassType().equals(Float.class)) {

                            JTextField t = textFields.get(argdef.getName());
                            if (t.getText() == null || t.getText().length() == 0) {
                                arguments.addOption(argdef.getName(), argdef.getDefaultValue());
                            } else {
                                arguments.addOption(argdef.getName(), Float.parseFloat(t.getText()));
                            }

                        } else if (argdef.getClassType().equals(Boolean.class)) {

                            arguments.addOption(argdef.getName(), checkBoxes.get(argdef.getName()).isSelected());

                        } else if (argdef.getClassType().equals(DataSource.class)) {

                            JComboBox cb = comboBoxes.get(argdef.getName());
                           
                            arguments.addOption(argdef.getName(), dataMap.get(cb.getSelectedItem().toString()));
                            

                        }
                        
                    }

                //pass the arguments to the parser
                transformer.setArguments(arguments);
            }
            catch(Exception ex)
            {
                ErrorDialog.show(ex);
                
            }
            
            //start KPM algorithm in a seperate thread
            
            final JPanel thisPanel = this;
            final JInternalFrame parentFrame = (JInternalFrame)this.getParent().getParent().getParent();
            worker = new SwingWorker<Boolean, Void>() {
                @Override
               public Boolean doInBackground() {
                    startKPM();
                    return true;
                }
                @Override
                public void done()
                {
                    thisPanel.removeAll();
                    initializeTagSelectionWindow();
                    parentFrame.pack();
                    used = true;
                }
            };
            worker.execute();


            //remove whatever was in the window
            this.removeAll();
            //show a wait window to the user
            initializeWaitWindow();
            //resize the window to fit all elements
            ((JInternalFrame) this.getParent().getParent().getParent()).pack();
            
        }
        else if(e.getActionCommand().equals("CLEAR"))
        {
            //clear all fields
            for(JTextField t : textFields.values())
            {
                //set textfields back to their default values
                t.setText(defaultValuesMap.get(t));
                t.setForeground(Color.GRAY);
                t.setFont(t.getFont().deriveFont(Font.ITALIC));
            }
            for(JCheckBox c : checkBoxes.values())
            {
                c.setSelected(false);
            }
            //set selection to the first element
            for(JComboBox cb : comboBoxes.values())
            {
                cb.setSelectedIndex(0);
            }
        }
        else if(e.getActionCommand().equals("SHOW_MORE")){
            
            //remove all components to rebuild window from scratch
            this.removeAll();
            
            //make the main components visible first:
            //make only the main ones visible
            for (String s : mainParameters) {
                if (textFields.containsKey(s)) {
                    textLabels.get(textFields.get(s)).setVisible(true);
                    this.add(textLabels.get(textFields.get(s)));
                    textFields.get(s).setVisible(true);
                    this.add(textFields.get(s));
                } else if (comboBoxes.containsKey(s)) {
                    comboBoxLabels.get(comboBoxes.get(s)).setVisible(true);
                    this.add(comboBoxLabels.get(comboBoxes.get(s)));
                    comboBoxes.get(s).setVisible(true);
                    this.add(comboBoxes.get(s));
                } else if (checkBoxes.containsKey(s)) {
                    checkBoxLabels.get(checkBoxes.get(s)).setVisible(true);
                    this.add(checkBoxLabels.get(checkBoxes.get(s)));
                    checkBoxes.get(s).setVisible(true);
                    this.add(checkBoxes.get(s));
                }
            }
            
            //then all the others
            for(JTextField t : textFields.values())
            {
                //add only if they weren't already added
                if(!t.isVisible())
                {
                    t.setVisible(true);
                    textLabels.get(t).setVisible(true);
                    this.add(textLabels.get(t));
                    this.add(t);
                }
            }
            for(JComboBox cb : comboBoxes.values())
            {
                if(!cb.isVisible())
                {
                    cb.setVisible(true);
                    comboBoxLabels.get(cb).setVisible(true);
                    this.add(comboBoxLabels.get(cb));
                    this.add(cb);
                }
            }
            for(JCheckBox cx : checkBoxes.values())
            {
                if(!cx.isVisible())
                {
                    cx.setVisible(true);
                    checkBoxLabels.get(cx).setVisible(true);
                    this.add(checkBoxLabels.get(cx));
                    this.add(cx);
                }
            }
            
            //add the control buttons
            showMoreButton.setVisible(false);
            this.add(showMoreButton);
            showLessButton.setVisible(true);
            this.add(showLessButton);
            this.add(goButton);
            this.add(clearButton);
            
            //rebuild the grid for the new number of components
            SpringUtilities.makeCompactGrid(this, //parent
                    this.getComponentCount() / 2, 2, //rows, columns
                    5, 5, //initx, inity
                    5, 5); //padX, padY
            
            //resize the window to fit all elements
            ((JInternalFrame) this.getParent().getParent().getParent()).pack();
            
            
        }
            else if(e.getActionCommand().equals("SHOW_LESS")){
                
            //make all components invisible 
            Component components[] = this.getComponents();
            for(Component c : components)
            {
                c.setVisible(false);
            }
            
            //remove them from the window
            this.removeAll();
            
            //make only the main ones visible
            for(String s : mainParameters)
            {
                if(textFields.containsKey(s))
                {
                    textLabels.get(textFields.get(s)).setVisible(true);
                    this.add(textLabels.get(textFields.get(s)));
                    textFields.get(s).setVisible(true);
                    this.add(textFields.get(s));
                }else if(comboBoxes.containsKey(s))
                {
                    comboBoxLabels.get(comboBoxes.get(s)).setVisible(true);
                    this.add(comboBoxLabels.get(comboBoxes.get(s)));
                    comboBoxes.get(s).setVisible(true);
                    this.add(comboBoxes.get(s));
                }
                else if(checkBoxes.containsKey(s))
                {
                    checkBoxLabels.get(checkBoxes.get(s)).setVisible(true);
                    this.add(checkBoxLabels.get(checkBoxes.get(s)));
                    checkBoxes.get(s).setVisible(true);
                    this.add(checkBoxes.get(s));
                }
                
                    
            }
            
            //add control buttons
            showLessButton.setVisible(false);
            this.add(showLessButton);
            showMoreButton.setVisible(true);
            this.add(showMoreButton);
            goButton.setVisible(true);
            this.add(goButton);
            clearButton.setVisible(true);
            this.add(clearButton);
            
            //rebuild the grid for the new number of components
            SpringUtilities.makeCompactGrid(this, //parent
                    this.getComponentCount() / 2, 2, //rows, columns
                    5, 5, //initx, inity
                    5, 5); //padX, padY
            
            //resize the window to fit all elements
            ((JInternalFrame) this.getParent().getParent().getParent()).pack();
            
            
        }
        else if(e.getActionCommand().equals("LOAD"))
        {

            try
            {
                //parse the file using Jochen Weile's MA file parser
                ma.parseMaFile(dataFile, dataMap.get(parserComboBox.getSelectedItem()));
                //print out all the warnings
               System.out.println(ma.getWarnings());
            }catch(Exception ex)
            {
                ErrorDialog.show(ex);
            }
            
            //remove whatever was in the window
            this.removeAll();
            //show the main KPM window
            initializeMainWindow();
            //resize the window to fit all elements
            ((JInternalFrame) this.getParent().getParent().getParent()).pack();
        }
        else if(e.getActionCommand().equals("FILTER"))
        {
            StateEdit edit = new StateEdit(new VisibilityUndo(viewer.getONDEXJUNGGraph()), this.getName());
            OVTK2Desktop desktop = OVTK2Desktop.getInstance();
            desktop.setRunningProcess(this.getName());
            
            Set<ONDEXConcept> concepts = null;
            Set<ONDEXRelation> relations = null;

            // new instance of filter and set arguments
            Filter filter = new Filter();
            //IntegerStringWrapper context = (IntegerStringWrapper) (Object)target;
            ONDEXConcept concept = graph.getConcept(target);
            
            // construct filter arguments
            ONDEXPluginArguments fa = new ONDEXPluginArguments(filter.getArgumentDefinitions());
            try{
                fa.addOption(ArgumentNames.TAG_ARG, concept.getId());
                filter.addONDEXListener(new ONDEXLogger());
                filter.setONDEXGraph(graph);
                filter.setArguments(fa);
                filter.start();
                concepts = BitSetFunctions.copy(filter.getVisibleConcepts());
                relations = BitSetFunctions.copy(filter.getVisibleRelations());

            }catch(Exception ex)
            {
                ErrorDialog.show(ex);
            }
            
            if (concepts != null) {

                // set first entire graph to invisible
                setGraphVisible(false);

                // set only graph returned by filter to visible
                setGraphVisible(concepts, relations);

                Map<Integer,Boolean> vis = graph.getVertices_visibility();
//                ONDEXBitSet bs = Annotation.getVisibleConcpets(viewer);
                for (ONDEXRelation r : graph.getRelations()) {
                    if (vis.get(r.getToConcept().getId())
                            && vis.get(r.getFromConcept().getId())) {
//                        Annotation.setVisibility(viewer, r, true);
                        graph.setVisibility(r, true);
                    }
                }

                // propagate change to viewer
                viewer.getVisualizationViewer().getModel().fireStateChanged();
            }
            
            edit.end();
            viewer.undoManager.addEdit(edit);
            desktop.getOVTK2Menu().updateUndoRedo(viewer);
            desktop.notifyTerminationOfProcess();
        }

    }

    /**
     * set visibility for entire graph
     * 
     * @param isVisible
     *            if true, entire graph is visible
     */
    private void setGraphVisible(boolean isVisible) {

        // make all nodes visible
        for (ONDEXConcept ac : graph.getConcepts()) {
            graph.setVisibility(ac, isVisible);
        }

        // make all edges visible
        for (ONDEXRelation ar : graph.getRelations()) {
            graph.setVisibility(ar, isVisible);
        }
    }

    /**
     * set visibility of graph using a filter
     * 
     * @param viewC
     * @param viewR
     */
    private void setGraphVisible(Set<ONDEXConcept> viewC,
            Set<ONDEXRelation> viewR) {

        // show concepts of filter
        for (ONDEXConcept ac : viewC) {
            graph.setVisibility(ac, true);
        }

        // show relations of filter
        for (ONDEXRelation ar : viewR) {
            graph.setVisibility(ar, true);
        }
    }

    @Override
    public boolean hasBeenUsed() {
        return used;
    }
}


