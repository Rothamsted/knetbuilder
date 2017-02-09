package net.sourceforge.ondex.dashboard.client;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("jira")
public interface JiraService extends RemoteService {
	Map<String,int[]> getIssueSignatures() throws Exception;
	String getDescription(String projectID) throws Exception;
	String[] getIssues(String projectID) throws Exception;
	void submitIssue(String projectID, String summary, String description, String priority, String type) throws Exception;
}
