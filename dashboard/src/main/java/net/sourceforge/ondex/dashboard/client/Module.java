package net.sourceforge.ondex.dashboard.client;

public class Module {
	private String jiraID, categoryID, alias, downloadURL;

	public Module(String jiraID, String categoryID, String alias, String downloadURL) {
		this.jiraID = jiraID;
		this.categoryID = categoryID;
		this.alias = alias;
		this.downloadURL = downloadURL;
	}
	
	public Module(String jiraID, String categoryID) {
		this.jiraID = jiraID;
		this.categoryID = categoryID;
	}
	
	public String getJiraID() {
		return jiraID;
	}

	public void setJiraID(String jiraID) {
		this.jiraID = jiraID;
	}

	public String getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}

}
