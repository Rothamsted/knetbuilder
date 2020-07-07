package net.sourceforge.ondex.parser.habitat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.exception.type.ParsingFailedException;

/**
 * Instances of this class can be used to read flat files of the
 * NCL Habitat DB project. The instance is a separate Thread and
 * provides all necessary data for comfortable parsing.
 * 
 * Use like this:
 * <code>
 * String[] cols = null;
 * TabFileReader r = new TabFileReader(fileRegister.get("taxon_protein.txt"));
 * r.start();
 * int taxIndex = r.getColumnIndex("taxonid");
 * int protIndex = r.getColumnIndex("tagid");
 * while ((cols = r.poll()) != null) {
 *     String taxoId = cols[taxIndex];
 *     String protId = cols[protIndex];
 *     //do something with your data
 * } 
 * if (r.getError() != null) {
 *     throw r.getError();
 * }
 * </code>
 * 
 * @author Jochen Weile
 *
 */
public class TabFileReader extends Thread {
	
	/**
	 * Pipe for the data elements.
	 */
	private ConcurrentLinkedQueue<ArrayWrapper> q;
	
	/**
	 * to keep the threads synchronised.
	 */
	private Semaphore s;
	
	/**
	 * maps the column names to the column indices.
	 */
	private HashMap<String,Integer> colNames;
	
	/**
	 * helper flags.
	 */
	private boolean done = false, ready = false;
	
	/**
	 * the file to be parsed.
	 */
	private File file;
	
	/**
	 * any caught exeptions.
	 */
	private Throwable throwable;
	
	/**
	 * constructor.
	 * @param file the file to parse
	 */
	public TabFileReader(File file) {
		super("FileReader Thread");
		this.file = file;
		q = new ConcurrentLinkedQueue<ArrayWrapper>();
		s = new Semaphore(10);
		colNames = new HashMap<String,Integer>();
	}
	
	/**
	 * @return all column keys
	 */
	public Set<String> getColumnKeys() {
		return colNames.keySet();
	}
	
	/**
	 * @param key the column key
	 * @return the index of the column with the given key
	 */
	public int getColumnIndex(String key) {
		return colNames.get(key);
	}
	
	/**
	 * @return the next array of elements or <code>null</code> if end of file
	 * waits for the next element if necessary.
	 */
	public String[] poll() {
		s.release();
		ArrayWrapper a = q.poll();
		while (a == null && !done) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
			a = q.poll();
		}
		if (a == null) {
			return null;
		} else {
			return a.getArray();
		}
	}
	
	/**
	 * @return any caught errors that might have occurred during
	 * thread execution.
	 */
	public Throwable getError() {
		return throwable;
	}
	
	/**
	 * parses the file, feeding into the pipe, that can be polled by the actual parser.
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			
			//get headers
			String[] cols = br.readLine().split("\\|");
			for (int i = 0; i < cols.length; i++) {
				String header_i = cols[i].trim();
				colNames.put(header_i,i);
			}
			ready = true;
			
			//skip second line
			br.readLine();
			
			//start actual parsing
			String line;
			int rows = 0;
			Pattern pattern = Pattern.compile("\\((\\d+) rows\\)");
			while ((line = br.readLine()) != null) {
				if (!line.startsWith(" ")) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.matches()) {//example: (125 rows)
						//end of file
						done();
						int expectedRows = Integer.parseInt(matcher.group(1));
						if (rows != expectedRows) {
							System.err.println("Unexpected number of rows: "+rows+" != "+expectedRows);
						}
						break;
					}
				} else {
					//parseable row
					cols = line.split("\\|");
					if (cols.length != colNames.keySet().size()) {
						throw new ParsingFailedException("Inconsistent number of columns!");
					}
					String[] clean = new String[cols.length];
					for (int i = 0; i < cols.length; i++) {
						clean[i] = cols[i].trim();
					}
					enqueue(clean);
					rows++;
				}
			}
			br.close();
			done();
		} catch (Throwable e) {
			throwable = e;
			done();
			e.printStackTrace();
		}
	}
	
	/**
	 * starts the thread and then waits until the reader has
	 * reached ready state.
	 * @see java.lang.Thread#start()
	 */
	public void start() {
		super.start();
		while (!ready) {
			Thread.yield();
		}
	}
	
	/**
	 * sets done.
	 */
	private void done() {
		done = true;
	}
	
	/**
	 * enqueues a new array into the pipe.
	 * @param array
	 */
	private void enqueue(String[] array) {
		try {
			s.acquire();
			q.offer(new ArrayWrapper(array));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * helper class to store arrays in a queue
	 * @author jweile
	 *
	 */
	private class ArrayWrapper {
		private String[] array;
		public ArrayWrapper(String[] array) {
			this.array = array;
		}
		public String[] getArray() {
			return array;
		}
	}
}
