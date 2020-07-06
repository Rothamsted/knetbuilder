package net.sourceforge.ondex.ovtk2.ui.console;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * 
 * @author lysenkoa
 * 
 */
public class ColouringWizard extends JInternalFrame implements ActionListener {

	private static final long serialVersionUID = 3092090115854724297L;

	private final JDesktopPane desktop;

	private JList choices = new JList();

	private JList configs = new JList();

	private static ColouringWizard instance = null;

	public static void show(JDesktopPane desktop) {
		if (instance != null) {
			instance.setVisible(true);
			instance.toFront();
		} else {
			instance = new ColouringWizard(desktop);
			instance.init();
		}

	}

	private ColouringWizard(JDesktopPane desktop) {
		super("Graph Colouring Wizard");
		this.desktop = desktop;
	}

	public void init() {
		this.setLayout(new GridBagLayout());
		this.setSize(600, 700);

		GridBagConstraints listCons = new GridBagConstraints();
		GridBagConstraints btnCons = new GridBagConstraints();
		GridBagConstraints panelCons = new GridBagConstraints();

		listCons.weightx = 0.25;
		listCons.weighty = 1;
		listCons.gridheight = 2;
		listCons.gridx = 0;
		listCons.gridy = 0;
		listCons.insets = new Insets(5, 5, 0, 5);
		listCons.fill = GridBagConstraints.BOTH;
		choices.setLayoutOrientation(JList.VERTICAL);
		choices.setVisibleRowCount(20);
		JScrollPane listScroller = new JScrollPane(choices);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScroller.setPreferredSize(new Dimension(25, 25));
		listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.add(listScroller, listCons);

		btnCons.weightx = 0.05;
		btnCons.weighty = 0.5;
		btnCons.gridx = 1;
		btnCons.gridy = 0;
		btnCons.insets = new Insets(0, 0, 5, 0);
		btnCons.anchor = GridBagConstraints.SOUTH;
		this.add(stdButton(">>", "in"), btnCons);
		btnCons.gridx = 1;
		btnCons.gridy = 1;
		btnCons.anchor = GridBagConstraints.NORTH;
		this.add(stdButton("<<", "out"), btnCons);

		listCons.gridx = 2;
		listCons.gridy = 0;
		listCons.weightx = 0.7;
		choices.setLayoutOrientation(JList.VERTICAL);
		configs.setVisibleRowCount(20);
		JScrollPane listScroller1 = new JScrollPane(configs);
		listScroller1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScroller1.setPreferredSize(new Dimension(40, 25));
		listScroller1.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.add(listScroller1, listCons);

		panelCons.anchor = GridBagConstraints.SOUTH;
		panelCons.gridwidth = 3;
		panelCons.gridx = 0;
		panelCons.gridy = 3;
		JPanel controlBtns = new JPanel();
		JButton b1 = new JButton("Apply");
		b1.addActionListener(this);
		controlBtns.add(b1);
		JButton b2 = new JButton("Clear");
		b2.addActionListener(this);
		controlBtns.add(b2);
		JButton b3 = new JButton("Save");
		b3.addActionListener(this);
		controlBtns.add(b3);
		JButton b4 = new JButton("Load");
		b4.addActionListener(this);
		controlBtns.add(b4);
		JButton b5 = new JButton("Close");
		b5.addActionListener(this);
		controlBtns.add(b5);
		this.add(controlBtns, panelCons);
		this.setVisible(true);
		desktop.add(this);
		this.toFront();
	}

	private JButton stdButton(String title, String command) {
		JButton Button2 = new JButton(title);
		Button2.setSize(15, 15);
		Button2.setActionCommand(command);
		Button2.addActionListener(this);
		return Button2;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("Close")) {
			saveAndExit();
		} else if (command.equals("Apply")) {

		} else if (command.equals("Clear")) {

		} else if (command.equals("Save")) {

		} else if (command.equals("Load")) {

		} else if (command.equals("in")) {

		} else if (command.equals("out")) {

		}

	}

	public void saveAndExit() {
		instance = null;
		this.dispose();
	}
}
