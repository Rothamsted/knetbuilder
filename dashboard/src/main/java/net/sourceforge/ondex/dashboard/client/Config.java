package net.sourceforge.ondex.dashboard.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

public class Config {
	
	public static final String configFile = "config.xml";
	
	private HashMap<String,String> categories;
	
	private HashMap<String,ArrayList<Module>> cat2moduleList;
	
	private static Config instance;
		
	private Config() {
		
	}
	
	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}

	public void update(final MyListener listener) {
		
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL() + configFile);
	    try {
	      requestBuilder.sendRequest(null, new RequestCallback() {
			   public void onError(Request request, Throwable exception) {
				   listener.error(exception);
			   }
			   public void onResponseReceived(Request request, Response response) {
				   String xmlContents = response.getText();
				   
				   Document dom = XMLParser.parse(xmlContents);
					
					categories = new HashMap<String,String>();
					cat2moduleList = new HashMap<String, ArrayList<Module>>();
					
					NodeList catList = dom.getElementsByTagName("categories").item(0).getChildNodes();
					for (int i = 0; i < catList.getLength(); i++) {
						if (catList.item(i) instanceof Element) {
							Element catNode = (Element) catList.item(i);
							String id = catNode.getAttribute("id");
							String name = catNode.getAttribute("name");
							categories.put(id,name);
						}
					}
					
					NodeList moduleList = dom.getElementsByTagName("modules").item(0).getChildNodes();
					for (int i = 0; i < moduleList.getLength(); i++) {
						if (!(moduleList.item(i) instanceof Element)) {
							continue;
						}
						Element modNode = (Element) moduleList.item(i);
						String jiraID = modNode.getAttribute("jiraID");
						String catKey = modNode.getAttribute("category");
						String alias = null;
						NodeList aliases = modNode.getElementsByTagName("alias");
						if (aliases != null && aliases.getLength() > 0) {
							alias = ((Text)aliases.item(0).getFirstChild()).getData();
						}
						String downloadURL = null;
						NodeList urls = modNode.getElementsByTagName("download-url");
						if (urls != null && urls.getLength() > 0) {
							downloadURL = ((Text)urls.item(0).getFirstChild()).getData();
						}
						
						ArrayList<Module> list = cat2moduleList.get(catKey);
						if (list == null) { 
							list = new ArrayList<Module>();
							cat2moduleList.put(catKey, list);
						}
						list.add(new Module(jiraID, catKey, alias, downloadURL));
					}
					listener.completed(null);
			   }
	      });
	    } catch (RequestException ex) {
	    	listener.error(ex);
	    }
	    
	}

	public Set<String> getCategoryKeys() {
		return categories.keySet();
	}
	
	public String getCategory(String key) {
		return categories.get(key);
	}
	
	public List<Module> getModulesForCategory(String key) {
		return cat2moduleList.get(key);
	}
	
}
