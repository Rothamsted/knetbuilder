package net.sourceforge.ondex.parser.gramene;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Expect tab table with the following data (objectxref.txt)
 * 0 `objectxref_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `table_name` varchar(60) NOT NULL default '',
 * 2 `row_id` int(11) NOT NULL default '0',
 * 3 `dbxref_id` int(11) NOT NULL default '0',
 * 
 * Manages references from objectids to xref ids
 * 
 * @author hindlem
 * @see net.sourceforge.ondex.parser.gramene.proteins.ProteinDBXref for translation of xrefIDs
 */
public class Object2XrefParser {

	private HashMap<Integer, HashSet<Integer>> object2XrefIndex = new HashMap<Integer, HashSet<Integer>> ();

	/**
	 * Creates an index on an objectxref.txt file as specified in the doc
	 * @param objectXrefFile the file containg the specified objectxref table in tab form
	 * @param tableRestriction restrict exclusively to the specified table (can be null)
	 */
	public Object2XrefParser(String objectXrefFile, String tableRestriction) {
		try {
			Pattern p = Pattern.compile("\t");
			
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(objectXrefFile),"UTF8"));

			while (input.ready()) {

				String inputLine = input.readLine();
				String[] columns = p.split(inputLine);

				if (columns.length < 4) {
					continue; //no useful info
				}

				String table = columns[1];

				if (tableRestriction != null && !tableRestriction.equalsIgnoreCase(table)) {
					continue;
				}

				int objectID = Integer.parseInt(columns[2].trim());

				HashSet<Integer> xrefs = object2XrefIndex.get(objectID);
				if (xrefs == null) {
					xrefs = new HashSet<Integer>();
					object2XrefIndex.put(objectID, xrefs);
				}
				xrefs.add(Integer.parseInt(columns[3].trim())); //add xrefdb id
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
	 * @param objectid the object id
	 * @return all xrefs for this object
	 */
	public HashSet<Integer> getXrefs(int objectid) {
		return object2XrefIndex.get(objectid);
	}
	
}
