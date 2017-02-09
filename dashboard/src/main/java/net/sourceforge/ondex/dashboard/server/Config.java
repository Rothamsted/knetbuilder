package net.sourceforge.ondex.dashboard.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Config {
	private static Config instance;

	public static final String configFile = "config.xml";

	private String jiraURL, jiraUser, jiraPwd;

	private Config() {
		update();
	}

	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}

	public void update() {
		System.out.println("Parsing config file");
		DOMParser parser = new DOMParser();
		try {
			// TODO: that is a bad hack here, only works with Tomcat
			File webapps = new File(System.getProperty("catalina.base")
					+ "/webapps");
			for (File child : webapps.listFiles()) {
				if (child.getName().startsWith("dashboard")) {
					File config = new File(child.getAbsolutePath() + "/"
							+ configFile);
					if (config.canRead()) {
						System.out.println(config);
						InputStreamReader reader = new InputStreamReader(
								new FileInputStream(config));
						InputSource source = new InputSource(reader);
						parser.parse(source);
						break;
					}
				}
			}

			Document dom = parser.getDocument();
			NodeList connCred = dom.getElementsByTagName("jiraConnection")
					.item(0).getChildNodes();
			for (int i = 0; i < connCred.getLength(); i++) {
				if (!(connCred.item(i) instanceof Element)) {
					continue;
				}
				Element e = (Element) connCred.item(i);
				if (e.getNodeName().equals("service-url")) {
					jiraURL = ((Text) e.getFirstChild()).getData();
				} else if (e.getNodeName().equals("username")) {
					jiraUser = ((Text) e.getFirstChild()).getData();
				} else if (e.getNodeName().equals("password")) {
					jiraPwd = ((Text) e.getFirstChild()).getData();
				} else if (e.getNodeName().equals("proxy")) {
					String proxy = ((Text) e.getFirstChild()).getData();
					String[] split = proxy.split(":");
					if (split.length == 2) {
						String host = split[0];
						String port = split[1];
						System.setProperty("http.proxyHost", host);
						System.setProperty("http.proxyPort", port);

					}
				}
			}

			System.out.println("Accessing: " + jiraURL + " with user "
					+ jiraUser);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getJiraURL() {
		return jiraURL;
	}

	public void setJiraURL(String jiraURL) {
		this.jiraURL = jiraURL;
	}

	public String getJiraUser() {
		return jiraUser;
	}

	public void setJiraUser(String jiraUser) {
		this.jiraUser = jiraUser;
	}

	public String getJiraPwd() {
		return jiraPwd;
	}

	public void setJiraPwd(String jiraPwd) {
		this.jiraPwd = jiraPwd;
	}
}
