package net.sourceforge.ondex.dashboard.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Dashboard implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	private final JiraServiceAsync jiraService = GWT.create(JiraService.class);

	private HashMap<String, Integer> mod2Row = new HashMap<String, Integer>();
	private HashMap<String, FlexTable> mod2table = new HashMap<String, FlexTable>();

	private DetailsDialog dialog;
	
	@SuppressWarnings("serial")
	public final static Map<String, String> priorities = new HashMap<String, String>() {
		{
			put("1", "Blocker");
			put("2", "Critical");
			put("3", "Major");
			put("4", "Minor");
			put("5", "Trivial");
		}
	};

	@SuppressWarnings("serial")
	public final static Map<String, String> types = new HashMap<String, String>() {
		{
			put("1", "Bug");
			put("2", "New Feature");
			put("3", "Task");
			put("4", "Improvement");
			put("5", "Sub-task");
			put("6", "Usability issue");
		}
	};


	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		RootPanel.get("main").add(new Image("loading.gif"));

		dialog = new DetailsDialog();

		Config.getInstance().update(new MyListener() {
			@Override
			public void completed(Object o) {
				TabPanel tabPanel = new TabPanel();
				Config config = Config.getInstance();
				for (String catKey : new TreeSet<String>(config
						.getCategoryKeys())) {
					String category = config.getCategory(catKey);
					List<Module> modules = config.getModulesForCategory(catKey);
					FlexTable table = new FlexTable();
					if (modules != null) {
						int i = 0;
						for (Module module : modules) {
							mod2Row.put(module.getJiraID(), i);
							mod2table.put(module.getJiraID(), table);
							addContents(table, module, i++);
						}
					}
					tabPanel.add(table, category);
				}
				queryStates();

				tabPanel.selectTab(0);
				RootPanel.get("main").clear();
				RootPanel.get("main").add(tabPanel);
			}

			@Override
			public void error(Throwable t) {
				RootPanel.get("main")
						.add(new HTML("Error loading config.xml: "
								+ t.getMessage()));
			}
		});

	}

	private void queryStates() {
		jiraService.getIssueSignatures(new AsyncCallback<Map<String, int[]>>() {

			@Override
			public void onFailure(Throwable caught) {
				for (FlexTable table : mod2table.values()) {
					for (int row = 0; row < table.getRowCount(); row++) {
						HTML fail = new HTML("fail");
						fail.getElement().setClassName("gwt-HTML fail");
						table.setWidget(row, 1, fail);
					}
				}
			}

			@Override
			public void onSuccess(Map<String, int[]> result) {
				if (result == null) {
					onFailure(null);
					return;
				}
				for (String modID : result.keySet()) {
					if (!mod2Row.containsKey(modID)) {
						continue;
					}
					int[] signature = result.get(modID);
					FlexTable table = mod2table.get(modID);
					int row = mod2Row.get(modID);
					String statusS = "fail";
					if (signature[0] > 0 || signature[1] > 0) {
						statusS = "broken";
					} else if (signature[2] > 0) {
						statusS = "unstable";
					} else {
						statusS = "stable";
					}
					HTML status = new HTML(statusS);
					status.getElement().setClassName("gwt-HTML " + statusS);
					table.setWidget(row, 1, status);
				}
				for (String modID : mod2Row.keySet()) {
					if (result.keySet().contains(modID)) {
						continue;
					}
					FlexTable table = mod2table.get(modID);
					int row = mod2Row.get(modID);
					String statusS = "stable";
					HTML status = new HTML(statusS);
					status.getElement().setClassName("gwt-HTML " + statusS);
					table.setWidget(row, 1, status);
				}
			}
		});
	}

	private void addContents(FlexTable table, final Module module, int row) {
		String name = module.getAlias() == null ? module.getJiraID() : module
				.getAlias();
		table.setHTML(row, 0, name);
		FlowPanel imgPanel = new FlowPanel();
		imgPanel.add(new Image("loading.gif"));
		table.setWidget(row, 1, imgPanel);
		final Button openButton = new Button("Open");
		openButton.getElement().setClassName("openButton");
		openButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.show(module);
			}
		});
		table.setWidget(row, 2, openButton);

	}

	private class DetailsDialog extends DialogBox {
		private Button closeButton;
		private FlexTable contents;
		private DisclosurePanel issuePanel, submitPanel;
		private String currentProject;

		public DetailsDialog() {
			setText("Details");
			setAnimationEnabled(true);
			setModal(true);
			closeButton = new Button("Close");
			// We can set the id of a widget by accessing its Element
			closeButton.getElement().setId("closeButton");
			VerticalPanel dialogVPanel = new VerticalPanel();
			dialogVPanel.addStyleName("dialogVPanel");
			setWidget(dialogVPanel);

			issuePanel = new DisclosurePanel("Issues");
			issuePanel.add(new Image("loading.gif"));

			submitPanel = new DisclosurePanel("Reporting");
			// submitPanel.add(createSubmitPanel());

			contents = new FlexTable();
			contents.setCellPadding(10);
			contents.getElement().setId("dialogContents");
			contents.setHTML(0, 0, "Name:");
			contents.setHTML(1, 0, "Description:");
			contents.setWidget(1, 1, new Image("loading.gif"));
			contents.setHTML(2, 0, "Download:");
			contents.getFlexCellFormatter().setColSpan(3, 0, 2);
			contents.setWidget(3, 0, submitPanel);
			contents.getFlexCellFormatter().setColSpan(4, 0, 2);
			contents.setWidget(4, 0, issuePanel);

			dialogVPanel.add(contents);

			dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
			dialogVPanel.add(closeButton);

			// Add a handler to close the DialogBox
			closeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					hide();
				}
			});
		}

		private Widget createSubmitPanel() {
			final TextBox summary = new TextBox();
			summary.setWidth("400px");
			summary.setText("Summary");

			final TextArea description = new TextArea();
			description.setWidth("400px");
			description.setText("Description");

			// drop down box with issue types
			final ListBox issueTypeBox = new ListBox();
			String[] keys = types.keySet().toArray(new String[0]);
			Arrays.sort(keys);
			for (String key : keys) {
				issueTypeBox.addItem(types.get(key), key);
			}
			issueTypeBox.setSelectedIndex(0);

			// drop down box with priorities
			keys = priorities.keySet().toArray(new String[0]);
			Arrays.sort(keys);
			final ListBox priorityBox = new ListBox();
			for (String key : keys) {
				priorityBox.addItem(priorities.get(key), key);
			}
			priorityBox.setSelectedIndex(3);

			// panel with issue type and priority
			final HorizontalPanel p2 = new HorizontalPanel();
			p2.add(new Label("Type: "));
			p2.add(issueTypeBox);
			p2.add(new Label(" Priority: "));
			p2.add(priorityBox);

			final TextBox email = new TextBox();
			email.setText("Email");
			email.setWidth("400px");

			final DockPanel panel = new DockPanel();

			final Button submitButton = new Button("Submit");
			submitButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					submitIssue(panel, currentProject, summary.getText(),
							description.getText(), email.getText(), priorityBox
									.getValue(priorityBox.getSelectedIndex()),
							issueTypeBox.getValue(issueTypeBox
									.getSelectedIndex()));
				}
			});

			panel.add(summary, DockPanel.NORTH);
			panel.add(description, DockPanel.CENTER);
			panel.setHorizontalAlignment(DockPanel.ALIGN_RIGHT);
			panel.add(submitButton, DockPanel.SOUTH);
			panel.setHorizontalAlignment(DockPanel.ALIGN_LEFT);
			panel.add(p2, DockPanel.SOUTH);
			panel.add(email, DockPanel.SOUTH);

			return panel;
		}

		private void submitIssue(final DockPanel panel, String project,
				String summary, String description, String email,
				String priority, String type) {

			// progress dialog
			final DialogBox note = new DialogBox();
			note.setText("Submitting...");
			note.setTitle("Submitting...");

			final DockPanel dialog = new DockPanel();
			note.add(dialog);

			final Image loading = new Image("loading.gif");
			dialog.add(loading, DockPanel.CENTER);

			// close button
			final Button closeButton = new Button("Close");
			closeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent arg0) {
					note.hide();
				}
			});

			note.show();

			String desc = description + "\n\nContact: " + email;

			jiraService.submitIssue(project, summary, desc, priority, type,
					new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							dialog.remove(loading);
							dialog.add(new HTML("<b>Submission failed!</b>"),
									DockPanel.CENTER);
							dialog.add(closeButton, DockPanel.SOUTH);
						}

						@Override
						public void onSuccess(Void result) {
							dialog.remove(loading);
							dialog.add(
									new HTML("<b>Submission successful!</b>"),
									DockPanel.CENTER);
							dialog.add(closeButton, DockPanel.SOUTH);
						}
					});
		}

		public void show(Module module) {
			String name = module.getAlias() == null ? module.getJiraID()
					: module.getAlias();
			currentProject = module.getJiraID();

			contents.setHTML(0, 1, name);
			contents.setHTML(2, 1, "<a href=\"" + module.getDownloadURL()
					+ "\">Latest snapshot build</a>");

			contents.clearCell(1, 1);
			contents.setWidget(1, 1, new Image("loading.gif"));
			jiraService.getDescription(module.getJiraID(),
					new AsyncCallback<String>() {
						@Override
						public void onFailure(Throwable caught) {
							contents.clearCell(1, 1);
							contents.setHTML(1, 1, "<i>No information</i>");
						}

						@Override
						public void onSuccess(String result) {
							if (result == null || result.equals("")) {
								onFailure(null);
							} else {
								contents.clearCell(1, 1);
								contents.setHTML(1, 1, result);
							}
						}
					});

			issuePanel.clear();
			issuePanel.add(new Image("loading.gif"));
			jiraService.getIssues(module.getJiraID(),
					new AsyncCallback<String[]>() {
						@Override
						public void onFailure(Throwable caught) {
							issuePanel.clear();
							issuePanel
									.add(new HTML("<i>Connection failed</i>"));
						}

						@Override
						public void onSuccess(String[] result) {
							System.out.println("Issues: " + result);
							if (result == null || result.length == 0) {
								issuePanel.clear();
								issuePanel.add(new HTML("<i>No issues</i>"));
							} else {
								StringBuilder b = new StringBuilder("<ul>");
								for (String s : result) {
									b.append("<li>" + s + "</li>");
								}
								b.append("</ul>");
								issuePanel.clear();
								ScrollPanel scrollpanel = new ScrollPanel(
										new HTML(b.toString()));
								scrollpanel.setHeight("200px");
								issuePanel.add(scrollpanel);
							}
						}
					});

			submitPanel.clear();
			submitPanel.add(createSubmitPanel());

			center();
			closeButton.setFocus(true);
		}
	}

}
