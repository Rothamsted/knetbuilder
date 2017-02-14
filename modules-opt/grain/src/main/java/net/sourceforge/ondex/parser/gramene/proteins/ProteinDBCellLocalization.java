package net.sourceforge.ondex.parser.gramene.proteins;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;


/**
 * 0 `gene_product_organelle_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `gene_product_id` int(11) NOT NULL default '0',
 * 2 `organelle` varchar(255) NOT NULL default '',
 * 
 * Parses cell localization information on proteins
 * 
 * @author hindlem
 *
 */
public class ProteinDBCellLocalization {

	private HashMap<Integer, HashSet<String>> proteinIDToCellLocName = new HashMap<Integer, HashSet<String>>();
	
	/**
	 * Indexes the cell localization data in the specified file
	 * @param cellLocalizationFile usualy gene_product_organelle.txt
	 */
	public ProteinDBCellLocalization(String cellLocalizationFile) {
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(cellLocalizationFile),"UTF8"));
			
			Pattern tabPattern = Pattern.compile("\t");
			
			while (input.ready()) {
				String inputLine = input.readLine();

				String[] columns = tabPattern.split(inputLine);
				if (columns.length < 3) { //can be 5 with a description but this is optional
					continue;
				}
				
				String cellPart = columns[2].toLowerCase().trim().intern();
				Integer id = Integer.parseInt(columns[1]);
				
				HashSet<String> objectCellRefs = proteinIDToCellLocName.get(id);
				if (objectCellRefs == null) {
					objectCellRefs = new HashSet<String>(1);
					proteinIDToCellLocName.put(id, objectCellRefs);
				}
				objectCellRefs.add(cellPart);
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param objectId the protein id
	 * @return the name of the cell compartment where they are located
	 */
	public HashSet<String> getCellLocalization(Integer objectId) {
		return proteinIDToCellLocName.get(objectId);
	}
	
}
