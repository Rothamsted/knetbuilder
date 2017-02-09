package net.sourceforge.ondex.dashboard.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.dashboard.client.Dashboard;
import net.sourceforge.ondex.dashboard.client.JiraService;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteFilter;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemoteProject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class JiraServiceImpl extends RemoteServiceServlet implements
		JiraService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Map<String, int[]> getIssueSignatures() throws Exception {
		String token = JiraConnector.getInstance().getAuthToken();
		JiraSoapService service = JiraConnector.getInstance().getService();
		Map<String, int[]> map = new HashMap<String, int[]>();
		int numPriorities = service.getPriorities(token).length;
		for (RemoteFilter filter : service.getFavouriteFilters(token)) {
			if (filter.getName().equals("all not closed")) {
				for (RemoteIssue issue : service.getIssuesFromFilter(token,
						filter.getId())) {
					// only count bugs
					if (issue.getType().equals("1")) {
						int priority = Integer.parseInt(issue.getPriority()) - 1;
						int[] counter = map.get(issue.getProject());
						if (counter == null) {
							counter = new int[numPriorities];
							map.put(issue.getProject(), counter);
						}
						counter[priority]++;
					}
				}
			}
		}
		// HashMap<String, int[]> map = new HashMap<String, int[]>();
		// map.put("OVTK",new int[]{0,0,0,0,0});
		// map.put("MDEDITOR", new int[]{0,0,0,0,0});
		System.out.println("Sending response");
		return map;
	}

	@Override
	public String getDescription(String projectID) throws Exception {
		String token = JiraConnector.getInstance().getAuthToken();
		JiraSoapService service = JiraConnector.getInstance().getService();
		RemoteProject p = service.getProjectByKey(token, projectID);
		return p.getDescription();

	}

	@Override
	public String[] getIssues(String projectID) throws Exception {
		String token = JiraConnector.getInstance().getAuthToken();
		JiraSoapService service = JiraConnector.getInstance().getService();

		ArrayList<String> list = new ArrayList<String>();
		String url = service.getServerInfo(token).getBaseUrl();

		// System.out.println("Available issue filters: ");
		RemoteFilter[] filters = service.getFavouriteFilters(token);
		for (RemoteFilter filter : filters) {
			// System.out.println(filter.getName());
			if (filter.getName().equals("all not closed")) {
				RemoteIssue[] issues = service.getIssuesFromFilter(token,
						filter.getId());
				for (RemoteIssue issue : issues) {
					if (issue.getProject().equals(projectID)) {
						String link = url + "/browse/" + issue.getKey();
						list.add("<a href=\"" + link + "\" target=\"new\">"
								+ issue.getSummary() + "</a> ("
								+ Dashboard.priorities.get(issue.getPriority())
								+ " - " + Dashboard.types.get(issue.getType())
								+ ")<br/>");
					}
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public void submitIssue(String projectID, String summary,
			String description, String priority, String type) throws Exception {
		String token = JiraConnector.getInstance().getAuthToken();
		JiraSoapService service = JiraConnector.getInstance().getService();

		RemoteIssue issue = new RemoteIssue();
		issue.setType(type);
		issue.setSummary(summary);
		issue.setProject(projectID);
		issue.setDescription(description);
		issue.setPriority(priority);

		service.createIssue(token, issue);
	}

}
