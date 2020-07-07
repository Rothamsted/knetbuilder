package net.sourceforge.ondex.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.ondex.config.Config;

/**
 * Parses the queries from the source files used by the SPAQL engine in the frontend and console.
 * @author lysenkoa
 *
 */
public class QuerySetParser {
	private final Map<String, String> queries = new HashMap<String, String>();
	private final Map<String, List<String>> lists = new HashMap<String, List<String>>();
	private final Map<String, List<String>> listsOfQueries = new HashMap<String, List<String>>();

	private File currentSet;
	
	public File getQuerySetLocation(){
		return currentSet;
	}
	
	public QuerySetParser() {}
	
	public void update() throws Exception{
		queries.clear();
		lists.clear();
		listsOfQueries.clear();
		for(File f : currentSet.listFiles()){
			if(f.getName().endsWith(".sqs") || f.getName().endsWith(".SQS") ){
				parse(f);
			}
		}
	}
	
	public void load(String id) throws Exception{
		File f = new File(Config.ondexDir + File.separator +"query_sets" + File.separator + id);
		if (f.exists() && f.isDirectory()) {
			currentSet = f;
			update();
		}
		else{
			throw new Exception("Could not find the directory \""+id+"\" at the expected location: "+ f.getAbsolutePath());
		}
	}
	
	public String getQuery(String id){
		String result = queries.get(id);
		if(result == null){
			throw new RuntimeException("Could not find \""+id+"\" in the query support registry.");
		}
		return result;
	}
	
	public List<String> getList(String id){
		List<String> result = lists.get(id);
		if(result == null){
			throw new RuntimeException("Could not find \""+id+"\" in the query support registry.");
		}
		return result;
	}
	
	public List<String> getCommandList(String id){
		List<String> result = listsOfQueries.get(id);
		if(result == null){
			throw new RuntimeException("Could not find \""+id+"\" in the query support registry.");
		}
		return result;
				
	}
	
	private BufferedReader br;

	public void parse(File f) throws Exception{
		System.out.println(f.getAbsolutePath());
		br = new BufferedReader(new FileReader(f));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#COMMAND_LIST")) {
				listsOfQueries.put(readName(), parseCommandList());
			}	
			else if (line.startsWith("#VARIABLE_LIST")) {
				lists.put(readName(), parseVarList());
			}
			else if (line.startsWith("#COMMAND")) {
				queries.put(readName(), parseCommand());
			}	
		}
		br.close();
	}

	protected String readName () throws Exception{
		String line = "";
		String name = "";
		while(name.length() == 0){
			line = br.readLine();
			name = line.trim();
		}
		return name;
	}

	protected String parseCommand() throws Exception{
		String line;
		StringBuffer command = new StringBuffer();
		while ((line = br.readLine()) != null) {
			if(line.startsWith("#VALUE")){
				continue;
			}
			else if(line.startsWith("#END")) {
				break;
			}
			else{ 
				command.append(" ");
				command.append(line);
			}
		}
		return command.toString();
	}

	protected List<String> parseCommandList() throws Exception{
		String line;
		List<String> list = new ArrayList<String>();
		StringBuffer command = new StringBuffer();
		while ((line = br.readLine()) != null) {
			if(line.startsWith("#VALUE")){
				if(command.length() == 0){
					continue;	
				}
				else{
					list.add(command.toString());
					command = new StringBuffer();
				}
			}
			else if(line.startsWith("#END")) {
				list.add(command.toString());
				break;
			}
			else{
				command.append(" ");
				command.append(line);

			}
		}
		return list;
	}

	protected List<String> parseVarList() throws Exception{
		String line;
		List<String> list = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			if(line.startsWith("#VALUE")){
				continue;
			}
			else if(line.startsWith("#END")) {
				break;
			}
			else{ 
				list.add(line.trim());
			}
		}
		return list;
	}

}
