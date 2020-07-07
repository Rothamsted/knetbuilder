package net.sourceforge.ondex.tools.auxfunctions;

// will be documented when it's somewhere close to finished

import java.io.IOException;

/**
 * 
 * Auxiliary functions for parsers
 * Fasta Parser
 * 
 * Parses Fasta files into FastaObjects
 * 
 * Usage:
 * 
 * Fasta f = new Fasta (String fn); // fn is the name of the file to be parsed
 * 
 * FastaObject fo = f.getNext(); // retrieve the next FastaObject, returned by order in file * 
 * 
 * @author sckuo
 * 
 */

public class Fasta extends AuxTextBase {
	

	public Fasta(String fn) {
		
		super(fn);
		
		// pre-read the first line
		try {
			
			inputLine = input.readLine();
			
			while (inputLine != null && inputLine.equals("")) {
				inputLine = input.readLine();
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FastaObject getNext() {
		
			FastaObject fObj = null;
			
			try {
				
				StringBuilder block = new StringBuilder();
				String header = null;
				
				// check for previous line read or last line
				
				if (inputLine != null && isHeader(inputLine)) {
						header = inputLine;					
				} 		
				
				while ((inputLine = input.readLine()) != null) {
					
					if (inputLine.equals("")) {
						
						continue;
						
					} else if (isHeader(inputLine)) {
						
						// commit
						fObj = new FastaObject (header, block.toString());
						block.setLength(0);
						break;
						
					} else {
						block.append(inputLine);
					}
			
				}

				// deal with final element
				if (inputLine == null && header != null) {
					
					fObj = new FastaObject (header, block.toString());
				}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return fObj;
			
	}
	
	private boolean isHeader (String a) {
	
		if (a.charAt(0) == '>') {
			return true;
		} else {
			return false;
		}
		
	}
		
}
	
