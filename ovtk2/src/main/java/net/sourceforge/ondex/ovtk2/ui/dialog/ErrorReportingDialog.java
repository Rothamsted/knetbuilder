package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.IssueType;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.Priority;

/**
 * Send error reports to JIRA
 * 
 * @author taubertj
 * 
 */
public class ErrorReportingDialog extends JInternalFrame implements ActionListener {

	/**
	 * action command cancel button
	 */
	private static final String CANCEL = "cancel";

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * action command submit button
	 */
	private static final String SUBMIT = "submit";

	/**
	 * a longer description of the problem
	 */
	private JTextArea description;

	/**
	 * JIRA connector service
	 */
	private Jira service;

	/**
	 * text input fields summary and email
	 */
	private JTextField summary, email;

	/**
	 * selection boxes for issue type and priority
	 */
	private JComboBox type, priority;

	/**
	 * Setup dialog
	 */
	public ErrorReportingDialog() {
		super(Config.language.getProperty("Dialog.Error.Title"), true, true, true, true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			initConnection();
			initGUI();
		} catch (Exception e) {
			ErrorDialog.show(e);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		// close window
		if (cmd.equals(CANCEL)) {
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e1) {
				ErrorDialog.show(e1);
			}
		}

		// first validate, then send
		else if (cmd.equals(SUBMIT)) {
			if (validateInput()) {
				try {
					submitIssue();
				} catch (Exception e1) {
					e1.printStackTrace();
					ErrorDialog.show(e1);
				}
			}
		}
	}

	/**
	 * Collects general information about the user system.
	 * 
	 * @return formated String
	 */
	private String collectSysInfo() {
		StringBuilder b = new StringBuilder("=== System information ===\n");

		// get build information
		String build = DesktopUtils.extractBuildNumber();
		if (build == null) {
			build = "N/A";
		}
		b.append("Build: ");
		b.append(build);
		b.append("\n");

		// get system information
		String arch = System.getProperty("os.arch");
		String osname = System.getProperty("os.name");
		String osversion = System.getProperty("os.version");
		b.append("OS: " + arch + " " + osname + " v" + osversion + "\n");

		// get memory in MB
		long maxMem = Runtime.getRuntime().maxMemory() / (1024 * 1024);
		long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
		b.append("RAM: current=" + totalMem + "MB unclaimed=" + freeMem + "MB max=" + maxMem + "MB\n");

		// get file system in MB
		File file = new File(Config.ovtkDir);
		long totalSpace = file.getTotalSpace() / (1024 * 1024);
		long freeSpace = file.getFreeSpace() / (1024 * 1024);
		b.append("HDD: total=" + totalSpace + "MB free=" + freeSpace + "MB\n");
		b.append("\n=== Problem description ===\n");

		return b.toString();
	}

	/**
	 * Open JIRA connection
	 * 
	 * @throws Exception
	 */
	private void initConnection() throws Exception {
		service = new Jira(Config.config.getProperty("JIRA.URL"));
		service.login(Config.config.getProperty("JIRA.User"), Config.config.getProperty("JIRA.Password"));
	}

	/**
	 * Build the GUI.
	 * 
	 */
	private void initGUI() {

		this.setSize(600, 500);

		// this is the root panel
		JPanel panel = new JPanel();
		this.add(panel);

		// start with a simple layout
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		// descriptive text for user
		JLabel header = new JLabel(Config.language.getProperty("Dialog.Error.Text"));
		panel.add(header);

		// ask user for a summary
		JLabel summaryLabel = new JLabel(Config.language.getProperty("Dialog.Error.Summary"));
		summary = new JTextField();
		panel.add(summaryLabel);
		panel.add(summary);

		// ask user for a description
		JLabel descriptionLabel = new JLabel(Config.language.getProperty("Dialog.Error.Description"));
		description = new JTextArea();
		description.setText(collectSysInfo());
		JScrollPane scroll = new JScrollPane(description);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(descriptionLabel);
		panel.add(scroll);

		// ask for type and priority of issue

		// compose type selection
		JLabel typeLabel = new JLabel(Config.language.getProperty("Dialog.Error.Type"));
		type = new JComboBox();
		List<IssueType> types = service.getIssueTypes();
		for (IssueType t : types) {
			type.addItem(t);
		}
		type.setSelectedIndex(0);
		panel.add(typeLabel);
		panel.add(type);

		// compose priority selection
		JLabel priorityLabel = new JLabel(Config.language.getProperty("Dialog.Error.Priority"));
		priority = new JComboBox();
		List<Priority> priorities = service.getPriorities();
		for (Priority p : priorities) {
			priority.addItem(p);
		}
		priority.setSelectedIndex(3);
		panel.add(priorityLabel);
		panel.add(priority);

		// user email
		JLabel emailLabel = new JLabel(Config.language.getProperty("Dialog.Error.Email"));
		email = new JTextField();
		panel.add(emailLabel);
		panel.add(email);

		// submit and cancel button
		JButton submit = new JButton(Config.language.getProperty("Dialog.Error.Submit"));
		submit.setActionCommand(SUBMIT);
		submit.addActionListener(this);
		panel.add(submit);
		JButton cancel = new JButton(Config.language.getProperty("Dialog.Error.Cancel"));
		cancel.setActionCommand(CANCEL);
		cancel.addActionListener(this);
		panel.add(cancel);

		// layout components here
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(header).addComponent(summaryLabel).addComponent(summary).addComponent(descriptionLabel).addComponent(scroll).addGroup(layout.createSequentialGroup().addComponent(typeLabel).addComponent(type).addComponent(priorityLabel).addComponent(priority)).addComponent(emailLabel).addComponent(email).addGroup(layout.createSequentialGroup().addComponent(submit).addComponent(cancel)));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(header).addComponent(summaryLabel).addComponent(summary).addComponent(descriptionLabel).addComponent(scroll).addGroup(layout.createParallelGroup().addComponent(typeLabel).addComponent(type).addComponent(priorityLabel).addComponent(priority)).addComponent(emailLabel).addComponent(email).addGroup(layout.createParallelGroup().addComponent(submit).addComponent(cancel)));

		// link sizes
		Component[] submitLabels = new Component[] { summaryLabel, descriptionLabel, emailLabel };
		layout.linkSize(submitLabels);

		Component[] submitFields = new Component[] { summary, scroll, email };
		layout.linkSize(SwingConstants.HORIZONTAL, submitFields);

		Component[] classifyBoxes = new Component[] { type, priority };
		layout.linkSize(SwingConstants.HORIZONTAL, classifyBoxes);

		Component[] classifyAll = new Component[] { typeLabel, type, priorityLabel, priority };
		layout.linkSize(SwingConstants.VERTICAL, classifyAll);

		Component[] buttons = new Component[] { submit, cancel };
		layout.linkSize(buttons);

		this.pack();
	}

	/**
	 * Submit issue to JIRA.
	 * 
	 * @throws Exception
	 */
	private void submitIssue() throws Exception {

		// create new issue
		Issue issue = new Issue();
		issue.setProject(service.getProject("OVTK"));
		issue.setType((IssueType) type.getSelectedItem());
		issue.setPriority((Priority) priority.getSelectedItem());
		issue.setSummary(summary.getText());
		issue.setDescription(description.getText() + "\n\nEmail: " + email.getText());

		// submit issue
		service.createIssue(issue);

		// close dialog
		try {
			this.setClosed(true);
		} catch (PropertyVetoException e1) {
			ErrorDialog.show(e1);
		}
	}

	/**
	 * Validates user input
	 * 
	 * @return validation result
	 */
	private boolean validateInput() {

		// check summary text
		String summaryText = summary.getText();
		if (summaryText.trim().length() == 0) {
			JOptionPane.showInternalMessageDialog(this, "Please provide a summary of the problem.", "Missing information", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		// check email text
		String emailText = email.getText();
		if (emailText.trim().length() == 0) {
			JOptionPane.showInternalMessageDialog(this, "Please provide an email address.", "Missing information", JOptionPane.WARNING_MESSAGE);
			return false;
		} else if (emailText.indexOf("@") < 1 || emailText.indexOf(".") < 1 || emailText.lastIndexOf(".") < emailText.indexOf("@")) {
			JOptionPane.showInternalMessageDialog(this, "Please provide a valid email address.", "Missing information", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
	}

}
