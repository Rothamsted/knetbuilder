package net.sourceforge.ondex.workflow2.gui.components;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class IniReader {
	private static final boolean DEBUG = false;
	private Map<String, String> sections = new HashMap<String, String>();
	private Map<String, String> defaults = new HashMap<String, String>();
	private Map<String, String> current = new HashMap<String, String>();
	private Map<String, ListOption> currentLists = new HashMap<String, ListOption>();
	private String file;
	private boolean doNotSave = false;
	private boolean disabled = false;
	
	public IniReader(String file){
		this.file = file;
		File f = new File(file);
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				if(DEBUG)e.printStackTrace();
				disabled = true;
			}	
		}
	}
	
	private Map<String, String> readMap(String file, String  delimiter)throws Exception{
		if(disabled)
			return new HashMap<String, String>();
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		Map<String, String> map = new HashMap<String, String>();
		while((strLine = br.readLine()) != null){
			strLine = strLine.trim();
			if(strLine.length()> 0 && !strLine.startsWith("#")){
				String [] data = strLine.split(delimiter);
				if(data.length > 1){
					map.put(data[0].trim(), data[1].trim());
				}	
			}
		}
		in.close();
		return map;
	}
	
	public void readConfig() throws Exception{
		if(disabled)
			return;
		current = readMap(file, "=");
		current.keySet().retainAll(sections.keySet());
		Map<String, String> defaultsCopy = new HashMap<String, String>(defaults);
		defaultsCopy.keySet().removeAll(current.keySet());
		current.putAll(defaultsCopy);
		Map<String, String> toList = new HashMap<String, String>(current);
		toList.keySet().retainAll(currentLists.keySet());
		current.keySet().removeAll(currentLists.keySet());
		for(Entry<String, String> ent: toList.entrySet()){
			ListOption l = currentLists.get(ent.getKey());
			if(l != null){
				l.addAll(ent.getValue().split(";"));
			}
		}
	}
	
	public void saveConfig() throws Exception{
		if(disabled)
			return;
		Set<String> set = new HashSet<String>(sections.values());
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		for(String s: set){
			bw.write(s);
			bw.write("\n");
			bw.flush();
			for(Entry<String, String> ent : current.entrySet()){
				if(sections.get(ent.getKey()).equals(s)){
					bw.write(ent.getKey());	
					bw.write("=");
					bw.write(ent.getValue());	
					bw.write("\n");
					bw.flush();
				}
			}
			for(Entry<String, ListOption> ent : currentLists.entrySet()){
				if(sections.get(ent.getKey()).equals(s)){
					bw.write(ent.getKey());	
					bw.write("=");
					bw.write(ent.getValue().getValue());	
					bw.write("\n");
					bw.flush();
				}
			}
			bw.write("\n");
			bw.flush();
		}
		out.close();
	}
	
	public void addOption(String value, String section, String defaultValue){
		section = processSectionName(section);
		sections.put(value, section);
		defaults.put(value, defaultValue);
	}
	
	public String getOption(String name){
		return current.get(name);
	}
	
	public void clearListOption(String name){
		ListOption lo = currentLists.get(name);
		if(lo != null){
			lo.clear();
		}
		try {
			if(!doNotSave)
				saveConfig();
		} catch (Exception e) {}
	}
	
	public Integer getIntegerOption(String name){
		String s =  current.get(name);
		if(s == null){
			return null;
		}
		else{
			try{
				return Integer.valueOf(s);
			}
			catch(NumberFormatException e){
				return null;	
			}
		}
	}
	
	public List<String> getOptionList(String name){
		return new ArrayList<String>(currentLists.get(name).getList());
	}
	

	public void addOption(String value, String section){
		section = processSectionName(section);
		sections.put(value, section);	
	}
	
    
    public void addToEndOfList(String name, String value){
    	ListOption lo = currentLists.get(name);
    	if(lo != null){
    		 lo.moveToEnd(value);	
    	}
		try{
			if(!doNotSave)
				saveConfig();	
		}
		catch(Exception e){}
    }
	
	public void addBoundedListOption(String value, String section, int limit){
		section = processSectionName(section);
		sections.put(value, section);
		currentLists.put(value, new ListOption(limit));
	}
	
	public void addUnboundedListOption(String value, String section){
		section = section.trim();
		if(!section.startsWith("#")){
			section = "#"+section;	
		}
		sections.put(value, section);
		currentLists.put(value, new ListOption());
	}
	
	public void setOption(String name, String value){
		if(!sections.containsKey(name)){
			return;	
		}
		ListOption lo = currentLists.get(name);
		if(lo != null){
			lo.add(value);
		}
		else{
			current.put(name, value);
		}
		try{
			if(!doNotSave)
				saveConfig();	
		}
		catch(Exception e){}
	}
	
	public void setDoNotSave(boolean b){
		doNotSave = b;
		if(!doNotSave){
			try {
				saveConfig();
			} catch (Exception e) {}
		}
	}
	
	private class ListOption{
		Integer bound  = null;
		LinkedList<String> values = new LinkedList<String>();
		
		public ListOption(){
			
		}
		
		public ListOption(Integer bound){
			this.bound = bound;
		}
		
		public ListOption(Collection<String> col, Integer bound){
			this.bound = bound;
			for(String s : col){
				add(s);
			}
		}
		
		public ListOption(Collection<String> col){
			for(String s : col){
				add(s);
			}
		}
		
		public void clear(){
			values.clear();
		}
		
		public void addAll(String [] col){
			for(String s : col){
				add(s);
			}
		}
		
		public void add(String value){
			values.add(value);
			if(bound != null && values.size() > bound){
				values.pop();
			}
		}
		
		public void moveToEnd(String value){
			values.remove(value);
			values.add(value);
		}
		
		public List<String> getList(){
			return values;
		}
		
		public String getValue(){
			if(values.size() ==0){
				return "";
			}
			StringBuffer sb = new StringBuffer();
			Iterator<String> it = values.iterator();
			sb.append(it.next());
			while(it.hasNext()){
				sb.append(";");
				sb.append(it.next());
			}
			return sb.toString();
		}
	}
	
	private static String processSectionName(String section){
		section = section.trim();
		if(!section.startsWith("#")){
			section = "#"+section;	
		}
		return section;
		
	}
}
