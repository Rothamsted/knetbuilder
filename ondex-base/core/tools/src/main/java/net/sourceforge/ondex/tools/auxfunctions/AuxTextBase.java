package net.sourceforge.ondex.tools.auxfunctions;


/**
 * 
 * Auxiliary functions for parsers
 * Text base class
 * 
 * Provides a buffered reader and some pre-declared variables
 * 
 * 
 * @author sckuo
 * 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public abstract class AuxTextBase {
	
	private String fileName;
	protected BufferedReader input;
	protected String inputLine; // holder line
	
	public AuxTextBase(String fn) { // fn is file name to be read
		
		this.fileName = fn;
		
			try {
				if(fileName.endsWith(".gz")) {
					GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fileName));
					input = new BufferedReader(new InputStreamReader(gzip));
				}
				else{		
					input = new BufferedReader(new FileReader(fileName));
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
	}
	
}
