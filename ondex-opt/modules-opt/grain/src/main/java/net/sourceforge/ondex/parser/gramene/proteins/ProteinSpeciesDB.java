package net.sourceforge.ondex.parser.gramene.proteins;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * INDEX for expected data table
 * 0 `species_id` int(11) NOT NULL default '0' PRIMARY KEY 
 * 1 `ncbi_taxa_id` int(11) default NULL,
 * 2 `common_name` varchar(255) default NULL,
 * 3 `lineage_string` text,
 * 4 `genus` varchar(32) default NULL,
 * 5 `species` varchar(32) default NULL,
 * 
 * 
 * Species index creator for translating to ncbi taxid from internal gramene species id
 * @author hindlem
 * @see #getNCBITaxID(int)
 */
public class ProteinSpeciesDB {

	private HashMap<Integer,Integer> idToNCBITaxID = new HashMap<Integer,Integer>();

	public ProteinSpeciesDB(String speciesFile) {
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(speciesFile),"UTF8"));

			Pattern tabPattern = Pattern.compile("\t");
			
			while (input.ready()) {
				String inputLine = input.readLine();
				String[] columns = tabPattern.split(inputLine);

				int internalId = Integer.parseInt(columns[0]);
				int NCBITaxId = Integer.parseInt(columns[1]);

				idToNCBITaxID.put(internalId, NCBITaxId);
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * retrieval system for translating to NCBI taxid
	 * @param internalDBId internal species Id (referenced in other tables)
	 * @return ncbi taxid that should be used in the TAXID gds
	 */
	public String getNCBITaxID(int internalDBId) {
		Integer taxId = idToNCBITaxID.get(internalDBId);
		if (taxId == null) 
			return null;
		else 
			return String.valueOf(taxId);
	}
	
}
