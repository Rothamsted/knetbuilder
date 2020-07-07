package net.sourceforge.ondex.parser.medline;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class ListReader {
	
	BufferedReader in;
	String inputline = "";
	HashSet<String> hash = new HashSet<String>();
	
	public HashSet<String> parse(String s) throws FileNotFoundException, IOException {
	
			//open
			in = new BufferedReader(new FileReader(s));
			
			//parse
			while ((inputline = in.readLine()) != null) {
				
				//check line
				//if line empty
				
				inputline=inputline.trim();
				
				if (!inputline.equals("")) {
					
					//check if more than one value separated by ;
					String[] ids = inputline.split(";");
					
					for (int i=0;i<ids.length;i++)  {
						
						hash.add(ids[i]);
					}
				}
			}
		
			//close
			in.close();
		
		return hash;	
	}
	
}
