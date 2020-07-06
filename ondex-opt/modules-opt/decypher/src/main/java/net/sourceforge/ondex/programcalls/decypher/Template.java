package net.sourceforge.ondex.programcalls.decypher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author hindlem
 *
 */
public class Template {

	public static boolean DEBUG = true;
	
	private HashMap<String, String> baseTemplateValues = null;

	private static final ConcurrentHashMap<String, HashMap<String, String>> existingBaseValues = new ConcurrentHashMap<String, HashMap<String, String>>() ;
	
	public Template(File baseTemplate) {
		baseTemplateValues = existingBaseValues.get(baseTemplate.getAbsolutePath());

		if (baseTemplateValues == null) {
			baseTemplateValues = new HashMap<String, String>(10);
			try {
				BufferedReader br = new BufferedReader(new FileReader(baseTemplate));
				int i=0 ;
				while (br.ready()) {
					String line = br.readLine();
					int from = line.indexOf('[');
					int to = line.indexOf(']');
					if (from > -1) {
						if (from > -1) {
							String property = line.substring(from, to+1);
							String value = line.substring(to+1, line.length()).trim();
							baseTemplateValues.put(property, value);
							if (property.equalsIgnoreCase(MAXSCORES)) {
								maxScores = Integer.parseInt(value);
							} else if (property.equalsIgnoreCase(MAXALIGNMENTS)) {
								maxAlignments = Integer.parseInt(value);
							}
						} else {
							System.err.println("Invalid line "+i+" in template \""+line+"\"");
						}
					}

					i++;
				}
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		existingBaseValues.put(baseTemplate.getAbsolutePath(), baseTemplateValues);
	}

	private static String COMMENT = "[comment]";
	private static String QUERYFILTER = "[query filter]";
	private static String MAXSCORES = "[max scores]";
	private static String MAXALIGNMENTS = "[max alignments]";
	private static String SIGNIFICANCE = "[significance]";
	private static String THRESHOLD = "[threshold]";
	private static String OUTPUTFORMAT = "[output format]";
	private static String FIELD = "[field]";

	private int maxScores = 100;
	private int maxAlignments = 50;
	private String queryFilter = "t";
	private String significance = "evalue";
	private String threshold = "significance="+Double.valueOf(0.0000001);
	private String outputFormat = "tab fieldrecord";
	private String[] fields = new String[0];


	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public int getMaxScores() {
		return maxScores;
	}

	public void setMaxScores(int maxScores) {
		this.maxScores = maxScores;
	}

	public int getMaxAlignments() {
		return maxAlignments;
	}

	public void setMaxAlignments(int maxAlignments) {
		this.maxAlignments = maxAlignments;
	}

	public void setEvalueAndBitscoreThreshold(float eValue, int score) {
		threshold = "Score="+score+" Significance="+eValue;
	}

	public double getEvalueThreshold() {
		return Double.parseDouble(threshold.substring(threshold.indexOf('='), threshold.length()));
	}

	public void setPropertyValue(String property, String value) {
		property = property.trim();
		if (property.startsWith("[") && property.endsWith("]")) {
			baseTemplateValues.put(property, value);
		} else {
			System.err.println("Property value "+property+" is invalid the format is [property_name]");
		}
	}
	
	public void removePropertyValue(String property) {
		property = property.trim();
		if (property.startsWith("[") && property.endsWith("]")) {
			baseTemplateValues.remove(property);
		} else {
			System.err.println("Property value "+property+" is invalid the format is [property_name]");
		}
	}

	public void writeNewTemplate(File file) throws IOException {
		baseTemplateValues.remove(COMMENT);
		baseTemplateValues.put(MAXSCORES, String.valueOf(maxAlignments));
		baseTemplateValues.put(MAXALIGNMENTS, String.valueOf(maxScores));
		baseTemplateValues.put(SIGNIFICANCE, significance);
		baseTemplateValues.put(THRESHOLD, threshold);
		baseTemplateValues.put(OUTPUTFORMAT, outputFormat);
		baseTemplateValues.put(QUERYFILTER, queryFilter);

		StringBuffer buffer = new StringBuffer();
		for (String field:fields) {
			buffer.append(" "+field);
		}
		baseTemplateValues.put(FIELD, buffer.toString().trim());
		file.delete();
		file.createNewFile();
		if (!file.canWrite()) {
			throw new IOException("File cannot write");
		}
		System.out.println("Writing "+file.getAbsolutePath());
		BufferedWriter bw = new BufferedWriter (new FileWriter(file));
		bw.write(COMMENT+" ONDEX_Generated_Template_for_decypher");
		System.out.println(COMMENT+" ONDEX_Generated_Template_for_decypher");
		bw.newLine();

		if (DEBUG) System.out.println("\n###Template FILE####\n");
		Iterator<String> properties = baseTemplateValues.keySet().iterator();
		while (properties.hasNext()) {
			String property = properties.next();
			String value = baseTemplateValues.get(property);
			bw.write(property+" "+value);
			if (DEBUG) System.out.println(property+" "+value);
			bw.newLine();
		}
		if (DEBUG) System.out.println("");
		bw.flush();
		bw.close();
	}

}
