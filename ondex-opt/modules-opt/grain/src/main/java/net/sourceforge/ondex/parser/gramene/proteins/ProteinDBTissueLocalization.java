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
 * 
 * 0 `gene_product_tissue_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `gene_product_id` int(11) NOT NULL default '0',
 * 2 `tissue` varchar(255) NOT NULL default '',
 * 
 * Parses tissue localization information on proteins
 * 
 * @author hindlem
 *
 */
public class ProteinDBTissueLocalization {

	private HashMap<Integer, HashSet<String>> proteinIDToTissueLocName = new HashMap<Integer, HashSet<String>>();
	
	/**
	 * Indexes the cell localization data in the specified file
	 * @param cellLocalizationFile usualy gene_product_organelle.txt
	 */
	public ProteinDBTissueLocalization(String cellLocalizationFile) {
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
				
				String tissuePart = columns[2].toLowerCase().trim().intern();
				Integer id = Integer.parseInt(columns[1]);
				
				HashSet<String> objectTisRefs = proteinIDToTissueLocName.get(id);
				if (objectTisRefs == null) {
					objectTisRefs = new HashSet<String>(1);
					proteinIDToTissueLocName.put(id, objectTisRefs);
				}
				objectTisRefs.add(tissuePart);
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
	 * @return the name of the tissue compartment where they are located
	 */
	public HashSet<String> getTissueLocalization(Integer objectId) {
		return proteinIDToTissueLocName.get(objectId);
	}
	
}
