package net.sourceforge.ondex.ovtk2.io;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.dot.ArgumentNames;
import net.sourceforge.ondex.export.dot.Export;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Wrapper GUI for back-end DOT export
 *
 * @author taubertj
 */
public class DOTExport implements ActionListener, ArgumentNames, OVTK2IO {

    /**
     * Use trim function to shorten names
     */
    JCheckBox trimBox = new JCheckBox("Trim names to specified length?");

    /**
     * What length to trim to
     */
    JSpinner trimSpinner = null;

    /**
     * Default node size
     */
    JSpinner sizeSpinner = null;

    /**
     * The GUI component
     */
    JInternalFrame gui = null;

    /**
     * Parent ONDEXGraph
     */
    ONDEXGraph graph = null;

    /**
     * File to export to
     */
    File file = null;

    public DOTExport() {
    }

    /**
     * GUI for setting parameters.
     */
    private void initGUI() {

        // new internal frame with layout
        gui = new JInternalFrame("Export options");
        BoxLayout layout = new BoxLayout(gui.getContentPane(),
                BoxLayout.PAGE_AXIS);
        gui.getContentPane().setLayout(layout);
        gui.getContentPane().add(trimBox);

        // flexible trim length up to 100
        SpinnerModel trimModel = new SpinnerNumberModel(20, 0, 100, 1);
        trimSpinner = new JSpinner(trimModel);
        trimSpinner.setPreferredSize(new Dimension(75, 15));
        JPanel trimPanel = new JPanel(new BorderLayout());
        trimPanel.add(new JLabel("Trim length:"), BorderLayout.WEST);
        trimPanel.add(trimSpinner, BorderLayout.EAST);
        gui.getContentPane().add(trimPanel);

        // model for node size, maximum of 25 inches
        SpinnerModel sizeModel = new SpinnerNumberModel(1.75, 0.01, 25, 0.01);
        sizeSpinner = new JSpinner(sizeModel);
        sizeSpinner.setPreferredSize(new Dimension(75, 15));
        JPanel sizePanel = new JPanel(new BorderLayout());
        sizePanel.add(new JLabel("Node size:"), BorderLayout.WEST);
        sizePanel.add(sizeSpinner, BorderLayout.EAST);
        gui.getContentPane().add(sizePanel);

        // the important button
        JButton go = new JButton("Proceed");
        go.addActionListener(this);
        gui.getContentPane().add(go);

        gui.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gui.setVisible(false);
        try {
            // run export
            Export export = new Export();
            // construct arguments
            ONDEXPluginArguments args = new ONDEXPluginArguments(export.getArgumentDefinitions());
            args.setOption(FileArgumentDefinition.EXPORT_FILE, file.getAbsolutePath());
            args.addOption(TRIM_ARG, trimBox.isSelected());
            args.addOption(TRIMLENGTH_ARG, trimSpinner.getValue());
            args.addOption(NODESIZE_ARG, sizeSpinner.getValue());

            export.setArguments(args);
            export.setONDEXGraph(graph);

            export.start();
        } catch (Exception ex) {
            ErrorDialog.show(ex);
        }
    }

    @Override
    public void start(File file) {
        this.file = file;

        OVTK2Desktop desktop = OVTK2Desktop.getInstance();

        JOptionPane
                .showInternalMessageDialog(
                        desktop.getDesktopPane(),
                        "To export appearance of the graph, please make sure you saved appearance first.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);

        // add GUI to desktop
        JDesktopPane pane = desktop.getDesktopPane();
        initGUI();
        pane.add(gui);

        gui.setVisible(true);
        gui.toFront();
    }

    @Override
    public void setGraph(ONDEXGraph graph) {
        // set references
        this.graph = graph;
    }

    @Override
    public String getExt() {
        return "dot";
    }

    @Override
    public boolean isImport() {
        return false;
    }
}
