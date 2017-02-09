package net.sourceforge.ondex.dashboard.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;


import com.atlassian.jira.rpc.soap.client.JiraSoapService;

public class JiraConnector {

	private String token;

	private JiraSoapService service;

	private static JiraConnector instance;

	private static long authTime;

	public JiraConnector(String serviceURL, String uname, String pwd)
			throws MalformedURLException, RemoteException {
		URL url = new URL(serviceURL);
		SOAPSession session = new SOAPSession(url);
		session.connect(uname, pwd);
		service = session.getJiraSoapService();
		token = session.getAuthenticationToken();
		authTime = System.currentTimeMillis();
	}

	public static JiraConnector getInstance() throws Exception {
		if (instance == null) {
			Config config = Config.getInstance();
			instance = new JiraConnector(config.getJiraURL(),
					config.getJiraUser(), config.getJiraPwd());
		} else {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - authTime) > 59 * 60000) {
				System.out.println("Re-authenticate after "
						+ ((currentTime - authTime) / 60000) + "min.");
				authTime = currentTime;
				instance = null;
				return getInstance();
			}
		}
		return instance;
	}

	public String getAuthToken() {
		return token;
	}

	public JiraSoapService getService() {
		return service;
	}

}
