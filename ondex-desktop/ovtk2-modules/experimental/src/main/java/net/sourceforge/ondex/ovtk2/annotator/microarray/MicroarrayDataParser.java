package net.sourceforge.ondex.ovtk2.annotator.microarray;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * A standard mircroarray data parser that extracts from the defined format a MicroarrayDataIndex
 * @author hindlem
 *
 */
public class MicroarrayDataParser {

	private String SPLIT_TYPE = "\t";
	private Double logBase;

	/**
	 * 
	 * @param name experiment name
	 */
	public MicroarrayDataParser(String name) {
		this(name, null);
	}
	
	/**
	 * 
	 * @param name experiment name
	 * @param logBase the log of the values (can be null if they are raw data)
	 */
	public MicroarrayDataParser(String name, Double logBase) {
		this.logBase = logBase;
	}
	
	/**
	 * 
	 * @param filename the file to parse microarray data from
	 * @param conditionRow the row that contains conditions
	 * @param seqRow the row where sequences start
	 * @param lsdCol a col that contains lsd values for the expression data
	 */
	public MicroarrayDataIndex parseData(String filename, 
			int conditionRow,
			int seqRow,
			Integer lsdCol) {
		
		if (conditionRow > seqRow) {
			throw new RuntimeException("sequences must be defined last");
		}
		
		MicroarrayDataIndex index = new MicroarrayDataIndex(logBase != null && logBase > 0);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));

			HashMap<Integer, String> colIndex = new HashMap<Integer, String>();
			
			int row = 0;
			
			while(br.ready()) {
				String line = br.readLine();
				String[] values = line.split(SPLIT_TYPE);
				
				if (row == conditionRow) {
					
					for (int i = 0; i < values.length; i++) {
						colIndex.put(i, values[i]);
					}
					
				} else if (row >= conditionRow) {
					
					String probeName = values[0];
					
					HashMap<String, Double> treatmentToExpression = new HashMap<String, Double>();
					Double lsd = null;
					
					for (int i = 1; i < values.length; i++) {
						double value = Double.parseDouble(values[i]);
						
						if (lsdCol != null && i >= lsdCol) {
							lsd = value;
							break;
						}
						
						String condition = colIndex.get(i);
						treatmentToExpression.put(condition, value);	
					}
					index.setProbe(probeName, treatmentToExpression, lsd);
				}
				
				row++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return index;
	}
	
}
