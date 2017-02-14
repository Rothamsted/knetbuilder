package net.sourceforge.ondex.programcalls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Well known implementation of a Gobbler Thread, takes a stream and stdout or stderr it
 * @author hindlem
 *
 */
public class StreamGobbler extends Thread  {
	
	private InputStream is;
	private boolean hasOutput = false;
	private String prefix;
	private boolean stderr = false;
	private boolean error = false;
	
	/**
	 * 
	 * @param is the stream to gobble
	 */
	public StreamGobbler(InputStream is) {
		this(is, "", false);
	}
	
	/**
	 * 
	 * @param is the stream to gobble
	 * @param prefix prefix to output before stream
	 */
	public StreamGobbler(InputStream is, String prefix) {
		this(is, prefix, false);
	}
	
	/**
	 * 
	 * @param is the stream to gobble
	 * @param stderr output as System.err.print instead of System.out.print
	 */
	public StreamGobbler(InputStream is, boolean stderr) {
		this(is, "", stderr);
	}
	
	/**
	 * 
	 * @param is the stream to gobble
	 * @param prefix prefix to output before stream
	 * @param stderr output as System.err.print instead of System.out.print
	 */
	public StreamGobbler(InputStream is, String prefix, boolean stderr) {
		this.is = is;
		this.prefix=prefix;
		this.stderr  = stderr;
	}
	
	/**
	 * Start Gobbeling Stream
	 */
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();

			if (line != null) {
				if (line.contains("Error") || line.contains("error") ) error = true;
				if (!stderr) System.out.println(prefix+" external prog out: " + line);
				else System.err.println(prefix+" external prog out: " + line);
				hasOutput = true;
			}
			while (line != null) {
				line = br.readLine();
				hasOutput = true;
				if (line != null) {
					if (line.contains("Error") || line.contains("error")) error = true;
					if (!stderr) System.out.println(prefix+" external prog out: " + line);
					else System.err.println(prefix+" external prog out: " + line);
				}
			}
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * 
	 * @return did this Gobbler Gobble anything?
	 */
	public boolean hasOutput() {
		return hasOutput;
	}
	
	/**
	 * 
	 * @return did the line contain the word Error|error
	 */
	public boolean hasError() {
		return error;
	}
}

