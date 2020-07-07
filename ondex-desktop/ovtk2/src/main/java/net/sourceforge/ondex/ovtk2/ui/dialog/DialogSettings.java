package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;

/**
 * Simple settings dialog.
 * 
 * @author taubertj
 * 
 */
public class DialogSettings extends OVTK2Dialog {

	// generated
	private static final long serialVersionUID = 1927615194290133309L;

	// user name input field
	private JTextField user = null;

	// password input field
	private JTextField password = null;

	// URL field for webservice
	private JTextField url = null;

	// when using a web proxy
	private JCheckBox proxySet = null;

	// hostname of proxy
	private JTextField proxyHost = null;

	// port number of proxy
	private JTextField proxyPort = null;

	// show notifications
	private JCheckBox notificationsSet = null;

	// show overwrite warnings
	private JCheckBox overwriteSet = null;

	// perform auto-save
	private JCheckBox autosaveSet = null;

	// choice how to close a filter
	private JComboBox filterCloseOptions = null;

	// activate choice of closing filter
	private JCheckBox filterCloseSet = null;

	// model for auto-save timer spinner
	private SpinnerModel spinnerModel = new SpinnerNumberModel(5, 1, 60, 1);

	// auto-save timer spinner
	private JSpinner timerSpinner = new JSpinner(spinnerModel);

	// custom field witdth in this case
	private int fieldWidth = 250;

	/**
	 * Constructs the settings dialog as internal frame.
	 * 
	 */
	public DialogSettings() {
		super("Dialog.Settings.Title", "Preferences16.gif");

		// add components to content pane
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeLoginPanel(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.Settings.Apply", "Dialog.Settings.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Make Login input panel.
	 * 
	 * @return JPanel
	 */
	private JPanel makeLoginPanel() {

		// check if webservice is enabled
		boolean webserviceEnable = true;
		if (Config.config.getProperty("Webservice.Enable") != null) {
			webserviceEnable = Boolean.valueOf(Config.config.getProperty("Webservice.Enable"));
		}

		// layout and put a border around login
		JPanel login = new JPanel();
		GroupLayout layout = new GroupLayout(login);
		layout.setAutoCreateGaps(true);
		login.setLayout(layout);

		TitledBorder loginBorder;
		loginBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Settings.Login"));
		login.setBorder(loginBorder);

		// panel for user name input
		JLabel userLabel = new JLabel(Config.language.getProperty("Dialog.Settings.User"));
		user = new JTextField(Config.config.getProperty("Program.User"));
		user.setPreferredSize(new Dimension(fieldWidth, this.getFieldHeight()));
		user.setBackground(this.getRequiredColor());

		login.add(userLabel);
		login.add(user);

		// panel for password input
		JLabel passwordLabel = new JLabel(Config.language.getProperty("Dialog.Settings.Password"));
		password = new JTextField(Config.config.getProperty("Program.Password"));
		password.setPreferredSize(new Dimension(fieldWidth, this.getFieldHeight()));
		password.setBackground(this.getRequiredColor());

		login.add(passwordLabel);
		login.add(password);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(userLabel).addComponent(passwordLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup().addComponent(user, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(password, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(userLabel).addComponent(user)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(passwordLabel).addComponent(password)));

		JPanel webservice = null;
		if (webserviceEnable) {
			// layout and put a border around webservice
			webservice = new JPanel();
			layout = new GroupLayout(webservice);
			layout.setAutoCreateGaps(true);
			webservice.setLayout(layout);

			TitledBorder webserviceBorder;
			webserviceBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Settings.Webservice"));
			webservice.setBorder(webserviceBorder);

			// panel for url input
			JLabel urlLabel = new JLabel(Config.language.getProperty("Dialog.Settings.URL"));
			url = new JTextField(Config.config.getProperty("Program.URL"));
			url.setPreferredSize(new Dimension(fieldWidth, this.getFieldHeight()));

			webservice.add(urlLabel);
			webservice.add(url);

			// panel for proxy configuration
			JLabel setLabel = new JLabel(Config.language.getProperty("Dialog.Settings.ProxySet"));
			proxySet = new JCheckBox();
			proxySet.setSelected(Boolean.parseBoolean(Config.config.getProperty("Proxy.Set")));

			webservice.add(setLabel);
			webservice.add(proxySet);

			JLabel hostLabel = new JLabel(Config.language.getProperty("Dialog.Settings.ProxyHost"));
			proxyHost = new JTextField(Config.config.getProperty("Proxy.Host"));
			proxyHost.setPreferredSize(new Dimension(fieldWidth, this.getFieldHeight()));

			webservice.add(hostLabel);
			webservice.add(proxyHost);

			JLabel portLabel = new JLabel(Config.language.getProperty("Dialog.Settings.ProxyPort"));
			proxyPort = new JTextField(Config.config.getProperty("Proxy.Port"));
			proxyPort.setPreferredSize(new Dimension(fieldWidth, this.getFieldHeight()));

			webservice.add(portLabel);
			webservice.add(proxyPort);

			layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(urlLabel).addComponent(setLabel).addComponent(hostLabel).addComponent(portLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup().addComponent(url, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(proxySet, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(proxyHost, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(proxyPort, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
			layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(urlLabel).addComponent(url)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(setLabel).addComponent(proxySet)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(hostLabel).addComponent(proxyHost)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(portLabel).addComponent(proxyPort)));
		}

		// panel for notifications configuration
		JPanel notificationsPanel = new JPanel();
		BoxLayout boxlayout = new BoxLayout(notificationsPanel, BoxLayout.PAGE_AXIS);
		notificationsPanel.setLayout(boxlayout);
		TitledBorder notificationsBorder;
		notificationsBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Settings.Notifications"));
		notificationsPanel.setBorder(notificationsBorder);

		JLabel notificationsLabel = new JLabel(Config.language.getProperty("Dialog.Settings.NotificationsSet"));
		notificationsSet = new JCheckBox();
		notificationsSet.setSelected(Boolean.parseBoolean(Config.config.getProperty("Notifications.Set")));

		JLabel overwriteLabel = new JLabel(Config.language.getProperty("Dialog.Settings.OverwriteSet"));
		overwriteSet = new JCheckBox();
		overwriteSet.setSelected(Boolean.parseBoolean(Config.config.getProperty("Overwrite.Set")));

		JLabel filterOptionsLabel = new JLabel(Config.language.getProperty("Dialog.Settings.FilterOptionsSet"));
		String[] options = new String[] { Config.language.getProperty("Filter.Save.Changes.Keep"), Config.language.getProperty("Filter.Save.Changes.KeepApply"), Config.language.getProperty("Filter.Save.Changes.Discard") };
		filterCloseOptions = new JComboBox(options);
		filterCloseOptions.setSelectedIndex(Integer.parseInt(Config.config.getProperty("FilterClose.Option")));
		filterCloseSet = new JCheckBox();
		filterCloseSet.setSelected(Boolean.parseBoolean(Config.config.getProperty("FilterClose.Set")));

		JPanel one = new JPanel(new FlowLayout());
		one.add(notificationsSet);
		one.add(notificationsLabel);
		JPanel two = new JPanel(new FlowLayout());
		two.add(overwriteSet);
		two.add(overwriteLabel);
		JPanel three = new JPanel(new FlowLayout());
		three.add(filterCloseSet);
		three.add(filterOptionsLabel);
		three.add(filterCloseOptions);

		notificationsPanel.add(one);
		notificationsPanel.add(two);
		notificationsPanel.add(three);

		// layout and put a border around auto-save
		JPanel autosave = new JPanel();
		layout = new GroupLayout(autosave);
		layout.setAutoCreateGaps(true);
		autosave.setLayout(layout);

		TitledBorder autosaveBorder;
		autosaveBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Settings.Autosave"));
		autosave.setBorder(autosaveBorder);

		// panel for enabling auto-save
		JLabel enableLabel = new JLabel(Config.language.getProperty("Dialog.Settings.AutosaveSet"));
		autosaveSet = new JCheckBox();
		if (Config.config.getProperty("Autosave.Set") != null && Boolean.valueOf(Config.config.getProperty("Autosave.Set")) == true)
			autosaveSet.setSelected(true);

		autosave.add(enableLabel);
		autosave.add(autosaveSet);

		// panel for time of auto-save
		JLabel timerLabel = new JLabel(Config.language.getProperty("Dialog.Settings.AutosaveTimer"));
		if (Config.config.getProperty("Autosave.Interval") != null) {
			int newvalue = Integer.parseInt(Config.config.getProperty("Autosave.Interval"));
			timerSpinner.setValue(newvalue);
		}

		autosave.add(timerLabel);
		autosave.add(timerSpinner);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(enableLabel).addComponent(timerLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup().addComponent(autosaveSet, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(timerSpinner, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(enableLabel).addComponent(autosaveSet)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(timerLabel).addComponent(timerSpinner)));

		// bringing it all together
		JPanel upper = new JPanel();
		layout = new GroupLayout(upper);
		layout.setAutoCreateGaps(true);
		upper.setLayout(layout);

		if (webservice != null)
			upper.add(webservice);
		upper.add(notificationsPanel);
		upper.add(autosave);
		upper.add(login);

		if (webservice != null) {
			layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(webservice, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(notificationsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(autosave, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(login, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
			layout.setVerticalGroup(layout.createSequentialGroup().addComponent(webservice).addComponent(notificationsPanel).addComponent(autosave).addComponent(login).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		} else {
			layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)

			.addComponent(notificationsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(autosave, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(login, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
			layout.setVerticalGroup(layout.createSequentialGroup().addComponent(notificationsPanel).addComponent(autosave).addComponent(login).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		}

		return upper;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {

		String cmd = arg0.getActionCommand();

		if (cmd.equals("apply")) {

			// update current config
			Config.config.setProperty("Program.User", user.getText());
			Config.config.setProperty("Program.Password", password.getText());
			if (url != null)
				Config.config.setProperty("Program.URL", url.getText());
			if (proxySet != null)
				Config.config.setProperty("Proxy.Set", Boolean.valueOf(proxySet.isSelected()).toString());
			if (proxyHost != null)
				Config.config.setProperty("Proxy.Host", proxyHost.getText());
			if (proxyPort != null)
				Config.config.setProperty("Proxy.Port", proxyPort.getText());
			Config.config.setProperty("Notifications.Set", Boolean.valueOf(notificationsSet.isSelected()).toString());
			Config.config.setProperty("Overwrite.Set", Boolean.valueOf(overwriteSet.isSelected()).toString());
			Config.config.setProperty("Autosave.Set", Boolean.valueOf(autosaveSet.isSelected()).toString());
			Config.config.setProperty("Autosave.Interval", timerSpinner.getValue().toString());
			Config.config.setProperty("FilterClose.Set", Boolean.valueOf(filterCloseSet.isSelected()).toString());
			Config.config.setProperty("FilterClose.Option", String.valueOf(filterCloseOptions.getSelectedIndex()));
			Config.initProxySetting();
			Config.saveConfig();

			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}
		} else if (cmd.equals("cancel")) {
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}
		}
	}
}
