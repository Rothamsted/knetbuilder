package net.sourceforge.ondex.programcalls.decypher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessGobbler extends Thread  {
	
		private InputStream is;
		private boolean hasOutput;
		private String process;
		
		public ProcessGobbler(InputStream is) {
			this.is = is;
			hasOutput = false;
		}
		
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = br.readLine();
				if (line != null) {
					System.out.println("pg@ external prog out: " + line);
					if (line.indexOf("OK") > -1) {
						process = line.split(" ")[1].trim();
						System.out.println("process="+process);
					}
				}
				while (line != null) {
					line = br.readLine();
					hasOutput = true;
					if (line != null) {
						System.out.println("pg@ external prog out: " + line);
						if (line.indexOf("OK") > -1) {
							process = line.split(" ")[1].trim();
							System.out.println("process="+process);
						}
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public boolean hasOutput() {
			return hasOutput;
		}

		public String getProcess() {
			return process;
		}
	}

