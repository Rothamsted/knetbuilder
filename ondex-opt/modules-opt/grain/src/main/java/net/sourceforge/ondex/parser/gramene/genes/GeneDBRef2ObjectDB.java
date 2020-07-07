package net.sourceforge.ondex.parser.gramene.genes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * INDEX for expected data table
 * 
 * 0 `dbxref_to_object_id` int(11) NOT NULL default '0' PRIMARY KEY, 1
 * `table_name` varchar(50) NOT NULL default '', 2 `record_id` int(11) NOT NULL
 * default '0', 3 `dbxref_id` int(11) NOT NULL default '0', 4 `dbxref_value`
 * varchar(50) NOT NULL default ''
 * 
 * 
 * Parsers Xrefs to index structure based on there internal id
 * 
 * @author hoekmanb
 * @see #getAssocation(int)
 */
public class GeneDBRef2ObjectDB {

	private Map<Integer, Set<GeneRef2ObjectRow>> table = new HashMap<Integer, Set<GeneRef2ObjectRow>>();

	/**
	 * parses db xrefs from the provided file
	 * 
	 * @param ref2objectFile
	 */
	public GeneDBRef2ObjectDB(String ref2objectFile) {

		GeneRef2ObjectRow row = null;

		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(
					new FileInputStream(ref2objectFile), "UTF8"));
			Pattern tabPattern = Pattern.compile("\t");

			while (input.ready()) {
				String inputLine = input.readLine();
				String[] columns = tabPattern.split(inputLine);

				String tableName = columns[1];
				int recordId = Integer.parseInt(columns[2]);
				int dbXRefId = Integer.parseInt(columns[3]);
				String dbXRefValue = columns[4];

				// Only interested in gene associations
				if (tableName.equalsIgnoreCase("gene")) {
					row = new GeneRef2ObjectRow();
					row.setdbXRefId(dbXRefId);
					row.setDBXRefValue(dbXRefValue);
					row.setRecordId(recordId);
					row.setTableName(tableName);

					if (!table.containsKey(recordId)) {

						Set<GeneRef2ObjectRow> temp = new HashSet<GeneRef2ObjectRow>();
						temp.add(row);

						this.table.put(recordId, temp);
					} else {
						this.table.get(recordId).add(row);
					}
				}
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
	 * @param objectId
	 *            the object (i.e. xref id)
	 * @return Its external references
	 */
	public Set<GeneRef2ObjectRow> getAssocation(int objectId) {

		if (table.containsKey(objectId)) {
			return table.get(objectId);
		} else {
			return null;
		}
	}
}
