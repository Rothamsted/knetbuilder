package net.sourceforge.ondex.parser.medline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.ondex.parser.medline.data.Abstract;


public class ImportSession {
	
	private Set<Integer> filenumbers = new HashSet<Integer>();
	
	private Set<String> fileNames;
	
	String listName = "";
	HashSet<String> pmids = new HashSet<String>();
	Iterator<Integer> files = null;

	
	String importDir = "";
	String prefix = "";
	String ending =".xml";
	
	int upperxmlBoundary = 0;
	int lowerxmlBoundary = 0;
	
	private boolean importList = false;
	
	public void setImportList(String f) throws FileNotFoundException, IOException {
		System.out.println("Set Import List");
		this.listName = f;
		System.out.println("Set Import List"+f);
		//get URL
		this.pmids = new ListReader().parse(f); 
		System.out.println("Read "+pmids.size()+" elements!");
		this.importList=true;
	}
	
	public void setImportList(HashSet<String> a) {
		this.pmids = a;
		this.importList=true;
	}
	
	private ArrayList<String> generateFileList() {
//		System.out.println("generate File List");
		ArrayList<String> arr = new ArrayList<String>();
		
		if(getFileNames() != null){
			//TODO check if it works...
			Iterator<String> fileNamesIt = getFileNames().iterator();
			while (fileNamesIt.hasNext()) {
				String fileName = fileNamesIt.next();
				arr.add(fileName);
			}
		}
		else{
			if (upperxmlBoundary>0 && lowerxmlBoundary>0) {
				
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int y=lowerxmlBoundary;y<=upperxmlBoundary;y++) {
					tmp.add(y);
					files = tmp.iterator();
				}
			}
			else {
				files = filenumbers.iterator();
			}
			
			while (files.hasNext()) {
				//System.out.println("Number in generate File List");
				String number = new String(files.next().toString());
				
				while (number.length()<4) {
					number = "0"+number;
				}
				
				String tmp = importDir+ System.getProperty("file.separator")+prefix+number+ending;
				arr.add(tmp);
			}
		
		}
		
		return arr;
	}
	
	public void setImportDir(String d) {
		this.importDir = d;
	}
	
	public void setCompression(String s) {
		
		if (s.equals("gz")) {
			
			this.ending=this.ending+"."+s;
		} else {
			
			this.ending="."+s;
		}
	}
	
	public String getEnding() {
		return this.ending;
	}
	
	public String getImportDir() {
		return this.importDir;
	}
	public HashSet<String> getImportList() {
		return this.pmids;
	}
	public boolean importListContains(String s) {
		if (pmids.contains(s)) {
			return true;
		}
		return false;
	}
	public void setupperxmlBoundary(int i) {
		this.upperxmlBoundary = i;
	}
	public void setlowerxmlBoundary(int i) {
			this.lowerxmlBoundary = i;
	}
	public int getlowerxmlBoundary() {
		return this.lowerxmlBoundary;
	}
	public int getupperxmlBoundary() {
		return this.upperxmlBoundary;
	}
	public Iterator<String> getFullFileNames() {
		return this.generateFileList().iterator();
	}
	public String getFilePrefix() {
		return prefix;
	}
	public String setFilePrefix(String p) {
		return this.prefix = p;
	}
	
	public void addXmlNumbers(HashSet<Integer> ints) {
		this.filenumbers.addAll(ints);
	}
	
	public void setXmlNumbers(HashSet<Integer> ints) {
		this.filenumbers=ints;
	}
	
	public Set<Integer> getFilenumbers() {
		return filenumbers;
	}
	
	public boolean isImportList() {
		return this.importList;
	}
	public boolean applyFilter(Abstract a) {
		return Filter.check(a,this);
	}

	public Set<String> getFileNames() {
		return fileNames;
	}

	public void setFileNames(Set<String> fileNames) {
		this.fileNames = fileNames;
	}
}
