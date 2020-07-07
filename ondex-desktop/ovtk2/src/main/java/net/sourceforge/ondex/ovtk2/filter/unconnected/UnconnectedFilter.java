package net.sourceforge.ondex.ovtk2.filter.unconnected;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;

public class UnconnectedFilter extends OVTK2Filter implements ActionListener {

	// ####FIELDS####
	/**
     *
     */
	private static final long serialVersionUID = 1617354150821387836L;
	private static final String GO = "GO";

	private JCheckBox onVisibleOnly;

	// change visibility
	private boolean visibility = false;

	/**
	 * Filter has been used
	 */
	private boolean used = false;

	// ####CONSTRUCTOR####

	public UnconnectedFilter(OVTK2Viewer viewer) {
		super(viewer);

		setLayout(new SpringLayout());

		JLabel infoLabel = new JLabel("Remove all unconnected nodes.");
		add(infoLabel);

		// The magic button
		JButton goButton = new JButton("Filter Graph");
		goButton.setActionCommand(GO);
		goButton.addActionListener(this);

		onVisibleOnly = new JCheckBox("Apply unconnected test to visible graph");
		onVisibleOnly.setSelected(true);

		JRadioButton yesButton = new JRadioButton("true", false);
		yesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				visibility = true;
			}
		});
		JRadioButton noButton = new JRadioButton("false", true);
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
		radioPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Change visibility to:"));
		add(radioPanel);

		add(goButton);
		add(onVisibleOnly);
		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5, 5, 5, 5);
	}

	// ####METHODS####

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.Unconnected");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(GO)) {
			try {
				callFilter();
			} catch (InvalidPluginArgumentException e1) {
				ErrorDialog.show(e1);
			}
			used = true;
		}
	}

	private void callFilter() throws InvalidPluginArgumentException {

		StateEdit edit = new StateEdit(new VisibilityUndo(viewer.getONDEXJUNGGraph()), this.getName());
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		desktop.setRunningProcess(this.getName());

		Set<ONDEXConcept> concepts = new HashSet<ONDEXConcept>(graph.getConcepts());

		if (onVisibleOnly.isSelected() && !visibility) {
			System.out.println("Calculating visible concepts");
			concepts = new HashSet<ONDEXConcept>(graph.getVertices());
		}

		System.out.println("Filtering graph");

		// set visibility of concepts
		for (ONDEXConcept c : concepts) {
			if (graph.getRelationsOfConcept(c).size() == 0)
				graph.setVisibility(c, visibility);

			// check for applying on visible relations
			else if (onVisibleOnly.isSelected()) {
				boolean anyVisible = false;
				for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
					anyVisible = anyVisible || graph.isVisible(r);
					if (anyVisible)
						break;
				}
				if (!anyVisible) {
					graph.setVisibility(c, visibility);
				}
			}
		}

		// propagate change to viewer
		viewer.getVisualizationViewer().getModel().fireStateChanged();

		edit.end();
		viewer.getUndoManager().addEdit(edit);
		desktop.getOVTK2Menu().updateUndoRedo(viewer);
		desktop.notifyTerminationOfProcess();
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
