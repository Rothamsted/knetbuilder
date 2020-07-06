package net.sourceforge.ondex.parser.gramene.genes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * INDEX for expected data table
 * 0 `species_id` int(11) NOT NULL default '0' PRIMARY KEY 
 * 1 `ncbi_taxa_id` int(11) default NULL,
 * 2 `gramene_taxa_id` varchar(32) default NULL,
 * 3 `common_name` varchar(255) default NULL,
 * 4 `genus` varchar(32) default NULL,
 * 5 `species` varchar(32) default NULL,
 * 
 * 
 * Species index creator for translating to ncbi taxid from internal gramene species id
 * @author hoekmanb
 * @see #getNCBITaxID(int)
 */
public class GeneSpeciesDB {

	private Map<Integer, Integer> idToNCBITaxID = new HashMap<Integer, Integer>();

	public GeneSpeciesDB(String speciesFile) {
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

				setSpeciesMap(internalId, NCBITaxId);
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * manages access to the species index
	 * @param dbId the internal species id
	 * @param NCBITaxID the taxid that the param represents
	 */
	private void setSpeciesMap(int dbId, int NCBITaxID) {
		idToNCBITaxID.put(dbId, NCBITaxID);
	}

	/**
	 * retrieval system for translating to NCBI taxid
	 * @param internalDBId internal species Id (referenced in other tables)
	 * @return ncbi taxid that should be used in the TAXID gds
	 */
	public String getNCBITaxID(int internalDBId) {
		int taxId = idToNCBITaxID.get(internalDBId);
		if (taxId > -1) {
			return String.valueOf(taxId);
		}
		return null;
	}
}
